package kr.ac.knue.commonfoundation.batchdefinition;

public record BatchDefinitionListItem(
    String batchId,
    String batchName,
    String schedule,
    String predecessorBatchId,
    String predecessorBatchName,
    String parameters,
    int maxRuntimeSeconds,
    String ownerId,
    String ownerName,
    String status,
    String statusName,
    String operationRule
) {
}
