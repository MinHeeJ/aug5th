package kr.ac.knue.commonfoundation.menupermission;

public record SaveMenuPermissionResponse(
    Long menuPermissionId,
    Boolean allowed,
    String targetType,
    String targetId,
    String message
) {
}
