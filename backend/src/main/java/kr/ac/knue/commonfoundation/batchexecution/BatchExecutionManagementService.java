package kr.ac.knue.commonfoundation.batchexecution;

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
public class BatchExecutionManagementService {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final BatchExecutionMapper batchExecutionMapper;
    private final CurrentUserContext currentUserContext;

    public BatchExecutionManagementService(BatchExecutionMapper batchExecutionMapper, CurrentUserContext currentUserContext) {
        this.batchExecutionMapper = batchExecutionMapper;
        this.currentUserContext = currentUserContext;
    }

    @Transactional(readOnly = true)
    public BatchExecutionListResponse listBatchExecutions(BatchExecutionSearchCondition condition) {
        return new BatchExecutionListResponse(
            batchExecutionMapper.selectBatchExecutions(condition),
            condition.page(),
            condition.size(),
            batchExecutionMapper.countBatchExecutions(condition),
            "SCR-BATCH-EXECUTION",
            "R09"
        );
    }

    @Transactional
    public BatchExecutionActionResponse runBatch(RunBatchRequest request) {
        SessionPrincipal principal = principal();
        String batchId = request.batchId();
        if (!batchExecutionMapper.existsBatchDefinition(batchId)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "배치 정의를 찾을 수 없습니다.", Map.of("id", "등록된 배치 ID를 선택하세요."));
        }
        String parameters = normalizeJson(request.normalizedParameters());
        Long executionId = batchExecutionMapper.insertBatchExecution(batchId, parameters, request.normalizedReason(), "RUNNING", principal.user().userId());
        BatchExecutionListItem after = batchExecutionMapper.selectBatchExecution(executionId);
        batchExecutionMapper.insertAudit(
            "batch_executions:" + executionId,
            principal.user().userId(),
            jsonValue(null, null),
            jsonValue(after, request.normalizedReason())
        );
        return new BatchExecutionActionResponse(executionId, batchId, after.executionStatus(), "배치 수동실행 요청이 기록되었습니다.");
    }

    @Transactional
    public BatchExecutionActionResponse stopBatch(long executionId, BatchExecutionActionRequest request) {
        return transitionRunningExecution(executionId, request.normalizedReason(), "CANCELLED", "배치 중지 요청이 기록되었습니다.");
    }

    @Transactional
    public BatchExecutionActionResponse rerunBatch(long executionId, BatchExecutionActionRequest request) {
        SessionPrincipal principal = principal();
        BatchExecutionListItem before = findExecution(executionId);
        String parameters = normalizeJson(before.parameters());
        Long newExecutionId = batchExecutionMapper.insertBatchExecution(before.batchId(), parameters, request.normalizedReason(), "RUNNING", principal.user().userId());
        BatchExecutionListItem after = batchExecutionMapper.selectBatchExecution(newExecutionId);
        batchExecutionMapper.insertAudit(
            "batch_executions:" + newExecutionId,
            principal.user().userId(),
            jsonValue(before, "재실행 원본"),
            jsonValue(after, request.normalizedReason())
        );
        return new BatchExecutionActionResponse(newExecutionId, after.batchId(), after.executionStatus(), "배치 재실행 요청이 기록되었습니다.");
    }

    private BatchExecutionActionResponse transitionRunningExecution(long executionId, String reason, String nextStatus, String message) {
        SessionPrincipal principal = principal();
        BatchExecutionListItem before = findExecution(executionId);
        int updated = batchExecutionMapper.updateRunningExecutionStatus(executionId, nextStatus, reason);
        if (updated != 1) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "실행 중인 배치만 중지할 수 있습니다.", Map.of("id", "REQUESTED 또는 RUNNING 상태의 실행을 선택하세요."));
        }
        BatchExecutionListItem after = batchExecutionMapper.selectBatchExecution(executionId);
        batchExecutionMapper.insertAudit(
            "batch_executions:" + executionId,
            principal.user().userId(),
            jsonValue(before, null),
            jsonValue(after, reason)
        );
        return new BatchExecutionActionResponse(executionId, after.batchId(), after.executionStatus(), message);
    }

    private BatchExecutionListItem findExecution(long executionId) {
        if (executionId < 1 || !batchExecutionMapper.existsBatchExecution(executionId)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "배치 실행 이력을 찾을 수 없습니다.");
        }
        return batchExecutionMapper.selectBatchExecution(executionId);
    }

    private SessionPrincipal principal() {
        return currentUserContext.current()
            .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "인증이 필요합니다."));
    }

    private static String normalizeJson(String parameters) {
        try {
            return OBJECT_MAPPER.writeValueAsString(OBJECT_MAPPER.readTree(parameters));
        } catch (JsonProcessingException exception) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "파라미터는 JSON 형식이어야 합니다.", Map.of("parameters", "유효한 JSON 객체를 입력하세요."));
        }
    }

    private static String jsonValue(BatchExecutionListItem item, String reason) {
        Map<String, Object> value = new LinkedHashMap<>();
        if (item == null) {
            value.put("exists", false);
        } else {
            value.put("batchExecutionId", item.batchExecutionId());
            value.put("batchId", item.batchId());
            value.put("parameters", item.parameters());
            value.put("executionStatus", item.executionStatus());
            value.put("requestedBy", item.requestedBy());
            value.put("rule", "배치 실행 관리는 정의·실행주기·선후행 관계와 원천 업무자료를 직접 변경하지 않습니다.");
        }
        if (reason != null) {
            value.put("reason", reason);
        }
        try {
            return OBJECT_MAPPER.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("배치 실행 감사 로그 JSON 생성에 실패했습니다.", exception);
        }
    }
}
