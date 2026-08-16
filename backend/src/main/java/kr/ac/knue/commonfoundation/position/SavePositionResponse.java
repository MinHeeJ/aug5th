package kr.ac.knue.commonfoundation.position;

public record SavePositionResponse(
    Long positionId,
    Boolean active,
    String validTo,
    String message
) {
}
