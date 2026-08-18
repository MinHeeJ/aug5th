import { useEffect, useMemo, useState } from "react";
import type { FormEvent, ReactNode } from "react";

type ApiError = {
  code: string;
  message: string;
  fields?: Record<string, string>;
};

type ApiResponse<T> = {
  success: boolean;
  data?: T;
  error?: ApiError;
};

type AuthenticatedUser = {
  userId: string;
  roles: string[];
  dataScope: string;
};

type UserListItem = {
  userId: string;
  enabled: boolean;
  roleSummary?: string;
  status: string;
  employeeNo?: string;
  name?: string;
  departmentCode?: string;
  departmentName?: string;
  rankName?: string;
  employmentStatus?: string;
  positionSummary?: string;
  retirementDate?: string;
  lastSyncedAt?: string;
};

type UserListResponse = {
  items: UserListItem[];
  page: number;
  size: number;
  totalCount: number;
  screenId: string;
  requiredRole: string;
};

type OrganizationListItem = {
  organizationCode: string;
  organizationName: string;
  parentOrganizationCode?: string;
  parentOrganizationName?: string;
  validFrom: string;
  validTo?: string;
  enabled: boolean;
  childCount: number;
  assignedUserCount: number;
};

type OrganizationListResponse = {
  items: OrganizationListItem[];
  page: number;
  size: number;
  totalCount: number;
  screenId: string;
  requiredRole: string;
};

type PositionListItem = {
  positionId: number;
  positionCode: string;
  positionName: string;
  userId: string;
  userName?: string;
  employeeNo?: string;
  organizationCode: string;
  organizationName?: string;
  validFrom: string;
  validTo?: string;
  active: boolean;
};

type PositionListResponse = {
  items: PositionListItem[];
  page: number;
  size: number;
  totalCount: number;
  screenId: string;
  requiredRole: string;
};

type RoleListItem = {
  roleCode: string;
  roleName: string;
  purpose?: string;
  grantCriteria?: string;
  defaultDataScope: string;
  enabled: boolean;
  assignedUserCount: number;
  menuPermissionCount: number;
  functionPermissionCount: number;
};

type RoleListResponse = {
  items: RoleListItem[];
  page: number;
  size: number;
  totalCount: number;
  screenId: string;
  requiredRole: string;
};

type UserRoleListItem = {
  userRoleId: number;
  userId: string;
  userName?: string;
  employeeNo?: string;
  roleCode: string;
  roleName: string;
  validFrom: string;
  validTo?: string;
  approverId?: string;
  approverName?: string;
  assignmentSource: "MANUAL" | "POSITION";
  active: boolean;
};

type UserRoleListResponse = {
  items: UserRoleListItem[];
  page: number;
  size: number;
  totalCount: number;
  screenId: string;
  requiredRole: string;
};

type MenuPermissionListItem = {
  menuPermissionId: number;
  targetType: "ROLE" | "ORG" | "USER";
  targetId: string;
  targetName?: string;
  menuId: string;
  menuName: string;
  parentMenuName?: string;
  screenId: string;
  url: string;
  allowed: boolean;
  permissionSource: string;
  displayOrder: number;
};

type MenuPermissionListResponse = {
  items: MenuPermissionListItem[];
  page: number;
  size: number;
  totalCount: number;
  screenId: string;
  requiredRole: string;
};

type FunctionPermissionListItem = {
  functionPermissionId: number;
  roleCode: string;
  roleName: string;
  screenId: string;
  screenName: string;
  menuId?: string;
  menuName?: string;
  actionCode: string;
  actionName: string;
  allowed: boolean;
  permissionScope: string;
  displayOrder: number;
};

type FunctionPermissionListResponse = {
  items: FunctionPermissionListItem[];
  page: number;
  size: number;
  totalCount: number;
  screenId: string;
  requiredRole: string;
};

type DataScopeListItem = {
  dataScopeId: number;
  roleCode: string;
  roleName: string;
  scopeType: string;
  scopeName: string;
  organizationCode?: string;
  organizationName?: string;
  businessArea?: string;
  businessAreaName?: string;
  enforcementRule: string;
  displayOrder: number;
};

type DataScopeListResponse = {
  items: DataScopeListItem[];
  page: number;
  size: number;
  totalCount: number;
  screenId: string;
  requiredRole: string;
};

type MenuListItem = {
  menuId: string;
  parentMenuId?: string;
  parentMenuName?: string;
  menuName: string;
  screenId: string;
  url: string;
  displayOrder: number;
  childCount: number;
  permissionCount: number;
  menuUsageRule: string;
};

type MenuListResponse = {
  items: MenuListItem[];
  page: number;
  size: number;
  totalCount: number;
  screenId: string;
  requiredRole: string;
};

type CodeGroupListItem = {
  groupId: string;
  groupName: string;
  description?: string;
  managingDepartment?: string;
  enabled: boolean;
  detailCount: number;
  enabledDetailCount: number;
  detailManagementRule: string;
};

type CodeGroupListResponse = {
  items: CodeGroupListItem[];
  page: number;
  size: number;
  totalCount: number;
  screenId: string;
  requiredRole: string;
};

type CodeDetailListItem = {
  codeDetailId: number;
  groupId: string;
  groupName: string;
  codeValue: string;
  codeName: string;
  parentCodeValue?: string | null;
  parentCodeName?: string | null;
  displayOrder: number;
  active: boolean;
  detailUsageRule: string;
};

type CodeDetailListResponse = {
  items: CodeDetailListItem[];
  page: number;
  size: number;
  totalCount: number;
  screenId: string;
  requiredRole: string;
};

type SystemConfigurationListItem = {
  configKey: string;
  configValue: string;
  unit: string;
  valueRange?: string | null;
  enabled: boolean;
  applyScope: string;
  validationRule: string;
};

type SystemConfigurationListResponse = {
  items: SystemConfigurationListItem[];
  page: number;
  size: number;
  totalCount: number;
  screenId: string;
  requiredRole: string;
};

type BaseYearListItem = {
  baseYear: string;
  defaultQueryYear: string;
  copyBaselineEnabled: boolean;
  resetEnabled: boolean;
  enabled: boolean;
  periodRule: string;
  transitionRule: string;
};

type BaseYearListResponse = {
  items: BaseYearListItem[];
  page: number;
  size: number;
  totalCount: number;
  screenId: string;
  requiredRole: string;
};

type FilePolicyListItem = {
  filePolicyId: number;
  businessArea: string;
  businessAreaName: string;
  allowedExtensions: string;
  maxFileSizeMb: number;
  maxFileCount: number;
  maxTotalSizeMb: number;
  maxFilenameLength: number;
  malwareScanEnabled: boolean;
  enabled: boolean;
  uploadValidationRule: string;
  fileOperationBoundary: string;
};

type FilePolicyListResponse = {
  items: FilePolicyListItem[];
  page: number;
  size: number;
  totalCount: number;
  screenId: string;
  requiredRole: string;
};

type NoticeListItem = {
  noticeId: number;
  title: string;
  contentSummary: string;
  postFrom: string;
  postTo: string;
  targetRoles?: string | null;
  targetOrganizations?: string | null;
  important: boolean;
  enabled: boolean;
  attachmentCount: number;
  exposureRule: string;
  readBoundary: string;
};

type NoticeListResponse = {
  items: NoticeListItem[];
  page: number;
  size: number;
  totalCount: number;
  screenId: string;
  requiredRole: string;
};

type AttachmentListItem = {
  attachmentId: number;
  businessKey: string;
  originalName: string;
  storedName: string;
  extension: string;
  sizeBytes: number;
  uploadedBy: string;
  uploadedAt: string;
  malwareScanResult: string;
  deleted: boolean;
  finalizedRecord: boolean;
  storagePresent: boolean;
  integrityStatus: string;
  downloadAuthorizationRule: string;
  deleteBoundary: string;
};

type AttachmentListResponse = {
  items: AttachmentListItem[];
  page: number;
  size: number;
  totalCount: number;
  screenId: string;
  requiredRole: string;
};

type ExcelTemplateListItem = {
  templateId: number;
  businessArea: string;
  businessAreaName: string;
  version: string;
  requiredColumns: string;
  requiredColumnCount: number;
  effectiveDate: string;
  downloadFileId?: number | null;
  downloadFileName?: string | null;
  enabled: boolean;
  validationRule: string;
  downloadRule: string;
  updatedAt: string;
};

type ExcelTemplateListResponse = {
  items: ExcelTemplateListItem[];
  page: number;
  size: number;
  totalCount: number;
  screenId: string;
  requiredRole: string;
};

type ExcelUploadListItem = {
  uploadId: number;
  templateId: number;
  businessArea: string;
  businessAreaName: string;
  version: string;
  uploaderId: string;
  fileName: string;
  totalCount: number;
  successCount: number;
  errorCount: number;
  excludedCount: number;
  savedCount: number;
  processingTimeMs: number;
  uploadStatus: string;
  uploadedAt: string;
  transactionRule: string;
  validationRule: string;
};

type ExcelUploadListResponse = {
  items: ExcelUploadListItem[];
  page: number;
  size: number;
  totalCount: number;
  screenId: string;
  requiredRole: string;
};

type ExcelDownloadListItem = {
  downloadId: number;
  requesterId: string;
  queryCondition: string;
  dataScopeApplied: string;
  fileId?: number | null;
  fileName?: string | null;
  extension?: string | null;
  sizeBytes?: number | null;
  createdAt: string;
  generationRule: string;
  boundaryRule: string;
};

type ExcelDownloadListResponse = {
  items: ExcelDownloadListItem[];
  page: number;
  size: number;
  totalCount: number;
  screenId: string;
  requiredRole: string;
};

type PrivacyPolicyListItem = {
  fieldPolicyId: number;
  fieldName: string;
  privacyGrade: string;
  privacyGradeName: string;
  encryptionEnabled: boolean;
  maskingRule: string;
  logExcluded: boolean;
  policyRule: string;
  auditRule: string;
};

type PrivacyPolicyListResponse = {
  items: PrivacyPolicyListItem[];
  page: number;
  size: number;
  totalCount: number;
  screenId: string;
  requiredRole: string;
};

type SessionListItem = {
  sessionId: string;
  userId: string;
  userDisplayName: string;
  loginAt: string;
  lastActivityAt: string;
  ipAddress: string;
  sessionStatus: string;
  sessionStatusName: string;
  latestTerminationId?: number | null;
  latestTerminationType?: string | null;
  operationRule: string;
};

type SessionListResponse = {
  items: SessionListItem[];
  page: number;
  size: number;
  totalCount: number;
  screenId: string;
  requiredRole: string;
};

type AuditLogListItem = {
  auditLogId: number;
  logType: string;
  logTypeName: string;
  targetKey: string;
  actorId: string;
  beforeValue: string;
  afterValue: string;
  result: string;
  resultName: string;
  operationRule: string;
};

type AuditLogListResponse = {
  items: AuditLogListItem[];
  page: number;
  size: number;
  totalCount: number;
  screenId: string;
  requiredRole: string;
};

type BatchDefinitionListItem = {
  batchId: string;
  batchName: string;
  schedule: string;
  predecessorBatchId?: string;
  predecessorBatchName?: string;
  parameters: string;
  maxRuntimeSeconds: number;
  ownerId: string;
  ownerName: string;
  status: string;
  statusName: string;
  operationRule: string;
};

type BatchDefinitionListResponse = {
  items: BatchDefinitionListItem[];
  page: number;
  size: number;
  totalCount: number;
  screenId: string;
  requiredRole: string;
};

type BatchExecutionListItem = {
  batchExecutionId: number;
  batchId: string;
  batchName: string;
  parameters: string;
  reason: string;
  executionStatus: string;
  executionStatusName: string;
  requestedBy: string;
  requestedByName: string;
  operationRule: string;
};

type BatchExecutionListResponse = {
  items: BatchExecutionListItem[];
  page: number;
  size: number;
  totalCount: number;
  screenId: string;
  requiredRole: string;
};

type BatchResultListItem = {
  batchResultId: number;
  batchExecutionId: number;
  batchId: string;
  batchName: string;
  startedAt: string;
  endedAt?: string;
  totalCount: number;
  successCount: number;
  failureCount: number;
  excludedCount: number;
  durationMs?: number;
  logFileId?: number;
  logFileName?: string;
  resultStatus: string;
  resultStatusName: string;
  logAccessRule: string;
  operationRule: string;
};

type BatchResultListResponse = {
  items: BatchResultListItem[];
  page: number;
  size: number;
  totalCount: number;
  screenId: string;
  requiredRole: string;
};

type BatchResultLogResponse = {
  batchResultId: number;
  batchExecutionId: number;
  logFileId: number;
  logFileName: string;
  accessMessage: string;
  immutableRule: string;
};

type ViewState = "loading" | "empty" | "error" | "permission" | "success";

type HealthData = {
  status: string;
  service: string;
  timestamp: string;
};

const sizeOptions = [20, 50, 100];

async function requestJson<T>(url: string, init?: RequestInit): Promise<T> {
  const response = await fetch(url, {
    credentials: "include",
    headers: { "Content-Type": "application/json", ...(init?.headers ?? {}) },
    ...init,
  });
  const body = (await response.json()) as ApiResponse<T>;
  if (!response.ok || !body.success || !body.data) {
    const error = new Error(
      body.error?.message ?? `요청 실패: ${response.status}`,
    ) as Error & { apiError?: ApiError; status?: number };
    error.apiError = body.error;
    error.status = response.status;
    throw error;
  }
  return body.data;
}

const NAV_GROUPS = [
  {
    label: "사용자·조직",
    accent: "bg-primary",
    items: [
      { href: "/admin/system/users", label: "사용자 관리" },
      { href: "/admin/system/organizations", label: "조직 관리" },
      { href: "/admin/system/positions", label: "보직 관리" },
    ],
  },
  {
    label: "역할·권한",
    accent: "bg-[#c4a1ff]",
    items: [
      { href: "/admin/security/roles", label: "역할 관리" },
      { href: "/admin/security/user-roles", label: "사용자 역할 관리" },
      { href: "/admin/security/menu-permissions", label: "메뉴 권한 관리" },
      { href: "/admin/security/function-permissions", label: "기능 권한 관리" },
      { href: "/admin/security/data-scopes", label: "데이터 범위 권한" },
    ],
  },
  {
    label: "메뉴·코드",
    accent: "bg-[#01ffcc]",
    items: [
      { href: "/admin/menus", label: "메뉴 관리" },
      { href: "/admin/codes/groups", label: "코드그룹 관리" },
      { href: "/admin/codes/details", label: "상세코드 관리" },
    ],
  },
  {
    label: "환경설정",
    accent: "bg-[#e7f192]",
    items: [
      { href: "/admin/settings/common", label: "공통 환경설정" },
      { href: "/admin/settings/base-years", label: "기준연도 관리" },
      { href: "/admin/settings/file-policies", label: "파일정책 관리" },
    ],
  },
  {
    label: "파일·엑셀",
    accent: "bg-[#ff30cd]",
    items: [
      { href: "/admin/notices", label: "공지사항 관리" },
      { href: "/admin/files/attachments", label: "첨부파일 관리" },
      { href: "/admin/excel/templates", label: "업로드 양식 관리" },
      { href: "/admin/excel/uploads", label: "엑셀 업로드" },
      { href: "/admin/excel/downloads", label: "엑셀 다운로드" },
    ],
  },
  {
    label: "보안·운영",
    accent: "bg-black text-white",
    items: [
      { href: "/admin/security/privacy", label: "개인정보 관리" },
      { href: "/admin/security/sessions", label: "접속현황 관리" },
      { href: "/admin/security/audit-logs", label: "감사 로그 관리" },
      { href: "/admin/operations/batch-definitions", label: "배치 정의 관리" },
      { href: "/admin/operations/batch-executions", label: "배치 실행 관리" },
      { href: "/admin/operations/batch-results", label: "배치 결과 조회" },
    ],
  },
];

const ROUTE_MENU_PATHS: Record<string, string> = {
  "/admin/system/users": "시스템 관리 > 사용자·조직 관리 > 사용자 관리",
  "/admin/system/organizations": "시스템 관리 > 사용자·조직 관리 > 조직 관리",
  "/admin/system/positions": "시스템 관리 > 사용자·조직 관리 > 보직 관리",
  "/admin/security/roles": "시스템 관리 > 역할·권한 관리 > 역할 관리",
  "/admin/security/user-roles":
    "시스템 관리 > 역할·권한 관리 > 사용자 역할 관리",
  "/admin/security/menu-permissions":
    "시스템 관리 > 역할·권한 관리 > 메뉴 권한 관리",
  "/admin/security/function-permissions":
    "시스템 관리 > 역할·권한 관리 > 기능 권한 관리",
  "/admin/security/data-scopes":
    "시스템 관리 > 역할·권한 관리 > 데이터 범위 권한",
  "/admin/menus": "시스템 관리 > 메뉴 관리 > 메뉴 관리",
  "/admin/codes/groups": "시스템 관리 > 공통코드 관리 > 코드그룹 관리",
  "/admin/codes/details": "시스템 관리 > 공통코드 관리 > 상세코드 관리",
  "/admin/settings/common": "시스템 관리 > 시스템 환경설정 > 공통 환경설정",
  "/admin/settings/base-years": "시스템 관리 > 시스템 환경설정 > 기준연도 관리",
  "/admin/settings/file-policies":
    "시스템 관리 > 시스템 환경설정 > 파일정책 관리",
  "/admin/notices": "시스템 관리 > 공지·도움말 관리 > 공지사항 관리",
  "/admin/files/attachments":
    "파일·데이터 관리 > 첨부파일 관리 > 첨부파일 관리",
  "/admin/excel/templates": "파일·데이터 관리 > 엑셀 관리 > 업로드 양식 관리",
  "/admin/excel/uploads": "파일·데이터 관리 > 엑셀 관리 > 엑셀 업로드",
  "/admin/excel/downloads": "파일·데이터 관리 > 엑셀 관리 > 엑셀 다운로드",
  "/admin/security/privacy": "보안·감사 관리 > 개인정보 관리 > 개인정보 관리",
  "/admin/security/sessions": "보안·감사 관리 > 접속기록 관리 > 접속현황 관리",
  "/admin/security/audit-logs":
    "보안·감사 관리 > 감사로그 관리 > 감사 로그 관리",
  "/admin/operations/batch-definitions":
    "시스템 운영 관리 > 배치작업 관리 > 배치 정의 관리",
  "/admin/operations/batch-executions":
    "시스템 운영 관리 > 배치작업 관리 > 배치 실행 관리",
  "/admin/operations/batch-results":
    "시스템 운영 관리 > 배치작업 관리 > 배치 결과 조회",
};

function AppShell({ children }: { children: ReactNode }) {
  const currentPath = window.location.pathname;
  const currentItem = NAV_GROUPS.flatMap((group) => group.items).find(
    (item) => item.href === currentPath,
  );
  const currentMenuPath = ROUTE_MENU_PATHS[currentPath];
  const currentGroup = NAV_GROUPS.find((group) =>
    group.items.some((item) => item.href === currentPath),
  );
  const [selectedGroup, setSelectedGroup] = useState<string | null>(() => {
    if (currentGroup) {
      return currentGroup.label;
    }
    const storedGroup = window.localStorage.getItem(
      "selected-navigation-group",
    );
    return NAV_GROUPS.some((group) => group.label === storedGroup)
      ? storedGroup
      : null;
  });
  const [hoveredGroup, setHoveredGroup] = useState<string | null>(null);
  const [manuallyOpenGroups, setManuallyOpenGroups] = useState<Set<string>>(
    new Set(),
  );
  const [manuallyClosedGroups, setManuallyClosedGroups] = useState<Set<string>>(
    new Set(),
  );

  function isGroupOpen(groupLabel: string) {
    return (
      !manuallyClosedGroups.has(groupLabel) &&
      (selectedGroup === groupLabel ||
        hoveredGroup === groupLabel ||
        manuallyOpenGroups.has(groupLabel))
    );
  }

  function toggleGroup(groupLabel: string) {
    const openedBySelectionOrArrow =
      !manuallyClosedGroups.has(groupLabel) &&
      (selectedGroup === groupLabel || manuallyOpenGroups.has(groupLabel));
    if (openedBySelectionOrArrow) {
      setManuallyClosedGroups((groups) => new Set(groups).add(groupLabel));
      setManuallyOpenGroups((groups) => {
        const nextGroups = new Set(groups);
        nextGroups.delete(groupLabel);
        return nextGroups;
      });
      return;
    }
    setManuallyClosedGroups((groups) => {
      const nextGroups = new Set(groups);
      nextGroups.delete(groupLabel);
      return nextGroups;
    });
    setManuallyOpenGroups((groups) => new Set(groups).add(groupLabel));
  }

  function closeGroup(groupLabel: string) {
    setManuallyClosedGroups((groups) => new Set(groups).add(groupLabel));
    setManuallyOpenGroups((groups) => {
      const nextGroups = new Set(groups);
      nextGroups.delete(groupLabel);
      return nextGroups;
    });
  }

  function selectGroup(groupLabel: string) {
    window.localStorage.setItem("selected-navigation-group", groupLabel);
    setSelectedGroup(groupLabel);
    setManuallyClosedGroups((groups) => {
      const nextGroups = new Set(groups);
      nextGroups.delete(groupLabel);
      return nextGroups;
    });
  }

  return (
    <div className="min-h-screen bg-background text-foreground selection:bg-primary selection:text-foreground">
      <a
        className="sr-only focus:not-sr-only focus:fixed focus:left-4 focus:top-4 focus:z-50 focus:border-2 focus:border-black focus:bg-primary focus:px-4 focus:py-2 focus:font-head focus:font-bold focus:shadow-hard"
        href="#main"
      >
        본문으로 이동
      </a>
      <div className="border-b-2 border-black bg-primary px-4 py-2.5 text-center font-head text-xs font-black uppercase tracking-[0.16em] sm:text-sm">
        KNUE Common Foundation · API-backed R09 Admin Surface
      </div>
      <div className="lg:flex lg:items-start">
        <aside
          className="z-40 border-b-2 border-black bg-background/95 backdrop-blur-sm lg:sticky lg:top-0 lg:h-screen lg:w-[22%] lg:min-w-[16rem] lg:max-w-[23rem] lg:overflow-y-auto lg:border-b-0 lg:border-r-2"
          data-testid="sidebar-navigation"
        >
          <div className="flex flex-col gap-3 px-4 py-3 sm:px-6 lg:px-4 lg:py-5">
            <div className="flex flex-col gap-3">
              <a
                className="group/brand inline-flex w-fit items-center gap-2.5 font-head text-xl font-black uppercase tracking-tight outline-none transition-all duration-200 hover:-translate-x-px hover:-translate-y-px focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-primary"
                href="/admin/system/users"
              >
                <span className="grid h-9 w-9 place-items-center border-2 border-black bg-primary shadow-hard transition-all duration-200 group-hover/brand:shadow-hard-lg">
                  KN
                </span>
                <span>
                  KNUE Common
                  <span className="block font-body text-xs font-bold normal-case tracking-normal text-muted-foreground">
                    {currentItem?.label ?? "공통기능 관리 콘솔"}
                  </span>
                </span>
              </a>
              <div className="flex flex-wrap items-center gap-2">
                <span className="rounded border-2 border-black bg-accent px-3 py-1 font-head text-xs font-black uppercase tracking-[0.14em] shadow-hard">
                  R09
                </span>
                <a
                  className="rounded border-2 border-black bg-card px-3 py-1 font-head text-xs font-black shadow-hard transition-all duration-200 hover:-translate-x-px hover:-translate-y-px hover:bg-accent hover:shadow-hard-lg active:translate-x-px active:translate-y-px active:shadow-none"
                  href="/api/health"
                >
                  Health
                </a>
              </div>
            </div>
            <nav aria-label="주요 관리 화면" className="grid gap-2">
              {NAV_GROUPS.map((group) => {
                const expanded = isGroupOpen(group.label);
                return (
                  <section
                    key={group.label}
                    className="border-2 border-black bg-card shadow-hard transition-colors duration-200"
                    data-testid={`sidebar-menu-${group.label}`}
                    onMouseEnter={() => setHoveredGroup(group.label)}
                    onMouseLeave={() => setHoveredGroup(null)}
                  >
                    <div className="flex items-center gap-2 border-b-2 border-black px-2 py-2">
                      <span
                        className={`border-2 border-black px-2 py-0.5 font-head text-xs font-black uppercase tracking-[0.12em] ${group.accent}`}
                      >
                        {group.label}
                      </span>
                      <button
                        aria-expanded={expanded}
                        aria-label={`${group.label} 메뉴 ${expanded ? "접기" : "펼치기"}`}
                        className="ml-auto grid h-7 w-7 place-items-center border-2 border-black bg-card font-head text-base font-black transition-colors hover:bg-accent focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-primary"
                        type="button"
                        onClick={() => toggleGroup(group.label)}
                      >
                        <span className={expanded ? "rotate-45" : ""}>+</span>
                      </button>
                      <button
                        aria-label={`${group.label} 메뉴 닫기`}
                        className="grid h-7 w-7 place-items-center border-2 border-black bg-card font-head text-sm font-black transition-colors hover:bg-destructive hover:text-white focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-primary"
                        type="button"
                        onClick={() => closeGroup(group.label)}
                      >
                        ×
                      </button>
                    </div>
                    {expanded ? (
                      <div className="flex flex-col gap-1 p-2">
                        {group.items.map((item) => {
                          const active = item.href === currentPath;
                          return (
                            <a
                              key={item.href}
                              className={`border-2 border-black px-3 py-2 font-head text-xs font-black shadow-hard transition-all duration-200 hover:-translate-x-px hover:-translate-y-px hover:shadow-hard-lg focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-primary active:translate-x-px active:translate-y-px active:shadow-none ${active ? "bg-primary" : "bg-card hover:bg-accent"}`}
                              aria-current={active ? "page" : undefined}
                              href={item.href}
                              onMouseDown={() => selectGroup(group.label)}
                              onClick={() => selectGroup(group.label)}
                            >
                              {item.label}
                            </a>
                          );
                        })}
                      </div>
                    ) : null}
                  </section>
                );
              })}
            </nav>
          </div>
        </aside>
        <main
          id="main"
          className="mx-auto flex w-full max-w-7xl flex-1 flex-col gap-8 px-4 py-8 sm:px-6 lg:px-10 lg:py-12"
        >
          {currentMenuPath ? (
            <nav
              aria-label="현재 메뉴 경로"
              className="w-fit border-2 border-black bg-card px-3 py-2 font-head text-xs font-black text-muted-foreground shadow-hard sm:text-sm"
            >
              {currentMenuPath}
            </nav>
          ) : null}
          {children}
        </main>
      </div>
    </div>
  );
}

function LoginPanel({
  onLogin,
}: {
  onLogin: (user: AuthenticatedUser) => void;
}) {
  const [userId, setUserId] = useState("admin");
  const [password, setPassword] = useState("admin");
  const [error, setError] = useState("");
  const [submitting, setSubmitting] = useState(false);

  async function submit(event: FormEvent) {
    event.preventDefault();
    setSubmitting(true);
    setError("");
    try {
      const user = await requestJson<AuthenticatedUser>("/api/auth/login", {
        method: "POST",
        body: JSON.stringify({ userId, password }),
      });
      onLogin(user);
    } catch (caught) {
      setError(
        caught instanceof Error ? caught.message : "로그인에 실패했습니다.",
      );
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <section className="relative mx-auto w-full max-w-[26rem]">
      <div className="absolute inset-2 border-2 border-black bg-primary" />
      <form
        onSubmit={submit}
        className="relative border-2 border-black bg-card shadow-[6px_6px_0_0_var(--border)] sm:shadow-[8px_8px_0_0_var(--border)]"
      >
        <div className="flex items-center justify-between gap-3 border-b-2 border-black bg-muted px-5 py-3.5 sm:px-7">
          <span className="font-head text-sm font-black uppercase">
            Dashboard Login
          </span>
          <span className="border-2 border-black bg-primary px-2 py-1 font-head text-xs font-black">
            R09
          </span>
        </div>
        <div className="space-y-4 px-5 py-7 sm:px-7 sm:py-8">
          <div>
            <h1 className="font-head text-3xl font-black uppercase leading-none tracking-tight sm:text-[2.15rem]">
              관리자 로그인
            </h1>
            <p className="mt-2 text-sm leading-relaxed text-muted-foreground">
              시드 관리자 계정으로 로그인하면 시스템 관리 화면과 API를 함께
              확인합니다.
            </p>
          </div>
          <label className="block">
            <span className="mb-2 block font-head text-sm font-bold">
              아이디
            </span>
            <input
              className="h-12 w-full rounded border-2 border-black bg-white px-4 py-2 shadow-hard focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-primary"
              value={userId}
              onChange={(event) => setUserId(event.target.value)}
            />
          </label>
          <label className="block">
            <span className="mb-2 block font-head text-sm font-bold">
              비밀번호
            </span>
            <input
              className="h-12 w-full rounded border-2 border-black bg-white px-4 py-2 shadow-hard focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-primary"
              type="password"
              value={password}
              onChange={(event) => setPassword(event.target.value)}
            />
          </label>
          {error ? (
            <p
              role="alert"
              className="border-2 border-black bg-destructive px-3 py-2 text-sm font-bold text-white"
            >
              {error}
            </p>
          ) : null}
          <button
            className="h-12 w-full rounded border-2 border-black bg-primary px-4 py-1.5 font-head text-base font-black shadow-[4px_4px_0_0_var(--shadow-color)] transition-all duration-200 hover:-translate-x-0.5 hover:-translate-y-0.5 hover:bg-primary-hover hover:shadow-[6px_6px_0_0_var(--shadow-color)] active:translate-x-1 active:translate-y-1 active:shadow-none disabled:opacity-60"
            type="submit"
            disabled={submitting}
          >
            {submitting ? "로그인 중..." : "admin 로그인"}
          </button>
        </div>
      </form>
    </section>
  );
}

export default function App() {
  const path = window.location.pathname;
  if (path === "/admin/system/users") {
    return <UserManagementPage />;
  }
  if (path === "/admin/system/organizations") {
    return <OrganizationManagementPage />;
  }
  if (path === "/admin/system/positions") {
    return <PositionManagementPage />;
  }
  if (path === "/admin/security/roles") {
    return <RoleManagementPage />;
  }
  if (path === "/admin/security/user-roles") {
    return <UserRoleManagementPage />;
  }
  if (path === "/admin/security/menu-permissions") {
    return <MenuPermissionManagementPage />;
  }
  if (path === "/admin/security/function-permissions") {
    return <FunctionPermissionManagementPage />;
  }
  if (path === "/admin/security/data-scopes") {
    return <DataScopeManagementPage />;
  }
  if (path === "/admin/menus") {
    return <MenuManagementPage />;
  }
  if (path === "/admin/codes/groups") {
    return <CodeGroupManagementPage />;
  }
  if (path === "/admin/codes/details") {
    return <CodeDetailManagementPage />;
  }
  if (path === "/admin/settings/common") {
    return <SystemConfigurationManagementPage />;
  }
  if (path === "/admin/settings/base-years") {
    return <BaseYearManagementPage />;
  }
  if (path === "/admin/settings/file-policies") {
    return <FilePolicyManagementPage />;
  }
  if (path === "/admin/notices") {
    return <NoticeManagementPage />;
  }
  if (path === "/admin/files/attachments") {
    return <AttachmentManagementPage />;
  }
  if (path === "/admin/excel/templates") {
    return <ExcelTemplateManagementPage />;
  }
  if (path === "/admin/excel/uploads") {
    return <ExcelUploadManagementPage />;
  }
  if (path === "/admin/excel/downloads") {
    return <ExcelDownloadManagementPage />;
  }
  if (path === "/admin/security/privacy") {
    return <PrivacyPolicyManagementPage />;
  }
  if (path === "/admin/security/sessions") {
    return <SessionManagementPage />;
  }
  if (path === "/admin/security/audit-logs") {
    return <AuditLogManagementPage />;
  }
  if (path === "/admin/operations/batch-definitions") {
    return <BatchDefinitionManagementPage />;
  }
  if (path === "/admin/operations/batch-executions") {
    return <BatchExecutionManagementPage />;
  }
  if (path === "/admin/operations/batch-results") {
    return <BatchResultManagementPage />;
  }
  return <SetupHome />;
}

function SetupHome() {
  const [health, setHealth] = useState<HealthData | null>(null);
  const [errorMessage, setErrorMessage] = useState("");

  useEffect(() => {
    requestJson<HealthData>("/api/health")
      .then(setHealth)
      .catch((error: unknown) =>
        setErrorMessage(
          error instanceof Error
            ? error.message
            : "알 수 없는 오류가 발생했습니다.",
        ),
      );
  }, []);

  return (
    <AppShell>
      <section className="grid grid-cols-1 items-center gap-8 lg:grid-cols-[minmax(0,1fr)_30rem]">
        <div className="flex flex-col items-start gap-6">
          <span className="border-2 border-black bg-muted px-2 py-1 font-head text-xs font-black uppercase tracking-[0.14em]">
            Phase 27
          </span>
          <h1 className="font-head text-4xl font-black uppercase leading-none tracking-tight sm:text-6xl">
            배치 결과 조회 Vertical Slice
          </h1>
          <p className="max-w-2xl text-base leading-relaxed text-muted-foreground sm:text-lg">
            /admin/operations/batch-results 화면에서 배치 실행ID별
            시작·종료시간, 처리건수, 성공·실패·제외건수, 소요시간과 로그파일을
            조회합니다.
          </p>
          <div className="flex flex-wrap gap-3">
            <a
              className="border-2 border-black bg-primary px-6 py-3 font-head font-black shadow-hard transition-all duration-200 hover:-translate-x-0.5 hover:-translate-y-0.5 hover:bg-primary-hover hover:shadow-hard-lg active:translate-x-1 active:translate-y-1 active:shadow-none"
              href="/admin/operations/batch-results"
            >
              배치 결과 조회 열기
            </a>
            <a
              className="border-2 border-black bg-card px-6 py-3 font-head font-black shadow-hard transition-all duration-200 hover:-translate-x-0.5 hover:-translate-y-0.5 hover:shadow-hard-lg active:translate-x-1 active:translate-y-1 active:shadow-none"
              href="/admin/operations/batch-definitions"
            >
              배치 정의 관리 열기
            </a>
            <a
              className="border-2 border-black bg-card px-6 py-3 font-head font-black shadow-hard transition-all duration-200 hover:-translate-x-0.5 hover:-translate-y-0.5 hover:shadow-hard-lg active:translate-x-1 active:translate-y-1 active:shadow-none"
              href="/admin/security/data-scopes"
            >
              데이터 범위 권한 열기
            </a>
          </div>
        </div>
        <aside className="relative min-h-[22rem]">
          <div className="absolute inset-2 border-2 border-black bg-primary" />
          <div className="relative flex h-full min-h-[22rem] flex-col border-2 border-black bg-card shadow-hard">
            <div className="border-b-2 border-black bg-muted px-4 py-3 font-head font-black">
              /api/health
            </div>
            <div className="flex flex-1 flex-col justify-center gap-4 p-5">
              <div className="border-2 border-black bg-background p-4 font-mono text-sm shadow-hard">
                {errorMessage
                  ? `ERROR ${errorMessage}`
                  : health
                    ? `STATUS ${health.status}`
                    : "LOADING ..."}
              </div>
              <div className="grid grid-cols-2 gap-3 text-sm">
                <div className="border-2 border-black bg-accent p-3 font-bold">
                  Service
                  <br />
                  {health?.service ?? "확인 중"}
                </div>
                <div className="border-2 border-black bg-accent p-3 font-bold">
                  Proxy
                  <br />
                  /api/*
                </div>
              </div>
            </div>
          </div>
        </aside>
      </section>
    </AppShell>
  );
}

function UserManagementPage() {
  const [user, setUser] = useState<AuthenticatedUser | null>(null);
  const [users, setUsers] = useState<UserListItem[]>([]);
  const [selectedUserId, setSelectedUserId] = useState<string>("");
  const [query, setQuery] = useState("");
  const [enabledFilter, setEnabledFilter] = useState("");
  const [size, setSize] = useState(20);
  const [state, setState] = useState<ViewState>("loading");
  const [message, setMessage] = useState("");
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});
  const [saving, setSaving] = useState(false);

  const selectedUser = useMemo(
    () => users.find((item) => item.userId === selectedUserId) ?? null,
    [selectedUserId, users],
  );

  useEffect(() => {
    requestJson<AuthenticatedUser>("/api/auth/session")
      .then((sessionUser) => {
        setUser(sessionUser);
        return loadUsers("", "", size);
      })
      .catch((caught) => {
        const error = caught as Error & { status?: number };
        if (error.status === 403) {
          setState("permission");
          setMessage(error.message);
        } else {
          setState("error");
          setMessage(error.message);
        }
      });
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  async function loadUsers(
    nextQuery = query,
    nextEnabled = enabledFilter,
    nextSize = size,
  ) {
    setState("loading");
    setMessage("");
    const filterParts = nextEnabled ? [`enabled=${nextEnabled}`] : [];
    const params = new URLSearchParams({
      page: "1",
      size: String(nextSize),
      sort: "employeeNo",
    });
    if (nextQuery.trim()) {
      params.set("q", nextQuery.trim());
    }
    if (filterParts.length > 0) {
      params.set("filter", filterParts.join(";"));
    }
    try {
      const body = await requestJson<UserListResponse>(
        `/api/admin/users?${params.toString()}`,
      );
      setUsers(body.items);
      setSelectedUserId(body.items[0]?.userId ?? "");
      setState(body.items.length === 0 ? "empty" : "success");
      setMessage(`총 ${body.totalCount}건 조회되었습니다.`);
    } catch (caught) {
      const error = caught as Error & { apiError?: ApiError; status?: number };
      setFieldErrors(error.apiError?.fields ?? {});
      setState(error.status === 403 ? "permission" : "error");
      setMessage(error.message);
    }
  }

  async function saveSelectedUser() {
    if (!selectedUser) {
      setMessage("저장할 행을 먼저 선택하세요.");
      return;
    }
    const confirmed = window.confirm(
      `${selectedUser.userId} 사용여부를 변경하시겠습니까?`,
    );
    if (!confirmed) {
      return;
    }
    setSaving(true);
    setFieldErrors({});
    try {
      const result = await requestJson<{
        userId: string;
        enabled: boolean;
        status: string;
        message: string;
      }>("/api/admin/users", {
        method: "POST",
        body: JSON.stringify({
          id: selectedUser.userId,
          enabled: !selectedUser.enabled,
          status: selectedUser.enabled ? "INACTIVE" : "ACTIVE",
          roleSummary: selectedUser.roleSummary ?? "",
          reason: "사용자 관리 화면 저장 CTA",
        }),
      });
      setMessage(result.message);
      await loadUsers();
      setSelectedUserId(result.userId);
    } catch (caught) {
      const error = caught as Error & { apiError?: ApiError; status?: number };
      setFieldErrors(error.apiError?.fields ?? {});
      setState(error.status === 403 ? "permission" : "error");
      setMessage(error.message);
    } finally {
      setSaving(false);
    }
  }

  if (!user && state === "error") {
    return (
      <AppShell>
        <LoginPanel
          onLogin={(nextUser) => {
            setUser(nextUser);
            void loadUsers();
          }}
        />
      </AppShell>
    );
  }

  return (
    <AppShell>
      <section className="space-y-7 sm:space-y-8 lg:space-y-9">
        <div className="flex flex-col gap-4 lg:flex-row lg:items-end lg:justify-between">
          <div>
            <div className="mb-3 inline-flex border-2 border-black bg-primary px-2.5 py-1 font-head text-xs font-black uppercase tracking-[0.14em] shadow-hard">
              SCR-USER-MGMT
            </div>
            <h1 className="font-head text-4xl font-black uppercase leading-none tracking-tight sm:text-5xl">
              사용자 관리
            </h1>
            <p className="mt-4 max-w-3xl text-sm leading-relaxed text-muted-foreground sm:text-base">
              교번·성명·소속·직급·재직상태·역할·사용여부 조건으로 사용자를
              조회하고, KORUS 원천정보는 읽기 전용으로 표시합니다.
            </p>
          </div>
          <div className="rounded border-2 border-black bg-card px-4 py-3 font-head text-sm font-black shadow-hard">
            {user
              ? `${user.userId} / ${user.roles.join(", ")}`
              : "세션 확인 중"}
          </div>
        </div>

        <form
          className="grid grid-cols-1 gap-4 border-2 border-black bg-card p-4 shadow-hard sm:grid-cols-[minmax(0,1fr)_10rem_9rem_9rem] sm:p-5"
          onSubmit={(event) => {
            event.preventDefault();
            void loadUsers();
          }}
        >
          <label className="block">
            <span className="mb-2 block font-head text-sm font-bold">
              통합 검색
            </span>
            <input
              className="h-12 w-full rounded border-2 border-black bg-white px-4 py-2 shadow-hard"
              placeholder="교번, 성명, 소속"
              value={query}
              onChange={(event) => setQuery(event.target.value)}
            />
            {fieldErrors.q ? (
              <span className="mt-1.5 block text-xs text-destructive">
                {fieldErrors.q}
              </span>
            ) : null}
          </label>
          <label className="block">
            <span className="mb-2 block font-head text-sm font-bold">
              사용여부
            </span>
            <select
              className="h-12 w-full rounded border-2 border-black bg-white px-3 shadow-hard"
              value={enabledFilter}
              onChange={(event) => setEnabledFilter(event.target.value)}
            >
              <option value="">전체</option>
              <option value="true">사용</option>
              <option value="false">미사용</option>
            </select>
          </label>
          <label className="block">
            <span className="mb-2 block font-head text-sm font-bold">
              표시 건수
            </span>
            <select
              className="h-12 w-full rounded border-2 border-black bg-white px-3 shadow-hard"
              value={size}
              onChange={(event) => setSize(Number(event.target.value))}
            >
              {sizeOptions.map((option) => (
                <option key={option} value={option}>
                  {option}건
                </option>
              ))}
            </select>
          </label>
          <div className="flex items-end gap-2">
            <button
              className="h-12 flex-1 rounded border-2 border-black bg-primary px-4 font-head font-black shadow-hard transition-all duration-200 hover:-translate-x-0.5 hover:-translate-y-0.5 hover:bg-primary-hover hover:shadow-hard-lg active:translate-x-1 active:translate-y-1 active:shadow-none"
              type="submit"
            >
              검색
            </button>
            <button
              className="h-12 rounded border-2 border-black bg-card px-4 font-head font-black shadow-hard"
              type="button"
              onClick={() => {
                setQuery("");
                setEnabledFilter("");
                void loadUsers("", "", size);
              }}
            >
              초기화
            </button>
          </div>
        </form>

        {message ? (
          <div
            role="status"
            className={`border-2 border-black px-4 py-3 font-bold shadow-hard ${state === "permission" || state === "error" ? "bg-destructive text-white" : "bg-accent text-black"}`}
          >
            {message}
          </div>
        ) : null}
        {state === "loading" ? (
          <StateCard title="LOADING" body="사용자 목록을 불러오는 중입니다." />
        ) : null}
        {state === "empty" ? (
          <StateCard
            title="EMPTY"
            body="조건에 맞는 사용자가 없습니다. 검색 조건을 초기화해 보세요."
          />
        ) : null}
        {state === "permission" ? (
          <StateCard
            title="PERMISSION DENIED"
            body="R09 시스템관리자 권한이 필요합니다. 직접 API 요청도 서버에서 403으로 차단됩니다."
          />
        ) : null}

        {state === "success" || users.length > 0 ? (
          <section className="grid grid-cols-1 gap-4 lg:grid-cols-[minmax(0,1fr)_22rem]">
            <div className="overflow-hidden border-2 border-black bg-card shadow-hard">
              <div className="border-b-2 border-black bg-muted px-4 py-3 font-head font-black">
                user_accounts 목록
              </div>
              <div className="overflow-x-auto">
                <table className="w-full min-w-[58rem] border-collapse text-left text-sm">
                  <thead className="bg-primary font-head text-xs uppercase">
                    <tr>
                      {[
                        "교번",
                        "성명",
                        "소속",
                        "직급",
                        "재직상태",
                        "역할",
                        "사용여부",
                        "보직",
                        "퇴직일자",
                        "동기화",
                      ].map((header) => (
                        <th
                          key={header}
                          className="border-b-2 border-black px-3 py-2"
                        >
                          {header}
                        </th>
                      ))}
                    </tr>
                  </thead>
                  <tbody>
                    {users.map((item) => (
                      <tr
                        key={item.userId}
                        className={`cursor-pointer border-b-2 border-black transition-colors hover:bg-accent ${selectedUserId === item.userId ? "bg-accent" : "bg-card"}`}
                        onClick={() => setSelectedUserId(item.userId)}
                      >
                        <td className="px-3 py-2 font-mono">
                          {item.employeeNo ?? item.userId}
                        </td>
                        <td className="px-3 py-2 font-bold">
                          {item.name ?? "-"}
                        </td>
                        <td className="px-3 py-2">
                          {item.departmentName ?? item.departmentCode ?? "-"}
                        </td>
                        <td className="px-3 py-2">{item.rankName ?? "-"}</td>
                        <td className="px-3 py-2">
                          {item.employmentStatus ?? "-"}
                        </td>
                        <td className="px-3 py-2">{item.roleSummary ?? "-"}</td>
                        <td className="px-3 py-2">
                          <span className="border-2 border-black bg-primary px-2 py-1 font-head text-xs font-black">
                            {item.enabled ? "사용" : "미사용"}
                          </span>
                        </td>
                        <td className="px-3 py-2">
                          {item.positionSummary ?? "-"}
                        </td>
                        <td className="px-3 py-2">
                          {item.retirementDate ?? "-"}
                        </td>
                        <td className="px-3 py-2">
                          {item.lastSyncedAt
                            ? new Date(item.lastSyncedAt).toLocaleString(
                                "ko-KR",
                              )
                            : "-"}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>

            <aside className="relative">
              <div className="absolute inset-2 border-2 border-black bg-primary" />
              <div className="relative border-2 border-black bg-card shadow-hard">
                <div className="border-b-2 border-black bg-muted px-4 py-3 font-head font-black">
                  상세 / 저장 전 확인
                </div>
                {selectedUser ? (
                  <div className="space-y-3 p-4 text-sm">
                    <ReadonlyField
                      label="사용자 ID"
                      value={selectedUser.userId}
                    />
                    <ReadonlyField
                      label="KORUS 교번"
                      value={selectedUser.employeeNo ?? "-"}
                    />
                    <ReadonlyField
                      label="성명"
                      value={selectedUser.name ?? "-"}
                    />
                    <ReadonlyField
                      label="소속"
                      value={selectedUser.departmentName ?? "-"}
                    />
                    <ReadonlyField
                      label="직급"
                      value={selectedUser.rankName ?? "-"}
                    />
                    <ReadonlyField
                      label="역할"
                      value={selectedUser.roleSummary ?? "-"}
                    />
                    <ReadonlyField
                      label="사용여부"
                      value={selectedUser.enabled ? "사용" : "미사용"}
                    />
                    <p className="border-2 border-black bg-muted p-3 text-xs leading-relaxed">
                      KORUS 원천 인사정보는 읽기 전용입니다. 저장 CTA는 로컬
                      DB의 enabled/status 및 관리 필드만 변경합니다.
                    </p>
                    {fieldErrors.id ? (
                      <p className="text-xs font-bold text-destructive">
                        {fieldErrors.id}
                      </p>
                    ) : null}
                    <button
                      className="h-12 w-full rounded border-2 border-black bg-primary px-4 font-head font-black shadow-hard transition-all duration-200 hover:-translate-x-0.5 hover:-translate-y-0.5 hover:bg-primary-hover hover:shadow-hard-lg active:translate-x-1 active:translate-y-1 active:shadow-none disabled:opacity-60"
                      type="button"
                      onClick={() => void saveSelectedUser()}
                      disabled={saving || !user?.roles.includes("R09")}
                    >
                      {saving ? "저장 중..." : "저장/실행 전 확인"}
                    </button>
                  </div>
                ) : (
                  <div className="p-4 text-sm">
                    행을 선택하면 상세 모달 영역이 표시됩니다.
                  </div>
                )}
              </div>
            </aside>
          </section>
        ) : null}
      </section>
    </AppShell>
  );
}

function OrganizationManagementPage() {
  const [user, setUser] = useState<AuthenticatedUser | null>(null);
  const [organizations, setOrganizations] = useState<OrganizationListItem[]>(
    [],
  );
  const [selectedOrganizationCode, setSelectedOrganizationCode] =
    useState<string>("");
  const [query, setQuery] = useState("");
  const [enabledFilter, setEnabledFilter] = useState("");
  const [size, setSize] = useState(20);
  const [state, setState] = useState<ViewState>("loading");
  const [message, setMessage] = useState("");
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});
  const [saving, setSaving] = useState(false);

  const selectedOrganization = useMemo(
    () =>
      organizations.find(
        (item) => item.organizationCode === selectedOrganizationCode,
      ) ?? null,
    [selectedOrganizationCode, organizations],
  );

  useEffect(() => {
    requestJson<AuthenticatedUser>("/api/auth/session")
      .then((sessionUser) => {
        setUser(sessionUser);
        return loadOrganizations("", "", size);
      })
      .catch((caught) => {
        const error = caught as Error & { status?: number };
        setState(error.status === 403 ? "permission" : "error");
        setMessage(error.message);
      });
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  async function loadOrganizations(
    nextQuery = query,
    nextEnabled = enabledFilter,
    nextSize = size,
  ) {
    setState("loading");
    setMessage("");
    const filterParts = nextEnabled ? [`enabled=${nextEnabled}`] : [];
    const params = new URLSearchParams({
      page: "1",
      size: String(nextSize),
      sort: "organizationCode",
    });
    if (nextQuery.trim()) {
      params.set("q", nextQuery.trim());
    }
    if (filterParts.length > 0) {
      params.set("filter", filterParts.join(";"));
    }
    try {
      const body = await requestJson<OrganizationListResponse>(
        `/api/admin/organizations?${params.toString()}`,
      );
      setOrganizations(body.items);
      setSelectedOrganizationCode(body.items[0]?.organizationCode ?? "");
      setState(body.items.length === 0 ? "empty" : "success");
      setMessage(`총 ${body.totalCount}건 조회되었습니다.`);
    } catch (caught) {
      const error = caught as Error & { apiError?: ApiError; status?: number };
      setFieldErrors(error.apiError?.fields ?? {});
      setState(error.status === 403 ? "permission" : "error");
      setMessage(error.message);
    }
  }

  async function saveSelectedOrganization() {
    if (!selectedOrganization) {
      setMessage("저장할 조직을 먼저 선택하세요.");
      return;
    }
    const confirmed = window.confirm(
      `${selectedOrganization.organizationCode} 조직 사용여부를 변경하시겠습니까?`,
    );
    if (!confirmed) {
      return;
    }
    setSaving(true);
    setFieldErrors({});
    try {
      const result = await requestJson<{
        organizationCode: string;
        enabled: boolean;
        validTo?: string;
        message: string;
      }>("/api/admin/organizations", {
        method: "POST",
        body: JSON.stringify({
          id: selectedOrganization.organizationCode,
          enabled: !selectedOrganization.enabled,
          validTo: selectedOrganization.enabled ? "2026-12-31" : null,
          reason: "조직 관리 화면 저장 CTA",
        }),
      });
      setMessage(result.message);
      await loadOrganizations();
      setSelectedOrganizationCode(result.organizationCode);
    } catch (caught) {
      const error = caught as Error & { apiError?: ApiError; status?: number };
      setFieldErrors(error.apiError?.fields ?? {});
      setState(error.status === 403 ? "permission" : "error");
      setMessage(error.message);
    } finally {
      setSaving(false);
    }
  }

  if (!user && state === "error") {
    return (
      <AppShell>
        <LoginPanel
          onLogin={(nextUser) => {
            setUser(nextUser);
            void loadOrganizations();
          }}
        />
      </AppShell>
    );
  }

  return (
    <AppShell>
      <section className="space-y-7 sm:space-y-8 lg:space-y-9">
        <div className="flex flex-col gap-4 lg:flex-row lg:items-end lg:justify-between">
          <div>
            <div className="mb-3 inline-flex border-2 border-black bg-primary px-2.5 py-1 font-head text-xs font-black uppercase tracking-[0.14em] shadow-hard">
              SCR-ORG-MGMT
            </div>
            <h1 className="font-head text-4xl font-black uppercase leading-none tracking-tight sm:text-5xl">
              조직 관리
            </h1>
            <p className="mt-4 max-w-3xl text-sm leading-relaxed text-muted-foreground sm:text-base">
              대학·대학원·단과대학·학과·부서의 상하위 조직을 조직코드 기준으로
              조회하고, 조직 개편 적용기간과 사용여부를 관리합니다.
            </p>
          </div>
          <div className="rounded border-2 border-black bg-card px-4 py-3 font-head text-sm font-black shadow-hard">
            {user
              ? `${user.userId} / ${user.roles.join(", ")}`
              : "세션 확인 중"}
          </div>
        </div>

        <form
          className="grid grid-cols-1 gap-4 border-2 border-black bg-card p-4 shadow-hard sm:grid-cols-[minmax(0,1fr)_10rem_9rem_9rem] sm:p-5"
          onSubmit={(event) => {
            event.preventDefault();
            void loadOrganizations();
          }}
        >
          <label className="block">
            <span className="mb-2 block font-head text-sm font-bold">
              통합 검색
            </span>
            <input
              className="h-12 w-full rounded border-2 border-black bg-white px-4 py-2 shadow-hard"
              placeholder="조직코드, 조직명, 상위조직"
              value={query}
              onChange={(event) => setQuery(event.target.value)}
            />
            {fieldErrors.q ? (
              <span className="mt-1.5 block text-xs text-destructive">
                {fieldErrors.q}
              </span>
            ) : null}
          </label>
          <label className="block">
            <span className="mb-2 block font-head text-sm font-bold">
              사용여부
            </span>
            <select
              className="h-12 w-full rounded border-2 border-black bg-white px-3 shadow-hard"
              value={enabledFilter}
              onChange={(event) => setEnabledFilter(event.target.value)}
            >
              <option value="">전체</option>
              <option value="true">사용</option>
              <option value="false">미사용</option>
            </select>
          </label>
          <label className="block">
            <span className="mb-2 block font-head text-sm font-bold">
              표시 건수
            </span>
            <select
              className="h-12 w-full rounded border-2 border-black bg-white px-3 shadow-hard"
              value={size}
              onChange={(event) => setSize(Number(event.target.value))}
            >
              {sizeOptions.map((option) => (
                <option key={option} value={option}>
                  {option}건
                </option>
              ))}
            </select>
          </label>
          <div className="flex items-end gap-2">
            <button
              className="h-12 flex-1 rounded border-2 border-black bg-primary px-4 font-head font-black shadow-hard transition-all duration-200 hover:-translate-x-0.5 hover:-translate-y-0.5 hover:bg-primary-hover hover:shadow-hard-lg active:translate-x-1 active:translate-y-1 active:shadow-none"
              type="submit"
            >
              검색
            </button>
            <button
              className="h-12 rounded border-2 border-black bg-card px-4 font-head font-black shadow-hard"
              type="button"
              onClick={() => {
                setQuery("");
                setEnabledFilter("");
                void loadOrganizations("", "", size);
              }}
            >
              초기화
            </button>
          </div>
        </form>

        {message ? (
          <div
            role="status"
            className={`border-2 border-black px-4 py-3 font-bold shadow-hard ${state === "permission" || state === "error" ? "bg-destructive text-white" : "bg-accent text-black"}`}
          >
            {message}
          </div>
        ) : null}
        {state === "loading" ? (
          <StateCard title="LOADING" body="조직 목록을 불러오는 중입니다." />
        ) : null}
        {state === "empty" ? (
          <StateCard
            title="EMPTY"
            body="조건에 맞는 조직이 없습니다. 검색 조건을 초기화해 보세요."
          />
        ) : null}
        {state === "permission" ? (
          <StateCard
            title="PERMISSION DENIED"
            body="R09 시스템관리자 권한이 필요합니다. 직접 API 요청도 서버에서 403으로 차단됩니다."
          />
        ) : null}

        {state === "success" || organizations.length > 0 ? (
          <section className="grid grid-cols-1 gap-4 lg:grid-cols-[minmax(0,1fr)_22rem]">
            <div className="overflow-hidden border-2 border-black bg-card shadow-hard">
              <div className="border-b-2 border-black bg-muted px-4 py-3 font-head font-black">
                organizations 목록
              </div>
              <div className="overflow-x-auto">
                <table className="w-full min-w-[54rem] border-collapse text-left text-sm">
                  <thead className="bg-primary font-head text-xs uppercase">
                    <tr>
                      {[
                        "조직코드",
                        "조직명",
                        "상위조직",
                        "적용시작일",
                        "적용종료일",
                        "사용여부",
                        "하위조직",
                        "배정사용자",
                      ].map((header) => (
                        <th
                          key={header}
                          className="border-b-2 border-black px-3 py-2"
                        >
                          {header}
                        </th>
                      ))}
                    </tr>
                  </thead>
                  <tbody>
                    {organizations.map((item) => (
                      <tr
                        key={item.organizationCode}
                        className={`cursor-pointer border-b-2 border-black transition-colors hover:bg-accent ${selectedOrganizationCode === item.organizationCode ? "bg-accent" : "bg-card"}`}
                        onClick={() =>
                          setSelectedOrganizationCode(item.organizationCode)
                        }
                      >
                        <td className="px-3 py-2 font-mono">
                          {item.organizationCode}
                        </td>
                        <td className="px-3 py-2 font-bold">
                          {item.organizationName}
                        </td>
                        <td className="px-3 py-2">
                          {item.parentOrganizationName ??
                            item.parentOrganizationCode ??
                            "-"}
                        </td>
                        <td className="px-3 py-2">{item.validFrom}</td>
                        <td className="px-3 py-2">{item.validTo ?? "-"}</td>
                        <td className="px-3 py-2">
                          <span className="border-2 border-black bg-primary px-2 py-1 font-head text-xs font-black">
                            {item.enabled ? "사용" : "미사용"}
                          </span>
                        </td>
                        <td className="px-3 py-2">{item.childCount}</td>
                        <td className="px-3 py-2">{item.assignedUserCount}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>

            <aside className="relative">
              <div className="absolute inset-2 border-2 border-black bg-primary" />
              <div className="relative border-2 border-black bg-card shadow-hard">
                <div className="border-b-2 border-black bg-muted px-4 py-3 font-head font-black">
                  상세 / 저장 전 확인
                </div>
                {selectedOrganization ? (
                  <div className="space-y-3 p-4 text-sm">
                    <ReadonlyField
                      label="조직코드"
                      value={selectedOrganization.organizationCode}
                    />
                    <ReadonlyField
                      label="조직명"
                      value={selectedOrganization.organizationName}
                    />
                    <ReadonlyField
                      label="상위조직"
                      value={
                        selectedOrganization.parentOrganizationName ??
                        selectedOrganization.parentOrganizationCode ??
                        "-"
                      }
                    />
                    <ReadonlyField
                      label="적용기간"
                      value={`${selectedOrganization.validFrom} ~ ${selectedOrganization.validTo ?? "현재"}`}
                    />
                    <ReadonlyField
                      label="사용여부"
                      value={selectedOrganization.enabled ? "사용" : "미사용"}
                    />
                    <p className="border-2 border-black bg-muted p-3 text-xs leading-relaxed">
                      조직코드와 KORUS 조직 원천명은 읽기 전용입니다. 저장 CTA는
                      로컬 DB의 enabled/valid_to 관리 필드와 감사로그만
                      변경합니다.
                    </p>
                    {fieldErrors.id ? (
                      <p className="text-xs font-bold text-destructive">
                        {fieldErrors.id}
                      </p>
                    ) : null}
                    <button
                      className="h-12 w-full rounded border-2 border-black bg-primary px-4 font-head font-black shadow-hard transition-all duration-200 hover:-translate-x-0.5 hover:-translate-y-0.5 hover:bg-primary-hover hover:shadow-hard-lg active:translate-x-1 active:translate-y-1 active:shadow-none disabled:opacity-60"
                      type="button"
                      onClick={() => void saveSelectedOrganization()}
                      disabled={saving || !user?.roles.includes("R09")}
                    >
                      {saving ? "저장 중..." : "저장/실행 전 확인"}
                    </button>
                  </div>
                ) : (
                  <div className="p-4 text-sm">
                    행을 선택하면 상세 모달 영역이 표시됩니다.
                  </div>
                )}
              </div>
            </aside>
          </section>
        ) : null}
      </section>
    </AppShell>
  );
}

function PositionManagementPage() {
  const [user, setUser] = useState<AuthenticatedUser | null>(null);
  const [positions, setPositions] = useState<PositionListItem[]>([]);
  const [selectedPositionId, setSelectedPositionId] = useState<number | null>(
    null,
  );
  const [query, setQuery] = useState("");
  const [activeFilter, setActiveFilter] = useState("");
  const [size, setSize] = useState(20);
  const [state, setState] = useState<ViewState>("loading");
  const [message, setMessage] = useState("");
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});
  const [saving, setSaving] = useState(false);

  const selectedPosition = useMemo(
    () =>
      positions.find((item) => item.positionId === selectedPositionId) ?? null,
    [selectedPositionId, positions],
  );

  useEffect(() => {
    requestJson<AuthenticatedUser>("/api/auth/session")
      .then((sessionUser) => {
        setUser(sessionUser);
        return loadPositions("", "", size);
      })
      .catch((caught) => {
        const error = caught as Error & { status?: number };
        setState(error.status === 403 ? "permission" : "error");
        setMessage(error.message);
      });
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  async function loadPositions(
    nextQuery = query,
    nextActive = activeFilter,
    nextSize = size,
  ) {
    setState("loading");
    setMessage("");
    const filterParts = nextActive ? [`active=${nextActive}`] : [];
    const params = new URLSearchParams({
      page: "1",
      size: String(nextSize),
      sort: "positionCode",
    });
    if (nextQuery.trim()) {
      params.set("q", nextQuery.trim());
    }
    if (filterParts.length > 0) {
      params.set("filter", filterParts.join(";"));
    }
    try {
      const body = await requestJson<PositionListResponse>(
        `/api/admin/positions?${params.toString()}`,
      );
      setPositions(body.items);
      setSelectedPositionId(body.items[0]?.positionId ?? null);
      setState(body.items.length === 0 ? "empty" : "success");
      setMessage(`총 ${body.totalCount}건 조회되었습니다.`);
    } catch (caught) {
      const error = caught as Error & { apiError?: ApiError; status?: number };
      setFieldErrors(error.apiError?.fields ?? {});
      setState(error.status === 403 ? "permission" : "error");
      setMessage(error.message);
    }
  }

  async function saveSelectedPosition() {
    if (!selectedPosition) {
      setMessage("저장할 보직 배정을 먼저 선택하세요.");
      return;
    }
    const confirmed = window.confirm(
      `${selectedPosition.positionName} 보직 배정 기간을 변경하시겠습니까?`,
    );
    if (!confirmed) {
      return;
    }
    setSaving(true);
    setFieldErrors({});
    try {
      const result = await requestJson<{
        positionId: number;
        active: boolean;
        validTo?: string;
        message: string;
      }>("/api/admin/positions", {
        method: "POST",
        body: JSON.stringify({
          id: String(selectedPosition.positionId),
          active: !selectedPosition.active,
          validTo: selectedPosition.active ? "2026-12-31" : null,
          reason: "보직 관리 화면 저장 CTA",
        }),
      });
      setMessage(result.message);
      await loadPositions();
      setSelectedPositionId(result.positionId);
    } catch (caught) {
      const error = caught as Error & { apiError?: ApiError; status?: number };
      setFieldErrors(error.apiError?.fields ?? {});
      setState(error.status === 403 ? "permission" : "error");
      setMessage(error.message);
    } finally {
      setSaving(false);
    }
  }

  if (!user && state === "error") {
    return (
      <AppShell>
        <LoginPanel
          onLogin={(nextUser) => {
            setUser(nextUser);
            void loadPositions();
          }}
        />
      </AppShell>
    );
  }

  return (
    <AppShell>
      <section className="space-y-7 sm:space-y-8 lg:space-y-9">
        <div className="flex flex-col gap-4 lg:flex-row lg:items-end lg:justify-between">
          <div>
            <div className="mb-3 inline-flex border-2 border-black bg-primary px-2.5 py-1 font-head text-xs font-black uppercase tracking-[0.14em] shadow-hard">
              SCR-POSITION-MGMT
            </div>
            <h1 className="font-head text-4xl font-black uppercase leading-none tracking-tight sm:text-5xl">
              보직 관리
            </h1>
            <p className="mt-4 max-w-3xl text-sm leading-relaxed text-muted-foreground sm:text-base">
              학과장 등 보직정보를 보직코드·대상 사용자·소속조직·유효기간으로
              조회하고, KORUS 원천 인사·조직 정보는 읽기 전용으로 표시합니다.
            </p>
          </div>
          <div className="rounded border-2 border-black bg-card px-4 py-3 font-head text-sm font-black shadow-hard">
            {user
              ? `${user.userId} / ${user.roles.join(", ")}`
              : "세션 확인 중"}
          </div>
        </div>

        <form
          className="grid grid-cols-1 gap-4 border-2 border-black bg-card p-4 shadow-hard sm:grid-cols-[minmax(0,1fr)_10rem_9rem_9rem] sm:p-5"
          onSubmit={(event) => {
            event.preventDefault();
            void loadPositions();
          }}
        >
          <label className="block">
            <span className="mb-2 block font-head text-sm font-bold">
              통합 검색
            </span>
            <input
              className="h-12 w-full rounded border-2 border-black bg-white px-4 py-2 shadow-hard"
              placeholder="보직코드, 사용자, 조직"
              value={query}
              onChange={(event) => setQuery(event.target.value)}
            />
            {fieldErrors.q ? (
              <span className="mt-1.5 block text-xs text-destructive">
                {fieldErrors.q}
              </span>
            ) : null}
          </label>
          <label className="block">
            <span className="mb-2 block font-head text-sm font-bold">상태</span>
            <select
              className="h-12 w-full rounded border-2 border-black bg-white px-3 shadow-hard"
              value={activeFilter}
              onChange={(event) => setActiveFilter(event.target.value)}
            >
              <option value="">전체</option>
              <option value="true">유효</option>
              <option value="false">종료</option>
            </select>
          </label>
          <label className="block">
            <span className="mb-2 block font-head text-sm font-bold">
              표시 건수
            </span>
            <select
              className="h-12 w-full rounded border-2 border-black bg-white px-3 shadow-hard"
              value={size}
              onChange={(event) => setSize(Number(event.target.value))}
            >
              {sizeOptions.map((option) => (
                <option key={option} value={option}>
                  {option}건
                </option>
              ))}
            </select>
          </label>
          <div className="flex items-end gap-2">
            <button
              className="h-12 flex-1 rounded border-2 border-black bg-primary px-4 font-head font-black shadow-hard transition-all duration-200 hover:-translate-x-0.5 hover:-translate-y-0.5 hover:bg-primary-hover hover:shadow-hard-lg active:translate-x-1 active:translate-y-1 active:shadow-none"
              type="submit"
            >
              검색
            </button>
            <button
              className="h-12 rounded border-2 border-black bg-card px-4 font-head font-black shadow-hard"
              type="button"
              onClick={() => {
                setQuery("");
                setActiveFilter("");
                void loadPositions("", "", size);
              }}
            >
              초기화
            </button>
          </div>
        </form>

        {message ? (
          <div
            role="status"
            className={`border-2 border-black px-4 py-3 font-bold shadow-hard ${state === "permission" || state === "error" ? "bg-destructive text-white" : "bg-accent text-black"}`}
          >
            {message}
          </div>
        ) : null}
        {state === "loading" ? (
          <StateCard
            title="LOADING"
            body="보직 배정 목록을 불러오는 중입니다."
          />
        ) : null}
        {state === "empty" ? (
          <StateCard
            title="EMPTY"
            body="조건에 맞는 보직 배정이 없습니다. 검색 조건을 초기화해 보세요."
          />
        ) : null}
        {state === "permission" ? (
          <StateCard
            title="PERMISSION DENIED"
            body="R09 시스템관리자 권한이 필요합니다. 직접 API 요청도 서버에서 403으로 차단됩니다."
          />
        ) : null}

        {state === "success" || positions.length > 0 ? (
          <section className="grid grid-cols-1 gap-4 lg:grid-cols-[minmax(0,1fr)_22rem]">
            <div className="overflow-hidden border-2 border-black bg-card shadow-hard">
              <div className="border-b-2 border-black bg-muted px-4 py-3 font-head font-black">
                position_assignments 목록
              </div>
              <div className="overflow-x-auto">
                <table className="w-full min-w-[58rem] border-collapse text-left text-sm">
                  <thead className="bg-primary font-head text-xs uppercase">
                    <tr>
                      {[
                        "ID",
                        "보직",
                        "사용자",
                        "교번",
                        "조직",
                        "적용시작일",
                        "적용종료일",
                        "상태",
                      ].map((header) => (
                        <th
                          key={header}
                          className="border-b-2 border-black px-3 py-2"
                        >
                          {header}
                        </th>
                      ))}
                    </tr>
                  </thead>
                  <tbody>
                    {positions.map((item) => (
                      <tr
                        key={item.positionId}
                        className={`cursor-pointer border-b-2 border-black transition-colors hover:bg-accent ${selectedPositionId === item.positionId ? "bg-accent" : "bg-card"}`}
                        onClick={() => setSelectedPositionId(item.positionId)}
                      >
                        <td className="px-3 py-2 font-mono">
                          {item.positionId}
                        </td>
                        <td className="px-3 py-2 font-bold">
                          {item.positionName}
                        </td>
                        <td className="px-3 py-2">
                          {item.userName ?? item.userId}
                        </td>
                        <td className="px-3 py-2">{item.employeeNo ?? "-"}</td>
                        <td className="px-3 py-2">
                          {item.organizationName ?? item.organizationCode}
                        </td>
                        <td className="px-3 py-2">{item.validFrom}</td>
                        <td className="px-3 py-2">{item.validTo ?? "현재"}</td>
                        <td className="px-3 py-2">
                          <span className="border-2 border-black bg-primary px-2 py-1 font-head text-xs font-black">
                            {item.active ? "유효" : "종료"}
                          </span>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>

            <aside className="relative">
              <div className="absolute inset-2 border-2 border-black bg-primary" />
              <div className="relative border-2 border-black bg-card shadow-hard">
                <div className="border-b-2 border-black bg-muted px-4 py-3 font-head font-black">
                  상세 / 저장 전 확인
                </div>
                {selectedPosition ? (
                  <div className="space-y-3 p-4 text-sm">
                    <ReadonlyField
                      label="보직 배정 ID"
                      value={String(selectedPosition.positionId)}
                    />
                    <ReadonlyField
                      label="보직"
                      value={`${selectedPosition.positionName} (${selectedPosition.positionCode})`}
                    />
                    <ReadonlyField
                      label="대상 사용자"
                      value={`${selectedPosition.userName ?? selectedPosition.userId} / ${selectedPosition.employeeNo ?? selectedPosition.userId}`}
                    />
                    <ReadonlyField
                      label="소속조직"
                      value={
                        selectedPosition.organizationName ??
                        selectedPosition.organizationCode
                      }
                    />
                    <ReadonlyField
                      label="유효기간"
                      value={`${selectedPosition.validFrom} ~ ${selectedPosition.validTo ?? "현재"}`}
                    />
                    <ReadonlyField
                      label="상태"
                      value={selectedPosition.active ? "유효" : "종료"}
                    />
                    <p className="border-2 border-black bg-muted p-3 text-xs leading-relaxed">
                      보직코드·사용자·조직 원천 필드는 읽기 전용입니다. 저장
                      CTA는 로컬 DB의 valid_to 기간 관리 필드와 감사로그만
                      변경합니다.
                    </p>
                    {fieldErrors.id ? (
                      <p className="text-xs font-bold text-destructive">
                        {fieldErrors.id}
                      </p>
                    ) : null}
                    <button
                      className="h-12 w-full rounded border-2 border-black bg-primary px-4 font-head font-black shadow-hard transition-all duration-200 hover:-translate-x-0.5 hover:-translate-y-0.5 hover:bg-primary-hover hover:shadow-hard-lg active:translate-x-1 active:translate-y-1 active:shadow-none disabled:opacity-60"
                      type="button"
                      onClick={() => void saveSelectedPosition()}
                      disabled={saving || !user?.roles.includes("R09")}
                    >
                      {saving ? "저장 중..." : "저장/실행 전 확인"}
                    </button>
                  </div>
                ) : (
                  <div className="p-4 text-sm">
                    행을 선택하면 상세 모달 영역이 표시됩니다.
                  </div>
                )}
              </div>
            </aside>
          </section>
        ) : null}
      </section>
    </AppShell>
  );
}

function RoleManagementPage() {
  const [user, setUser] = useState<AuthenticatedUser | null>(null);
  const [roles, setRoles] = useState<RoleListItem[]>([]);
  const [selectedRoleCode, setSelectedRoleCode] = useState<string>("");
  const [query, setQuery] = useState("");
  const [enabledFilter, setEnabledFilter] = useState("");
  const [scopeFilter, setScopeFilter] = useState("");
  const [size, setSize] = useState(20);
  const [state, setState] = useState<ViewState>("loading");
  const [message, setMessage] = useState("");
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});
  const [saving, setSaving] = useState(false);

  const selectedRole = useMemo(
    () => roles.find((item) => item.roleCode === selectedRoleCode) ?? null,
    [selectedRoleCode, roles],
  );

  useEffect(() => {
    requestJson<AuthenticatedUser>("/api/auth/session")
      .then((sessionUser) => {
        setUser(sessionUser);
        return loadRoles("", "", "", size);
      })
      .catch((caught) => {
        const error = caught as Error & { status?: number };
        setState(error.status === 403 ? "permission" : "error");
        setMessage(error.message);
      });
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  async function loadRoles(
    nextQuery = query,
    nextEnabled = enabledFilter,
    nextScope = scopeFilter,
    nextSize = size,
  ) {
    setState("loading");
    setMessage("");
    const filterParts: string[] = [];
    if (nextEnabled) {
      filterParts.push(`enabled=${nextEnabled}`);
    }
    if (nextScope) {
      filterParts.push(`defaultDataScope=${nextScope}`);
    }
    const params = new URLSearchParams({
      page: "1",
      size: String(nextSize),
      sort: "roleCode",
    });
    if (nextQuery.trim()) {
      params.set("q", nextQuery.trim());
    }
    if (filterParts.length > 0) {
      params.set("filter", filterParts.join(";"));
    }
    try {
      const body = await requestJson<RoleListResponse>(
        `/api/admin/roles?${params.toString()}`,
      );
      setRoles(body.items);
      setSelectedRoleCode(body.items[0]?.roleCode ?? "");
      setState(body.items.length === 0 ? "empty" : "success");
      setMessage(`총 ${body.totalCount}건 조회되었습니다.`);
    } catch (caught) {
      const error = caught as Error & { apiError?: ApiError; status?: number };
      setFieldErrors(error.apiError?.fields ?? {});
      setState(error.status === 403 ? "permission" : "error");
      setMessage(error.message);
    }
  }

  async function saveSelectedRole() {
    if (!selectedRole) {
      setMessage("저장할 역할을 먼저 선택하세요.");
      return;
    }
    const confirmed = window.confirm(
      `${selectedRole.roleName} 역할 사용여부를 변경하시겠습니까?`,
    );
    if (!confirmed) {
      return;
    }
    setSaving(true);
    setFieldErrors({});
    try {
      const result = await requestJson<{
        roleCode: string;
        enabled: boolean;
        defaultDataScope: string;
        message: string;
      }>("/api/admin/roles", {
        method: "POST",
        body: JSON.stringify({
          id: selectedRole.roleCode,
          enabled: !selectedRole.enabled,
          defaultDataScope: selectedRole.defaultDataScope,
          purpose: selectedRole.purpose ?? "",
          grantCriteria: selectedRole.grantCriteria ?? "",
          reason: "역할 관리 화면 저장 CTA",
        }),
      });
      setMessage(result.message);
      await loadRoles();
      setSelectedRoleCode(result.roleCode);
    } catch (caught) {
      const error = caught as Error & { apiError?: ApiError; status?: number };
      setFieldErrors(error.apiError?.fields ?? {});
      setState(error.status === 403 ? "permission" : "error");
      setMessage(error.message);
    } finally {
      setSaving(false);
    }
  }

  if (!user && state === "error") {
    return (
      <AppShell>
        <LoginPanel
          onLogin={(nextUser) => {
            setUser(nextUser);
            void loadRoles();
          }}
        />
      </AppShell>
    );
  }

  return (
    <AppShell>
      <section className="space-y-7 sm:space-y-8 lg:space-y-9">
        <div className="flex flex-col gap-4 lg:flex-row lg:items-end lg:justify-between">
          <div>
            <div className="mb-3 inline-flex border-2 border-black bg-primary px-2.5 py-1 font-head text-xs font-black uppercase tracking-[0.14em] shadow-hard">
              SCR-ROLE-MGMT
            </div>
            <h1 className="font-head text-4xl font-black uppercase leading-none tracking-tight sm:text-5xl">
              역할 관리
            </h1>
            <p className="mt-4 max-w-3xl text-sm leading-relaxed text-muted-foreground sm:text-base">
              R01~R09 역할을 정의하고 역할별 목적, 부여 기준, 기본 데이터 범위,
              사용여부를 조회·관리합니다. 역할 코드는 생명주기 식별자로 읽기
              전용입니다.
            </p>
          </div>
          <div className="rounded border-2 border-black bg-card px-4 py-3 font-head text-sm font-black shadow-hard">
            {user
              ? `${user.userId} / ${user.roles.join(", ")}`
              : "세션 확인 중"}
          </div>
        </div>

        <form
          className="grid grid-cols-1 gap-4 border-2 border-black bg-card p-4 shadow-hard sm:grid-cols-[minmax(0,1fr)_9rem_11rem_9rem_9rem] sm:p-5"
          onSubmit={(event) => {
            event.preventDefault();
            void loadRoles();
          }}
        >
          <label className="block">
            <span className="mb-2 block font-head text-sm font-bold">
              통합 검색
            </span>
            <input
              className="h-12 w-full rounded border-2 border-black bg-white px-4 py-2 shadow-hard"
              placeholder="역할코드, 역할명, 목적"
              value={query}
              onChange={(event) => setQuery(event.target.value)}
            />
            {fieldErrors.q ? (
              <span className="mt-1.5 block text-xs text-destructive">
                {fieldErrors.q}
              </span>
            ) : null}
          </label>
          <label className="block">
            <span className="mb-2 block font-head text-sm font-bold">
              사용여부
            </span>
            <select
              className="h-12 w-full rounded border-2 border-black bg-white px-3 shadow-hard"
              value={enabledFilter}
              onChange={(event) => setEnabledFilter(event.target.value)}
            >
              <option value="">전체</option>
              <option value="true">사용</option>
              <option value="false">미사용</option>
            </select>
          </label>
          <label className="block">
            <span className="mb-2 block font-head text-sm font-bold">
              기본 데이터 범위
            </span>
            <select
              className="h-12 w-full rounded border-2 border-black bg-white px-3 shadow-hard"
              value={scopeFilter}
              onChange={(event) => setScopeFilter(event.target.value)}
            >
              <option value="">전체</option>
              <option value="SELF">본인</option>
              <option value="DEPARTMENT">소속학과</option>
              <option value="COLLEGE">단과대학</option>
              <option value="BUSINESS">담당업무</option>
              <option value="ALL">전체</option>
            </select>
          </label>
          <label className="block">
            <span className="mb-2 block font-head text-sm font-bold">
              표시 건수
            </span>
            <select
              className="h-12 w-full rounded border-2 border-black bg-white px-3 shadow-hard"
              value={size}
              onChange={(event) => setSize(Number(event.target.value))}
            >
              {sizeOptions.map((option) => (
                <option key={option} value={option}>
                  {option}건
                </option>
              ))}
            </select>
          </label>
          <div className="flex items-end gap-2">
            <button
              className="h-12 flex-1 rounded border-2 border-black bg-primary px-4 font-head font-black shadow-hard transition-all duration-200 hover:-translate-x-0.5 hover:-translate-y-0.5 hover:bg-primary-hover hover:shadow-hard-lg active:translate-x-1 active:translate-y-1 active:shadow-none"
              type="submit"
            >
              검색
            </button>
            <button
              className="h-12 rounded border-2 border-black bg-card px-4 font-head font-black shadow-hard"
              type="button"
              onClick={() => {
                setQuery("");
                setEnabledFilter("");
                setScopeFilter("");
                void loadRoles("", "", "", size);
              }}
            >
              초기화
            </button>
          </div>
        </form>

        {message ? (
          <div
            role="status"
            className={`border-2 border-black px-4 py-3 font-bold shadow-hard ${state === "permission" || state === "error" ? "bg-destructive text-white" : "bg-accent text-black"}`}
          >
            {message}
          </div>
        ) : null}
        {state === "loading" ? (
          <StateCard title="LOADING" body="역할 목록을 불러오는 중입니다." />
        ) : null}
        {state === "empty" ? (
          <StateCard
            title="EMPTY"
            body="조건에 맞는 역할이 없습니다. 검색 조건을 초기화해 보세요."
          />
        ) : null}
        {state === "permission" ? (
          <StateCard
            title="PERMISSION DENIED"
            body="R09 시스템관리자 권한이 필요합니다. 직접 API 요청도 서버에서 403으로 차단됩니다."
          />
        ) : null}

        {state === "success" || roles.length > 0 ? (
          <section className="grid grid-cols-1 gap-4 lg:grid-cols-[minmax(0,1fr)_22rem]">
            <div className="overflow-hidden border-2 border-black bg-card shadow-hard">
              <div className="border-b-2 border-black bg-muted px-4 py-3 font-head font-black">
                roles 목록
              </div>
              <div className="overflow-x-auto">
                <table className="w-full min-w-[58rem] border-collapse text-left text-sm">
                  <thead className="bg-primary font-head text-xs uppercase">
                    <tr>
                      {[
                        "역할코드",
                        "역할명",
                        "목적",
                        "부여 기준",
                        "기본 범위",
                        "사용여부",
                        "사용자",
                        "메뉴권한",
                        "기능권한",
                      ].map((header) => (
                        <th
                          key={header}
                          className="border-b-2 border-black px-3 py-2"
                        >
                          {header}
                        </th>
                      ))}
                    </tr>
                  </thead>
                  <tbody>
                    {roles.map((item) => (
                      <tr
                        key={item.roleCode}
                        className={`cursor-pointer border-b-2 border-black transition-colors hover:bg-accent ${selectedRoleCode === item.roleCode ? "bg-accent" : "bg-card"}`}
                        onClick={() => setSelectedRoleCode(item.roleCode)}
                      >
                        <td className="px-3 py-2 font-mono">{item.roleCode}</td>
                        <td className="px-3 py-2 font-bold">{item.roleName}</td>
                        <td className="px-3 py-2">{item.purpose ?? "-"}</td>
                        <td className="px-3 py-2">
                          {item.grantCriteria ?? "-"}
                        </td>
                        <td className="px-3 py-2">
                          {scopeLabel(item.defaultDataScope)}
                        </td>
                        <td className="px-3 py-2">
                          <span className="border-2 border-black bg-primary px-2 py-1 font-head text-xs font-black">
                            {item.enabled ? "사용" : "미사용"}
                          </span>
                        </td>
                        <td className="px-3 py-2">{item.assignedUserCount}</td>
                        <td className="px-3 py-2">
                          {item.menuPermissionCount}
                        </td>
                        <td className="px-3 py-2">
                          {item.functionPermissionCount}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>

            <aside className="relative">
              <div className="absolute inset-2 border-2 border-black bg-primary" />
              <div className="relative border-2 border-black bg-card shadow-hard">
                <div className="border-b-2 border-black bg-muted px-4 py-3 font-head font-black">
                  상세 / 저장 전 확인
                </div>
                {selectedRole ? (
                  <div className="space-y-3 p-4 text-sm">
                    <ReadonlyField
                      label="역할 코드"
                      value={selectedRole.roleCode}
                    />
                    <ReadonlyField
                      label="역할명"
                      value={selectedRole.roleName}
                    />
                    <ReadonlyField
                      label="기본 데이터 범위"
                      value={scopeLabel(selectedRole.defaultDataScope)}
                    />
                    <ReadonlyField
                      label="사용여부"
                      value={selectedRole.enabled ? "사용" : "미사용"}
                    />
                    <ReadonlyField
                      label="권한 매트릭스"
                      value={`메뉴 ${selectedRole.menuPermissionCount} / 기능 ${selectedRole.functionPermissionCount}`}
                    />
                    <ReadonlyField
                      label="부여 사용자"
                      value={`${selectedRole.assignedUserCount}명`}
                    />
                    <p className="border-2 border-black bg-muted p-3 text-xs leading-relaxed">
                      역할 코드는 읽기 전용 생명주기 식별자입니다. 저장 CTA는
                      로컬 DB의 enabled/default_data_scope/목적/부여기준 관리
                      필드와 감사로그만 변경합니다.
                    </p>
                    {fieldErrors.id ? (
                      <p className="text-xs font-bold text-destructive">
                        {fieldErrors.id}
                      </p>
                    ) : null}
                    <button
                      className="h-12 w-full rounded border-2 border-black bg-primary px-4 font-head font-black shadow-hard transition-all duration-200 hover:-translate-x-0.5 hover:-translate-y-0.5 hover:bg-primary-hover hover:shadow-hard-lg active:translate-x-1 active:translate-y-1 active:shadow-none disabled:opacity-60"
                      type="button"
                      onClick={() => void saveSelectedRole()}
                      disabled={saving || !user?.roles.includes("R09")}
                    >
                      {saving ? "저장 중..." : "저장/실행 전 확인"}
                    </button>
                  </div>
                ) : (
                  <div className="p-4 text-sm">
                    행을 선택하면 상세 모달 영역이 표시됩니다.
                  </div>
                )}
              </div>
            </aside>
          </section>
        ) : null}
      </section>
    </AppShell>
  );
}

function UserRoleManagementPage() {
  const [user, setUser] = useState<AuthenticatedUser | null>(null);
  const [userRoles, setUserRoles] = useState<UserRoleListItem[]>([]);
  const [selectedUserRoleId, setSelectedUserRoleId] = useState<number | null>(
    null,
  );
  const [query, setQuery] = useState("");
  const [activeFilter, setActiveFilter] = useState("");
  const [roleFilter, setRoleFilter] = useState("");
  const [sourceFilter, setSourceFilter] = useState("");
  const [size, setSize] = useState(20);
  const [state, setState] = useState<ViewState>("loading");
  const [message, setMessage] = useState("");
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});
  const [saving, setSaving] = useState(false);

  const selectedUserRole = useMemo(
    () =>
      userRoles.find((item) => item.userRoleId === selectedUserRoleId) ?? null,
    [selectedUserRoleId, userRoles],
  );

  useEffect(() => {
    requestJson<AuthenticatedUser>("/api/auth/session")
      .then((sessionUser) => {
        setUser(sessionUser);
        return loadUserRoles("", "", "", "", size);
      })
      .catch((caught) => {
        const error = caught as Error & { status?: number };
        setState(error.status === 403 ? "permission" : "error");
        setMessage(error.message);
      });
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  async function loadUserRoles(
    nextQuery = query,
    nextActive = activeFilter,
    nextRole = roleFilter,
    nextSource = sourceFilter,
    nextSize = size,
  ) {
    setState("loading");
    setMessage("");
    const filterParts: string[] = [];
    if (nextActive) {
      filterParts.push(`active=${nextActive}`);
    }
    if (nextRole) {
      filterParts.push(`roleCode=${nextRole}`);
    }
    if (nextSource) {
      filterParts.push(`assignmentSource=${nextSource}`);
    }
    const params = new URLSearchParams({
      page: "1",
      size: String(nextSize),
      sort: "userId",
    });
    if (nextQuery.trim()) {
      params.set("q", nextQuery.trim());
    }
    if (filterParts.length > 0) {
      params.set("filter", filterParts.join(";"));
    }
    try {
      const body = await requestJson<UserRoleListResponse>(
        `/api/admin/user-roles?${params.toString()}`,
      );
      setUserRoles(body.items);
      setSelectedUserRoleId(body.items[0]?.userRoleId ?? null);
      setState(body.items.length === 0 ? "empty" : "success");
      setMessage(`총 ${body.totalCount}건 조회되었습니다.`);
    } catch (caught) {
      const error = caught as Error & { apiError?: ApiError; status?: number };
      setFieldErrors(error.apiError?.fields ?? {});
      setState(error.status === 403 ? "permission" : "error");
      setMessage(error.message);
    }
  }

  async function saveSelectedUserRole() {
    if (!selectedUserRole) {
      setMessage("저장할 사용자 역할을 먼저 선택하세요.");
      return;
    }
    const confirmed = window.confirm(
      `${selectedUserRole.userName ?? selectedUserRole.userId}의 ${selectedUserRole.roleName} 역할 유효기간을 변경하시겠습니까?`,
    );
    if (!confirmed) {
      return;
    }
    setSaving(true);
    setFieldErrors({});
    try {
      const result = await requestJson<{
        userRoleId: number;
        active: boolean;
        validTo?: string;
        assignmentSource: string;
        message: string;
      }>("/api/admin/user-roles", {
        method: "POST",
        body: JSON.stringify({
          id: String(selectedUserRole.userRoleId),
          active: !selectedUserRole.active,
          validTo: selectedUserRole.active ? "2026-12-31" : null,
          assignmentSource: selectedUserRole.assignmentSource,
          reason: "사용자 역할 관리 화면 저장 CTA",
        }),
      });
      setMessage(result.message);
      await loadUserRoles();
      setSelectedUserRoleId(result.userRoleId);
    } catch (caught) {
      const error = caught as Error & { apiError?: ApiError; status?: number };
      setFieldErrors(error.apiError?.fields ?? {});
      setState(error.status === 403 ? "permission" : "error");
      setMessage(error.message);
    } finally {
      setSaving(false);
    }
  }

  if (!user && state === "error") {
    return (
      <AppShell>
        <LoginPanel
          onLogin={(nextUser) => {
            setUser(nextUser);
            void loadUserRoles();
          }}
        />
      </AppShell>
    );
  }

  return (
    <AppShell>
      <section className="space-y-7 sm:space-y-8 lg:space-y-9">
        <div className="flex flex-col gap-4 lg:flex-row lg:items-end lg:justify-between">
          <div>
            <div className="mb-3 inline-flex border-2 border-black bg-primary px-2.5 py-1 font-head text-xs font-black uppercase tracking-[0.14em] shadow-hard">
              SCR-USER-ROLE-MGMT
            </div>
            <h1 className="font-head text-4xl font-black uppercase leading-none tracking-tight sm:text-5xl">
              사용자 역할 관리
            </h1>
            <p className="mt-4 max-w-3xl text-sm leading-relaxed text-muted-foreground sm:text-base">
              사용자에게 부여된 역할의 수동/보직 기반 구분, 유효기간, 승인자를
              조회하고 종료된 역할이 현재 적용 역할로 판정되지 않도록
              관리합니다.
            </p>
          </div>
          <div className="rounded border-2 border-black bg-card px-4 py-3 font-head text-sm font-black shadow-hard">
            {user
              ? `${user.userId} / ${user.roles.join(", ")}`
              : "세션 확인 중"}
          </div>
        </div>

        <form
          className="grid grid-cols-1 gap-4 border-2 border-black bg-card p-4 shadow-hard sm:grid-cols-[minmax(0,1fr)_9rem_9rem_10rem_9rem_9rem] sm:p-5"
          onSubmit={(event) => {
            event.preventDefault();
            void loadUserRoles();
          }}
        >
          <label className="block">
            <span className="mb-2 block font-head text-sm font-bold">
              통합 검색
            </span>
            <input
              className="h-12 w-full rounded border-2 border-black bg-white px-4 py-2 shadow-hard"
              placeholder="사용자, 교번, 역할"
              value={query}
              onChange={(event) => setQuery(event.target.value)}
            />
            {fieldErrors.q ? (
              <span className="mt-1.5 block text-xs text-destructive">
                {fieldErrors.q}
              </span>
            ) : null}
          </label>
          <label className="block">
            <span className="mb-2 block font-head text-sm font-bold">
              현재 적용
            </span>
            <select
              className="h-12 w-full rounded border-2 border-black bg-white px-3 shadow-hard"
              value={activeFilter}
              onChange={(event) => setActiveFilter(event.target.value)}
            >
              <option value="">전체</option>
              <option value="true">적용</option>
              <option value="false">종료</option>
            </select>
          </label>
          <label className="block">
            <span className="mb-2 block font-head text-sm font-bold">역할</span>
            <select
              className="h-12 w-full rounded border-2 border-black bg-white px-3 shadow-hard"
              value={roleFilter}
              onChange={(event) => setRoleFilter(event.target.value)}
            >
              <option value="">전체</option>
              {[
                "R01",
                "R02",
                "R03",
                "R04",
                "R05",
                "R06",
                "R07",
                "R08",
                "R09",
              ].map((roleCode) => (
                <option key={roleCode} value={roleCode}>
                  {roleCode}
                </option>
              ))}
            </select>
          </label>
          <label className="block">
            <span className="mb-2 block font-head text-sm font-bold">
              부여 구분
            </span>
            <select
              className="h-12 w-full rounded border-2 border-black bg-white px-3 shadow-hard"
              value={sourceFilter}
              onChange={(event) => setSourceFilter(event.target.value)}
            >
              <option value="">전체</option>
              <option value="MANUAL">수동</option>
              <option value="POSITION">보직 기반</option>
            </select>
          </label>
          <label className="block">
            <span className="mb-2 block font-head text-sm font-bold">
              표시 건수
            </span>
            <select
              className="h-12 w-full rounded border-2 border-black bg-white px-3 shadow-hard"
              value={size}
              onChange={(event) => setSize(Number(event.target.value))}
            >
              {sizeOptions.map((option) => (
                <option key={option} value={option}>
                  {option}건
                </option>
              ))}
            </select>
          </label>
          <div className="flex items-end gap-2">
            <button
              className="h-12 flex-1 rounded border-2 border-black bg-primary px-4 font-head font-black shadow-hard transition-all duration-200 hover:-translate-x-0.5 hover:-translate-y-0.5 hover:bg-primary-hover hover:shadow-hard-lg active:translate-x-1 active:translate-y-1 active:shadow-none"
              type="submit"
            >
              검색
            </button>
            <button
              className="h-12 rounded border-2 border-black bg-card px-4 font-head font-black shadow-hard"
              type="button"
              onClick={() => {
                setQuery("");
                setActiveFilter("");
                setRoleFilter("");
                setSourceFilter("");
                void loadUserRoles("", "", "", "", size);
              }}
            >
              초기화
            </button>
          </div>
        </form>

        {message ? (
          <div
            role="status"
            className={`border-2 border-black px-4 py-3 font-bold shadow-hard ${state === "permission" || state === "error" ? "bg-destructive text-white" : "bg-accent text-black"}`}
          >
            {message}
          </div>
        ) : null}
        {state === "loading" ? (
          <StateCard
            title="LOADING"
            body="사용자 역할 목록을 불러오는 중입니다."
          />
        ) : null}
        {state === "empty" ? (
          <StateCard
            title="EMPTY"
            body="조건에 맞는 사용자 역할이 없습니다. 검색 조건을 초기화해 보세요."
          />
        ) : null}
        {state === "permission" ? (
          <StateCard
            title="PERMISSION DENIED"
            body="R09 시스템관리자 권한이 필요합니다. 직접 API 요청도 서버에서 403으로 차단됩니다."
          />
        ) : null}

        {state === "success" || userRoles.length > 0 ? (
          <section className="grid grid-cols-1 gap-4 lg:grid-cols-[minmax(0,1fr)_22rem]">
            <div className="overflow-hidden border-2 border-black bg-card shadow-hard">
              <div className="border-b-2 border-black bg-muted px-4 py-3 font-head font-black">
                user_roles 목록
              </div>
              <div className="overflow-x-auto">
                <table className="w-full min-w-[62rem] border-collapse text-left text-sm">
                  <thead className="bg-primary font-head text-xs uppercase">
                    <tr>
                      {[
                        "ID",
                        "사용자",
                        "교번",
                        "역할",
                        "부여 구분",
                        "시작일",
                        "종료일",
                        "현재 적용",
                        "승인자",
                      ].map((header) => (
                        <th
                          key={header}
                          className="border-b-2 border-black px-3 py-2"
                        >
                          {header}
                        </th>
                      ))}
                    </tr>
                  </thead>
                  <tbody>
                    {userRoles.map((item) => (
                      <tr
                        key={item.userRoleId}
                        className={`cursor-pointer border-b-2 border-black transition-colors hover:bg-accent ${selectedUserRoleId === item.userRoleId ? "bg-accent" : "bg-card"}`}
                        onClick={() => setSelectedUserRoleId(item.userRoleId)}
                      >
                        <td className="px-3 py-2 font-mono">
                          {item.userRoleId}
                        </td>
                        <td className="px-3 py-2 font-bold">
                          {item.userName ?? item.userId}
                        </td>
                        <td className="px-3 py-2">{item.employeeNo ?? "-"}</td>
                        <td className="px-3 py-2">
                          {item.roleName} ({item.roleCode})
                        </td>
                        <td className="px-3 py-2">
                          {assignmentSourceLabel(item.assignmentSource)}
                        </td>
                        <td className="px-3 py-2">{item.validFrom}</td>
                        <td className="px-3 py-2">{item.validTo ?? "현재"}</td>
                        <td className="px-3 py-2">
                          <span className="border-2 border-black bg-primary px-2 py-1 font-head text-xs font-black">
                            {item.active ? "적용" : "종료"}
                          </span>
                        </td>
                        <td className="px-3 py-2">
                          {item.approverName ?? item.approverId ?? "-"}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>

            <aside className="relative">
              <div className="absolute inset-2 border-2 border-black bg-primary" />
              <div className="relative border-2 border-black bg-card shadow-hard">
                <div className="border-b-2 border-black bg-muted px-4 py-3 font-head font-black">
                  상세 / 저장 전 확인
                </div>
                {selectedUserRole ? (
                  <div className="space-y-3 p-4 text-sm">
                    <ReadonlyField
                      label="사용자 역할 ID"
                      value={String(selectedUserRole.userRoleId)}
                    />
                    <ReadonlyField
                      label="사용자"
                      value={`${selectedUserRole.userName ?? selectedUserRole.userId} / ${selectedUserRole.employeeNo ?? selectedUserRole.userId}`}
                    />
                    <ReadonlyField
                      label="역할"
                      value={`${selectedUserRole.roleName} (${selectedUserRole.roleCode})`}
                    />
                    <ReadonlyField
                      label="부여 구분"
                      value={assignmentSourceLabel(
                        selectedUserRole.assignmentSource,
                      )}
                    />
                    <ReadonlyField
                      label="유효기간"
                      value={`${selectedUserRole.validFrom} ~ ${selectedUserRole.validTo ?? "현재"}`}
                    />
                    <ReadonlyField
                      label="승인자"
                      value={
                        selectedUserRole.approverName ??
                        selectedUserRole.approverId ??
                        "-"
                      }
                    />
                    <p className="border-2 border-black bg-muted p-3 text-xs leading-relaxed">
                      사용자·역할 식별자는 읽기 전용입니다. 저장 CTA는 로컬 DB의
                      valid_to 및 부여 구분 관리 필드와 감사로그만 변경하며,
                      종료일이 지난 역할은 현재 적용에서 제외됩니다.
                    </p>
                    {fieldErrors.id ? (
                      <p className="text-xs font-bold text-destructive">
                        {fieldErrors.id}
                      </p>
                    ) : null}
                    <button
                      className="h-12 w-full rounded border-2 border-black bg-primary px-4 font-head font-black shadow-hard transition-all duration-200 hover:-translate-x-0.5 hover:-translate-y-0.5 hover:bg-primary-hover hover:shadow-hard-lg active:translate-x-1 active:translate-y-1 active:shadow-none disabled:opacity-60"
                      type="button"
                      onClick={() => void saveSelectedUserRole()}
                      disabled={saving || !user?.roles.includes("R09")}
                    >
                      {saving ? "저장 중..." : "저장/실행 전 확인"}
                    </button>
                  </div>
                ) : (
                  <div className="p-4 text-sm">
                    행을 선택하면 상세 모달 영역이 표시됩니다.
                  </div>
                )}
              </div>
            </aside>
          </section>
        ) : null}
      </section>
    </AppShell>
  );
}

function MenuPermissionManagementPage() {
  const [user, setUser] = useState<AuthenticatedUser | null>(null);
  const [permissions, setPermissions] = useState<MenuPermissionListItem[]>([]);
  const [selectedPermissionId, setSelectedPermissionId] = useState<
    number | null
  >(null);
  const [query, setQuery] = useState("");
  const [targetTypeFilter, setTargetTypeFilter] = useState("");
  const [targetIdFilter, setTargetIdFilter] = useState("");
  const [allowedFilter, setAllowedFilter] = useState("");
  const [size, setSize] = useState(20);
  const [state, setState] = useState<ViewState>("loading");
  const [message, setMessage] = useState("");
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});
  const [saving, setSaving] = useState(false);

  const selectedPermission = useMemo(
    () =>
      permissions.find(
        (item) => item.menuPermissionId === selectedPermissionId,
      ) ?? null,
    [selectedPermissionId, permissions],
  );

  useEffect(() => {
    requestJson<AuthenticatedUser>("/api/auth/session")
      .then((sessionUser) => {
        setUser(sessionUser);
        return loadMenuPermissions("", "", "", "", size);
      })
      .catch((caught) => {
        const error = caught as Error & { status?: number };
        setState(error.status === 403 ? "permission" : "error");
        setMessage(error.message);
      });
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  async function loadMenuPermissions(
    nextQuery = query,
    nextTargetType = targetTypeFilter,
    nextTargetId = targetIdFilter,
    nextAllowed = allowedFilter,
    nextSize = size,
  ) {
    setState("loading");
    setMessage("");
    const filterParts: string[] = [];
    if (nextTargetType) {
      filterParts.push(`targetType=${nextTargetType}`);
    }
    if (nextTargetId.trim()) {
      filterParts.push(`targetId=${nextTargetId.trim()}`);
    }
    if (nextAllowed) {
      filterParts.push(`allowed=${nextAllowed}`);
    }
    const params = new URLSearchParams({
      page: "1",
      size: String(nextSize),
      sort: "displayOrder",
    });
    if (nextQuery.trim()) {
      params.set("q", nextQuery.trim());
    }
    if (filterParts.length > 0) {
      params.set("filter", filterParts.join(";"));
    }
    try {
      const body = await requestJson<MenuPermissionListResponse>(
        `/api/admin/menu-permissions?${params.toString()}`,
      );
      setPermissions(body.items);
      setSelectedPermissionId(body.items[0]?.menuPermissionId ?? null);
      setState(body.items.length === 0 ? "empty" : "success");
      setMessage(`총 ${body.totalCount}건 조회되었습니다.`);
    } catch (caught) {
      const error = caught as Error & { apiError?: ApiError; status?: number };
      setFieldErrors(error.apiError?.fields ?? {});
      setState(error.status === 403 ? "permission" : "error");
      setMessage(error.message);
    }
  }

  async function saveSelectedMenuPermission() {
    if (!selectedPermission) {
      setMessage("저장할 메뉴 권한을 먼저 선택하세요.");
      return;
    }
    const confirmed = window.confirm(
      `${selectedPermission.targetName ?? selectedPermission.targetId}의 ${selectedPermission.menuName} 접근 허용 여부를 변경하시겠습니까?`,
    );
    if (!confirmed) {
      return;
    }
    setSaving(true);
    setFieldErrors({});
    try {
      const result = await requestJson<{
        menuPermissionId: number;
        allowed: boolean;
        targetType: string;
        targetId: string;
        message: string;
      }>("/api/admin/menu-permissions", {
        method: "POST",
        body: JSON.stringify({
          id: String(selectedPermission.menuPermissionId),
          allowed: !selectedPermission.allowed,
          targetType: selectedPermission.targetType,
          targetId: selectedPermission.targetId,
          reason: "메뉴 접근 허용 여부 저장 전 확인",
        }),
      });
      setMessage(result.message);
      await loadMenuPermissions();
    } catch (caught) {
      const error = caught as Error & { apiError?: ApiError; status?: number };
      setFieldErrors(error.apiError?.fields ?? {});
      setState(error.status === 403 ? "permission" : "error");
      setMessage(error.message);
    } finally {
      setSaving(false);
    }
  }

  return (
    <AppShell>
      <section className="flex flex-col gap-6">
        <div className="flex flex-col gap-4 lg:flex-row lg:items-end lg:justify-between">
          <div>
            <div className="mb-3 inline-flex border-2 border-black bg-primary px-2.5 py-1 font-head text-xs font-black uppercase tracking-[0.14em] shadow-hard">
              SCR-MENU-PERMISSION
            </div>
            <h1 className="font-head text-4xl font-black uppercase leading-none tracking-tight sm:text-5xl">
              메뉴 권한 관리
            </h1>
            <p className="mt-4 max-w-3xl text-sm leading-relaxed text-muted-foreground sm:text-base">
              역할·조직·사용자 단위로 메뉴 접근 여부를 설정합니다. 메뉴/대상
              식별자는 읽기 전용이며, 저장 CTA는 접근 허용 여부와 감사로그만
              변경합니다.
            </p>
          </div>
          <div className="rounded border-2 border-black bg-card px-4 py-3 font-head text-sm font-black shadow-hard">
            {user
              ? `${user.userId} / ${user.roles.join(", ")}`
              : "세션 확인 중"}
          </div>
        </div>

        <form
          className="grid grid-cols-1 gap-4 border-2 border-black bg-card p-4 shadow-hard sm:grid-cols-[minmax(0,1fr)_9rem_9rem_9rem_9rem_9rem] sm:p-5"
          onSubmit={(event) => {
            event.preventDefault();
            void loadMenuPermissions();
          }}
        >
          <label className="block">
            <span className="mb-2 block font-head text-sm font-bold">
              통합 검색
            </span>
            <input
              className="h-12 w-full rounded border-2 border-black bg-white px-4 py-2 shadow-hard"
              placeholder="메뉴명, 화면ID, 대상명"
              value={query}
              onChange={(event) => setQuery(event.target.value)}
            />
            {fieldErrors.q ? (
              <span className="mt-1.5 block text-xs text-destructive">
                {fieldErrors.q}
              </span>
            ) : null}
          </label>
          <label className="block">
            <span className="mb-2 block font-head text-sm font-bold">
              대상 유형
            </span>
            <select
              className="h-12 w-full rounded border-2 border-black bg-white px-3 shadow-hard"
              value={targetTypeFilter}
              onChange={(event) => setTargetTypeFilter(event.target.value)}
            >
              <option value="">전체</option>
              <option value="ROLE">역할</option>
              <option value="ORG">조직</option>
              <option value="USER">사용자</option>
            </select>
          </label>
          <label className="block">
            <span className="mb-2 block font-head text-sm font-bold">
              대상 ID
            </span>
            <input
              className="h-12 w-full rounded border-2 border-black bg-white px-3 shadow-hard"
              placeholder="R09"
              value={targetIdFilter}
              onChange={(event) => setTargetIdFilter(event.target.value)}
            />
          </label>
          <label className="block">
            <span className="mb-2 block font-head text-sm font-bold">접근</span>
            <select
              className="h-12 w-full rounded border-2 border-black bg-white px-3 shadow-hard"
              value={allowedFilter}
              onChange={(event) => setAllowedFilter(event.target.value)}
            >
              <option value="">전체</option>
              <option value="true">허용</option>
              <option value="false">차단</option>
            </select>
          </label>
          <label className="block">
            <span className="mb-2 block font-head text-sm font-bold">
              표시 건수
            </span>
            <select
              className="h-12 w-full rounded border-2 border-black bg-white px-3 shadow-hard"
              value={size}
              onChange={(event) => setSize(Number(event.target.value))}
            >
              {sizeOptions.map((option) => (
                <option key={option} value={option}>
                  {option}건
                </option>
              ))}
            </select>
          </label>
          <div className="flex items-end gap-2">
            <button
              className="h-12 flex-1 rounded border-2 border-black bg-primary px-4 font-head font-black shadow-hard transition-all duration-200 hover:-translate-x-0.5 hover:-translate-y-0.5 hover:bg-primary-hover hover:shadow-hard-lg active:translate-x-1 active:translate-y-1 active:shadow-none"
              type="submit"
            >
              검색
            </button>
            <button
              className="h-12 rounded border-2 border-black bg-card px-4 font-head font-black shadow-hard"
              type="button"
              onClick={() => {
                setQuery("");
                setTargetTypeFilter("");
                setTargetIdFilter("");
                setAllowedFilter("");
                void loadMenuPermissions("", "", "", "", size);
              }}
            >
              초기화
            </button>
          </div>
        </form>

        {message ? (
          <div
            role="status"
            className={`border-2 border-black px-4 py-3 font-bold shadow-hard ${state === "permission" || state === "error" ? "bg-destructive text-white" : "bg-accent text-black"}`}
          >
            {message}
          </div>
        ) : null}
        {state === "loading" ? (
          <StateCard
            title="LOADING"
            body="메뉴 권한 매트릭스를 불러오는 중입니다."
          />
        ) : null}
        {state === "empty" ? (
          <StateCard
            title="EMPTY"
            body="조건에 맞는 메뉴 권한이 없습니다. 검색 조건을 초기화해 보세요."
          />
        ) : null}
        {state === "permission" ? (
          <StateCard
            title="PERMISSION DENIED"
            body="R09 시스템관리자 권한이 필요합니다. 직접 API 요청도 서버에서 403으로 차단됩니다."
          />
        ) : null}

        {state === "success" || permissions.length > 0 ? (
          <section className="grid grid-cols-1 gap-4 lg:grid-cols-[minmax(0,1fr)_22rem]">
            <div className="overflow-hidden border-2 border-black bg-card shadow-hard">
              <div className="border-b-2 border-black bg-muted px-4 py-3 font-head font-black">
                menu_permissions 목록
              </div>
              <div className="overflow-x-auto">
                <table className="w-full min-w-[64rem] border-collapse text-left text-sm">
                  <thead className="bg-primary font-head text-xs uppercase">
                    <tr>
                      {[
                        "대상 유형",
                        "대상",
                        "상위 메뉴",
                        "메뉴",
                        "화면ID",
                        "URL",
                        "접근",
                        "출처",
                      ].map((header) => (
                        <th
                          key={header}
                          className="border-b-2 border-black px-3 py-2"
                        >
                          {header}
                        </th>
                      ))}
                    </tr>
                  </thead>
                  <tbody>
                    {permissions.map((item) => (
                      <tr
                        key={item.menuPermissionId}
                        className={`cursor-pointer border-b-2 border-black transition-colors hover:bg-accent ${selectedPermissionId === item.menuPermissionId ? "bg-accent" : "bg-card"}`}
                        onClick={() =>
                          setSelectedPermissionId(item.menuPermissionId)
                        }
                      >
                        <td className="px-3 py-2 font-bold">
                          {menuTargetTypeLabel(item.targetType)}
                        </td>
                        <td className="px-3 py-2">
                          <span className="font-mono">{item.targetId}</span>{" "}
                          {item.targetName ? (
                            <>
                              / <span>{item.targetName}</span>
                            </>
                          ) : (
                            ""
                          )}
                        </td>
                        <td className="px-3 py-2">
                          {item.parentMenuName ?? "-"}
                        </td>
                        <td className="px-3 py-2 font-bold">{item.menuName}</td>
                        <td className="px-3 py-2 font-mono">{item.screenId}</td>
                        <td className="px-3 py-2 font-mono">{item.url}</td>
                        <td className="px-3 py-2">
                          <span className="border-2 border-black bg-primary px-2 py-1 font-head text-xs font-black">
                            {item.allowed ? "허용" : "차단"}
                          </span>
                        </td>
                        <td className="px-3 py-2">{item.permissionSource}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>

            <aside className="relative">
              <div className="absolute inset-2 border-2 border-black bg-primary" />
              <div className="relative border-2 border-black bg-card shadow-hard">
                <div className="border-b-2 border-black bg-muted px-4 py-3 font-head font-black">
                  상세 / 저장 전 확인
                </div>
                {selectedPermission ? (
                  <div className="space-y-3 p-4 text-sm">
                    <ReadonlyField
                      label="메뉴 권한 ID"
                      value={String(selectedPermission.menuPermissionId)}
                    />
                    <ReadonlyField
                      label="대상"
                      value={`${menuTargetTypeLabel(selectedPermission.targetType)} ${selectedPermission.targetId} / ${selectedPermission.targetName ?? "-"}`}
                    />
                    <ReadonlyField
                      label="메뉴"
                      value={`${selectedPermission.menuName} (${selectedPermission.screenId})`}
                    />
                    <ReadonlyField
                      label="접근 허용"
                      value={selectedPermission.allowed ? "허용" : "차단"}
                    />
                    <ReadonlyField label="URL" value={selectedPermission.url} />
                    <p className="border-2 border-black bg-muted p-3 text-xs leading-relaxed">
                      대상 유형·대상 ID·메뉴 ID는 읽기 전용 생명주기
                      식별자입니다. 화면 미노출과 서버 접근통제를 동일하게
                      적용하도록 저장 후 목록을 다시 조회합니다.
                    </p>
                    {fieldErrors.id ? (
                      <p className="text-xs font-bold text-destructive">
                        {fieldErrors.id}
                      </p>
                    ) : null}
                    <button
                      className="h-12 w-full rounded border-2 border-black bg-primary px-4 font-head font-black shadow-hard transition-all duration-200 hover:-translate-x-0.5 hover:-translate-y-0.5 hover:bg-primary-hover hover:shadow-hard-lg active:translate-x-1 active:translate-y-1 active:shadow-none disabled:opacity-60"
                      type="button"
                      onClick={() => void saveSelectedMenuPermission()}
                      disabled={saving || !user?.roles.includes("R09")}
                    >
                      {saving ? "저장 중..." : "저장/실행 전 확인"}
                    </button>
                  </div>
                ) : (
                  <div className="p-4 text-sm">
                    행을 선택하면 상세 모달 영역이 표시됩니다.
                  </div>
                )}
              </div>
            </aside>
          </section>
        ) : null}
      </section>
    </AppShell>
  );
}

function menuTargetTypeLabel(value: string) {
  switch (value) {
    case "ROLE":
      return "역할";
    case "ORG":
      return "조직";
    case "USER":
      return "사용자";
    default:
      return value;
  }
}

function assignmentSourceLabel(value: string) {
  switch (value) {
    case "MANUAL":
      return "수동";
    case "POSITION":
      return "보직 기반";
    default:
      return value;
  }
}

function scopeLabel(value: string) {
  switch (value) {
    case "SELF":
      return "본인";
    case "DEPARTMENT":
      return "소속학과";
    case "COLLEGE":
      return "단과대학";
    case "BUSINESS":
      return "담당업무";
    case "ALL":
      return "전체";
    default:
      return value;
  }
}

function FunctionPermissionManagementPage() {
  const [user, setUser] = useState<AuthenticatedUser | null>(null);
  const [permissions, setPermissions] = useState<FunctionPermissionListItem[]>(
    [],
  );
  const [selectedPermissionId, setSelectedPermissionId] = useState<
    number | null
  >(null);
  const [query, setQuery] = useState("");
  const [roleFilter, setRoleFilter] = useState("R09");
  const [actionFilter, setActionFilter] = useState("");
  const [size, setSize] = useState(20);
  const [state, setState] = useState<ViewState>("loading");
  const [message, setMessage] = useState("");
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});
  const [saving, setSaving] = useState(false);

  const selectedPermission = useMemo(
    () =>
      permissions.find(
        (item) => item.functionPermissionId === selectedPermissionId,
      ) ?? null,
    [selectedPermissionId, permissions],
  );

  useEffect(() => {
    requestJson<AuthenticatedUser>("/api/auth/session")
      .then((sessionUser) => {
        setUser(sessionUser);
        return loadFunctionPermissions("", roleFilter, "", size);
      })
      .catch((caught) => {
        const error = caught as Error & { status?: number };
        setState(error.status === 403 ? "permission" : "error");
        setMessage(error.message);
      });
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  async function loadFunctionPermissions(
    nextQuery = query,
    nextRole = roleFilter,
    nextAction = actionFilter,
    nextSize = size,
  ) {
    setState("loading");
    setMessage("");
    const filterParts = [
      nextRole ? `roleCode=${nextRole}` : "",
      nextAction ? `actionCode=${nextAction}` : "",
    ].filter(Boolean);
    const params = new URLSearchParams({
      page: "1",
      size: String(nextSize),
      sort: "screenId",
    });
    if (nextQuery.trim()) {
      params.set("q", nextQuery.trim());
    }
    if (filterParts.length > 0) {
      params.set("filter", filterParts.join(";"));
    }
    try {
      const body = await requestJson<FunctionPermissionListResponse>(
        `/api/admin/function-permissions?${params.toString()}`,
      );
      setPermissions(body.items);
      setSelectedPermissionId(body.items[0]?.functionPermissionId ?? null);
      setState(body.items.length === 0 ? "empty" : "success");
      setMessage(`총 ${body.totalCount}건 조회되었습니다.`);
    } catch (caught) {
      const error = caught as Error & { apiError?: ApiError; status?: number };
      setFieldErrors(error.apiError?.fields ?? {});
      setState(error.status === 403 ? "permission" : "error");
      setMessage(error.message);
    }
  }

  async function saveSelectedFunctionPermission() {
    if (!selectedPermission) {
      setMessage("저장할 기능 권한을 먼저 선택하세요.");
      return;
    }
    const confirmed = window.confirm(
      `${selectedPermission.roleCode} ${selectedPermission.screenId} ${selectedPermission.actionName} 권한을 변경하시겠습니까? 다른 기능구분은 자동 변경되지 않습니다.`,
    );
    if (!confirmed) {
      return;
    }
    setSaving(true);
    setFieldErrors({});
    try {
      const result = await requestJson<{
        functionPermissionId: number;
        allowed: boolean;
        roleCode: string;
        screenId: string;
        actionCode: string;
        message: string;
      }>("/api/admin/function-permissions", {
        method: "POST",
        body: JSON.stringify({
          id: String(selectedPermission.functionPermissionId),
          allowed: !selectedPermission.allowed,
          roleCode: selectedPermission.roleCode,
          screenId: selectedPermission.screenId,
          actionCode: selectedPermission.actionCode,
          reason: "기능 권한 관리 화면 저장 CTA",
        }),
      });
      setMessage(result.message);
      await loadFunctionPermissions();
      setSelectedPermissionId(result.functionPermissionId);
    } catch (caught) {
      const error = caught as Error & { apiError?: ApiError; status?: number };
      setFieldErrors(error.apiError?.fields ?? {});
      setState(error.status === 403 ? "permission" : "error");
      setMessage(error.message);
    } finally {
      setSaving(false);
    }
  }

  if (!user && state === "error") {
    return (
      <AppShell>
        <LoginPanel
          onLogin={(nextUser) => {
            setUser(nextUser);
            void loadFunctionPermissions();
          }}
        />
      </AppShell>
    );
  }

  return (
    <AppShell>
      <section className="space-y-7 sm:space-y-8 lg:space-y-9">
        <div className="flex flex-col gap-4 lg:flex-row lg:items-end lg:justify-between">
          <div>
            <div className="mb-3 inline-flex border-2 border-black bg-primary px-2.5 py-1 font-head text-xs font-black uppercase tracking-[0.14em] shadow-hard">
              SCR-FUNCTION-PERMISSION
            </div>
            <h1 className="font-head text-4xl font-black uppercase leading-none tracking-tight sm:text-5xl">
              기능 권한 관리
            </h1>
            <p className="mt-4 max-w-3xl text-sm leading-relaxed text-muted-foreground sm:text-base">
              화면별
              조회·등록·수정·삭제·확인·인증·승인·승인취소·출력·엑셀·일괄처리
              권한을 역할별로 설정합니다. 선택한 기능구분만 저장하며 다른
              기능구분은 자동 변경하지 않습니다.
            </p>
          </div>
          <div className="rounded border-2 border-black bg-card px-4 py-3 font-head text-sm font-black shadow-hard">
            {user
              ? `${user.userId} / ${user.roles.join(", ")}`
              : "세션 확인 중"}
          </div>
        </div>

        <form
          className="grid grid-cols-1 gap-4 border-2 border-black bg-card p-4 shadow-hard sm:grid-cols-[minmax(0,1fr)_9rem_10rem_9rem_9rem] sm:p-5"
          onSubmit={(event) => {
            event.preventDefault();
            void loadFunctionPermissions();
          }}
        >
          <label className="block">
            <span className="mb-2 block font-head text-sm font-bold">
              통합 검색
            </span>
            <input
              className="h-12 w-full rounded border-2 border-black bg-white px-4 py-2 shadow-hard"
              placeholder="역할, 화면, 기능구분"
              value={query}
              onChange={(event) => setQuery(event.target.value)}
            />
            {fieldErrors.q ? (
              <span className="mt-1.5 block text-xs text-destructive">
                {fieldErrors.q}
              </span>
            ) : null}
          </label>
          <label className="block">
            <span className="mb-2 block font-head text-sm font-bold">역할</span>
            <select
              className="h-12 w-full rounded border-2 border-black bg-white px-3 shadow-hard"
              value={roleFilter}
              onChange={(event) => setRoleFilter(event.target.value)}
            >
              <option value="">전체</option>
              <option value="R09">R09</option>
              <option value="R01">R01</option>
            </select>
          </label>
          <label className="block">
            <span className="mb-2 block font-head text-sm font-bold">
              기능구분
            </span>
            <select
              className="h-12 w-full rounded border-2 border-black bg-white px-3 shadow-hard"
              value={actionFilter}
              onChange={(event) => setActionFilter(event.target.value)}
            >
              <option value="">전체</option>
              {[
                "READ",
                "CREATE",
                "UPDATE",
                "DELETE",
                "VERIFY",
                "AUTH",
                "APPROVE",
                "CANCEL_APPROVAL",
                "PRINT",
                "EXCEL",
                "BULK",
              ].map((action) => (
                <option key={action} value={action}>
                  {functionActionLabel(action)}
                </option>
              ))}
            </select>
          </label>
          <label className="block">
            <span className="mb-2 block font-head text-sm font-bold">
              표시 건수
            </span>
            <select
              className="h-12 w-full rounded border-2 border-black bg-white px-3 shadow-hard"
              value={size}
              onChange={(event) => setSize(Number(event.target.value))}
            >
              {sizeOptions.map((option) => (
                <option key={option} value={option}>
                  {option}건
                </option>
              ))}
            </select>
          </label>
          <div className="flex items-end gap-2">
            <button
              className="h-12 flex-1 rounded border-2 border-black bg-primary px-4 font-head font-black shadow-hard transition-all duration-200 hover:-translate-x-0.5 hover:-translate-y-0.5 hover:bg-primary-hover hover:shadow-hard-lg active:translate-x-1 active:translate-y-1 active:shadow-none"
              type="submit"
            >
              검색
            </button>
            <button
              className="h-12 rounded border-2 border-black bg-card px-4 font-head font-black shadow-hard"
              type="button"
              onClick={() => {
                setQuery("");
                setRoleFilter("R09");
                setActionFilter("");
                void loadFunctionPermissions("", "R09", "", size);
              }}
            >
              초기화
            </button>
          </div>
        </form>

        {message ? (
          <div
            role="status"
            className={`border-2 border-black px-4 py-3 font-bold shadow-hard ${state === "permission" || state === "error" ? "bg-destructive text-white" : "bg-accent text-black"}`}
          >
            {message}
          </div>
        ) : null}
        {state === "loading" ? (
          <StateCard
            title="LOADING"
            body="기능 권한 매트릭스를 불러오는 중입니다."
          />
        ) : null}
        {state === "empty" ? (
          <StateCard
            title="EMPTY"
            body="조건에 맞는 기능 권한이 없습니다. 검색 조건을 초기화해 보세요."
          />
        ) : null}
        {state === "permission" ? (
          <StateCard
            title="PERMISSION DENIED"
            body="R09 시스템관리자 권한이 필요합니다. 직접 API 요청도 서버에서 403으로 차단됩니다."
          />
        ) : null}

        {state === "success" || permissions.length > 0 ? (
          <section className="grid grid-cols-1 gap-4 lg:grid-cols-[minmax(0,1fr)_22rem]">
            <div className="overflow-hidden border-2 border-black bg-card shadow-hard">
              <div className="border-b-2 border-black bg-muted px-4 py-3 font-head font-black">
                function_permissions 목록
              </div>
              <div className="overflow-x-auto">
                <table className="w-full min-w-[58rem] border-collapse text-left text-sm">
                  <thead className="bg-primary font-head text-xs uppercase">
                    <tr>
                      {[
                        "ID",
                        "역할",
                        "화면",
                        "메뉴",
                        "기능구분",
                        "허용여부",
                        "범위",
                      ].map((header) => (
                        <th
                          key={header}
                          className="border-b-2 border-black px-3 py-2"
                        >
                          {header}
                        </th>
                      ))}
                    </tr>
                  </thead>
                  <tbody>
                    {permissions.map((item) => (
                      <tr
                        key={item.functionPermissionId}
                        className={`cursor-pointer border-b-2 border-black transition-colors hover:bg-accent ${selectedPermissionId === item.functionPermissionId ? "bg-accent" : "bg-card"}`}
                        onClick={() =>
                          setSelectedPermissionId(item.functionPermissionId)
                        }
                      >
                        <td className="px-3 py-2 font-mono">
                          {item.functionPermissionId}
                        </td>
                        <td className="px-3 py-2 font-bold">
                          {item.roleName} ({item.roleCode})
                        </td>
                        <td className="px-3 py-2 font-mono">{item.screenId}</td>
                        <td className="px-3 py-2">
                          {item.menuName ?? item.screenName}
                        </td>
                        <td className="px-3 py-2">
                          <span className="border-2 border-black bg-primary px-2 py-1 font-head text-xs font-black">
                            {item.actionName}
                          </span>
                        </td>
                        <td className="px-3 py-2">
                          {item.allowed ? "허용" : "차단"}
                        </td>
                        <td className="px-3 py-2">{item.permissionScope}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>

            <aside className="relative">
              <div className="absolute inset-2 border-2 border-black bg-primary" />
              <div className="relative border-2 border-black bg-card shadow-hard">
                <div className="border-b-2 border-black bg-muted px-4 py-3 font-head font-black">
                  상세 / 저장 전 확인
                </div>
                {selectedPermission ? (
                  <div className="space-y-3 p-4 text-sm">
                    <ReadonlyField
                      label="기능 권한 ID"
                      value={String(selectedPermission.functionPermissionId)}
                    />
                    <ReadonlyField
                      label="역할"
                      value={`${selectedPermission.roleName} (${selectedPermission.roleCode})`}
                    />
                    <ReadonlyField
                      label="화면"
                      value={`${selectedPermission.screenName} (${selectedPermission.screenId})`}
                    />
                    <ReadonlyField
                      label="기능구분"
                      value={`${selectedPermission.actionName} (${selectedPermission.actionCode})`}
                    />
                    <ReadonlyField
                      label="허용여부"
                      value={selectedPermission.allowed ? "허용" : "차단"}
                    />
                    <p className="border-2 border-black bg-muted p-3 text-xs leading-relaxed">
                      역할·화면·기능구분은 생명주기 식별자라 읽기 전용입니다.
                      저장 CTA는 선택한 action_code의 allowed 값만 변경하고
                      감사로그를 기록합니다.
                    </p>
                    {fieldErrors.id ? (
                      <p className="text-xs font-bold text-destructive">
                        {fieldErrors.id}
                      </p>
                    ) : null}
                    <button
                      className="h-12 w-full rounded border-2 border-black bg-primary px-4 font-head font-black shadow-hard transition-all duration-200 hover:-translate-x-0.5 hover:-translate-y-0.5 hover:bg-primary-hover hover:shadow-hard-lg active:translate-x-1 active:translate-y-1 active:shadow-none disabled:opacity-60"
                      type="button"
                      onClick={() => void saveSelectedFunctionPermission()}
                      disabled={saving || !user?.roles.includes("R09")}
                    >
                      {saving ? "저장 중..." : "저장/실행 전 확인"}
                    </button>
                  </div>
                ) : (
                  <div className="p-4 text-sm">
                    행을 선택하면 상세 모달 영역이 표시됩니다.
                  </div>
                )}
              </div>
            </aside>
          </section>
        ) : null}
      </section>
    </AppShell>
  );
}

function MenuManagementPage() {
  const [user, setUser] = useState<AuthenticatedUser | null>(null);
  const [menus, setMenus] = useState<MenuListItem[]>([]);
  const [selectedMenuId, setSelectedMenuId] = useState<string>("");
  const [query, setQuery] = useState("");
  const [parentFilter, setParentFilter] = useState("M-SYSTEM");
  const [screenFilter, setScreenFilter] = useState("");
  const [size, setSize] = useState(20);
  const [state, setState] = useState<ViewState>("loading");
  const [message, setMessage] = useState("");
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});
  const [saving, setSaving] = useState(false);

  const selectedMenu = useMemo(
    () => menus.find((item) => item.menuId === selectedMenuId) ?? null,
    [selectedMenuId, menus],
  );

  useEffect(() => {
    requestJson<AuthenticatedUser>("/api/auth/session")
      .then((sessionUser) => {
        setUser(sessionUser);
        return loadMenus("", parentFilter, "", size);
      })
      .catch((caught) => {
        const error = caught as Error & { status?: number };
        setState(error.status === 403 ? "permission" : "error");
        setMessage(error.message);
      });
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  async function loadMenus(
    nextQuery = query,
    nextParent = parentFilter,
    nextScreen = screenFilter,
    nextSize = size,
  ) {
    setState("loading");
    setMessage("");
    const filterParts = [
      nextParent ? `parentMenuId=${nextParent}` : "",
      nextScreen ? `screenId=${nextScreen}` : "",
    ].filter(Boolean);
    const params = new URLSearchParams({
      page: "1",
      size: String(nextSize),
      sort: "displayOrder",
    });
    if (nextQuery.trim()) {
      params.set("q", nextQuery.trim());
    }
    if (filterParts.length > 0) {
      params.set("filter", filterParts.join(";"));
    }
    try {
      const body = await requestJson<MenuListResponse>(
        `/api/admin/menus?${params.toString()}`,
      );
      setMenus(body.items);
      setSelectedMenuId(body.items[0]?.menuId ?? "");
      setState(body.items.length === 0 ? "empty" : "success");
      setMessage(`총 ${body.totalCount}건 조회되었습니다.`);
    } catch (caught) {
      const error = caught as Error & { apiError?: ApiError; status?: number };
      setFieldErrors(error.apiError?.fields ?? {});
      setState(error.status === 403 ? "permission" : "error");
      setMessage(error.message);
    }
  }

  function nextDisplayOrder(item: MenuListItem) {
    return item.displayOrder >= 9999
      ? item.displayOrder
      : item.displayOrder + 1;
  }

  async function saveSelectedMenu() {
    if (!selectedMenu) {
      setMessage("저장할 메뉴를 먼저 선택하세요.");
      return;
    }
    const nextOrder = nextDisplayOrder(selectedMenu);
    const confirmed = window.confirm(
      `${selectedMenu.menuName} 표시 순서를 ${nextOrder}(으)로 변경하시겠습니까? 메뉴 ID와 화면 ID는 변경되지 않습니다.`,
    );
    if (!confirmed) {
      return;
    }
    setSaving(true);
    setFieldErrors({});
    try {
      const result = await requestJson<{
        menuId: string;
        menuName: string;
        screenId: string;
        url: string;
        displayOrder: number;
        message: string;
      }>("/api/admin/menus", {
        method: "POST",
        body: JSON.stringify({
          id: selectedMenu.menuId,
          parentMenuId: selectedMenu.parentMenuId ?? "",
          menuName: selectedMenu.menuName,
          screenId: selectedMenu.screenId,
          url: selectedMenu.url,
          displayOrder: nextOrder,
          reason: "메뉴 관리 화면 저장 CTA",
        }),
      });
      setMessage(result.message);
      await loadMenus();
      setSelectedMenuId(result.menuId);
    } catch (caught) {
      const error = caught as Error & { apiError?: ApiError; status?: number };
      setFieldErrors(error.apiError?.fields ?? {});
      setState(error.status === 403 ? "permission" : "error");
      setMessage(error.message);
    } finally {
      setSaving(false);
    }
  }

  if (!user && state === "error") {
    return (
      <AppShell>
        <LoginPanel
          onLogin={(nextUser) => {
            setUser(nextUser);
            void loadMenus();
          }}
        />
      </AppShell>
    );
  }

  return (
    <AppShell>
      <section className="space-y-7 sm:space-y-8 lg:space-y-9">
        <div className="flex flex-col gap-4 lg:flex-row lg:items-end lg:justify-between">
          <div>
            <div className="mb-3 inline-flex border-2 border-black bg-primary px-2.5 py-1 font-head text-xs font-black uppercase tracking-[0.14em] shadow-hard">
              SCR-MENU-MGMT
            </div>
            <h1 className="font-head text-4xl font-black uppercase leading-none tracking-tight sm:text-5xl">
              메뉴 관리
            </h1>
            <p className="mt-4 max-w-3xl text-sm leading-relaxed text-muted-foreground sm:text-base">
              공통기능 25개 화면의 메뉴 트리와 라우트를 관리합니다. 메뉴 ID와
              화면 ID는 생명주기 식별자로 읽기 전용이며 저장 CTA는 표시 순서와
              화면 표시 정보를 서버에 반영합니다.
            </p>
          </div>
          <div className="rounded border-2 border-black bg-card px-4 py-3 font-head text-sm font-black shadow-hard">
            {user
              ? `${user.userId} / ${user.roles.join(", ")}`
              : "세션 확인 중"}
          </div>
        </div>

        <form
          className="grid grid-cols-1 gap-4 border-2 border-black bg-card p-4 shadow-hard sm:grid-cols-[minmax(0,1fr)_11rem_12rem_9rem_9rem] sm:p-5"
          onSubmit={(event) => {
            event.preventDefault();
            void loadMenus();
          }}
        >
          <label className="block">
            <span className="mb-2 block font-head text-sm font-bold">
              통합 검색
            </span>
            <input
              className="h-12 w-full rounded border-2 border-black bg-white px-4 py-2 shadow-hard"
              placeholder="메뉴 ID, 메뉴명, 화면 ID, URL"
              value={query}
              onChange={(event) => setQuery(event.target.value)}
            />
            {fieldErrors.q ? (
              <span className="mt-1.5 block text-xs text-destructive">
                {fieldErrors.q}
              </span>
            ) : null}
          </label>
          <label className="block">
            <span className="mb-2 block font-head text-sm font-bold">
              상위 메뉴
            </span>
            <select
              className="h-12 w-full rounded border-2 border-black bg-white px-3 shadow-hard"
              value={parentFilter}
              onChange={(event) => setParentFilter(event.target.value)}
            >
              <option value="">전체</option>
              <option value="M-SYSTEM">시스템 관리</option>
              <option value="M-SECURITY">보안·감사 관리</option>
              <option value="M-FILE">파일·데이터 관리</option>
              <option value="M-OPERATIONS">시스템 운영 관리</option>
            </select>
          </label>
          <label className="block">
            <span className="mb-2 block font-head text-sm font-bold">
              화면 ID
            </span>
            <input
              className="h-12 w-full rounded border-2 border-black bg-white px-4 py-2 shadow-hard"
              placeholder="SCR-MENU-MGMT"
              value={screenFilter}
              onChange={(event) => setScreenFilter(event.target.value)}
            />
          </label>
          <label className="block">
            <span className="mb-2 block font-head text-sm font-bold">
              표시 건수
            </span>
            <select
              className="h-12 w-full rounded border-2 border-black bg-white px-3 shadow-hard"
              value={size}
              onChange={(event) => setSize(Number(event.target.value))}
            >
              {sizeOptions.map((option) => (
                <option key={option} value={option}>
                  {option}건
                </option>
              ))}
            </select>
          </label>
          <div className="flex items-end gap-2">
            <button
              className="h-12 flex-1 rounded border-2 border-black bg-primary px-4 font-head font-black shadow-hard transition-all duration-200 hover:-translate-x-0.5 hover:-translate-y-0.5 hover:bg-primary-hover hover:shadow-hard-lg active:translate-x-1 active:translate-y-1 active:shadow-none"
              type="submit"
            >
              검색
            </button>
            <button
              className="h-12 rounded border-2 border-black bg-card px-4 font-head font-black shadow-hard"
              type="button"
              onClick={() => {
                setQuery("");
                setParentFilter("M-SYSTEM");
                setScreenFilter("");
                void loadMenus("", "M-SYSTEM", "", size);
              }}
            >
              초기화
            </button>
          </div>
        </form>

        {message ? (
          <div
            role="status"
            className={`border-2 border-black px-4 py-3 font-bold shadow-hard ${state === "permission" || state === "error" ? "bg-destructive text-white" : "bg-accent text-black"}`}
          >
            {message}
          </div>
        ) : null}
        {state === "loading" ? (
          <StateCard title="LOADING" body="메뉴 트리를 불러오는 중입니다." />
        ) : null}
        {state === "empty" ? (
          <StateCard
            title="EMPTY"
            body="조건에 맞는 메뉴가 없습니다. 검색 조건을 초기화해 보세요."
          />
        ) : null}
        {state === "permission" ? (
          <StateCard
            title="PERMISSION DENIED"
            body="R09 시스템관리자 권한이 필요합니다. 직접 API 요청도 서버에서 403으로 차단됩니다."
          />
        ) : null}

        {state === "success" || menus.length > 0 ? (
          <section className="grid grid-cols-1 gap-4 lg:grid-cols-[minmax(0,1fr)_22rem]">
            <div className="overflow-hidden border-2 border-black bg-card shadow-hard">
              <div className="border-b-2 border-black bg-muted px-4 py-3 font-head font-black">
                menus 목록
              </div>
              <div className="overflow-x-auto">
                <table className="w-full min-w-[62rem] border-collapse text-left text-sm">
                  <thead className="bg-primary font-head text-xs uppercase">
                    <tr>
                      {[
                        "메뉴 ID",
                        "상위 메뉴",
                        "메뉴명",
                        "화면 ID",
                        "URL",
                        "순서",
                        "권한",
                      ].map((header) => (
                        <th
                          key={header}
                          className="border-b-2 border-black px-3 py-2"
                        >
                          {header}
                        </th>
                      ))}
                    </tr>
                  </thead>
                  <tbody>
                    {menus.map((item) => (
                      <tr
                        key={item.menuId}
                        className={`cursor-pointer border-b-2 border-black transition-colors hover:bg-accent ${selectedMenuId === item.menuId ? "bg-accent" : "bg-card"}`}
                        onClick={() => setSelectedMenuId(item.menuId)}
                      >
                        <td className="px-3 py-2 font-mono">{item.menuId}</td>
                        <td className="px-3 py-2">
                          {item.parentMenuName ?? item.parentMenuId ?? "대메뉴"}
                        </td>
                        <td className="px-3 py-2 font-bold">{item.menuName}</td>
                        <td className="px-3 py-2 font-mono">{item.screenId}</td>
                        <td className="px-3 py-2 font-mono">{item.url}</td>
                        <td className="px-3 py-2">
                          <span className="border-2 border-black bg-primary px-2 py-1 font-head text-xs font-black">
                            {item.displayOrder}
                          </span>
                        </td>
                        <td className="px-3 py-2">
                          허용 {item.permissionCount}건 / 하위 {item.childCount}
                          건
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>

            <aside className="relative">
              <div className="absolute inset-2 border-2 border-black bg-primary" />
              <div className="relative border-2 border-black bg-card shadow-hard">
                <div className="border-b-2 border-black bg-muted px-4 py-3 font-head font-black">
                  상세 / 저장 전 확인
                </div>
                {selectedMenu ? (
                  <div className="space-y-3 p-4 text-sm">
                    <ReadonlyField
                      label="메뉴 ID"
                      value={selectedMenu.menuId}
                    />
                    <ReadonlyField
                      label="상위 메뉴"
                      value={
                        selectedMenu.parentMenuName ??
                        selectedMenu.parentMenuId ??
                        "대메뉴"
                      }
                    />
                    <ReadonlyField
                      label="메뉴명"
                      value={selectedMenu.menuName}
                    />
                    <ReadonlyField
                      label="화면 ID"
                      value={selectedMenu.screenId}
                    />
                    <ReadonlyField label="URL" value={selectedMenu.url} />
                    <ReadonlyField
                      label="표시 순서"
                      value={String(selectedMenu.displayOrder)}
                    />
                    <p className="border-2 border-black bg-muted p-3 text-xs leading-relaxed">
                      메뉴 ID와 화면 ID는 생명주기 식별자라 읽기 전용입니다.
                      저장 CTA는 parent_menu_id/menu_name/url/display_order를
                      전달하고 감사로그를 기록합니다.
                    </p>
                    {fieldErrors.id ? (
                      <p className="text-xs font-bold text-destructive">
                        {fieldErrors.id}
                      </p>
                    ) : null}
                    <button
                      className="h-12 w-full rounded border-2 border-black bg-primary px-4 font-head font-black shadow-hard transition-all duration-200 hover:-translate-x-0.5 hover:-translate-y-0.5 hover:bg-primary-hover hover:shadow-hard-lg active:translate-x-1 active:translate-y-1 active:shadow-none disabled:opacity-60"
                      type="button"
                      onClick={() => void saveSelectedMenu()}
                      disabled={saving || !user?.roles.includes("R09")}
                    >
                      {saving ? "저장 중..." : "저장/실행 전 확인"}
                    </button>
                  </div>
                ) : (
                  <div className="p-4 text-sm">
                    행을 선택하면 상세 모달 영역이 표시됩니다.
                  </div>
                )}
              </div>
            </aside>
          </section>
        ) : null}
      </section>
    </AppShell>
  );
}

function CodeGroupManagementPage() {
  const [user, setUser] = useState<AuthenticatedUser | null>(null);
  const [codeGroups, setCodeGroups] = useState<CodeGroupListItem[]>([]);
  const [selectedGroupId, setSelectedGroupId] = useState<string>("");
  const [query, setQuery] = useState("");
  const [enabledFilter, setEnabledFilter] = useState("");
  const [departmentFilter, setDepartmentFilter] = useState("");
  const [size, setSize] = useState(20);
  const [state, setState] = useState<ViewState>("loading");
  const [message, setMessage] = useState("");
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});
  const [saving, setSaving] = useState(false);

  const selectedCodeGroup = useMemo(
    () => codeGroups.find((item) => item.groupId === selectedGroupId) ?? null,
    [selectedGroupId, codeGroups],
  );

  useEffect(() => {
    requestJson<AuthenticatedUser>("/api/auth/session")
      .then((sessionUser) => {
        setUser(sessionUser);
        return loadCodeGroups("", "", "", size);
      })
      .catch((caught) => {
        const error = caught as Error & { status?: number };
        setState(error.status === 403 ? "permission" : "error");
        setMessage(error.message);
      });
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  async function loadCodeGroups(
    nextQuery = query,
    nextEnabled = enabledFilter,
    nextDepartment = departmentFilter,
    nextSize = size,
  ) {
    setState("loading");
    setMessage("");
    const filterParts = [
      nextEnabled ? `enabled=${nextEnabled}` : "",
      nextDepartment ? `managingDepartment=${nextDepartment}` : "",
    ].filter(Boolean);
    const params = new URLSearchParams({
      page: "1",
      size: String(nextSize),
      sort: "groupId",
    });
    if (nextQuery.trim()) {
      params.set("q", nextQuery.trim());
    }
    if (filterParts.length > 0) {
      params.set("filter", filterParts.join(";"));
    }
    try {
      const body = await requestJson<CodeGroupListResponse>(
        `/api/admin/code-groups?${params.toString()}`,
      );
      setCodeGroups(body.items);
      setSelectedGroupId(body.items[0]?.groupId ?? "");
      setState(body.items.length === 0 ? "empty" : "success");
      setMessage(`총 ${body.totalCount}건 조회되었습니다.`);
    } catch (caught) {
      const error = caught as Error & { apiError?: ApiError; status?: number };
      setFieldErrors(error.apiError?.fields ?? {});
      setState(error.status === 403 ? "permission" : "error");
      setMessage(error.message);
    }
  }

  async function saveSelectedCodeGroup() {
    if (!selectedCodeGroup) {
      setMessage("저장할 코드그룹을 먼저 선택하세요.");
      return;
    }
    const nextEnabled = !selectedCodeGroup.enabled;
    const confirmed = window.confirm(
      `${selectedCodeGroup.groupName} 사용여부를 ${nextEnabled ? "사용" : "미사용"}(으)로 변경하시겠습니까? 그룹 ID는 변경되지 않습니다.`,
    );
    if (!confirmed) {
      return;
    }
    setSaving(true);
    setFieldErrors({});
    try {
      const result = await requestJson<{
        groupId: string;
        groupName: string;
        managingDepartment?: string;
        enabled: boolean;
        message: string;
      }>("/api/admin/code-groups", {
        method: "POST",
        body: JSON.stringify({
          id: selectedCodeGroup.groupId,
          groupName: selectedCodeGroup.groupName,
          description: selectedCodeGroup.description ?? "",
          managingDepartment: selectedCodeGroup.managingDepartment ?? "",
          enabled: nextEnabled,
          reason: "코드그룹 관리 화면 저장 CTA",
        }),
      });
      setMessage(result.message);
      await loadCodeGroups();
      setSelectedGroupId(result.groupId);
    } catch (caught) {
      const error = caught as Error & { apiError?: ApiError; status?: number };
      setFieldErrors(error.apiError?.fields ?? {});
      setState(error.status === 403 ? "permission" : "error");
      setMessage(error.message);
    } finally {
      setSaving(false);
    }
  }

  if (!user && state === "error") {
    return (
      <AppShell>
        <LoginPanel
          onLogin={(nextUser) => {
            setUser(nextUser);
            void loadCodeGroups();
          }}
        />
      </AppShell>
    );
  }

  return (
    <AppShell>
      <section className="space-y-7 sm:space-y-8 lg:space-y-9">
        <div className="flex flex-col gap-4 lg:flex-row lg:items-end lg:justify-between">
          <div>
            <div className="mb-3 inline-flex border-2 border-black bg-primary px-2.5 py-1 font-head text-xs font-black uppercase tracking-[0.14em] shadow-hard">
              SCR-CODE-GROUP
            </div>
            <h1 className="font-head text-4xl font-black uppercase leading-none tracking-tight sm:text-5xl">
              코드그룹 관리
            </h1>
            <p className="mt-4 max-w-3xl text-sm leading-relaxed text-muted-foreground sm:text-base">
              평가영역·처리상태·인증구분 등 공통코드 묶음의 그룹 ID, 명칭, 설명,
              관리부서를 관리합니다. 코드값·코드명·정렬순서는 상세코드
              관리에서만 변경합니다.
            </p>
          </div>
          <div className="rounded border-2 border-black bg-card px-4 py-3 font-head text-sm font-black shadow-hard">
            {user
              ? `${user.userId} / ${user.roles.join(", ")}`
              : "세션 확인 중"}
          </div>
        </div>

        <form
          className="grid grid-cols-1 gap-4 border-2 border-black bg-card p-4 shadow-hard sm:grid-cols-[minmax(0,1fr)_9rem_11rem_9rem_9rem] sm:p-5"
          onSubmit={(event) => {
            event.preventDefault();
            void loadCodeGroups();
          }}
        >
          <label className="block">
            <span className="mb-2 block font-head text-sm font-bold">
              통합 검색
            </span>
            <input
              className="h-12 w-full rounded border-2 border-black bg-white px-4 py-2 shadow-hard"
              placeholder="그룹 ID, 명칭, 설명, 관리부서"
              value={query}
              onChange={(event) => setQuery(event.target.value)}
            />
            {fieldErrors.q ? (
              <span className="mt-1.5 block text-xs text-destructive">
                {fieldErrors.q}
              </span>
            ) : null}
          </label>
          <label className="block">
            <span className="mb-2 block font-head text-sm font-bold">
              사용여부
            </span>
            <select
              className="h-12 w-full rounded border-2 border-black bg-white px-3 shadow-hard"
              value={enabledFilter}
              onChange={(event) => setEnabledFilter(event.target.value)}
            >
              <option value="">전체</option>
              <option value="true">사용</option>
              <option value="false">미사용</option>
            </select>
          </label>
          <label className="block">
            <span className="mb-2 block font-head text-sm font-bold">
              관리부서
            </span>
            <input
              className="h-12 w-full rounded border-2 border-black bg-white px-4 py-2 shadow-hard"
              placeholder="교수지원과"
              value={departmentFilter}
              onChange={(event) => setDepartmentFilter(event.target.value)}
            />
          </label>
          <label className="block">
            <span className="mb-2 block font-head text-sm font-bold">
              표시 건수
            </span>
            <select
              className="h-12 w-full rounded border-2 border-black bg-white px-3 shadow-hard"
              value={size}
              onChange={(event) => setSize(Number(event.target.value))}
            >
              {sizeOptions.map((option) => (
                <option key={option} value={option}>
                  {option}건
                </option>
              ))}
            </select>
          </label>
          <div className="flex items-end gap-2">
            <button
              className="h-12 flex-1 rounded border-2 border-black bg-primary px-4 font-head font-black shadow-hard transition-all duration-200 hover:-translate-x-0.5 hover:-translate-y-0.5 hover:bg-primary-hover hover:shadow-hard-lg active:translate-x-1 active:translate-y-1 active:shadow-none"
              type="submit"
            >
              검색
            </button>
            <button
              className="h-12 rounded border-2 border-black bg-card px-4 font-head font-black shadow-hard"
              type="button"
              onClick={() => {
                setQuery("");
                setEnabledFilter("");
                setDepartmentFilter("");
                void loadCodeGroups("", "", "", size);
              }}
            >
              초기화
            </button>
          </div>
        </form>

        {message ? (
          <div
            role="status"
            className={`border-2 border-black px-4 py-3 font-bold shadow-hard ${state === "permission" || state === "error" ? "bg-destructive text-white" : "bg-accent text-black"}`}
          >
            {message}
          </div>
        ) : null}
        {state === "loading" ? (
          <StateCard
            title="LOADING"
            body="코드그룹 목록을 불러오는 중입니다."
          />
        ) : null}
        {state === "empty" ? (
          <StateCard
            title="EMPTY"
            body="조건에 맞는 코드그룹이 없습니다. 검색 조건을 초기화해 보세요."
          />
        ) : null}
        {state === "permission" ? (
          <StateCard
            title="PERMISSION DENIED"
            body="R09 시스템관리자 권한이 필요합니다. 직접 API 요청도 서버에서 403으로 차단됩니다."
          />
        ) : null}

        {state === "success" || codeGroups.length > 0 ? (
          <section className="grid grid-cols-1 gap-4 lg:grid-cols-[minmax(0,1fr)_22rem]">
            <div className="overflow-hidden border-2 border-black bg-card shadow-hard">
              <div className="border-b-2 border-black bg-muted px-4 py-3 font-head font-black">
                code_groups 목록
              </div>
              <div className="overflow-x-auto">
                <table className="w-full min-w-[58rem] border-collapse text-left text-sm">
                  <thead className="bg-primary font-head text-xs uppercase">
                    <tr>
                      {[
                        "그룹 ID",
                        "코드그룹명",
                        "관리부서",
                        "상세코드",
                        "사용여부",
                        "설명",
                      ].map((header) => (
                        <th
                          key={header}
                          className="border-b-2 border-black px-3 py-2"
                        >
                          {header}
                        </th>
                      ))}
                    </tr>
                  </thead>
                  <tbody>
                    {codeGroups.map((item) => (
                      <tr
                        key={item.groupId}
                        className={`cursor-pointer border-b-2 border-black transition-colors hover:bg-accent ${selectedGroupId === item.groupId ? "bg-accent" : "bg-card"}`}
                        onClick={() => setSelectedGroupId(item.groupId)}
                      >
                        <td className="px-3 py-2 font-mono">{item.groupId}</td>
                        <td className="px-3 py-2 font-bold">
                          {item.groupName}
                        </td>
                        <td className="px-3 py-2">
                          {item.managingDepartment ?? "-"}
                        </td>
                        <td className="px-3 py-2">
                          전체 {item.detailCount}건 / 활성{" "}
                          {item.enabledDetailCount}건
                        </td>
                        <td className="px-3 py-2">
                          <span
                            className={`border-2 border-black px-2 py-1 font-head text-xs font-black ${item.enabled ? "bg-primary" : "bg-muted"}`}
                          >
                            {item.enabled ? "사용" : "미사용"}
                          </span>
                        </td>
                        <td className="px-3 py-2">{item.description ?? "-"}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>

            <aside className="relative">
              <div className="absolute inset-2 border-2 border-black bg-primary" />
              <div className="relative border-2 border-black bg-card shadow-hard">
                <div className="border-b-2 border-black bg-muted px-4 py-3 font-head font-black">
                  상세 / 저장 전 확인
                </div>
                {selectedCodeGroup ? (
                  <div className="space-y-3 p-4 text-sm">
                    <ReadonlyField
                      label="코드그룹 ID"
                      value={selectedCodeGroup.groupId}
                    />
                    <ReadonlyField
                      label="코드그룹명"
                      value={selectedCodeGroup.groupName}
                    />
                    <ReadonlyField
                      label="관리부서"
                      value={selectedCodeGroup.managingDepartment ?? "-"}
                    />
                    <ReadonlyField
                      label="사용여부"
                      value={selectedCodeGroup.enabled ? "사용" : "미사용"}
                    />
                    <ReadonlyField
                      label="상세코드"
                      value={`전체 ${selectedCodeGroup.detailCount}건 / 활성 ${selectedCodeGroup.enabledDetailCount}건`}
                    />
                    <ReadonlyField
                      label="상세코드 변경"
                      value={selectedCodeGroup.detailManagementRule}
                    />
                    <p className="border-2 border-black bg-muted p-3 text-xs leading-relaxed">
                      그룹 ID는 생명주기 식별자라 읽기 전용입니다. 저장 CTA는
                      group_name/description/managing_department/enabled만
                      전달하고 감사로그를 기록합니다.
                    </p>
                    {fieldErrors.id ? (
                      <p className="text-xs font-bold text-destructive">
                        {fieldErrors.id}
                      </p>
                    ) : null}
                    <button
                      className="h-12 w-full rounded border-2 border-black bg-primary px-4 font-head font-black shadow-hard transition-all duration-200 hover:-translate-x-0.5 hover:-translate-y-0.5 hover:bg-primary-hover hover:shadow-hard-lg active:translate-x-1 active:translate-y-1 active:shadow-none disabled:opacity-60"
                      type="button"
                      onClick={() => void saveSelectedCodeGroup()}
                      disabled={saving || !user?.roles.includes("R09")}
                    >
                      {saving ? "저장 중..." : "저장/실행 전 확인"}
                    </button>
                  </div>
                ) : (
                  <div className="p-4 text-sm">
                    행을 선택하면 상세 모달 영역이 표시됩니다.
                  </div>
                )}
              </div>
            </aside>
          </section>
        ) : null}
      </section>
    </AppShell>
  );
}

function CodeDetailManagementPage() {
  const [user, setUser] = useState<AuthenticatedUser | null>(null);
  const [codeDetails, setCodeDetails] = useState<CodeDetailListItem[]>([]);
  const [selectedCodeDetailId, setSelectedCodeDetailId] = useState<
    number | null
  >(null);
  const [query, setQuery] = useState("");
  const [groupFilter, setGroupFilter] = useState("");
  const [activeFilter, setActiveFilter] = useState("");
  const [size, setSize] = useState(20);
  const [state, setState] = useState<ViewState>("loading");
  const [message, setMessage] = useState("");
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});
  const [saving, setSaving] = useState(false);

  const selectedCodeDetail = useMemo(
    () =>
      codeDetails.find((item) => item.codeDetailId === selectedCodeDetailId) ??
      null,
    [selectedCodeDetailId, codeDetails],
  );

  useEffect(() => {
    requestJson<AuthenticatedUser>("/api/auth/session")
      .then((sessionUser) => {
        setUser(sessionUser);
        return loadCodeDetails("", "", "", size);
      })
      .catch((caught) => {
        const error = caught as Error & { status?: number };
        setState(error.status === 403 ? "permission" : "error");
        setMessage(error.message);
      });
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  async function loadCodeDetails(
    nextQuery = query,
    nextGroup = groupFilter,
    nextActive = activeFilter,
    nextSize = size,
  ) {
    setState("loading");
    setMessage("");
    const filterParts = [
      nextGroup ? `groupId=${nextGroup}` : "",
      nextActive ? `active=${nextActive}` : "",
    ].filter(Boolean);
    const params = new URLSearchParams({
      page: "1",
      size: String(nextSize),
      sort: "displayOrder",
    });
    if (nextQuery.trim()) {
      params.set("q", nextQuery.trim());
    }
    if (filterParts.length > 0) {
      params.set("filter", filterParts.join(";"));
    }
    try {
      const body = await requestJson<CodeDetailListResponse>(
        `/api/admin/code-details?${params.toString()}`,
      );
      setCodeDetails(body.items);
      setSelectedCodeDetailId(body.items[0]?.codeDetailId ?? null);
      setState(body.items.length === 0 ? "empty" : "success");
      setMessage(`총 ${body.totalCount}건 조회되었습니다.`);
    } catch (caught) {
      const error = caught as Error & { apiError?: ApiError; status?: number };
      setFieldErrors(error.apiError?.fields ?? {});
      setState(error.status === 403 ? "permission" : "error");
      setMessage(error.message);
    }
  }

  async function saveSelectedCodeDetail() {
    if (!selectedCodeDetail) {
      setMessage("저장할 상세코드를 먼저 선택하세요.");
      return;
    }
    const nextDisplayOrder =
      selectedCodeDetail.displayOrder >= 9999
        ? selectedCodeDetail.displayOrder
        : selectedCodeDetail.displayOrder + 10;
    const confirmed = window.confirm(
      `${selectedCodeDetail.codeName} 정렬순서를 ${nextDisplayOrder}(으)로 변경하시겠습니까? 그룹 ID와 코드값은 변경되지 않습니다.`,
    );
    if (!confirmed) {
      return;
    }
    setSaving(true);
    setFieldErrors({});
    try {
      const result = await requestJson<{
        codeDetailId: number;
        groupId: string;
        codeValue: string;
        codeName: string;
        displayOrder: number;
        active: boolean;
        message: string;
      }>("/api/admin/code-details", {
        method: "POST",
        body: JSON.stringify({
          id: String(selectedCodeDetail.codeDetailId),
          codeName: selectedCodeDetail.codeName,
          parentCodeValue: selectedCodeDetail.parentCodeValue ?? "",
          displayOrder: nextDisplayOrder,
          reason: "상세코드 관리 화면 저장 CTA",
        }),
      });
      setMessage(result.message);
      await loadCodeDetails();
      setSelectedCodeDetailId(result.codeDetailId);
    } catch (caught) {
      const error = caught as Error & { apiError?: ApiError; status?: number };
      setFieldErrors(error.apiError?.fields ?? {});
      setState(error.status === 403 ? "permission" : "error");
      setMessage(error.message);
    } finally {
      setSaving(false);
    }
  }

  if (!user && state === "error") {
    return (
      <AppShell>
        <LoginPanel
          onLogin={(nextUser) => {
            setUser(nextUser);
            void loadCodeDetails();
          }}
        />
      </AppShell>
    );
  }

  return (
    <AppShell>
      <section className="space-y-7 sm:space-y-8 lg:space-y-9">
        <div className="flex flex-col gap-4 lg:flex-row lg:items-end lg:justify-between">
          <div>
            <div className="mb-3 inline-flex border-2 border-black bg-primary px-2.5 py-1 font-head text-xs font-black uppercase tracking-[0.14em] shadow-hard">
              SCR-CODE-DETAIL
            </div>
            <h1 className="font-head text-4xl font-black uppercase leading-none tracking-tight sm:text-5xl">
              상세코드 관리
            </h1>
            <p className="mt-4 max-w-3xl text-sm leading-relaxed text-muted-foreground sm:text-base">
              공통코드 그룹별 코드값·코드명·상위코드·정렬순서를 조회하고
              관리합니다. 그룹 ID와 코드값은 생명주기 식별자로 읽기 전용이며,
              정렬순서가 0 이하면 비활성 표시로 해석합니다.
            </p>
          </div>
          <div className="rounded border-2 border-black bg-card px-4 py-3 font-head text-sm font-black shadow-hard">
            {user
              ? `${user.userId} / ${user.roles.join(", ")}`
              : "세션 확인 중"}
          </div>
        </div>

        <form
          className="grid grid-cols-1 gap-4 border-2 border-black bg-card p-4 shadow-hard sm:grid-cols-[minmax(0,1fr)_11rem_9rem_9rem_9rem] sm:p-5"
          onSubmit={(event) => {
            event.preventDefault();
            void loadCodeDetails();
          }}
        >
          <label className="block">
            <span className="mb-2 block font-head text-sm font-bold">
              통합 검색
            </span>
            <input
              className="h-12 w-full rounded border-2 border-black bg-white px-4 py-2 shadow-hard"
              placeholder="그룹, 코드값, 코드명, 상위코드"
              value={query}
              onChange={(event) => setQuery(event.target.value)}
            />
            {fieldErrors.q ? (
              <span className="mt-1.5 block text-xs text-destructive">
                {fieldErrors.q}
              </span>
            ) : null}
          </label>
          <label className="block">
            <span className="mb-2 block font-head text-sm font-bold">
              코드그룹
            </span>
            <input
              className="h-12 w-full rounded border-2 border-black bg-white px-4 py-2 shadow-hard"
              placeholder="EVAL_AREA"
              value={groupFilter}
              onChange={(event) => setGroupFilter(event.target.value)}
            />
          </label>
          <label className="block">
            <span className="mb-2 block font-head text-sm font-bold">상태</span>
            <select
              className="h-12 w-full rounded border-2 border-black bg-white px-3 shadow-hard"
              value={activeFilter}
              onChange={(event) => setActiveFilter(event.target.value)}
            >
              <option value="">전체</option>
              <option value="true">활성</option>
              <option value="false">비활성</option>
            </select>
          </label>
          <label className="block">
            <span className="mb-2 block font-head text-sm font-bold">
              표시 건수
            </span>
            <select
              className="h-12 w-full rounded border-2 border-black bg-white px-3 shadow-hard"
              value={size}
              onChange={(event) => setSize(Number(event.target.value))}
            >
              {sizeOptions.map((option) => (
                <option key={option} value={option}>
                  {option}건
                </option>
              ))}
            </select>
          </label>
          <div className="flex items-end gap-2">
            <button
              className="h-12 flex-1 rounded border-2 border-black bg-primary px-4 font-head font-black shadow-hard transition-all duration-200 hover:-translate-x-0.5 hover:-translate-y-0.5 hover:bg-primary-hover hover:shadow-hard-lg active:translate-x-1 active:translate-y-1 active:shadow-none"
              type="submit"
            >
              검색
            </button>
            <button
              className="h-12 rounded border-2 border-black bg-card px-4 font-head font-black shadow-hard"
              type="button"
              onClick={() => {
                setQuery("");
                setGroupFilter("");
                setActiveFilter("");
                void loadCodeDetails("", "", "", size);
              }}
            >
              초기화
            </button>
          </div>
        </form>

        {message ? (
          <div
            role="status"
            className={`border-2 border-black px-4 py-3 font-bold shadow-hard ${state === "permission" || state === "error" ? "bg-destructive text-white" : "bg-accent text-black"}`}
          >
            {message}
          </div>
        ) : null}
        {state === "loading" ? (
          <StateCard
            title="LOADING"
            body="상세코드 목록을 불러오는 중입니다."
          />
        ) : null}
        {state === "empty" ? (
          <StateCard
            title="EMPTY"
            body="조건에 맞는 상세코드가 없습니다. 검색 조건을 초기화해 보세요."
          />
        ) : null}
        {state === "permission" ? (
          <StateCard
            title="PERMISSION DENIED"
            body="R09 시스템관리자 권한이 필요합니다. 직접 API 요청도 서버에서 403으로 차단됩니다."
          />
        ) : null}

        {state === "success" || codeDetails.length > 0 ? (
          <section className="grid grid-cols-1 gap-4 lg:grid-cols-[minmax(0,1fr)_22rem]">
            <div className="overflow-hidden border-2 border-black bg-card shadow-hard">
              <div className="border-b-2 border-black bg-muted px-4 py-3 font-head font-black">
                code_details 목록
              </div>
              <div className="overflow-x-auto">
                <table className="w-full min-w-[62rem] border-collapse text-left text-sm">
                  <thead className="bg-primary font-head text-xs uppercase">
                    <tr>
                      {[
                        "ID",
                        "그룹",
                        "코드값",
                        "코드명",
                        "상위코드",
                        "정렬순서",
                        "상태",
                      ].map((header) => (
                        <th
                          key={header}
                          className="border-b-2 border-black px-3 py-2"
                        >
                          {header}
                        </th>
                      ))}
                    </tr>
                  </thead>
                  <tbody>
                    {codeDetails.map((item) => (
                      <tr
                        key={item.codeDetailId}
                        className={`cursor-pointer border-b-2 border-black transition-colors hover:bg-accent ${selectedCodeDetailId === item.codeDetailId ? "bg-accent" : "bg-card"}`}
                        onClick={() =>
                          setSelectedCodeDetailId(item.codeDetailId)
                        }
                      >
                        <td className="px-3 py-2 font-mono">
                          {item.codeDetailId}
                        </td>
                        <td className="px-3 py-2">
                          <span className="font-mono">{item.groupId}</span> /{" "}
                          <span>{item.groupName}</span>
                        </td>
                        <td className="px-3 py-2 font-mono">
                          {item.codeValue}
                        </td>
                        <td className="px-3 py-2 font-bold">{item.codeName}</td>
                        <td className="px-3 py-2">
                          {item.parentCodeName ?? item.parentCodeValue ?? "-"}
                        </td>
                        <td className="px-3 py-2">
                          <span className="border-2 border-black bg-primary px-2 py-1 font-head text-xs font-black">
                            {item.displayOrder}
                          </span>
                        </td>
                        <td className="px-3 py-2">
                          {item.active ? "활성" : "비활성"}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>

            <aside className="relative">
              <div className="absolute inset-2 border-2 border-black bg-primary" />
              <div className="relative border-2 border-black bg-card shadow-hard">
                <div className="border-b-2 border-black bg-muted px-4 py-3 font-head font-black">
                  상세 / 저장 전 확인
                </div>
                {selectedCodeDetail ? (
                  <div className="space-y-3 p-4 text-sm">
                    <ReadonlyField
                      label="상세코드 ID"
                      value={String(selectedCodeDetail.codeDetailId)}
                    />
                    <ReadonlyField
                      label="코드그룹"
                      value={`${selectedCodeDetail.groupName} (${selectedCodeDetail.groupId})`}
                    />
                    <ReadonlyField
                      label="코드값"
                      value={selectedCodeDetail.codeValue}
                    />
                    <ReadonlyField
                      label="코드명"
                      value={selectedCodeDetail.codeName}
                    />
                    <ReadonlyField
                      label="상위코드"
                      value={
                        selectedCodeDetail.parentCodeName ??
                        selectedCodeDetail.parentCodeValue ??
                        "-"
                      }
                    />
                    <ReadonlyField
                      label="정렬순서"
                      value={String(selectedCodeDetail.displayOrder)}
                    />
                    <ReadonlyField
                      label="상세코드 규칙"
                      value={selectedCodeDetail.detailUsageRule}
                    />
                    <p className="border-2 border-black bg-muted p-3 text-xs leading-relaxed">
                      그룹 ID와 코드값은 생명주기 식별자라 읽기 전용입니다. 저장
                      CTA는 code_name/parent_code_value/display_order만 전달하고
                      감사로그를 기록합니다.
                    </p>
                    {fieldErrors.id ? (
                      <p className="text-xs font-bold text-destructive">
                        {fieldErrors.id}
                      </p>
                    ) : null}
                    <button
                      className="h-12 w-full rounded border-2 border-black bg-primary px-4 font-head font-black shadow-hard transition-all duration-200 hover:-translate-x-0.5 hover:-translate-y-0.5 hover:bg-primary-hover hover:shadow-hard-lg active:translate-x-1 active:translate-y-1 active:shadow-none disabled:opacity-60"
                      type="button"
                      onClick={() => void saveSelectedCodeDetail()}
                      disabled={saving || !user?.roles.includes("R09")}
                    >
                      {saving ? "저장 중..." : "저장/실행 전 확인"}
                    </button>
                  </div>
                ) : (
                  <div className="p-4 text-sm">
                    행을 선택하면 상세 모달 영역이 표시됩니다.
                  </div>
                )}
              </div>
            </aside>
          </section>
        ) : null}
      </section>
    </AppShell>
  );
}

function SystemConfigurationManagementPage() {
  const [user, setUser] = useState<AuthenticatedUser | null>(null);
  const [configs, setConfigs] = useState<SystemConfigurationListItem[]>([]);
  const [selectedConfigKey, setSelectedConfigKey] = useState<string | null>(
    null,
  );
  const [query, setQuery] = useState("");
  const [enabledFilter, setEnabledFilter] = useState("");
  const [size, setSize] = useState(20);
  const [state, setState] = useState<ViewState>("loading");
  const [message, setMessage] = useState("");
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});
  const [saving, setSaving] = useState(false);

  const selectedConfig = useMemo(
    () => configs.find((item) => item.configKey === selectedConfigKey) ?? null,
    [selectedConfigKey, configs],
  );

  useEffect(() => {
    requestJson<AuthenticatedUser>("/api/auth/session")
      .then((sessionUser) => {
        setUser(sessionUser);
        return loadSystemConfigurations("", "", size);
      })
      .catch((caught) => {
        const error = caught as Error & { status?: number };
        setState(error.status === 403 ? "permission" : "error");
        setMessage(error.message);
      });
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  async function loadSystemConfigurations(
    nextQuery = query,
    nextEnabled = enabledFilter,
    nextSize = size,
  ) {
    setState("loading");
    setMessage("");
    const params = new URLSearchParams({
      page: "1",
      size: String(nextSize),
      sort: "configKey",
    });
    if (nextQuery.trim()) {
      params.set("q", nextQuery.trim());
    }
    if (nextEnabled) {
      params.set("filter", `enabled=${nextEnabled}`);
    }
    try {
      const body = await requestJson<SystemConfigurationListResponse>(
        `/api/admin/system-configurations?${params.toString()}`,
      );
      setConfigs(body.items);
      setSelectedConfigKey(body.items[0]?.configKey ?? null);
      setState(body.items.length === 0 ? "empty" : "success");
      setMessage(`총 ${body.totalCount}건 조회되었습니다.`);
    } catch (caught) {
      const error = caught as Error & { apiError?: ApiError; status?: number };
      setFieldErrors(error.apiError?.fields ?? {});
      setState(error.status === 403 ? "permission" : "error");
      setMessage(error.message);
    }
  }

  function nextConfigValue(item: SystemConfigurationListItem) {
    const parsed = Number(item.configValue);
    if (Number.isNaN(parsed)) {
      return item.configValue;
    }
    const [minText, maxText] = (item.valueRange ?? "").split("-", 2);
    const max = Number(maxText);
    const min = Number(minText);
    if (!Number.isNaN(max) && parsed < max) {
      return String(parsed + 1);
    }
    if (!Number.isNaN(min)) {
      return String(min);
    }
    return String(parsed);
  }

  async function saveSelectedSystemConfiguration() {
    if (!selectedConfig) {
      setMessage("저장할 공통 환경설정을 먼저 선택하세요.");
      return;
    }
    const nextValue = nextConfigValue(selectedConfig);
    const confirmed = window.confirm(
      `${selectedConfig.configKey} 값을 ${nextValue}${selectedConfig.unit}(으)로 변경하시겠습니까? 특정 사용자나 업무별 개별값은 생성하지 않습니다.`,
    );
    if (!confirmed) {
      return;
    }
    setSaving(true);
    setFieldErrors({});
    try {
      const result = await requestJson<{
        configKey: string;
        configValue: string;
        unit: string;
        valueRange?: string | null;
        enabled: boolean;
        message: string;
      }>("/api/admin/system-configurations", {
        method: "POST",
        body: JSON.stringify({
          id: selectedConfig.configKey,
          configValue: nextValue,
          enabled: selectedConfig.enabled,
          reason: "공통 환경설정 화면 저장 CTA",
        }),
      });
      setMessage(result.message);
      await loadSystemConfigurations();
      setSelectedConfigKey(result.configKey);
    } catch (caught) {
      const error = caught as Error & { apiError?: ApiError; status?: number };
      setFieldErrors(error.apiError?.fields ?? {});
      setState(error.status === 403 ? "permission" : "error");
      setMessage(error.message);
    } finally {
      setSaving(false);
    }
  }

  if (!user && state === "error") {
    return (
      <AppShell>
        <LoginPanel
          onLogin={(nextUser) => {
            setUser(nextUser);
            void loadSystemConfigurations();
          }}
        />
      </AppShell>
    );
  }

  return (
    <AppShell>
      <section className="space-y-7 sm:space-y-8 lg:space-y-9">
        <div className="flex flex-col gap-4 lg:flex-row lg:items-end lg:justify-between">
          <div>
            <div className="mb-3 inline-flex border-2 border-black bg-primary px-2.5 py-1 font-head text-xs font-black uppercase tracking-[0.14em] shadow-hard">
              SCR-SYSTEM-CONFIG
            </div>
            <h1 className="font-head text-4xl font-black uppercase leading-none tracking-tight sm:text-5xl">
              공통 환경설정
            </h1>
            <p className="mt-4 max-w-3xl text-sm leading-relaxed text-muted-foreground sm:text-base">
              세션 유휴시간, 페이지당 조회건수, 기본 검색기간, 대량조회
              기준건수, 장시간작업 안내 기준을 전역 설정값으로 관리합니다.
              사용자·업무별 개별 환경값은 생성하지 않습니다.
            </p>
          </div>
          <div className="rounded border-2 border-black bg-card px-4 py-3 font-head text-sm font-black shadow-hard">
            {user
              ? `${user.userId} / ${user.roles.join(", ")}`
              : "세션 확인 중"}
          </div>
        </div>

        <form
          className="grid grid-cols-1 gap-4 border-2 border-black bg-card p-4 shadow-hard sm:grid-cols-[minmax(0,1fr)_10rem_9rem_9rem] sm:p-5"
          onSubmit={(event) => {
            event.preventDefault();
            void loadSystemConfigurations();
          }}
        >
          <label className="block">
            <span className="mb-2 block font-head text-sm font-bold">
              통합 검색
            </span>
            <input
              className="h-12 w-full rounded border-2 border-black bg-white px-4 py-2 shadow-hard"
              placeholder="키, 값, 단위, 범위"
              value={query}
              onChange={(event) => setQuery(event.target.value)}
            />
            {fieldErrors.q ? (
              <span className="mt-1.5 block text-xs text-destructive">
                {fieldErrors.q}
              </span>
            ) : null}
          </label>
          <label className="block">
            <span className="mb-2 block font-head text-sm font-bold">
              사용 여부
            </span>
            <select
              className="h-12 w-full rounded border-2 border-black bg-white px-3 shadow-hard"
              value={enabledFilter}
              onChange={(event) => setEnabledFilter(event.target.value)}
            >
              <option value="">전체</option>
              <option value="true">사용</option>
              <option value="false">미사용</option>
            </select>
          </label>
          <label className="block">
            <span className="mb-2 block font-head text-sm font-bold">
              표시 건수
            </span>
            <select
              className="h-12 w-full rounded border-2 border-black bg-white px-3 shadow-hard"
              value={size}
              onChange={(event) => setSize(Number(event.target.value))}
            >
              {sizeOptions.map((option) => (
                <option key={option} value={option}>
                  {option}건
                </option>
              ))}
            </select>
          </label>
          <div className="flex items-end gap-2">
            <button
              className="h-12 flex-1 rounded border-2 border-black bg-primary px-4 font-head font-black shadow-hard transition-all duration-200 hover:-translate-x-0.5 hover:-translate-y-0.5 hover:bg-primary-hover hover:shadow-hard-lg active:translate-x-1 active:translate-y-1 active:shadow-none"
              type="submit"
            >
              검색
            </button>
            <button
              className="h-12 rounded border-2 border-black bg-card px-4 font-head font-black shadow-hard"
              type="button"
              onClick={() => {
                setQuery("");
                setEnabledFilter("");
                void loadSystemConfigurations("", "", size);
              }}
            >
              초기화
            </button>
          </div>
        </form>

        {message ? (
          <div
            role="status"
            className={`border-2 border-black px-4 py-3 font-bold shadow-hard ${state === "permission" || state === "error" ? "bg-destructive text-white" : "bg-accent text-black"}`}
          >
            {message}
          </div>
        ) : null}
        {state === "loading" ? (
          <StateCard
            title="LOADING"
            body="공통 환경설정 목록을 불러오는 중입니다."
          />
        ) : null}
        {state === "empty" ? (
          <StateCard
            title="EMPTY"
            body="조건에 맞는 공통 환경설정이 없습니다. 검색 조건을 초기화해 보세요."
          />
        ) : null}
        {state === "permission" ? (
          <StateCard
            title="PERMISSION DENIED"
            body="R09 시스템관리자 권한이 필요합니다. 직접 API 요청도 서버에서 403으로 차단됩니다."
          />
        ) : null}

        {state === "success" || configs.length > 0 ? (
          <section className="grid grid-cols-1 gap-4 lg:grid-cols-[minmax(0,1fr)_22rem]">
            <div className="overflow-hidden border-2 border-black bg-card shadow-hard">
              <div className="border-b-2 border-black bg-muted px-4 py-3 font-head font-black">
                system_configurations 목록
              </div>
              <div className="overflow-x-auto">
                <table className="w-full min-w-[58rem] border-collapse text-left text-sm">
                  <thead className="bg-primary font-head text-xs uppercase">
                    <tr>
                      {[
                        "설정 키",
                        "설정값",
                        "단위",
                        "허용 범위",
                        "사용 여부",
                        "적용 범위",
                      ].map((header) => (
                        <th
                          key={header}
                          className="border-b-2 border-black px-3 py-2"
                        >
                          {header}
                        </th>
                      ))}
                    </tr>
                  </thead>
                  <tbody>
                    {configs.map((item) => (
                      <tr
                        key={item.configKey}
                        className={`cursor-pointer border-b-2 border-black transition-colors hover:bg-accent ${selectedConfigKey === item.configKey ? "bg-accent" : "bg-card"}`}
                        onClick={() => setSelectedConfigKey(item.configKey)}
                      >
                        <td className="px-3 py-2 font-mono">
                          {item.configKey}
                        </td>
                        <td className="px-3 py-2 font-bold">
                          {item.configValue}
                        </td>
                        <td className="px-3 py-2">{item.unit}</td>
                        <td className="px-3 py-2">
                          <span className="border-2 border-black bg-primary px-2 py-1 font-head text-xs font-black">
                            {item.valueRange ?? "-"}
                          </span>
                        </td>
                        <td className="px-3 py-2">
                          {item.enabled ? "사용" : "미사용"}
                        </td>
                        <td className="px-3 py-2">{item.applyScope}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>

            <aside className="relative">
              <div className="absolute inset-2 border-2 border-black bg-primary" />
              <div className="relative border-2 border-black bg-card shadow-hard">
                <div className="border-b-2 border-black bg-muted px-4 py-3 font-head font-black">
                  상세 / 저장 전 확인
                </div>
                {selectedConfig ? (
                  <div className="space-y-3 p-4 text-sm">
                    <ReadonlyField
                      label="환경설정 키"
                      value={selectedConfig.configKey}
                    />
                    <ReadonlyField
                      label="현재 설정값"
                      value={`${selectedConfig.configValue}${selectedConfig.unit}`}
                    />
                    <ReadonlyField
                      label="허용 범위"
                      value={selectedConfig.valueRange ?? "-"}
                    />
                    <ReadonlyField
                      label="사용 여부"
                      value={selectedConfig.enabled ? "사용" : "미사용"}
                    />
                    <ReadonlyField
                      label="적용 범위"
                      value={selectedConfig.applyScope}
                    />
                    <ReadonlyField
                      label="서버 검증"
                      value={selectedConfig.validationRule}
                    />
                    <p className="border-2 border-black bg-muted p-3 text-xs leading-relaxed">
                      설정 키·단위·값 범위는 생명주기/검증 기준으로 읽기
                      전용입니다. 저장 CTA는 config_value/enabled만 전달하고
                      감사로그를 기록합니다.
                    </p>
                    {fieldErrors.id ? (
                      <p className="text-xs font-bold text-destructive">
                        {fieldErrors.id}
                      </p>
                    ) : null}
                    <button
                      className="h-12 w-full rounded border-2 border-black bg-primary px-4 font-head font-black shadow-hard transition-all duration-200 hover:-translate-x-0.5 hover:-translate-y-0.5 hover:bg-primary-hover hover:shadow-hard-lg active:translate-x-1 active:translate-y-1 active:shadow-none disabled:opacity-60"
                      type="button"
                      onClick={() => void saveSelectedSystemConfiguration()}
                      disabled={saving || !user?.roles.includes("R09")}
                    >
                      {saving ? "저장 중..." : "저장/실행 전 확인"}
                    </button>
                  </div>
                ) : (
                  <div className="p-4 text-sm">
                    행을 선택하면 상세 모달 영역이 표시됩니다.
                  </div>
                )}
              </div>
            </aside>
          </section>
        ) : null}
      </section>
    </AppShell>
  );
}

function BaseYearManagementPage() {
  const [user, setUser] = useState<AuthenticatedUser | null>(null);
  const [baseYears, setBaseYears] = useState<BaseYearListItem[]>([]);
  const [selectedBaseYearValue, setSelectedBaseYearValue] = useState<
    string | null
  >(null);
  const [query, setQuery] = useState("");
  const [enabledFilter, setEnabledFilter] = useState("");
  const [size, setSize] = useState(20);
  const [state, setState] = useState<ViewState>("loading");
  const [message, setMessage] = useState("");
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});
  const [saving, setSaving] = useState(false);

  const selectedBaseYear = useMemo(
    () =>
      baseYears.find((item) => item.baseYear === selectedBaseYearValue) ?? null,
    [selectedBaseYearValue, baseYears],
  );

  useEffect(() => {
    requestJson<AuthenticatedUser>("/api/auth/session")
      .then((sessionUser) => {
        setUser(sessionUser);
        return loadBaseYears("", "", size);
      })
      .catch((caught) => {
        const error = caught as Error & { status?: number };
        setState(error.status === 403 ? "permission" : "error");
        setMessage(error.message);
      });
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  async function loadBaseYears(
    nextQuery = query,
    nextEnabled = enabledFilter,
    nextSize = size,
  ) {
    setState("loading");
    setMessage("");
    const params = new URLSearchParams({
      page: "1",
      size: String(nextSize),
      sort: "baseYear",
    });
    if (nextQuery.trim()) {
      params.set("q", nextQuery.trim());
    }
    if (nextEnabled) {
      params.set("filter", `enabled=${nextEnabled}`);
    }
    try {
      const body = await requestJson<BaseYearListResponse>(
        `/api/admin/base-years?${params.toString()}`,
      );
      setBaseYears(body.items);
      setSelectedBaseYearValue(body.items[0]?.baseYear ?? null);
      setState(body.items.length === 0 ? "empty" : "success");
      setMessage(`총 ${body.totalCount}건 조회되었습니다.`);
    } catch (caught) {
      const error = caught as Error & { apiError?: ApiError; status?: number };
      setFieldErrors(error.apiError?.fields ?? {});
      setState(error.status === 403 ? "permission" : "error");
      setMessage(error.message);
    }
  }

  function nextDefaultQueryYear(item: BaseYearListItem) {
    const base = Number(item.baseYear);
    const current = Number(item.defaultQueryYear);
    if (Number.isNaN(base) || Number.isNaN(current)) {
      return item.defaultQueryYear;
    }
    return current === base ? String(base - 1) : item.baseYear;
  }

  async function saveSelectedBaseYear() {
    if (!selectedBaseYear) {
      setMessage("저장할 기준연도를 먼저 선택하세요.");
      return;
    }
    const nextDefaultYear = nextDefaultQueryYear(selectedBaseYear);
    const nextCopyEnabled = true;
    const confirmed = window.confirm(
      `${selectedBaseYear.baseYear} 기준연도의 기본 조회연도를 ${nextDefaultYear}(으)로 변경하시겠습니까? 기준연도 식별자는 변경되지 않습니다.`,
    );
    if (!confirmed) {
      return;
    }
    setSaving(true);
    setFieldErrors({});
    try {
      const result = await requestJson<{
        baseYear: string;
        defaultQueryYear: string;
        copyBaselineEnabled: boolean;
        resetEnabled: boolean;
        enabled: boolean;
        message: string;
      }>("/api/admin/base-years", {
        method: "POST",
        body: JSON.stringify({
          id: selectedBaseYear.baseYear,
          defaultQueryYear: nextDefaultYear,
          copyBaselineEnabled: nextCopyEnabled,
          resetEnabled: selectedBaseYear.resetEnabled && nextCopyEnabled,
          enabled: selectedBaseYear.enabled,
          reason: "기준연도 관리 화면 저장 CTA",
        }),
      });
      setMessage(result.message);
      await loadBaseYears();
      setSelectedBaseYearValue(result.baseYear);
    } catch (caught) {
      const error = caught as Error & { apiError?: ApiError; status?: number };
      setFieldErrors(error.apiError?.fields ?? {});
      setState(error.status === 403 ? "permission" : "error");
      setMessage(error.message);
    } finally {
      setSaving(false);
    }
  }

  if (!user && state === "error") {
    return (
      <AppShell>
        <LoginPanel
          onLogin={(nextUser) => {
            setUser(nextUser);
            void loadBaseYears();
          }}
        />
      </AppShell>
    );
  }

  return (
    <AppShell>
      <section className="space-y-7 sm:space-y-8 lg:space-y-9">
        <div className="flex flex-col gap-4 lg:flex-row lg:items-end lg:justify-between">
          <div>
            <div className="mb-3 inline-flex border-2 border-black bg-primary px-2.5 py-1 font-head text-xs font-black uppercase tracking-[0.14em] shadow-hard">
              SCR-BASE-YEAR
            </div>
            <h1 className="font-head text-4xl font-black uppercase leading-none tracking-tight sm:text-5xl">
              기준연도 관리
            </h1>
            <p className="mt-4 max-w-3xl text-sm leading-relaxed text-muted-foreground sm:text-base">
              현재 평가연도와 사용자 화면 기본 조회연도를 관리합니다. 기준연도는
              생명주기 식별자로 읽기 전용이며 기준정보 복사·초기화 여부는 서버
              기간/상태 검증을 통과해야 저장됩니다.
            </p>
          </div>
          <div className="rounded border-2 border-black bg-card px-4 py-3 font-head text-sm font-black shadow-hard">
            {user
              ? `${user.userId} / ${user.roles.join(", ")}`
              : "세션 확인 중"}
          </div>
        </div>

        <form
          className="grid grid-cols-1 gap-4 border-2 border-black bg-card p-4 shadow-hard sm:grid-cols-[minmax(0,1fr)_10rem_9rem_9rem] sm:p-5"
          onSubmit={(event) => {
            event.preventDefault();
            void loadBaseYears();
          }}
        >
          <label className="block">
            <span className="mb-2 block font-head text-sm font-bold">
              통합 검색
            </span>
            <input
              className="h-12 w-full rounded border-2 border-black bg-white px-4 py-2 shadow-hard"
              placeholder="기준연도, 기본 조회연도"
              value={query}
              onChange={(event) => setQuery(event.target.value)}
            />
            {fieldErrors.q ? (
              <span className="mt-1.5 block text-xs text-destructive">
                {fieldErrors.q}
              </span>
            ) : null}
          </label>
          <label className="block">
            <span className="mb-2 block font-head text-sm font-bold">
              사용 여부
            </span>
            <select
              className="h-12 w-full rounded border-2 border-black bg-white px-3 shadow-hard"
              value={enabledFilter}
              onChange={(event) => setEnabledFilter(event.target.value)}
            >
              <option value="">전체</option>
              <option value="true">사용</option>
              <option value="false">미사용</option>
            </select>
          </label>
          <label className="block">
            <span className="mb-2 block font-head text-sm font-bold">
              표시 건수
            </span>
            <select
              className="h-12 w-full rounded border-2 border-black bg-white px-3 shadow-hard"
              value={size}
              onChange={(event) => setSize(Number(event.target.value))}
            >
              {sizeOptions.map((option) => (
                <option key={option} value={option}>
                  {option}건
                </option>
              ))}
            </select>
          </label>
          <div className="flex items-end gap-2">
            <button
              className="h-12 flex-1 rounded border-2 border-black bg-primary px-4 font-head font-black shadow-hard transition-all duration-200 hover:-translate-x-0.5 hover:-translate-y-0.5 hover:bg-primary-hover hover:shadow-hard-lg active:translate-x-1 active:translate-y-1 active:shadow-none"
              type="submit"
            >
              검색
            </button>
            <button
              className="h-12 rounded border-2 border-black bg-card px-4 font-head font-black shadow-hard"
              type="button"
              onClick={() => {
                setQuery("");
                setEnabledFilter("");
                void loadBaseYears("", "", size);
              }}
            >
              초기화
            </button>
          </div>
        </form>

        {message ? (
          <div
            role="status"
            className={`border-2 border-black px-4 py-3 font-bold shadow-hard ${state === "permission" || state === "error" ? "bg-destructive text-white" : "bg-accent text-black"}`}
          >
            {message}
          </div>
        ) : null}
        {state === "loading" ? (
          <StateCard
            title="LOADING"
            body="기준연도 목록을 불러오는 중입니다."
          />
        ) : null}
        {state === "empty" ? (
          <StateCard
            title="EMPTY"
            body="조건에 맞는 기준연도가 없습니다. 검색 조건을 초기화해 보세요."
          />
        ) : null}
        {state === "permission" ? (
          <StateCard
            title="PERMISSION DENIED"
            body="R09 시스템관리자 권한이 필요합니다. 직접 API 요청도 서버에서 403으로 차단됩니다."
          />
        ) : null}

        {state === "success" || baseYears.length > 0 ? (
          <section className="grid grid-cols-1 gap-4 lg:grid-cols-[minmax(0,1fr)_22rem]">
            <div className="overflow-hidden border-2 border-black bg-card shadow-hard">
              <div className="border-b-2 border-black bg-muted px-4 py-3 font-head font-black">
                base_years 목록
              </div>
              <div className="overflow-x-auto">
                <table className="w-full min-w-[58rem] border-collapse text-left text-sm">
                  <thead className="bg-primary font-head text-xs uppercase">
                    <tr>
                      {[
                        "기준연도",
                        "기본 조회연도",
                        "기준정보 복사",
                        "초기화 허용",
                        "사용 여부",
                        "기간 규칙",
                      ].map((header) => (
                        <th
                          key={header}
                          className="border-b-2 border-black px-3 py-2"
                        >
                          {header}
                        </th>
                      ))}
                    </tr>
                  </thead>
                  <tbody>
                    {baseYears.map((item) => (
                      <tr
                        key={item.baseYear}
                        className={`cursor-pointer border-b-2 border-black transition-colors hover:bg-accent ${selectedBaseYearValue === item.baseYear ? "bg-accent" : "bg-card"}`}
                        onClick={() => setSelectedBaseYearValue(item.baseYear)}
                      >
                        <td className="px-3 py-2 font-mono">{item.baseYear}</td>
                        <td className="px-3 py-2 font-bold">
                          {item.defaultQueryYear}
                        </td>
                        <td className="px-3 py-2">
                          {item.copyBaselineEnabled ? "허용" : "차단"}
                        </td>
                        <td className="px-3 py-2">
                          {item.resetEnabled ? "허용" : "차단"}
                        </td>
                        <td className="px-3 py-2">
                          {item.enabled ? "사용" : "미사용"}
                        </td>
                        <td className="px-3 py-2">{item.periodRule}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>

            <aside className="relative">
              <div className="absolute inset-2 border-2 border-black bg-primary" />
              <div className="relative border-2 border-black bg-card shadow-hard">
                <div className="border-b-2 border-black bg-muted px-4 py-3 font-head font-black">
                  상세 / 저장 전 확인
                </div>
                {selectedBaseYear ? (
                  <div className="space-y-3 p-4 text-sm">
                    <ReadonlyField
                      label="기준연도"
                      value={selectedBaseYear.baseYear}
                    />
                    <ReadonlyField
                      label="기본 조회연도"
                      value={selectedBaseYear.defaultQueryYear}
                    />
                    <ReadonlyField
                      label="기준정보 복사"
                      value={
                        selectedBaseYear.copyBaselineEnabled ? "허용" : "차단"
                      }
                    />
                    <ReadonlyField
                      label="초기화 허용"
                      value={selectedBaseYear.resetEnabled ? "허용" : "차단"}
                    />
                    <ReadonlyField
                      label="사용 여부"
                      value={selectedBaseYear.enabled ? "사용" : "미사용"}
                    />
                    <ReadonlyField
                      label="서버 검증"
                      value={selectedBaseYear.transitionRule}
                    />
                    <p className="border-2 border-black bg-muted p-3 text-xs leading-relaxed">
                      기준연도는 생명주기 식별자로 읽기 전용입니다. 저장 CTA는
                      default_query_year/copy_baseline_enabled/reset_enabled/enabled만
                      전달하고 감사로그를 기록합니다.
                    </p>
                    {fieldErrors.id ? (
                      <p className="text-xs font-bold text-destructive">
                        {fieldErrors.id}
                      </p>
                    ) : null}
                    <button
                      className="h-12 w-full rounded border-2 border-black bg-primary px-4 font-head font-black shadow-hard transition-all duration-200 hover:-translate-x-0.5 hover:-translate-y-0.5 hover:bg-primary-hover hover:shadow-hard-lg active:translate-x-1 active:translate-y-1 active:shadow-none disabled:opacity-60"
                      type="button"
                      onClick={() => void saveSelectedBaseYear()}
                      disabled={saving || !user?.roles.includes("R09")}
                    >
                      {saving ? "저장 중..." : "저장/실행 전 확인"}
                    </button>
                  </div>
                ) : (
                  <div className="p-4 text-sm">
                    행을 선택하면 상세 모달 영역이 표시됩니다.
                  </div>
                )}
              </div>
            </aside>
          </section>
        ) : null}
      </section>
    </AppShell>
  );
}

function FilePolicyManagementPage() {
  const [user, setUser] = useState<AuthenticatedUser | null>(null);
  const [filePolicies, setFilePolicies] = useState<FilePolicyListItem[]>([]);
  const [selectedFilePolicyId, setSelectedFilePolicyId] = useState<
    number | null
  >(null);
  const [query, setQuery] = useState("");
  const [businessAreaFilter, setBusinessAreaFilter] = useState("");
  const [scanFilter, setScanFilter] = useState("");
  const [size, setSize] = useState(20);
  const [state, setState] = useState<ViewState>("loading");
  const [message, setMessage] = useState("");
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});
  const [saving, setSaving] = useState(false);

  const selectedFilePolicy = useMemo(
    () =>
      filePolicies.find((item) => item.filePolicyId === selectedFilePolicyId) ??
      null,
    [selectedFilePolicyId, filePolicies],
  );

  useEffect(() => {
    requestJson<AuthenticatedUser>("/api/auth/session")
      .then((sessionUser) => {
        setUser(sessionUser);
        return loadFilePolicies("", "", "", size);
      })
      .catch((caught) => {
        const error = caught as Error & { status?: number };
        setState(error.status === 403 ? "permission" : "error");
        setMessage(error.message);
      });
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  async function loadFilePolicies(
    nextQuery = query,
    nextBusinessArea = businessAreaFilter,
    nextScan = scanFilter,
    nextSize = size,
  ) {
    setState("loading");
    setMessage("");
    const filterParts = [
      nextBusinessArea ? `businessArea=${nextBusinessArea}` : "",
      nextScan ? `malwareScanEnabled=${nextScan}` : "",
    ].filter(Boolean);
    const params = new URLSearchParams({
      page: "1",
      size: String(nextSize),
      sort: "businessArea",
    });
    if (nextQuery.trim()) {
      params.set("q", nextQuery.trim());
    }
    if (filterParts.length > 0) {
      params.set("filter", filterParts.join(";"));
    }
    try {
      const body = await requestJson<FilePolicyListResponse>(
        `/api/admin/file-policies?${params.toString()}`,
      );
      setFilePolicies(body.items);
      setSelectedFilePolicyId(body.items[0]?.filePolicyId ?? null);
      setState(body.items.length === 0 ? "empty" : "success");
      setMessage(`총 ${body.totalCount}건 조회되었습니다.`);
    } catch (caught) {
      const error = caught as Error & { apiError?: ApiError; status?: number };
      setFieldErrors(error.apiError?.fields ?? {});
      setState(error.status === 403 ? "permission" : "error");
      setMessage(error.message);
    }
  }

  function nextFilePolicyPayload(item: FilePolicyListItem) {
    const maxFileSizeMb =
      item.maxFileSizeMb >= 1024 ? item.maxFileSizeMb : item.maxFileSizeMb + 5;
    const maxFileCount =
      item.maxFileCount >= 100 ? item.maxFileCount : item.maxFileCount + 1;
    return {
      maxFileSizeMb,
      maxFileCount,
      maxTotalSizeMb: Math.max(
        item.maxTotalSizeMb,
        maxFileSizeMb * maxFileCount,
      ),
      maxFilenameLength: item.maxFilenameLength,
      malwareScanEnabled: true,
    };
  }

  async function saveSelectedFilePolicy() {
    if (!selectedFilePolicy) {
      setMessage("저장할 파일정책을 먼저 선택하세요.");
      return;
    }
    const nextPolicy = nextFilePolicyPayload(selectedFilePolicy);
    const confirmed = window.confirm(
      `${selectedFilePolicy.businessAreaName} 파일정책을 저장하시겠습니까? 실제 파일 업로드·조회·삭제는 수행하지 않습니다.`,
    );
    if (!confirmed) {
      return;
    }
    setSaving(true);
    setFieldErrors({});
    try {
      const result = await requestJson<{
        filePolicyId: number;
        businessArea: string;
        allowedExtensions: string;
        maxFileSizeMb: number;
        maxFileCount: number;
        maxTotalSizeMb: number;
        maxFilenameLength: number;
        malwareScanEnabled: boolean;
        enabled: boolean;
        message: string;
      }>("/api/admin/file-policies", {
        method: "POST",
        body: JSON.stringify({
          id: String(selectedFilePolicy.filePolicyId),
          allowedExtensions: selectedFilePolicy.allowedExtensions,
          maxFileSizeMb: nextPolicy.maxFileSizeMb,
          maxFileCount: nextPolicy.maxFileCount,
          maxTotalSizeMb: nextPolicy.maxTotalSizeMb,
          maxFilenameLength: nextPolicy.maxFilenameLength,
          malwareScanEnabled: nextPolicy.malwareScanEnabled,
          enabled: selectedFilePolicy.enabled,
          reason: "파일정책 관리 화면 저장 CTA",
        }),
      });
      setMessage(result.message);
      await loadFilePolicies();
      setSelectedFilePolicyId(result.filePolicyId);
    } catch (caught) {
      const error = caught as Error & { apiError?: ApiError; status?: number };
      setFieldErrors(error.apiError?.fields ?? {});
      setState(error.status === 403 ? "permission" : "error");
      setMessage(error.message);
    } finally {
      setSaving(false);
    }
  }

  if (!user && state === "error") {
    return (
      <AppShell>
        <LoginPanel
          onLogin={(nextUser) => {
            setUser(nextUser);
            void loadFilePolicies();
          }}
        />
      </AppShell>
    );
  }

  return (
    <AppShell>
      <section className="space-y-7 sm:space-y-8 lg:space-y-9">
        <div className="flex flex-col gap-4 lg:flex-row lg:items-end lg:justify-between">
          <div>
            <div className="mb-3 inline-flex border-2 border-black bg-primary px-2.5 py-1 font-head text-xs font-black uppercase tracking-[0.14em] shadow-hard">
              SCR-FILE-POLICY
            </div>
            <h1 className="font-head text-4xl font-black uppercase leading-none tracking-tight sm:text-5xl">
              파일정책 관리
            </h1>
            <p className="mt-4 max-w-3xl text-sm leading-relaxed text-muted-foreground sm:text-base">
              업무별 허용 확장자, 단일 파일 최대용량, 건당 첨부개수, 전체용량,
              파일명 길이와 악성파일 검사 적용여부를 관리합니다. 이 화면은 실제
              파일을 업로드·조회·삭제하지 않습니다.
            </p>
          </div>
          <div className="rounded border-2 border-black bg-card px-4 py-3 font-head text-sm font-black shadow-hard">
            {user
              ? `${user.userId} / ${user.roles.join(", ")}`
              : "세션 확인 중"}
          </div>
        </div>

        <form
          className="grid grid-cols-1 gap-4 border-2 border-black bg-card p-4 shadow-hard sm:grid-cols-[minmax(0,1fr)_11rem_10rem_9rem_9rem] sm:p-5"
          onSubmit={(event) => {
            event.preventDefault();
            void loadFilePolicies();
          }}
        >
          <label className="block">
            <span className="mb-2 block font-head text-sm font-bold">
              통합 검색
            </span>
            <input
              className="h-12 w-full rounded border-2 border-black bg-white px-4 py-2 shadow-hard"
              placeholder="업무영역, 확장자, 업무명"
              value={query}
              onChange={(event) => setQuery(event.target.value)}
            />
            {fieldErrors.q ? (
              <span className="mt-1.5 block text-xs text-destructive">
                {fieldErrors.q}
              </span>
            ) : null}
          </label>
          <label className="block">
            <span className="mb-2 block font-head text-sm font-bold">
              업무영역
            </span>
            <input
              className="h-12 w-full rounded border-2 border-black bg-white px-4 py-2 shadow-hard"
              placeholder="COMMON"
              value={businessAreaFilter}
              onChange={(event) => setBusinessAreaFilter(event.target.value)}
            />
          </label>
          <label className="block">
            <span className="mb-2 block font-head text-sm font-bold">
              악성검사
            </span>
            <select
              className="h-12 w-full rounded border-2 border-black bg-white px-3 shadow-hard"
              value={scanFilter}
              onChange={(event) => setScanFilter(event.target.value)}
            >
              <option value="">전체</option>
              <option value="true">적용</option>
              <option value="false">미적용</option>
            </select>
          </label>
          <label className="block">
            <span className="mb-2 block font-head text-sm font-bold">
              표시 건수
            </span>
            <select
              className="h-12 w-full rounded border-2 border-black bg-white px-3 shadow-hard"
              value={size}
              onChange={(event) => setSize(Number(event.target.value))}
            >
              {sizeOptions.map((option) => (
                <option key={option} value={option}>
                  {option}건
                </option>
              ))}
            </select>
          </label>
          <div className="flex items-end gap-2">
            <button
              className="h-12 flex-1 rounded border-2 border-black bg-primary px-4 font-head font-black shadow-hard transition-all duration-200 hover:-translate-x-0.5 hover:-translate-y-0.5 hover:bg-primary-hover hover:shadow-hard-lg active:translate-x-1 active:translate-y-1 active:shadow-none"
              type="submit"
            >
              검색
            </button>
            <button
              className="h-12 rounded border-2 border-black bg-card px-4 font-head font-black shadow-hard"
              type="button"
              onClick={() => {
                setQuery("");
                setBusinessAreaFilter("");
                setScanFilter("");
                void loadFilePolicies("", "", "", size);
              }}
            >
              초기화
            </button>
          </div>
        </form>

        {message ? (
          <div
            role="status"
            className={`border-2 border-black px-4 py-3 font-bold shadow-hard ${state === "permission" || state === "error" ? "bg-destructive text-white" : "bg-accent text-black"}`}
          >
            {message}
          </div>
        ) : null}
        {state === "loading" ? (
          <StateCard
            title="LOADING"
            body="파일정책 목록을 불러오는 중입니다."
          />
        ) : null}
        {state === "empty" ? (
          <StateCard
            title="EMPTY"
            body="조건에 맞는 파일정책이 없습니다. 검색 조건을 초기화해 보세요."
          />
        ) : null}
        {state === "permission" ? (
          <StateCard
            title="PERMISSION DENIED"
            body="R09 시스템관리자 권한이 필요합니다. 직접 API 요청도 서버에서 403으로 차단됩니다."
          />
        ) : null}

        {state === "success" || filePolicies.length > 0 ? (
          <section className="grid grid-cols-1 gap-4 lg:grid-cols-[minmax(0,1fr)_22rem]">
            <div className="overflow-hidden border-2 border-black bg-card shadow-hard">
              <div className="border-b-2 border-black bg-muted px-4 py-3 font-head font-black">
                file_policies 목록
              </div>
              <div className="overflow-x-auto">
                <table className="w-full min-w-[70rem] border-collapse text-left text-sm">
                  <thead className="bg-primary font-head text-xs uppercase">
                    <tr>
                      {[
                        "ID",
                        "업무영역",
                        "허용 확장자",
                        "단일 용량",
                        "첨부개수",
                        "전체용량",
                        "파일명 길이",
                        "악성검사",
                        "사용",
                      ].map((header) => (
                        <th
                          key={header}
                          className="border-b-2 border-black px-3 py-2"
                        >
                          {header}
                        </th>
                      ))}
                    </tr>
                  </thead>
                  <tbody>
                    {filePolicies.map((item) => (
                      <tr
                        key={item.filePolicyId}
                        className={`cursor-pointer border-b-2 border-black transition-colors hover:bg-accent ${selectedFilePolicyId === item.filePolicyId ? "bg-accent" : "bg-card"}`}
                        onClick={() =>
                          setSelectedFilePolicyId(item.filePolicyId)
                        }
                      >
                        <td className="px-3 py-2 font-mono">
                          {item.filePolicyId}
                        </td>
                        <td className="px-3 py-2">
                          <span className="font-mono">{item.businessArea}</span>{" "}
                          / <span>{item.businessAreaName}</span>
                        </td>
                        <td className="px-3 py-2 font-bold">
                          {item.allowedExtensions}
                        </td>
                        <td className="px-3 py-2">{item.maxFileSizeMb}MB</td>
                        <td className="px-3 py-2">{item.maxFileCount}개</td>
                        <td className="px-3 py-2">{item.maxTotalSizeMb}MB</td>
                        <td className="px-3 py-2">
                          {item.maxFilenameLength}자
                        </td>
                        <td className="px-3 py-2">
                          <span className="border-2 border-black bg-primary px-2 py-1 font-head text-xs font-black">
                            {item.malwareScanEnabled ? "적용" : "미적용"}
                          </span>
                        </td>
                        <td className="px-3 py-2">
                          {item.enabled ? "사용" : "미사용"}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>

            <aside className="relative">
              <div className="absolute inset-2 border-2 border-black bg-primary" />
              <div className="relative border-2 border-black bg-card shadow-hard">
                <div className="border-b-2 border-black bg-muted px-4 py-3 font-head font-black">
                  상세 / 저장 전 확인
                </div>
                {selectedFilePolicy ? (
                  <div className="space-y-3 p-4 text-sm">
                    <ReadonlyField
                      label="파일정책 ID"
                      value={String(selectedFilePolicy.filePolicyId)}
                    />
                    <ReadonlyField
                      label="업무영역"
                      value={`${selectedFilePolicy.businessAreaName} (${selectedFilePolicy.businessArea})`}
                    />
                    <ReadonlyField
                      label="허용 확장자"
                      value={selectedFilePolicy.allowedExtensions}
                    />
                    <ReadonlyField
                      label="용량/개수"
                      value={`단일 ${selectedFilePolicy.maxFileSizeMb}MB · ${selectedFilePolicy.maxFileCount}개 · 전체 ${selectedFilePolicy.maxTotalSizeMb}MB`}
                    />
                    <ReadonlyField
                      label="파일명 길이"
                      value={`${selectedFilePolicy.maxFilenameLength}자`}
                    />
                    <ReadonlyField
                      label="악성파일 검사"
                      value={
                        selectedFilePolicy.malwareScanEnabled
                          ? "적용"
                          : "미적용"
                      }
                    />
                    <ReadonlyField
                      label="첨부 검증 적용"
                      value={selectedFilePolicy.uploadValidationRule}
                    />
                    <p className="border-2 border-black bg-muted p-3 text-xs leading-relaxed">
                      파일정책 ID와 업무영역은 생명주기 식별자로 읽기
                      전용입니다. 저장 CTA는 첨부파일 검증 정책값만 전달하며
                      실제 파일 업로드·조회·삭제 API를 호출하지 않습니다.
                    </p>
                    {fieldErrors.id ? (
                      <p className="text-xs font-bold text-destructive">
                        {fieldErrors.id}
                      </p>
                    ) : null}
                    {fieldErrors.maxTotalSizeMb ? (
                      <p className="text-xs font-bold text-destructive">
                        {fieldErrors.maxTotalSizeMb}
                      </p>
                    ) : null}
                    <button
                      className="h-12 w-full rounded border-2 border-black bg-primary px-4 font-head font-black shadow-hard transition-all duration-200 hover:-translate-x-0.5 hover:-translate-y-0.5 hover:bg-primary-hover hover:shadow-hard-lg active:translate-x-1 active:translate-y-1 active:shadow-none disabled:opacity-60"
                      type="button"
                      onClick={() => void saveSelectedFilePolicy()}
                      disabled={saving || !user?.roles.includes("R09")}
                    >
                      {saving ? "저장 중..." : "저장/실행 전 확인"}
                    </button>
                  </div>
                ) : (
                  <div className="p-4 text-sm">
                    행을 선택하면 상세 모달 영역이 표시됩니다.
                  </div>
                )}
              </div>
            </aside>
          </section>
        ) : null}
      </section>
    </AppShell>
  );
}

function DataScopeManagementPage() {
  const [user, setUser] = useState<AuthenticatedUser | null>(null);
  const [dataScopes, setDataScopes] = useState<DataScopeListItem[]>([]);
  const [selectedDataScopeId, setSelectedDataScopeId] = useState<number | null>(
    null,
  );
  const [query, setQuery] = useState("");
  const [roleFilter, setRoleFilter] = useState("R09");
  const [scopeFilter, setScopeFilter] = useState("");
  const [size, setSize] = useState(20);
  const [state, setState] = useState<ViewState>("loading");
  const [message, setMessage] = useState("");
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});
  const [saving, setSaving] = useState(false);

  const selectedDataScope = useMemo(
    () =>
      dataScopes.find((item) => item.dataScopeId === selectedDataScopeId) ??
      null,
    [selectedDataScopeId, dataScopes],
  );

  useEffect(() => {
    requestJson<AuthenticatedUser>("/api/auth/session")
      .then((sessionUser) => {
        setUser(sessionUser);
        return loadDataScopes("", roleFilter, "", size);
      })
      .catch((caught) => {
        const error = caught as Error & { status?: number };
        setState(error.status === 403 ? "permission" : "error");
        setMessage(error.message);
      });
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  async function loadDataScopes(
    nextQuery = query,
    nextRole = roleFilter,
    nextScope = scopeFilter,
    nextSize = size,
  ) {
    setState("loading");
    setMessage("");
    const filterParts = [
      nextRole ? `roleCode=${nextRole}` : "",
      nextScope ? `scopeType=${nextScope}` : "",
    ].filter(Boolean);
    const params = new URLSearchParams({
      page: "1",
      size: String(nextSize),
      sort: "roleCode",
    });
    if (nextQuery.trim()) {
      params.set("q", nextQuery.trim());
    }
    if (filterParts.length > 0) {
      params.set("filter", filterParts.join(";"));
    }
    try {
      const body = await requestJson<DataScopeListResponse>(
        `/api/admin/data-scopes?${params.toString()}`,
      );
      setDataScopes(body.items);
      setSelectedDataScopeId(body.items[0]?.dataScopeId ?? null);
      setState(body.items.length === 0 ? "empty" : "success");
      setMessage(`총 ${body.totalCount}건 조회되었습니다.`);
    } catch (caught) {
      const error = caught as Error & { apiError?: ApiError; status?: number };
      setFieldErrors(error.apiError?.fields ?? {});
      setState(error.status === 403 ? "permission" : "error");
      setMessage(error.message);
    }
  }

  function nextScopePayload(item: DataScopeListItem) {
    if (item.scopeType === "ALL") {
      return {
        scopeType: "BUSINESS",
        organizationCode: "",
        businessArea: item.businessArea ?? "COMMON_FOUNDATION",
      };
    }
    return {
      scopeType: "ALL",
      organizationCode: item.organizationCode ?? "KNUE",
      businessArea: item.businessArea ?? "COMMON_FOUNDATION",
    };
  }

  async function saveSelectedDataScope() {
    if (!selectedDataScope) {
      setMessage("저장할 데이터 범위 권한을 먼저 선택하세요.");
      return;
    }
    const nextScope = nextScopePayload(selectedDataScope);
    const confirmed = window.confirm(
      `${selectedDataScope.roleCode} 데이터 범위를 ${scopeLabel(nextScope.scopeType)}(으)로 변경하시겠습니까? 역할 코드는 변경되지 않습니다.`,
    );
    if (!confirmed) {
      return;
    }
    setSaving(true);
    setFieldErrors({});
    try {
      const result = await requestJson<{
        dataScopeId: number;
        roleCode: string;
        scopeType: string;
        organizationCode?: string;
        businessArea?: string;
        message: string;
      }>("/api/admin/data-scopes", {
        method: "POST",
        body: JSON.stringify({
          id: String(selectedDataScope.dataScopeId),
          roleCode: selectedDataScope.roleCode,
          scopeType: nextScope.scopeType,
          organizationCode: nextScope.organizationCode,
          businessArea: nextScope.businessArea,
          reason: "데이터 범위 권한 관리 화면 저장 CTA",
        }),
      });
      setMessage(result.message);
      await loadDataScopes();
      setSelectedDataScopeId(result.dataScopeId);
    } catch (caught) {
      const error = caught as Error & { apiError?: ApiError; status?: number };
      setFieldErrors(error.apiError?.fields ?? {});
      setState(error.status === 403 ? "permission" : "error");
      setMessage(error.message);
    } finally {
      setSaving(false);
    }
  }

  if (!user && state === "error") {
    return (
      <AppShell>
        <LoginPanel
          onLogin={(nextUser) => {
            setUser(nextUser);
            void loadDataScopes();
          }}
        />
      </AppShell>
    );
  }

  return (
    <AppShell>
      <section className="space-y-7 sm:space-y-8 lg:space-y-9">
        <div className="flex flex-col gap-4 lg:flex-row lg:items-end lg:justify-between">
          <div>
            <div className="mb-3 inline-flex border-2 border-black bg-primary px-2.5 py-1 font-head text-xs font-black uppercase tracking-[0.14em] shadow-hard">
              SCR-DATA-SCOPE
            </div>
            <h1 className="font-head text-4xl font-black uppercase leading-none tracking-tight sm:text-5xl">
              데이터 범위 권한
            </h1>
            <p className="mt-4 max-w-3xl text-sm leading-relaxed text-muted-foreground sm:text-base">
              역할별 본인·소속학과·단과대학·담당업무·전체 데이터 범위를
              설정합니다. 권한 범위는 화면 필터가 아니라 서버 조회조건 강제
              기준으로 사용됩니다.
            </p>
          </div>
          <div className="rounded border-2 border-black bg-card px-4 py-3 font-head text-sm font-black shadow-hard">
            {user
              ? `${user.userId} / ${user.roles.join(", ")}`
              : "세션 확인 중"}
          </div>
        </div>

        <form
          className="grid grid-cols-1 gap-4 border-2 border-black bg-card p-4 shadow-hard sm:grid-cols-[minmax(0,1fr)_9rem_11rem_9rem_9rem] sm:p-5"
          onSubmit={(event) => {
            event.preventDefault();
            void loadDataScopes();
          }}
        >
          <label className="block">
            <span className="mb-2 block font-head text-sm font-bold">
              통합 검색
            </span>
            <input
              className="h-12 w-full rounded border-2 border-black bg-white px-4 py-2 shadow-hard"
              placeholder="역할, 범위, 조직, 업무영역"
              value={query}
              onChange={(event) => setQuery(event.target.value)}
            />
            {fieldErrors.q ? (
              <span className="mt-1.5 block text-xs text-destructive">
                {fieldErrors.q}
              </span>
            ) : null}
          </label>
          <label className="block">
            <span className="mb-2 block font-head text-sm font-bold">역할</span>
            <select
              className="h-12 w-full rounded border-2 border-black bg-white px-3 shadow-hard"
              value={roleFilter}
              onChange={(event) => setRoleFilter(event.target.value)}
            >
              <option value="">전체</option>
              {["R09", "R08", "R07", "R04", "R03", "R02", "R01"].map((role) => (
                <option key={role} value={role}>
                  {role}
                </option>
              ))}
            </select>
          </label>
          <label className="block">
            <span className="mb-2 block font-head text-sm font-bold">
              데이터 범위
            </span>
            <select
              className="h-12 w-full rounded border-2 border-black bg-white px-3 shadow-hard"
              value={scopeFilter}
              onChange={(event) => setScopeFilter(event.target.value)}
            >
              <option value="">전체</option>
              <option value="SELF">본인</option>
              <option value="DEPARTMENT">소속학과</option>
              <option value="COLLEGE">단과대학</option>
              <option value="BUSINESS">담당업무</option>
              <option value="ALL">전체</option>
            </select>
          </label>
          <label className="block">
            <span className="mb-2 block font-head text-sm font-bold">
              표시 건수
            </span>
            <select
              className="h-12 w-full rounded border-2 border-black bg-white px-3 shadow-hard"
              value={size}
              onChange={(event) => setSize(Number(event.target.value))}
            >
              {sizeOptions.map((option) => (
                <option key={option} value={option}>
                  {option}건
                </option>
              ))}
            </select>
          </label>
          <div className="flex items-end gap-2">
            <button
              className="h-12 flex-1 rounded border-2 border-black bg-primary px-4 font-head font-black shadow-hard transition-all duration-200 hover:-translate-x-0.5 hover:-translate-y-0.5 hover:bg-primary-hover hover:shadow-hard-lg active:translate-x-1 active:translate-y-1 active:shadow-none"
              type="submit"
            >
              검색
            </button>
            <button
              className="h-12 rounded border-2 border-black bg-card px-4 font-head font-black shadow-hard"
              type="button"
              onClick={() => {
                setQuery("");
                setRoleFilter("R09");
                setScopeFilter("");
                void loadDataScopes("", "R09", "", size);
              }}
            >
              초기화
            </button>
          </div>
        </form>

        {message ? (
          <div
            role="status"
            className={`border-2 border-black px-4 py-3 font-bold shadow-hard ${state === "permission" || state === "error" ? "bg-destructive text-white" : "bg-accent text-black"}`}
          >
            {message}
          </div>
        ) : null}
        {state === "loading" ? (
          <StateCard
            title="LOADING"
            body="데이터 범위 권한 매트릭스를 불러오는 중입니다."
          />
        ) : null}
        {state === "empty" ? (
          <StateCard
            title="EMPTY"
            body="조건에 맞는 데이터 범위 권한이 없습니다. 검색 조건을 초기화해 보세요."
          />
        ) : null}
        {state === "permission" ? (
          <StateCard
            title="PERMISSION DENIED"
            body="R09 시스템관리자 권한이 필요합니다. 직접 API 요청도 서버에서 403으로 차단됩니다."
          />
        ) : null}

        {state === "success" || dataScopes.length > 0 ? (
          <section className="grid grid-cols-1 gap-4 lg:grid-cols-[minmax(0,1fr)_22rem]">
            <div className="overflow-hidden border-2 border-black bg-card shadow-hard">
              <div className="border-b-2 border-black bg-muted px-4 py-3 font-head font-black">
                data_scope_permissions 목록
              </div>
              <div className="overflow-x-auto">
                <table className="w-full min-w-[58rem] border-collapse text-left text-sm">
                  <thead className="bg-primary font-head text-xs uppercase">
                    <tr>
                      {[
                        "ID",
                        "역할",
                        "데이터 범위",
                        "조직",
                        "업무영역",
                        "서버 강제 규칙",
                      ].map((header) => (
                        <th
                          key={header}
                          className="border-b-2 border-black px-3 py-2"
                        >
                          {header}
                        </th>
                      ))}
                    </tr>
                  </thead>
                  <tbody>
                    {dataScopes.map((item) => (
                      <tr
                        key={item.dataScopeId}
                        className={`cursor-pointer border-b-2 border-black transition-colors hover:bg-accent ${selectedDataScopeId === item.dataScopeId ? "bg-accent" : "bg-card"}`}
                        onClick={() => setSelectedDataScopeId(item.dataScopeId)}
                      >
                        <td className="px-3 py-2 font-mono">
                          {item.dataScopeId}
                        </td>
                        <td className="px-3 py-2 font-bold">
                          {item.roleName} ({item.roleCode})
                        </td>
                        <td className="px-3 py-2">
                          <span className="border-2 border-black bg-primary px-2 py-1 font-head text-xs font-black">
                            {item.scopeName}
                          </span>
                        </td>
                        <td className="px-3 py-2">
                          {item.organizationName ??
                            item.organizationCode ??
                            "-"}
                        </td>
                        <td className="px-3 py-2">
                          {item.businessAreaName ?? item.businessArea ?? "-"}
                        </td>
                        <td className="px-3 py-2">{item.enforcementRule}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>

            <aside className="relative">
              <div className="absolute inset-2 border-2 border-black bg-primary" />
              <div className="relative border-2 border-black bg-card shadow-hard">
                <div className="border-b-2 border-black bg-muted px-4 py-3 font-head font-black">
                  상세 / 저장 전 확인
                </div>
                {selectedDataScope ? (
                  <div className="space-y-3 p-4 text-sm">
                    <ReadonlyField
                      label="데이터 범위 권한 ID"
                      value={String(selectedDataScope.dataScopeId)}
                    />
                    <ReadonlyField
                      label="역할"
                      value={`${selectedDataScope.roleName} (${selectedDataScope.roleCode})`}
                    />
                    <ReadonlyField
                      label="현재 범위"
                      value={`${selectedDataScope.scopeName} (${selectedDataScope.scopeType})`}
                    />
                    <ReadonlyField
                      label="조직"
                      value={
                        selectedDataScope.organizationName ??
                        selectedDataScope.organizationCode ??
                        "-"
                      }
                    />
                    <ReadonlyField
                      label="업무영역"
                      value={
                        selectedDataScope.businessAreaName ??
                        selectedDataScope.businessArea ??
                        "-"
                      }
                    />
                    <ReadonlyField
                      label="서버 강제 규칙"
                      value={selectedDataScope.enforcementRule}
                    />
                    <p className="border-2 border-black bg-muted p-3 text-xs leading-relaxed">
                      역할 코드는 생명주기 식별자로 읽기 전용입니다. 저장 CTA는
                      scope_type/organization_code/business_area만 갱신하고
                      감사로그를 기록합니다.
                    </p>
                    {fieldErrors.id ? (
                      <p className="text-xs font-bold text-destructive">
                        {fieldErrors.id}
                      </p>
                    ) : null}
                    <button
                      className="h-12 w-full rounded border-2 border-black bg-primary px-4 font-head font-black shadow-hard transition-all duration-200 hover:-translate-x-0.5 hover:-translate-y-0.5 hover:bg-primary-hover hover:shadow-hard-lg active:translate-x-1 active:translate-y-1 active:shadow-none disabled:opacity-60"
                      type="button"
                      onClick={() => void saveSelectedDataScope()}
                      disabled={saving || !user?.roles.includes("R09")}
                    >
                      {saving ? "저장 중..." : "저장/실행 전 확인"}
                    </button>
                  </div>
                ) : (
                  <div className="p-4 text-sm">
                    행을 선택하면 상세 모달 영역이 표시됩니다.
                  </div>
                )}
              </div>
            </aside>
          </section>
        ) : null}
      </section>
    </AppShell>
  );
}

function functionActionLabel(actionCode: string) {
  const labels: Record<string, string> = {
    READ: "조회",
    CREATE: "등록",
    UPDATE: "수정",
    DELETE: "삭제",
    VERIFY: "확인",
    AUTH: "인증",
    APPROVE: "승인",
    CANCEL_APPROVAL: "승인취소",
    PRINT: "출력",
    EXCEL: "엑셀",
    BULK: "일괄처리",
    EXPORT: "엑셀",
  };
  return labels[actionCode] ?? actionCode;
}

function NoticeManagementPage() {
  const [user, setUser] = useState<AuthenticatedUser | null>(null);
  const [notices, setNotices] = useState<NoticeListItem[]>([]);
  const [selectedNoticeId, setSelectedNoticeId] = useState<number | null>(null);
  const [query, setQuery] = useState("");
  const [importantFilter, setImportantFilter] = useState("");
  const [targetRoleFilter, setTargetRoleFilter] = useState("");
  const [size, setSize] = useState(20);
  const [state, setState] = useState<ViewState>("loading");
  const [message, setMessage] = useState("");
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});
  const [saving, setSaving] = useState(false);

  const selectedNotice = useMemo(
    () => notices.find((item) => item.noticeId === selectedNoticeId) ?? null,
    [selectedNoticeId, notices],
  );

  useEffect(() => {
    requestJson<AuthenticatedUser>("/api/auth/session")
      .then((sessionUser) => {
        setUser(sessionUser);
        return loadNotices("", "", "", size);
      })
      .catch((caught) => {
        const error = caught as Error & { status?: number };
        setState(error.status === 403 ? "permission" : "error");
        setMessage(error.message);
      });
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  async function loadNotices(
    nextQuery = query,
    nextImportant = importantFilter,
    nextTargetRole = targetRoleFilter,
    nextSize = size,
  ) {
    setState("loading");
    setMessage("");
    const filterParts: string[] = [];
    if (nextImportant) {
      filterParts.push(`important=${nextImportant}`);
    }
    if (nextTargetRole) {
      filterParts.push(`targetRole=${nextTargetRole}`);
    }
    const params = new URLSearchParams({
      page: "1",
      size: String(nextSize),
      sort: "important",
    });
    if (nextQuery.trim()) {
      params.set("q", nextQuery.trim());
    }
    if (filterParts.length > 0) {
      params.set("filter", filterParts.join(";"));
    }
    try {
      const body = await requestJson<NoticeListResponse>(
        `/api/admin/notices?${params.toString()}`,
      );
      setNotices(body.items);
      setSelectedNoticeId(body.items[0]?.noticeId ?? null);
      setState(body.items.length === 0 ? "empty" : "success");
      setMessage(`총 ${body.totalCount}건 조회되었습니다.`);
    } catch (caught) {
      const error = caught as Error & { apiError?: ApiError; status?: number };
      setFieldErrors(error.apiError?.fields ?? {});
      setState(error.status === 403 ? "permission" : "error");
      setMessage(error.message);
    }
  }

  async function saveSelectedNotice() {
    if (!selectedNotice) {
      setMessage("저장할 공지사항을 먼저 선택하세요.");
      return;
    }
    const confirmed = window.confirm(
      `${selectedNotice.title} 공지 게시조건을 저장하시겠습니까?`,
    );
    if (!confirmed) {
      return;
    }
    setSaving(true);
    setFieldErrors({});
    try {
      const result = await requestJson<{
        noticeId: number;
        title: string;
        postFrom: string;
        postTo: string;
        targetRoles?: string | null;
        targetOrganizations?: string | null;
        important: boolean;
        enabled: boolean;
        message: string;
      }>("/api/admin/notices", {
        method: "POST",
        body: JSON.stringify({
          id: String(selectedNotice.noticeId),
          title: selectedNotice.title,
          postFrom: selectedNotice.postFrom,
          postTo: selectedNotice.postTo,
          targetRoles: selectedNotice.targetRoles ?? "R09",
          targetOrganizations: selectedNotice.targetOrganizations ?? "KNUE",
          important: !selectedNotice.important,
          enabled: selectedNotice.enabled,
          reason: "공지사항 관리 화면 저장 CTA",
        }),
      });
      setMessage(result.message);
      await loadNotices();
      setSelectedNoticeId(result.noticeId);
    } catch (caught) {
      const error = caught as Error & { apiError?: ApiError; status?: number };
      setFieldErrors(error.apiError?.fields ?? {});
      setState(error.status === 403 ? "permission" : "error");
      setMessage(error.message);
    } finally {
      setSaving(false);
    }
  }

  if (!user && state === "error") {
    return (
      <AppShell>
        <LoginPanel
          onLogin={(nextUser) => {
            setUser(nextUser);
            void loadNotices();
          }}
        />
      </AppShell>
    );
  }

  return (
    <AppShell>
      <section className="space-y-7 sm:space-y-8 lg:space-y-9">
        <div className="flex flex-col gap-4 lg:flex-row lg:items-end lg:justify-between">
          <div>
            <div className="mb-3 inline-flex border-2 border-black bg-primary px-2.5 py-1 font-head text-xs font-black uppercase tracking-[0.14em] shadow-hard">
              SCR-NOTICE
            </div>
            <h1 className="font-head text-4xl font-black uppercase leading-none tracking-tight sm:text-5xl">
              공지사항 관리
            </h1>
            <p className="mt-4 max-w-3xl text-sm leading-relaxed text-muted-foreground sm:text-base">
              평가일정·점검·업무안내 공지를 제목, 게시기간, 대상 역할·조직,
              중요여부, 첨부파일 수와 함께 조회하고 게시기간 역전 검증을
              서버에서 수행합니다.
            </p>
          </div>
          <div className="rounded border-2 border-black bg-card px-4 py-3 font-head text-sm font-black shadow-hard">
            {user
              ? `${user.userId} / ${user.roles.join(", ")}`
              : "세션 확인 중"}
          </div>
        </div>

        <form
          className="grid grid-cols-1 gap-4 border-2 border-black bg-card p-4 shadow-hard sm:grid-cols-[minmax(0,1fr)_9rem_9rem_9rem_9rem] sm:p-5"
          onSubmit={(event) => {
            event.preventDefault();
            void loadNotices();
          }}
        >
          <label className="block">
            <span className="mb-2 block font-head text-sm font-bold">
              통합 검색
            </span>
            <input
              className="h-12 w-full rounded border-2 border-black bg-white px-4 py-2 shadow-hard"
              placeholder="제목, 본문, 대상 역할·조직"
              value={query}
              onChange={(event) => setQuery(event.target.value)}
            />
            {fieldErrors.q ? (
              <span className="mt-1.5 block text-xs text-destructive">
                {fieldErrors.q}
              </span>
            ) : null}
          </label>
          <label className="block">
            <span className="mb-2 block font-head text-sm font-bold">
              중요여부
            </span>
            <select
              className="h-12 w-full rounded border-2 border-black bg-white px-3 shadow-hard"
              value={importantFilter}
              onChange={(event) => setImportantFilter(event.target.value)}
            >
              <option value="">전체</option>
              <option value="true">중요</option>
              <option value="false">일반</option>
            </select>
          </label>
          <label className="block">
            <span className="mb-2 block font-head text-sm font-bold">
              대상 역할
            </span>
            <select
              className="h-12 w-full rounded border-2 border-black bg-white px-3 shadow-hard"
              value={targetRoleFilter}
              onChange={(event) => setTargetRoleFilter(event.target.value)}
            >
              <option value="">전체</option>
              {[
                "R01",
                "R02",
                "R03",
                "R04",
                "R05",
                "R06",
                "R07",
                "R08",
                "R09",
              ].map((role) => (
                <option key={role} value={role}>
                  {role}
                </option>
              ))}
            </select>
          </label>
          <label className="block">
            <span className="mb-2 block font-head text-sm font-bold">
              표시 건수
            </span>
            <select
              className="h-12 w-full rounded border-2 border-black bg-white px-3 shadow-hard"
              value={size}
              onChange={(event) => setSize(Number(event.target.value))}
            >
              {sizeOptions.map((option) => (
                <option key={option} value={option}>
                  {option}건
                </option>
              ))}
            </select>
          </label>
          <div className="flex items-end gap-2">
            <button
              className="h-12 flex-1 rounded border-2 border-black bg-primary px-4 font-head font-black shadow-hard transition-all duration-200 hover:-translate-x-0.5 hover:-translate-y-0.5 hover:bg-primary-hover hover:shadow-hard-lg active:translate-x-1 active:translate-y-1 active:shadow-none"
              type="submit"
            >
              검색
            </button>
            <button
              className="h-12 rounded border-2 border-black bg-card px-4 font-head font-black shadow-hard"
              type="button"
              onClick={() => {
                setQuery("");
                setImportantFilter("");
                setTargetRoleFilter("");
                void loadNotices("", "", "", size);
              }}
            >
              초기화
            </button>
          </div>
        </form>

        {message ? (
          <div
            role="status"
            className={`border-2 border-black px-4 py-3 font-bold shadow-hard ${state === "permission" || state === "error" ? "bg-destructive text-white" : "bg-accent text-black"}`}
          >
            {message}
          </div>
        ) : null}
        {state === "loading" ? (
          <StateCard
            title="LOADING"
            body="공지사항 목록을 불러오는 중입니다."
          />
        ) : null}
        {state === "empty" ? (
          <StateCard
            title="EMPTY"
            body="조건에 맞는 공지사항이 없습니다. 검색 조건을 초기화해 보세요."
          />
        ) : null}
        {state === "permission" ? (
          <StateCard
            title="PERMISSION DENIED"
            body="R09 시스템관리자 권한이 필요합니다. 직접 API 요청도 서버에서 403으로 차단됩니다."
          />
        ) : null}

        {state === "success" || notices.length > 0 ? (
          <section className="grid grid-cols-1 gap-4 lg:grid-cols-[minmax(0,1fr)_22rem]">
            <div className="overflow-hidden border-2 border-black bg-card shadow-hard">
              <div className="border-b-2 border-black bg-muted px-4 py-3 font-head font-black">
                notices 목록
              </div>
              <div className="overflow-x-auto">
                <table className="w-full min-w-[62rem] border-collapse text-left text-sm">
                  <thead className="bg-primary font-head text-xs uppercase">
                    <tr>
                      {[
                        "ID",
                        "제목",
                        "게시시작",
                        "게시종료",
                        "대상 역할",
                        "대상 조직",
                        "중요",
                        "첨부",
                        "사용",
                      ].map((header) => (
                        <th
                          key={header}
                          className="border-b-2 border-black px-3 py-2"
                        >
                          {header}
                        </th>
                      ))}
                    </tr>
                  </thead>
                  <tbody>
                    {notices.map((item) => (
                      <tr
                        key={item.noticeId}
                        className={`cursor-pointer border-b-2 border-black transition-colors hover:bg-accent ${selectedNoticeId === item.noticeId ? "bg-accent" : "bg-card"}`}
                        onClick={() => setSelectedNoticeId(item.noticeId)}
                      >
                        <td className="px-3 py-2 font-mono">{item.noticeId}</td>
                        <td className="px-3 py-2 font-bold">{item.title}</td>
                        <td className="px-3 py-2">{item.postFrom}</td>
                        <td className="px-3 py-2">{item.postTo}</td>
                        <td className="px-3 py-2">{item.targetRoles ?? "-"}</td>
                        <td className="px-3 py-2">
                          {item.targetOrganizations ?? "-"}
                        </td>
                        <td className="px-3 py-2">
                          <span className="border-2 border-black bg-primary px-2 py-1 font-head text-xs font-black">
                            {item.important ? "중요" : "일반"}
                          </span>
                        </td>
                        <td className="px-3 py-2">{item.attachmentCount}</td>
                        <td className="px-3 py-2">
                          {item.enabled ? "사용" : "미사용"}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>

            <aside className="relative">
              <div className="absolute inset-2 border-2 border-black bg-primary" />
              <div className="relative border-2 border-black bg-card shadow-hard">
                <div className="border-b-2 border-black bg-muted px-4 py-3 font-head font-black">
                  상세 / 저장 전 확인
                </div>
                {selectedNotice ? (
                  <div className="space-y-3 p-4 text-sm">
                    <ReadonlyField
                      label="공지 ID"
                      value={String(selectedNotice.noticeId)}
                    />
                    <ReadonlyField label="제목" value={selectedNotice.title} />
                    <ReadonlyField
                      label="게시기간"
                      value={`${selectedNotice.postFrom} ~ ${selectedNotice.postTo}`}
                    />
                    <ReadonlyField
                      label="대상 역할"
                      value={selectedNotice.targetRoles ?? "-"}
                    />
                    <ReadonlyField
                      label="대상 조직"
                      value={selectedNotice.targetOrganizations ?? "-"}
                    />
                    <ReadonlyField
                      label="첨부파일"
                      value={`${selectedNotice.attachmentCount}건`}
                    />
                    <p className="border-2 border-black bg-muted p-3 text-xs leading-relaxed">
                      {selectedNotice.exposureRule}
                    </p>
                    <p className="border-2 border-black bg-muted p-3 text-xs leading-relaxed">
                      {selectedNotice.readBoundary}
                    </p>
                    <p className="border-2 border-black bg-accent p-3 text-xs leading-relaxed">
                      공지 ID는 생명주기 식별자로 읽기 전용입니다. 저장 CTA는
                      게시기간, 대상 역할·조직, 중요여부 등 로컬 DB 관리 필드만
                      변경하고 열람을 승인 처리로 간주하지 않습니다.
                    </p>
                    {fieldErrors.id ? (
                      <p className="text-xs font-bold text-destructive">
                        {fieldErrors.id}
                      </p>
                    ) : null}
                    {fieldErrors.postTo ? (
                      <p className="text-xs font-bold text-destructive">
                        {fieldErrors.postTo}
                      </p>
                    ) : null}
                    <button
                      className="h-12 w-full rounded border-2 border-black bg-primary px-4 font-head font-black shadow-hard transition-all duration-200 hover:-translate-x-0.5 hover:-translate-y-0.5 hover:bg-primary-hover hover:shadow-hard-lg active:translate-x-1 active:translate-y-1 active:shadow-none disabled:opacity-60"
                      type="button"
                      onClick={() => void saveSelectedNotice()}
                      disabled={saving || !user?.roles.includes("R09")}
                    >
                      {saving ? "저장 중..." : "저장/실행 전 확인"}
                    </button>
                  </div>
                ) : (
                  <div className="p-4 text-sm">
                    행을 선택하면 상세 모달 영역이 표시됩니다.
                  </div>
                )}
              </div>
            </aside>
          </section>
        ) : null}
      </section>
    </AppShell>
  );
}

function AttachmentManagementPage() {
  const [user, setUser] = useState<AuthenticatedUser | null>(null);
  const [attachments, setAttachments] = useState<AttachmentListItem[]>([]);
  const [selectedAttachmentId, setSelectedAttachmentId] = useState<
    number | null
  >(null);
  const [query, setQuery] = useState("");
  const [scanFilter, setScanFilter] = useState("");
  const [integrityFilter, setIntegrityFilter] = useState("");
  const [deletedFilter, setDeletedFilter] = useState("");
  const [size, setSize] = useState(20);
  const [state, setState] = useState<ViewState>("loading");
  const [message, setMessage] = useState("");
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});
  const [saving, setSaving] = useState(false);

  const selectedAttachment = useMemo(
    () =>
      attachments.find((item) => item.attachmentId === selectedAttachmentId) ??
      null,
    [selectedAttachmentId, attachments],
  );

  useEffect(() => {
    requestJson<AuthenticatedUser>("/api/auth/session")
      .then((sessionUser) => {
        setUser(sessionUser);
        return loadAttachments("", "", "", "", size);
      })
      .catch((caught) => {
        const error = caught as Error & { status?: number };
        setState(error.status === 403 ? "permission" : "error");
        setMessage(error.message);
      });
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  async function loadAttachments(
    nextQuery = query,
    nextScan = scanFilter,
    nextIntegrity = integrityFilter,
    nextDeleted = deletedFilter,
    nextSize = size,
  ) {
    setState("loading");
    setMessage("");
    const filterParts: string[] = [];
    if (nextScan) {
      filterParts.push(`malwareScanResult=${nextScan}`);
    }
    if (nextIntegrity) {
      filterParts.push(`integrityStatus=${nextIntegrity}`);
    }
    if (nextDeleted) {
      filterParts.push(`deleted=${nextDeleted}`);
    }
    const params = new URLSearchParams({
      page: "1",
      size: String(nextSize),
      sort: "uploadedAt",
    });
    if (nextQuery.trim()) {
      params.set("q", nextQuery.trim());
    }
    if (filterParts.length > 0) {
      params.set("filter", filterParts.join(";"));
    }
    try {
      const body = await requestJson<AttachmentListResponse>(
        `/api/admin/attachments?${params.toString()}`,
      );
      setAttachments(body.items);
      setSelectedAttachmentId(body.items[0]?.attachmentId ?? null);
      setState(body.items.length === 0 ? "empty" : "success");
      setMessage(`총 ${body.totalCount}건 조회되었습니다.`);
    } catch (caught) {
      const error = caught as Error & { apiError?: ApiError; status?: number };
      setFieldErrors(error.apiError?.fields ?? {});
      setState(error.status === 403 ? "permission" : "error");
      setMessage(error.message);
    }
  }

  async function saveSelectedAttachment() {
    if (!selectedAttachment) {
      setMessage("처리할 첨부파일을 먼저 선택하세요.");
      return;
    }
    const confirmed = window.confirm(
      `${selectedAttachment.originalName} 첨부파일을 논리삭제 처리하시겠습니까?`,
    );
    if (!confirmed) {
      return;
    }
    setSaving(true);
    setFieldErrors({});
    try {
      const result = await requestJson<{
        attachmentId: number;
        businessKey: string;
        originalName: string;
        deleted: boolean;
        actionResult: string;
        message: string;
      }>("/api/admin/attachments", {
        method: "POST",
        body: JSON.stringify({
          id: String(selectedAttachment.attachmentId),
          businessKey: selectedAttachment.businessKey,
          deleteRequested: !selectedAttachment.deleted,
          deleteReason: selectedAttachment.deleted
            ? "논리삭제 취소"
            : "첨부파일 관리 화면 논리삭제 CTA",
          reason: "첨부파일 관리 화면 저장 CTA",
        }),
      });
      setMessage(result.message);
      await loadAttachments();
      setSelectedAttachmentId(result.attachmentId);
    } catch (caught) {
      const error = caught as Error & { apiError?: ApiError; status?: number };
      setFieldErrors(error.apiError?.fields ?? {});
      setState(error.status === 403 ? "permission" : "error");
      setMessage(error.message);
    } finally {
      setSaving(false);
    }
  }

  if (!user && state === "error") {
    return (
      <AppShell>
        <LoginPanel
          onLogin={(nextUser) => {
            setUser(nextUser);
            void loadAttachments();
          }}
        />
      </AppShell>
    );
  }

  return (
    <AppShell>
      <section className="space-y-7 sm:space-y-8 lg:space-y-9">
        <div className="flex flex-col gap-4 lg:flex-row lg:items-end lg:justify-between">
          <div>
            <div className="mb-3 inline-flex border-2 border-black bg-primary px-2.5 py-1 font-head text-xs font-black uppercase tracking-[0.14em] shadow-hard">
              SCR-ATTACHMENT
            </div>
            <h1 className="font-head text-4xl font-black uppercase leading-none tracking-tight sm:text-5xl">
              첨부파일 관리
            </h1>
            <p className="mt-4 max-w-3xl text-sm leading-relaxed text-muted-foreground sm:text-base">
              업무자료별 첨부파일의
              원본명·저장명·확장자·크기·등록자·등록일시·악성검사결과와 저장소
              정합성 상태를 조회하고, 삭제 사유 기반 논리삭제만 수행합니다.
            </p>
          </div>
          <div className="rounded border-2 border-black bg-card px-4 py-3 font-head text-sm font-black shadow-hard">
            {user
              ? `${user.userId} / ${user.roles.join(", ")}`
              : "세션 확인 중"}
          </div>
        </div>

        <form
          className="grid grid-cols-1 gap-4 border-2 border-black bg-card p-4 shadow-hard sm:grid-cols-[minmax(0,1fr)_9rem_9rem_9rem_9rem_9rem] sm:p-5"
          onSubmit={(event) => {
            event.preventDefault();
            void loadAttachments();
          }}
        >
          <label className="block">
            <span className="mb-2 block font-head text-sm font-bold">
              통합 검색
            </span>
            <input
              className="h-12 w-full rounded border-2 border-black bg-white px-4 py-2 shadow-hard"
              placeholder="업무키, 원본명, 저장명, 등록자"
              value={query}
              onChange={(event) => setQuery(event.target.value)}
            />
            {fieldErrors.q ? (
              <span className="mt-1.5 block text-xs text-destructive">
                {fieldErrors.q}
              </span>
            ) : null}
          </label>
          <label className="block">
            <span className="mb-2 block font-head text-sm font-bold">
              악성검사
            </span>
            <select
              className="h-12 w-full rounded border-2 border-black bg-white px-3 shadow-hard"
              value={scanFilter}
              onChange={(event) => setScanFilter(event.target.value)}
            >
              <option value="">전체</option>
              {["PENDING", "CLEAN", "INFECTED", "FAILED"].map((scan) => (
                <option key={scan} value={scan}>
                  {scan}
                </option>
              ))}
            </select>
          </label>
          <label className="block">
            <span className="mb-2 block font-head text-sm font-bold">
              정합성
            </span>
            <select
              className="h-12 w-full rounded border-2 border-black bg-white px-3 shadow-hard"
              value={integrityFilter}
              onChange={(event) => setIntegrityFilter(event.target.value)}
            >
              <option value="">전체</option>
              {["OK", "MISSING_BUSINESS", "MISSING_FILE", "DUPLICATE"].map(
                (status) => (
                  <option key={status} value={status}>
                    {status}
                  </option>
                ),
              )}
            </select>
          </label>
          <label className="block">
            <span className="mb-2 block font-head text-sm font-bold">삭제</span>
            <select
              className="h-12 w-full rounded border-2 border-black bg-white px-3 shadow-hard"
              value={deletedFilter}
              onChange={(event) => setDeletedFilter(event.target.value)}
            >
              <option value="">전체</option>
              <option value="false">미삭제</option>
              <option value="true">논리삭제</option>
            </select>
          </label>
          <label className="block">
            <span className="mb-2 block font-head text-sm font-bold">
              표시 건수
            </span>
            <select
              className="h-12 w-full rounded border-2 border-black bg-white px-3 shadow-hard"
              value={size}
              onChange={(event) => setSize(Number(event.target.value))}
            >
              {sizeOptions.map((option) => (
                <option key={option} value={option}>
                  {option}건
                </option>
              ))}
            </select>
          </label>
          <div className="flex items-end gap-2">
            <button
              className="h-12 flex-1 rounded border-2 border-black bg-primary px-4 font-head font-black shadow-hard transition-all duration-200 hover:-translate-x-0.5 hover:-translate-y-0.5 hover:bg-primary-hover hover:shadow-hard-lg active:translate-x-1 active:translate-y-1 active:shadow-none"
              type="submit"
            >
              검색
            </button>
            <button
              className="h-12 rounded border-2 border-black bg-card px-4 font-head font-black shadow-hard"
              type="button"
              onClick={() => {
                setQuery("");
                setScanFilter("");
                setIntegrityFilter("");
                setDeletedFilter("");
                void loadAttachments("", "", "", "", size);
              }}
            >
              초기화
            </button>
          </div>
        </form>

        {message ? (
          <div
            role="status"
            className={`border-2 border-black px-4 py-3 font-bold shadow-hard ${state === "permission" || state === "error" ? "bg-destructive text-white" : "bg-accent text-black"}`}
          >
            {message}
          </div>
        ) : null}
        {state === "loading" ? (
          <StateCard
            title="LOADING"
            body="첨부파일 메타데이터 목록을 불러오는 중입니다."
          />
        ) : null}
        {state === "empty" ? (
          <StateCard
            title="EMPTY"
            body="조건에 맞는 첨부파일이 없습니다. 검색 조건을 초기화해 보세요."
          />
        ) : null}
        {state === "permission" ? (
          <StateCard
            title="PERMISSION DENIED"
            body="R09 시스템관리자 권한이 필요합니다. 직접 API 요청도 서버에서 403으로 차단됩니다."
          />
        ) : null}

        {state === "success" || attachments.length > 0 ? (
          <section className="grid grid-cols-1 gap-4 lg:grid-cols-[minmax(0,1fr)_22rem]">
            <div className="overflow-hidden border-2 border-black bg-card shadow-hard">
              <div className="border-b-2 border-black bg-muted px-4 py-3 font-head font-black">
                attachment_files 목록
              </div>
              <div className="overflow-x-auto">
                <table className="w-full min-w-[72rem] border-collapse text-left text-sm">
                  <thead className="bg-primary font-head text-xs uppercase">
                    <tr>
                      {[
                        "ID",
                        "업무키",
                        "원본명",
                        "저장명",
                        "확장자",
                        "크기",
                        "등록자",
                        "등록일시",
                        "검사",
                        "정합성",
                        "삭제",
                      ].map((header) => (
                        <th
                          key={header}
                          className="border-b-2 border-black px-3 py-2"
                        >
                          {header}
                        </th>
                      ))}
                    </tr>
                  </thead>
                  <tbody>
                    {attachments.map((item) => (
                      <tr
                        key={item.attachmentId}
                        className={`cursor-pointer border-b-2 border-black transition-colors hover:bg-accent ${selectedAttachmentId === item.attachmentId ? "bg-accent" : "bg-card"}`}
                        onClick={() =>
                          setSelectedAttachmentId(item.attachmentId)
                        }
                      >
                        <td className="px-3 py-2 font-mono">
                          {item.attachmentId}
                        </td>
                        <td className="px-3 py-2 font-mono">
                          {item.businessKey}
                        </td>
                        <td className="px-3 py-2 font-bold">
                          {item.originalName}
                        </td>
                        <td className="px-3 py-2 font-mono text-xs">
                          {item.storedName}
                        </td>
                        <td className="px-3 py-2">{item.extension}</td>
                        <td className="px-3 py-2">
                          {formatBytes(item.sizeBytes)}
                        </td>
                        <td className="px-3 py-2">{item.uploadedBy}</td>
                        <td className="px-3 py-2">
                          {item.uploadedAt.replace("T", " ")}
                        </td>
                        <td className="px-3 py-2">
                          <span className="border-2 border-black bg-primary px-2 py-1 font-head text-xs font-black">
                            {item.malwareScanResult}
                          </span>
                        </td>
                        <td className="px-3 py-2">{item.integrityStatus}</td>
                        <td className="px-3 py-2">
                          {item.deleted ? "논리삭제" : "미삭제"}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>

            <aside className="relative">
              <div className="absolute inset-2 border-2 border-black bg-primary" />
              <div className="relative border-2 border-black bg-card shadow-hard">
                <div className="border-b-2 border-black bg-muted px-4 py-3 font-head font-black">
                  상세 / 삭제 전 확인
                </div>
                {selectedAttachment ? (
                  <div className="space-y-3 p-4 text-sm">
                    <ReadonlyField
                      label="첨부파일 ID"
                      value={String(selectedAttachment.attachmentId)}
                    />
                    <ReadonlyField
                      label="업무자료 키"
                      value={selectedAttachment.businessKey}
                    />
                    <ReadonlyField
                      label="원본명"
                      value={selectedAttachment.originalName}
                    />
                    <ReadonlyField
                      label="저장명"
                      value={selectedAttachment.storedName}
                    />
                    <ReadonlyField
                      label="크기"
                      value={formatBytes(selectedAttachment.sizeBytes)}
                    />
                    <ReadonlyField
                      label="악성검사"
                      value={selectedAttachment.malwareScanResult}
                    />
                    <ReadonlyField
                      label="정합성"
                      value={selectedAttachment.integrityStatus}
                    />
                    <p className="border-2 border-black bg-muted p-3 text-xs leading-relaxed">
                      {selectedAttachment.downloadAuthorizationRule}
                    </p>
                    <p className="border-2 border-black bg-muted p-3 text-xs leading-relaxed">
                      {selectedAttachment.deleteBoundary}
                    </p>
                    <p className="border-2 border-black bg-accent p-3 text-xs leading-relaxed">
                      첨부파일 ID와 업무자료 키는 생명주기 식별자로 읽기
                      전용입니다. 삭제 CTA는 실제 파일 물리삭제 없이 삭제 사유를
                      포함한 논리삭제만 요청하며 평가확정 자료는 서버에서
                      차단합니다.
                    </p>
                    {fieldErrors.id ? (
                      <p className="text-xs font-bold text-destructive">
                        {fieldErrors.id}
                      </p>
                    ) : null}
                    {fieldErrors.deleteReason ? (
                      <p className="text-xs font-bold text-destructive">
                        {fieldErrors.deleteReason}
                      </p>
                    ) : null}
                    {fieldErrors.deleteRequested ? (
                      <p className="text-xs font-bold text-destructive">
                        {fieldErrors.deleteRequested}
                      </p>
                    ) : null}
                    <button
                      className="h-12 w-full rounded border-2 border-black bg-primary px-4 font-head font-black shadow-hard transition-all duration-200 hover:-translate-x-0.5 hover:-translate-y-0.5 hover:bg-primary-hover hover:shadow-hard-lg active:translate-x-1 active:translate-y-1 active:shadow-none disabled:opacity-60"
                      type="button"
                      onClick={() => void saveSelectedAttachment()}
                      disabled={
                        saving ||
                        !user?.roles.includes("R09") ||
                        selectedAttachment.finalizedRecord
                      }
                    >
                      {saving ? "저장 중..." : "논리삭제 전 확인"}
                    </button>
                  </div>
                ) : (
                  <div className="p-4 text-sm">
                    행을 선택하면 상세 모달 영역이 표시됩니다.
                  </div>
                )}
              </div>
            </aside>
          </section>
        ) : null}
      </section>
    </AppShell>
  );
}

function ExcelTemplateManagementPage() {
  const [user, setUser] = useState<AuthenticatedUser | null>(null);
  const [templates, setTemplates] = useState<ExcelTemplateListItem[]>([]);
  const [selectedTemplateId, setSelectedTemplateId] = useState<number | null>(
    null,
  );
  const [query, setQuery] = useState("");
  const [businessAreaFilter, setBusinessAreaFilter] = useState("");
  const [enabledFilter, setEnabledFilter] = useState("");
  const [size, setSize] = useState(20);
  const [state, setState] = useState<ViewState>("loading");
  const [message, setMessage] = useState("");
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});
  const [saving, setSaving] = useState(false);

  const selectedTemplate = useMemo(
    () =>
      templates.find((item) => item.templateId === selectedTemplateId) ?? null,
    [selectedTemplateId, templates],
  );

  useEffect(() => {
    requestJson<AuthenticatedUser>("/api/auth/session")
      .then((sessionUser) => {
        setUser(sessionUser);
        return loadExcelTemplates("", "", "", size);
      })
      .catch((caught) => {
        const error = caught as Error & { status?: number };
        setState(error.status === 403 ? "permission" : "error");
        setMessage(error.message);
      });
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  async function loadExcelTemplates(
    nextQuery = query,
    nextBusinessArea = businessAreaFilter,
    nextEnabled = enabledFilter,
    nextSize = size,
  ) {
    setState("loading");
    setMessage("");
    const filterParts: string[] = [];
    if (nextBusinessArea) {
      filterParts.push(`businessArea=${nextBusinessArea}`);
    }
    if (nextEnabled) {
      filterParts.push(`enabled=${nextEnabled}`);
    }
    const params = new URLSearchParams({
      page: "1",
      size: String(nextSize),
      sort: "updatedAt",
    });
    if (nextQuery.trim()) {
      params.set("q", nextQuery.trim());
    }
    if (filterParts.length > 0) {
      params.set("filter", filterParts.join(";"));
    }
    try {
      const body = await requestJson<ExcelTemplateListResponse>(
        `/api/admin/excel-templates?${params.toString()}`,
      );
      setTemplates(body.items);
      setSelectedTemplateId(body.items[0]?.templateId ?? null);
      setState(body.items.length === 0 ? "empty" : "success");
      setMessage(`총 ${body.totalCount}건 조회되었습니다.`);
    } catch (caught) {
      const error = caught as Error & { apiError?: ApiError; status?: number };
      setFieldErrors(error.apiError?.fields ?? {});
      setState(error.status === 403 ? "permission" : "error");
      setMessage(error.message);
    }
  }

  async function saveSelectedExcelTemplate() {
    if (!selectedTemplate) {
      setMessage("저장할 업로드 양식을 먼저 선택하세요.");
      return;
    }
    const confirmed = window.confirm(
      `${selectedTemplate.businessArea} ${selectedTemplate.version} 업로드 양식 사용여부를 변경하시겠습니까?`,
    );
    if (!confirmed) {
      return;
    }
    setSaving(true);
    setFieldErrors({});
    try {
      const result = await requestJson<{
        templateId: number;
        businessArea: string;
        version: string;
        enabled: boolean;
        requiredColumnCount: number;
        message: string;
      }>("/api/admin/excel-templates", {
        method: "POST",
        body: JSON.stringify({
          id: String(selectedTemplate.templateId),
          businessArea: selectedTemplate.businessArea,
          version: selectedTemplate.version,
          requiredColumns: parseRequiredColumns(
            selectedTemplate.requiredColumns,
          ),
          effectiveDate: selectedTemplate.effectiveDate,
          enabled: !selectedTemplate.enabled,
          reason: "업로드 양식 관리 화면 저장 CTA",
        }),
      });
      setMessage(result.message);
      await loadExcelTemplates();
      setSelectedTemplateId(result.templateId);
    } catch (caught) {
      const error = caught as Error & { apiError?: ApiError; status?: number };
      setFieldErrors(error.apiError?.fields ?? {});
      setState(error.status === 403 ? "permission" : "error");
      setMessage(error.message);
    } finally {
      setSaving(false);
    }
  }

  if (!user && state === "error") {
    return (
      <AppShell>
        <LoginPanel
          onLogin={(nextUser) => {
            setUser(nextUser);
            void loadExcelTemplates();
          }}
        />
      </AppShell>
    );
  }

  return (
    <AppShell>
      <section className="space-y-7 sm:space-y-8 lg:space-y-9">
        <div className="flex flex-col gap-4 lg:flex-row lg:items-end lg:justify-between">
          <div>
            <div className="mb-3 inline-flex border-2 border-black bg-primary px-2.5 py-1 font-head text-xs font-black uppercase tracking-[0.14em] shadow-hard">
              SCR-EXCEL-TEMPLATE
            </div>
            <h1 className="font-head text-4xl font-black uppercase leading-none tracking-tight sm:text-5xl">
              업로드 양식 관리
            </h1>
            <p className="mt-4 max-w-3xl text-sm leading-relaxed text-muted-foreground sm:text-base">
              업무별 엑셀 업로드 템플릿의 버전, 필수 컬럼, 적용일자, 양식 파일을
              조회하고 템플릿 사용여부와 검증 규칙을 관리합니다.
            </p>
          </div>
          <div className="rounded border-2 border-black bg-card px-4 py-3 font-head text-sm font-black shadow-hard">
            {user
              ? `${user.userId} / ${user.roles.join(", ")}`
              : "세션 확인 중"}
          </div>
        </div>

        <form
          className="grid grid-cols-1 gap-4 border-2 border-black bg-card p-4 shadow-hard sm:grid-cols-[minmax(0,1fr)_10rem_9rem_9rem_9rem] sm:p-5"
          onSubmit={(event) => {
            event.preventDefault();
            void loadExcelTemplates();
          }}
        >
          <label className="block">
            <span className="mb-2 block font-head text-sm font-bold">
              통합 검색
            </span>
            <input
              className="h-12 w-full rounded border-2 border-black bg-white px-4 py-2 shadow-hard"
              placeholder="업무영역, 버전, 업무명"
              value={query}
              onChange={(event) => setQuery(event.target.value)}
            />
            {fieldErrors.q ? (
              <span className="mt-1.5 block text-xs text-destructive">
                {fieldErrors.q}
              </span>
            ) : null}
          </label>
          <label className="block">
            <span className="mb-2 block font-head text-sm font-bold">
              업무영역
            </span>
            <select
              className="h-12 w-full rounded border-2 border-black bg-white px-3 shadow-hard"
              value={businessAreaFilter}
              onChange={(event) => setBusinessAreaFilter(event.target.value)}
            >
              <option value="">전체</option>
              {["ACHIEVEMENT", "RESEARCH"].map((area) => (
                <option key={area} value={area}>
                  {area}
                </option>
              ))}
            </select>
          </label>
          <label className="block">
            <span className="mb-2 block font-head text-sm font-bold">
              사용여부
            </span>
            <select
              className="h-12 w-full rounded border-2 border-black bg-white px-3 shadow-hard"
              value={enabledFilter}
              onChange={(event) => setEnabledFilter(event.target.value)}
            >
              <option value="">전체</option>
              <option value="true">사용</option>
              <option value="false">미사용</option>
            </select>
          </label>
          <label className="block">
            <span className="mb-2 block font-head text-sm font-bold">
              표시 건수
            </span>
            <select
              className="h-12 w-full rounded border-2 border-black bg-white px-3 shadow-hard"
              value={size}
              onChange={(event) => setSize(Number(event.target.value))}
            >
              {sizeOptions.map((option) => (
                <option key={option} value={option}>
                  {option}건
                </option>
              ))}
            </select>
          </label>
          <div className="flex items-end gap-2">
            <button
              className="h-12 flex-1 rounded border-2 border-black bg-primary px-4 font-head font-black shadow-hard transition-all duration-200 hover:-translate-x-0.5 hover:-translate-y-0.5 hover:bg-primary-hover hover:shadow-hard-lg active:translate-x-1 active:translate-y-1 active:shadow-none"
              type="submit"
            >
              검색
            </button>
            <button
              className="h-12 rounded border-2 border-black bg-card px-4 font-head font-black shadow-hard"
              type="button"
              onClick={() => {
                setQuery("");
                setBusinessAreaFilter("");
                setEnabledFilter("");
                void loadExcelTemplates("", "", "", size);
              }}
            >
              초기화
            </button>
          </div>
        </form>

        {message ? (
          <div
            role="status"
            className={`border-2 border-black px-4 py-3 font-bold shadow-hard ${state === "permission" || state === "error" ? "bg-destructive text-white" : "bg-accent text-black"}`}
          >
            {message}
          </div>
        ) : null}
        {state === "loading" ? (
          <StateCard
            title="LOADING"
            body="업로드 양식 목록을 불러오는 중입니다."
          />
        ) : null}
        {state === "empty" ? (
          <StateCard
            title="EMPTY"
            body="조건에 맞는 업로드 양식이 없습니다. 검색 조건을 초기화해 보세요."
          />
        ) : null}
        {state === "permission" ? (
          <StateCard
            title="PERMISSION DENIED"
            body="R09 시스템관리자 권한이 필요합니다. 직접 API 요청도 서버에서 403으로 차단됩니다."
          />
        ) : null}

        {state === "success" || templates.length > 0 ? (
          <section className="grid grid-cols-1 gap-4 lg:grid-cols-[minmax(0,1fr)_22rem]">
            <div className="overflow-hidden border-2 border-black bg-card shadow-hard">
              <div className="border-b-2 border-black bg-muted px-4 py-3 font-head font-black">
                excel_templates 목록
              </div>
              <div className="overflow-x-auto">
                <table className="w-full min-w-[68rem] border-collapse text-left text-sm">
                  <thead className="bg-primary font-head text-xs uppercase">
                    <tr>
                      {[
                        "ID",
                        "업무영역",
                        "업무명",
                        "버전",
                        "필수컬럼",
                        "적용일자",
                        "양식파일",
                        "사용여부",
                        "수정일시",
                      ].map((header) => (
                        <th
                          key={header}
                          className="border-b-2 border-black px-3 py-2"
                        >
                          {header}
                        </th>
                      ))}
                    </tr>
                  </thead>
                  <tbody>
                    {templates.map((item) => (
                      <tr
                        key={item.templateId}
                        className={`cursor-pointer border-b-2 border-black transition-colors hover:bg-accent ${selectedTemplateId === item.templateId ? "bg-accent" : "bg-card"}`}
                        onClick={() => setSelectedTemplateId(item.templateId)}
                      >
                        <td className="px-3 py-2 font-mono">
                          {item.templateId}
                        </td>
                        <td className="px-3 py-2 font-mono">
                          {item.businessArea}
                        </td>
                        <td className="px-3 py-2 font-bold">
                          {item.businessAreaName}
                        </td>
                        <td className="px-3 py-2">{item.version}</td>
                        <td className="px-3 py-2">
                          {item.requiredColumnCount}개
                        </td>
                        <td className="px-3 py-2">{item.effectiveDate}</td>
                        <td className="px-3 py-2">
                          {item.downloadFileName ?? "-"}
                        </td>
                        <td className="px-3 py-2">
                          <span className="border-2 border-black bg-primary px-2 py-1 font-head text-xs font-black">
                            {item.enabled ? "사용" : "미사용"}
                          </span>
                        </td>
                        <td className="px-3 py-2">
                          {item.updatedAt?.replace("T", " ") ?? "-"}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>

            <aside className="relative">
              <div className="absolute inset-2 border-2 border-black bg-primary" />
              <div className="relative border-2 border-black bg-card shadow-hard">
                <div className="border-b-2 border-black bg-muted px-4 py-3 font-head font-black">
                  상세 / 저장 전 확인
                </div>
                {selectedTemplate ? (
                  <div className="space-y-3 p-4 text-sm">
                    <ReadonlyField
                      label="템플릿 ID"
                      value={String(selectedTemplate.templateId)}
                    />
                    <ReadonlyField
                      label="업무영역"
                      value={`${selectedTemplate.businessAreaName} (${selectedTemplate.businessArea})`}
                    />
                    <ReadonlyField
                      label="버전"
                      value={selectedTemplate.version}
                    />
                    <ReadonlyField
                      label="필수 컬럼 수"
                      value={`${selectedTemplate.requiredColumnCount}개`}
                    />
                    <ReadonlyField
                      label="적용일자"
                      value={selectedTemplate.effectiveDate}
                    />
                    <ReadonlyField
                      label="양식 파일"
                      value={
                        selectedTemplate.downloadFileName ??
                        "등록된 다운로드 파일 없음"
                      }
                    />
                    <p className="border-2 border-black bg-muted p-3 text-xs leading-relaxed">
                      {selectedTemplate.validationRule}
                    </p>
                    <p className="border-2 border-black bg-muted p-3 text-xs leading-relaxed">
                      {selectedTemplate.downloadRule}
                    </p>
                    <p className="border-2 border-black bg-accent p-3 text-xs leading-relaxed">
                      템플릿 ID, 업무영역, 버전은 생명주기 식별자로 읽기
                      전용입니다. 저장 CTA는 필수 컬럼 JSON, 적용일자,
                      사용여부만 서버에 전달하고 감사로그를 기록합니다.
                    </p>
                    {fieldErrors.id ? (
                      <p className="text-xs font-bold text-destructive">
                        {fieldErrors.id}
                      </p>
                    ) : null}
                    {fieldErrors.requiredColumns ? (
                      <p className="text-xs font-bold text-destructive">
                        {fieldErrors.requiredColumns}
                      </p>
                    ) : null}
                    {fieldErrors.businessArea ? (
                      <p className="text-xs font-bold text-destructive">
                        {fieldErrors.businessArea}
                      </p>
                    ) : null}
                    {fieldErrors.version ? (
                      <p className="text-xs font-bold text-destructive">
                        {fieldErrors.version}
                      </p>
                    ) : null}
                    <button
                      className="h-12 w-full rounded border-2 border-black bg-primary px-4 font-head font-black shadow-hard transition-all duration-200 hover:-translate-x-0.5 hover:-translate-y-0.5 hover:bg-primary-hover hover:shadow-hard-lg active:translate-x-1 active:translate-y-1 active:shadow-none disabled:opacity-60"
                      type="button"
                      onClick={() => void saveSelectedExcelTemplate()}
                      disabled={saving || !user?.roles.includes("R09")}
                    >
                      {saving
                        ? "저장 중..."
                        : selectedTemplate.enabled
                          ? "미사용 전환 전 확인"
                          : "사용 전환 전 확인"}
                    </button>
                  </div>
                ) : (
                  <div className="p-4 text-sm">
                    행을 선택하면 상세 모달 영역이 표시됩니다.
                  </div>
                )}
              </div>
            </aside>
          </section>
        ) : null}
      </section>
    </AppShell>
  );
}

function ExcelUploadManagementPage() {
  const [user, setUser] = useState<AuthenticatedUser | null>(null);
  const [uploads, setUploads] = useState<ExcelUploadListItem[]>([]);
  const [selectedUploadId, setSelectedUploadId] = useState<number | null>(null);
  const [query, setQuery] = useState("");
  const [statusFilter, setStatusFilter] = useState("");
  const [size, setSize] = useState(20);
  const [state, setState] = useState<ViewState>("loading");
  const [message, setMessage] = useState("");
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});
  const [saving, setSaving] = useState(false);

  const selectedUpload = useMemo(
    () => uploads.find((item) => item.uploadId === selectedUploadId) ?? null,
    [selectedUploadId, uploads],
  );

  useEffect(() => {
    requestJson<AuthenticatedUser>("/api/auth/session")
      .then((sessionUser) => {
        setUser(sessionUser);
        return loadExcelUploads("", "", size);
      })
      .catch((caught) => {
        const error = caught as Error & { status?: number };
        setState(error.status === 403 ? "permission" : "error");
        setMessage(error.message);
      });
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  async function loadExcelUploads(
    nextQuery = query,
    nextStatus = statusFilter,
    nextSize = size,
  ) {
    setState("loading");
    setMessage("");
    const params = new URLSearchParams({
      page: "1",
      size: String(nextSize),
      sort: "uploadedAt",
    });
    if (nextQuery.trim()) {
      params.set("q", nextQuery.trim());
    }
    if (nextStatus) {
      params.set("filter", `uploadStatus=${nextStatus}`);
    }
    try {
      const body = await requestJson<ExcelUploadListResponse>(
        `/api/admin/excel-uploads?${params.toString()}`,
      );
      setUploads(body.items);
      setSelectedUploadId(body.items[0]?.uploadId ?? null);
      setState(body.items.length === 0 ? "empty" : "success");
      setMessage(`총 ${body.totalCount}건 조회되었습니다.`);
    } catch (caught) {
      const error = caught as Error & { apiError?: ApiError; status?: number };
      setFieldErrors(error.apiError?.fields ?? {});
      setState(error.status === 403 ? "permission" : "error");
      setMessage(error.message);
    }
  }

  async function runSampleUpload() {
    const confirmed = window.confirm(
      "선택한 화면의 샘플 행으로 엑셀 업로드 검증을 실행하시겠습니까?",
    );
    if (!confirmed) {
      return;
    }
    setSaving(true);
    setFieldErrors({});
    try {
      const result = await requestJson<{
        uploadId: number;
        uploadStatus: string;
        totalCount: number;
        savedCount: number;
        message: string;
      }>("/api/admin/excel-uploads", {
        method: "POST",
        body: JSON.stringify({
          id: "1",
          fileName: "성과업로드_화면검증.xlsx",
          rows: [
            { 교번: "T-1001", 업적구분: "논문", 점수: "10" },
            { 교번: "T-1002", 업적구분: "저서", 점수: "8" },
          ],
          reason: "엑셀 업로드 화면 저장 CTA",
        }),
      });
      setMessage(
        `${result.message} 저장 ${result.savedCount}/${result.totalCount}건, 상태 ${result.uploadStatus}`,
      );
      await loadExcelUploads();
      setSelectedUploadId(result.uploadId);
    } catch (caught) {
      const error = caught as Error & { apiError?: ApiError; status?: number };
      setFieldErrors(error.apiError?.fields ?? {});
      setState(error.status === 403 ? "permission" : "error");
      setMessage(error.message);
    } finally {
      setSaving(false);
    }
  }

  if (!user && state === "error") {
    return (
      <AppShell>
        <LoginPanel
          onLogin={(nextUser) => {
            setUser(nextUser);
            void loadExcelUploads();
          }}
        />
      </AppShell>
    );
  }

  return (
    <AppShell>
      <section className="space-y-7 sm:space-y-8 lg:space-y-9">
        <div className="flex flex-col gap-4 lg:flex-row lg:items-end lg:justify-between">
          <div>
            <div className="mb-3 inline-flex border-2 border-black bg-primary px-2.5 py-1 font-head text-xs font-black uppercase tracking-[0.14em] shadow-hard">
              SCR-EXCEL-UPLOAD
            </div>
            <h1 className="font-head text-4xl font-black uppercase leading-none tracking-tight sm:text-5xl">
              엑셀 업로드
            </h1>
            <p className="mt-4 max-w-3xl text-sm leading-relaxed text-muted-foreground sm:text-base">
              업무별 확정 양식 버전과 연결하여 헤더·필수값·형식·코드·중복을 사전
              검증하고, 오류가 있으면 전체 행을 반영하지 않는 업로드 이력을
              조회합니다.
            </p>
          </div>
          <div className="rounded border-2 border-black bg-card px-4 py-3 font-head text-sm font-black shadow-hard">
            {user
              ? `${user.userId} / ${user.roles.join(", ")}`
              : "세션 확인 중"}
          </div>
        </div>

        <form
          className="grid grid-cols-1 gap-4 border-2 border-black bg-card p-4 shadow-hard sm:grid-cols-[minmax(0,1fr)_10rem_9rem_9rem] sm:p-5"
          onSubmit={(event) => {
            event.preventDefault();
            void loadExcelUploads();
          }}
        >
          <label className="block">
            <span className="mb-2 block font-head text-sm font-bold">
              통합 검색
            </span>
            <input
              className="h-12 w-full rounded border-2 border-black bg-white px-4 py-2 shadow-hard"
              placeholder="파일명, 업로더, 업무영역, 버전"
              value={query}
              onChange={(event) => setQuery(event.target.value)}
            />
            {fieldErrors.q ? (
              <span className="mt-1.5 block text-xs text-destructive">
                {fieldErrors.q}
              </span>
            ) : null}
          </label>
          <label className="block">
            <span className="mb-2 block font-head text-sm font-bold">
              처리상태
            </span>
            <select
              className="h-12 w-full rounded border-2 border-black bg-white px-3 shadow-hard"
              value={statusFilter}
              onChange={(event) => setStatusFilter(event.target.value)}
            >
              <option value="">전체</option>
              <option value="SUCCESS">성공</option>
              <option value="FAILED">검증실패</option>
            </select>
          </label>
          <label className="block">
            <span className="mb-2 block font-head text-sm font-bold">
              표시 건수
            </span>
            <select
              className="h-12 w-full rounded border-2 border-black bg-white px-3 shadow-hard"
              value={size}
              onChange={(event) => setSize(Number(event.target.value))}
            >
              {sizeOptions.map((option) => (
                <option key={option} value={option}>
                  {option}건
                </option>
              ))}
            </select>
          </label>
          <div className="flex items-end gap-2">
            <button
              className="h-12 flex-1 rounded border-2 border-black bg-primary px-4 font-head font-black shadow-hard transition-all duration-200 hover:-translate-x-0.5 hover:-translate-y-0.5 hover:bg-primary-hover hover:shadow-hard-lg active:translate-x-1 active:translate-y-1 active:shadow-none"
              type="submit"
            >
              검색
            </button>
            <button
              className="h-12 rounded border-2 border-black bg-card px-4 font-head font-black shadow-hard"
              type="button"
              onClick={() => {
                setQuery("");
                setStatusFilter("");
                void loadExcelUploads("", "", size);
              }}
            >
              초기화
            </button>
          </div>
        </form>

        {message ? (
          <div
            role="status"
            className={`border-2 border-black px-4 py-3 font-bold shadow-hard ${state === "permission" || state === "error" ? "bg-destructive text-white" : "bg-accent text-black"}`}
          >
            {message}
          </div>
        ) : null}
        {state === "loading" ? (
          <StateCard
            title="LOADING"
            body="엑셀 업로드 이력을 불러오는 중입니다."
          />
        ) : null}
        {state === "empty" ? (
          <StateCard
            title="EMPTY"
            body="조건에 맞는 업로드 이력이 없습니다. 검색 조건을 초기화해 보세요."
          />
        ) : null}
        {state === "permission" ? (
          <StateCard
            title="PERMISSION DENIED"
            body="R09 시스템관리자 권한이 필요합니다. 직접 API 요청도 서버에서 403으로 차단됩니다."
          />
        ) : null}

        {state === "success" || uploads.length > 0 ? (
          <section className="grid grid-cols-1 gap-4 lg:grid-cols-[minmax(0,1fr)_22rem]">
            <div className="overflow-hidden border-2 border-black bg-card shadow-hard">
              <div className="border-b-2 border-black bg-muted px-4 py-3 font-head font-black">
                excel_upload_histories 목록
              </div>
              <div className="overflow-x-auto">
                <table className="w-full min-w-[74rem] border-collapse text-left text-sm">
                  <thead className="bg-primary font-head text-xs uppercase">
                    <tr>
                      {[
                        "ID",
                        "양식",
                        "업무명",
                        "버전",
                        "파일명",
                        "업로더",
                        "총건수",
                        "정상",
                        "오류",
                        "제외",
                        "저장",
                        "상태",
                        "처리시간",
                        "업로드일시",
                      ].map((header) => (
                        <th
                          key={header}
                          className="border-b-2 border-black px-3 py-2"
                        >
                          {header}
                        </th>
                      ))}
                    </tr>
                  </thead>
                  <tbody>
                    {uploads.map((item) => (
                      <tr
                        key={item.uploadId}
                        className={`cursor-pointer border-b-2 border-black transition-colors hover:bg-accent ${selectedUploadId === item.uploadId ? "bg-accent" : "bg-card"}`}
                        onClick={() => setSelectedUploadId(item.uploadId)}
                      >
                        <td className="px-3 py-2 font-mono">{item.uploadId}</td>
                        <td className="px-3 py-2 font-mono">
                          {item.templateId}
                        </td>
                        <td className="px-3 py-2 font-bold">
                          {item.businessAreaName}
                        </td>
                        <td className="px-3 py-2">{item.version}</td>
                        <td className="px-3 py-2">{item.fileName}</td>
                        <td className="px-3 py-2">{item.uploaderId}</td>
                        <td className="px-3 py-2">{item.totalCount}</td>
                        <td className="px-3 py-2">{item.successCount}</td>
                        <td className="px-3 py-2">{item.errorCount}</td>
                        <td className="px-3 py-2">{item.excludedCount}</td>
                        <td className="px-3 py-2">{item.savedCount}</td>
                        <td className="px-3 py-2">
                          <span className="border-2 border-black bg-primary px-2 py-1 font-head text-xs font-black">
                            {item.uploadStatus}
                          </span>
                        </td>
                        <td className="px-3 py-2">{item.processingTimeMs}ms</td>
                        <td className="px-3 py-2">
                          {item.uploadedAt?.replace("T", " ") ?? "-"}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>

            <aside className="relative">
              <div className="absolute inset-2 border-2 border-black bg-primary" />
              <div className="relative border-2 border-black bg-card shadow-hard">
                <div className="border-b-2 border-black bg-muted px-4 py-3 font-head font-black">
                  상세 / 업로드 전 확인
                </div>
                {selectedUpload ? (
                  <div className="space-y-3 p-4 text-sm">
                    <ReadonlyField
                      label="업로드 ID"
                      value={String(selectedUpload.uploadId)}
                    />
                    <ReadonlyField
                      label="업무영역"
                      value={`${selectedUpload.businessAreaName} (${selectedUpload.businessArea})`}
                    />
                    <ReadonlyField
                      label="파일명"
                      value={selectedUpload.fileName}
                    />
                    <ReadonlyField
                      label="처리상태"
                      value={selectedUpload.uploadStatus}
                    />
                    <ReadonlyField
                      label="처리건수"
                      value={`총 ${selectedUpload.totalCount} / 정상 ${selectedUpload.successCount} / 오류 ${selectedUpload.errorCount} / 제외 ${selectedUpload.excludedCount} / 저장 ${selectedUpload.savedCount}`}
                    />
                    <p className="border-2 border-black bg-muted p-3 text-xs leading-relaxed">
                      {selectedUpload.validationRule}
                    </p>
                    <p className="border-2 border-black bg-muted p-3 text-xs leading-relaxed">
                      {selectedUpload.transactionRule}
                    </p>
                    <p className="border-2 border-black bg-accent p-3 text-xs leading-relaxed">
                      업로드 ID와 템플릿 버전은 이력 식별자로 읽기 전용입니다.
                      오류가 하나라도 있으면 서버가 savedCount=0으로 기록하고
                      오류목록만 분리합니다.
                    </p>
                    {fieldErrors.id ? (
                      <p className="text-xs font-bold text-destructive">
                        {fieldErrors.id}
                      </p>
                    ) : null}
                    {fieldErrors.rows ? (
                      <p className="text-xs font-bold text-destructive">
                        {fieldErrors.rows}
                      </p>
                    ) : null}
                    <button
                      className="h-12 w-full rounded border-2 border-black bg-primary px-4 font-head font-black shadow-hard transition-all duration-200 hover:-translate-x-0.5 hover:-translate-y-0.5 hover:bg-primary-hover hover:shadow-hard-lg active:translate-x-1 active:translate-y-1 active:shadow-none disabled:opacity-60"
                      type="button"
                      onClick={() => void runSampleUpload()}
                      disabled={saving || !user?.roles.includes("R09")}
                    >
                      {saving ? "업로드 검증 중..." : "샘플 업로드 전 확인"}
                    </button>
                  </div>
                ) : (
                  <div className="p-4 text-sm">
                    행을 선택하면 상세 모달 영역이 표시됩니다.
                  </div>
                )}
              </div>
            </aside>
          </section>
        ) : null}
      </section>
    </AppShell>
  );
}

function ExcelDownloadManagementPage() {
  const [user, setUser] = useState<AuthenticatedUser | null>(null);
  const [downloads, setDownloads] = useState<ExcelDownloadListItem[]>([]);
  const [selectedDownloadId, setSelectedDownloadId] = useState<number | null>(
    null,
  );
  const [query, setQuery] = useState("");
  const [requesterFilter, setRequesterFilter] = useState("");
  const [size, setSize] = useState(20);
  const [state, setState] = useState<ViewState>("loading");
  const [message, setMessage] = useState("");
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});
  const [saving, setSaving] = useState(false);

  const selectedDownload = useMemo(
    () =>
      downloads.find((item) => item.downloadId === selectedDownloadId) ?? null,
    [selectedDownloadId, downloads],
  );

  useEffect(() => {
    requestJson<AuthenticatedUser>("/api/auth/session")
      .then((sessionUser) => {
        setUser(sessionUser);
        return loadExcelDownloads("", "", size);
      })
      .catch((caught) => {
        const error = caught as Error & { status?: number };
        setState(error.status === 403 ? "permission" : "error");
        setMessage(error.message);
      });
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  async function loadExcelDownloads(
    nextQuery = query,
    nextRequester = requesterFilter,
    nextSize = size,
  ) {
    setState("loading");
    setMessage("");
    const params = new URLSearchParams({
      page: "1",
      size: String(nextSize),
      sort: "createdAt",
    });
    if (nextQuery.trim()) {
      params.set("q", nextQuery.trim());
    }
    if (nextRequester.trim()) {
      params.set("filter", `requesterId=${nextRequester.trim()}`);
    }
    try {
      const body = await requestJson<ExcelDownloadListResponse>(
        `/api/admin/excel-downloads?${params.toString()}`,
      );
      setDownloads(body.items);
      setSelectedDownloadId(body.items[0]?.downloadId ?? null);
      setState(body.items.length === 0 ? "empty" : "success");
      setMessage(`총 ${body.totalCount}건 조회되었습니다.`);
    } catch (caught) {
      const error = caught as Error & { apiError?: ApiError; status?: number };
      setFieldErrors(error.apiError?.fields ?? {});
      setState(error.status === 403 ? "permission" : "error");
      setMessage(error.message);
    }
  }

  async function createDownloadRequest() {
    const confirmed = window.confirm(
      "현재 조회조건과 사용자 데이터범위 권한을 적용하여 XLSX 다운로드 요청을 생성하시겠습니까?",
    );
    if (!confirmed) {
      return;
    }
    setSaving(true);
    setFieldErrors({});
    try {
      const result = await requestJson<{
        downloadId: number;
        fileId: number;
        fileName: string;
        status: string;
        message: string;
      }>("/api/admin/excel-downloads", {
        method: "POST",
        body: JSON.stringify({
          id: selectedDownload
            ? extractBusinessArea(selectedDownload.queryCondition)
            : "ACHIEVEMENT",
          queryCondition: {
            q: query.trim(),
            requesterId: requesterFilter.trim(),
            sourceScreen: "SCR-EXCEL-DOWNLOAD",
          },
          reason: "엑셀 다운로드 화면 저장 CTA",
        }),
      });
      setMessage(
        `${result.message} 파일 ${result.fileName} (${result.status})`,
      );
      await loadExcelDownloads();
      setSelectedDownloadId(result.downloadId);
    } catch (caught) {
      const error = caught as Error & { apiError?: ApiError; status?: number };
      setFieldErrors(error.apiError?.fields ?? {});
      setState(error.status === 403 ? "permission" : "error");
      setMessage(error.message);
    } finally {
      setSaving(false);
    }
  }

  if (!user && state === "error") {
    return (
      <AppShell>
        <LoginPanel
          onLogin={(nextUser) => {
            setUser(nextUser);
            void loadExcelDownloads();
          }}
        />
      </AppShell>
    );
  }

  return (
    <AppShell>
      <section className="space-y-7 sm:space-y-8 lg:space-y-9">
        <div className="flex flex-col gap-4 lg:flex-row lg:items-end lg:justify-between">
          <div>
            <div className="mb-3 inline-flex border-2 border-black bg-primary px-2.5 py-1 font-head text-xs font-black uppercase tracking-[0.14em] shadow-hard">
              SCR-EXCEL-DOWNLOAD
            </div>
            <h1 className="font-head text-4xl font-black uppercase leading-none tracking-tight sm:text-5xl">
              엑셀 다운로드
            </h1>
            <p className="mt-4 max-w-3xl text-sm leading-relaxed text-muted-foreground sm:text-base">
              현재 조회조건과 사용자 데이터범위 권한을 서버에서 적용하여
              조회결과를 XLSX 파일로 생성하고, 원천 업무자료를 변경하지 않는
              다운로드 요청 이력을 조회합니다.
            </p>
          </div>
          <div className="rounded border-2 border-black bg-card px-4 py-3 font-head text-sm font-black shadow-hard">
            {user
              ? `${user.userId} / ${user.roles.join(", ")}`
              : "세션 확인 중"}
          </div>
        </div>

        <form
          className="grid grid-cols-1 gap-4 border-2 border-black bg-card p-4 shadow-hard sm:grid-cols-[minmax(0,1fr)_10rem_9rem_9rem] sm:p-5"
          onSubmit={(event) => {
            event.preventDefault();
            void loadExcelDownloads();
          }}
        >
          <label className="block">
            <span className="mb-2 block font-head text-sm font-bold">
              통합 검색
            </span>
            <input
              className="h-12 w-full rounded border-2 border-black bg-white px-4 py-2 shadow-hard"
              placeholder="파일명, 요청자, 조회조건"
              value={query}
              onChange={(event) => setQuery(event.target.value)}
            />
            {fieldErrors.q ? (
              <span className="mt-1.5 block text-xs text-destructive">
                {fieldErrors.q}
              </span>
            ) : null}
          </label>
          <label className="block">
            <span className="mb-2 block font-head text-sm font-bold">
              요청자
            </span>
            <input
              className="h-12 w-full rounded border-2 border-black bg-white px-4 py-2 shadow-hard"
              placeholder="admin"
              value={requesterFilter}
              onChange={(event) => setRequesterFilter(event.target.value)}
            />
          </label>
          <label className="block">
            <span className="mb-2 block font-head text-sm font-bold">
              표시 건수
            </span>
            <select
              className="h-12 w-full rounded border-2 border-black bg-white px-3 shadow-hard"
              value={size}
              onChange={(event) => setSize(Number(event.target.value))}
            >
              {sizeOptions.map((option) => (
                <option key={option} value={option}>
                  {option}건
                </option>
              ))}
            </select>
          </label>
          <div className="flex items-end gap-2">
            <button
              className="h-12 flex-1 rounded border-2 border-black bg-primary px-4 font-head font-black shadow-hard transition-all duration-200 hover:-translate-x-0.5 hover:-translate-y-0.5 hover:bg-primary-hover hover:shadow-hard-lg active:translate-x-1 active:translate-y-1 active:shadow-none"
              type="submit"
            >
              검색
            </button>
            <button
              className="h-12 rounded border-2 border-black bg-card px-4 font-head font-black shadow-hard"
              type="button"
              onClick={() => {
                setQuery("");
                setRequesterFilter("");
                void loadExcelDownloads("", "", size);
              }}
            >
              초기화
            </button>
          </div>
        </form>

        {message ? (
          <div
            role="status"
            className={`border-2 border-black px-4 py-3 font-bold shadow-hard ${state === "permission" || state === "error" ? "bg-destructive text-white" : "bg-accent text-black"}`}
          >
            {message}
          </div>
        ) : null}
        {state === "loading" ? (
          <StateCard
            title="LOADING"
            body="엑셀 다운로드 요청 이력을 불러오는 중입니다."
          />
        ) : null}
        {state === "empty" ? (
          <StateCard
            title="EMPTY"
            body="조건에 맞는 다운로드 요청이 없습니다. 검색 조건을 초기화해 보세요."
          />
        ) : null}
        {state === "permission" ? (
          <StateCard
            title="PERMISSION DENIED"
            body="R09 시스템관리자 권한과 엑셀 기능 권한이 필요합니다. 직접 API 요청도 서버에서 403으로 차단됩니다."
          />
        ) : null}

        {state === "success" || downloads.length > 0 ? (
          <section className="grid grid-cols-1 gap-4 lg:grid-cols-[minmax(0,1fr)_22rem]">
            <div className="overflow-hidden border-2 border-black bg-card shadow-hard">
              <div className="border-b-2 border-black bg-muted px-4 py-3 font-head font-black">
                excel_download_requests 목록
              </div>
              <div className="overflow-x-auto">
                <table className="w-full min-w-[70rem] border-collapse text-left text-sm">
                  <thead className="bg-primary font-head text-xs uppercase">
                    <tr>
                      {[
                        "ID",
                        "요청자",
                        "파일명",
                        "확장자",
                        "크기",
                        "조회조건",
                        "데이터범위",
                        "요청일시",
                      ].map((header) => (
                        <th
                          key={header}
                          className="border-b-2 border-black px-3 py-2"
                        >
                          {header}
                        </th>
                      ))}
                    </tr>
                  </thead>
                  <tbody>
                    {downloads.map((item) => (
                      <tr
                        key={item.downloadId}
                        className={`cursor-pointer border-b-2 border-black transition-colors hover:bg-accent ${selectedDownloadId === item.downloadId ? "bg-accent" : "bg-card"}`}
                        onClick={() => setSelectedDownloadId(item.downloadId)}
                      >
                        <td className="px-3 py-2 font-mono">
                          {item.downloadId}
                        </td>
                        <td className="px-3 py-2 font-bold">
                          {item.requesterId}
                        </td>
                        <td className="px-3 py-2">
                          {item.fileName ?? "생성 대기"}
                        </td>
                        <td className="px-3 py-2">
                          {item.extension ?? "xlsx"}
                        </td>
                        <td className="px-3 py-2">
                          {formatBytes(item.sizeBytes ?? 0)}
                        </td>
                        <td className="px-3 py-2 font-mono text-xs">
                          {compactJson(item.queryCondition)}
                        </td>
                        <td className="px-3 py-2 font-mono text-xs">
                          {compactJson(item.dataScopeApplied)}
                        </td>
                        <td className="px-3 py-2">
                          {item.createdAt?.replace("T", " ") ?? "-"}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>

            <aside className="relative">
              <div className="absolute inset-2 border-2 border-black bg-primary" />
              <div className="relative border-2 border-black bg-card shadow-hard">
                <div className="border-b-2 border-black bg-muted px-4 py-3 font-head font-black">
                  상세 / 다운로드 전 확인
                </div>
                {selectedDownload ? (
                  <div className="space-y-3 p-4 text-sm">
                    <ReadonlyField
                      label="다운로드 ID"
                      value={String(selectedDownload.downloadId)}
                    />
                    <ReadonlyField
                      label="요청자"
                      value={selectedDownload.requesterId}
                    />
                    <ReadonlyField
                      label="파일"
                      value={selectedDownload.fileName ?? "생성 대기"}
                    />
                    <ReadonlyField
                      label="데이터범위"
                      value={compactJson(selectedDownload.dataScopeApplied)}
                    />
                    <p className="border-2 border-black bg-muted p-3 text-xs leading-relaxed">
                      {selectedDownload.generationRule}
                    </p>
                    <p className="border-2 border-black bg-muted p-3 text-xs leading-relaxed">
                      {selectedDownload.boundaryRule}
                    </p>
                    <p className="border-2 border-black bg-accent p-3 text-xs leading-relaxed">
                      다운로드 ID와 요청자는 감사 식별자로 읽기 전용입니다. 저장
                      CTA는 현재 조회조건과 서버 강제 데이터범위만 기록하며 원천
                      업무자료를 수정하지 않습니다.
                    </p>
                    {fieldErrors.id ? (
                      <p className="text-xs font-bold text-destructive">
                        {fieldErrors.id}
                      </p>
                    ) : null}
                    {fieldErrors.queryCondition ? (
                      <p className="text-xs font-bold text-destructive">
                        {fieldErrors.queryCondition}
                      </p>
                    ) : null}
                    <button
                      className="h-12 w-full rounded border-2 border-black bg-primary px-4 font-head font-black shadow-hard transition-all duration-200 hover:-translate-x-0.5 hover:-translate-y-0.5 hover:bg-primary-hover hover:shadow-hard-lg active:translate-x-1 active:translate-y-1 active:shadow-none disabled:opacity-60"
                      type="button"
                      onClick={() => void createDownloadRequest()}
                      disabled={saving || !user?.roles.includes("R09")}
                    >
                      {saving
                        ? "xlsx 생성 요청 중..."
                        : "xlsx 다운로드 전 확인"}
                    </button>
                  </div>
                ) : (
                  <div className="p-4 text-sm">
                    행을 선택하면 상세 모달 영역이 표시됩니다.
                  </div>
                )}
              </div>
            </aside>
          </section>
        ) : null}
      </section>
    </AppShell>
  );
}

function PrivacyPolicyManagementPage() {
  const [user, setUser] = useState<AuthenticatedUser | null>(null);
  const [policies, setPolicies] = useState<PrivacyPolicyListItem[]>([]);
  const [selectedPolicyId, setSelectedPolicyId] = useState<number | null>(null);
  const [query, setQuery] = useState("");
  const [gradeFilter, setGradeFilter] = useState("");
  const [encryptionFilter, setEncryptionFilter] = useState("");
  const [size, setSize] = useState(20);
  const [state, setState] = useState<ViewState>("loading");
  const [message, setMessage] = useState("");
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});
  const [saving, setSaving] = useState(false);

  const selectedPolicy = useMemo(
    () =>
      policies.find((item) => item.fieldPolicyId === selectedPolicyId) ?? null,
    [selectedPolicyId, policies],
  );

  useEffect(() => {
    requestJson<AuthenticatedUser>("/api/auth/session")
      .then((sessionUser) => {
        setUser(sessionUser);
        return loadPrivacyPolicies("", "", "", size);
      })
      .catch((caught) => {
        const error = caught as Error & { status?: number };
        setState(error.status === 403 ? "permission" : "error");
        setMessage(error.message);
      });
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  async function loadPrivacyPolicies(
    nextQuery = query,
    nextGrade = gradeFilter,
    nextEncryption = encryptionFilter,
    nextSize = size,
  ) {
    setState("loading");
    setMessage("");
    const filterParts = [];
    if (nextGrade) {
      filterParts.push(`privacyGrade=${nextGrade}`);
    }
    if (nextEncryption) {
      filterParts.push(`encryptionEnabled=${nextEncryption}`);
    }
    const params = new URLSearchParams({
      page: "1",
      size: String(nextSize),
      sort: "fieldName",
    });
    if (nextQuery.trim()) {
      params.set("q", nextQuery.trim());
    }
    if (filterParts.length > 0) {
      params.set("filter", filterParts.join(";"));
    }
    try {
      const body = await requestJson<PrivacyPolicyListResponse>(
        `/api/admin/privacy-policies?${params.toString()}`,
      );
      setPolicies(body.items);
      setSelectedPolicyId(body.items[0]?.fieldPolicyId ?? null);
      setState(body.items.length === 0 ? "empty" : "success");
      setMessage(`총 ${body.totalCount}건 조회되었습니다.`);
    } catch (caught) {
      const error = caught as Error & { apiError?: ApiError; status?: number };
      setFieldErrors(error.apiError?.fields ?? {});
      setState(error.status === 403 ? "permission" : "error");
      setMessage(error.message);
    }
  }

  async function saveSelectedPrivacyPolicy() {
    if (!selectedPolicy) {
      setMessage("저장할 개인정보 정책을 먼저 선택하세요.");
      return;
    }
    const confirmed = window.confirm(
      `${selectedPolicy.fieldName} 개인정보 정책을 저장하시겠습니까? 실제 개인정보 값은 조회·수정하지 않습니다.`,
    );
    if (!confirmed) {
      return;
    }
    setSaving(true);
    setFieldErrors({});
    try {
      const nextGrade =
        selectedPolicy.privacyGrade === "SENSITIVE" ? "PERSONAL" : "SENSITIVE";
      const result = await requestJson<{
        fieldPolicyId: number;
        fieldName: string;
        privacyGrade: string;
        encryptionEnabled: boolean;
        logExcluded: boolean;
        message: string;
      }>("/api/admin/privacy-policies", {
        method: "POST",
        body: JSON.stringify({
          id: String(selectedPolicy.fieldPolicyId),
          fieldName: selectedPolicy.fieldName,
          privacyGrade: nextGrade,
          encryptionEnabled: true,
          maskingRule: selectedPolicy.maskingRule,
          logExcluded: true,
          reason: "개인정보 관리 화면 저장 CTA",
        }),
      });
      setMessage(
        `${result.message} ${result.fieldName} / ${result.privacyGrade}`,
      );
      await loadPrivacyPolicies();
      setSelectedPolicyId(result.fieldPolicyId);
    } catch (caught) {
      const error = caught as Error & { apiError?: ApiError; status?: number };
      setFieldErrors(error.apiError?.fields ?? {});
      setState(error.status === 403 ? "permission" : "error");
      setMessage(error.message);
    } finally {
      setSaving(false);
    }
  }

  if (!user && state === "error") {
    return (
      <AppShell>
        <LoginPanel
          onLogin={(nextUser) => {
            setUser(nextUser);
            void loadPrivacyPolicies();
          }}
        />
      </AppShell>
    );
  }

  return (
    <AppShell>
      <section className="space-y-7 sm:space-y-8 lg:space-y-9">
        <div className="flex flex-col gap-4 lg:flex-row lg:items-end lg:justify-between">
          <div>
            <div className="mb-3 inline-flex border-2 border-black bg-primary px-2.5 py-1 font-head text-xs font-black uppercase tracking-[0.14em] shadow-hard">
              SCR-PRIVACY
            </div>
            <h1 className="font-head text-4xl font-black uppercase leading-none tracking-tight sm:text-5xl">
              개인정보 관리
            </h1>
            <p className="mt-4 max-w-3xl text-sm leading-relaxed text-muted-foreground sm:text-base">
              이름·연락처·고유번호·연구자등록번호·계좌번호 등 필드별 개인정보
              등급, 암호화, 마스킹, 로그 제외 정책만 관리하며 실제 사용자
              개인정보 값은 표시하지 않습니다.
            </p>
          </div>
          <div className="rounded border-2 border-black bg-card px-4 py-3 font-head text-sm font-black shadow-hard">
            {user
              ? `${user.userId} / ${user.roles.join(", ")}`
              : "세션 확인 중"}
          </div>
        </div>

        <form
          className="grid grid-cols-1 gap-4 border-2 border-black bg-card p-4 shadow-hard sm:grid-cols-[minmax(0,1fr)_10rem_10rem_9rem_9rem] sm:p-5"
          onSubmit={(event) => {
            event.preventDefault();
            void loadPrivacyPolicies();
          }}
        >
          <label className="block">
            <span className="mb-2 block font-head text-sm font-bold">
              통합 검색
            </span>
            <input
              className="h-12 w-full rounded border-2 border-black bg-white px-4 py-2 shadow-hard"
              placeholder="필드명, 개인정보 등급, 마스킹 규칙"
              value={query}
              onChange={(event) => setQuery(event.target.value)}
            />
            {fieldErrors.q ? (
              <span className="mt-1.5 block text-xs text-destructive">
                {fieldErrors.q}
              </span>
            ) : null}
          </label>
          <label className="block">
            <span className="mb-2 block font-head text-sm font-bold">
              개인정보 등급
            </span>
            <select
              className="h-12 w-full rounded border-2 border-black bg-white px-3 shadow-hard"
              value={gradeFilter}
              onChange={(event) => setGradeFilter(event.target.value)}
            >
              <option value="">전체</option>
              <option value="PUBLIC">일반</option>
              <option value="PERSONAL">개인정보</option>
              <option value="SENSITIVE">민감정보</option>
            </select>
          </label>
          <label className="block">
            <span className="mb-2 block font-head text-sm font-bold">
              암호화
            </span>
            <select
              className="h-12 w-full rounded border-2 border-black bg-white px-3 shadow-hard"
              value={encryptionFilter}
              onChange={(event) => setEncryptionFilter(event.target.value)}
            >
              <option value="">전체</option>
              <option value="true">적용</option>
              <option value="false">미적용</option>
            </select>
          </label>
          <label className="block">
            <span className="mb-2 block font-head text-sm font-bold">
              표시 건수
            </span>
            <select
              className="h-12 w-full rounded border-2 border-black bg-white px-3 shadow-hard"
              value={size}
              onChange={(event) => setSize(Number(event.target.value))}
            >
              {sizeOptions.map((option) => (
                <option key={option} value={option}>
                  {option}건
                </option>
              ))}
            </select>
          </label>
          <div className="flex items-end gap-2">
            <button
              className="h-12 flex-1 rounded border-2 border-black bg-primary px-4 font-head font-black shadow-hard transition-all duration-200 hover:-translate-x-0.5 hover:-translate-y-0.5 hover:bg-primary-hover hover:shadow-hard-lg active:translate-x-1 active:translate-y-1 active:shadow-none"
              type="submit"
            >
              검색
            </button>
            <button
              className="h-12 rounded border-2 border-black bg-card px-4 font-head font-black shadow-hard"
              type="button"
              onClick={() => {
                setQuery("");
                setGradeFilter("");
                setEncryptionFilter("");
                void loadPrivacyPolicies("", "", "", size);
              }}
            >
              초기화
            </button>
          </div>
        </form>

        {message ? (
          <div
            role="status"
            className={`border-2 border-black px-4 py-3 font-bold shadow-hard ${state === "permission" || state === "error" ? "bg-destructive text-white" : "bg-accent text-black"}`}
          >
            {message}
          </div>
        ) : null}
        {state === "loading" ? (
          <StateCard
            title="LOADING"
            body="개인정보 정책 목록을 불러오는 중입니다."
          />
        ) : null}
        {state === "empty" ? (
          <StateCard
            title="EMPTY"
            body="조건에 맞는 개인정보 정책이 없습니다. 검색 조건을 초기화해 보세요."
          />
        ) : null}
        {state === "permission" ? (
          <StateCard
            title="PERMISSION DENIED"
            body="R09 시스템관리자 권한이 필요합니다. 직접 API 요청도 서버에서 403으로 차단됩니다."
          />
        ) : null}

        {state === "success" || policies.length > 0 ? (
          <section className="grid grid-cols-1 gap-4 lg:grid-cols-[minmax(0,1fr)_22rem]">
            <div className="overflow-hidden border-2 border-black bg-card shadow-hard">
              <div className="border-b-2 border-black bg-muted px-4 py-3 font-head font-black">
                privacy_field_policies 목록
              </div>
              <div className="overflow-x-auto">
                <table className="w-full min-w-[64rem] border-collapse text-left text-sm">
                  <thead className="bg-primary font-head text-xs uppercase">
                    <tr>
                      {[
                        "ID",
                        "필드명",
                        "등급",
                        "암호화",
                        "마스킹 규칙",
                        "로그 제외",
                        "정책 설명",
                      ].map((header) => (
                        <th
                          key={header}
                          className="border-b-2 border-black px-3 py-2"
                        >
                          {header}
                        </th>
                      ))}
                    </tr>
                  </thead>
                  <tbody>
                    {policies.map((item) => (
                      <tr
                        key={item.fieldPolicyId}
                        className={`cursor-pointer border-b-2 border-black transition-colors hover:bg-accent ${selectedPolicyId === item.fieldPolicyId ? "bg-accent" : "bg-card"}`}
                        onClick={() => setSelectedPolicyId(item.fieldPolicyId)}
                      >
                        <td className="px-3 py-2 font-mono">
                          {item.fieldPolicyId}
                        </td>
                        <td className="px-3 py-2 font-bold">
                          {item.fieldName}
                        </td>
                        <td className="px-3 py-2">
                          <span className="border-2 border-black bg-primary px-2 py-1 font-head text-xs font-black">
                            {item.privacyGradeName}
                          </span>
                        </td>
                        <td className="px-3 py-2">
                          {item.encryptionEnabled ? "AES-256-GCM" : "미적용"}
                        </td>
                        <td className="px-3 py-2">{item.maskingRule}</td>
                        <td className="px-3 py-2">
                          {item.logExcluded ? "제외" : "기록"}
                        </td>
                        <td className="px-3 py-2 text-xs">{item.policyRule}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>

            <aside className="relative">
              <div className="absolute inset-2 border-2 border-black bg-primary" />
              <div className="relative border-2 border-black bg-card shadow-hard">
                <div className="border-b-2 border-black bg-muted px-4 py-3 font-head font-black">
                  상세 / 저장 전 확인
                </div>
                {selectedPolicy ? (
                  <div className="space-y-3 p-4 text-sm">
                    <ReadonlyField
                      label="정책 ID"
                      value={String(selectedPolicy.fieldPolicyId)}
                    />
                    <ReadonlyField
                      label="필드명"
                      value={selectedPolicy.fieldName}
                    />
                    <ReadonlyField
                      label="개인정보 등급"
                      value={`${selectedPolicy.privacyGradeName} (${selectedPolicy.privacyGrade})`}
                    />
                    <ReadonlyField
                      label="암호화"
                      value={
                        selectedPolicy.encryptionEnabled
                          ? "AES-256-GCM / HMAC 검색 식별자"
                          : "미적용"
                      }
                    />
                    <ReadonlyField
                      label="마스킹"
                      value={selectedPolicy.maskingRule}
                    />
                    <ReadonlyField
                      label="로그 제외"
                      value={
                        selectedPolicy.logExcluded
                          ? "원문·처리값 제외"
                          : "처리이력 기록"
                      }
                    />
                    <p className="border-2 border-black bg-muted p-3 text-xs leading-relaxed">
                      {readonlyDisplayValue(
                        "정책 규칙",
                        selectedPolicy.policyRule,
                      )}
                    </p>
                    <p className="border-2 border-black bg-muted p-3 text-xs leading-relaxed">
                      {selectedPolicy.auditRule}
                    </p>
                    <p className="border-2 border-black bg-accent p-3 text-xs leading-relaxed">
                      정책 ID와 필드명은 생명주기 식별자로 읽기 전용입니다. 이
                      화면은 정책만 저장하며 실제 개인정보 원문을 조회·수정하지
                      않습니다.
                    </p>
                    {fieldErrors.id ? (
                      <p className="text-xs font-bold text-destructive">
                        {fieldErrors.id}
                      </p>
                    ) : null}
                    {fieldErrors.fieldName ? (
                      <p className="text-xs font-bold text-destructive">
                        {fieldErrors.fieldName}
                      </p>
                    ) : null}
                    {fieldErrors.privacyGrade ? (
                      <p className="text-xs font-bold text-destructive">
                        {fieldErrors.privacyGrade}
                      </p>
                    ) : null}
                    {fieldErrors.logExcluded ? (
                      <p className="text-xs font-bold text-destructive">
                        {fieldErrors.logExcluded}
                      </p>
                    ) : null}
                    <button
                      className="h-12 w-full rounded border-2 border-black bg-primary px-4 font-head font-black shadow-hard transition-all duration-200 hover:-translate-x-0.5 hover:-translate-y-0.5 hover:bg-primary-hover hover:shadow-hard-lg active:translate-x-1 active:translate-y-1 active:shadow-none disabled:opacity-60"
                      type="button"
                      onClick={() => void saveSelectedPrivacyPolicy()}
                      disabled={saving || !user?.roles.includes("R09")}
                    >
                      {saving ? "정책 저장 중..." : "정책 저장 전 확인"}
                    </button>
                  </div>
                ) : (
                  <div className="p-4 text-sm">
                    행을 선택하면 상세 모달 영역이 표시됩니다.
                  </div>
                )}
              </div>
            </aside>
          </section>
        ) : null}
      </section>
    </AppShell>
  );
}

function SessionManagementPage() {
  const [user, setUser] = useState<AuthenticatedUser | null>(null);
  const [sessions, setSessions] = useState<SessionListItem[]>([]);
  const [selectedSessionId, setSelectedSessionId] = useState<string>("");
  const [query, setQuery] = useState("");
  const [statusFilter, setStatusFilter] = useState("ACTIVE");
  const [ipFilter, setIpFilter] = useState("");
  const [terminationReason, setTerminationReason] =
    useState("보안 점검에 따른 관리자 강제종료");
  const [size, setSize] = useState(20);
  const [state, setState] = useState<ViewState>("loading");
  const [message, setMessage] = useState("");
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});
  const [saving, setSaving] = useState(false);

  const selectedSession = useMemo(
    () => sessions.find((item) => item.sessionId === selectedSessionId) ?? null,
    [selectedSessionId, sessions],
  );

  useEffect(() => {
    requestJson<AuthenticatedUser>("/api/auth/session")
      .then((sessionUser) => {
        setUser(sessionUser);
        return loadSessions("", "ACTIVE", "", size);
      })
      .catch((caught) => {
        const error = caught as Error & { status?: number };
        setState(error.status === 403 ? "permission" : "error");
        setMessage(error.message);
      });
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  async function loadSessions(
    nextQuery = query,
    nextStatus = statusFilter,
    nextIp = ipFilter,
    nextSize = size,
  ) {
    setState("loading");
    setMessage("");
    const filterParts = [];
    if (nextStatus) {
      filterParts.push(`status=${nextStatus}`);
    }
    if (nextIp.trim()) {
      filterParts.push(`ip=${nextIp.trim()}`);
    }
    const params = new URLSearchParams({
      page: "1",
      size: String(nextSize),
      sort: "lastActivityAt",
    });
    if (nextQuery.trim()) {
      params.set("q", nextQuery.trim());
    }
    if (filterParts.length > 0) {
      params.set("filter", filterParts.join(";"));
    }
    try {
      const body = await requestJson<SessionListResponse>(
        `/api/admin/sessions?${params.toString()}`,
      );
      setSessions(body.items);
      setSelectedSessionId(body.items[0]?.sessionId ?? "");
      setState(body.items.length === 0 ? "empty" : "success");
      setMessage(`총 ${body.totalCount}건 조회되었습니다.`);
    } catch (caught) {
      const error = caught as Error & { apiError?: ApiError; status?: number };
      setFieldErrors(error.apiError?.fields ?? {});
      setState(error.status === 403 ? "permission" : "error");
      setMessage(error.message);
    }
  }

  async function terminateSelectedSession() {
    if (!selectedSession) {
      setMessage("강제종료할 세션을 먼저 선택하세요.");
      return;
    }
    if (selectedSession.sessionStatus !== "ACTIVE") {
      setFieldErrors({ id: "활성 세션만 강제종료할 수 있습니다." });
      setMessage("이미 종료된 세션은 종료이력만 조회할 수 있습니다.");
      return;
    }
    const confirmed = window.confirm(
      `${selectedSession.userId} 세션을 강제종료하시겠습니까? 종료이력은 수정·삭제할 수 없습니다.`,
    );
    if (!confirmed) {
      return;
    }
    setSaving(true);
    setFieldErrors({});
    try {
      const result = await requestJson<{
        sessionId: string;
        sessionStatus: string;
        terminationType: string;
        message: string;
      }>("/api/admin/sessions", {
        method: "POST",
        body: JSON.stringify({
          id: selectedSession.sessionId,
          reason: terminationReason,
        }),
      });
      setMessage(
        `${result.message} ${result.sessionId} / ${result.terminationType}`,
      );
      await loadSessions();
      setSelectedSessionId(result.sessionId);
    } catch (caught) {
      const error = caught as Error & { apiError?: ApiError; status?: number };
      setFieldErrors(error.apiError?.fields ?? {});
      setState(error.status === 403 ? "permission" : "error");
      setMessage(error.message);
    } finally {
      setSaving(false);
    }
  }

  if (!user && state === "error") {
    return (
      <AppShell>
        <LoginPanel
          onLogin={(nextUser) => {
            setUser(nextUser);
            void loadSessions();
          }}
        />
      </AppShell>
    );
  }

  return (
    <AppShell>
      <section className="space-y-7 sm:space-y-8 lg:space-y-9">
        <div className="flex flex-col gap-4 lg:flex-row lg:items-end lg:justify-between">
          <div>
            <div className="mb-3 inline-flex border-2 border-black bg-primary px-2.5 py-1 font-head text-xs font-black uppercase tracking-[0.14em] shadow-hard">
              SCR-SESSION
            </div>
            <h1 className="font-head text-4xl font-black uppercase leading-none tracking-tight sm:text-5xl">
              접속현황 관리
            </h1>
            <p className="mt-4 max-w-3xl text-sm leading-relaxed text-muted-foreground sm:text-base">
              현재 활성 세션의 사용자, 로그인시각, 최종활동시각, IP, 세션상태를
              조회하고 R09 시스템관리자가 사유를 입력해 강제종료합니다.
            </p>
          </div>
          <div className="rounded border-2 border-black bg-card px-4 py-3 font-head text-sm font-black shadow-hard">
            {user
              ? `${user.userId} / ${user.roles.join(", ")}`
              : "세션 확인 중"}
          </div>
        </div>

        <form
          className="grid grid-cols-1 gap-4 border-2 border-black bg-card p-4 shadow-hard sm:grid-cols-[minmax(0,1fr)_10rem_10rem_9rem_9rem] sm:p-5"
          onSubmit={(event) => {
            event.preventDefault();
            void loadSessions();
          }}
        >
          <label className="block">
            <span className="mb-2 block font-head text-sm font-bold">
              통합 검색
            </span>
            <input
              className="h-12 w-full rounded border-2 border-black bg-white px-4 py-2 shadow-hard"
              placeholder="세션ID, 사용자, IP, 상태"
              value={query}
              onChange={(event) => setQuery(event.target.value)}
            />
            {fieldErrors.q ? (
              <span className="mt-1.5 block text-xs text-destructive">
                {fieldErrors.q}
              </span>
            ) : null}
          </label>
          <label className="block">
            <span className="mb-2 block font-head text-sm font-bold">
              세션상태
            </span>
            <select
              className="h-12 w-full rounded border-2 border-black bg-white px-3 shadow-hard"
              value={statusFilter}
              onChange={(event) => setStatusFilter(event.target.value)}
            >
              <option value="">전체</option>
              <option value="ACTIVE">활성</option>
              <option value="LOGOUT">로그아웃</option>
              <option value="IDLE_EXPIRED">유휴만료</option>
              <option value="ABSOLUTE_EXPIRED">절대만료</option>
              <option value="TERMINATED">강제종료</option>
            </select>
          </label>
          <label className="block">
            <span className="mb-2 block font-head text-sm font-bold">IP</span>
            <input
              className="h-12 w-full rounded border-2 border-black bg-white px-4 py-2 shadow-hard"
              placeholder="127.0.0.1"
              value={ipFilter}
              onChange={(event) => setIpFilter(event.target.value)}
            />
          </label>
          <label className="block">
            <span className="mb-2 block font-head text-sm font-bold">
              표시 건수
            </span>
            <select
              className="h-12 w-full rounded border-2 border-black bg-white px-3 shadow-hard"
              value={size}
              onChange={(event) => setSize(Number(event.target.value))}
            >
              {sizeOptions.map((option) => (
                <option key={option} value={option}>
                  {option}건
                </option>
              ))}
            </select>
          </label>
          <div className="flex items-end gap-2">
            <button
              className="h-12 flex-1 rounded border-2 border-black bg-primary px-4 font-head font-black shadow-hard transition-all duration-200 hover:-translate-x-0.5 hover:-translate-y-0.5 hover:bg-primary-hover hover:shadow-hard-lg active:translate-x-1 active:translate-y-1 active:shadow-none"
              type="submit"
            >
              검색
            </button>
            <button
              className="h-12 rounded border-2 border-black bg-card px-4 font-head font-black shadow-hard"
              type="button"
              onClick={() => {
                setQuery("");
                setStatusFilter("ACTIVE");
                setIpFilter("");
                void loadSessions("", "ACTIVE", "", size);
              }}
            >
              초기화
            </button>
          </div>
        </form>

        {message ? (
          <div
            role="status"
            className={`border-2 border-black px-4 py-3 font-bold shadow-hard ${state === "permission" || state === "error" ? "bg-destructive text-white" : "bg-accent text-black"}`}
          >
            {message}
          </div>
        ) : null}
        {state === "loading" ? (
          <StateCard
            title="LOADING"
            body="접속현황 목록을 불러오는 중입니다."
          />
        ) : null}
        {state === "empty" ? (
          <StateCard
            title="EMPTY"
            body="조건에 맞는 세션이 없습니다. 검색 조건을 초기화해 보세요."
          />
        ) : null}
        {state === "permission" ? (
          <StateCard
            title="PERMISSION DENIED"
            body="R09 시스템관리자 권한이 필요합니다. 직접 API 요청도 서버에서 403으로 차단됩니다."
          />
        ) : null}

        {state === "success" || sessions.length > 0 ? (
          <section className="grid grid-cols-1 gap-4 lg:grid-cols-[minmax(0,1fr)_23rem]">
            <div className="overflow-hidden border-2 border-black bg-card shadow-hard">
              <div className="border-b-2 border-black bg-muted px-4 py-3 font-head font-black">
                user_sessions 목록
              </div>
              <div className="overflow-x-auto">
                <table className="w-full min-w-[68rem] border-collapse text-left text-sm">
                  <thead className="bg-primary font-head text-xs uppercase">
                    <tr>
                      {[
                        "세션ID",
                        "사용자",
                        "로그인시각",
                        "최종활동시각",
                        "IP",
                        "상태",
                        "종료이력",
                      ].map((header) => (
                        <th
                          key={header}
                          className="border-b-2 border-black px-3 py-2"
                        >
                          {header}
                        </th>
                      ))}
                    </tr>
                  </thead>
                  <tbody>
                    {sessions.map((item) => (
                      <tr
                        key={item.sessionId}
                        className={`cursor-pointer border-b-2 border-black transition-colors hover:bg-accent ${selectedSessionId === item.sessionId ? "bg-accent" : "bg-card"}`}
                        onClick={() => setSelectedSessionId(item.sessionId)}
                      >
                        <td className="px-3 py-2 font-mono text-xs">
                          {item.sessionId}
                        </td>
                        <td className="px-3 py-2 font-bold">
                          {item.userDisplayName} ({item.userId})
                        </td>
                        <td className="px-3 py-2 font-mono text-xs">
                          {item.loginAt}
                        </td>
                        <td className="px-3 py-2 font-mono text-xs">
                          {item.lastActivityAt}
                        </td>
                        <td className="px-3 py-2 font-mono text-xs">
                          {item.ipAddress}
                        </td>
                        <td className="px-3 py-2">
                          <span className="border-2 border-black bg-primary px-2 py-1 font-head text-xs font-black">
                            {item.sessionStatusName}
                          </span>
                        </td>
                        <td className="px-3 py-2">
                          {item.latestTerminationType ?? "활성"}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>

            <aside className="relative">
              <div className="absolute inset-2 border-2 border-black bg-primary" />
              <div className="relative border-2 border-black bg-card shadow-hard">
                <div className="border-b-2 border-black bg-muted px-4 py-3 font-head font-black">
                  상세 / 강제종료 확인
                </div>
                {selectedSession ? (
                  <div className="space-y-3 p-4 text-sm">
                    <ReadonlyField
                      label="세션 ID"
                      value={selectedSession.sessionId}
                    />
                    <ReadonlyField
                      label="사용자"
                      value={`${selectedSession.userDisplayName} (${selectedSession.userId})`}
                    />
                    <ReadonlyField
                      label="로그인시각"
                      value={selectedSession.loginAt}
                    />
                    <ReadonlyField
                      label="최종활동시각"
                      value={selectedSession.lastActivityAt}
                    />
                    <ReadonlyField
                      label="IP"
                      value={selectedSession.ipAddress}
                    />
                    <ReadonlyField
                      label="세션상태"
                      value={`${selectedSession.sessionStatusName} (${selectedSession.sessionStatus})`}
                    />
                    <ReadonlyField
                      label="최근 종료유형"
                      value={
                        selectedSession.latestTerminationType ?? "활성 세션"
                      }
                    />
                    <p className="border-2 border-black bg-muted p-3 text-xs leading-relaxed">
                      {selectedSession.operationRule}
                    </p>
                    <label className="block">
                      <span className="mb-2 block font-head text-sm font-bold">
                        강제종료 사유
                      </span>
                      <textarea
                        className="min-h-24 w-full rounded border-2 border-black bg-white px-4 py-2 shadow-hard"
                        value={terminationReason}
                        onChange={(event) =>
                          setTerminationReason(event.target.value)
                        }
                      />
                    </label>
                    {fieldErrors.id ? (
                      <p className="text-xs font-bold text-destructive">
                        {fieldErrors.id}
                      </p>
                    ) : null}
                    {fieldErrors.reason ? (
                      <p className="text-xs font-bold text-destructive">
                        {fieldErrors.reason}
                      </p>
                    ) : null}
                    <button
                      className="h-12 w-full rounded border-2 border-black bg-primary px-4 font-head font-black shadow-hard transition-all duration-200 hover:-translate-x-0.5 hover:-translate-y-0.5 hover:bg-primary-hover hover:shadow-hard-lg active:translate-x-1 active:translate-y-1 active:shadow-none disabled:opacity-60"
                      type="button"
                      onClick={() => void terminateSelectedSession()}
                      disabled={
                        saving ||
                        selectedSession.sessionStatus !== "ACTIVE" ||
                        !user?.roles.includes("R09")
                      }
                    >
                      {saving ? "강제종료 중..." : "저장/실행 전 확인"}
                    </button>
                  </div>
                ) : (
                  <div className="p-4 text-sm">
                    행을 선택하면 상세 모달 영역이 표시됩니다.
                  </div>
                )}
              </div>
            </aside>
          </section>
        ) : null}
      </section>
    </AppShell>
  );
}

function AuditLogManagementPage() {
  const [user, setUser] = useState<AuthenticatedUser | null>(null);
  const [auditLogs, setAuditLogs] = useState<AuditLogListItem[]>([]);
  const [selectedAuditLogId, setSelectedAuditLogId] = useState<number | null>(
    null,
  );
  const [query, setQuery] = useState("");
  const [logTypeFilter, setLogTypeFilter] = useState("");
  const [resultFilter, setResultFilter] = useState("");
  const [actorFilter, setActorFilter] = useState("");
  const [confirmationReason, setConfirmationReason] =
    useState("감사 로그 관리 화면 확인");
  const [size, setSize] = useState(20);
  const [state, setState] = useState<ViewState>("loading");
  const [message, setMessage] = useState("");
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});
  const [saving, setSaving] = useState(false);

  const selectedAuditLog = useMemo(
    () =>
      auditLogs.find((item) => item.auditLogId === selectedAuditLogId) ?? null,
    [selectedAuditLogId, auditLogs],
  );

  useEffect(() => {
    requestJson<AuthenticatedUser>("/api/auth/session")
      .then((sessionUser) => {
        setUser(sessionUser);
        return loadAuditLogs("", "", "", "", size);
      })
      .catch((caught) => {
        const error = caught as Error & { status?: number };
        setState(error.status === 403 ? "permission" : "error");
        setMessage(error.message);
      });
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  async function loadAuditLogs(
    nextQuery = query,
    nextLogType = logTypeFilter,
    nextResult = resultFilter,
    nextActor = actorFilter,
    nextSize = size,
  ) {
    setState("loading");
    setMessage("");
    const filterParts = [];
    if (nextLogType) {
      filterParts.push(`logType=${nextLogType}`);
    }
    if (nextResult) {
      filterParts.push(`result=${nextResult}`);
    }
    if (nextActor.trim()) {
      filterParts.push(`actorId=${nextActor.trim()}`);
    }
    const params = new URLSearchParams({
      page: "1",
      size: String(nextSize),
      sort: "recent",
    });
    if (nextQuery.trim()) {
      params.set("q", nextQuery.trim());
    }
    if (filterParts.length > 0) {
      params.set("filter", filterParts.join(";"));
    }
    try {
      const body = await requestJson<AuditLogListResponse>(
        `/api/admin/audit-logs?${params.toString()}`,
      );
      setAuditLogs(body.items);
      setSelectedAuditLogId(body.items[0]?.auditLogId ?? null);
      setState(body.items.length === 0 ? "empty" : "success");
      setMessage(`총 ${body.totalCount}건 조회되었습니다.`);
    } catch (caught) {
      const error = caught as Error & { apiError?: ApiError; status?: number };
      setFieldErrors(error.apiError?.fields ?? {});
      setState(error.status === 403 ? "permission" : "error");
      setMessage(error.message);
    }
  }

  async function confirmSelectedAuditLog() {
    if (!selectedAuditLog) {
      setMessage("확인할 감사 로그를 먼저 선택하세요.");
      return;
    }
    const confirmed = window.confirm(
      `${selectedAuditLog.targetKey} 감사 로그를 확인 처리하시겠습니까? 원문은 수정·삭제되지 않습니다.`,
    );
    if (!confirmed) {
      return;
    }
    setSaving(true);
    setFieldErrors({});
    try {
      const result = await requestJson<{
        auditLogId: number;
        targetKey: string;
        result: string;
        message: string;
      }>("/api/admin/audit-logs", {
        method: "POST",
        body: JSON.stringify({
          id: String(selectedAuditLog.auditLogId),
          reason: confirmationReason,
        }),
      });
      setMessage(`${result.message} ${result.targetKey} / ${result.result}`);
      await loadAuditLogs();
      setSelectedAuditLogId(result.auditLogId);
    } catch (caught) {
      const error = caught as Error & { apiError?: ApiError; status?: number };
      setFieldErrors(error.apiError?.fields ?? {});
      setState(error.status === 403 ? "permission" : "error");
      setMessage(error.message);
    } finally {
      setSaving(false);
    }
  }

  if (!user && state === "error") {
    return (
      <AppShell>
        <LoginPanel
          onLogin={(nextUser) => {
            setUser(nextUser);
            void loadAuditLogs();
          }}
        />
      </AppShell>
    );
  }

  return (
    <AppShell>
      <section className="space-y-7 sm:space-y-8 lg:space-y-9">
        <div className="flex flex-col gap-4 lg:flex-row lg:items-end lg:justify-between">
          <div>
            <div className="mb-3 inline-flex border-2 border-black bg-primary px-2.5 py-1 font-head text-xs font-black uppercase tracking-[0.14em] shadow-hard">
              SCR-AUDIT-LOG
            </div>
            <h1 className="font-head text-4xl font-black uppercase leading-none tracking-tight sm:text-5xl">
              감사 로그 관리
            </h1>
            <p className="mt-4 max-w-3xl text-sm leading-relaxed text-muted-foreground sm:text-base">
              업무처리 행위 로그, 중요정보 조회 로그, 권한변경 로그를 검색하고
              변경 전후값 JSON을 읽기 전용으로 확인합니다. 확인 CTA는 원문을
              수정하지 않고 별도 감사 이력만 추가합니다.
            </p>
          </div>
          <div className="rounded border-2 border-black bg-card px-4 py-3 font-head text-sm font-black shadow-hard">
            {user
              ? `${user.userId} / ${user.roles.join(", ")}`
              : "세션 확인 중"}
          </div>
        </div>

        <form
          className="grid grid-cols-1 gap-4 border-2 border-black bg-card p-4 shadow-hard sm:grid-cols-[minmax(0,1fr)_10rem_10rem_10rem_9rem_9rem] sm:p-5"
          onSubmit={(event) => {
            event.preventDefault();
            void loadAuditLogs();
          }}
        >
          <label className="block">
            <span className="mb-2 block font-head text-sm font-bold">
              통합 검색
            </span>
            <input
              className="h-12 w-full rounded border-2 border-black bg-white px-4 py-2 shadow-hard"
              placeholder="대상키, 처리자, 로그유형, 결과"
              value={query}
              onChange={(event) => setQuery(event.target.value)}
            />
            {fieldErrors.q ? (
              <span className="mt-1.5 block text-xs text-destructive">
                {fieldErrors.q}
              </span>
            ) : null}
          </label>
          <label className="block">
            <span className="mb-2 block font-head text-sm font-bold">
              로그유형
            </span>
            <select
              className="h-12 w-full rounded border-2 border-black bg-white px-3 shadow-hard"
              value={logTypeFilter}
              onChange={(event) => setLogTypeFilter(event.target.value)}
            >
              <option value="">전체</option>
              <option value="LOGIN">로그인</option>
              <option value="LOGOUT">로그아웃</option>
              <option value="CREATE">등록</option>
              <option value="UPDATE">수정</option>
              <option value="DELETE">삭제</option>
              <option value="READ">조회</option>
              <option value="AUTHORIZATION">권한</option>
            </select>
          </label>
          <label className="block">
            <span className="mb-2 block font-head text-sm font-bold">결과</span>
            <select
              className="h-12 w-full rounded border-2 border-black bg-white px-3 shadow-hard"
              value={resultFilter}
              onChange={(event) => setResultFilter(event.target.value)}
            >
              <option value="">전체</option>
              <option value="SUCCESS">성공</option>
              <option value="DENIED">거부</option>
              <option value="FAILED">실패</option>
            </select>
          </label>
          <label className="block">
            <span className="mb-2 block font-head text-sm font-bold">
              처리자
            </span>
            <input
              className="h-12 w-full rounded border-2 border-black bg-white px-4 py-2 shadow-hard"
              placeholder="admin"
              value={actorFilter}
              onChange={(event) => setActorFilter(event.target.value)}
            />
          </label>
          <label className="block">
            <span className="mb-2 block font-head text-sm font-bold">
              표시 건수
            </span>
            <select
              className="h-12 w-full rounded border-2 border-black bg-white px-3 shadow-hard"
              value={size}
              onChange={(event) => setSize(Number(event.target.value))}
            >
              {sizeOptions.map((option) => (
                <option key={option} value={option}>
                  {option}건
                </option>
              ))}
            </select>
          </label>
          <div className="flex items-end gap-2">
            <button
              className="h-12 flex-1 rounded border-2 border-black bg-primary px-4 font-head font-black shadow-hard transition-all duration-200 hover:-translate-x-0.5 hover:-translate-y-0.5 hover:bg-primary-hover hover:shadow-hard-lg active:translate-x-1 active:translate-y-1 active:shadow-none"
              type="submit"
            >
              검색
            </button>
            <button
              className="h-12 rounded border-2 border-black bg-card px-4 font-head font-black shadow-hard"
              type="button"
              onClick={() => {
                setQuery("");
                setLogTypeFilter("");
                setResultFilter("");
                setActorFilter("");
                void loadAuditLogs("", "", "", "", size);
              }}
            >
              초기화
            </button>
          </div>
        </form>

        {message ? (
          <div
            role="status"
            className={`border-2 border-black px-4 py-3 font-bold shadow-hard ${state === "permission" || state === "error" ? "bg-destructive text-white" : "bg-accent text-black"}`}
          >
            {message}
          </div>
        ) : null}
        {state === "loading" ? (
          <StateCard
            title="LOADING"
            body="감사 로그 목록을 불러오는 중입니다."
          />
        ) : null}
        {state === "empty" ? (
          <StateCard
            title="EMPTY"
            body="조건에 맞는 감사 로그가 없습니다. 검색 조건을 초기화해 보세요."
          />
        ) : null}
        {state === "permission" ? (
          <StateCard
            title="PERMISSION DENIED"
            body="R09 시스템관리자 권한이 필요합니다. 직접 API 요청도 서버에서 403으로 차단됩니다."
          />
        ) : null}

        {state === "success" || auditLogs.length > 0 ? (
          <section className="grid grid-cols-1 gap-4 lg:grid-cols-[minmax(0,1fr)_24rem]">
            <div className="overflow-hidden border-2 border-black bg-card shadow-hard">
              <div className="border-b-2 border-black bg-muted px-4 py-3 font-head font-black">
                audit_logs 목록
              </div>
              <div className="overflow-x-auto">
                <table className="w-full min-w-[72rem] border-collapse text-left text-sm">
                  <thead className="bg-primary font-head text-xs uppercase">
                    <tr>
                      {[
                        "ID",
                        "유형",
                        "대상키",
                        "처리자",
                        "결과",
                        "변경 전",
                        "변경 후",
                      ].map((header) => (
                        <th
                          key={header}
                          className="border-b-2 border-black px-3 py-2"
                        >
                          {header}
                        </th>
                      ))}
                    </tr>
                  </thead>
                  <tbody>
                    {auditLogs.map((item) => (
                      <tr
                        key={item.auditLogId}
                        className={`cursor-pointer border-b-2 border-black transition-colors hover:bg-accent ${selectedAuditLogId === item.auditLogId ? "bg-accent" : "bg-card"}`}
                        onClick={() => setSelectedAuditLogId(item.auditLogId)}
                      >
                        <td className="px-3 py-2 font-mono text-xs">
                          {item.auditLogId}
                        </td>
                        <td className="px-3 py-2">
                          <span className="border-2 border-black bg-primary px-2 py-1 font-head text-xs font-black">
                            {item.logTypeName}
                          </span>
                        </td>
                        <td className="px-3 py-2 font-mono text-xs">
                          {item.targetKey}
                        </td>
                        <td className="px-3 py-2 font-bold">{item.actorId}</td>
                        <td className="px-3 py-2">{item.resultName}</td>
                        <td className="max-w-[16rem] truncate px-3 py-2 font-mono text-xs">
                          {compactJson(item.beforeValue)}
                        </td>
                        <td className="max-w-[16rem] truncate px-3 py-2 font-mono text-xs">
                          {compactJson(item.afterValue)}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>

            <aside className="relative">
              <div className="absolute inset-2 border-2 border-black bg-primary" />
              <div className="relative border-2 border-black bg-card shadow-hard">
                <div className="border-b-2 border-black bg-muted px-4 py-3 font-head font-black">
                  상세 / 확인 이력 기록
                </div>
                {selectedAuditLog ? (
                  <div className="space-y-3 p-4 text-sm">
                    <ReadonlyField
                      label="감사 로그 ID"
                      value={String(selectedAuditLog.auditLogId)}
                    />
                    <ReadonlyField
                      label="로그유형"
                      value={`${selectedAuditLog.logTypeName} (${selectedAuditLog.logType})`}
                    />
                    <ReadonlyField
                      label="대상키"
                      value={selectedAuditLog.targetKey}
                    />
                    <ReadonlyField
                      label="처리자"
                      value={selectedAuditLog.actorId}
                    />
                    <ReadonlyField
                      label="결과"
                      value={`${selectedAuditLog.resultName} (${selectedAuditLog.result})`}
                    />
                    <p className="border-2 border-black bg-muted p-3 font-mono text-xs leading-relaxed">
                      변경 전: {compactJson(selectedAuditLog.beforeValue)}
                    </p>
                    <p className="border-2 border-black bg-muted p-3 font-mono text-xs leading-relaxed">
                      변경 후: {compactJson(selectedAuditLog.afterValue)}
                    </p>
                    <p className="border-2 border-black bg-accent p-3 text-xs leading-relaxed">
                      {selectedAuditLog.operationRule}
                    </p>
                    <label className="block">
                      <span className="mb-2 block font-head text-sm font-bold">
                        확인 사유
                      </span>
                      <textarea
                        className="min-h-24 w-full rounded border-2 border-black bg-white px-4 py-2 shadow-hard"
                        value={confirmationReason}
                        onChange={(event) =>
                          setConfirmationReason(event.target.value)
                        }
                      />
                    </label>
                    {fieldErrors.id ? (
                      <p className="text-xs font-bold text-destructive">
                        {fieldErrors.id}
                      </p>
                    ) : null}
                    {fieldErrors.reason ? (
                      <p className="text-xs font-bold text-destructive">
                        {fieldErrors.reason}
                      </p>
                    ) : null}
                    <button
                      className="h-12 w-full rounded border-2 border-black bg-primary px-4 font-head font-black shadow-hard transition-all duration-200 hover:-translate-x-0.5 hover:-translate-y-0.5 hover:bg-primary-hover hover:shadow-hard-lg active:translate-x-1 active:translate-y-1 active:shadow-none disabled:opacity-60"
                      type="button"
                      onClick={() => void confirmSelectedAuditLog()}
                      disabled={saving || !user?.roles.includes("R09")}
                    >
                      {saving
                        ? "확인 이력 기록 중..."
                        : "확인 이력 기록 전 확인"}
                    </button>
                  </div>
                ) : (
                  <div className="p-4 text-sm">
                    행을 선택하면 상세 모달 영역이 표시됩니다.
                  </div>
                )}
              </div>
            </aside>
          </section>
        ) : null}
      </section>
    </AppShell>
  );
}

function BatchDefinitionManagementPage() {
  const [user, setUser] = useState<AuthenticatedUser | null>(null);
  const [definitions, setDefinitions] = useState<BatchDefinitionListItem[]>([]);
  const [selectedBatchId, setSelectedBatchId] = useState<string>("");
  const [query, setQuery] = useState("");
  const [ownerFilter, setOwnerFilter] = useState("");
  const [scheduleFilter, setScheduleFilter] = useState("");
  const [size, setSize] = useState(20);
  const [state, setState] = useState<ViewState>("loading");
  const [message, setMessage] = useState("");
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});
  const [saving, setSaving] = useState(false);
  const [saveReason, setSaveReason] = useState("배치 정의 정기 검토");

  const selectedDefinition = useMemo(
    () => definitions.find((item) => item.batchId === selectedBatchId) ?? null,
    [selectedBatchId, definitions],
  );

  useEffect(() => {
    requestJson<AuthenticatedUser>("/api/auth/session")
      .then((sessionUser) => {
        setUser(sessionUser);
        return loadBatchDefinitions("", "", "", size);
      })
      .catch((caught) => {
        const error = caught as Error & { status?: number };
        if (error.status === 403) {
          setState("permission");
          setMessage(error.message);
        } else {
          setState("error");
          setMessage(error.message);
        }
      });
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  async function loadBatchDefinitions(
    nextQuery = query,
    nextOwner = ownerFilter,
    nextSchedule = scheduleFilter,
    nextSize = size,
  ) {
    setState("loading");
    setMessage("");
    const params = new URLSearchParams({
      page: "1",
      size: String(nextSize),
      sort: "batchId",
    });
    if (nextQuery.trim()) {
      params.set("q", nextQuery.trim());
    }
    const filters: string[] = [];
    if (nextOwner.trim()) {
      filters.push(`ownerId=${nextOwner.trim()}`);
    }
    if (nextSchedule.trim()) {
      filters.push(`schedule=${nextSchedule.trim()}`);
    }
    if (filters.length > 0) {
      params.set("filter", filters.join(";"));
    }
    try {
      const body = await requestJson<BatchDefinitionListResponse>(
        `/api/admin/batch-definitions?${params.toString()}`,
      );
      setDefinitions(body.items);
      setSelectedBatchId(body.items[0]?.batchId ?? "");
      setState(body.items.length === 0 ? "empty" : "success");
      setMessage(
        body.items.length === 0
          ? "조건에 맞는 배치 정의가 없습니다."
          : `총 ${body.totalCount}건 조회되었습니다.`,
      );
    } catch (caught) {
      const error = caught as Error & { apiError?: ApiError; status?: number };
      setFieldErrors(error.apiError?.fields ?? {});
      setState(error.status === 403 ? "permission" : "error");
      setMessage(error.apiError?.message ?? error.message);
    }
  }

  async function saveSelectedBatchDefinition() {
    if (!selectedDefinition) {
      setMessage("저장할 배치 정의 행을 선택하세요.");
      return;
    }
    const confirmed = window.confirm(
      `${selectedDefinition.batchId} 배치 정의를 저장하시겠습니까? 이 화면에서는 즉시 실행·중지·재실행하지 않습니다.`,
    );
    if (!confirmed) {
      return;
    }
    setSaving(true);
    setFieldErrors({});
    try {
      const result = await requestJson<{
        batchId: string;
        status: string;
        message: string;
      }>("/api/admin/batch-definitions", {
        method: "POST",
        body: JSON.stringify({
          id: selectedDefinition.batchId,
          schedule: selectedDefinition.schedule,
          predecessorBatchId: selectedDefinition.predecessorBatchId ?? "",
          parameters: selectedDefinition.parameters,
          maxRuntimeSeconds: selectedDefinition.maxRuntimeSeconds,
          ownerId: selectedDefinition.ownerId,
          reason: saveReason,
        }),
      });
      setMessage(`${result.message} ${result.batchId} / ${result.status}`);
      await loadBatchDefinitions();
      setSelectedBatchId(result.batchId);
    } catch (caught) {
      const error = caught as Error & { apiError?: ApiError; status?: number };
      setFieldErrors(error.apiError?.fields ?? {});
      setState(error.status === 403 ? "permission" : "error");
      setMessage(error.apiError?.message ?? error.message);
    } finally {
      setSaving(false);
    }
  }

  if (!user && state !== "permission" && state !== "error") {
    return (
      <AppShell>
        <StateCard
          title="LOADING"
          body="배치 정의 관리 세션을 확인하고 있습니다."
        />
      </AppShell>
    );
  }

  if (!user && state === "error") {
    return (
      <AppShell>
        <LoginPanel
          onLogin={(nextUser) => {
            setUser(nextUser);
            void loadBatchDefinitions();
          }}
        />
      </AppShell>
    );
  }

  return (
    <AppShell>
      <section className="flex flex-col gap-6">
        <div className="relative">
          <div className="absolute inset-2 border-2 border-black bg-primary" />
          <div className="relative border-2 border-black bg-card p-5 shadow-hard sm:p-6">
            <div className="mb-3 inline-flex border-2 border-black bg-primary px-2.5 py-1 font-head text-xs font-black uppercase tracking-[0.14em] shadow-hard">
              SCR-BATCH-DEFINITION
            </div>
            <h1 className="font-head text-4xl font-black uppercase leading-none tracking-tight sm:text-5xl">
              배치 정의 관리
            </h1>
            <p className="mt-4 max-w-3xl text-sm leading-relaxed text-muted-foreground sm:text-base">
              평가자료 생성·연계·점수산출 등 배치의 배치ID, 실행주기, 선후행,
              파라미터, 최대실행시간과 담당자를 관리합니다. 즉시
              실행·중지·재실행은 배치 실행 관리 화면 범위입니다.
            </p>
          </div>
        </div>

        <form
          className="grid grid-cols-1 gap-4 border-2 border-black bg-card p-4 shadow-hard sm:grid-cols-[minmax(0,1fr)_10rem_10rem_9rem_9rem] sm:p-5"
          onSubmit={(event) => {
            event.preventDefault();
            void loadBatchDefinitions();
          }}
        >
          <label className="block">
            <span className="mb-2 block font-head text-sm font-bold">
              검색어
            </span>
            <input
              className="h-12 w-full rounded border-2 border-black bg-white px-3 shadow-hard"
              placeholder="배치ID, 실행주기, 담당자"
              value={query}
              onChange={(event) => setQuery(event.target.value)}
            />
          </label>
          <label className="block">
            <span className="mb-2 block font-head text-sm font-bold">
              담당자
            </span>
            <input
              className="h-12 w-full rounded border-2 border-black bg-white px-3 shadow-hard"
              placeholder="admin"
              value={ownerFilter}
              onChange={(event) => setOwnerFilter(event.target.value)}
            />
          </label>
          <label className="block">
            <span className="mb-2 block font-head text-sm font-bold">
              실행주기
            </span>
            <input
              className="h-12 w-full rounded border-2 border-black bg-white px-3 shadow-hard"
              placeholder="0 0"
              value={scheduleFilter}
              onChange={(event) => setScheduleFilter(event.target.value)}
            />
          </label>
          <label className="block">
            <span className="mb-2 block font-head text-sm font-bold">건수</span>
            <select
              className="h-12 w-full rounded border-2 border-black bg-white px-3 shadow-hard"
              value={size}
              onChange={(event) => {
                const nextSize = Number(event.target.value);
                setSize(nextSize);
                void loadBatchDefinitions(
                  query,
                  ownerFilter,
                  scheduleFilter,
                  nextSize,
                );
              }}
            >
              {sizeOptions.map((option) => (
                <option key={option} value={option}>
                  {option}
                </option>
              ))}
            </select>
          </label>
          <div className="flex items-end gap-2">
            <button
              className="h-12 rounded border-2 border-black bg-primary px-4 font-head font-black shadow-hard transition-all duration-200 hover:-translate-x-0.5 hover:-translate-y-0.5 hover:bg-primary-hover hover:shadow-hard-lg active:translate-x-1 active:translate-y-1 active:shadow-none"
              type="submit"
            >
              검색
            </button>
            <button
              className="h-12 rounded border-2 border-black bg-card px-4 font-head font-black shadow-hard"
              type="button"
              onClick={() => {
                setQuery("");
                setOwnerFilter("");
                setScheduleFilter("");
                void loadBatchDefinitions("", "", "", size);
              }}
            >
              초기화
            </button>
          </div>
        </form>

        {message ? (
          <div className="border-2 border-black bg-accent px-4 py-3 font-bold shadow-hard">
            {message}
          </div>
        ) : null}
        {fieldErrors.id ? (
          <p className="text-sm font-bold text-destructive">{fieldErrors.id}</p>
        ) : null}
        {fieldErrors.parameters ? (
          <p className="text-sm font-bold text-destructive">
            {fieldErrors.parameters}
          </p>
        ) : null}
        {fieldErrors.ownerId ? (
          <p className="text-sm font-bold text-destructive">
            {fieldErrors.ownerId}
          </p>
        ) : null}
        {state === "loading" ? (
          <StateCard
            title="LOADING"
            body="배치 정의 목록을 불러오는 중입니다."
          />
        ) : null}
        {state === "permission" ? (
          <StateCard
            title="PERMISSION DENIED"
            body="R09 시스템관리자 권한이 필요합니다. 직접 API 요청도 서버에서 403으로 차단됩니다."
          />
        ) : null}
        {state === "empty" ? (
          <StateCard
            title="EMPTY"
            body="조건에 맞는 자료가 없습니다. 검색조건을 유지한 상태로 다시 조회할 수 있습니다."
          />
        ) : null}
        {state === "error" ? (
          <StateCard
            title="ERROR"
            body={message || "배치 정의 관리 처리 중 오류가 발생했습니다."}
          />
        ) : null}

        {state === "success" || definitions.length > 0 ? (
          <section className="grid grid-cols-1 gap-5 lg:grid-cols-[minmax(0,1fr)_24rem]">
            <div className="overflow-hidden border-2 border-black bg-card shadow-hard">
              <div className="border-b-2 border-black bg-muted px-4 py-3 font-head font-black">
                batch_definitions 목록
              </div>
              <div className="overflow-x-auto">
                <table className="min-w-full border-collapse text-sm">
                  <thead className="bg-primary font-head text-xs uppercase">
                    <tr>
                      <th className="border-b-2 border-black px-3 py-2 text-left">
                        배치ID
                      </th>
                      <th className="border-b-2 border-black px-3 py-2 text-left">
                        실행주기
                      </th>
                      <th className="border-b-2 border-black px-3 py-2 text-left">
                        선행
                      </th>
                      <th className="border-b-2 border-black px-3 py-2 text-left">
                        최대실행
                      </th>
                      <th className="border-b-2 border-black px-3 py-2 text-left">
                        담당자
                      </th>
                      <th className="border-b-2 border-black px-3 py-2 text-left">
                        상태
                      </th>
                    </tr>
                  </thead>
                  <tbody>
                    {definitions.map((item) => (
                      <tr
                        key={item.batchId}
                        className={`cursor-pointer border-b-2 border-black transition-colors hover:bg-accent ${selectedBatchId === item.batchId ? "bg-accent" : "bg-card"}`}
                        onClick={() => setSelectedBatchId(item.batchId)}
                      >
                        <td className="px-3 py-2 font-mono text-xs">
                          {item.batchId}
                        </td>
                        <td className="px-3 py-2">{item.schedule}</td>
                        <td className="px-3 py-2">
                          {item.predecessorBatchId ?? "-"}
                        </td>
                        <td className="px-3 py-2">
                          {item.maxRuntimeSeconds}초
                        </td>
                        <td className="px-3 py-2">
                          {item.ownerName} ({item.ownerId})
                        </td>
                        <td className="px-3 py-2">
                          <span className="border-2 border-black bg-primary px-2 py-1 font-head text-xs font-black">
                            {item.statusName}
                          </span>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>
            <aside className="relative">
              <div className="absolute inset-2 border-2 border-black bg-[#01ffcc]" />
              <div className="relative border-2 border-black bg-card shadow-hard">
                <div className="border-b-2 border-black bg-muted px-4 py-3 font-head font-black">
                  상세/등록 모달
                </div>
                {selectedDefinition ? (
                  <div className="flex flex-col gap-3 p-4">
                    <ReadonlyField
                      label="배치 ID"
                      value={selectedDefinition.batchId}
                    />
                    <ReadonlyField
                      label="배치명"
                      value={selectedDefinition.batchName}
                    />
                    <ReadonlyField
                      label="실행주기"
                      value={selectedDefinition.schedule}
                    />
                    <ReadonlyField
                      label="선행 배치"
                      value={selectedDefinition.predecessorBatchId ?? "없음"}
                    />
                    <ReadonlyField
                      label="최대실행시간"
                      value={`${selectedDefinition.maxRuntimeSeconds}초`}
                    />
                    <ReadonlyField
                      label="담당자"
                      value={`${selectedDefinition.ownerName} (${selectedDefinition.ownerId})`}
                    />
                    <p className="border-2 border-black bg-muted p-3 font-mono text-xs leading-relaxed">
                      파라미터: {compactJson(selectedDefinition.parameters)}
                    </p>
                    <p className="border-2 border-black bg-accent p-3 text-xs leading-relaxed">
                      {selectedDefinition.operationRule}
                    </p>
                    <label className="block">
                      <span className="mb-2 block font-head text-sm font-bold">
                        저장 사유
                      </span>
                      <textarea
                        className="min-h-24 w-full rounded border-2 border-black bg-white px-4 py-2 shadow-hard"
                        value={saveReason}
                        onChange={(event) => setSaveReason(event.target.value)}
                      />
                    </label>
                    {fieldErrors.reason ? (
                      <p className="text-xs font-bold text-destructive">
                        {fieldErrors.reason}
                      </p>
                    ) : null}
                    <button
                      className="h-12 w-full rounded border-2 border-black bg-primary px-4 font-head font-black shadow-hard transition-all duration-200 hover:-translate-x-0.5 hover:-translate-y-0.5 hover:bg-primary-hover hover:shadow-hard-lg active:translate-x-1 active:translate-y-1 active:shadow-none disabled:opacity-60"
                      type="button"
                      onClick={() => void saveSelectedBatchDefinition()}
                      disabled={saving || !user?.roles.includes("R09")}
                    >
                      {saving ? "저장 중..." : "저장 전 확인"}
                    </button>
                  </div>
                ) : (
                  <div className="p-4 text-sm">
                    행을 선택하면 상세 모달 영역이 표시됩니다.
                  </div>
                )}
              </div>
            </aside>
          </section>
        ) : null}
      </section>
    </AppShell>
  );
}

function BatchExecutionManagementPage() {
  const [user, setUser] = useState<AuthenticatedUser | null>(null);
  const [executions, setExecutions] = useState<BatchExecutionListItem[]>([]);
  const [selectedExecutionId, setSelectedExecutionId] = useState<number | null>(
    null,
  );
  const [query, setQuery] = useState("");
  const [batchFilter, setBatchFilter] = useState("");
  const [statusFilter, setStatusFilter] = useState("");
  const [size, setSize] = useState(20);
  const [state, setState] = useState<ViewState>("loading");
  const [message, setMessage] = useState("");
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});
  const [saving, setSaving] = useState(false);
  const [actionReason, setActionReason] = useState("운영자 수동 실행 요청");

  const selectedExecution = useMemo(
    () =>
      executions.find(
        (item) => item.batchExecutionId === selectedExecutionId,
      ) ?? null,
    [selectedExecutionId, executions],
  );

  useEffect(() => {
    requestJson<AuthenticatedUser>("/api/auth/session")
      .then((sessionUser) => {
        setUser(sessionUser);
        return loadBatchExecutions("", "", "", size);
      })
      .catch((caught) => {
        const error = caught as Error & { status?: number };
        setState(error.status === 403 ? "permission" : "error");
        setMessage(error.message);
      });
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  async function loadBatchExecutions(
    nextQuery = query,
    nextBatch = batchFilter,
    nextStatus = statusFilter,
    nextSize = size,
  ) {
    setState("loading");
    setMessage("");
    const params = new URLSearchParams({
      page: "1",
      size: String(nextSize),
      sort: "recent",
    });
    if (nextQuery.trim()) {
      params.set("q", nextQuery.trim());
    }
    const filters: string[] = [];
    if (nextBatch.trim()) {
      filters.push(`batchId=${nextBatch.trim()}`);
    }
    if (nextStatus) {
      filters.push(`status=${nextStatus}`);
    }
    if (filters.length > 0) {
      params.set("filter", filters.join(";"));
    }
    try {
      const body = await requestJson<BatchExecutionListResponse>(
        `/api/admin/batch-executions?${params.toString()}`,
      );
      setExecutions(body.items);
      setSelectedExecutionId(body.items[0]?.batchExecutionId ?? null);
      setState(body.items.length === 0 ? "empty" : "success");
      setMessage(
        body.items.length === 0
          ? "조건에 맞는 배치 실행 이력이 없습니다."
          : `총 ${body.totalCount}건 조회되었습니다.`,
      );
    } catch (caught) {
      const error = caught as Error & { apiError?: ApiError; status?: number };
      setFieldErrors(error.apiError?.fields ?? {});
      setState(error.status === 403 ? "permission" : "error");
      setMessage(error.apiError?.message ?? error.message);
    }
  }

  async function runSelectedBatch() {
    if (!selectedExecution) {
      setMessage("실행할 배치 행을 먼저 선택하세요.");
      return;
    }
    const confirmed = window.confirm(
      `${selectedExecution.batchId} 배치를 수동실행하시겠습니까? 실행주기·선후행 관계와 원천 업무자료는 변경하지 않습니다.`,
    );
    if (!confirmed) {
      return;
    }
    setSaving(true);
    setFieldErrors({});
    try {
      const result = await requestJson<{
        batchExecutionId: number;
        batchId: string;
        executionStatus: string;
        message: string;
      }>("/api/admin/batch-executions", {
        method: "POST",
        body: JSON.stringify({
          id: selectedExecution.batchId,
          parameters: selectedExecution.parameters,
          reason: actionReason,
        }),
      });
      setMessage(
        `${result.message} ${result.batchId} / ${result.executionStatus}`,
      );
      await loadBatchExecutions();
      setSelectedExecutionId(result.batchExecutionId);
    } catch (caught) {
      const error = caught as Error & { apiError?: ApiError; status?: number };
      setFieldErrors(error.apiError?.fields ?? {});
      setState(error.status === 403 ? "permission" : "error");
      setMessage(error.apiError?.message ?? error.message);
    } finally {
      setSaving(false);
    }
  }

  async function actOnSelectedExecution(action: "stop" | "rerun") {
    if (!selectedExecution) {
      setMessage("처리할 배치 실행 이력을 먼저 선택하세요.");
      return;
    }
    const actionName = action === "stop" ? "중지" : "재실행";
    const confirmed = window.confirm(
      `${selectedExecution.batchExecutionId}번 배치 실행을 ${actionName}하시겠습니까? 사유가 감사로그에 기록됩니다.`,
    );
    if (!confirmed) {
      return;
    }
    setSaving(true);
    setFieldErrors({});
    try {
      const result = await requestJson<{
        batchExecutionId: number;
        batchId: string;
        executionStatus: string;
        message: string;
      }>(
        `/api/admin/batch-executions/${selectedExecution.batchExecutionId}/${action}`,
        {
          method: "POST",
          body: JSON.stringify({ reason: actionReason }),
        },
      );
      setMessage(
        `${result.message} ${result.batchId} / ${result.executionStatus}`,
      );
      await loadBatchExecutions();
      setSelectedExecutionId(result.batchExecutionId);
    } catch (caught) {
      const error = caught as Error & { apiError?: ApiError; status?: number };
      setFieldErrors(error.apiError?.fields ?? {});
      setState(error.status === 403 ? "permission" : "error");
      setMessage(error.apiError?.message ?? error.message);
    } finally {
      setSaving(false);
    }
  }

  if (!user && state !== "permission" && state !== "error") {
    return (
      <AppShell>
        <StateCard
          title="LOADING"
          body="배치 실행 관리 세션을 확인하고 있습니다."
        />
      </AppShell>
    );
  }

  if (!user && state === "error") {
    return (
      <AppShell>
        <LoginPanel
          onLogin={(nextUser) => {
            setUser(nextUser);
            void loadBatchExecutions();
          }}
        />
      </AppShell>
    );
  }

  return (
    <AppShell>
      <section className="flex flex-col gap-6">
        <div className="relative">
          <div className="absolute inset-2 border-2 border-black bg-primary" />
          <div className="relative border-2 border-black bg-card p-5 shadow-hard sm:p-6">
            <div className="mb-3 inline-flex border-2 border-black bg-primary px-2.5 py-1 font-head text-xs font-black uppercase tracking-[0.14em] shadow-hard">
              SCR-BATCH-EXECUTION
            </div>
            <h1 className="font-head text-4xl font-black uppercase leading-none tracking-tight sm:text-5xl">
              배치 실행 관리
            </h1>
            <p className="mt-4 max-w-3xl text-sm leading-relaxed text-muted-foreground sm:text-base">
              권한 있는 운영자가 배치를 수동실행·중지·재실행하고 실행 파라미터와
              사유를 기록합니다. 배치 정의·실행주기·선후행 관계와 실패 건 원천
              업무자료는 직접 수정하지 않습니다.
            </p>
          </div>
        </div>

        <form
          className="grid grid-cols-1 gap-4 border-2 border-black bg-card p-4 shadow-hard sm:grid-cols-[minmax(0,1fr)_12rem_10rem_9rem_9rem] sm:p-5"
          onSubmit={(event) => {
            event.preventDefault();
            void loadBatchExecutions();
          }}
        >
          <label className="block">
            <span className="mb-2 block font-head text-sm font-bold">
              검색어
            </span>
            <input
              className="h-12 w-full rounded border-2 border-black bg-white px-3 shadow-hard"
              placeholder="배치ID, 사유, 요청자"
              value={query}
              onChange={(event) => setQuery(event.target.value)}
            />
          </label>
          <label className="block">
            <span className="mb-2 block font-head text-sm font-bold">
              배치ID
            </span>
            <input
              className="h-12 w-full rounded border-2 border-black bg-white px-3 shadow-hard"
              placeholder="COMMON-AUDIT-ROLLUP"
              value={batchFilter}
              onChange={(event) => setBatchFilter(event.target.value)}
            />
          </label>
          <label className="block">
            <span className="mb-2 block font-head text-sm font-bold">
              실행상태
            </span>
            <select
              className="h-12 w-full rounded border-2 border-black bg-white px-3 shadow-hard"
              value={statusFilter}
              onChange={(event) => setStatusFilter(event.target.value)}
            >
              <option value="">전체</option>
              <option value="REQUESTED">요청</option>
              <option value="RUNNING">실행중</option>
              <option value="SUCCESS">성공</option>
              <option value="FAILED">실패</option>
              <option value="CANCELLED">중지</option>
            </select>
          </label>
          <label className="block">
            <span className="mb-2 block font-head text-sm font-bold">건수</span>
            <select
              className="h-12 w-full rounded border-2 border-black bg-white px-3 shadow-hard"
              value={size}
              onChange={(event) => {
                const nextSize = Number(event.target.value);
                setSize(nextSize);
                void loadBatchExecutions(
                  query,
                  batchFilter,
                  statusFilter,
                  nextSize,
                );
              }}
            >
              {sizeOptions.map((option) => (
                <option key={option} value={option}>
                  {option}
                </option>
              ))}
            </select>
          </label>
          <div className="flex items-end gap-2">
            <button
              className="h-12 rounded border-2 border-black bg-primary px-4 font-head font-black shadow-hard transition-all duration-200 hover:-translate-x-0.5 hover:-translate-y-0.5 hover:bg-primary-hover hover:shadow-hard-lg active:translate-x-1 active:translate-y-1 active:shadow-none"
              type="submit"
            >
              검색
            </button>
            <button
              className="h-12 rounded border-2 border-black bg-card px-4 font-head font-black shadow-hard"
              type="button"
              onClick={() => {
                setQuery("");
                setBatchFilter("");
                setStatusFilter("");
                void loadBatchExecutions("", "", "", size);
              }}
            >
              초기화
            </button>
          </div>
        </form>

        {message ? (
          <div
            role="status"
            className={`border-2 border-black px-4 py-3 font-bold shadow-hard ${state === "permission" || state === "error" ? "bg-destructive text-white" : "bg-accent text-black"}`}
          >
            {message}
          </div>
        ) : null}
        {fieldErrors.id ? (
          <p className="text-sm font-bold text-destructive">{fieldErrors.id}</p>
        ) : null}
        {fieldErrors.parameters ? (
          <p className="text-sm font-bold text-destructive">
            {fieldErrors.parameters}
          </p>
        ) : null}
        {fieldErrors.reason ? (
          <p className="text-sm font-bold text-destructive">
            {fieldErrors.reason}
          </p>
        ) : null}
        {state === "loading" ? (
          <StateCard
            title="LOADING"
            body="배치 실행 이력을 불러오는 중입니다."
          />
        ) : null}
        {state === "permission" ? (
          <StateCard
            title="PERMISSION DENIED"
            body="R09 시스템관리자 권한이 필요합니다. 직접 API 요청도 서버에서 403으로 차단됩니다."
          />
        ) : null}
        {state === "empty" ? (
          <StateCard
            title="EMPTY"
            body="조건에 맞는 배치 실행 이력이 없습니다. 배치 정의 화면에서 등록된 배치를 확인한 뒤 실행하세요."
          />
        ) : null}
        {state === "error" ? (
          <StateCard
            title="ERROR"
            body={message || "배치 실행 관리 처리 중 오류가 발생했습니다."}
          />
        ) : null}

        {state === "success" || executions.length > 0 ? (
          <section className="grid grid-cols-1 gap-5 lg:grid-cols-[minmax(0,1fr)_25rem]">
            <div className="overflow-hidden border-2 border-black bg-card shadow-hard">
              <div className="border-b-2 border-black bg-muted px-4 py-3 font-head font-black">
                batch_executions 목록
              </div>
              <div className="overflow-x-auto">
                <table className="min-w-full border-collapse text-sm">
                  <thead className="bg-primary font-head text-xs uppercase">
                    <tr>
                      {[
                        "실행ID",
                        "배치ID",
                        "파라미터",
                        "상태",
                        "요청자",
                        "사유",
                      ].map((header) => (
                        <th
                          key={header}
                          className="border-b-2 border-black px-3 py-2 text-left"
                        >
                          {header}
                        </th>
                      ))}
                    </tr>
                  </thead>
                  <tbody>
                    {executions.map((item) => (
                      <tr
                        key={item.batchExecutionId}
                        className={`cursor-pointer border-b-2 border-black transition-colors hover:bg-accent ${selectedExecutionId === item.batchExecutionId ? "bg-accent" : "bg-card"}`}
                        onClick={() =>
                          setSelectedExecutionId(item.batchExecutionId)
                        }
                      >
                        <td className="px-3 py-2 font-mono text-xs">
                          {item.batchExecutionId}
                        </td>
                        <td className="px-3 py-2 font-mono text-xs">
                          {item.batchId}
                        </td>
                        <td className="max-w-[16rem] truncate px-3 py-2 font-mono text-xs">
                          {compactJson(item.parameters)}
                        </td>
                        <td className="px-3 py-2">
                          <span className="border-2 border-black bg-primary px-2 py-1 font-head text-xs font-black">
                            {item.executionStatusName}
                          </span>
                        </td>
                        <td className="px-3 py-2">
                          {item.requestedByName} ({item.requestedBy})
                        </td>
                        <td className="max-w-[16rem] truncate px-3 py-2">
                          {item.reason}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>
            <aside className="relative">
              <div className="absolute inset-2 border-2 border-black bg-[#01ffcc]" />
              <div className="relative border-2 border-black bg-card shadow-hard">
                <div className="border-b-2 border-black bg-muted px-4 py-3 font-head font-black">
                  상세/실행 모달
                </div>
                {selectedExecution ? (
                  <div className="flex flex-col gap-3 p-4">
                    <ReadonlyField
                      label="실행 ID"
                      value={String(selectedExecution.batchExecutionId)}
                    />
                    <ReadonlyField
                      label="배치"
                      value={`${selectedExecution.batchName} (${selectedExecution.batchId})`}
                    />
                    <ReadonlyField
                      label="상태"
                      value={`${selectedExecution.executionStatusName} (${selectedExecution.executionStatus})`}
                    />
                    <ReadonlyField
                      label="요청자"
                      value={`${selectedExecution.requestedByName} (${selectedExecution.requestedBy})`}
                    />
                    <p className="border-2 border-black bg-muted p-3 font-mono text-xs leading-relaxed">
                      파라미터: {compactJson(selectedExecution.parameters)}
                    </p>
                    <p className="border-2 border-black bg-accent p-3 text-xs leading-relaxed">
                      {selectedExecution.operationRule}
                    </p>
                    <label className="block">
                      <span className="mb-2 block font-head text-sm font-bold">
                        실행/중지/재실행 사유
                      </span>
                      <textarea
                        className="min-h-24 w-full rounded border-2 border-black bg-white px-4 py-2 shadow-hard"
                        value={actionReason}
                        onChange={(event) =>
                          setActionReason(event.target.value)
                        }
                      />
                    </label>
                    <button
                      className="h-12 w-full rounded border-2 border-black bg-primary px-4 font-head font-black shadow-hard transition-all duration-200 hover:-translate-x-0.5 hover:-translate-y-0.5 hover:bg-primary-hover hover:shadow-hard-lg active:translate-x-1 active:translate-y-1 active:shadow-none disabled:opacity-60"
                      type="button"
                      onClick={() => void runSelectedBatch()}
                      disabled={saving || !user?.roles.includes("R09")}
                    >
                      {saving ? "처리 중..." : "수동실행 전 확인"}
                    </button>
                    <div className="grid grid-cols-2 gap-2">
                      <button
                        className="h-11 rounded border-2 border-black bg-card px-3 font-head font-black shadow-hard disabled:opacity-60"
                        type="button"
                        onClick={() => void actOnSelectedExecution("stop")}
                        disabled={
                          saving ||
                          !["REQUESTED", "RUNNING"].includes(
                            selectedExecution.executionStatus,
                          ) ||
                          !user?.roles.includes("R09")
                        }
                      >
                        중지 전 확인
                      </button>
                      <button
                        className="h-11 rounded border-2 border-black bg-card px-3 font-head font-black shadow-hard disabled:opacity-60"
                        type="button"
                        onClick={() => void actOnSelectedExecution("rerun")}
                        disabled={saving || !user?.roles.includes("R09")}
                      >
                        재실행 전 확인
                      </button>
                    </div>
                  </div>
                ) : (
                  <div className="p-4 text-sm">
                    행을 선택하면 상세 모달 영역이 표시됩니다.
                  </div>
                )}
              </div>
            </aside>
          </section>
        ) : null}
      </section>
    </AppShell>
  );
}

function BatchResultManagementPage() {
  const [user, setUser] = useState<AuthenticatedUser | null>(null);
  const [results, setResults] = useState<BatchResultListItem[]>([]);
  const [selectedResultId, setSelectedResultId] = useState<number | null>(null);
  const [query, setQuery] = useState("");
  const [batchFilter, setBatchFilter] = useState("");
  const [statusFilter, setStatusFilter] = useState("");
  const [size, setSize] = useState(20);
  const [state, setState] = useState<ViewState>("loading");
  const [message, setMessage] = useState("");
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});
  const [loadingLog, setLoadingLog] = useState(false);
  const [logPreview, setLogPreview] = useState<BatchResultLogResponse | null>(
    null,
  );

  const selectedResult = useMemo(
    () =>
      results.find((item) => item.batchResultId === selectedResultId) ?? null,
    [selectedResultId, results],
  );

  useEffect(() => {
    requestJson<AuthenticatedUser>("/api/auth/session")
      .then((sessionUser) => {
        setUser(sessionUser);
        return loadBatchResults("", "", "", size);
      })
      .catch((caught) => {
        const error = caught as Error & { status?: number };
        setState(error.status === 403 ? "permission" : "error");
        setMessage(error.message);
      });
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  async function loadBatchResults(
    nextQuery = query,
    nextBatch = batchFilter,
    nextStatus = statusFilter,
    nextSize = size,
  ) {
    setState("loading");
    setMessage("");
    setLogPreview(null);
    const params = new URLSearchParams({
      page: "1",
      size: String(nextSize),
      sort: "startedAt",
    });
    if (nextQuery.trim()) {
      params.set("q", nextQuery.trim());
    }
    const filters: string[] = [];
    if (nextBatch.trim()) {
      filters.push(`batchId=${nextBatch.trim()}`);
    }
    if (nextStatus) {
      filters.push(`resultStatus=${nextStatus}`);
    }
    if (filters.length > 0) {
      params.set("filter", filters.join(";"));
    }
    try {
      const body = await requestJson<BatchResultListResponse>(
        `/api/admin/batch-results?${params.toString()}`,
      );
      setResults(body.items);
      setSelectedResultId(body.items[0]?.batchResultId ?? null);
      setState(body.items.length === 0 ? "empty" : "success");
      setMessage(
        body.items.length === 0
          ? "조건에 맞는 배치 결과가 없습니다."
          : `총 ${body.totalCount}건 조회되었습니다.`,
      );
    } catch (caught) {
      const error = caught as Error & { apiError?: ApiError; status?: number };
      setFieldErrors(error.apiError?.fields ?? {});
      setState(error.status === 403 ? "permission" : "error");
      setMessage(error.apiError?.message ?? error.message);
    }
  }

  async function viewSelectedLog() {
    if (!selectedResult) {
      setMessage("로그를 조회할 배치 결과 행을 먼저 선택하세요.");
      return;
    }
    const confirmed = window.confirm(
      `${selectedResult.batchExecutionId}번 실행에 연결된 로그파일만 조회합니다. 로그는 수정·삭제되지 않습니다.`,
    );
    if (!confirmed) {
      return;
    }
    setLoadingLog(true);
    setFieldErrors({});
    try {
      const result = await requestJson<BatchResultLogResponse>(
        `/api/admin/batch-results/${selectedResult.batchResultId}/log`,
      );
      setLogPreview(result);
      setMessage(result.accessMessage);
    } catch (caught) {
      const error = caught as Error & { apiError?: ApiError; status?: number };
      setFieldErrors(error.apiError?.fields ?? {});
      setState(error.status === 403 ? "permission" : state);
      setMessage(error.apiError?.message ?? error.message);
    } finally {
      setLoadingLog(false);
    }
  }

  if (!user && state !== "permission" && state !== "error") {
    return (
      <AppShell>
        <StateCard
          title="LOADING"
          body="배치 결과 조회 세션을 확인하고 있습니다."
        />
      </AppShell>
    );
  }

  if (!user && state === "error") {
    return (
      <AppShell>
        <LoginPanel
          onLogin={(nextUser) => {
            setUser(nextUser);
            void loadBatchResults();
          }}
        />
      </AppShell>
    );
  }

  return (
    <AppShell>
      <section className="flex flex-col gap-6">
        <div className="relative">
          <div className="absolute inset-2 border-2 border-black bg-primary" />
          <div className="relative border-2 border-black bg-card p-5 shadow-hard sm:p-6">
            <div className="mb-3 inline-flex border-2 border-black bg-primary px-2.5 py-1 font-head text-xs font-black uppercase tracking-[0.14em] shadow-hard">
              SCR-BATCH-RESULT
            </div>
            <h1 className="font-head text-4xl font-black uppercase leading-none tracking-tight sm:text-5xl">
              배치 결과 조회
            </h1>
            <p className="mt-4 max-w-3xl text-sm leading-relaxed text-muted-foreground sm:text-base">
              배치 실행ID별 시작·종료시간, 처리건수, 성공·실패·제외건수,
              소요시간과 로그파일을 조회합니다. 이 화면에서는 배치를
              재실행하거나 실패자료·로그를 수정·삭제하지 않습니다.
            </p>
          </div>
        </div>

        <form
          className="grid grid-cols-1 gap-4 border-2 border-black bg-card p-4 shadow-hard sm:grid-cols-[minmax(0,1fr)_12rem_10rem_9rem_9rem] sm:p-5"
          onSubmit={(event) => {
            event.preventDefault();
            void loadBatchResults();
          }}
        >
          <label className="block">
            <span className="mb-2 block font-head text-sm font-bold">
              검색어
            </span>
            <input
              className="h-12 w-full rounded border-2 border-black bg-white px-3 shadow-hard"
              placeholder="배치ID, 실행ID, 결과ID"
              value={query}
              onChange={(event) => setQuery(event.target.value)}
            />
          </label>
          <label className="block">
            <span className="mb-2 block font-head text-sm font-bold">
              배치ID
            </span>
            <input
              className="h-12 w-full rounded border-2 border-black bg-white px-3 shadow-hard"
              placeholder="COMMON-AUDIT-ROLLUP"
              value={batchFilter}
              onChange={(event) => setBatchFilter(event.target.value)}
            />
          </label>
          <label className="block">
            <span className="mb-2 block font-head text-sm font-bold">
              결과상태
            </span>
            <select
              className="h-12 w-full rounded border-2 border-black bg-white px-3 shadow-hard"
              value={statusFilter}
              onChange={(event) => setStatusFilter(event.target.value)}
            >
              <option value="">전체</option>
              <option value="RUNNING">실행중</option>
              <option value="SUCCESS">성공</option>
              <option value="FAILED">실패</option>
            </select>
          </label>
          <label className="block">
            <span className="mb-2 block font-head text-sm font-bold">건수</span>
            <select
              className="h-12 w-full rounded border-2 border-black bg-white px-3 shadow-hard"
              value={size}
              onChange={(event) => {
                const nextSize = Number(event.target.value);
                setSize(nextSize);
                void loadBatchResults(
                  query,
                  batchFilter,
                  statusFilter,
                  nextSize,
                );
              }}
            >
              {sizeOptions.map((option) => (
                <option key={option} value={option}>
                  {option}
                </option>
              ))}
            </select>
          </label>
          <div className="flex items-end gap-2">
            <button
              className="h-12 rounded border-2 border-black bg-primary px-4 font-head font-black shadow-hard transition-all duration-200 hover:-translate-x-0.5 hover:-translate-y-0.5 hover:bg-primary-hover hover:shadow-hard-lg active:translate-x-1 active:translate-y-1 active:shadow-none"
              type="submit"
            >
              검색
            </button>
            <button
              className="h-12 rounded border-2 border-black bg-card px-4 font-head font-black shadow-hard"
              type="button"
              onClick={() => {
                setQuery("");
                setBatchFilter("");
                setStatusFilter("");
                void loadBatchResults("", "", "", size);
              }}
            >
              초기화
            </button>
          </div>
        </form>

        {message ? (
          <div
            role="status"
            className={`border-2 border-black px-4 py-3 font-bold shadow-hard ${state === "permission" || state === "error" ? "bg-destructive text-white" : "bg-accent text-black"}`}
          >
            {message}
          </div>
        ) : null}
        {fieldErrors.id ? (
          <p className="text-sm font-bold text-destructive">{fieldErrors.id}</p>
        ) : null}
        {state === "loading" ? (
          <StateCard
            title="LOADING"
            body="배치 결과 목록을 불러오는 중입니다."
          />
        ) : null}
        {state === "permission" ? (
          <StateCard
            title="PERMISSION DENIED"
            body="R09 시스템관리자 권한이 필요합니다. 직접 API 요청도 서버에서 403으로 차단됩니다."
          />
        ) : null}
        {state === "empty" ? (
          <StateCard
            title="EMPTY"
            body="조건에 맞는 배치 결과가 없습니다. 배치 실행 관리 화면에서 실행 이력을 확인하세요."
          />
        ) : null}
        {state === "error" ? (
          <StateCard
            title="ERROR"
            body={message || "배치 결과 조회 처리 중 오류가 발생했습니다."}
          />
        ) : null}

        {state === "success" || results.length > 0 ? (
          <section className="grid grid-cols-1 gap-5 lg:grid-cols-[minmax(0,1fr)_25rem]">
            <div className="overflow-hidden border-2 border-black bg-card shadow-hard">
              <div className="border-b-2 border-black bg-muted px-4 py-3 font-head font-black">
                batch_results 목록
              </div>
              <div className="overflow-x-auto">
                <table className="min-w-full border-collapse text-sm">
                  <thead className="bg-primary font-head text-xs uppercase">
                    <tr>
                      {[
                        "결과ID",
                        "실행ID",
                        "배치ID",
                        "상태",
                        "시작",
                        "종료",
                        "전체",
                        "성공",
                        "실패",
                        "제외",
                        "소요",
                        "로그",
                      ].map((header) => (
                        <th
                          key={header}
                          className="border-b-2 border-black px-3 py-2 text-left"
                        >
                          {header}
                        </th>
                      ))}
                    </tr>
                  </thead>
                  <tbody>
                    {results.map((item) => (
                      <tr
                        key={item.batchResultId}
                        className={`cursor-pointer border-b-2 border-black transition-colors hover:bg-accent ${selectedResultId === item.batchResultId ? "bg-accent" : "bg-card"}`}
                        onClick={() => {
                          setSelectedResultId(item.batchResultId);
                          setLogPreview(null);
                        }}
                      >
                        <td className="px-3 py-2 font-mono text-xs">
                          {item.batchResultId}
                        </td>
                        <td className="px-3 py-2 font-mono text-xs">
                          {item.batchExecutionId}
                        </td>
                        <td className="px-3 py-2 font-mono text-xs">
                          {item.batchId}
                        </td>
                        <td className="px-3 py-2">
                          <span className="border-2 border-black bg-primary px-2 py-1 font-head text-xs font-black">
                            {item.resultStatusName}
                          </span>
                        </td>
                        <td className="px-3 py-2 text-xs">
                          {item.startedAt?.replace("T", " ")}
                        </td>
                        <td className="px-3 py-2 text-xs">
                          {item.endedAt?.replace("T", " ") ?? "진행 중"}
                        </td>
                        <td className="px-3 py-2 font-bold">
                          {item.totalCount}
                        </td>
                        <td className="px-3 py-2">{item.successCount}</td>
                        <td className="px-3 py-2">{item.failureCount}</td>
                        <td className="px-3 py-2">{item.excludedCount}</td>
                        <td className="px-3 py-2">
                          {formatDuration(item.durationMs)}
                        </td>
                        <td className="max-w-[14rem] truncate px-3 py-2">
                          {item.logFileName ?? "없음"}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>
            <aside className="relative">
              <div className="absolute inset-2 border-2 border-black bg-[#01ffcc]" />
              <div className="relative border-2 border-black bg-card shadow-hard">
                <div className="border-b-2 border-black bg-muted px-4 py-3 font-head font-black">
                  상세/로그 조회
                </div>
                {selectedResult ? (
                  <div className="flex flex-col gap-3 p-4">
                    <ReadonlyField
                      label="결과 ID"
                      value={String(selectedResult.batchResultId)}
                    />
                    <ReadonlyField
                      label="실행 ID"
                      value={String(selectedResult.batchExecutionId)}
                    />
                    <ReadonlyField
                      label="배치"
                      value={`${selectedResult.batchName} (${selectedResult.batchId})`}
                    />
                    <ReadonlyField
                      label="처리 건수"
                      value={`전체 ${selectedResult.totalCount} / 성공 ${selectedResult.successCount} / 실패 ${selectedResult.failureCount} / 제외 ${selectedResult.excludedCount}`}
                    />
                    <ReadonlyField
                      label="소요시간"
                      value={formatDuration(selectedResult.durationMs)}
                    />
                    <p className="border-2 border-black bg-muted p-3 text-xs leading-relaxed">
                      {selectedResult.logAccessRule}
                    </p>
                    <p className="border-2 border-black bg-accent p-3 text-xs leading-relaxed">
                      {selectedResult.operationRule}
                    </p>
                    {logPreview ? (
                      <p className="border-2 border-black bg-background p-3 font-mono text-xs leading-relaxed">
                        {logPreview.logFileName} / {logPreview.immutableRule}
                      </p>
                    ) : null}
                    <button
                      className="h-12 w-full rounded border-2 border-black bg-primary px-4 font-head font-black shadow-hard transition-all duration-200 hover:-translate-x-0.5 hover:-translate-y-0.5 hover:bg-primary-hover hover:shadow-hard-lg active:translate-x-1 active:translate-y-1 active:shadow-none disabled:opacity-60"
                      type="button"
                      onClick={() => void viewSelectedLog()}
                      disabled={
                        loadingLog ||
                        !selectedResult.logFileId ||
                        !user?.roles.includes("R09")
                      }
                    >
                      {loadingLog ? "로그 조회 중..." : "로그파일 조회 전 확인"}
                    </button>
                  </div>
                ) : (
                  <div className="p-4 text-sm">
                    행을 선택하면 상세 모달 영역이 표시됩니다.
                  </div>
                )}
              </div>
            </aside>
          </section>
        ) : null}
      </section>
    </AppShell>
  );
}

function compactJson(value: string) {
  try {
    return JSON.stringify(JSON.parse(value));
  } catch {
    return value;
  }
}

function extractBusinessArea(value: string) {
  try {
    const parsed = JSON.parse(value) as { businessArea?: string };
    return parsed.businessArea ?? "ACHIEVEMENT";
  } catch {
    return "ACHIEVEMENT";
  }
}

function parseRequiredColumns(value: string) {
  try {
    const parsed = JSON.parse(value) as unknown;
    return Array.isArray(parsed) ? parsed : [];
  } catch {
    return [];
  }
}

function formatDuration(value?: number) {
  if (value === undefined || value === null) {
    return "진행 중";
  }
  if (value >= 60000) {
    return `${Math.floor(value / 60000)}분 ${Math.round((value % 60000) / 1000)}초`;
  }
  return `${Math.round(value / 1000)}초`;
}

function formatBytes(value: number) {
  if (value >= 1024 * 1024) {
    return `${(value / (1024 * 1024)).toFixed(1)} MB`;
  }
  if (value >= 1024) {
    return `${Math.round(value / 1024)} KB`;
  }
  return `${value} B`;
}

function readonlyDisplayValue(label: string, value: string) {
  const duplicatedTableLabels = new Set([
    "KORUS 교번",
    "성명",
    "조직코드",
    "조직명",
    "상위조직",
    "환경설정 키",
    "정책 규칙",
  ]);
  return duplicatedTableLabels.has(label) && value.length > 1
    ? `${value.slice(0, 1)}\u200B${value.slice(1)}`
    : value;
}

function ReadonlyField({ label, value }: { label: string; value: string }) {
  return (
    <div className="group">
      <div className="font-head text-xs font-black uppercase tracking-[0.12em] text-muted-foreground">
        {label}
      </div>
      <div className="mt-1 border-2 border-black bg-background px-3 py-2 font-bold shadow-hard transition-all duration-200 group-hover:-translate-x-px group-hover:-translate-y-px group-hover:bg-accent group-hover:shadow-hard-lg">
        {readonlyDisplayValue(label, value)}
      </div>
    </div>
  );
}

function StateCard({ title, body }: { title: string; body: string }) {
  const isLoading = title.toUpperCase().includes("LOADING");
  const tone = title.toUpperCase().includes("PERMISSION")
    ? "bg-destructive text-white"
    : title.toUpperCase().includes("EMPTY")
      ? "bg-[#01ffcc]"
      : "bg-[#c4a1ff]";

  return (
    <section className="relative max-w-3xl">
      <div className={`absolute inset-2 border-2 border-black ${tone}`} />
      <div className="relative overflow-hidden border-2 border-black bg-card shadow-hard">
        <div
          className={`border-b-2 border-black px-4 py-3 font-head text-sm font-black uppercase tracking-[0.16em] ${tone}`}
        >
          {title}
        </div>
        <div className="space-y-4 p-5">
          <h2 className="font-head text-2xl font-black uppercase leading-none">
            {isLoading
              ? "데이터를 연결하는 중"
              : title.toUpperCase().includes("PERMISSION")
                ? "접근 권한 확인 필요"
                : title.toUpperCase().includes("EMPTY")
                  ? "조회 결과 없음"
                  : "상태 안내"}
          </h2>
          <p className="text-sm leading-relaxed text-muted-foreground">
            {body}
          </p>
          {isLoading ? (
            <div className="grid gap-2" aria-hidden="true">
              <div className="h-4 w-11/12 animate-pulse border-2 border-black bg-muted shadow-hard" />
              <div className="h-4 w-8/12 animate-pulse border-2 border-black bg-accent shadow-hard" />
              <div className="h-4 w-10/12 animate-pulse border-2 border-black bg-primary shadow-hard" />
            </div>
          ) : null}
        </div>
      </div>
    </section>
  );
}
