#!/usr/bin/env bash
set -Eeuo pipefail

COMPOSE_FILE="${COMPOSE_FILE:-infra/docker-compose.yml}"
BACKEND_BASE_URL="${BACKEND_BASE_URL:-http://127.0.0.1:8080}"
FRONTEND_BASE_URL="${FRONTEND_BASE_URL:-http://127.0.0.1:3000}"
ADMIN_USERNAME="${ADMIN_USERNAME:-admin}"
ADMIN_PASSWORD="${ADMIN_PASSWORD:-admin}"
FACULTY_USERNAME="${FACULTY_USERNAME:-faculty}"
FACULTY_PASSWORD="${FACULTY_PASSWORD:-faculty}"
TIMEOUT_SECONDS="${TIMEOUT_SECONDS:-180}"
COOKIE_JAR="$(mktemp)"

cleanup() {
  rm -f "${COOKIE_JAR}"
}
trap cleanup EXIT

log() {
  printf '[validate-local] %s\n' "$*"
}

fail() {
  printf '[validate-local] ERROR: %s\n' "$*" >&2
  exit 1
}

curl_json() {
  curl --silent --show-error --fail-with-body "$@"
}

wait_for_url() {
  local url="$1"
  local label="$2"
  local deadline=$((SECONDS + TIMEOUT_SECONDS))
  until curl --silent --show-error --fail "${url}" >/dev/null 2>&1; do
    if (( SECONDS >= deadline )); then
      fail "${label} did not become ready within ${TIMEOUT_SECONDS}s (${url})"
    fi
    sleep 2
  done
}

assert_json_success() {
  local body="$1"
  local label="$2"
  python3 -c '
import json
import sys
label = sys.argv[1]
payload = json.load(sys.stdin)
if payload.get("success") is not True:
    raise SystemExit(f"{label}: expected success=true, got {payload}")
if "meta" not in payload:
    raise SystemExit(f"{label}: expected meta envelope")
' "$label" <<<"${body}"
}

assert_status() {
  local expected="$1"
  local label="$2"
  shift 2
  local status
  status=$(curl --silent --output /tmp/validate-local-response.$$ --write-out '%{http_code}' "$@")
  rm -f /tmp/validate-local-response.$$
  if [[ "${status}" != "${expected}" ]]; then
    fail "${label}: expected HTTP ${expected}, got ${status}"
  fi
}

log "starting docker compose stack from ${COMPOSE_FILE}"
docker compose -f "${COMPOSE_FILE}" up -d --build

log "waiting for backend health"
wait_for_url "${BACKEND_BASE_URL}/api/health" "backend /api/health"
health_body=$(curl_json "${BACKEND_BASE_URL}/api/health")
assert_json_success "${health_body}" "/api/health"

log "waiting for frontend health"
wait_for_url "${FRONTEND_BASE_URL}/healthz" "frontend /healthz"
wait_for_url "${FRONTEND_BASE_URL}/api/health" "frontend nginx /api/health proxy"

log "checking unauthenticated protected API returns 401"
assert_status 401 "GET /api/admin/users without session" "${BACKEND_BASE_URL}/api/admin/users"

log "logging in as seed administrator"
login_body=$(curl_json -c "${COOKIE_JAR}" -H 'Content-Type: application/json' -X POST \
  -d "{\"username\":\"${ADMIN_USERNAME}\",\"password\":\"${ADMIN_PASSWORD}\"}" \
  "${BACKEND_BASE_URL}/api/auth/login")
assert_json_success "${login_body}" "admin login"

admin_read_paths=(
  "/api/admin/users"
  "/api/admin/organizations"
  "/api/admin/roles"
  "/api/admin/user-roles"
  "/api/admin/menu-permissions?targetType=ROLE&targetId=R09"
  "/api/admin/menu-structure"
  "/api/admin/menus"
  "/api/admin/code-groups"
  "/api/admin/code-groups/COMMON/codes"
)

for path in "${admin_read_paths[@]}"; do
  log "checking admin read API 2xx: ${path}"
  body=$(curl_json -b "${COOKIE_JAR}" "${BACKEND_BASE_URL}${path}")
  assert_json_success "${body}" "${path}"
done

log "checking navigation exposes 9 managed screen routes"
nav_body=$(curl_json -b "${COOKIE_JAR}" "${BACKEND_BASE_URL}/api/navigation/menus")
assert_json_success "${nav_body}" "/api/navigation/menus"
python3 -c '
import json
import sys
payload = json.load(sys.stdin)
required = {
    "/admin/users", "/admin/organizations", "/admin/roles", "/admin/user-roles",
    "/admin/menu-permissions", "/admin/menu-structure", "/admin/menus",
    "/admin/code-groups", "/admin/code-groups/COMMON/codes",
}
seen = set()
def walk(items):
    for item in items:
        if item.get("url"):
            seen.add(item["url"])
        walk(item.get("children") or [])
walk(payload.get("data") or [])
missing = sorted(required - seen)
if missing:
    raise SystemExit(f"missing navigation routes: {missing}")
' <<<"${nav_body}"

log "checking authenticated non-admin request returns 403 when seeded faculty account exists"
faculty_cookie="$(mktemp)"
trap 'rm -f "${COOKIE_JAR}" "${faculty_cookie}"' EXIT
if curl --silent --show-error --fail-with-body -c "${faculty_cookie}" -H 'Content-Type: application/json' -X POST \
  -d "{\"username\":\"${FACULTY_USERNAME}\",\"password\":\"${FACULTY_PASSWORD}\"}" \
  "${BACKEND_BASE_URL}/api/auth/login" >/dev/null; then
  assert_status 403 "GET /api/admin/users with non-R09 session" -b "${faculty_cookie}" "${BACKEND_BASE_URL}/api/admin/users"
else
  log "faculty/faculty login is unavailable; 403 check skipped after 401 check passed"
fi
rm -f "${faculty_cookie}"

log "checking frontend admin routes return SPA document"
frontend_routes=(
  "/login"
  "/admin/users"
  "/admin/organizations"
  "/admin/roles"
  "/admin/user-roles"
  "/admin/menu-permissions"
  "/admin/menu-structure"
  "/admin/menus"
  "/admin/code-groups"
  "/admin/code-groups/COMMON/codes"
)
for route in "${frontend_routes[@]}"; do
  curl --silent --show-error --fail "${FRONTEND_BASE_URL}${route}" | grep -q '<div id="root"></div>' \
    || fail "frontend route did not return SPA shell: ${route}"
done

if [[ "${RUN_PLAYWRIGHT:-0}" == "1" ]]; then
  log "running Playwright E2E because RUN_PLAYWRIGHT=1"
  (cd frontend && npm run test:e2e)
else
  log "Playwright E2E skipped by default; set RUN_PLAYWRIGHT=1 to run frontend/tests/e2e/*.spec.ts"
fi

log "local validation completed successfully"
