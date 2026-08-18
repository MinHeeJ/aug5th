import { FormEvent, useEffect, useMemo, useState } from "react";
import {
  listUsers,
  updateUserSystemAccess,
  UserSearchFilters,
  UserSummary,
} from "../api/admin";

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

function formatNullable(value: string | null | undefined): string {
  return value && value.length > 0 ? value : "-";
}

export default function UserManagementPage() {
  const [filters, setFilters] = useState<UserSearchFilters>({});
  const [users, setUsers] = useState<UserSummary[]>([]);
  const [selectedUserId, setSelectedUserId] = useState<string>("");
  const [enabled, setEnabled] = useState(true);
  const [selectedRoles, setSelectedRoles] = useState<string[]>([]);
  const [changeReason, setChangeReason] = useState("");
  const [status, setStatus] = useState<
    "loading" | "empty" | "error" | "permission" | "success"
  >("loading");
  const [message, setMessage] = useState("");

  const selectedUser = useMemo(
    () => users.find((user) => user.userId === selectedUserId),
    [selectedUserId, users],
  );

  async function loadUsers(nextFilters = filters) {
    setStatus("loading");
    setMessage("사용자 목록을 조회하는 중입니다.");
    try {
      const data = await listUsers(nextFilters);
      setUsers(data);
      setStatus(data.length === 0 ? "empty" : "success");
      setMessage(
        data.length === 0
          ? "조건에 맞는 사용자가 없습니다"
          : "사용자 목록을 조회했습니다.",
      );
      if (
        data.length > 0 &&
        !data.some((user) => user.userId === selectedUserId)
      ) {
        selectUser(data[0]);
      }
    } catch (error) {
      const apiError = error as {
        status?: number;
        payload?: { error?: { message?: string } };
      };
      setStatus(
        apiError.status === 401 || apiError.status === 403
          ? "permission"
          : "error",
      );
      setMessage(
        apiError.payload?.error?.message ?? "사용자 목록을 조회할 수 없습니다.",
      );
    }
  }

  function selectUser(user: UserSummary) {
    setSelectedUserId(user.userId);
    setEnabled(user.isSystemEnabled);
    setSelectedRoles(user.roles);
    setChangeReason("");
  }

  useEffect(() => {
    void loadUsers({});
  }, []);

  function updateFilter(name: keyof UserSearchFilters, value: string) {
    setFilters((current) => ({ ...current, [name]: value }));
  }

  function submitSearch(event: FormEvent) {
    event.preventDefault();
    void loadUsers(filters);
  }

  function resetFilters() {
    setFilters({});
    setMessage("검색조건을 초기화했습니다.");
  }

  function toggleRole(roleCode: string) {
    setSelectedRoles((current) =>
      current.includes(roleCode)
        ? current.filter((code) => code !== roleCode)
        : [...current, roleCode],
    );
  }

  async function saveAccess() {
    if (!selectedUser) {
      setMessage("사용자를 선택하세요.");
      return;
    }
    try {
      await updateUserSystemAccess(selectedUser.userId, {
        isSystemEnabled: enabled,
        roleCodes: selectedRoles,
        changeReason,
      });
      setMessage("사용자 사용여부와 업무 역할이 저장되었습니다");
      await loadUsers(filters);
    } catch (error) {
      const apiError = error as {
        status?: number;
        payload?: { error?: { message?: string } };
      };
      setStatus(
        apiError.status === 401 || apiError.status === 403
          ? "permission"
          : "error",
      );
      setMessage(
        apiError.payload?.error?.message ??
          "사용자 사용여부와 업무 역할을 저장할 수 없습니다.",
      );
    }
  }

  function cancelEdit() {
    if (selectedUser) {
      selectUser(selectedUser);
      setMessage("마지막 조회값으로 복원했습니다.");
    }
  }

  return (
    <section className="space-y-6" aria-labelledby="user-management-title">
      <header>
        <p className="text-sm text-slate-500">
          시스템 관리 &gt; 사용자·조직 관리
        </p>
        <h1
          id="user-management-title"
          className="text-2xl font-semibold text-slate-900"
        >
          사용자 관리
        </h1>
        <p className="text-sm text-slate-600">
          KORUS 원천정보는 읽기전용으로 확인하고 로컬 시스템 사용여부와 업무
          역할만 저장합니다.
        </p>
      </header>

      <form
        onSubmit={submitSearch}
        className="grid gap-3 rounded-2xl border border-slate-200 bg-white p-4 shadow-sm md:grid-cols-4"
      >
        <input
          aria-label="교번"
          className="rounded-lg border p-2"
          placeholder="교번"
          value={filters.staffId ?? ""}
          onChange={(event) => updateFilter("staffId", event.target.value)}
        />
        <input
          aria-label="성명"
          className="rounded-lg border p-2"
          placeholder="성명"
          value={filters.staffName ?? ""}
          onChange={(event) => updateFilter("staffName", event.target.value)}
        />
        <input
          aria-label="소속"
          className="rounded-lg border p-2"
          placeholder="소속"
          value={filters.organizationCode ?? ""}
          onChange={(event) =>
            updateFilter("organizationCode", event.target.value)
          }
        />
        <input
          aria-label="직급"
          className="rounded-lg border p-2"
          placeholder="직급"
          value={filters.rankTitle ?? ""}
          onChange={(event) => updateFilter("rankTitle", event.target.value)}
        />
        <select
          aria-label="재직상태"
          className="rounded-lg border p-2"
          value={filters.employmentStatus ?? ""}
          onChange={(event) =>
            updateFilter("employmentStatus", event.target.value)
          }
        >
          <option value="">재직상태 전체</option>
          <option value="ACTIVE">재직</option>
          <option value="RETIRED">퇴직</option>
          <option value="LEAVE">휴직</option>
        </select>
        <select
          aria-label="역할"
          className="rounded-lg border p-2"
          value={filters.roleCode ?? ""}
          onChange={(event) => updateFilter("roleCode", event.target.value)}
        >
          <option value="">역할 전체</option>
          {roleCodes.map((roleCode) => (
            <option key={roleCode} value={roleCode}>
              {roleCode}
            </option>
          ))}
        </select>
        <select
          aria-label="사용여부"
          className="rounded-lg border p-2"
          value={filters.systemEnabled ?? ""}
          onChange={(event) =>
            updateFilter("systemEnabled", event.target.value)
          }
        >
          <option value="">사용여부 전체</option>
          <option value="true">사용</option>
          <option value="false">미사용</option>
        </select>
        <div className="flex gap-2">
          <button
            type="submit"
            className="rounded-lg bg-indigo-600 px-4 py-2 text-white"
          >
            조회
          </button>
          <button
            type="button"
            onClick={resetFilters}
            className="rounded-lg border px-4 py-2"
          >
            조건 초기화
          </button>
        </div>
      </form>

      <div
        role="status"
        className="rounded-xl bg-slate-50 p-3 text-sm text-slate-700"
      >
        {message}
      </div>
      {status === "permission" && (
        <div className="rounded-xl border border-rose-200 bg-rose-50 p-4 text-rose-700">
          사용자 관리 접근 권한이 없습니다
        </div>
      )}

      <div className="overflow-x-auto rounded-2xl border border-slate-200 bg-white shadow-sm">
        <table className="min-w-full text-sm">
          <thead className="bg-slate-50 text-left text-slate-600">
            <tr>
              <th className="p-3">교번</th>
              <th>성명</th>
              <th>소속</th>
              <th>직급</th>
              <th>재직상태</th>
              <th>역할</th>
              <th>사용</th>
              <th>보직</th>
              <th>퇴직일자</th>
              <th>최종 동기화일시</th>
            </tr>
          </thead>
          <tbody>
            {status === "loading" && (
              <tr>
                <td className="p-3" colSpan={10}>
                  사용자 목록 loading...
                </td>
              </tr>
            )}
            {status === "empty" && (
              <tr>
                <td className="p-3" colSpan={10}>
                  조건에 맞는 사용자가 없습니다
                </td>
              </tr>
            )}
            {users.map((user) => (
              <tr
                key={user.userId}
                onClick={() => selectUser(user)}
                className={`cursor-pointer border-t ${selectedUserId === user.userId ? "bg-indigo-50" : ""}`}
              >
                <td className="p-3">{user.staffId}</td>
                <td>{user.staffName}</td>
                <td>{user.organizationCode}</td>
                <td>{user.rankTitle}</td>
                <td>{user.employmentStatus}</td>
                <td>{user.roles.join(", ")}</td>
                <td>{user.isSystemEnabled ? "사용" : "미사용"}</td>
                <td>{formatNullable(user.positionTitle)}</td>
                <td>{formatNullable(user.retirementDate)}</td>
                <td>{formatNullable(user.lastSyncedAt)}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      <aside className="rounded-2xl border border-slate-200 bg-white p-4 shadow-sm">
        <h2 className="text-lg font-semibold">선택 사용자 상세 및 변경</h2>
        {!selectedUser && (
          <p className="text-sm text-slate-500">사용자를 선택하세요.</p>
        )}
        {selectedUser && (
          <div className="grid gap-4 md:grid-cols-2">
            <fieldset className="rounded-xl border p-4" disabled>
              <legend className="px-2 font-medium">
                KORUS 원천정보(read-only)
              </legend>
              <p>
                교번: <input readOnly value={selectedUser.staffId} />
              </p>
              <p>
                성명: <input readOnly value={selectedUser.staffName} />
              </p>
              <p>
                소속: <input readOnly value={selectedUser.organizationCode} />
              </p>
              <p>
                직급: <input readOnly value={selectedUser.rankTitle} />
              </p>
              <p>
                재직상태:{" "}
                <input readOnly value={selectedUser.employmentStatus} />
              </p>
              <p>
                보직:{" "}
                <input
                  readOnly
                  value={formatNullable(selectedUser.positionTitle)}
                />
              </p>
              <p>
                퇴직일자:{" "}
                <input
                  readOnly
                  value={formatNullable(selectedUser.retirementDate)}
                />
              </p>
              <p>
                최종 동기화일시:{" "}
                <input
                  readOnly
                  value={formatNullable(selectedUser.lastSyncedAt)}
                />
              </p>
            </fieldset>
            <fieldset className="rounded-xl border p-4">
              <legend className="px-2 font-medium">로컬 관리 항목</legend>
              <label className="flex items-center gap-2">
                <input
                  type="checkbox"
                  checked={enabled}
                  onChange={(event) => setEnabled(event.target.checked)}
                />{" "}
                시스템 사용여부
              </label>
              <div className="mt-3 flex flex-wrap gap-2" aria-label="업무 역할">
                {roleCodes.map((roleCode) => (
                  <label
                    key={roleCode}
                    className="rounded-full border px-3 py-1"
                  >
                    <input
                      type="checkbox"
                      checked={selectedRoles.includes(roleCode)}
                      onChange={() => toggleRole(roleCode)}
                    />{" "}
                    {roleCode}
                  </label>
                ))}
              </div>
              <label className="mt-3 block">
                변경 사유
                <input
                  className="mt-1 w-full rounded-lg border p-2"
                  value={changeReason}
                  onChange={(event) => setChangeReason(event.target.value)}
                />
              </label>
              <div className="mt-4 flex gap-2">
                <button
                  type="button"
                  onClick={saveAccess}
                  className="rounded-lg bg-indigo-600 px-4 py-2 text-white"
                >
                  저장
                </button>
                <button
                  type="button"
                  onClick={cancelEdit}
                  className="rounded-lg border px-4 py-2"
                >
                  취소
                </button>
              </div>
            </fieldset>
          </div>
        )}
      </aside>
    </section>
  );
}
