package kr.ac.knue.cms.menus;

import java.util.UUID;

public class NavigationMenuRow {
    private String menuId;
    private String parentMenuId;
    private String menuLevel;
    private int displayOrder;
    private String menuName;
    private String screenId;
    private String url;
    private String icon;
    private String businessDivision;

    public UUID getMenuId() {
        return menuId == null ? null : UUID.fromString(menuId);
    }

    public void setMenuId(String menuId) {
        this.menuId = menuId;
    }

    public UUID getParentMenuId() {
        return parentMenuId == null ? null : UUID.fromString(parentMenuId);
    }

    public void setParentMenuId(String parentMenuId) {
        this.parentMenuId = parentMenuId;
    }

    public String getMenuLevel() {
        return menuLevel;
    }

    public void setMenuLevel(String menuLevel) {
        this.menuLevel = menuLevel;
    }

    public int getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(int displayOrder) {
        this.displayOrder = displayOrder;
    }

    public String getMenuName() {
        return menuName;
    }

    public void setMenuName(String menuName) {
        this.menuName = menuName;
    }

    public String getScreenId() {
        return screenId;
    }

    public void setScreenId(String screenId) {
        this.screenId = screenId;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getBusinessDivision() {
        return businessDivision;
    }

    public void setBusinessDivision(String businessDivision) {
        this.businessDivision = businessDivision;
    }
}
