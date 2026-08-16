package kr.ac.knue.commonfoundation.batchexecution;

public record BatchExecutionActionResponse(
    Long batchExecutionId,
    String batchId,
    String executionStatus,
    String message
) {
}
