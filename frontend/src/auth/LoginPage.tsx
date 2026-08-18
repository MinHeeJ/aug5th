import { FormEvent, useState } from "react";
import {
  errorMessage,
  fieldErrors,
  firstLeafUrl,
  listNavigationMenus,
  login,
  NavigationMenu,
} from "./session";

interface LoginPageProps {
  onAuthenticated: (menus: NavigationMenu[]) => void;
  navigate: (path: string) => void;
}

export function LoginPage({ onAuthenticated, navigate }: LoginPageProps) {
  const [username, setUsername] = useState("admin");
  const [password, setPassword] = useState("admin");
  const [status, setStatus] = useState<
    "empty" | "loading" | "error" | "permission" | "success"
  >("empty");
  const [message, setMessage] = useState("");
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [previewMenus, setPreviewMenus] = useState<NavigationMenu[]>([]);

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setStatus("loading");
    setMessage("로그인 처리 중...");
    setErrors({});
    try {
      const user = await login(username, password);
      if (!user.roleCodes.includes("R09")) {
        setStatus("permission");
        setMessage("권한 없음: R09 시스템관리자 권한이 필요합니다.");
        return;
      }
      setMessage("메뉴를 불러오는 중...");
      const menus = await listNavigationMenus();
      if (countLeafMenus(menus) === 0) {
        setStatus("permission");
        setMessage("권한 없음: 접근 가능한 관리 메뉴가 없습니다.");
        return;
      }
      setPreviewMenus(menus);
      setStatus("success");
      setMessage("R09 시스템관리자 메뉴를 불러왔습니다.");
      onAuthenticated(menus);
      const nextPath = firstLeafUrl(menus);
      if (nextPath) {
        navigate(nextPath);
      }
    } catch (error) {
      setStatus("error");
      setMessage(errorMessage(error));
      setErrors(fieldErrors(error));
    }
  }

  function resetForm() {
    setUsername("");
    setPassword("");
    setStatus("empty");
    setMessage("");
    setErrors({});
    setPreviewMenus([]);
  }

  return (
    <main className="login-shell">
      <aside className="login-side" aria-label="로그인 사이드바">
        <div className="brand-mark">KN</div>
        <span className="side-icon" aria-hidden="true">
          🔒
        </span>
        <span>로그인</span>
      </aside>
      <section className="login-panel" aria-labelledby="login-title">
        <p className="eyebrow">ex 스타일 미니멀 시스템관리 Shell</p>
        <h1 id="login-title">한국교원대학교 교수업적평가시스템</h1>
        <p className="muted">
          로컬 Docker Compose 직후 시드 관리자 계정으로 접속합니다.
        </p>
        <form className="card login-card" onSubmit={submit} noValidate>
          <label>
            사용자 ID
            <input
              aria-label="사용자 ID"
              value={username}
              onChange={(event) => setUsername(event.target.value)}
              autoComplete="username"
            />
            {errors.username ? (
              <span className="field-error">{errors.username}</span>
            ) : null}
          </label>
          <label>
            비밀번호
            <input
              aria-label="비밀번호"
              type="password"
              value={password}
              onChange={(event) => setPassword(event.target.value)}
              autoComplete="current-password"
            />
            {errors.password ? (
              <span className="field-error">{errors.password}</span>
            ) : null}
          </label>
          <div className="button-row">
            <button type="submit" disabled={status === "loading"}>
              로그인
            </button>
            <button type="button" className="secondary" onClick={resetForm}>
              입력 초기화
            </button>
          </div>
          <StatusMessage status={status} message={message} />
        </form>
        <section
          className="card menu-preview"
          aria-label="로그인 성공 후 표시되는 R09 메뉴 미리보기"
        >
          <h2>R09 메뉴 미리보기</h2>
          {previewMenus.length === 0 ? (
            <p className="muted">로그인 전에는 메뉴 영역을 비워 둡니다.</p>
          ) : (
            <MenuPreview menus={previewMenus} />
          )}
        </section>
      </section>
    </main>
  );
}

function StatusMessage({
  status,
  message,
}: {
  status: string;
  message: string;
}) {
  const fallback =
    status === "empty" ? "사용자 ID와 비밀번호를 입력해 주세요." : message;
  return (
    <div
      className={`status ${status}`}
      role={status === "error" || status === "permission" ? "alert" : "status"}
    >
      {fallback}
    </div>
  );
}

function MenuPreview({ menus }: { menus: NavigationMenu[] }) {
  return (
    <ul>
      {menus.map((menu) => (
        <li key={menu.menuId}>
          {menu.menuName}
          <MenuPreview menus={menu.children ?? []} />
        </li>
      ))}
    </ul>
  );
}

function countLeafMenus(menus: NavigationMenu[]): number {
  return menus.reduce(
    (sum, menu) =>
      sum +
      (menu.menuLevel === "SUB" ? 1 : 0) +
      countLeafMenus(menu.children ?? []),
    0,
  );
}
