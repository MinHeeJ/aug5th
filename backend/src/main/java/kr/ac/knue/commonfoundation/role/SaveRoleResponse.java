package kr.ac.knue.commonfoundation.role;

public record SaveRoleResponse(
    String roleCode,
    boolean enabled,
    String defaultDataScope,
    String message
) {
}
