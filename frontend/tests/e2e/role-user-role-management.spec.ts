import { describe, expect, it } from "vitest";
import { adminRoutes } from "../../src/routes/adminRoutes";

const byPath = (path: string) =>
  adminRoutes.find((route) => route.path === path);

describe("역할 및 사용자 역할 관리 화면 계약", () => {
  it("역할 관리와 사용자 역할 관리는 R09 관리자 route shell에 등록된다", () => {
    expect(byPath("/admin/roles")).toMatchObject({
      title: "역할 관리",
      menuPath: expect.stringContaining("역할·권한"),
    });
    expect(byPath("/admin/user-roles")).toMatchObject({
      title: "사용자 역할 관리",
      menuPath: expect.stringContaining("역할·권한"),
    });
  });

  it("역할 관련 route는 절대 backend URL을 포함하지 않는다", () => {
    for (const route of [byPath("/admin/roles"), byPath("/admin/user-roles")]) {
      expect(JSON.stringify(route)).not.toMatch(/https?:\/\//);
    }
  });
});
