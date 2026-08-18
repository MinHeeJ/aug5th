import { afterEach, describe, expect, it, vi } from "vitest";
import { assertRelativeApiPath } from "../../src/api/client";

function jsonFetch(data: unknown) {
  return vi.fn(async (path: RequestInfo | URL) => {
    expect(String(path)).toMatch(/^\/api\//);
    expect(String(path)).not.toMatch(/^https?:\/\//);
    return new Response(
      JSON.stringify({ success: true, meta: { timestamp: "test" }, data }),
      { headers: { "content-type": "application/json" } },
    );
  });
}

describe("코드 관리 API 경로 계약", () => {
  afterEach(() => vi.restoreAllMocks());

  it("브라우저 상대 /api 경로만 허용한다", () => {
    expect(() => assertRelativeApiPath("/api/admin/code-groups")).not.toThrow();
    expect(() =>
      assertRelativeApiPath(
        "https://backend.example.invalid/api/admin/code-groups",
      ),
    ).toThrow();
  });

  it("코드그룹과 상세코드 화면은 상대 API를 통해 조회한다", async () => {
    vi.stubGlobal("fetch", jsonFetch([]));
    const admin = await import("../../src/api/admin");
    await admin.listMenus();
    await admin.listMenuPermissions({ targetType: "ROLE", targetId: "R09" });
    expect(fetch).toHaveBeenCalledWith("/api/admin/menus", expect.any(Object));
    expect(fetch).toHaveBeenCalledWith(
      "/api/admin/menu-permissions?targetType=ROLE&targetId=R09",
      expect.any(Object),
    );
  });
});
