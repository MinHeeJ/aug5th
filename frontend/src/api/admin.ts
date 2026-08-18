import { apiFetch } from "./client";

export interface ApiResponse<T> {
  success: boolean;
  meta: Record<string, unknown>;
  data: T;
  message?: string | null;
}

export interface UserSummary {
  userId: string;
  staffId: string;
  staffName: string;
  organizationCode: string;
  rankTitle: string;
  employmentStatus: string;
  positionTitle: string;
  retirementDate: string | null;
  lastSyncedAt: string;
  isSystemEnabled: boolean;
  roles: string[];
}

export interface UserSearchFilters {
  staffId?: string;
  staffName?: string;
  organizationCode?: string;
  rankTitle?: string;
  employmentStatus?: string;
  roleCode?: string;
  systemEnabled?: string;
}

export interface Organization {
  organizationId: string;
  organizationCode: string;
  organizationName: string;
  organizationType: string;
  isUsed: boolean;
}

export interface OrganizationTreeNode extends Organization {
  relationshipId: string | null;
  parentOrganizationId: string | null;
  effectiveStartDate: string | null;
  effectiveEndDate: string | null;
  children: OrganizationTreeNode[];
}

export interface OrganizationRelationshipRequest {
  organizationId: string;
  parentOrganizationId: string | null;
  effectiveStartDate: string;
  effectiveEndDate?: string | null;
  changeReason?: string | null;
}

function buildQuery(params: Record<string, string | undefined>): string {
  const search = new URLSearchParams();
  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined && value !== "") {
      search.set(key, value);
    }
  });
  const query = search.toString();
  return query ? `?${query}` : "";
}

export async function listUsers(
  filters: UserSearchFilters = {},
): Promise<UserSummary[]> {
  const query = buildQuery({
    staffId: filters.staffId,
    staffName: filters.staffName,
    organizationCode: filters.organizationCode,
    rankTitle: filters.rankTitle,
    employmentStatus: filters.employmentStatus,
    roleCode: filters.roleCode,
    systemEnabled: filters.systemEnabled,
  });
  const response = await apiFetch<ApiResponse<UserSummary[]>>(
    `/api/admin/users${query}`,
  );
  return response.data;
}

export async function updateUserSystemAccess(
  userId: string,
  body: {
    isSystemEnabled: boolean;
    roleCodes: string[];
    changeReason?: string;
  },
): Promise<UserSummary> {
  const response = await apiFetch<ApiResponse<UserSummary>>(
    `/api/admin/users/${userId}/system-access`,
    {
      method: "PATCH",
      body,
    },
  );
  return response.data;
}

export async function listOrganizations(
  filters: { organizationCode?: string; organizationType?: string } = {},
): Promise<Organization[]> {
  const query = buildQuery(filters);
  const response = await apiFetch<ApiResponse<Organization[]>>(
    `/api/admin/organizations${query}`,
  );
  return response.data;
}

export async function getOrganizationTree(): Promise<OrganizationTreeNode[]> {
  const response = await apiFetch<ApiResponse<OrganizationTreeNode[]>>(
    "/api/admin/organization-tree",
  );
  return response.data;
}

export async function saveOrganizationRelationship(
  relationshipId: string,
  body: OrganizationRelationshipRequest,
): Promise<unknown> {
  const response = await apiFetch<ApiResponse<unknown>>(
    `/api/admin/organization-relationships/${relationshipId}`,
    {
      method: "PUT",
      body,
    },
  );
  return response.data;
}

export interface MenuItem {
  menuId: string;
  parentMenuId: string | null;
  menuLevel: "MAIN" | "MIDDLE" | "SUB" | string;
  displayOrder: number;
  menuName: string;
  screenId: string | null;
  url: string | null;
  icon: string | null;
  businessDivision: string | null;
  description: string | null;
  isUsed: boolean;
}

export interface MenuRequest {
  menuId?: string;
  parentMenuId?: string | null;
  menuLevel: string;
  displayOrder: number;
  menuName: string;
  screenId?: string | null;
  url?: string | null;
  icon?: string | null;
  businessDivision?: string | null;
  description?: string | null;
  isUsed: boolean;
  changeReason?: string | null;
}

export interface MenuPermission {
  permissionId: string | null;
  targetType: string;
  targetId: string;
  menuId: string;
  parentMenuId: string | null;
  menuLevel: string;
  displayOrder: number;
  menuName: string;
  screenId: string | null;
  url: string | null;
  icon: string | null;
  businessDivision: string | null;
  description: string | null;
  isAllowed: boolean;
  isMenuUsed: boolean;
}

export async function listMenuPermissions(
  params: { targetType?: string; targetId?: string; filter?: string } = {},
): Promise<MenuPermission[]> {
  const query = buildQuery(params);
  const response = await apiFetch<ApiResponse<MenuPermission[]>>(
    `/api/admin/menu-permissions${query}`,
  );
  return response.data;
}

export async function saveMenuPermissions(
  targetType: string,
  targetId: string,
  permissions: Array<Pick<MenuPermission, "menuId" | "isAllowed">>,
  changeReason?: string,
): Promise<MenuPermission[]> {
  const response = await apiFetch<ApiResponse<MenuPermission[]>>(
    `/api/admin/menu-permissions/${targetType}/${targetId}`,
    {
      method: "PUT",
      body: {
        permissions: permissions.map((permission) => ({
          targetType,
          targetId,
          menuId: permission.menuId,
          isAllowed: permission.isAllowed,
        })),
        changeReason,
      },
    },
  );
  return response.data;
}

export async function getEffectiveMenuPermissions(): Promise<MenuPermission[]> {
  const response = await apiFetch<ApiResponse<MenuPermission[]>>(
    "/api/admin/menu-permissions/effective",
  );
  return response.data;
}

export async function getMenuStructure(): Promise<MenuItem[]> {
  const response = await apiFetch<ApiResponse<MenuItem[]>>(
    "/api/admin/menu-structure",
  );
  return response.data;
}

export async function saveMenuStructure(
  menuId: string,
  body: MenuRequest,
): Promise<MenuItem> {
  const response = await apiFetch<ApiResponse<MenuItem>>(
    `/api/admin/menu-structure/${menuId}`,
    { method: "PUT", body },
  );
  return response.data;
}

export async function reorderMenuStructure(
  parentMenuId: string | null,
  orderedMenuIds: string[],
): Promise<MenuItem[]> {
  const response = await apiFetch<ApiResponse<MenuItem[]>>(
    "/api/admin/menu-structure/reorder",
    {
      method: "PUT",
      body: { parentMenuId, orderedMenuIds },
    },
  );
  return response.data;
}

export async function listMenus(): Promise<MenuItem[]> {
  const response = await apiFetch<ApiResponse<MenuItem[]>>("/api/admin/menus");
  return response.data;
}

export async function saveMenu(
  menuId: string,
  body: MenuRequest,
): Promise<MenuItem> {
  const response = await apiFetch<ApiResponse<MenuItem>>(
    `/api/admin/menus/${menuId}`,
    { method: "PUT", body },
  );
  return response.data;
}

export async function updateMenuStatus(
  menuId: string,
  isUsed: boolean,
  changeReason?: string,
): Promise<MenuItem> {
  const response = await apiFetch<ApiResponse<MenuItem>>(
    `/api/admin/menus/${menuId}/status`,
    {
      method: "PATCH",
      body: { isUsed, changeReason },
    },
  );
  return response.data;
}
