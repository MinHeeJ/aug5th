import { useEffect, useMemo, useState } from "react";
import { errorMessage, fieldErrors } from "../auth/session";
import {
  getMenuStructure,
  MenuItem,
  reorderMenuStructure,
  saveMenuStructure,
} from "../api/admin";

export default function MenuStructureManagementPage() {
  const [menus, setMenus] = useState<MenuItem[]>([]);
  const [selectedId, setSelectedId] = useState("");
  const [form, setForm] = useState({
    parentMenuId: "",
    displayOrder: 1,
    changeReason: "메뉴 구조 변경",
  });
  const [status, setStatus] = useState<
    "loading" | "success" | "empty" | "error" | "permission"
  >("loading");
  const [message, setMessage] = useState("메뉴 구조를 불러오는 중입니다.");
  const [errors, setErrors] = useState<Record<string, string>>({});

  useEffect(() => {
    void loadMenus();
  }, []);

  const selected = useMemo(
    () => menus.find((menu) => menu.menuId === selectedId) ?? null,
    [menus, selectedId],
  );
  const sameParent = useMemo(
    () =>
      menus
        .filter((menu) => (menu.parentMenuId ?? "") === form.parentMenuId)
        .sort((a, b) => a.displayOrder - b.displayOrder),
    [menus, form.parentMenuId],
  );

  async function loadMenus() {
    setStatus("loading");
    setErrors({});
    try {
      const data = await getMenuStructure();
      setMenus(data);
      setStatus(data.length > 0 ? "success" : "empty");
      setMessage(
        data.length > 0
          ? `${data.length}개 메뉴 구조를 불러왔습니다.`
          : "등록된 메뉴가 없습니다.",
      );
      if (!selectedId && data[0]) {
        selectMenu(data[0]);
      }
    } catch (error) {
      setStatus("error");
      setMessage(errorMessage(error));
      setErrors(fieldErrors(error));
    }
  }

  function selectMenu(menu: MenuItem) {
    setSelectedId(menu.menuId);
    setForm({
      parentMenuId: menu.parentMenuId ?? "",
      displayOrder: menu.displayOrder,
      changeReason: "메뉴 구조 변경",
    });
  }

  async function saveStructure() {
    if (!selected) {
      setMessage("구조를 변경할 메뉴를 선택하세요.");
      return;
    }
    setStatus("loading");
    setErrors({});
    try {
      const saved = await saveMenuStructure(selected.menuId, {
        menuId: selected.menuId,
        parentMenuId: form.parentMenuId || null,
        menuLevel: selected.menuLevel,
        displayOrder: Number(form.displayOrder),
        menuName: selected.menuName,
        screenId: selected.screenId,
        url: selected.url,
        icon: selected.icon,
        businessDivision: selected.businessDivision,
        description: selected.description,
        isUsed: selected.isUsed,
        changeReason: form.changeReason,
      });
      setMenus((current) =>
        current.map((menu) => (menu.menuId === saved.menuId ? saved : menu)),
      );
      setStatus("success");
      setMessage("메뉴 구조를 저장했습니다.");
    } catch (error) {
      setStatus("error");
      setMessage(errorMessage(error));
      setErrors(fieldErrors(error));
    }
  }

  async function moveSelected(direction: -1 | 1) {
    if (!selected) {
      return;
    }
    const index = sameParent.findIndex(
      (menu) => menu.menuId === selected.menuId,
    );
    const nextIndex = index + direction;
    if (index < 0 || nextIndex < 0 || nextIndex >= sameParent.length) {
      return;
    }
    const ordered = [...sameParent];
    [ordered[index], ordered[nextIndex]] = [ordered[nextIndex], ordered[index]];
    setStatus("loading");
    try {
      const data = await reorderMenuStructure(
        selected.parentMenuId,
        ordered.map((menu) => menu.menuId),
      );
      setMenus(data);
      setStatus("success");
      setMessage("동일 계층 표시순서를 재정렬했습니다.");
    } catch (error) {
      setStatus("error");
      setMessage(errorMessage(error));
      setErrors(fieldErrors(error));
    }
  }

  return (
    <section
      className="content-card management-page"
      aria-labelledby="menu-structure-title"
    >
      <p className="breadcrumb">
        시스템 관리 &gt; 메뉴 관리 &gt; 메뉴 구조 관리
      </p>
      <div className="page-heading">
        <div>
          <h1 id="menu-structure-title">메뉴 구조 관리</h1>
          <p className="muted">대·중·소메뉴의 부모와 표시순서를 관리합니다.</p>
        </div>
        <button
          type="button"
          onClick={saveStructure}
          disabled={!selected || status === "loading"}
        >
          구조 저장
        </button>
      </div>
      <div className={`status ${status}`}>{message}</div>
      {Object.entries(errors).map(([field, text]) => (
        <p key={field} className="field-error">
          {field}: {text}
        </p>
      ))}
      <div className="split-grid">
        <div className="table-wrap">
          <table>
            <thead>
              <tr>
                <th>순서</th>
                <th>레벨</th>
                <th>메뉴명</th>
                <th>부모</th>
              </tr>
            </thead>
            <tbody>
              {menus.map((menu) => (
                <tr
                  key={menu.menuId}
                  className={menu.menuId === selectedId ? "selected-row" : ""}
                  onClick={() => selectMenu(menu)}
                >
                  <td>{menu.displayOrder}</td>
                  <td>{menu.menuLevel}</td>
                  <td>{menu.menuName}</td>
                  <td>{menu.parentMenuId ?? "-"}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
        <form
          className="compact-form"
          onSubmit={(event) => {
            event.preventDefault();
            void saveStructure();
          }}
        >
          <label>
            메뉴 ID
            <input value={selected?.menuId ?? ""} readOnly />
          </label>
          <label>
            메뉴명
            <input value={selected?.menuName ?? ""} readOnly />
          </label>
          <label>
            부모 메뉴 ID
            <input
              value={form.parentMenuId}
              onChange={(event) =>
                setForm((current) => ({
                  ...current,
                  parentMenuId: event.target.value,
                }))
              }
              placeholder="최상위는 빈 값"
            />
          </label>
          <label>
            표시순서
            <input
              type="number"
              min="1"
              value={form.displayOrder}
              onChange={(event) =>
                setForm((current) => ({
                  ...current,
                  displayOrder: Number(event.target.value),
                }))
              }
            />
          </label>
          <label>
            변경사유
            <input
              value={form.changeReason}
              onChange={(event) =>
                setForm((current) => ({
                  ...current,
                  changeReason: event.target.value,
                }))
              }
            />
          </label>
          <div className="button-row">
            <button
              type="button"
              className="secondary"
              onClick={() => moveSelected(-1)}
            >
              위로
            </button>
            <button
              type="button"
              className="secondary"
              onClick={() => moveSelected(1)}
            >
              아래로
            </button>
            <button type="submit">저장</button>
          </div>
        </form>
      </div>
    </section>
  );
}
