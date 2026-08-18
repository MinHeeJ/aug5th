import { apiFetch } from "../api/client";

export interface ApiResponse<T> {
  success: boolean;
  meta: Record<string, unknown>;
  data: T;
  message?: string | null;
}

export interface ApiErrorPayload {
  success: false;
  meta?: Record<string, unknown>;
  error?: {
    code?: string;
    message?: string;
    fields?: Record<string, string>;
  };
}

export interface AuthenticatedUser {
  userId?: string;
  loginId: string;
  staffName: string;
  roleCodes: string[];
}

export interface NavigationMenu {
  menuId: string;
  parentMenuId?: string | null;
  menuLevel: "MAIN" | "MIDDLE" | "SUB" | string;
  displayOrder: number;
  menuName: string;
  screenId?: string | null;
  url?: string | null;
  icon?: string | null;
  businessDivision?: string | null;
  children: NavigationMenu[];
}

export interface SessionState {
  user: AuthenticatedUser | null;
  menus: NavigationMenu[];
}

function unwrap<T>(response: ApiResponse<T>): T {
  return response.data;
}

export async function login(
  username: string,
  password: string,
): Promise<AuthenticatedUser> {
  return unwrap(
    await apiFetch<ApiResponse<AuthenticatedUser>>("/api/auth/login", {
      method: "POST",
      body: { username, password },
    }),
  );
}

export async function logout(): Promise<void> {
  await apiFetch<ApiResponse<Record<string, never>>>("/api/auth/logout", {
    method: "POST",
  });
}

export async function getCurrentUser(): Promise<AuthenticatedUser> {
  return unwrap(await apiFetch<ApiResponse<AuthenticatedUser>>("/api/auth/me"));
}

export async function listNavigationMenus(): Promise<NavigationMenu[]> {
  return unwrap(
    await apiFetch<ApiResponse<NavigationMenu[]>>("/api/navigation/menus"),
  );
}

export async function loadSession(): Promise<SessionState> {
  const user = await getCurrentUser();
  const menus = await listNavigationMenus();
  return { user, menus };
}

export function firstLeafUrl(menus: NavigationMenu[]): string | null {
  for (const menu of menus) {
    if (menu.menuLevel === "SUB" && menu.url) {
      return menu.url;
    }
    const child = firstLeafUrl(menu.children ?? []);
    if (child) {
      return child;
    }
  }
  return null;
}

export function errorMessage(error: unknown): string {
  const payload =
    typeof error === "object" && error !== null && "payload" in error
      ? (error as { payload?: ApiErrorPayload }).payload
      : undefined;
  return payload?.error?.message ?? "요청 처리 중 오류가 발생했습니다.";
}

export function fieldErrors(error: unknown): Record<string, string> {
  const payload =
    typeof error === "object" && error !== null && "payload" in error
      ? (error as { payload?: ApiErrorPayload }).payload
      : undefined;
  return payload?.error?.fields ?? {};
}
