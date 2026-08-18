import { FormEvent, useEffect, useMemo, useState } from "react";
import { ApiClientError, apiFetch } from "../api/client";

type ApiEnvelope<T> = {
  success: boolean;
  data: T;
  message?: string;
  error?: { message?: string; fields?: Record<string, string> };
};

type CodeGroup = {
  groupId: string;
  groupName: string;
  description: string | null;
  managingDepartment: string | null;
  isUsed: boolean;
};

const emptyDraft: CodeGroup = {
  groupId: "",
  groupName: "",
  description: "",
  managingDepartment: "",
  isUsed: true,
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

export default function CodeGroupManagementPage() {
  const [groups, setGroups] = useState<CodeGroup[]>([]);
  const [filter, setFilter] = useState("");
  const [selectedGroupId, setSelectedGroupId] = useState("");
  const [draft, setDraft] = useState<CodeGroup>(emptyDraft);
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");
  const [fields, setFields] = useState<Record<string, string>>({});
  const [permissionDenied, setPermissionDenied] = useState(false);

  const selected = useMemo(
    () => groups.find((group) => group.groupId === selectedGroupId) ?? null,
    [groups, selectedGroupId],
  );

  async function loadGroups(nextSelectedGroupId = selectedGroupId) {
    setLoading(true);
    setError("");
    setPermissionDenied(false);
    try {
      const query = filter.trim()
        ? `?filter=${encodeURIComponent(filter.trim())}`
        : "";
      const response = await apiFetch<ApiEnvelope<CodeGroup[]>>(
        `/api/admin/code-groups${query}`,
      );
      const nextGroups = response.data ?? [];
      setGroups(nextGroups);
      const nextSelected =
        nextGroups.find((group) => group.groupId === nextSelectedGroupId) ??
        nextGroups[0] ??
        null;
      setSelectedGroupId(nextSelected?.groupId ?? "");
      setDraft(nextSelected ? { ...nextSelected } : emptyDraft);
      if (nextGroups.length === 0) {
        setMessage(
          "조건에 맞는 코드그룹이 없습니다. 신규 그룹을 입력해 저장할 수 있습니다.",
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
    void loadGroups("");
  }, []);

  function selectGroup(group: CodeGroup) {
    setSelectedGroupId(group.groupId);
    setDraft({ ...group });
    setFields({});
    setMessage("선택한 코드그룹을 편집합니다.");
    setError("");
  }

  function newGroup() {
    setSelectedGroupId("");
    setDraft(emptyDraft);
    setFields({});
    setMessage("신규 코드그룹을 입력하세요.");
    setError("");
  }

  function resetDraft() {
    setDraft(selected ? { ...selected } : emptyDraft);
    setFields({});
    setMessage(
      selected
        ? "마지막 조회 결과로 복원했습니다."
        : "신규 입력을 초기화했습니다.",
    );
  }

  async function saveGroup(event: FormEvent) {
    event.preventDefault();
    const targetGroupId = draft.groupId.trim();
    if (!targetGroupId) {
      setFields({ groupId: "그룹ID는 필수입니다." });
      setError("입력값을 확인해 주세요.");
      return;
    }
    setLoading(true);
    setError("");
    setFields({});
    try {
      await apiFetch<ApiEnvelope<CodeGroup>>(
        `/api/admin/code-groups/${encodeURIComponent(targetGroupId)}`,
        {
          method: "PUT",
          body: { ...draft, groupId: targetGroupId },
        },
      );
      setMessage("코드그룹이 저장되었습니다");
      await loadGroups(targetGroupId);
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
          시스템 관리 &gt; 공통코드 관리 &gt; 코드그룹 관리
        </p>
        <h1 className="text-2xl font-bold">코드그룹 관리</h1>
        <p className="text-slate-600">
          평가영역·처리상태·인증구분 등 코드 묶음의 그룹ID, 명칭, 설명,
          관리부서를 관리합니다.
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

      <form
        onSubmit={(event) => {
          event.preventDefault();
          void loadGroups(selectedGroupId);
        }}
        className="rounded-2xl bg-white p-5 shadow-sm"
      >
        <label className="block text-sm font-medium">
          검색어
          <input
            aria-label="코드그룹 검색어"
            value={filter}
            onChange={(event) => setFilter(event.target.value)}
            className="mt-1 w-full rounded-lg border p-2"
            placeholder="그룹ID, 명칭, 설명, 관리부서"
          />
        </label>
        <div className="mt-3 flex gap-2">
          <button
            type="submit"
            disabled={loading}
            className="rounded-lg bg-slate-900 px-4 py-2 text-white disabled:opacity-50"
          >
            조회
          </button>
          <button
            type="button"
            onClick={newGroup}
            className="rounded-lg border px-4 py-2"
          >
            신규
          </button>
        </div>
        {loading && (
          <p className="mt-3 text-slate-500">코드그룹을 불러오는 중...</p>
        )}
        {!loading && groups.length === 0 && (
          <p className="mt-3 text-slate-500">
            조건에 맞는 코드그룹이 없습니다.
          </p>
        )}
      </form>

      <div className="grid gap-6 lg:grid-cols-[1.1fr_0.9fr]">
        <div className="overflow-hidden rounded-2xl bg-white shadow-sm">
          <table className="min-w-full text-sm">
            <thead className="bg-slate-100 text-left text-slate-600">
              <tr>
                <th className="p-3">groupId</th>
                <th className="p-3">groupName</th>
                <th className="p-3">관리부서</th>
                <th className="p-3">상세</th>
              </tr>
            </thead>
            <tbody>
              {groups.map((group) => (
                <tr
                  key={group.groupId}
                  onClick={() => selectGroup(group)}
                  className={`cursor-pointer border-t ${group.groupId === selectedGroupId ? "bg-blue-50" : "bg-white"}`}
                >
                  <td className="p-3 font-mono">{group.groupId}</td>
                  <td className="p-3">{group.groupName}</td>
                  <td className="p-3">{group.managingDepartment}</td>
                  <td className="p-3">
                    <a
                      className="text-blue-700 underline"
                      href={`/admin/code-groups/${encodeURIComponent(group.groupId)}/codes`}
                    >
                      상세코드
                    </a>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        <form
          onSubmit={saveGroup}
          className="space-y-4 rounded-2xl bg-white p-5 shadow-sm"
        >
          <h2 className="text-lg font-semibold">코드그룹 상세/편집</h2>
          <label className="block text-sm font-medium">
            groupId
            <input
              aria-label="groupId"
              value={draft.groupId}
              disabled={Boolean(selectedGroupId)}
              onChange={(event) =>
                setDraft({ ...draft, groupId: event.target.value })
              }
              className="mt-1 w-full rounded-lg border p-2 font-mono disabled:bg-slate-100"
            />
          </label>
          {fields.groupId && (
            <p className="text-sm text-rose-700">{fields.groupId}</p>
          )}
          <label className="block text-sm font-medium">
            groupName
            <input
              aria-label="groupName"
              value={draft.groupName ?? ""}
              onChange={(event) =>
                setDraft({ ...draft, groupName: event.target.value })
              }
              className="mt-1 w-full rounded-lg border p-2"
            />
          </label>
          {fields.groupName && (
            <p className="text-sm text-rose-700">{fields.groupName}</p>
          )}
          <label className="block text-sm font-medium">
            description
            <textarea
              aria-label="description"
              value={draft.description ?? ""}
              onChange={(event) =>
                setDraft({ ...draft, description: event.target.value })
              }
              className="mt-1 w-full rounded-lg border p-2"
            />
          </label>
          <label className="block text-sm font-medium">
            managingDepartment
            <input
              aria-label="managingDepartment"
              value={draft.managingDepartment ?? ""}
              onChange={(event) =>
                setDraft({ ...draft, managingDepartment: event.target.value })
              }
              className="mt-1 w-full rounded-lg border p-2"
            />
          </label>
          <label className="flex items-center gap-2 text-sm font-medium">
            <input
              type="checkbox"
              checked={draft.isUsed ?? true}
              onChange={(event) =>
                setDraft({ ...draft, isUsed: event.target.checked })
              }
            />{" "}
            isUsed
          </label>
          <div className="flex gap-2">
            <button
              type="submit"
              disabled={loading}
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
            {draft.groupId ? (
              <a
                className="rounded-lg border px-4 py-2 text-blue-700"
                href={`/admin/code-groups/${encodeURIComponent(draft.groupId)}/codes`}
              >
                상세코드 이동
              </a>
            ) : null}
          </div>
        </form>
      </div>
    </section>
  );
}
