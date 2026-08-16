package kr.ac.knue.commonfoundation.userrole;

public record SaveUserRoleResponse(
    Long userRoleId,
    Boolean active,
    String validTo,
    String assignmentSource,
    String message
) {
}
