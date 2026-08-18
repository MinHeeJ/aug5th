package kr.ac.knue.commonfoundation.menu;

public record MenuListItem(
    String menuId,
    String parentMenuId,
    String parentMenuName,
    String menuName,
    String screenId,
    String url,
    int displayOrder,
    long childCount,
    long permissionCount,
    String menuUsageRule
) {
}
