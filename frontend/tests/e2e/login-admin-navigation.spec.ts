import { afterEach, describe, expect, it, vi } from "vitest";
import {
  firstLeafUrl,
  listNavigationMenus,
  login,
} from "../../src/auth/session";

const navigation = [
  {
    menuId: "root",
    menuName: "시스템 관리",
    menuLevel: "MAIN",
    displayOrder: 1,
    children: [
      {
        menuId: "middle",
        menuName: "사용자·조직",
        menuLevel: "MIDDLE",
        displayOrder: 1,
        children: [
          {
            menuId: "leaf",
            menuName: "사용자 관리",
            menuLevel: "SUB",
            displayOrder: 1,
            url: "/admin/users",
            children: [],
          },
        ],
      },
    ],
  },
];

describe("로그인 후 관리자 navigation 계약", () => {
  afterEach(() => vi.restoreAllMocks());

  it("로그인과 navigation 조회는 상대 /api 경로를 사용하고 첫 leaf URL을 계산한다", async () => {
    vi.stubGlobal(
      "fetch",
      vi.fn(async (path: RequestInfo | URL) => {
        expect(String(path)).toMatch(/^\/api\//);
        const data = String(path).includes("navigation")
          ? navigation
          : {
              loginId: "admin",
              staffName: "시스템 관리자",
              roleCodes: ["R09"],
            };
        return new Response(
          JSON.stringify({ success: true, meta: { timestamp: "test" }, data }),
          { headers: { "content-type": "application/json" } },
        );
      }),
    );

    await expect(login("admin", "admin")).resolves.toMatchObject({
      loginId: "admin",
    });
    await expect(listNavigationMenus()).resolves.toEqual(navigation);
    expect(firstLeafUrl(navigation)).toBe("/admin/users");
  });
});
