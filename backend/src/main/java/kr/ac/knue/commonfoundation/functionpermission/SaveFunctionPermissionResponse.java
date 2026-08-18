package kr.ac.knue.commonfoundation.functionpermission;

public record SaveFunctionPermissionResponse(
    Long functionPermissionId,
    Boolean allowed,
    String roleCode,
    String screenId,
    String actionCode,
    String message
) {
}
