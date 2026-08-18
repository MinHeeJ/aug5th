import { useEffect, useMemo, useState } from "react";
import { LoginPage } from "../auth/LoginPage";
import {
  errorMessage,
  firstLeafUrl,
  loadSession,
  logout,
  NavigationMenu,
} from "../auth/session";
import { AdminLayout } from "../layout/AdminLayout";
import { renderAdminRoute } from "./adminRoutes";

interface SessionViewState {
  status: "checking" | "anonymous" | "authenticated" | "permission" | "error";
  userName: string;
  menus: NavigationMenu[];
  message?: string;
}

export function AppRoutes() {
  const [pathname, setPathname] = useState(window.location.pathname);
  const [session, setSession] = useState<SessionViewState>({
    status: "checking",
    userName: "",
    menus: [],
  });

  const navigate = useMemo(
    () => (path: string) => {
      window.history.pushState({}, "", path);
      setPathname(path);
    },
    [],
  );

  useEffect(() => {
    const onPopState = () => setPathname(window.location.pathname);
    window.addEventListener("popstate", onPopState);
    return () => window.removeEventListener("popstate", onPopState);
  }, []);

  useEffect(() => {
    if (pathname === "/login") {
      setSession((current) =>
        current.status === "authenticated"
          ? current
          : { status: "anonymous", userName: "", menus: [] },
      );
      return;
    }
    let active = true;
    setSession((current) => ({
      ...current,
      status: current.status === "authenticated" ? "authenticated" : "checking",
    }));
    loadSession()
      .then(({ user, menus }) => {
        if (!active) {
          return;
        }
        if (
          user == null ||
          !user.roleCodes.includes("R09") ||
          menus.length === 0
        ) {
          setSession({
            status: "permission",
            userName: user?.staffName ?? "",
            menus,
            message: "권한 없음: R09 시스템관리자 메뉴 접근권한이 필요합니다.",
          });
          return;
        }
        setSession({
          status: "authenticated",
          userName: user.staffName,
          menus,
          message: "R09 시스템관리자 메뉴를 불러왔습니다.",
        });
      })
      .catch((error) => {
        if (!active) {
          return;
        }
        setSession({
          status: "anonymous",
          userName: "",
          menus: [],
          message: errorMessage(error),
        });
        navigate("/login");
      });
    return () => {
      active = false;
    };
  }, [pathname, navigate]);

  async function handleLogout() {
    try {
      await logout();
    } finally {
      setSession({ status: "anonymous", userName: "", menus: [] });
      navigate("/login");
    }
  }

  if (pathname === "/login") {
    return (
      <LoginPage
        navigate={navigate}
        onAuthenticated={(menus) => {
          setSession({
            status: "authenticated",
            userName: "시스템 관리자",
            menus,
            message: "R09 시스템관리자 메뉴를 불러왔습니다.",
          });
        }}
      />
    );
  }

  if (session.status === "checking") {
    return (
      <FullScreenState
        label="loading"
        message="세션과 메뉴를 확인하는 중입니다."
      />
    );
  }

  if (session.status === "permission") {
    return (
      <FullScreenState
        label="permission"
        message={session.message ?? "관리 화면 접근 권한이 없습니다."}
      />
    );
  }

  if (session.status !== "authenticated") {
    return (
      <FullScreenState
        label="empty"
        message="로그인이 필요합니다."
        actionLabel="로그인으로 이동"
        onAction={() => navigate("/login")}
      />
    );
  }

  const safePath =
    pathname === "/"
      ? (firstLeafUrl(session.menus) ?? "/admin/users")
      : pathname;

  return (
    <AdminLayout
      menus={session.menus}
      pathname={safePath}
      userName={session.userName}
      message={session.message}
      navigate={navigate}
      onLogout={handleLogout}
    >
      {renderAdminRoute(safePath)}
    </AdminLayout>
  );
}

function FullScreenState({
  label,
  message,
  actionLabel,
  onAction,
}: {
  label: string;
  message: string;
  actionLabel?: string;
  onAction?: () => void;
}) {
  return (
    <main className="full-state">
      <div className={`status ${label}`}>{label}</div>
      <h1>{message}</h1>
      {actionLabel && onAction ? (
        <button type="button" onClick={onAction}>
          {actionLabel}
        </button>
      ) : null}
    </main>
  );
}
