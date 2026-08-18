import { useEffect, useState } from "react";
import { errorMessage, fieldErrors } from "../auth/session";
import { listMenus, MenuItem, saveMenu, updateMenuStatus } from "../api/admin";

type MenuForm = {
  menuName: string;
  screenId: string;
  url: string;
  icon: string;
  businessDivision: string;
  description: string;
  changeReason: string;
};

export default function MenuInformationManagementPage() {
  const [menus, setMenus] = useState<MenuItem[]>([]);
  const [selected, setSelected] = useState<MenuItem | null>(null);
  const [form, setForm] = useState<MenuForm>({
    menuName: "",
    screenId: "",
    url: "",
    icon: "",
    businessDivision: "",
    description: "",
    changeReason: "메뉴 정보 변경",
  });
  const [status, setStatus] = useState<
    "loading" | "success" | "empty" | "error" | "permission"
  >("loading");
  const [message, setMessage] = useState("메뉴 정보를 불러오는 중입니다.");
  const [errors, setErrors] = useState<Record<string, string>>({});

  useEffect(() => {
    void loadMenus();
  }, []);

  async function loadMenus() {
    setStatus("loading");
    setErrors({});
    try {
      const data = await listMenus();
      setMenus(data);
      setStatus(data.length > 0 ? "success" : "empty");
      setMessage(
        data.length > 0
          ? `${data.length}개 메뉴 정보를 불러왔습니다.`
          : "등록된 메뉴가 없습니다.",
      );
      if (!selected && data[0]) {
        selectMenu(data[0]);
      }
    } catch (error) {
      setStatus("error");
      setMessage(errorMessage(error));
      setErrors(fieldErrors(error));
    }
  }

  function selectMenu(menu: MenuItem) {
    setSelected(menu);
    setForm({
      menuName: menu.menuName,
      screenId: menu.screenId ?? "",
      url: menu.url ?? "",
      icon: menu.icon ?? "",
      businessDivision: menu.businessDivision ?? "",
      description: menu.description ?? "",
      changeReason: "메뉴 정보 변경",
    });
  }

  async function saveInformation() {
    if (!selected) {
      setMessage("정보를 수정할 메뉴를 선택하세요.");
      return;
    }
    setStatus("loading");
    setErrors({});
    try {
      const saved = await saveMenu(selected.menuId, {
        menuId: selected.menuId,
        parentMenuId: selected.parentMenuId,
        menuLevel: selected.menuLevel,
        displayOrder: selected.displayOrder,
        menuName: form.menuName,
        screenId: form.screenId || null,
        url: form.url || null,
        icon: form.icon || null,
        businessDivision: form.businessDivision || null,
        description: form.description || null,
        isUsed: selected.isUsed,
        changeReason: form.changeReason,
      });
      setMenus((current) =>
        current.map((menu) => (menu.menuId === saved.menuId ? saved : menu)),
      );
      selectMenu(saved);
      setStatus("success");
      setMessage("메뉴 정보 저장 후 상세를 갱신했습니다.");
    } catch (error) {
      setStatus("error");
      setMessage(errorMessage(error));
      setErrors(fieldErrors(error));
    }
  }

  async function toggleStatus() {
    if (!selected) {
      return;
    }
    if (
      !window.confirm(
        `${selected.menuName} 메뉴 상태를 ${selected.isUsed ? "미사용" : "사용"}으로 변경할까요?`,
      )
    ) {
      return;
    }
    setStatus("loading");
    try {
      const saved = await updateMenuStatus(
        selected.menuId,
        !selected.isUsed,
        "메뉴 사용여부 변경",
      );
      setMenus((current) =>
        current.map((menu) => (menu.menuId === saved.menuId ? saved : menu)),
      );
      selectMenu(saved);
      setStatus("success");
      setMessage("메뉴 사용여부를 변경했습니다.");
    } catch (error) {
      setStatus("error");
      setMessage(errorMessage(error));
      setErrors(fieldErrors(error));
    }
  }

  return (
    <section
      className="content-card management-page"
      aria-labelledby="menu-info-title"
    >
      <p className="breadcrumb">
        시스템 관리 &gt; 메뉴 관리 &gt; 메뉴 정보 관리
      </p>
      <div className="page-heading">
        <div>
          <h1 id="menu-info-title">메뉴 정보 관리</h1>
          <p className="muted">
            메뉴명, 화면ID, URL, icon, 업무구분, 설명을 관리합니다.
          </p>
        </div>
        <div className="button-row">
          <button
            type="button"
            onClick={saveInformation}
            disabled={!selected || status === "loading"}
          >
            정보 저장
          </button>
          <button
            type="button"
            className="secondary"
            onClick={toggleStatus}
            disabled={!selected}
          >
            {selected?.isUsed ? "미사용 처리" : "사용 처리"}
          </button>
        </div>
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
                <th>메뉴명</th>
                <th>화면ID</th>
                <th>URL</th>
                <th>상태</th>
              </tr>
            </thead>
            <tbody>
              {menus.map((menu) => (
                <tr
                  key={menu.menuId}
                  className={
                    menu.menuId === selected?.menuId ? "selected-row" : ""
                  }
                  onClick={() => selectMenu(menu)}
                >
                  <td>{menu.menuName}</td>
                  <td>{menu.screenId ?? "-"}</td>
                  <td>{menu.url ?? "-"}</td>
                  <td>{menu.isUsed ? "사용" : "미사용"}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
        <form
          className="compact-form"
          onSubmit={(event) => {
            event.preventDefault();
            void saveInformation();
          }}
        >
          <label>
            메뉴 ID
            <input value={selected?.menuId ?? ""} readOnly />
          </label>
          <label>
            메뉴 레벨
            <input value={selected?.menuLevel ?? ""} readOnly />
          </label>
          <label>
            메뉴명
            <input
              value={form.menuName}
              onChange={(event) =>
                setForm((current) => ({
                  ...current,
                  menuName: event.target.value,
                }))
              }
            />
          </label>
          <label>
            화면ID
            <input
              value={form.screenId}
              onChange={(event) =>
                setForm((current) => ({
                  ...current,
                  screenId: event.target.value,
                }))
              }
            />
          </label>
          <label>
            URL
            <input
              value={form.url}
              onChange={(event) =>
                setForm((current) => ({ ...current, url: event.target.value }))
              }
            />
          </label>
          <label>
            icon
            <input
              value={form.icon}
              onChange={(event) =>
                setForm((current) => ({ ...current, icon: event.target.value }))
              }
            />
          </label>
          <label>
            업무구분
            <input
              value={form.businessDivision}
              onChange={(event) =>
                setForm((current) => ({
                  ...current,
                  businessDivision: event.target.value,
                }))
              }
            />
          </label>
          <label>
            설명
            <input
              value={form.description}
              onChange={(event) =>
                setForm((current) => ({
                  ...current,
                  description: event.target.value,
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
          <button type="submit">정보 저장</button>
        </form>
      </div>
    </section>
  );
}
