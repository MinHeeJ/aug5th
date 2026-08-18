import { FormEvent, useEffect, useMemo, useState } from "react";
import { ApiClientError, apiFetch } from "../api/client";

type ApiEnvelope<T> = {
  success: boolean;
  data: T;
  message?: string;
  error?: { message?: string; fields?: Record<string, string> };
};

type Role = {
  roleCode: string;
  roleName: string;
  rolePurpose: string;
  assignmentCriteria: string;
  defaultDataScope: string;
  isUsed: boolean;
};

const emptyFields: Record<string, string> = {};

function extractError(error: unknown): {
  message: string;
  fields: Record<string, string>;
  permission: boolean;
} {
  if (error instanceof ApiClientError) {
    const payload = error.payload as {
      error?: { message?: string; fields?: Record<string, string> };
    };
    return {
      message: payload.error?.message ?? "요청 처리 중 오류가 발생했습니다.",
      fields: payload.error?.fields ?? emptyFields,
      permission: error.status === 401 || error.status === 403,
    };
  }
  return {
    message: "네트워크 오류가 발생했습니다.",
    fields: emptyFields,
    permission: false,
  };
}

export default function RoleManagementPage() {
  const [roles, setRoles] = useState<Role[]>([]);
  const [selectedCode, setSelectedCode] = useState<string>("");
  const [draft, setDraft] = useState<Role | null>(null);
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");
  const [fields, setFields] = useState<Record<string, string>>({});
  const [permissionDenied, setPermissionDenied] = useState(false);

  const selected = useMemo(
    () => roles.find((role) => role.roleCode === selectedCode) ?? null,
    [roles, selectedCode],
  );

  async function loadRoles(nextSelectedCode = selectedCode) {
    setLoading(true);
    setError("");
    setPermissionDenied(false);
    try {
      const response = await apiFetch<ApiEnvelope<Role[]>>("/api/admin/roles");
      const nextRoles = response.data ?? [];
      setRoles(nextRoles);
      const nextSelected =
        nextRoles.find((role) => role.roleCode === nextSelectedCode) ??
        nextRoles[0] ??
        null;
      setSelectedCode(nextSelected?.roleCode ?? "");
      setDraft(nextSelected ? { ...nextSelected } : null);
      if (nextRoles.length === 0) {
        setMessage(
          "R01~R09 역할 seed가 없습니다. seed-data/Flyway를 확인하세요.",
        );
      }
    } catch (caught) {
      const parsed = extractError(caught);
      setError(parsed.message);
      setFields(parsed.fields);
      setPermissionDenied(parsed.permission);
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    void loadRoles("");
  }, []);

  function selectRole(role: Role) {
    setSelectedCode(role.roleCode);
    setDraft({ ...role });
    setFields({});
    setError("");
  }

  function resetDraft() {
    setDraft(selected ? { ...selected } : null);
    setFields({});
    setMessage(
      selected ? "마지막 조회 결과로 복원했습니다." : "역할을 선택하세요.",
    );
  }

  async function saveRole(event: FormEvent) {
    event.preventDefault();
    if (!draft) {
      setError("역할을 선택하세요.");
      return;
    }
    setLoading(true);
    setError("");
    setFields({});
    try {
      const { roleCode: _readonlyRoleCode, ...body } = draft;
      await apiFetch<ApiEnvelope<Role>>(`/api/admin/roles/${draft.roleCode}`, {
        method: "PUT",
        body,
      });
      setMessage("역할 기준정보가 저장되었습니다");
      await loadRoles(draft.roleCode);
    } catch (caught) {
      const parsed = extractError(caught);
      setError(parsed.message);
      setFields(parsed.fields);
      setPermissionDenied(parsed.permission);
    } finally {
      setLoading(false);
    }
  }

  if (permissionDenied) {
    return (
      <section className="rounded-2xl bg-white p-6 shadow-sm">
        <h1 className="text-xl font-bold">권한이 없습니다</h1>
        <p>{error}</p>
      </section>
    );
  }

  return (
    <section className="space-y-6">
      <header className="rounded-2xl bg-white p-6 shadow-sm">
        <p className="text-sm text-slate-500">
          시스템 관리 &gt; 역할·권한 관리 &gt; 역할 관리
        </p>
        <h1 className="text-2xl font-bold">역할 관리</h1>
        <p className="text-slate-600">
          R01~R09 역할 기준정보를 관리합니다. roleCode는 읽기 전용입니다.
        </p>
      </header>

      {message && (
        <div className="rounded-xl bg-emerald-50 p-4 text-emerald-800">
          {message}
        </div>
      )}
      {error && (
        <div className="rounded-xl bg-rose-50 p-4 text-rose-800">{error}</div>
      )}

      <div className="rounded-2xl bg-white p-5 shadow-sm">
        <button
          type="button"
          disabled={loading}
          onClick={() => void loadRoles(selectedCode)}
          className="rounded-lg bg-slate-900 px-4 py-2 text-white disabled:opacity-50"
        >
          역할 목록 새로고침
        </button>
        {loading && (
          <p className="mt-3 text-slate-500">역할 목록을 불러오는 중...</p>
        )}
        {!loading && roles.length === 0 && (
          <p className="mt-3 text-slate-500">R01~R09 역할 seed가 없습니다.</p>
        )}
      </div>

      <div className="grid gap-6 lg:grid-cols-[1.1fr_0.9fr]">
        <div className="overflow-hidden rounded-2xl bg-white shadow-sm">
          <table className="min-w-full text-sm">
            <thead className="bg-slate-100 text-left text-slate-600">
              <tr>
                <th className="p-3">roleCode</th>
                <th className="p-3">roleName</th>
                <th className="p-3">rolePurpose</th>
                <th className="p-3">isUsed</th>
              </tr>
            </thead>
            <tbody>
              {roles.map((role) => (
                <tr
                  key={role.roleCode}
                  onClick={() => selectRole(role)}
                  className={`cursor-pointer border-t ${role.roleCode === selectedCode ? "bg-blue-50" : "bg-white"}`}
                >
                  <td className="p-3 font-mono">{role.roleCode}</td>
                  <td className="p-3">{role.roleName}</td>
                  <td className="p-3">{role.rolePurpose}</td>
                  <td className="p-3">{String(role.isUsed)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        <form
          onSubmit={saveRole}
          className="space-y-4 rounded-2xl bg-white p-5 shadow-sm"
        >
          <h2 className="text-lg font-semibold">선택 역할 상세/편집</h2>
          <label className="block text-sm font-medium">
            roleCode
            <input
              aria-label="roleCode"
              value={draft?.roleCode ?? ""}
              disabled
              className="mt-1 w-full rounded-lg border bg-slate-100 p-2 font-mono"
            />
          </label>
          {fields.roleCode && (
            <p className="text-sm text-rose-700">{fields.roleCode}</p>
          )}
          <label className="block text-sm font-medium">
            roleName
            <input
              aria-label="roleName"
              value={draft?.roleName ?? ""}
              onChange={(event) =>
                draft && setDraft({ ...draft, roleName: event.target.value })
              }
              className="mt-1 w-full rounded-lg border p-2"
            />
          </label>
          {fields.roleName && (
            <p className="text-sm text-rose-700">{fields.roleName}</p>
          )}
          <label className="block text-sm font-medium">
            rolePurpose
            <textarea
              aria-label="rolePurpose"
              value={draft?.rolePurpose ?? ""}
              onChange={(event) =>
                draft && setDraft({ ...draft, rolePurpose: event.target.value })
              }
              className="mt-1 w-full rounded-lg border p-2"
            />
          </label>
          {fields.rolePurpose && (
            <p className="text-sm text-rose-700">{fields.rolePurpose}</p>
          )}
          <label className="block text-sm font-medium">
            assignmentCriteria
            <textarea
              aria-label="assignmentCriteria"
              value={draft?.assignmentCriteria ?? ""}
              onChange={(event) =>
                draft &&
                setDraft({ ...draft, assignmentCriteria: event.target.value })
              }
              className="mt-1 w-full rounded-lg border p-2"
            />
          </label>
          {fields.assignmentCriteria && (
            <p className="text-sm text-rose-700">{fields.assignmentCriteria}</p>
          )}
          <label className="block text-sm font-medium">
            defaultDataScope
            <input
              aria-label="defaultDataScope"
              value={draft?.defaultDataScope ?? ""}
              onChange={(event) =>
                draft &&
                setDraft({ ...draft, defaultDataScope: event.target.value })
              }
              className="mt-1 w-full rounded-lg border p-2"
            />
          </label>
          {fields.defaultDataScope && (
            <p className="text-sm text-rose-700">{fields.defaultDataScope}</p>
          )}
          <label className="flex items-center gap-2 text-sm font-medium">
            <input
              type="checkbox"
              checked={draft?.isUsed ?? false}
              onChange={(event) =>
                draft && setDraft({ ...draft, isUsed: event.target.checked })
              }
            />{" "}
            isUsed
          </label>
          <div className="flex gap-2">
            <button
              type="submit"
              disabled={!draft || loading}
              className="rounded-lg bg-blue-600 px-4 py-2 text-white disabled:opacity-50"
            >
              저장
            </button>
            <button
              type="button"
              onClick={resetDraft}
              className="rounded-lg border px-4 py-2"
            >
              취소
            </button>
          </div>
        </form>
      </div>
    </section>
  );
}
