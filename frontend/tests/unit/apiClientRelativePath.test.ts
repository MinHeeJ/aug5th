import { describe, expect, it, vi } from "vitest";
import { apiFetch, assertRelativeApiPath } from "../../src/api/client";

describe("api client relative path guard", () => {
  it("accepts only browser-relative /api paths", () => {
    expect(() => assertRelativeApiPath("/api/health")).not.toThrow();
    expect(() =>
      assertRelativeApiPath("/api/admin/users?staffName=%EA%B9%80"),
    ).not.toThrow();
  });

  it("rejects absolute localhost docker service and non-api paths before fetch is called", async () => {
    const fetchSpy = vi.spyOn(globalThis, "fetch");

    await expect(
      apiFetch("https://backend.example.invalid/api/health"),
    ).rejects.toThrow("browser-relative /api path");
    await expect(apiFetch("http://backend:8080/api/health")).rejects.toThrow(
      "browser-relative /api path",
    );
    await expect(apiFetch("//backend:8080/api/health")).rejects.toThrow(
      "browser-relative /api path",
    );
    await expect(apiFetch("/admin/users")).rejects.toThrow(
      "browser-relative /api path",
    );

    expect(fetchSpy).not.toHaveBeenCalled();
    fetchSpy.mockRestore();
  });

  it("sends fetch requests to the same relative path and preserves credentials", async () => {
    const response = new Response(
      JSON.stringify({ success: true, meta: {}, data: { ok: true } }),
      {
        status: 200,
        headers: { "Content-Type": "application/json" },
      },
    );
    const fetchMock = vi.spyOn(globalThis, "fetch").mockResolvedValue(response);

    await apiFetch("/api/health");

    expect(fetchMock).toHaveBeenCalledWith(
      "/api/health",
      expect.objectContaining({ credentials: "include" }),
    );
    fetchMock.mockRestore();
  });
});
