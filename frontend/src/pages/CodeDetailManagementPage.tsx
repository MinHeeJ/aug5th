import { FormEvent, useEffect, useMemo, useState } from "react";
import { ApiClientError, apiFetch } from "../api/client";

type ApiEnvelope<T> = {
  success: boolean;
  data: T;
  message?: string;
  error?: { message?: string; fields?: Record<string, string> };
};

type Code = {
  codeId: string;
  groupId: string;
  codeValue: string;
  codeName: string;
  parentCodeId: string | null;
  sortOrder: number;
  extraAttributes: Record<string, unknown> | null;
  validFrom: string | null;
  validTo: string | null;
  isUsed: boolean;
};

type DraftCode = Omit<Code, "extraAttributes" | "sortOrder"> & {
  sortOrder: string;
  extraAttributes: string;
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

function groupIdFromPath(): string {
  return decodeURIComponent(
    window.location.pathname.match(
      /^\/admin\/code-groups\/([^/]+)\/codes$/,
    )?.[1] ?? "",
  );
}

function newDraft(groupId: string): DraftCode {
  return {
    codeId: "",
    groupId,
    codeValue: "",
    codeName: "",
    parentCodeId: "",
    sortOrder: "0",
    extraAttributes: "{}",
    validFrom: "",
    validTo: "",
    isUsed: true,
  };
}

function toDraft(code: Code): DraftCode {
  return {
    ...code,
    parentCodeId: code.parentCodeId ?? "",
    sortOrder: String(code.sortOrder ?? 0),
    extraAttributes: JSON.stringify(code.extraAttributes ?? {}, null, 2),
    validFrom: code.validFrom ?? "",
    validTo: code.validTo ?? "",
  };
}

export default function CodeDetailManagementPage() {
  const [groupId] = useState(groupIdFromPath());
  const [codes, setCodes] = useState<Code[]>([]);
  const [selectedCodeValue, setSelectedCodeValue] = useState("");
  const [draft, setDraft] = useState<DraftCode>(newDraft(groupId));
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");
  const [fields, setFields] = useState<Record<string, string>>({});
  const [permissionDenied, setPermissionDenied] = useState(false);

  const selected = useMemo(
    () => codes.find((code) => code.codeValue === selectedCodeValue) ?? null,
    [codes, selectedCodeValue],
  );

  async function loadCodes(nextSelectedCodeValue = selectedCodeValue) {
    setLoading(true);
    setError("");
    setPermissionDenied(false);
    try {
      const response = await apiFetch<ApiEnvelope<Code[]>>(
        `/api/admin/code-groups/${encodeURIComponent(groupId)}/codes`,
      );
      const nextCodes = response.data ?? [];
      setCodes(nextCodes);
      const nextSelected =
        nextCodes.find((code) => code.codeValue === nextSelectedCodeValue) ??
        nextCodes[0] ??
        null;
      setSelectedCodeValue(nextSelected?.codeValue ?? "");
      setDraft(nextSelected ? toDraft(nextSelected) : newDraft(groupId));
      if (nextCodes.length === 0) {
        setMessage("상세코드가 없습니다. 신규 상세코드를 입력해 저장하세요.");
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
    void loadCodes("");
  }, []);

  function selectCode(code: Code) {
    setSelectedCodeValue(code.codeValue);
    setDraft(toDraft(code));
    setFields({});
    setMessage("선택한 상세코드를 편집합니다.");
    setError("");
  }

  function newCode() {
    setSelectedCodeValue("");
    setDraft(newDraft(groupId));
    setFields({});
    setMessage("신규 상세코드를 입력하세요.");
    setError("");
  }

  function resetDraft() {
    setDraft(selected ? toDraft(selected) : newDraft(groupId));
    setFields({});
    setMessage(
      selected
        ? "마지막 조회 결과로 복원했습니다."
        : "신규 입력을 초기화했습니다.",
    );
  }

  async function saveCode(event: FormEvent) {
    event.preventDefault();
    const targetCodeValue = draft.codeValue.trim();
    if (!targetCodeValue) {
      setFields({ codeValue: "코드값은 필수입니다." });
      setError("입력값을 확인해 주세요.");
      return;
    }
    let parsedAttributes: Record<string, unknown>;
    try {
      parsedAttributes = draft.extraAttributes.trim()
        ? (JSON.parse(draft.extraAttributes) as Record<string, unknown>)
        : {};
    } catch {
      setFields({ extraAttributes: "추가속성은 JSON 객체 형식이어야 합니다." });
      setError("입력값을 확인해 주세요.");
      return;
    }
    setLoading(true);
    setError("");
    setFields({});
    try {
      await apiFetch<ApiEnvelope<Code>>(
        `/api/admin/code-groups/${encodeURIComponent(groupId)}/codes/${encodeURIComponent(targetCodeValue)}`,
        {
          method: "PUT",
          body: {
            groupId,
            codeValue: targetCodeValue,
            codeName: draft.codeName,
            parentCodeId: draft.parentCodeId || null,
            sortOrder: Number(draft.sortOrder),
            extraAttributes: parsedAttributes,
            validFrom: draft.validFrom || null,
            validTo: draft.validTo || null,
            isUsed: draft.isUsed,
          },
        },
      );
      setMessage("상세코드가 저장되었습니다");
      await loadCodes(targetCodeValue);
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
          시스템 관리 &gt; 공통코드 관리 &gt; 상세코드 관리
        </p>
        <h1 className="text-2xl font-bold">상세코드 관리</h1>
        <p className="text-slate-600">
          선택 코드그룹 <strong className="font-mono">{groupId}</strong>의
          코드값, 코드명, 상위코드, 정렬순서, 추가속성, 사용여부, 유효기간을
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
        <div className="flex flex-wrap gap-2">
          <button
            type="button"
            disabled={loading}
            onClick={() => void loadCodes(selectedCodeValue)}
            className="rounded-lg bg-slate-900 px-4 py-2 text-white disabled:opacity-50"
          >
            상세코드 조회
          </button>
          <button
            type="button"
            onClick={newCode}
            className="rounded-lg border px-4 py-2"
          >
            신규
          </button>
          <a
            href="/admin/code-groups"
            className="rounded-lg border px-4 py-2 text-blue-700"
          >
            코드그룹 목록
          </a>
        </div>
        {loading && (
          <p className="mt-3 text-slate-500">상세코드를 불러오는 중...</p>
        )}
        {!loading && codes.length === 0 && (
          <p className="mt-3 text-slate-500">상세코드가 없습니다.</p>
        )}
      </div>

      <div className="grid gap-6 xl:grid-cols-[1.15fr_0.85fr]">
        <div className="overflow-hidden rounded-2xl bg-white shadow-sm">
          <table className="min-w-full text-sm">
            <thead className="bg-slate-100 text-left text-slate-600">
              <tr>
                <th className="p-3">codeValue</th>
                <th className="p-3">codeName</th>
                <th className="p-3">parent</th>
                <th className="p-3">sort</th>
                <th className="p-3">mapping</th>
                <th className="p-3">isUsed</th>
              </tr>
            </thead>
            <tbody>
              {codes.map((code) => {
                const parent = codes.find(
                  (item) => item.codeId === code.parentCodeId,
                );
                return (
                  <tr
                    key={code.codeId}
                    onClick={() => selectCode(code)}
                    className={`cursor-pointer border-t ${code.codeValue === selectedCodeValue ? "bg-blue-50" : "bg-white"}`}
                  >
                    <td className="p-3 font-mono">{code.codeValue}</td>
                    <td className="p-3">{code.codeName}</td>
                    <td className="p-3 font-mono">
                      {parent?.codeValue ?? "-"}
                    </td>
                    <td className="p-3">{code.sortOrder}</td>
                    <td className="p-3">
                      <code>{JSON.stringify(code.extraAttributes ?? {})}</code>
                    </td>
                    <td className="p-3">{String(code.isUsed)}</td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>

        <form
          onSubmit={saveCode}
          className="space-y-4 rounded-2xl bg-white p-5 shadow-sm"
        >
          <h2 className="text-lg font-semibold">상세코드 상세/편집</h2>
          <label className="block text-sm font-medium">
            codeValue
            <input
              aria-label="codeValue"
              value={draft.codeValue}
              disabled={Boolean(selectedCodeValue)}
              onChange={(event) =>
                setDraft({ ...draft, codeValue: event.target.value })
              }
              className="mt-1 w-full rounded-lg border p-2 font-mono disabled:bg-slate-100"
            />
          </label>
          {fields.codeValue && (
            <p className="text-sm text-rose-700">{fields.codeValue}</p>
          )}
          <label className="block text-sm font-medium">
            codeName
            <input
              aria-label="codeName"
              value={draft.codeName}
              onChange={(event) =>
                setDraft({ ...draft, codeName: event.target.value })
              }
              className="mt-1 w-full rounded-lg border p-2"
            />
          </label>
          {fields.codeName && (
            <p className="text-sm text-rose-700">{fields.codeName}</p>
          )}
          <label className="block text-sm font-medium">
            parentCodeId
            <select
              aria-label="parentCodeId"
              value={draft.parentCodeId ?? ""}
              onChange={(event) =>
                setDraft({ ...draft, parentCodeId: event.target.value })
              }
              className="mt-1 w-full rounded-lg border p-2"
            >
              <option value="">상위코드 없음</option>
              {codes
                .filter((code) => code.codeValue !== draft.codeValue)
                .map((code) => (
                  <option key={code.codeId} value={code.codeId}>
                    {code.codeValue} - {code.codeName}
                  </option>
                ))}
            </select>
          </label>
          {fields.parentCodeId && (
            <p className="text-sm text-rose-700">{fields.parentCodeId}</p>
          )}
          <label className="block text-sm font-medium">
            sortOrder
            <input
              aria-label="sortOrder"
              type="number"
              value={draft.sortOrder}
              onChange={(event) =>
                setDraft({ ...draft, sortOrder: event.target.value })
              }
              className="mt-1 w-full rounded-lg border p-2"
            />
          </label>
          {fields.sortOrder && (
            <p className="text-sm text-rose-700">{fields.sortOrder}</p>
          )}
          <label className="block text-sm font-medium">
            extraAttributes
            <textarea
              aria-label="extraAttributes"
              value={draft.extraAttributes}
              onChange={(event) =>
                setDraft({ ...draft, extraAttributes: event.target.value })
              }
              className="mt-1 h-28 w-full rounded-lg border p-2 font-mono"
            />
          </label>
          {fields.extraAttributes && (
            <p className="text-sm text-rose-700">{fields.extraAttributes}</p>
          )}
          <div className="grid gap-3 sm:grid-cols-2">
            <label className="block text-sm font-medium">
              validFrom
              <input
                aria-label="validFrom"
                type="date"
                value={draft.validFrom ?? ""}
                onChange={(event) =>
                  setDraft({ ...draft, validFrom: event.target.value })
                }
                className="mt-1 w-full rounded-lg border p-2"
              />
            </label>
            <label className="block text-sm font-medium">
              validTo
              <input
                aria-label="validTo"
                type="date"
                value={draft.validTo ?? ""}
                onChange={(event) =>
                  setDraft({ ...draft, validTo: event.target.value })
                }
                className="mt-1 w-full rounded-lg border p-2"
              />
            </label>
          </div>
          {fields.validTo && (
            <p className="text-sm text-rose-700">{fields.validTo}</p>
          )}
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
          </div>
        </form>
      </div>
    </section>
  );
}
