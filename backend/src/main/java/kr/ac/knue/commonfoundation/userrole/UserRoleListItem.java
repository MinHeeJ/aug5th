package kr.ac.knue.commonfoundation.userrole;

public record UserRoleListItem(
    Long userRoleId,
    String userId,
    String userName,
    String employeeNo,
    String roleCode,
    String roleName,
    String validFrom,
    String validTo,
    String approverId,
    String approverName,
    String assignmentSource,
    Boolean active
) {
}
