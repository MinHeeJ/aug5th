package kr.ac.knue.commonfoundation.batchresult;

public record BatchResultLogResponse(
    Long batchResultId,
    Long batchExecutionId,
    Long logFileId,
    String logFileName,
    String accessMessage,
    String immutableRule
) {
}
