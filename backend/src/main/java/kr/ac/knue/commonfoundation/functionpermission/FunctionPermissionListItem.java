package kr.ac.knue.commonfoundation.functionpermission;

public record FunctionPermissionListItem(
    Long functionPermissionId,
    String roleCode,
    String roleName,
    String screenId,
    String screenName,
    String menuId,
    String menuName,
    String actionCode,
    String actionName,
    Boolean allowed,
    String permissionScope,
    Integer displayOrder
) {
}
