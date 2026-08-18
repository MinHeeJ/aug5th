import type { ReactNode } from "react";

export interface OutOfScopeRouteDefinition {
  pathPrefix: string;
  title: string;
  reason: string;
}

export const outOfScopeRoutes: OutOfScopeRouteDefinition[] = [
  {
    pathPrefix: "/admin/professor-achievements",
    title: "교수업적평가 업무",
    reason:
      "교수업적 입력, 평가규칙, 평가대상자, 점수, 승인 흐름은 1차 범위에서 제외되었습니다.",
  },
  {
    pathPrefix: "/admin/academic-grants",
    title: "학술지원금 업무",
    reason:
      "학술지원금 신청, 상태, 승인, 업무보고서는 1차 범위에서 제외되었습니다.",
  },
  {
    pathPrefix: "/admin/files",
    title: "파일 관리",
    reason: "파일 업로드, 다운로드, 삭제 기능은 1차 범위에서 제외되었습니다.",
  },
  {
    pathPrefix: "/admin/excel",
    title: "Excel 처리",
    reason: "Excel 가져오기와 내보내기는 1차 범위에서 제외되었습니다.",
  },
  {
    pathPrefix: "/admin/personal-information",
    title: "개인정보 처리",
    reason: "개인정보 처리 기능은 1차 범위에서 제외되었습니다.",
  },
  {
    pathPrefix: "/admin/access-logs",
    title: "접속기록",
    reason: "접속기록 조회와 운영 기능은 1차 범위에서 제외되었습니다.",
  },
  {
    pathPrefix: "/admin/audit-logs",
    title: "감사로그",
    reason: "감사로그 화면과 API는 1차 범위에서 제외되었습니다.",
  },
  {
    pathPrefix: "/admin/batches",
    title: "배치 운영",
    reason:
      "배치 운영 기능은 외부 연계 없이 검증하는 1차 범위에 포함되지 않습니다.",
  },
];

export function isOutOfScopeRoute(pathname: string): boolean {
  return outOfScopeRoutes.some(
    (route) =>
      pathname === route.pathPrefix ||
      pathname.startsWith(`${route.pathPrefix}/`),
  );
}

export function renderOutOfScopeRoute(pathname: string): ReactNode {
  const route = outOfScopeRoutes.find(
    (item) =>
      pathname === item.pathPrefix ||
      pathname.startsWith(`${item.pathPrefix}/`),
  );
  return <OutOfScopePlaceholder route={route} pathname={pathname} />;
}

function OutOfScopePlaceholder({
  route,
  pathname,
}: {
  route?: OutOfScopeRouteDefinition;
  pathname: string;
}) {
  const title = route?.title ?? "범위 밖 메뉴";
  const reason =
    route?.reason ?? "이 route는 1차 시스템 관리 범위에 포함되지 않습니다.";

  return (
    <section
      className="content-card permission-state"
      aria-labelledby="out-of-scope-title"
    >
      <p className="breadcrumb">시스템 관리 &gt; 준비 중</p>
      <div className="status permission">준비 중</div>
      <h1 id="out-of-scope-title">{title}</h1>
      <p>{reason}</p>
      <p>
        요청 route <strong>{pathname}</strong> 는 비업무 placeholder로만
        표시되며 브라우저에서 범위 밖 API를 호출하지 않습니다.
      </p>
    </section>
  );
}
