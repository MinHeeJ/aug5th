package kr.ac.knue.commonfoundation.batchexecution;

public record BatchExecutionListItem(
    Long batchExecutionId,
    String batchId,
    String batchName,
    String parameters,
    String reason,
    String executionStatus,
    String executionStatusName,
    String requestedBy,
    String requestedByName,
    String operationRule
) {
}
