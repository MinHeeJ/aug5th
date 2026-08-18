package kr.ac.knue.commonfoundation.batchdefinition;

import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.ac.knue.commonfoundation.api.ApiException;
import kr.ac.knue.commonfoundation.auth.CurrentUserContext;
import kr.ac.knue.commonfoundation.auth.SessionPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BatchDefinitionManagementService {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final BatchDefinitionMapper batchDefinitionMapper;
    private final CurrentUserContext currentUserContext;

    public BatchDefinitionManagementService(BatchDefinitionMapper batchDefinitionMapper, CurrentUserContext currentUserContext) {
        this.batchDefinitionMapper = batchDefinitionMapper;
        this.currentUserContext = currentUserContext;
    }

    @Transactional(readOnly = true)
    public BatchDefinitionListResponse listBatchDefinitions(BatchDefinitionSearchCondition condition) {
        return new BatchDefinitionListResponse(
            batchDefinitionMapper.selectBatchDefinitions(condition),
            condition.page(),
            condition.size(),
            batchDefinitionMapper.countBatchDefinitions(condition),
            "SCR-BATCH-DEFINITION",
            "R09"
        );
    }

    @Transactional
    public SaveBatchDefinitionResponse saveBatchDefinition(SaveBatchDefinitionRequest request) {
        SessionPrincipal principal = currentUserContext.current()
            .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "인증이 필요합니다."));
        String batchId = request.batchId();
        String predecessorBatchId = request.normalizedPredecessorBatchId();
        if (batchId.equals(predecessorBatchId)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "선행 배치는 자기 자신일 수 없습니다.", Map.of("predecessorBatchId", "자기 자신을 선행 배치로 지정할 수 없습니다."));
        }
        if (predecessorBatchId != null && !batchDefinitionMapper.existsBatchDefinition(predecessorBatchId)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "선행 배치를 찾을 수 없습니다.", Map.of("predecessorBatchId", "등록된 선행 배치 ID를 입력하세요."));
        }
        if (!batchDefinitionMapper.existsOwner(request.normalizedOwnerId())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "담당자를 찾을 수 없습니다.", Map.of("ownerId", "등록된 담당자 ID를 입력하세요."));
        }
        String parameters = normalizeJson(request.normalizedParameters());
        BatchDefinitionListItem before = batchDefinitionMapper.existsBatchDefinition(batchId)
            ? batchDefinitionMapper.selectBatchDefinition(batchId)
            : null;
        batchDefinitionMapper.upsertBatchDefinition(
            batchId,
            request.normalizedSchedule(),
            predecessorBatchId,
            parameters,
            request.normalizedMaxRuntimeSeconds(),
            request.normalizedOwnerId()
        );
        BatchDefinitionListItem after = batchDefinitionMapper.selectBatchDefinition(batchId);
        batchDefinitionMapper.insertAudit(
            "batch_definitions:" + batchId,
            principal.user().userId(),
            jsonValue(before, null),
            jsonValue(after, request.normalizedReason())
        );
        return new SaveBatchDefinitionResponse(batchId, after.status(), "배치 정의 관리 저장이 완료되었습니다.");
    }

    private static String normalizeJson(String parameters) {
        try {
            return OBJECT_MAPPER.writeValueAsString(OBJECT_MAPPER.readTree(parameters));
        } catch (JsonProcessingException exception) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "파라미터는 JSON 형식이어야 합니다.", Map.of("parameters", "유효한 JSON 객체를 입력하세요."));
        }
    }

    private static String jsonValue(BatchDefinitionListItem item, String reason) {
        Map<String, Object> value = new LinkedHashMap<>();
        if (item == null) {
            value.put("exists", false);
        } else {
            value.put("batchId", item.batchId());
            value.put("schedule", item.schedule());
            value.put("predecessorBatchId", item.predecessorBatchId());
            value.put("parameters", item.parameters());
            value.put("maxRuntimeSeconds", item.maxRuntimeSeconds());
            value.put("ownerId", item.ownerId());
            value.put("status", item.status());
        }
        if (reason != null) {
            value.put("reason", reason);
            value.put("rule", "배치 정의 관리는 실행·중지·재실행을 수행하지 않고 정의만 저장합니다.");
        }
        try {
            return OBJECT_MAPPER.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("배치 정의 감사 로그 JSON 생성에 실패했습니다.", exception);
        }
    }
}
