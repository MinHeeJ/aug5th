import type { ReactNode } from "react";
import CodeDetailManagementPage from "../pages/CodeDetailManagementPage";
import CodeGroupManagementPage from "../pages/CodeGroupManagementPage";
import MenuInformationManagementPage from "../pages/MenuInformationManagementPage";
import MenuPermissionManagementPage from "../pages/MenuPermissionManagementPage";
import MenuStructureManagementPage from "../pages/MenuStructureManagementPage";
import OrganizationManagementPage from "../pages/OrganizationManagementPage";
import RoleManagementPage from "../pages/RoleManagementPage";
import UserManagementPage from "../pages/UserManagementPage";
import UserRoleManagementPage from "../pages/UserRoleManagementPage";
import { isOutOfScopeRoute, renderOutOfScopeRoute } from "./outOfScope";

export interface AdminRouteDefinition {
  path: string;
  title: string;
  description: string;
  menuPath: string;
  stateSummary: string;
}

export const adminRoutes: AdminRouteDefinition[] = [
  {
    path: "/admin/users",
    title: "사용자 관리",
    description:
      "교번·성명·소속·직급·재직상태·역할·사용여부 조건 조회와 KORUS 읽기전용 상세를 위한 route shell입니다.",
    menuPath: "시스템 관리 > 사용자·조직 관리 > 사용자 관리",
    stateSummary:
      "loading / empty / error / permission / success 상태 영역을 제공합니다.",
  },
  {
    path: "/admin/organizations",
    title: "조직 관리",
    description:
      "조직 목록, 조직 계층, 관계 및 적용기간 편집을 위한 route shell입니다.",
    menuPath: "시스템 관리 > 사용자·조직 관리 > 조직 관리",
    stateSummary: "조직 관리 기능 상세 구현은 후속 phase 범위입니다.",
  },
  {
    path: "/admin/roles",
    title: "역할 관리",
    description:
      "R01~R09 역할 목록과 roleCode 읽기전용 편집 흐름을 위한 route shell입니다.",
    menuPath: "시스템 관리 > 역할·권한 관리 > 역할 관리",
    stateSummary: "역할 기준정보 편집 기능은 후속 phase 범위입니다.",
  },
  {
    path: "/admin/user-roles",
    title: "사용자 역할 관리",
    description:
      "사용자별 현재 역할, 유효기간, 승인자, 역할 구분 표시 route shell입니다.",
    menuPath: "시스템 관리 > 역할·권한 관리 > 사용자 역할 관리",
    stateSummary: "사용자 역할 저장 기능은 후속 phase 범위입니다.",
  },
  {
    path: "/admin/menu-permissions",
    title: "메뉴 권한 관리",
    description:
      "ROLE, ORGANIZATION, USER 대상별 메뉴 접근권한 matrix route shell입니다.",
    menuPath: "시스템 관리 > 역할·권한 관리 > 메뉴 권한 관리",
    stateSummary: "권한 저장 기능은 후속 phase 범위입니다.",
  },
  {
    path: "/admin/menu-structure",
    title: "메뉴 구조 관리",
    description: "대메뉴·중메뉴·소메뉴 계층과 표시순서 관리 route shell입니다.",
    menuPath: "시스템 관리 > 메뉴 관리 > 메뉴 구조 관리",
    stateSummary:
      "navigation 재조회 preview를 통해 DB 정의 순서 계약을 확인합니다.",
  },
  {
    path: "/admin/menus",
    title: "메뉴 정보 관리",
    description:
      "메뉴명, 화면ID, URL, icon, 업무구분, 설명 관리 route shell입니다.",
    menuPath: "시스템 관리 > 메뉴 관리 > 메뉴 정보 관리",
    stateSummary: "메뉴 실행정보 저장은 후속 phase 범위입니다.",
  },
  {
    path: "/admin/code-groups",
    title: "코드그룹 관리",
    description: "코드그룹 검색, 목록, 상세코드 이동 CTA route shell입니다.",
    menuPath: "시스템 관리 > 공통코드 관리 > 코드그룹 관리",
    stateSummary:
      "상세코드 route parameter는 선택된 코드그룹에서만 구성합니다.",
  },
  {
    path: "/admin/code-groups/:groupId/codes",
    title: "상세코드 관리",
    description:
      "선택된 코드그룹 하위 상세코드 목록과 계층 preview route shell입니다.",
    menuPath: "시스템 관리 > 공통코드 관리 > 상세코드 관리",
    stateSummary: "코드그룹 식별자는 route parameter에서 읽습니다.",
  },
];

export function renderAdminRoute(pathname: string): ReactNode {
  if (isOutOfScopeRoute(pathname)) {
    return renderOutOfScopeRoute(pathname);
  }
  if (pathname === "/admin/users") {
    return <UserManagementPage />;
  }
  if (pathname === "/admin/organizations") {
    return <OrganizationManagementPage />;
  }
  if (pathname === "/admin/roles") {
    return <RoleManagementPage />;
  }
  if (pathname === "/admin/user-roles") {
    return <UserRoleManagementPage />;
  }
  if (pathname === "/admin/menu-permissions") {
    return <MenuPermissionManagementPage />;
  }
  if (pathname === "/admin/menu-structure") {
    return <MenuStructureManagementPage />;
  }
  if (pathname === "/admin/menus") {
    return <MenuInformationManagementPage />;
  }
  if (pathname === "/admin/code-groups") {
    return <CodeGroupManagementPage />;
  }
  if (/^\/admin\/code-groups\/[^/]+\/codes$/.test(pathname)) {
    return <CodeDetailManagementPage />;
  }

  const route = adminRoutes.find((item) => matchesRoute(item.path, pathname));
  if (!route) {
    return <PermissionState title="관리 route 접근 권한이 없습니다" />;
  }
  return <RouteShell route={route} pathname={pathname} />;
}

export function routeTitle(pathname: string): string {
  return (
    adminRoutes.find((item) => matchesRoute(item.path, pathname))?.title ??
    "권한 없음"
  );
}

function RouteShell({
  route,
  pathname,
}: {
  route: AdminRouteDefinition;
  pathname: string;
}) {
  const groupId = extractGroupId(route.path, pathname);
  return (
    <section className="content-card" aria-labelledby="page-title">
      <p className="breadcrumb">{route.menuPath}</p>
      <h1 id="page-title">{route.title}</h1>
      <p>{route.description}</p>
      {groupId ? <p className="info-chip">선택 코드그룹: {groupId}</p> : null}
      <div className="state-grid" aria-label="공통 화면 상태">
        <StateBox label="loading" text="데이터를 불러오는 중입니다." />
        <StateBox label="empty" text="조건에 맞는 데이터가 없습니다." />
        <StateBox
          label="error"
          text="ApiError.message와 field 오류를 표시합니다."
        />
        <StateBox label="permission" text="접근 권한이 없습니다." />
        <StateBox label="success" text={route.stateSummary} />
      </div>
    </section>
  );
}

function StateBox({ label, text }: { label: string; text: string }) {
  return (
    <div className="state-box">
      <strong>{label}</strong>
      <span>{text}</span>
    </div>
  );
}

function PermissionState({ title }: { title: string }) {
  return (
    <section className="content-card permission-state">
      <h1>{title}</h1>
      <p>
        listNavigationMenus에 없는 메뉴는 표시하지 않으며 직접 URL 접근은 권한
        없음 상태로 연결합니다.
      </p>
    </section>
  );
}

function matchesRoute(pattern: string, pathname: string): boolean {
  if (!pattern.includes(":")) {
    return pattern === pathname;
  }
  const regex = new RegExp(`^${pattern.replace(":groupId", "[^/]+")}$`);
  return regex.test(pathname);
}

function extractGroupId(pattern: string, pathname: string): string | null {
  if (pattern !== "/admin/code-groups/:groupId/codes") {
    return null;
  }
  return pathname.match(/^\/admin\/code-groups\/([^/]+)\/codes$/)?.[1] ?? null;
}
