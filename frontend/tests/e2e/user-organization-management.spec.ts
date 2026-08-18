import { afterEach, describe, expect, it, vi } from "vitest";

describe("사용자·조직 기준정보 API 계약", () => {
  afterEach(() => vi.restoreAllMocks());

  it("사용자 조회/시스템 접근 변경/조직 조회는 상대 /api 경로를 사용한다", async () => {
    vi.stubGlobal(
      "fetch",
      vi.fn(async (path: RequestInfo | URL) => {
        expect(String(path)).toMatch(/^\/api\//);
        expect(String(path)).not.toMatch(/^https?:\/\//);
        return new Response(
          JSON.stringify({
            success: true,
            meta: { timestamp: "test" },
            data: [],
          }),
          { headers: { "content-type": "application/json" } },
        );
      }),
    );

    const admin = await import("../../src/api/admin");
    await admin.listUsers({ roleCode: "R09", systemEnabled: "true" });
    await admin.updateUserSystemAccess("20000000-0000-0000-0000-000000000002", {
      isSystemEnabled: false,
      roleCodes: ["R01"],
      changeReason: "테스트",
    });
    await admin.listOrganizations({ organizationCode: "KNUE" });
    await admin.getOrganizationTree();

    expect(fetch).toHaveBeenCalledWith(
      "/api/admin/users?roleCode=R09&systemEnabled=true",
      expect.any(Object),
    );
    expect(fetch).toHaveBeenCalledWith(
      "/api/admin/users/20000000-0000-0000-0000-000000000002/system-access",
      expect.objectContaining({ method: "PATCH" }),
    );
    expect(fetch).toHaveBeenCalledWith(
      "/api/admin/organization-tree",
      expect.any(Object),
    );
  });
});
