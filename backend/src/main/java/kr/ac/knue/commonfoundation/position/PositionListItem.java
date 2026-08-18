package kr.ac.knue.commonfoundation.position;

public record PositionListItem(
    Long positionId,
    String positionCode,
    String positionName,
    String userId,
    String userName,
    String employeeNo,
    String organizationCode,
    String organizationName,
    String validFrom,
    String validTo,
    Boolean active
) {
}
