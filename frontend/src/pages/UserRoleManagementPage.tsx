import { FormEvent, useEffect, useMemo, useState } from "react";
import { ApiClientError, apiFetch } from "../api/client";

type ApiEnvelope<T> = {
  success: boolean;
  data: T;
  message?: string;
  error?: { message?: string; fields?: Record<string, string> };
};

type UserRole = {
  userRoleId?: string;
  userId: string;
  loginId?: string;
  staffName?: string;
  roleCode: string;
  roleName?: string;
  assignmentType: "POSITION" | "MANUAL";
  validFrom: string;
  validTo?: string | null;
  approvedByUserId?: string | null;
  approvedByLoginId?: string | null;
  revokedAt?: string | null;
  isUsed: boolean;
};

const roleCodes = [
  "R01",
  "R02",
  "R03",
  "R04",
  "R05",
  "R06",
  "R07",
  "R08",
  "R09",
];
const defaultUserId = "";
const defaultApproverId = "20000000-0000-0000-0000-000000000001";

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
      fields: payload.error?.fields ?? {},
      permission: error.status === 401 || error.status === 403,
    };
  }
  return {
    message: "네트워크 오류가 발생했습니다.",
    fields: {},
    permission: false,
  };
}

function today() {
  return new Date().toISOString().slice(0, 10);
}

export default function UserRoleManagementPage() {
  const [userId, setUserId] = useState(defaultUserId);
  const [roleFilter, setRoleFilter] = useState("");
  const [rows, setRows] = useState<UserRole[]>([]);
  const [selectedRoleCode, setSelectedRoleCode] = useState("");
  const [selectedCodes, setSelectedCodes] = useState<string[]>([]);
  const [assignmentType, setAssignmentType] = useState<"MANUAL" | "POSITION">(
    "MANUAL",
  );
  const [validFrom, setValidFrom] = useState(today());
  const [validTo, setValidTo] = useState("");
  const [approvedByUserId, setApprovedByUserId] = useState(defaultApproverId);
  const [revokeSelected, setRevokeSelected] = useState(false);
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");
  const [fields, setFields] = useState<Record<string, string>>({});
  const [permissionDenied, setPermissionDenied] = useState(false);

  const selected = useMemo(
    () => rows.find((row) => row.roleCode === selectedRoleCode) ?? null,
    [rows, selectedRoleCode],
  );

  async function loadUserRoles(nextUserId = userId) {
    setLoading(true);
    setError("");
    setPermissionDenied(false);
    const params = new URLSearchParams();
    if (nextUserId) params.set("userId", nextUserId);
    if (roleFilter) params.set("roleCode", roleFilter);
    try {
      const response = await apiFetch<ApiEnvelope<UserRole[]>>(
        `/api/admin/user-roles${params.toString() ? `?${params}` : ""}`,
      );
      const nextRows = response.data ?? [];
      setRows(nextRows);
      const first = nextRows[0];
      if (first) {
        bindRow(first);
      } else {
        setSelectedRoleCode("");
        setSelectedCodes([]);
        setMessage(
          nextUserId
            ? "현재 부여된 역할이 없습니다. 하나 이상의 roleCode를 선택해 저장하세요."
            : "사용자별 현재 역할을 먼저 조회하세요.",
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
    void loadUserRoles("");
  }, []);

  function bindRow(row: UserRole) {
    setUserId(row.userId);
    setSelectedRoleCode(row.roleCode);
    setSelectedCodes([row.roleCode]);
    setAssignmentType(row.assignmentType);
    setValidFrom(row.validFrom ?? today());
    setValidTo(row.validTo ?? "");
    setApprovedByUserId(row.approvedByUserId ?? defaultApproverId);
    setRevokeSelected(!row.isUsed || Boolean(row.revokedAt));
    setFields({});
    setError("");
  }

  function toggleRoleCode(roleCode: string, checked: boolean) {
    setSelectedCodes((current) =>
      checked
        ? [...new Set([...current, roleCode])]
        : current.filter((code) => code !== roleCode),
    );
  }

  function resetForm() {
    if (selected) {
      bindRow(selected);
      setMessage("마지막 조회 결과로 복원했습니다.");
    } else {
      setSelectedCodes([]);
      setValidFrom(today());
      setValidTo("");
      setRevokeSelected(false);
      setMessage("사용자별 현재 역할을 먼저 조회하세요.");
    }
  }

  async function saveUserRoles(event: FormEvent) {
    event.preventDefault();
    if (!userId) {
      setError("선택 userId를 입력하세요.");
      return;
    }
    if (selectedCodes.length === 0) {
      setError("하나 이상의 역할을 선택하세요.");
      setFields({ roles: "하나 이상의 역할을 선택하세요." });
      return;
    }
    setLoading(true);
    setError("");
    setFields({});
    const roles = selectedCodes.map((roleCode) => ({
      userId,
      roleCode,
      assignmentType,
      validFrom,
      validTo: validTo || null,
      approvedByUserId: approvedByUserId || null,
      isUsed: !revokeSelected,
    }));
    try {
      const response = await apiFetch<ApiEnvelope<UserRole[]>>(
        `/api/admin/user-roles/${userId}`,
        { method: "PUT", body: { roles } },
      );
      setRows(response.data ?? []);
      setMessage("사용자 역할이 저장되었습니다");
      await loadUserRoles(userId);
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
          시스템 관리 &gt; 역할·권한 관리 &gt; 사용자 역할 관리
        </p>
        <h1 className="text-2xl font-bold">사용자 역할 관리</h1>
        <p className="text-slate-600">
          사용자별 현재 역할, 유효기간, 승인자, POSITION/MANUAL 구분을
          관리합니다.
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
        <div className="grid gap-4 md:grid-cols-[1fr_220px_auto]">
          <label className="block text-sm font-medium">
            userId
            <input
              aria-label="userId"
              value={userId}
              onChange={(event) => setUserId(event.target.value)}
              className="mt-1 w-full rounded-lg border p-2 font-mono"
            />
          </label>
          <label className="block text-sm font-medium">
            roleCode
            <select
              aria-label="roleCode filter"
              value={roleFilter}
              onChange={(event) => setRoleFilter(event.target.value)}
              className="mt-1 w-full rounded-lg border p-2"
            >
              <option value="">전체</option>
              {roleCodes.map((code) => (
                <option key={code} value={code}>
                  {code}
                </option>
              ))}
            </select>
          </label>
          <button
            type="button"
            disabled={loading}
            onClick={() => void loadUserRoles(userId)}
            className="self-end rounded-lg bg-slate-900 px-4 py-2 text-white disabled:opacity-50"
          >
            조회
          </button>
        </div>
        {loading && (
          <p className="mt-3 text-slate-500">사용자 역할을 불러오는 중...</p>
        )}
      </div>

      <div className="grid gap-6 xl:grid-cols-[1.2fr_0.8fr]">
        <div className="overflow-hidden rounded-2xl bg-white shadow-sm">
          <table className="min-w-full text-sm">
            <thead className="bg-slate-100 text-left text-slate-600">
              <tr>
                <th className="p-3">userId</th>
                <th className="p-3">roleCode</th>
                <th className="p-3">assignmentType</th>
                <th className="p-3">validFrom</th>
                <th className="p-3">validTo</th>
                <th className="p-3">승인자</th>
                <th className="p-3">상태</th>
              </tr>
            </thead>
            <tbody>
              {rows.map((row) => (
                <tr
                  key={`${row.userRoleId}-${row.roleCode}-${row.revokedAt ?? "active"}`}
                  onClick={() => bindRow(row)}
                  className={`cursor-pointer border-t ${row.roleCode === selectedRoleCode ? "bg-blue-50" : "bg-white"}`}
                >
                  <td className="p-3 font-mono text-xs">{row.userId}</td>
                  <td className="p-3 font-mono">{row.roleCode}</td>
                  <td className="p-3">
                    <span className="rounded-full bg-slate-100 px-2 py-1">
                      {row.assignmentType}
                    </span>
                  </td>
                  <td className="p-3">{row.validFrom}</td>
                  <td className="p-3">{row.validTo ?? "-"}</td>
                  <td className="p-3 font-mono text-xs">
                    {row.approvedByUserId ?? "-"}
                  </td>
                  <td className="p-3">
                    {row.isUsed ? "active" : "revoked"}{" "}
                    {row.revokedAt ? `(${row.revokedAt})` : ""}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
          {rows.length === 0 && !loading && (
            <p className="p-4 text-slate-500">
              현재 부여된 역할이 없습니다. 하나 이상의 roleCode를 선택해
              저장하세요.
            </p>
          )}
          <p className="border-t p-4 text-sm text-slate-600">
            구분 안내: POSITION=보직 기반 역할, MANUAL=수동 부여 역할
          </p>
        </div>

        <form
          onSubmit={saveUserRoles}
          className="space-y-4 rounded-2xl bg-white p-5 shadow-sm"
        >
          <h2 className="text-lg font-semibold">
            선택 사용자 역할 편집 / 부여·변경·회수
          </h2>
          <label className="block text-sm font-medium">
            선택 userId
            <input
              value={userId}
              readOnly
              className="mt-1 w-full rounded-lg border bg-slate-100 p-2 font-mono"
            />
          </label>
          <fieldset className="rounded-xl border p-3">
            <legend className="px-1 text-sm font-medium">
              roleCode 하나 이상 선택
            </legend>
            <div className="grid grid-cols-3 gap-2">
              {roleCodes.map((code) => (
                <label
                  key={code}
                  className="flex items-center gap-2 rounded-lg border p-2"
                >
                  <input
                    aria-label={code}
                    type="checkbox"
                    checked={selectedCodes.includes(code)}
                    onChange={(event) =>
                      toggleRoleCode(code, event.target.checked)
                    }
                  />{" "}
                  {code}
                </label>
              ))}
            </div>
            {fields.roles && (
              <p className="mt-2 text-sm text-rose-700">{fields.roles}</p>
            )}
          </fieldset>
          <label className="block text-sm font-medium">
            assignmentType
            <select
              aria-label="assignmentType"
              value={assignmentType}
              onChange={(event) =>
                setAssignmentType(event.target.value as "POSITION" | "MANUAL")
              }
              className="mt-1 w-full rounded-lg border p-2"
            >
              <option value="MANUAL">MANUAL - 수동 부여 역할</option>
              <option value="POSITION">POSITION - 보직 기반 역할</option>
            </select>
          </label>
          {fields.assignmentType && (
            <p className="text-sm text-rose-700">{fields.assignmentType}</p>
          )}
          <label className="block text-sm font-medium">
            validFrom
            <input
              aria-label="validFrom"
              type="date"
              value={validFrom}
              onChange={(event) => setValidFrom(event.target.value)}
              className="mt-1 w-full rounded-lg border p-2"
            />
          </label>
          {fields.validFrom && (
            <p className="text-sm text-rose-700">{fields.validFrom}</p>
          )}
          <label className="block text-sm font-medium">
            validTo
            <input
              aria-label="validTo"
              type="date"
              value={validTo}
              onChange={(event) => setValidTo(event.target.value)}
              className="mt-1 w-full rounded-lg border p-2"
            />
          </label>
          {fields.validTo && (
            <p className="text-sm text-rose-700">{fields.validTo}</p>
          )}
          <label className="block text-sm font-medium">
            approvedByUserId
            <input
              aria-label="approvedByUserId"
              value={approvedByUserId}
              onChange={(event) => setApprovedByUserId(event.target.value)}
              className="mt-1 w-full rounded-lg border p-2 font-mono"
            />
          </label>
          {fields.approvedByUserId && (
            <p className="text-sm text-rose-700">{fields.approvedByUserId}</p>
          )}
          <label className="flex items-center gap-2 text-sm font-medium">
            <input
              aria-label="회수 대상"
              type="checkbox"
              checked={revokeSelected}
              onChange={(event) => setRevokeSelected(event.target.checked)}
            />{" "}
            회수 대상(revokedAt/isUsed 갱신)
          </label>
          <div className="rounded-lg bg-slate-50 p-3 text-sm text-slate-600">
            OQ-UI-051: 승인자는 기본적으로 현재 시스템관리자 ID를 사용하며 별도
            선택 방식은 추후 확정합니다.
          </div>
          <div className="flex gap-2">
            <button
              type="submit"
              disabled={loading || !userId || selectedCodes.length === 0}
              className="rounded-lg bg-blue-600 px-4 py-2 text-white disabled:opacity-50"
            >
              저장
            </button>
            <button
              type="button"
              onClick={resetForm}
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
