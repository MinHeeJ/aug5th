package kr.ac.knue.commonfoundation.batchresult;

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
public class BatchResultManagementService {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final BatchResultMapper batchResultMapper;
    private final CurrentUserContext currentUserContext;

    public BatchResultManagementService(BatchResultMapper batchResultMapper, CurrentUserContext currentUserContext) {
        this.batchResultMapper = batchResultMapper;
        this.currentUserContext = currentUserContext;
    }

    @Transactional(readOnly = true)
    public BatchResultListResponse listBatchResults(BatchResultSearchCondition condition) {
        return new BatchResultListResponse(
            batchResultMapper.selectBatchResults(condition),
            condition.page(),
            condition.size(),
            batchResultMapper.countBatchResults(condition),
            "SCR-BATCH-RESULT",
            "R09"
        );
    }

    @Transactional
    public BatchResultLogResponse getBatchResultLog(long batchResultId) {
        SessionPrincipal principal = currentUserContext.current()
            .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "인증이 필요합니다."));
        BatchResultListItem result = findResult(batchResultId);
        if (result.logFileId() == null) {
            throw new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "연결된 배치 로그파일이 없습니다.");
        }
        batchResultMapper.insertAudit(
            "batch_results:" + batchResultId + ":log",
            principal.user().userId(),
            jsonValue(result),
            jsonValue(Map.of(
                "access", "LOG_READ",
                "batchResultId", result.batchResultId(),
                "batchExecutionId", result.batchExecutionId(),
                "logFileId", result.logFileId(),
                "rule", "배치 실행ID와 연결된 로그만 반환하고 로그를 수정·삭제하지 않습니다."
            ))
        );
        return new BatchResultLogResponse(
            result.batchResultId(),
            result.batchExecutionId(),
            result.logFileId(),
            result.logFileName(),
            "배치 실행ID " + result.batchExecutionId() + " 로그 파일 조회가 허용되었습니다.",
            "결과 조회 화면에서는 로그를 수정·삭제하지 않습니다."
        );
    }

    @Transactional
    public SaveBatchResultResponse recordReadOnlySaveRequest() {
        SessionPrincipal principal = currentUserContext.current()
            .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "인증이 필요합니다."));
        batchResultMapper.insertAudit(
            "batch_results:read-only-save",
            principal.user().userId(),
            "{}",
            jsonValue(Map.of(
                "status", "READ_ONLY",
                "screenId", "SCR-BATCH-RESULT",
                "rule", "배치 결과 조회 화면에서는 결과·실패자료·로그파일을 수정하지 않습니다."
            ))
        );
        return new SaveBatchResultResponse(
            "READ_ONLY",
            "SCR-BATCH-RESULT",
            "배치 결과 조회 화면은 읽기 전용입니다. 조회 상태만 기록했습니다.",
            "결과 조회 화면에서는 재실행하거나 실패자료·로그파일을 수정·삭제하지 않습니다."
        );
    }

    private BatchResultListItem findResult(long batchResultId) {
        if (batchResultId < 1 || !batchResultMapper.existsBatchResult(batchResultId)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "배치 결과를 찾을 수 없습니다.");
        }
        return batchResultMapper.selectBatchResult(batchResultId);
    }

    private static String jsonValue(Object value) {
        try {
            return OBJECT_MAPPER.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("배치 결과 감사 로그 JSON 생성에 실패했습니다.", exception);
        }
    }

    private static String jsonValue(BatchResultListItem item) {
        Map<String, Object> value = new LinkedHashMap<>();
        value.put("batchResultId", item.batchResultId());
        value.put("batchExecutionId", item.batchExecutionId());
        value.put("batchId", item.batchId());
        value.put("logFileId", item.logFileId());
        value.put("rule", "배치 결과 조회는 재실행·실패자료 변경·로그 수정삭제를 수행하지 않습니다.");
        return jsonValue(value);
    }
}
