# aug5th

한국교원대학교 교수업적평가시스템 공통기능 생성 프로젝트입니다.

## Phase 1 산출물

- `backend/`: Java 17, Spring Boot 3.3.x, Maven, MyBatis 기반 executable boot jar 프로젝트
- `frontend/`: React 18, TypeScript, Vite 5, Tailwind CSS 3 기반 SPA 프로젝트
- `infra/docker-compose.yml`: PostgreSQL 16, backend, frontend 서비스 및 nginx `/api/*` 프록시 구성

## 로컬 실행

```bash
docker compose -f infra/docker-compose.yml up --build
```

브라우저: http://localhost:3000
헬스체크: http://localhost:3000/api/health

## 시드 계정

- 관리자 계정: `admin`
- 초기 비밀번호: `admin`
- 부여 역할: `R09 시스템관리자`

## Phase 2 산출물

- Flyway foundation schema: data-model Entity Registry 30개 테이블 생성
- Flyway seed: R01~R09 역할, 4개 대메뉴, 25개 화면 메뉴, 메뉴 권한, 기능 권한, 데이터 범위 권한
- 공통 `ApiResponse`/`ApiError` envelope 및 예외 처리
- 세션 쿠키 기반 인증, R09 관리자 API 권한 차단, 데이터 범위 조회 기반

## 주의

Phase 2는 공통 기반 구현 범위입니다. 25개 vertical slice의 개별 CRUD API와 화면 구현은 후속 phase에서 진행됩니다.
