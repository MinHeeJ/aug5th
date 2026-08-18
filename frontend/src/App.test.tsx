import { fireEvent, render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import App from "./App";

const healthResponse = {
  success: true,
  data: {
    status: "UP",
    service: "common-foundation",
    timestamp: "2026-08-16T00:00:00Z",
  },
};

const sessionResponse = {
  success: true,
  data: { userId: "admin", roles: ["R09"], dataScope: "ALL" },
};

const usersResponse = {
  success: true,
  data: {
    items: [
      {
        userId: "teacher01",
        enabled: true,
        roleSummary: "R01 교원",
        status: "ACTIVE",
        employeeNo: "P-2026-001",
        name: "김교*",
        departmentCode: "KNUE-EDU",
        departmentName: "교육학과",
        rankName: "교수",
        employmentStatus: "ACTIVE",
        positionSummary: "PROFESSOR",
        lastSyncedAt: "2026-08-16T00:00:00",
      },
    ],
    page: 1,
    size: 20,
    totalCount: 1,
    screenId: "SCR-USER-MGMT",
    requiredRole: "R09",
  },
};

const organizationsResponse = {
  success: true,
  data: {
    items: [
      {
        organizationCode: "KNUE-EDU",
        organizationName: "교육학과",
        parentOrganizationCode: "KNUE-COLLEGE",
        parentOrganizationName: "사범대학",
        validFrom: "2026-01-01",
        enabled: true,
        childCount: 0,
        assignedUserCount: 1,
      },
    ],
    page: 1,
    size: 20,
    totalCount: 1,
    screenId: "SCR-ORG-MGMT",
    requiredRole: "R09",
  },
};

const positionsResponse = {
  success: true,
  data: {
    items: [
      {
        positionId: 1,
        positionCode: "DEPT_HEAD",
        positionName: "학과장",
        userId: "teacher01",
        userName: "김교*",
        employeeNo: "P-2026-001",
        organizationCode: "KNUE-EDU",
        organizationName: "교육학과",
        validFrom: "2026-01-01",
        active: true,
      },
    ],
    page: 1,
    size: 20,
    totalCount: 1,
    screenId: "SCR-POSITION-MGMT",
    requiredRole: "R09",
  },
};

const rolesResponse = {
  success: true,
  data: {
    items: [
      {
        roleCode: "R09",
        roleName: "시스템관리자",
        purpose: "사용자·조직·메뉴·권한 관리",
        grantCriteria: "시스템 관리자 승인 대상자",
        defaultDataScope: "ALL",
        enabled: true,
        assignedUserCount: 1,
        menuPermissionCount: 25,
        functionPermissionCount: 125,
      },
    ],
    page: 1,
    size: 20,
    totalCount: 1,
    screenId: "SCR-ROLE-MGMT",
    requiredRole: "R09",
  },
};

const menuPermissionsResponse = {
  success: true,
  data: {
    items: [
      {
        menuPermissionId: 7001,
        targetType: "ROLE",
        targetId: "R09",
        targetName: "시스템관리자",
        menuId: "M-MENU-PERMISSION",
        menuName: "메뉴 권한 관리",
        parentMenuName: "보안·감사 관리",
        screenId: "SCR-MENU-PERMISSION",
        url: "/admin/security/menu-permissions",
        allowed: true,
        permissionSource: "역할 권한",
        displayOrder: 15,
      },
    ],
    page: 1,
    size: 20,
    totalCount: 1,
    screenId: "SCR-MENU-PERMISSION",
    requiredRole: "R09",
  },
};

const userRolesResponse = {
  success: true,
  data: {
    items: [
      {
        userRoleId: 1001,
        userId: "teacher01",
        userName: "김교*",
        employeeNo: "P-2026-001",
        roleCode: "R01",
        roleName: "교원",
        validFrom: "2026-01-01",
        approverId: "admin",
        approverName: "시스템관리자",
        assignmentSource: "MANUAL",
        active: true,
      },
    ],
    page: 1,
    size: 20,
    totalCount: 1,
    screenId: "SCR-USER-ROLE-MGMT",
    requiredRole: "R09",
  },
};

const functionPermissionsResponse = {
  success: true,
  data: {
    items: [
      {
        functionPermissionId: 9001,
        roleCode: "R09",
        roleName: "시스템관리자",
        screenId: "SCR-FUNCTION-PERMISSION",
        screenName: "기능 권한 관리",
        menuId: "M-FUNCTION-PERMISSION",
        menuName: "기능 권한 관리",
        actionCode: "UPDATE",
        actionName: "수정",
        allowed: true,
        permissionScope: "시스템관리자 전체 기능",
        displayOrder: 16,
      },
    ],
    page: 1,
    size: 20,
    totalCount: 1,
    screenId: "SCR-FUNCTION-PERMISSION",
    requiredRole: "R09",
  },
};

const dataScopesResponse = {
  success: true,
  data: {
    items: [
      {
        dataScopeId: 10001,
        roleCode: "R09",
        roleName: "시스템관리자",
        scopeType: "ALL",
        scopeName: "전체",
        organizationCode: "KNUE",
        organizationName: "한국교원대학교",
        businessArea: "COMMON_FOUNDATION",
        businessAreaName: "공통기능 전체",
        enforcementRule: "서버 조회조건 전체 범위 강제",
        displayOrder: 9,
      },
    ],
    page: 1,
    size: 20,
    totalCount: 1,
    screenId: "SCR-DATA-SCOPE",
    requiredRole: "R09",
  },
};

const menusResponse = {
  success: true,
  data: {
    items: [
      {
        menuId: "M-MENU-MGMT",
        parentMenuId: "M-SYSTEM",
        parentMenuName: "시스템 관리",
        menuName: "메뉴 관리",
        screenId: "SCR-MENU-MGMT",
        url: "/admin/menus",
        displayOrder: 18,
        childCount: 0,
        permissionCount: 9,
        menuUsageRule: "화면 표시 및 서버 메뉴 권한 판정 기준",
      },
    ],
    page: 1,
    size: 20,
    totalCount: 1,
    screenId: "SCR-MENU-MGMT",
    requiredRole: "R09",
  },
};

const codeGroupsResponse = {
  success: true,
  data: {
    items: [
      {
        groupId: "EVAL_AREA",
        groupName: "평가영역",
        description: "교수업적 평가영역 코드 묶음",
        managingDepartment: "교수지원과",
        enabled: true,
        detailCount: 3,
        enabledDetailCount: 3,
        detailManagementRule: "상세코드 관리에서 코드값·코드명·정렬순서 변경",
      },
    ],
    page: 1,
    size: 20,
    totalCount: 1,
    screenId: "SCR-CODE-GROUP",
    requiredRole: "R09",
  },
};

const codeDetailsResponse = {
  success: true,
  data: {
    items: [
      {
        codeDetailId: 1,
        groupId: "EVAL_AREA",
        groupName: "평가영역",
        codeValue: "TEACHING",
        codeName: "교육영역",
        parentCodeValue: null,
        parentCodeName: null,
        displayOrder: 10,
        active: true,
        detailUsageRule:
          "그룹 내 코드값은 중복될 수 없고 정렬순서로 표시됩니다.",
      },
    ],
    page: 1,
    size: 20,
    totalCount: 1,
    screenId: "SCR-CODE-DETAIL",
    requiredRole: "R09",
  },
};

const systemConfigurationsResponse = {
  success: true,
  data: {
    items: [
      {
        configKey: "SESSION_IDLE_MINUTES",
        configValue: "30",
        unit: "분",
        valueRange: "5-240",
        enabled: true,
        applyScope: "전체 사용자 공통 적용",
        validationRule: "설정값은 항목별 단위와 값 범위를 서버에서 검증합니다.",
      },
    ],
    page: 1,
    size: 20,
    totalCount: 1,
    screenId: "SCR-SYSTEM-CONFIG",
    requiredRole: "R09",
  },
};

const baseYearsResponse = {
  success: true,
  data: {
    items: [
      {
        baseYear: "2026",
        defaultQueryYear: "2026",
        copyBaselineEnabled: true,
        resetEnabled: false,
        enabled: true,
        periodRule:
          "기준연도는 4자리 연도이며 기본 조회연도는 기준연도 이하로 관리합니다.",
        transitionRule:
          "기준정보 복사 후 초기화 실행 여부를 서버에서 검증합니다.",
      },
    ],
    page: 1,
    size: 20,
    totalCount: 1,
    screenId: "SCR-BASE-YEAR",
    requiredRole: "R09",
  },
};

const filePoliciesResponse = {
  success: true,
  data: {
    items: [
      {
        filePolicyId: 1,
        businessArea: "COMMON",
        businessAreaName: "공통 첨부",
        allowedExtensions: "pdf,xlsx,docx,png,jpg",
        maxFileSizeMb: 20,
        maxFileCount: 5,
        maxTotalSizeMb: 100,
        maxFilenameLength: 120,
        malwareScanEnabled: true,
        enabled: true,
        uploadValidationRule:
          "첨부파일 업로드 검증 시 확장자·용량·개수·파일명 길이 정책을 적용합니다.",
        fileOperationBoundary:
          "이 화면에서는 실제 파일 업로드·조회·삭제를 수행하지 않습니다.",
      },
    ],
    page: 1,
    size: 20,
    totalCount: 1,
    screenId: "SCR-FILE-POLICY",
    requiredRole: "R09",
  },
};

const noticesResponse = {
  success: true,
  data: {
    items: [
      {
        noticeId: 1,
        title: "2026학년도 교수업적평가 공통 일정 안내",
        contentSummary: "평가일정과 시스템 점검 기간을 확인하세요.",
        postFrom: "2026-01-01",
        postTo: "2026-12-31",
        targetRoles: "R01,R09",
        targetOrganizations: "KNUE-EDU",
        important: true,
        enabled: true,
        attachmentCount: 0,
        exposureRule: "지정 대상 역할·조직과 게시기간에만 노출됩니다.",
        readBoundary: "공지 열람은 업무 승인이나 확인처리로 간주하지 않습니다.",
      },
    ],
    page: 1,
    size: 20,
    totalCount: 1,
    screenId: "SCR-NOTICE",
    requiredRole: "R09",
  },
};

const attachmentsResponse = {
  success: true,
  data: {
    items: [
      {
        attachmentId: 1,
        businessKey: "NOTICE:1",
        originalName: "평가일정 안내.pdf",
        storedName: "2026/08/notice-1.pdf",
        extension: "pdf",
        sizeBytes: 204800,
        uploadedBy: "admin",
        uploadedAt: "2026-08-16T09:30:00",
        malwareScanResult: "CLEAN",
        deleted: false,
        finalizedRecord: false,
        storagePresent: true,
        integrityStatus: "OK",
        downloadAuthorizationRule: "다운로드 시 권한을 재검증합니다.",
        deleteBoundary: "개발·검증 환경에서는 논리삭제만 허용합니다.",
      },
    ],
    page: 1,
    size: 20,
    totalCount: 1,
    screenId: "SCR-ATTACHMENT",
    requiredRole: "R09",
  },
};

const excelTemplatesResponse = {
  success: true,
  data: {
    items: [
      {
        templateId: 1,
        businessArea: "ACHIEVEMENT",
        businessAreaName: "교수업적",
        version: "2026.1",
        requiredColumns: '[{"name":"교번","type":"STRING","required":true}]',
        requiredColumnCount: 1,
        effectiveDate: "2026-03-01",
        downloadFileId: 1,
        downloadFileName: "교수업적_업로드양식_2026.xlsx",
        enabled: true,
        validationRule: "필수값·타입·중복규칙을 템플릿 버전으로 검증합니다.",
        downloadRule:
          "다운로드 시 첨부파일 권한과 템플릿 사용여부를 재검증합니다.",
        updatedAt: "2026-08-16T12:00:00",
      },
    ],
    page: 1,
    size: 20,
    totalCount: 1,
    screenId: "SCR-EXCEL-TEMPLATE",
    requiredRole: "R09",
  },
};

const excelUploadsResponse = {
  success: true,
  data: {
    items: [
      {
        uploadId: 1,
        templateId: 1,
        businessArea: "ACHIEVEMENT",
        businessAreaName: "교수업적",
        version: "2026.1",
        uploaderId: "admin",
        fileName: "교수업적_업로드_정상_2026.xlsx",
        totalCount: 3,
        successCount: 3,
        errorCount: 0,
        excludedCount: 0,
        savedCount: 3,
        processingTimeMs: 1250,
        uploadStatus: "SUCCESS",
        uploadedAt: "2026-08-16T13:00:00",
        transactionRule:
          "모든 행이 정상일 때만 하나의 트랜잭션으로 등록합니다.",
        validationRule:
          "업무별 확정 양식 버전과 헤더·필수값·형식·코드·중복을 검증합니다.",
      },
    ],
    page: 1,
    size: 20,
    totalCount: 1,
    screenId: "SCR-EXCEL-UPLOAD",
    requiredRole: "R09",
  },
};

const excelDownloadsResponse = {
  success: true,
  data: {
    items: [
      {
        downloadId: 1,
        requesterId: "admin",
        queryCondition:
          '{"businessArea":"ACHIEVEMENT","q":"성과","year":"2026"}',
        dataScopeApplied: '{"role":"R09","scope":"ALL","serverEnforced":true}',
        fileId: 1,
        fileName: "교수업적_조회결과_2026.xlsx",
        extension: "xlsx",
        sizeBytes: 2048,
        createdAt: "2026-08-16T14:00:00",
        generationRule:
          "현재 조회조건과 사용자 데이터범위 권한을 적용하여 생성합니다.",
        boundaryRule:
          "원천 업무자료는 변경하지 않고 권한 밖 자료는 포함하지 않습니다.",
      },
    ],
    page: 1,
    size: 20,
    totalCount: 1,
    screenId: "SCR-EXCEL-DOWNLOAD",
    requiredRole: "R09",
  },
};

const privacyPoliciesResponse = {
  success: true,
  data: {
    items: [
      {
        fieldPolicyId: 1,
        fieldName: "researcher_registration_no",
        privacyGrade: "SENSITIVE",
        privacyGradeName: "민감정보",
        encryptionEnabled: true,
        maskingRule: "앞 3자리 + 뒤 2자리 표시",
        logExcluded: true,
        policyRule: "AES-256-GCM 암호화와 HMAC 검색 식별자 적용 대상입니다.",
        auditRule:
          "감사로그에는 원문과 처리값을 제외하고 목적·결과만 기록합니다.",
      },
    ],
    page: 1,
    size: 20,
    totalCount: 1,
    screenId: "SCR-PRIVACY",
    requiredRole: "R09",
  },
};

const activeSessionsResponse = {
  success: true,
  data: {
    items: [
      {
        sessionId: "SEED-ACTIVE-ADMIN",
        userId: "admin",
        userDisplayName: "관리자",
        loginAt: "2026-08-16T09:00:00",
        lastActivityAt: "2026-08-16T09:30:00",
        ipAddress: "127.0.0.1",
        sessionStatus: "ACTIVE",
        sessionStatusName: "활성",
        latestTerminationId: null,
        latestTerminationType: null,
        operationRule: "강제종료 가능: 활성 세션이며 R09 사유 입력 필요",
      },
    ],
    page: 1,
    size: 20,
    totalCount: 1,
    screenId: "SCR-SESSION",
    requiredRole: "R09",
  },
};

const auditLogsResponse = {
  success: true,
  data: {
    items: [
      {
        auditLogId: 1001,
        logType: "AUTHORIZATION",
        logTypeName: "권한",
        targetKey: "roles:R09",
        actorId: "admin",
        beforeValue: '{"roleCode":"R09","allowed":false}',
        afterValue: '{"roleCode":"R09","allowed":true}',
        result: "SUCCESS",
        resultName: "성공",
        operationRule:
          "감사로그 원문은 수정·삭제할 수 없으며 상세에서 변경 전후값을 조회합니다.",
      },
    ],
    page: 1,
    size: 20,
    totalCount: 1,
    screenId: "SCR-AUDIT-LOG",
    requiredRole: "R09",
  },
};

const batchDefinitionsResponse = {
  success: true,
  data: {
    items: [
      {
        batchId: "COMMON-AUDIT-ROLLUP",
        batchName: "감사 로그 일별 집계",
        schedule: "0 0 * * *",
        predecessorBatchId: null,
        predecessorBatchName: null,
        parameters: '{"businessArea":"COMMON_FOUNDATION"}',
        maxRuntimeSeconds: 3600,
        ownerId: "admin",
        ownerName: "관리자",
        status: "DEFINED",
        statusName: "정의됨",
        operationRule:
          "배치 정의 화면은 즉시 실행·중지·재실행을 제공하지 않고 정의만 저장합니다.",
      },
    ],
    page: 1,
    size: 20,
    totalCount: 1,
    screenId: "SCR-BATCH-DEFINITION",
    requiredRole: "R09",
  },
};

const batchExecutionsResponse = {
  success: true,
  data: {
    items: [
      {
        batchExecutionId: 10,
        batchId: "COMMON-AUDIT-ROLLUP",
        batchName: "감사 로그 일별 집계",
        parameters: '{"businessArea":"COMMON_FOUNDATION","mode":"manual"}',
        reason: "시드 배치 실행 이력",
        executionStatus: "RUNNING",
        executionStatusName: "실행중",
        requestedBy: "admin",
        requestedByName: "관리자",
        operationRule: "중지 가능: 실행 중 배치이며 R09 사유 입력 필요",
      },
    ],
    page: 1,
    size: 20,
    totalCount: 1,
    screenId: "SCR-BATCH-EXECUTION",
    requiredRole: "R09",
  },
};

const batchResultsResponse = {
  success: true,
  data: {
    items: [
      {
        batchResultId: 100,
        batchExecutionId: 10,
        batchId: "COMMON-AUDIT-ROLLUP",
        batchName: "감사 로그 일별 집계",
        startedAt: "2026-08-16T00:00:00",
        endedAt: "2026-08-16T00:03:00",
        totalCount: 120,
        successCount: 118,
        failureCount: 1,
        excludedCount: 1,
        durationMs: 180000,
        logFileId: 77,
        logFileName: "batch-result-10.log",
        resultStatus: "FAILED",
        resultStatusName: "실패",
        logAccessRule:
          "로그파일은 해당 실행ID에 연결된 파일만 조회하며 수정·삭제하지 않습니다.",
        operationRule:
          "배치 결과 조회 화면에서는 재실행·실패자료수정·로그삭제를 제공하지 않습니다.",
      },
    ],
    page: 1,
    size: 20,
    totalCount: 1,
    screenId: "SCR-BATCH-RESULT",
    requiredRole: "R09",
  },
};

function jsonResponse(body: unknown, status = 200) {
  return Promise.resolve(
    new Response(JSON.stringify(body), {
      status,
      headers: { "Content-Type": "application/json" },
    }),
  );
}

beforeEach(() => {
  window.history.pushState({}, "", "/");
  window.localStorage.clear();
});

afterEach(() => {
  vi.unstubAllGlobals();
  vi.restoreAllMocks();
});

test("renders setup shell and loads health through relative api path", async () => {
  const fetchMock = vi.fn(() => jsonResponse(healthResponse));
  vi.stubGlobal("fetch", fetchMock);

  render(<App />);

  expect(
    screen.getByRole("heading", { name: "배치 결과 조회 Vertical Slice" }),
  ).toBeInTheDocument();
  expect(await screen.findByText("STATUS UP")).toBeInTheDocument();
  expect(fetchMock).toHaveBeenCalledWith(
    "/api/health",
    expect.objectContaining({ credentials: "include" }),
  );
});

test("sidebar menu expands a hovered tree and keeps the last selected menu tree open", async () => {
  const user = userEvent.setup();
  vi.stubGlobal(
    "fetch",
    vi.fn(() => jsonResponse(healthResponse)),
  );

  const { unmount } = render(<App />);
  await screen.findByText("STATUS UP");

  const sidebar = screen.getByTestId("sidebar-navigation");
  expect(sidebar).toBeInTheDocument();
  expect(sidebar).toHaveClass("lg:w-[22%]");

  const roleMenuToggle = screen.getByRole("button", {
    name: "역할·권한 메뉴 펼치기",
  });
  expect(
    screen.queryByRole("link", { name: "역할 관리" }),
  ).not.toBeInTheDocument();

  await user.hover(roleMenuToggle);
  expect(screen.getByRole("link", { name: "역할 관리" })).toBeVisible();

  await user.unhover(roleMenuToggle);
  expect(
    screen.queryByRole("link", { name: "역할 관리" }),
  ).not.toBeInTheDocument();

  await user.click(roleMenuToggle);
  fireEvent.mouseDown(screen.getByRole("link", { name: "역할 관리" }));
  expect(screen.getByRole("link", { name: "역할 관리" })).toBeVisible();

  unmount();
  render(<App />);
  await screen.findByText("STATUS UP");
  expect(screen.getByRole("link", { name: "역할 관리" })).toBeVisible();
});

test("smoke: opens user route, loads users, runs primary save action from selected row", async () => {
  window.history.pushState({}, "", "/admin/system/users");
  vi.spyOn(window, "confirm").mockReturnValue(true);
  const fetchMock = vi
    .fn()
    .mockImplementationOnce(() => jsonResponse(sessionResponse))
    .mockImplementationOnce(() => jsonResponse(usersResponse))
    .mockImplementationOnce(() =>
      jsonResponse({
        success: true,
        data: {
          userId: "teacher01",
          enabled: false,
          status: "INACTIVE",
          message: "사용자 관리 저장이 완료되었습니다.",
        },
      }),
    )
    .mockImplementationOnce(() => jsonResponse(usersResponse));
  vi.stubGlobal("fetch", fetchMock);

  render(<App />);

  expect(
    await screen.findByRole("heading", { name: "사용자 관리" }),
  ).toBeInTheDocument();
  expect(await screen.findByText("P-2026-001")).toBeInTheDocument();
  expect(screen.getByText("김교*")).toBeInTheDocument();

  await userEvent.click(
    screen.getByRole("button", { name: "저장/실행 전 확인" }),
  );

  await waitFor(() =>
    expect(fetchMock).toHaveBeenCalledWith(
      "/api/admin/users",
      expect.objectContaining({ method: "POST" }),
    ),
  );
  expect(await screen.findByText("총 1건 조회되었습니다.")).toBeInTheDocument();
});

test("permission state is shown when admin user API returns forbidden", async () => {
  window.history.pushState({}, "", "/admin/system/users");
  const fetchMock = vi
    .fn()
    .mockImplementationOnce(() => jsonResponse(sessionResponse))
    .mockImplementationOnce(() =>
      jsonResponse(
        {
          success: false,
          error: { code: "FORBIDDEN", message: "권한이 없습니다." },
        },
        403,
      ),
    );
  vi.stubGlobal("fetch", fetchMock);

  render(<App />);

  expect(await screen.findByText("PERMISSION DENIED")).toBeInTheDocument();
  expect(screen.getByText("권한이 없습니다.")).toBeInTheDocument();
});

test("smoke: opens organization route, loads organizations, runs primary save action from selected row", async () => {
  window.history.pushState({}, "", "/admin/system/organizations");
  vi.spyOn(window, "confirm").mockReturnValue(true);
  const fetchMock = vi
    .fn()
    .mockImplementationOnce(() => jsonResponse(sessionResponse))
    .mockImplementationOnce(() => jsonResponse(organizationsResponse))
    .mockImplementationOnce(() =>
      jsonResponse({
        success: true,
        data: {
          organizationCode: "KNUE-EDU",
          enabled: false,
          validTo: "2026-12-31",
          message: "조직 관리 저장이 완료되었습니다.",
        },
      }),
    )
    .mockImplementationOnce(() => jsonResponse(organizationsResponse));
  vi.stubGlobal("fetch", fetchMock);

  render(<App />);

  expect(
    await screen.findByRole("heading", { name: "조직 관리" }),
  ).toBeInTheDocument();
  expect(await screen.findByText("KNUE-EDU")).toBeInTheDocument();
  expect(screen.getByText("교육학과")).toBeInTheDocument();
  expect(screen.getByText("사범대학")).toBeInTheDocument();

  await userEvent.click(
    screen.getByRole("button", { name: "저장/실행 전 확인" }),
  );

  await waitFor(() =>
    expect(fetchMock).toHaveBeenCalledWith(
      "/api/admin/organizations",
      expect.objectContaining({ method: "POST" }),
    ),
  );
  expect(await screen.findByText("총 1건 조회되었습니다.")).toBeInTheDocument();
});

test("organization permission state is shown when admin organization API returns forbidden", async () => {
  window.history.pushState({}, "", "/admin/system/organizations");
  const fetchMock = vi
    .fn()
    .mockImplementationOnce(() => jsonResponse(sessionResponse))
    .mockImplementationOnce(() =>
      jsonResponse(
        {
          success: false,
          error: { code: "FORBIDDEN", message: "권한이 없습니다." },
        },
        403,
      ),
    );
  vi.stubGlobal("fetch", fetchMock);

  render(<App />);

  expect(await screen.findByText("PERMISSION DENIED")).toBeInTheDocument();
  expect(screen.getByText("권한이 없습니다.")).toBeInTheDocument();
});

test("smoke: opens position route, loads positions, runs primary save action from selected row", async () => {
  window.history.pushState({}, "", "/admin/system/positions");
  vi.spyOn(window, "confirm").mockReturnValue(true);
  const fetchMock = vi
    .fn()
    .mockImplementationOnce(() => jsonResponse(sessionResponse))
    .mockImplementationOnce(() => jsonResponse(positionsResponse))
    .mockImplementationOnce(() =>
      jsonResponse({
        success: true,
        data: {
          positionId: 1,
          active: false,
          validTo: "2026-12-31",
          message: "보직 관리 저장이 완료되었습니다.",
        },
      }),
    )
    .mockImplementationOnce(() => jsonResponse(positionsResponse));
  vi.stubGlobal("fetch", fetchMock);

  render(<App />);

  expect(
    await screen.findByRole("heading", { name: "보직 관리" }),
  ).toBeInTheDocument();
  await waitFor(() =>
    expect(screen.getAllByText("학과장").length).toBeGreaterThan(0),
  );
  expect(screen.getAllByText("김교*").length).toBeGreaterThan(0);
  expect(screen.getAllByText("교육학과")[0]).toBeInTheDocument();

  await userEvent.click(
    screen.getByRole("button", { name: "저장/실행 전 확인" }),
  );

  await waitFor(() =>
    expect(fetchMock).toHaveBeenCalledWith(
      "/api/admin/positions",
      expect.objectContaining({ method: "POST" }),
    ),
  );
  expect(await screen.findByText("총 1건 조회되었습니다.")).toBeInTheDocument();
});

test("position permission state is shown when admin position API returns forbidden", async () => {
  window.history.pushState({}, "", "/admin/system/positions");
  const fetchMock = vi
    .fn()
    .mockImplementationOnce(() => jsonResponse(sessionResponse))
    .mockImplementationOnce(() =>
      jsonResponse(
        {
          success: false,
          error: { code: "FORBIDDEN", message: "권한이 없습니다." },
        },
        403,
      ),
    );
  vi.stubGlobal("fetch", fetchMock);

  render(<App />);

  expect(await screen.findByText("PERMISSION DENIED")).toBeInTheDocument();
  expect(screen.getByText("권한이 없습니다.")).toBeInTheDocument();
});

test("smoke: opens role route, loads roles, runs primary save action from selected row", async () => {
  window.history.pushState({}, "", "/admin/security/roles");
  vi.spyOn(window, "confirm").mockReturnValue(true);
  const fetchMock = vi
    .fn()
    .mockImplementationOnce(() => jsonResponse(sessionResponse))
    .mockImplementationOnce(() => jsonResponse(rolesResponse))
    .mockImplementationOnce(() =>
      jsonResponse({
        success: true,
        data: {
          roleCode: "R09",
          enabled: false,
          defaultDataScope: "ALL",
          message: "역할 관리 저장이 완료되었습니다.",
        },
      }),
    )
    .mockImplementationOnce(() => jsonResponse(rolesResponse));
  vi.stubGlobal("fetch", fetchMock);

  render(<App />);

  expect(
    await screen.findByRole("heading", { name: "역할 관리" }),
  ).toBeInTheDocument();
  await waitFor(() =>
    expect(screen.getAllByText("R09").length).toBeGreaterThan(0),
  );
  expect(screen.getAllByText("시스템관리자").length).toBeGreaterThan(0);
  expect(screen.getByText("사용자·조직·메뉴·권한 관리")).toBeInTheDocument();

  await userEvent.click(
    screen.getByRole("button", { name: "저장/실행 전 확인" }),
  );

  await waitFor(() =>
    expect(fetchMock).toHaveBeenCalledWith(
      "/api/admin/roles",
      expect.objectContaining({ method: "POST" }),
    ),
  );
  expect(await screen.findByText("총 1건 조회되었습니다.")).toBeInTheDocument();
});

test("role permission state is shown when admin role API returns forbidden", async () => {
  window.history.pushState({}, "", "/admin/security/roles");
  const fetchMock = vi
    .fn()
    .mockImplementationOnce(() => jsonResponse(sessionResponse))
    .mockImplementationOnce(() =>
      jsonResponse(
        {
          success: false,
          error: { code: "FORBIDDEN", message: "권한이 없습니다." },
        },
        403,
      ),
    );
  vi.stubGlobal("fetch", fetchMock);

  render(<App />);

  expect(await screen.findByText("PERMISSION DENIED")).toBeInTheDocument();
  expect(screen.getByText("권한이 없습니다.")).toBeInTheDocument();
});

test("smoke: opens user-role route, loads assignments, runs primary save action from selected row", async () => {
  window.history.pushState({}, "", "/admin/security/user-roles");
  vi.spyOn(window, "confirm").mockReturnValue(true);
  const fetchMock = vi
    .fn()
    .mockImplementationOnce(() => jsonResponse(sessionResponse))
    .mockImplementationOnce(() => jsonResponse(userRolesResponse))
    .mockImplementationOnce(() =>
      jsonResponse({
        success: true,
        data: {
          userRoleId: 1001,
          active: false,
          validTo: "2026-12-31",
          assignmentSource: "MANUAL",
          message: "사용자 역할 관리 저장이 완료되었습니다.",
        },
      }),
    )
    .mockImplementationOnce(() => jsonResponse(userRolesResponse));
  vi.stubGlobal("fetch", fetchMock);

  render(<App />);

  expect(
    await screen.findByRole("heading", { name: "사용자 역할 관리" }),
  ).toBeInTheDocument();
  await waitFor(() =>
    expect(screen.getAllByText("김교*").length).toBeGreaterThan(0),
  );
  expect(screen.getAllByText("교원 (R01)").length).toBeGreaterThan(0);
  expect(screen.getAllByText("수동").length).toBeGreaterThan(0);

  await userEvent.click(
    screen.getByRole("button", { name: "저장/실행 전 확인" }),
  );

  await waitFor(() =>
    expect(fetchMock).toHaveBeenCalledWith(
      "/api/admin/user-roles",
      expect.objectContaining({ method: "POST" }),
    ),
  );
  expect(await screen.findByText("총 1건 조회되었습니다.")).toBeInTheDocument();
});

test("user-role permission state is shown when admin user-role API returns forbidden", async () => {
  window.history.pushState({}, "", "/admin/security/user-roles");
  const fetchMock = vi
    .fn()
    .mockImplementationOnce(() => jsonResponse(sessionResponse))
    .mockImplementationOnce(() =>
      jsonResponse(
        {
          success: false,
          error: { code: "FORBIDDEN", message: "권한이 없습니다." },
        },
        403,
      ),
    );
  vi.stubGlobal("fetch", fetchMock);

  render(<App />);

  expect(await screen.findByText("PERMISSION DENIED")).toBeInTheDocument();
  expect(screen.getByText("권한이 없습니다.")).toBeInTheDocument();
});

test("smoke: opens menu-permission route, loads permission matrix, runs primary save action from selected row", async () => {
  window.history.pushState({}, "", "/admin/security/menu-permissions");
  vi.spyOn(window, "confirm").mockReturnValue(true);
  const fetchMock = vi
    .fn()
    .mockImplementationOnce(() => jsonResponse(sessionResponse))
    .mockImplementationOnce(() => jsonResponse(menuPermissionsResponse))
    .mockImplementationOnce(() =>
      jsonResponse({
        success: true,
        data: {
          menuPermissionId: 7001,
          allowed: false,
          targetType: "ROLE",
          targetId: "R09",
          message: "메뉴 권한 관리 저장이 완료되었습니다.",
        },
      }),
    )
    .mockImplementationOnce(() => jsonResponse(menuPermissionsResponse));
  vi.stubGlobal("fetch", fetchMock);

  render(<App />);

  expect(
    await screen.findByRole("heading", { name: "메뉴 권한 관리" }),
  ).toBeInTheDocument();
  await waitFor(() =>
    expect(screen.getAllByText("메뉴 권한 관리").length).toBeGreaterThan(0),
  );
  expect(screen.getAllByText("시스템관리자").length).toBeGreaterThan(0);
  expect(screen.getAllByText("SCR-MENU-PERMISSION").length).toBeGreaterThan(0);

  await userEvent.click(
    screen.getByRole("button", { name: "저장/실행 전 확인" }),
  );

  await waitFor(() =>
    expect(fetchMock).toHaveBeenCalledWith(
      "/api/admin/menu-permissions",
      expect.objectContaining({ method: "POST" }),
    ),
  );
  expect(await screen.findByText("총 1건 조회되었습니다.")).toBeInTheDocument();
});

test("menu-permission permission state is shown when admin menu-permission API returns forbidden", async () => {
  window.history.pushState({}, "", "/admin/security/menu-permissions");
  const fetchMock = vi
    .fn()
    .mockImplementationOnce(() => jsonResponse(sessionResponse))
    .mockImplementationOnce(() =>
      jsonResponse(
        {
          success: false,
          error: { code: "FORBIDDEN", message: "권한이 없습니다." },
        },
        403,
      ),
    );
  vi.stubGlobal("fetch", fetchMock);

  render(<App />);

  expect(await screen.findByText("PERMISSION DENIED")).toBeInTheDocument();
  expect(screen.getByText("권한이 없습니다.")).toBeInTheDocument();
});

test("smoke: opens function-permission route, loads function action matrix, runs primary save action from selected row", async () => {
  window.history.pushState({}, "", "/admin/security/function-permissions");
  vi.spyOn(window, "confirm").mockReturnValue(true);
  const fetchMock = vi
    .fn()
    .mockImplementationOnce(() => jsonResponse(sessionResponse))
    .mockImplementationOnce(() => jsonResponse(functionPermissionsResponse))
    .mockImplementationOnce(() =>
      jsonResponse({
        success: true,
        data: {
          functionPermissionId: 9001,
          allowed: false,
          roleCode: "R09",
          screenId: "SCR-FUNCTION-PERMISSION",
          actionCode: "UPDATE",
          message: "기능 권한 관리 저장이 완료되었습니다.",
        },
      }),
    )
    .mockImplementationOnce(() => jsonResponse(functionPermissionsResponse));
  vi.stubGlobal("fetch", fetchMock);

  render(<App />);

  expect(
    await screen.findByRole("heading", { name: "기능 권한 관리" }),
  ).toBeInTheDocument();
  await waitFor(() =>
    expect(screen.getAllByText("기능 권한 관리").length).toBeGreaterThan(0),
  );
  expect(screen.getAllByText("시스템관리자 (R09)").length).toBeGreaterThan(0);
  expect(screen.getAllByText("SCR-FUNCTION-PERMISSION").length).toBeGreaterThan(
    0,
  );
  expect(screen.getAllByText("수정").length).toBeGreaterThan(0);

  await userEvent.click(
    screen.getByRole("button", { name: "저장/실행 전 확인" }),
  );

  await waitFor(() =>
    expect(fetchMock).toHaveBeenCalledWith(
      "/api/admin/function-permissions",
      expect.objectContaining({ method: "POST" }),
    ),
  );
  expect(await screen.findByText("총 1건 조회되었습니다.")).toBeInTheDocument();
});

test("function-permission permission state is shown when admin function-permission API returns forbidden", async () => {
  window.history.pushState({}, "", "/admin/security/function-permissions");
  const fetchMock = vi
    .fn()
    .mockImplementationOnce(() => jsonResponse(sessionResponse))
    .mockImplementationOnce(() =>
      jsonResponse(
        {
          success: false,
          error: { code: "FORBIDDEN", message: "권한이 없습니다." },
        },
        403,
      ),
    );
  vi.stubGlobal("fetch", fetchMock);

  render(<App />);

  expect(await screen.findByText("PERMISSION DENIED")).toBeInTheDocument();
  expect(screen.getByText("권한이 없습니다.")).toBeInTheDocument();
});

test("smoke: opens data-scope route, loads scope matrix, runs primary save action from selected row", async () => {
  window.history.pushState({}, "", "/admin/security/data-scopes");
  vi.spyOn(window, "confirm").mockReturnValue(true);
  const fetchMock = vi
    .fn()
    .mockImplementationOnce(() => jsonResponse(sessionResponse))
    .mockImplementationOnce(() => jsonResponse(dataScopesResponse))
    .mockImplementationOnce(() =>
      jsonResponse({
        success: true,
        data: {
          dataScopeId: 10001,
          roleCode: "R09",
          scopeType: "ALL",
          organizationCode: "KNUE",
          businessArea: "COMMON_FOUNDATION",
          message: "데이터 범위 권한 저장이 완료되었습니다.",
        },
      }),
    )
    .mockImplementationOnce(() => jsonResponse(dataScopesResponse));
  vi.stubGlobal("fetch", fetchMock);

  render(<App />);

  expect(
    await screen.findByRole("heading", { name: "데이터 범위 권한" }),
  ).toBeInTheDocument();
  await waitFor(() =>
    expect(screen.getAllByText("시스템관리자 (R09)").length).toBeGreaterThan(0),
  );
  expect(screen.getAllByText("전체").length).toBeGreaterThan(0);
  expect(screen.getAllByText("공통기능 전체").length).toBeGreaterThan(0);

  await userEvent.click(
    screen.getByRole("button", { name: "저장/실행 전 확인" }),
  );

  await waitFor(() =>
    expect(fetchMock).toHaveBeenCalledWith(
      "/api/admin/data-scopes",
      expect.objectContaining({ method: "POST" }),
    ),
  );
  expect(await screen.findByText("총 1건 조회되었습니다.")).toBeInTheDocument();
});

test("data-scope permission state is shown when admin data-scope API returns forbidden", async () => {
  window.history.pushState({}, "", "/admin/security/data-scopes");
  const fetchMock = vi
    .fn()
    .mockImplementationOnce(() => jsonResponse(sessionResponse))
    .mockImplementationOnce(() =>
      jsonResponse(
        {
          success: false,
          error: { code: "FORBIDDEN", message: "권한이 없습니다." },
        },
        403,
      ),
    );
  vi.stubGlobal("fetch", fetchMock);

  render(<App />);

  expect(await screen.findByText("PERMISSION DENIED")).toBeInTheDocument();
  expect(screen.getByText("권한이 없습니다.")).toBeInTheDocument();
});

test("smoke: opens menu route, loads menus, runs primary save action from selected row", async () => {
  window.history.pushState({}, "", "/admin/menus");
  vi.spyOn(window, "confirm").mockReturnValue(true);
  const fetchMock = vi
    .fn()
    .mockImplementationOnce(() => jsonResponse(sessionResponse))
    .mockImplementationOnce(() => jsonResponse(menusResponse))
    .mockImplementationOnce(() =>
      jsonResponse({
        success: true,
        data: {
          menuId: "M-MENU-MGMT",
          menuName: "메뉴 관리",
          screenId: "SCR-MENU-MGMT",
          url: "/admin/menus",
          displayOrder: 19,
          message: "메뉴 관리 저장이 완료되었습니다.",
        },
      }),
    )
    .mockImplementationOnce(() => jsonResponse(menusResponse));
  vi.stubGlobal("fetch", fetchMock);

  render(<App />);

  expect(
    await screen.findByRole("heading", { name: "메뉴 관리" }),
  ).toBeInTheDocument();
  await waitFor(() =>
    expect(screen.getAllByText("M-MENU-MGMT").length).toBeGreaterThan(0),
  );
  expect(screen.getAllByText("시스템 관리").length).toBeGreaterThan(0);
  expect(screen.getAllByText("SCR-MENU-MGMT").length).toBeGreaterThan(0);
  expect(screen.getAllByText("/admin/menus").length).toBeGreaterThan(0);

  await userEvent.click(
    screen.getByRole("button", { name: "저장/실행 전 확인" }),
  );

  await waitFor(() =>
    expect(fetchMock).toHaveBeenCalledWith(
      "/api/admin/menus",
      expect.objectContaining({ method: "POST" }),
    ),
  );
  expect(await screen.findByText("총 1건 조회되었습니다.")).toBeInTheDocument();
});

test("menu permission state is shown when admin menu API returns forbidden", async () => {
  window.history.pushState({}, "", "/admin/menus");
  const fetchMock = vi
    .fn()
    .mockImplementationOnce(() => jsonResponse(sessionResponse))
    .mockImplementationOnce(() =>
      jsonResponse(
        {
          success: false,
          error: { code: "FORBIDDEN", message: "권한이 없습니다." },
        },
        403,
      ),
    );
  vi.stubGlobal("fetch", fetchMock);

  render(<App />);

  expect(await screen.findByText("PERMISSION DENIED")).toBeInTheDocument();
  expect(screen.getByText("권한이 없습니다.")).toBeInTheDocument();
});
test("smoke: opens code-group route, loads code groups, runs primary save action from selected row", async () => {
  window.history.pushState({}, "", "/admin/codes/groups");
  vi.spyOn(window, "confirm").mockReturnValue(true);
  const fetchMock = vi
    .fn()
    .mockImplementationOnce(() => jsonResponse(sessionResponse))
    .mockImplementationOnce(() => jsonResponse(codeGroupsResponse))
    .mockImplementationOnce(() =>
      jsonResponse({
        success: true,
        data: {
          groupId: "EVAL_AREA",
          groupName: "평가영역",
          managingDepartment: "교수지원과",
          enabled: false,
          message: "코드그룹 관리 저장이 완료되었습니다.",
        },
      }),
    )
    .mockImplementationOnce(() => jsonResponse(codeGroupsResponse));
  vi.stubGlobal("fetch", fetchMock);

  render(<App />);

  expect(
    await screen.findByRole("heading", { name: "코드그룹 관리" }),
  ).toBeInTheDocument();
  await waitFor(() =>
    expect(screen.getAllByText("EVAL_AREA").length).toBeGreaterThan(0),
  );
  expect(screen.getAllByText("평가영역").length).toBeGreaterThan(0);
  expect(screen.getAllByText("교수지원과").length).toBeGreaterThan(0);
  expect(
    screen.getByText("상세코드 관리에서 코드값·코드명·정렬순서 변경"),
  ).toBeInTheDocument();

  await userEvent.click(
    screen.getByRole("button", { name: "저장/실행 전 확인" }),
  );

  await waitFor(() =>
    expect(fetchMock).toHaveBeenCalledWith(
      "/api/admin/code-groups",
      expect.objectContaining({ method: "POST" }),
    ),
  );
  expect(await screen.findByText("총 1건 조회되었습니다.")).toBeInTheDocument();
});

test("code-group permission state is shown when admin code-group API returns forbidden", async () => {
  window.history.pushState({}, "", "/admin/codes/groups");
  const fetchMock = vi
    .fn()
    .mockImplementationOnce(() => jsonResponse(sessionResponse))
    .mockImplementationOnce(() =>
      jsonResponse(
        {
          success: false,
          error: { code: "FORBIDDEN", message: "권한이 없습니다." },
        },
        403,
      ),
    );
  vi.stubGlobal("fetch", fetchMock);

  render(<App />);

  expect(await screen.findByText("PERMISSION DENIED")).toBeInTheDocument();
  expect(screen.getByText("권한이 없습니다.")).toBeInTheDocument();
});

test("smoke: opens code-detail route, loads code details, runs primary save action from selected row", async () => {
  window.history.pushState({}, "", "/admin/codes/details");
  vi.spyOn(window, "confirm").mockReturnValue(true);
  const fetchMock = vi
    .fn()
    .mockImplementationOnce(() => jsonResponse(sessionResponse))
    .mockImplementationOnce(() => jsonResponse(codeDetailsResponse))
    .mockImplementationOnce(() =>
      jsonResponse({
        success: true,
        data: {
          codeDetailId: 1,
          groupId: "EVAL_AREA",
          codeValue: "TEACHING",
          codeName: "교육영역",
          displayOrder: 20,
          active: true,
          message: "상세코드 관리 저장이 완료되었습니다.",
        },
      }),
    )
    .mockImplementationOnce(() => jsonResponse(codeDetailsResponse));
  vi.stubGlobal("fetch", fetchMock);

  render(<App />);

  expect(
    await screen.findByRole("heading", { name: "상세코드 관리" }),
  ).toBeInTheDocument();
  await waitFor(() =>
    expect(screen.getAllByText("EVAL_AREA").length).toBeGreaterThan(0),
  );
  expect(screen.getAllByText("평가영역").length).toBeGreaterThan(0);
  expect(screen.getAllByText("TEACHING").length).toBeGreaterThan(0);
  expect(screen.getAllByText("교육영역").length).toBeGreaterThan(0);
  expect(
    screen.getByText("그룹 내 코드값은 중복될 수 없고 정렬순서로 표시됩니다."),
  ).toBeInTheDocument();

  await userEvent.click(
    screen.getByRole("button", { name: "저장/실행 전 확인" }),
  );

  await waitFor(() =>
    expect(fetchMock).toHaveBeenCalledWith(
      "/api/admin/code-details",
      expect.objectContaining({ method: "POST" }),
    ),
  );
  expect(await screen.findByText("총 1건 조회되었습니다.")).toBeInTheDocument();
});

test("code-detail permission state is shown when admin code-detail API returns forbidden", async () => {
  window.history.pushState({}, "", "/admin/codes/details");
  const fetchMock = vi
    .fn()
    .mockImplementationOnce(() => jsonResponse(sessionResponse))
    .mockImplementationOnce(() =>
      jsonResponse(
        {
          success: false,
          error: { code: "FORBIDDEN", message: "권한이 없습니다." },
        },
        403,
      ),
    );
  vi.stubGlobal("fetch", fetchMock);

  render(<App />);

  expect(await screen.findByText("PERMISSION DENIED")).toBeInTheDocument();
  expect(screen.getByText("권한이 없습니다.")).toBeInTheDocument();
});

test("smoke: opens system configuration route, loads configurations, runs primary save action from selected row", async () => {
  window.history.pushState({}, "", "/admin/settings/common");
  vi.spyOn(window, "confirm").mockReturnValue(true);
  const fetchMock = vi
    .fn()
    .mockImplementationOnce(() => jsonResponse(sessionResponse))
    .mockImplementationOnce(() => jsonResponse(systemConfigurationsResponse))
    .mockImplementationOnce(() =>
      jsonResponse({
        success: true,
        data: {
          configKey: "SESSION_IDLE_MINUTES",
          configValue: "31",
          unit: "분",
          valueRange: "5-240",
          enabled: true,
          message: "공통 환경설정 저장이 완료되었습니다.",
        },
      }),
    )
    .mockImplementationOnce(() => jsonResponse(systemConfigurationsResponse));
  vi.stubGlobal("fetch", fetchMock);

  render(<App />);

  expect(
    await screen.findByRole("heading", { name: "공통 환경설정" }),
  ).toBeInTheDocument();
  expect(await screen.findByText("SESSION_IDLE_MINUTES")).toBeInTheDocument();
  expect(screen.getAllByText("30").length).toBeGreaterThan(0);
  expect(screen.getAllByText("전체 사용자 공통 적용").length).toBeGreaterThan(
    0,
  );

  await userEvent.click(
    screen.getByRole("button", { name: "저장/실행 전 확인" }),
  );

  await waitFor(() =>
    expect(fetchMock).toHaveBeenCalledWith(
      "/api/admin/system-configurations",
      expect.objectContaining({ method: "POST" }),
    ),
  );
  expect(await screen.findByText("총 1건 조회되었습니다.")).toBeInTheDocument();
});

test("system configuration permission state is shown when API returns forbidden", async () => {
  window.history.pushState({}, "", "/admin/settings/common");
  const fetchMock = vi
    .fn()
    .mockImplementationOnce(() => jsonResponse(sessionResponse))
    .mockImplementationOnce(() =>
      jsonResponse(
        {
          success: false,
          error: { code: "FORBIDDEN", message: "권한이 없습니다." },
        },
        403,
      ),
    );
  vi.stubGlobal("fetch", fetchMock);

  render(<App />);

  expect(await screen.findByText("PERMISSION DENIED")).toBeInTheDocument();
  expect(screen.getByText("권한이 없습니다.")).toBeInTheDocument();
});

test("smoke: opens base-year route, loads base years, runs primary save action from selected row", async () => {
  window.history.pushState({}, "", "/admin/settings/base-years");
  vi.spyOn(window, "confirm").mockReturnValue(true);
  const fetchMock = vi
    .fn()
    .mockImplementationOnce(() => jsonResponse(sessionResponse))
    .mockImplementationOnce(() => jsonResponse(baseYearsResponse))
    .mockImplementationOnce(() =>
      jsonResponse({
        success: true,
        data: {
          baseYear: "2026",
          defaultQueryYear: "2025",
          copyBaselineEnabled: true,
          resetEnabled: false,
          enabled: true,
          message: "기준연도 관리 저장이 완료되었습니다.",
        },
      }),
    )
    .mockImplementationOnce(() => jsonResponse(baseYearsResponse));
  vi.stubGlobal("fetch", fetchMock);

  render(<App />);

  expect(
    await screen.findByRole("heading", { name: "기준연도 관리" }),
  ).toBeInTheDocument();
  await waitFor(() =>
    expect(screen.getAllByText("2026").length).toBeGreaterThan(0),
  );
  expect(
    screen.getByText(
      "기준연도는 4자리 연도이며 기본 조회연도는 기준연도 이하로 관리합니다.",
    ),
  ).toBeInTheDocument();
  expect(screen.getAllByText("허용").length).toBeGreaterThan(0);

  await userEvent.click(
    screen.getByRole("button", { name: "저장/실행 전 확인" }),
  );

  await waitFor(() =>
    expect(fetchMock).toHaveBeenCalledWith(
      "/api/admin/base-years",
      expect.objectContaining({ method: "POST" }),
    ),
  );
  expect(await screen.findByText("총 1건 조회되었습니다.")).toBeInTheDocument();
});

test("base-year permission state is shown when API returns forbidden", async () => {
  window.history.pushState({}, "", "/admin/settings/base-years");
  const fetchMock = vi
    .fn()
    .mockImplementationOnce(() => jsonResponse(sessionResponse))
    .mockImplementationOnce(() =>
      jsonResponse(
        {
          success: false,
          error: { code: "FORBIDDEN", message: "권한이 없습니다." },
        },
        403,
      ),
    );
  vi.stubGlobal("fetch", fetchMock);

  render(<App />);

  expect(await screen.findByText("PERMISSION DENIED")).toBeInTheDocument();
  expect(screen.getByText("권한이 없습니다.")).toBeInTheDocument();
});

test("smoke: opens file-policy route, loads file policies, runs primary save action from selected row", async () => {
  window.history.pushState({}, "", "/admin/settings/file-policies");
  vi.spyOn(window, "confirm").mockReturnValue(true);
  const fetchMock = vi
    .fn()
    .mockImplementationOnce(() => jsonResponse(sessionResponse))
    .mockImplementationOnce(() => jsonResponse(filePoliciesResponse))
    .mockImplementationOnce(() =>
      jsonResponse({
        success: true,
        data: {
          filePolicyId: 1,
          businessArea: "COMMON",
          allowedExtensions: "pdf,xlsx,docx,png,jpg",
          maxFileSizeMb: 25,
          maxFileCount: 6,
          maxTotalSizeMb: 150,
          maxFilenameLength: 120,
          malwareScanEnabled: true,
          enabled: true,
          message: "파일정책 관리 저장이 완료되었습니다.",
        },
      }),
    )
    .mockImplementationOnce(() => jsonResponse(filePoliciesResponse));
  vi.stubGlobal("fetch", fetchMock);

  render(<App />);

  expect(
    await screen.findByRole("heading", { name: "파일정책 관리" }),
  ).toBeInTheDocument();
  await waitFor(() =>
    expect(screen.getAllByText("COMMON").length).toBeGreaterThan(0),
  );
  expect(screen.getAllByText("공통 첨부").length).toBeGreaterThan(0);
  expect(screen.getAllByText("pdf,xlsx,docx,png,jpg").length).toBeGreaterThan(
    0,
  );
  expect(
    screen.getByText(
      "첨부파일 업로드 검증 시 확장자·용량·개수·파일명 길이 정책을 적용합니다.",
    ),
  ).toBeInTheDocument();
  expect(
    screen.getByText(
      "파일정책 ID와 업무영역은 생명주기 식별자로 읽기 전용입니다. 저장 CTA는 첨부파일 검증 정책값만 전달하며 실제 파일 업로드·조회·삭제 API를 호출하지 않습니다.",
    ),
  ).toBeInTheDocument();

  await userEvent.click(
    screen.getByRole("button", { name: "저장/실행 전 확인" }),
  );

  await waitFor(() =>
    expect(fetchMock).toHaveBeenCalledWith(
      "/api/admin/file-policies",
      expect.objectContaining({ method: "POST" }),
    ),
  );
  expect(await screen.findByText("총 1건 조회되었습니다.")).toBeInTheDocument();
});

test("file-policy permission state is shown when API returns forbidden", async () => {
  window.history.pushState({}, "", "/admin/settings/file-policies");
  const fetchMock = vi
    .fn()
    .mockImplementationOnce(() => jsonResponse(sessionResponse))
    .mockImplementationOnce(() =>
      jsonResponse(
        {
          success: false,
          error: { code: "FORBIDDEN", message: "권한이 없습니다." },
        },
        403,
      ),
    );
  vi.stubGlobal("fetch", fetchMock);

  render(<App />);

  expect(await screen.findByText("PERMISSION DENIED")).toBeInTheDocument();
  expect(screen.getByText("권한이 없습니다.")).toBeInTheDocument();
});

test("smoke: opens notice route, loads notices, runs primary save action from selected row", async () => {
  window.history.pushState({}, "", "/admin/notices");
  vi.spyOn(window, "confirm").mockReturnValue(true);
  const fetchMock = vi
    .fn()
    .mockImplementationOnce(() => jsonResponse(sessionResponse))
    .mockImplementationOnce(() => jsonResponse(noticesResponse))
    .mockImplementationOnce(() =>
      jsonResponse({
        success: true,
        data: {
          noticeId: 1,
          title: "2026학년도 교수업적평가 공통 일정 안내",
          postFrom: "2026-01-01",
          postTo: "2026-12-31",
          targetRoles: "R01,R09",
          targetOrganizations: "KNUE-EDU",
          important: false,
          enabled: true,
          message: "공지사항 관리 저장이 완료되었습니다.",
        },
      }),
    )
    .mockImplementationOnce(() => jsonResponse(noticesResponse));
  vi.stubGlobal("fetch", fetchMock);

  render(<App />);

  expect(
    await screen.findByRole("heading", { name: "공지사항 관리" }),
  ).toBeInTheDocument();
  await waitFor(() =>
    expect(
      screen.getAllByText("2026학년도 교수업적평가 공통 일정 안내").length,
    ).toBeGreaterThan(0),
  );
  expect(screen.getAllByText("R01,R09").length).toBeGreaterThan(0);
  expect(screen.getAllByText("KNUE-EDU").length).toBeGreaterThan(0);
  expect(
    screen.getByText("지정 대상 역할·조직과 게시기간에만 노출됩니다."),
  ).toBeInTheDocument();
  expect(
    screen.getByText("공지 열람은 업무 승인이나 확인처리로 간주하지 않습니다."),
  ).toBeInTheDocument();

  await userEvent.click(
    screen.getByRole("button", { name: "저장/실행 전 확인" }),
  );

  await waitFor(() =>
    expect(fetchMock).toHaveBeenCalledWith(
      "/api/admin/notices",
      expect.objectContaining({ method: "POST" }),
    ),
  );
  expect(await screen.findByText("총 1건 조회되었습니다.")).toBeInTheDocument();
});

test("notice permission state is shown when API returns forbidden", async () => {
  window.history.pushState({}, "", "/admin/notices");
  const fetchMock = vi
    .fn()
    .mockImplementationOnce(() => jsonResponse(sessionResponse))
    .mockImplementationOnce(() =>
      jsonResponse(
        {
          success: false,
          error: { code: "FORBIDDEN", message: "권한이 없습니다." },
        },
        403,
      ),
    );
  vi.stubGlobal("fetch", fetchMock);

  render(<App />);

  expect(await screen.findByText("PERMISSION DENIED")).toBeInTheDocument();
  expect(screen.getByText("권한이 없습니다.")).toBeInTheDocument();
});

test("smoke: opens attachment route, loads metadata, runs logical delete CTA from selected row", async () => {
  window.history.pushState({}, "", "/admin/files/attachments");
  vi.spyOn(window, "confirm").mockReturnValue(true);
  const fetchMock = vi
    .fn()
    .mockImplementationOnce(() => jsonResponse(sessionResponse))
    .mockImplementationOnce(() => jsonResponse(attachmentsResponse))
    .mockImplementationOnce(() =>
      jsonResponse({
        success: true,
        data: {
          attachmentId: 1,
          businessKey: "NOTICE:1",
          originalName: "평가일정 안내.pdf",
          deleted: true,
          actionResult: "LOGICAL_DELETE",
          message: "첨부파일 관리 저장이 완료되었습니다.",
        },
      }),
    )
    .mockImplementationOnce(() => jsonResponse(attachmentsResponse));
  vi.stubGlobal("fetch", fetchMock);

  render(<App />);

  expect(
    await screen.findByRole("heading", { name: "첨부파일 관리" }),
  ).toBeInTheDocument();
  await waitFor(() =>
    expect(screen.getAllByText("평가일정 안내.pdf").length).toBeGreaterThan(0),
  );
  expect(screen.getAllByText("NOTICE:1").length).toBeGreaterThan(0);
  expect(
    screen.getByText("다운로드 시 권한을 재검증합니다."),
  ).toBeInTheDocument();
  expect(
    screen.getByText("개발·검증 환경에서는 논리삭제만 허용합니다."),
  ).toBeInTheDocument();

  await userEvent.click(
    screen.getByRole("button", { name: "논리삭제 전 확인" }),
  );

  await waitFor(() =>
    expect(fetchMock).toHaveBeenCalledWith(
      "/api/admin/attachments",
      expect.objectContaining({ method: "POST" }),
    ),
  );
  expect(await screen.findByText("총 1건 조회되었습니다.")).toBeInTheDocument();
});

test("smoke: opens excel-template route, loads templates, runs primary save action from selected row", async () => {
  window.history.pushState({}, "", "/admin/excel/templates");
  vi.spyOn(window, "confirm").mockReturnValue(true);
  const fetchMock = vi
    .fn()
    .mockImplementationOnce(() => jsonResponse(sessionResponse))
    .mockImplementationOnce(() => jsonResponse(excelTemplatesResponse))
    .mockImplementationOnce(() =>
      jsonResponse({
        success: true,
        data: {
          templateId: 1,
          businessArea: "ACHIEVEMENT",
          version: "2026.1",
          enabled: false,
          requiredColumnCount: 1,
          message: "업로드 양식 관리 저장이 완료되었습니다.",
        },
      }),
    )
    .mockImplementationOnce(() => jsonResponse(excelTemplatesResponse));
  vi.stubGlobal("fetch", fetchMock);

  render(<App />);

  expect(
    await screen.findByRole("heading", { name: "업로드 양식 관리" }),
  ).toBeInTheDocument();
  await waitFor(() =>
    expect(screen.getAllByText("ACHIEVEMENT").length).toBeGreaterThan(0),
  );
  expect(screen.getAllByText("교수업적").length).toBeGreaterThan(0);
  expect(screen.getAllByText("2026.1").length).toBeGreaterThan(0);
  expect(
    screen.getByText("필수값·타입·중복규칙을 템플릿 버전으로 검증합니다."),
  ).toBeInTheDocument();
  expect(
    screen.getByText(
      "다운로드 시 첨부파일 권한과 템플릿 사용여부를 재검증합니다.",
    ),
  ).toBeInTheDocument();

  await userEvent.click(
    screen.getByRole("button", { name: "미사용 전환 전 확인" }),
  );

  await waitFor(() =>
    expect(fetchMock).toHaveBeenCalledWith(
      "/api/admin/excel-templates",
      expect.objectContaining({ method: "POST" }),
    ),
  );
  expect(await screen.findByText("총 1건 조회되었습니다.")).toBeInTheDocument();
});

test("smoke: opens excel-upload route, loads histories, runs sample upload action from selected row", async () => {
  window.history.pushState({}, "", "/admin/excel/uploads");
  vi.spyOn(window, "confirm").mockReturnValue(true);
  const fetchMock = vi
    .fn()
    .mockImplementationOnce(() => jsonResponse(sessionResponse))
    .mockImplementationOnce(() => jsonResponse(excelUploadsResponse))
    .mockImplementationOnce(() =>
      jsonResponse({
        success: true,
        data: {
          uploadId: 7,
          templateId: 1,
          uploadStatus: "SUCCESS",
          totalCount: 2,
          successCount: 2,
          errorCount: 0,
          excludedCount: 0,
          savedCount: 2,
          message: "엑셀 업로드 검증과 등록이 완료되었습니다.",
        },
      }),
    )
    .mockImplementationOnce(() => jsonResponse(excelUploadsResponse));
  vi.stubGlobal("fetch", fetchMock);

  render(<App />);

  expect(
    await screen.findByRole("heading", { name: "엑셀 업로드" }),
  ).toBeInTheDocument();
  await waitFor(() =>
    expect(
      screen.getAllByText("교수업적_업로드_정상_2026.xlsx").length,
    ).toBeGreaterThan(0),
  );
  expect(
    screen.getByText(
      "업무별 확정 양식 버전과 헤더·필수값·형식·코드·중복을 검증합니다.",
    ),
  ).toBeInTheDocument();
  expect(
    screen.getByText("모든 행이 정상일 때만 하나의 트랜잭션으로 등록합니다."),
  ).toBeInTheDocument();

  await userEvent.click(
    screen.getByRole("button", { name: "샘플 업로드 전 확인" }),
  );

  await waitFor(() =>
    expect(fetchMock).toHaveBeenCalledWith(
      "/api/admin/excel-uploads",
      expect.objectContaining({ method: "POST" }),
    ),
  );
  expect(await screen.findByText("총 1건 조회되었습니다.")).toBeInTheDocument();
});

test("excel-upload permission state is shown when API returns forbidden", async () => {
  window.history.pushState({}, "", "/admin/excel/uploads");
  const fetchMock = vi
    .fn()
    .mockImplementationOnce(() => jsonResponse(sessionResponse))
    .mockImplementationOnce(() =>
      jsonResponse(
        {
          success: false,
          error: { code: "FORBIDDEN", message: "권한이 없습니다." },
        },
        403,
      ),
    );
  vi.stubGlobal("fetch", fetchMock);

  render(<App />);

  expect(await screen.findByText("PERMISSION DENIED")).toBeInTheDocument();
  expect(screen.getByText("권한이 없습니다.")).toBeInTheDocument();
});

test("excel-template permission state is shown when API returns forbidden", async () => {
  window.history.pushState({}, "", "/admin/excel/templates");
  const fetchMock = vi
    .fn()
    .mockImplementationOnce(() => jsonResponse(sessionResponse))
    .mockImplementationOnce(() =>
      jsonResponse(
        {
          success: false,
          error: { code: "FORBIDDEN", message: "권한이 없습니다." },
        },
        403,
      ),
    );
  vi.stubGlobal("fetch", fetchMock);

  render(<App />);

  expect(await screen.findByText("PERMISSION DENIED")).toBeInTheDocument();
  expect(screen.getByText("권한이 없습니다.")).toBeInTheDocument();
});

test("attachment permission state is shown when API returns forbidden", async () => {
  window.history.pushState({}, "", "/admin/files/attachments");
  const fetchMock = vi
    .fn()
    .mockImplementationOnce(() => jsonResponse(sessionResponse))
    .mockImplementationOnce(() =>
      jsonResponse(
        {
          success: false,
          error: { code: "FORBIDDEN", message: "권한이 없습니다." },
        },
        403,
      ),
    );
  vi.stubGlobal("fetch", fetchMock);

  render(<App />);

  expect(await screen.findByText("PERMISSION DENIED")).toBeInTheDocument();
  expect(screen.getByText("권한이 없습니다.")).toBeInTheDocument();
});

test("smoke: opens excel-download route, loads requests, creates xlsx download request from current query", async () => {
  window.history.pushState({}, "", "/admin/excel/downloads");
  vi.spyOn(window, "confirm").mockReturnValue(true);
  const fetchMock = vi
    .fn()
    .mockImplementationOnce(() => jsonResponse(sessionResponse))
    .mockImplementationOnce(() => jsonResponse(excelDownloadsResponse))
    .mockImplementationOnce(() =>
      jsonResponse({
        success: true,
        data: {
          downloadId: 7,
          fileId: 1,
          fileName: "ACHIEVEMENT_조회결과_20260816.xlsx",
          status: "READY",
          message: "엑셀 다운로드 요청이 생성되었습니다.",
        },
      }),
    )
    .mockImplementationOnce(() => jsonResponse(excelDownloadsResponse));
  vi.stubGlobal("fetch", fetchMock);

  render(<App />);

  expect(
    await screen.findByRole("heading", { name: "엑셀 다운로드" }),
  ).toBeInTheDocument();
  await waitFor(() =>
    expect(
      screen.getAllByText("교수업적_조회결과_2026.xlsx").length,
    ).toBeGreaterThan(0),
  );
  expect(
    screen.getByText(
      "현재 조회조건과 사용자 데이터범위 권한을 적용하여 생성합니다.",
    ),
  ).toBeInTheDocument();
  expect(
    screen.getByText(
      "원천 업무자료는 변경하지 않고 권한 밖 자료는 포함하지 않습니다.",
    ),
  ).toBeInTheDocument();

  await userEvent.click(
    screen.getByRole("button", { name: "xlsx 다운로드 전 확인" }),
  );

  await waitFor(() =>
    expect(fetchMock).toHaveBeenCalledWith(
      "/api/admin/excel-downloads",
      expect.objectContaining({ method: "POST" }),
    ),
  );
  expect(await screen.findByText("총 1건 조회되었습니다.")).toBeInTheDocument();
});

test("smoke: opens privacy route, loads field policies, runs primary save action from selected row", async () => {
  window.history.pushState({}, "", "/admin/security/privacy");
  vi.spyOn(window, "confirm").mockReturnValue(true);
  const fetchMock = vi
    .fn()
    .mockImplementationOnce(() => jsonResponse(sessionResponse))
    .mockImplementationOnce(() => jsonResponse(privacyPoliciesResponse))
    .mockImplementationOnce(() =>
      jsonResponse({
        success: true,
        data: {
          fieldPolicyId: 1,
          fieldName: "researcher_registration_no",
          privacyGrade: "PERSONAL",
          encryptionEnabled: true,
          logExcluded: true,
          message: "개인정보 관리 정책 저장이 완료되었습니다.",
        },
      }),
    )
    .mockImplementationOnce(() => jsonResponse(privacyPoliciesResponse));
  vi.stubGlobal("fetch", fetchMock);

  render(<App />);

  expect(
    await screen.findByRole("heading", { name: "개인정보 관리" }),
  ).toBeInTheDocument();
  await waitFor(() =>
    expect(
      screen.getAllByText("researcher_registration_no").length,
    ).toBeGreaterThan(0),
  );
  expect(screen.getAllByText("민감정보").length).toBeGreaterThan(0);
  expect(
    screen.getByText("AES-256-GCM 암호화와 HMAC 검색 식별자 적용 대상입니다."),
  ).toBeInTheDocument();
  expect(
    screen.getByText(
      "감사로그에는 원문과 처리값을 제외하고 목적·결과만 기록합니다.",
    ),
  ).toBeInTheDocument();

  await userEvent.click(
    screen.getByRole("button", { name: "정책 저장 전 확인" }),
  );

  await waitFor(() =>
    expect(fetchMock).toHaveBeenCalledWith(
      "/api/admin/privacy-policies",
      expect.objectContaining({ method: "POST" }),
    ),
  );
  expect(await screen.findByText("총 1건 조회되었습니다.")).toBeInTheDocument();
});

test("privacy permission state is shown when API returns forbidden", async () => {
  window.history.pushState({}, "", "/admin/security/privacy");
  const fetchMock = vi
    .fn()
    .mockImplementationOnce(() => jsonResponse(sessionResponse))
    .mockImplementationOnce(() =>
      jsonResponse(
        {
          success: false,
          error: { code: "FORBIDDEN", message: "권한이 없습니다." },
        },
        403,
      ),
    );
  vi.stubGlobal("fetch", fetchMock);

  render(<App />);

  expect(await screen.findByText("PERMISSION DENIED")).toBeInTheDocument();
  expect(screen.getByText("권한이 없습니다.")).toBeInTheDocument();
});

test("smoke: opens session route, loads active sessions, runs forced termination CTA from selected row", async () => {
  window.history.pushState({}, "", "/admin/security/sessions");
  vi.spyOn(window, "confirm").mockReturnValue(true);
  const fetchMock = vi
    .fn()
    .mockImplementationOnce(() => jsonResponse(sessionResponse))
    .mockImplementationOnce(() => jsonResponse(activeSessionsResponse))
    .mockImplementationOnce(() =>
      jsonResponse({
        success: true,
        data: {
          sessionId: "SEED-ACTIVE-ADMIN",
          sessionStatus: "TERMINATED",
          terminationType: "FORCED",
          message: "접속현황 관리 세션 강제종료가 완료되었습니다.",
        },
      }),
    )
    .mockImplementationOnce(() => jsonResponse(activeSessionsResponse));
  vi.stubGlobal("fetch", fetchMock);

  render(<App />);

  expect(
    await screen.findByRole("heading", { name: "접속현황 관리" }),
  ).toBeInTheDocument();
  await waitFor(() =>
    expect(screen.getAllByText("SEED-ACTIVE-ADMIN").length).toBeGreaterThan(0),
  );
  expect(screen.getAllByText("관리자 (admin)").length).toBeGreaterThan(0);
  expect(screen.getAllByText("127.0.0.1").length).toBeGreaterThan(0);
  expect(
    screen.getByText("강제종료 가능: 활성 세션이며 R09 사유 입력 필요"),
  ).toBeInTheDocument();

  await userEvent.click(
    screen.getByRole("button", { name: "저장/실행 전 확인" }),
  );

  await waitFor(() =>
    expect(fetchMock).toHaveBeenCalledWith(
      "/api/admin/sessions",
      expect.objectContaining({ method: "POST" }),
    ),
  );
  expect(await screen.findByText("총 1건 조회되었습니다.")).toBeInTheDocument();
});

test("session permission state is shown when API returns forbidden", async () => {
  window.history.pushState({}, "", "/admin/security/sessions");
  const fetchMock = vi
    .fn()
    .mockImplementationOnce(() => jsonResponse(sessionResponse))
    .mockImplementationOnce(() =>
      jsonResponse(
        {
          success: false,
          error: { code: "FORBIDDEN", message: "권한이 없습니다." },
        },
        403,
      ),
    );
  vi.stubGlobal("fetch", fetchMock);

  render(<App />);

  expect(await screen.findByText("PERMISSION DENIED")).toBeInTheDocument();
  expect(screen.getByText("권한이 없습니다.")).toBeInTheDocument();
});

test("excel-download permission state is shown when API returns forbidden", async () => {
  window.history.pushState({}, "", "/admin/excel/downloads");
  const fetchMock = vi
    .fn()
    .mockImplementationOnce(() => jsonResponse(sessionResponse))
    .mockImplementationOnce(() =>
      jsonResponse(
        {
          success: false,
          error: { code: "FORBIDDEN", message: "권한이 없습니다." },
        },
        403,
      ),
    );
  vi.stubGlobal("fetch", fetchMock);

  render(<App />);

  expect(await screen.findByText("PERMISSION DENIED")).toBeInTheDocument();
  expect(screen.getByText("권한이 없습니다.")).toBeInTheDocument();
});

test("smoke: opens audit-log route, loads immutable logs, records confirmation audit from selected row", async () => {
  window.history.pushState({}, "", "/admin/security/audit-logs");
  vi.spyOn(window, "confirm").mockReturnValue(true);
  const fetchMock = vi
    .fn()
    .mockImplementationOnce(() => jsonResponse(sessionResponse))
    .mockImplementationOnce(() => jsonResponse(auditLogsResponse))
    .mockImplementationOnce(() =>
      jsonResponse({
        success: true,
        data: {
          auditLogId: 2002,
          targetKey: "audit_logs:1001",
          result: "SUCCESS",
          message: "감사 로그 관리 확인 이력이 기록되었습니다.",
        },
      }),
    )
    .mockImplementationOnce(() => jsonResponse(auditLogsResponse));
  vi.stubGlobal("fetch", fetchMock);

  render(<App />);

  expect(
    await screen.findByRole("heading", { name: "감사 로그 관리" }),
  ).toBeInTheDocument();
  await waitFor(() =>
    expect(screen.getAllByText("roles:R09").length).toBeGreaterThan(0),
  );
  expect(screen.getAllByText("권한").length).toBeGreaterThan(0);
  expect(screen.getAllByText("admin").length).toBeGreaterThan(0);
  expect(
    screen.getByText(
      "감사로그 원문은 수정·삭제할 수 없으며 상세에서 변경 전후값을 조회합니다.",
    ),
  ).toBeInTheDocument();

  await userEvent.click(
    screen.getByRole("button", { name: "확인 이력 기록 전 확인" }),
  );

  await waitFor(() =>
    expect(fetchMock).toHaveBeenCalledWith(
      "/api/admin/audit-logs",
      expect.objectContaining({ method: "POST" }),
    ),
  );
  expect(await screen.findByText("총 1건 조회되었습니다.")).toBeInTheDocument();
});

test("audit-log permission state is shown when API returns forbidden", async () => {
  window.history.pushState({}, "", "/admin/security/audit-logs");
  const fetchMock = vi
    .fn()
    .mockImplementationOnce(() => jsonResponse(sessionResponse))
    .mockImplementationOnce(() =>
      jsonResponse(
        {
          success: false,
          error: { code: "FORBIDDEN", message: "권한이 없습니다." },
        },
        403,
      ),
    );
  vi.stubGlobal("fetch", fetchMock);

  render(<App />);

  expect(await screen.findByText("PERMISSION DENIED")).toBeInTheDocument();
  expect(screen.getByText("권한이 없습니다.")).toBeInTheDocument();
});

test("smoke: opens batch definition route, loads definitions, saves selected definition without execution", async () => {
  window.history.pushState({}, "", "/admin/operations/batch-definitions");
  vi.spyOn(window, "confirm").mockReturnValue(true);
  const fetchMock = vi
    .fn()
    .mockImplementationOnce(() => jsonResponse(sessionResponse))
    .mockImplementationOnce(() => jsonResponse(batchDefinitionsResponse))
    .mockImplementationOnce(() =>
      jsonResponse({
        success: true,
        data: {
          batchId: "COMMON-AUDIT-ROLLUP",
          status: "DEFINED",
          message: "배치 정의 관리 저장이 완료되었습니다.",
        },
      }),
    )
    .mockImplementationOnce(() => jsonResponse(batchDefinitionsResponse));
  vi.stubGlobal("fetch", fetchMock);

  render(<App />);

  expect(
    await screen.findByRole("heading", { name: "배치 정의 관리" }),
  ).toBeInTheDocument();
  await waitFor(() =>
    expect(screen.getAllByText("COMMON-AUDIT-ROLLUP").length).toBeGreaterThan(
      0,
    ),
  );
  expect(screen.getByText("감사 로그 일별 집계")).toBeInTheDocument();
  expect(
    screen.getByText(
      "배치 정의 화면은 즉시 실행·중지·재실행을 제공하지 않고 정의만 저장합니다.",
    ),
  ).toBeInTheDocument();

  await userEvent.click(screen.getByRole("button", { name: "저장 전 확인" }));

  await waitFor(() =>
    expect(fetchMock).toHaveBeenCalledWith(
      "/api/admin/batch-definitions",
      expect.objectContaining({ method: "POST" }),
    ),
  );
  expect(await screen.findByText("총 1건 조회되었습니다.")).toBeInTheDocument();
});

test("batch definition permission state is shown when API returns forbidden", async () => {
  window.history.pushState({}, "", "/admin/operations/batch-definitions");
  const fetchMock = vi
    .fn()
    .mockImplementationOnce(() => jsonResponse(sessionResponse))
    .mockImplementationOnce(() =>
      jsonResponse(
        {
          success: false,
          error: { code: "FORBIDDEN", message: "권한이 없습니다." },
        },
        403,
      ),
    );
  vi.stubGlobal("fetch", fetchMock);

  render(<App />);

  expect(await screen.findByText("PERMISSION DENIED")).toBeInTheDocument();
  expect(screen.getByText("권한이 없습니다.")).toBeInTheDocument();
});

test("smoke: opens batch-execution route, loads executions, runs primary manual execution action from selected row", async () => {
  window.history.pushState({}, "", "/admin/operations/batch-executions");
  vi.spyOn(window, "confirm").mockReturnValue(true);
  const fetchMock = vi
    .fn()
    .mockImplementationOnce(() => jsonResponse(sessionResponse))
    .mockImplementationOnce(() => jsonResponse(batchExecutionsResponse))
    .mockImplementationOnce(() =>
      jsonResponse({
        success: true,
        data: {
          batchExecutionId: 11,
          batchId: "COMMON-AUDIT-ROLLUP",
          executionStatus: "RUNNING",
          message: "배치 수동실행 요청이 기록되었습니다.",
        },
      }),
    )
    .mockImplementationOnce(() => jsonResponse(batchExecutionsResponse));
  vi.stubGlobal("fetch", fetchMock);

  render(<App />);

  expect(
    await screen.findByRole("heading", { name: "배치 실행 관리" }),
  ).toBeInTheDocument();
  await waitFor(() =>
    expect(screen.getAllByText("COMMON-AUDIT-ROLLUP").length).toBeGreaterThan(
      0,
    ),
  );
  expect(screen.getAllByText("실행중").length).toBeGreaterThan(0);
  expect(
    screen.getByText("중지 가능: 실행 중 배치이며 R09 사유 입력 필요"),
  ).toBeInTheDocument();

  await userEvent.click(
    screen.getByRole("button", { name: "수동실행 전 확인" }),
  );

  await waitFor(() =>
    expect(fetchMock).toHaveBeenCalledWith(
      "/api/admin/batch-executions",
      expect.objectContaining({ method: "POST" }),
    ),
  );
  expect(await screen.findByText("총 1건 조회되었습니다.")).toBeInTheDocument();
});

test("batch-execution permission state is shown when API returns forbidden", async () => {
  window.history.pushState({}, "", "/admin/operations/batch-executions");
  const fetchMock = vi
    .fn()
    .mockImplementationOnce(() => jsonResponse(sessionResponse))
    .mockImplementationOnce(() =>
      jsonResponse(
        {
          success: false,
          error: { code: "FORBIDDEN", message: "권한이 없습니다." },
        },
        403,
      ),
    );
  vi.stubGlobal("fetch", fetchMock);

  render(<App />);

  expect(await screen.findByText("PERMISSION DENIED")).toBeInTheDocument();
  expect(screen.getByText("권한이 없습니다.")).toBeInTheDocument();
});

test("smoke: opens batch-result route, loads immutable result counts, and previews selected log", async () => {
  window.history.pushState({}, "", "/admin/operations/batch-results");
  vi.spyOn(window, "confirm").mockReturnValue(true);
  const fetchMock = vi
    .fn()
    .mockImplementationOnce(() => jsonResponse(sessionResponse))
    .mockImplementationOnce(() => jsonResponse(batchResultsResponse))
    .mockImplementationOnce(() =>
      jsonResponse({
        success: true,
        data: {
          batchResultId: 100,
          batchExecutionId: 10,
          logFileId: 77,
          logFileName: "batch-result-10.log",
          accessMessage: "로그파일 조회 감사 이력이 기록되었습니다.",
          immutableRule: "로그파일은 조회만 가능하며 수정·삭제하지 않습니다.",
        },
      }),
    );
  vi.stubGlobal("fetch", fetchMock);

  render(<App />);

  expect(
    await screen.findByRole("heading", { name: "배치 결과 조회" }),
  ).toBeInTheDocument();
  expect(
    screen.getByRole("navigation", { name: "현재 메뉴 경로" }),
  ).toHaveTextContent("시스템 운영 관리 > 배치작업 관리 > 배치 결과 조회");
  expect(screen.getByRole("link", { name: "배치 결과 조회" })).toHaveAttribute(
    "aria-current",
    "page",
  );
  await waitFor(() =>
    expect(screen.getAllByText("COMMON-AUDIT-ROLLUP").length).toBeGreaterThan(
      0,
    ),
  );
  expect(screen.getAllByText("batch-result-10.log").length).toBeGreaterThan(0);
  expect(
    screen.getByText(
      "배치 결과 조회 화면에서는 재실행·실패자료수정·로그삭제를 제공하지 않습니다.",
    ),
  ).toBeInTheDocument();
  expect(
    screen.getByText(
      "로그파일은 해당 실행ID에 연결된 파일만 조회하며 수정·삭제하지 않습니다.",
    ),
  ).toBeInTheDocument();

  await userEvent.click(
    screen.getByRole("button", { name: "로그파일 조회 전 확인" }),
  );

  await waitFor(() =>
    expect(fetchMock).toHaveBeenCalledWith(
      "/api/admin/batch-results/100/log",
      expect.objectContaining({ credentials: "include" }),
    ),
  );
  expect(
    await screen.findByText("로그파일 조회 감사 이력이 기록되었습니다."),
  ).toBeInTheDocument();
});

test("batch-result permission state is shown when API returns forbidden", async () => {
  window.history.pushState({}, "", "/admin/operations/batch-results");
  const fetchMock = vi
    .fn()
    .mockImplementationOnce(() => jsonResponse(sessionResponse))
    .mockImplementationOnce(() =>
      jsonResponse(
        {
          success: false,
          error: { code: "FORBIDDEN", message: "권한이 없습니다." },
        },
        403,
      ),
    );
  vi.stubGlobal("fetch", fetchMock);

  render(<App />);

  expect(await screen.findByText("PERMISSION DENIED")).toBeInTheDocument();
  expect(screen.getByText("권한이 없습니다.")).toBeInTheDocument();
});
