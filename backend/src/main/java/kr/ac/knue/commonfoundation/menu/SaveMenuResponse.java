package kr.ac.knue.commonfoundation.menu;

public record SaveMenuResponse(
    String menuId,
    String menuName,
    String screenId,
    String url,
    int displayOrder,
    String message
) {
}
