import type { ReactNode } from "react";
import { firstLeafUrl, NavigationMenu } from "../auth/session";
import { routeTitle } from "../routes/adminRoutes";

interface AdminLayoutProps {
  menus: NavigationMenu[];
  pathname: string;
  userName: string;
  message?: string;
  navigate: (path: string) => void;
  onLogout: () => void;
  children: ReactNode;
}

export function AdminLayout({
  menus,
  pathname,
  userName,
  message,
  navigate,
  onLogout,
  children,
}: AdminLayoutProps) {
  const leafCount = countLeafMenus(menus);
  return (
    <div className="admin-shell">
      <aside className="admin-sidebar" aria-label="관리 메뉴">
        <button
          type="button"
          className="brand-button"
          onClick={() => navigate(firstLeafUrl(menus) ?? "/login")}
          aria-label="첫 관리 메뉴로 이동"
        >
          KN
        </button>
        <nav>
          {menus.map((menu) => (
            <MenuBranch
              key={menu.menuId}
              menu={menu}
              pathname={pathname}
              navigate={navigate}
            />
          ))}
        </nav>
      </aside>
      <div className="admin-main">
        <header className="admin-header">
          <div>
            <span className="eyebrow">Dashboard / Admin</span>
            <h1>{routeTitle(pathname)}</h1>
          </div>
          <div className="profile-area">
            <span>{userName}</span>
            <span className="role-badge">R09 시스템관리자</span>
            <button type="button" className="secondary" onClick={onLogout}>
              로그아웃
            </button>
          </div>
        </header>
        <div className="shell-status success" role="status">
          {message ??
            `DB 내비게이션 기준 ${leafCount}개 관리 메뉴를 표시합니다.`}
        </div>
        {children}
      </div>
    </div>
  );
}

function MenuBranch({
  menu,
  pathname,
  navigate,
}: {
  menu: NavigationMenu;
  pathname: string;
  navigate: (path: string) => void;
}) {
  if (menu.menuLevel === "SUB" && menu.url) {
    const active = pathname === menu.url;
    return (
      <a
        href={menu.url}
        data-testid="admin-menu-leaf"
        className={active ? "menu-leaf active" : "menu-leaf"}
        onClick={(event) => {
          event.preventDefault();
          navigate(menu.url ?? "/");
        }}
      >
        <span className="menu-icon" aria-hidden="true">
          {iconLabel(menu.icon)}
        </span>
        <span>{menu.menuName}</span>
      </a>
    );
  }
  return (
    <section className={`menu-branch level-${menu.menuLevel.toLowerCase()}`}>
      <div className="menu-title">
        <span className="menu-icon" aria-hidden="true">
          {iconLabel(menu.icon)}
        </span>
        <span>{menu.menuName}</span>
      </div>
      <div className="menu-children">
        {(menu.children ?? []).map((child) => (
          <MenuBranch
            key={child.menuId}
            menu={child}
            pathname={pathname}
            navigate={navigate}
          />
        ))}
      </div>
    </section>
  );
}

function iconLabel(icon?: string | null): string {
  const labels: Record<string, string> = {
    settings: "⚙",
    users: "◉",
    shield: "◆",
    menu: "▦",
    code: "⌘",
    user: "👤",
    building: "▥",
    badge: "◇",
    "id-card": "▣",
    lock: "🔒",
    tree: "┬",
    layout: "▤",
    "folder-code": "▰",
    "list-code": "☷",
  };
  return labels[icon ?? ""] ?? "•";
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
