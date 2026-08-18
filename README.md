# CMS 공통기능 1차 관리자 애플리케이션

한국교원대학교 교수업적평가시스템의 1차 공통관리 범위를 구현한 Spring Boot + React + PostgreSQL 애플리케이션입니다.

## 구성

- Backend: Java 17, Spring Boot 3.3.x, Maven, MyBatis, PostgreSQL 16, executable boot jar
- Frontend: React 18, TypeScript, Vite 5, nginx 정적 서빙 및 `/api/*` reverse proxy
- Runtime: Docker Compose (`infra/docker-compose.yml`)
- Health endpoint: `GET /api/health`

## 로컬 실행

```bash
docker compose -f infra/docker-compose.yml up -d --build
```

서비스 포트:

- Frontend: http://127.0.0.1:3000
- Backend API: http://127.0.0.1:8080
- Backend health: http://127.0.0.1:8080/api/health
- Frontend nginx health: http://127.0.0.1:3000/healthz

PostgreSQL은 Compose 내부 네트워크에서만 사용하며 host port를 공개하지 않습니다.

## 시드 로그인 계정

- 사용자 ID: `admin`
- 비밀번호: `admin`
- 역할: `R09 시스템관리자`

Compose 실행 직후 Flyway migration과 seed data가 적용되면 위 계정으로 로그인할 수 있습니다. 실제 KORUS, SSO, 외부기관 API는 호출하지 않으며 내부 계정 adapter와 KORUS Mock snapshot을 사용합니다.

## 주요 화면 검증

로그인 후 다음 9개 시스템 관리 화면 접근을 확인합니다.

1. 사용자 관리: `/admin/users`
2. 조직 관리: `/admin/organizations`
3. 역할 관리: `/admin/roles`
4. 사용자 역할 관리: `/admin/user-roles`
5. 메뉴 권한 관리: `/admin/menu-permissions`
6. 메뉴 구조 관리: `/admin/menu-structure`
7. 메뉴 정보 관리: `/admin/menus`
8. 코드그룹 관리: `/admin/code-groups`
9. 상세코드 관리: `/admin/code-groups/COMMON/codes`

Expected results:

- `/api/health`가 `success: true` envelope로 응답합니다.
- `admin/admin` 로그인 응답에 `SESSION` HttpOnly cookie와 `R09` role code가 포함됩니다.
- 위 9개 화면의 대표 조회 API가 인증된 관리자 세션으로 2xx와 `success: true` envelope를 반환합니다.
- 인증 없이 보호 API를 호출하면 401 error envelope를 반환합니다.
- R09가 아닌 인증 세션으로 관리자 API를 호출하면 403 error envelope를 반환합니다.
- Frontend는 `/api/...` 상대경로만 사용하며 nginx가 backend 서비스로 프록시합니다.

## 자동 로컬 검증

Phase 9 검증 스크립트:

```bash
scripts/validate-local.sh
```

검증 항목:

- `docker compose -f infra/docker-compose.yml up -d --build`
- backend `/api/health`
- frontend `/healthz` 및 nginx `/api/health` proxy
- `admin/admin` login
- 9개 관리 조회 API 2xx + envelope
- 인증 없음 401
- 시드 `faculty/faculty` 계정이 존재할 경우 비관리자 403
- 10개 frontend route(`/login` + 9개 관리자 화면)가 SPA shell을 반환하는지 확인

Playwright까지 함께 실행하려면 의존성이 준비된 환경에서 다음처럼 실행합니다.

```bash
RUN_PLAYWRIGHT=1 scripts/validate-local.sh
```

## 테스트 파일

- Backend smoke: `backend/src/test/java/kr/ac/knue/cms/integration/AllAdminReadApisSmokeTest.java`
- Frontend E2E: `frontend/tests/e2e/all-admin-screens.spec.ts`
- Local validation: `scripts/validate-local.sh`

## 종료

```bash
docker compose -f infra/docker-compose.yml down
```

데이터 볼륨까지 삭제하려면 다음을 사용합니다.

```bash
docker compose -f infra/docker-compose.yml down -v
```
