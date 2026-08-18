import { afterEach, describe, expect, it, vi } from "vitest";

function ok(data: unknown) {
  return vi.fn(async (path: RequestInfo | URL, init?: RequestInit) => {
    expect(String(path)).toMatch(/^\/api\//);
    return new Response(
      JSON.stringify({ success: true, meta: { timestamp: "test" }, data }),
      { headers: { "content-type": "application/json" } },
    );
  });
}

describe("메뉴 관리 API 계약", () => {
  afterEach(() => vi.restoreAllMocks());

  it("권한, 구조, 실행 정보 저장은 상대 /api 경로와 지정 HTTP method를 사용한다", async () => {
    vi.stubGlobal("fetch", ok([]));
    const admin = await import("../../src/api/admin");

    await admin.saveMenuPermissions("ROLE", "R09", [
      { menuId: "00000000-0000-0000-0000-000000000205", isAllowed: true },
    ]);
    await admin.saveMenuStructure("00000000-0000-0000-0000-000000000207", {
      menuLevel: "SUB",
      displayOrder: 2,
      menuName: "메뉴 정보 관리",
      isUsed: true,
    });
    await admin.updateMenuStatus("00000000-0000-0000-0000-000000000207", false);

    expect(fetch).toHaveBeenCalledWith(
      "/api/admin/menu-permissions/ROLE/R09",
      expect.objectContaining({ method: "PUT" }),
    );
    expect(fetch).toHaveBeenCalledWith(
      "/api/admin/menu-structure/00000000-0000-0000-0000-000000000207",
      expect.objectContaining({ method: "PUT" }),
    );
    expect(fetch).toHaveBeenCalledWith(
      "/api/admin/menus/00000000-0000-0000-0000-000000000207/status",
      expect.objectContaining({ method: "PATCH" }),
    );
  });
});
