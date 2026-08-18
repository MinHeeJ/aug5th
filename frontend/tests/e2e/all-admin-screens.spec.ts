import { describe, expect, it } from "vitest";
import { adminRoutes, routeTitle } from "../../src/routes/adminRoutes";

const expectedScreens = [
  ["/admin/users", "사용자 관리"],
  ["/admin/organizations", "조직 관리"],
  ["/admin/roles", "역할 관리"],
  ["/admin/user-roles", "사용자 역할 관리"],
  ["/admin/menu-permissions", "메뉴 권한 관리"],
  ["/admin/menu-structure", "메뉴 구조 관리"],
  ["/admin/menus", "메뉴 정보 관리"],
  ["/admin/code-groups", "코드그룹 관리"],
  ["/admin/code-groups/:groupId/codes", "상세코드 관리"],
];

describe("관리자 9개 화면 route 계약", () => {
  it("모든 1차 관리자 화면 route와 제목을 제공한다", () => {
    expect(adminRoutes.map(({ path, title }) => [path, title])).toEqual(
      expectedScreens,
    );
  });

  it("동적 상세코드 route 제목을 실제 URL에서도 계산한다", () => {
    expect(routeTitle("/admin/code-groups/COMMON/codes")).toBe("상세코드 관리");
  });
});
