package kr.ac.knue.cms.menus;

import java.util.List;
import java.util.UUID;

public record NavigationMenu(
    UUID menuId,
    UUID parentMenuId,
    String menuLevel,
    int displayOrder,
    String menuName,
    String screenId,
    String url,
    String icon,
    String businessDivision,
    List<NavigationMenu> children
) {
}
