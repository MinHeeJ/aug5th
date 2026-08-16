package kr.ac.knue.commonfoundation.batchresult;

public record SaveBatchResultResponse(
    String status,
    String screenId,
    String message,
    String operationRule
) {
}
