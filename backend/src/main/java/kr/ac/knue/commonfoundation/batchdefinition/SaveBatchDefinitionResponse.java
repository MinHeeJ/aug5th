package kr.ac.knue.commonfoundation.batchdefinition;

public record SaveBatchDefinitionResponse(
    String batchId,
    String status,
    String message
) {
}
