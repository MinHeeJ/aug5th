package kr.ac.knue.commonfoundation.menupermission;

public record MenuPermissionListItem(
    Long menuPermissionId,
    String targetType,
    String targetId,
    String targetName,
    String menuId,
    String menuName,
    String parentMenuName,
    String screenId,
    String url,
    Boolean allowed,
    String permissionSource,
    Integer displayOrder
) {
}
