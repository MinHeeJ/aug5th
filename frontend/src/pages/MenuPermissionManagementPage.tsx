import { useEffect, useMemo, useState } from "react";
import { errorMessage, fieldErrors } from "../auth/session";
import {
  listMenuPermissions,
  MenuPermission,
  saveMenuPermissions,
} from "../api/admin";

const targetTypes = [
  { value: "ROLE", label: "역할" },
  { value: "ORGANIZATION", label: "조직" },
  { value: "USER", label: "사용자" },
];

export default function MenuPermissionManagementPage() {
  const [targetType, setTargetType] = useState("ROLE");
  const [targetId, setTargetId] = useState("R09");
  const [filter, setFilter] = useState("");
  const [permissions, setPermissions] = useState<MenuPermission[]>([]);
  const [dirty, setDirty] = useState<Record<string, boolean>>({});
  const [status, setStatus] = useState<
    "loading" | "success" | "empty" | "error" | "permission"
  >("loading");
  const [message, setMessage] = useState("메뉴 권한을 불러오는 중입니다.");
  const [errors, setErrors] = useState<Record<string, string>>({});

  useEffect(() => {
    void loadPermissions();
  }, []);

  const changedRows = useMemo(
    () =>
      permissions.filter(
        (permission) => dirty[permission.menuId] !== undefined,
      ),
    [permissions, dirty],
  );

  async function loadPermissions(nextFilter = filter) {
    setStatus("loading");
    setMessage("대상별 메뉴 접근권한 matrix를 조회합니다.");
    setErrors({});
    try {
      const data = await listMenuPermissions({
        targetType,
        targetId,
        filter: nextFilter,
      });
      setPermissions(data);
      setDirty({});
      setStatus(data.length > 0 ? "success" : "empty");
      setMessage(
        data.length > 0
          ? `${data.length}개 메뉴 권한을 불러왔습니다.`
          : "조건에 맞는 메뉴 권한이 없습니다.",
      );
    } catch (error) {
      setStatus("error");
      setMessage(errorMessage(error));
      setErrors(fieldErrors(error));
    }
  }

  function updateAllowed(menuId: string, isAllowed: boolean) {
    setPermissions((current) =>
      current.map((permission) =>
        permission.menuId === menuId
          ? { ...permission, isAllowed }
          : permission,
      ),
    );
    setDirty((current) => ({ ...current, [menuId]: isAllowed }));
  }

  async function saveChanges() {
    if (changedRows.length === 0) {
      setMessage("저장할 변경사항이 없습니다.");
      return;
    }
    setStatus("loading");
    setErrors({});
    try {
      const data = await saveMenuPermissions(
        targetType,
        targetId,
        changedRows,
        "화면에서 메뉴 권한 변경",
      );
      setPermissions(data);
      setDirty({});
      setStatus("success");
      setMessage("권한 저장 후 matrix를 재조회했습니다.");
    } catch (error) {
      setStatus("error");
      setMessage(errorMessage(error));
      setErrors(fieldErrors(error));
    }
  }

  return (
    <section
      className="content-card management-page"
      aria-labelledby="menu-permission-title"
    >
      <p className="breadcrumb">
        시스템 관리 &gt; 역할·권한 관리 &gt; 메뉴 권한 관리
      </p>
      <div className="page-heading">
        <div>
          <h1 id="menu-permission-title">메뉴 권한 관리</h1>
          <p className="muted">
            ROLE, ORGANIZATION, USER 대상별 메뉴 접근 허용 여부를 저장하고 즉시
            재조회합니다.
          </p>
        </div>
        <button
          type="button"
          onClick={saveChanges}
          disabled={changedRows.length === 0 || status === "loading"}
        >
          권한 저장
        </button>
      </div>
      <div className={`status ${status}`}>{message}</div>
      {Object.entries(errors).map(([field, text]) => (
        <p key={field} className="field-error">
          {field}: {text}
        </p>
      ))}
      <div className="filter-grid compact-form">
        <label>
          대상 구분
          <select
            value={targetType}
            onChange={(event) => setTargetType(event.target.value)}
          >
            {targetTypes.map((item) => (
              <option key={item.value} value={item.value}>
                {item.label}
              </option>
            ))}
          </select>
        </label>
        <label>
          대상 식별자
          <input
            value={targetId}
            onChange={(event) => setTargetId(event.target.value)}
            placeholder="예: R09"
          />
        </label>
        <label>
          메뉴 검색
          <input
            value={filter}
            onChange={(event) => setFilter(event.target.value)}
            placeholder="메뉴명, 화면ID, URL"
          />
        </label>
        <button
          type="button"
          className="secondary"
          onClick={() => loadPermissions(filter)}
        >
          조회
        </button>
      </div>
      {permissions.length === 0 ? (
        <div className="empty-panel">조건에 맞는 메뉴 권한이 없습니다.</div>
      ) : null}
      <div className="table-wrap">
        <table>
          <thead>
            <tr>
              <th>허용</th>
              <th>레벨</th>
              <th>메뉴명</th>
              <th>화면ID</th>
              <th>URL</th>
              <th>상태</th>
            </tr>
          </thead>
          <tbody>
            {permissions.map((permission) => (
              <tr key={permission.menuId}>
                <td>
                  <input
                    type="checkbox"
                    aria-label={`${permission.menuName} 허용`}
                    checked={permission.isAllowed}
                    onChange={(event) =>
                      updateAllowed(permission.menuId, event.target.checked)
                    }
                  />
                </td>
                <td>{permission.menuLevel}</td>
                <td>{permission.menuName}</td>
                <td>{permission.screenId ?? "-"}</td>
                <td>{permission.url ?? "-"}</td>
                <td>{permission.isMenuUsed ? "사용" : "미사용"}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </section>
  );
}
