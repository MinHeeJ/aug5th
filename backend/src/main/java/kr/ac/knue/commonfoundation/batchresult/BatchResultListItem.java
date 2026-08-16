package kr.ac.knue.commonfoundation.batchresult;

import java.time.LocalDateTime;

public record BatchResultListItem(
    Long batchResultId,
    Long batchExecutionId,
    String batchId,
    String batchName,
    LocalDateTime startedAt,
    LocalDateTime endedAt,
    Integer totalCount,
    Integer successCount,
    Integer failureCount,
    Integer excludedCount,
    Long durationMs,
    Long logFileId,
    String logFileName,
    String resultStatus,
    String resultStatusName,
    String logAccessRule,
    String operationRule
) {
}
