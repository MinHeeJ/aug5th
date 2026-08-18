package kr.ac.knue.commonfoundation.role;

public record RoleListItem(
    String roleCode,
    String roleName,
    String purpose,
    String grantCriteria,
    String defaultDataScope,
    boolean enabled,
    long assignedUserCount,
    long menuPermissionCount,
    long functionPermissionCount
) {
}
