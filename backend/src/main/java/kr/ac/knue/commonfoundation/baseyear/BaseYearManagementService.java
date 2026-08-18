package kr.ac.knue.commonfoundation.baseyear;

import java.util.Map;

import kr.ac.knue.commonfoundation.api.ApiException;
import kr.ac.knue.commonfoundation.auth.CurrentUserContext;
import kr.ac.knue.commonfoundation.auth.SessionPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BaseYearManagementService {

    private final BaseYearMapper baseYearMapper;
    private final CurrentUserContext currentUserContext;

    public BaseYearManagementService(BaseYearMapper baseYearMapper, CurrentUserContext currentUserContext) {
        this.baseYearMapper = baseYearMapper;
        this.currentUserContext = currentUserContext;
    }

    @Transactional(readOnly = true)
    public BaseYearListResponse getBaseYears(BaseYearSearchCondition condition) {
        return new BaseYearListResponse(
            baseYearMapper.selectBaseYears(condition),
            condition.page(),
            condition.size(),
            baseYearMapper.countBaseYears(condition),
            "SCR-BASE-YEAR",
            "R09"
        );
    }

    @Transactional
    public SaveBaseYearResponse saveBaseYear(SaveBaseYearRequest request) {
        SessionPrincipal principal = currentUserContext.current()
            .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "인증이 필요합니다."));
        String baseYear = request.baseYear();
        if (!baseYearMapper.existsBaseYear(baseYear)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "기준연도를 찾을 수 없습니다.");
        }
        BaseYearListItem before = baseYearMapper.selectBaseYear(baseYear);
        validateYearPolicy(baseYear, request.defaultQueryYear(), request.copyBaselineEnabled(), request.resetEnabled());
        baseYearMapper.updateBaseYear(
            baseYear,
            request.defaultQueryYear().trim(),
            request.copyBaselineEnabled(),
            request.resetEnabled(),
            request.enabled()
        );
        BaseYearListItem saved = baseYearMapper.selectBaseYear(baseYear);
        baseYearMapper.insertAudit(
            "UPDATE",
            "base_years:" + baseYear,
            principal.user().userId(),
            jsonValue(before, null),
            jsonValue(saved, request.reason())
        );
        return new SaveBaseYearResponse(
            saved.baseYear(),
            saved.defaultQueryYear(),
            saved.copyBaselineEnabled(),
            saved.resetEnabled(),
            saved.enabled(),
            "기준연도 관리 저장이 완료되었습니다."
        );
    }

    private static void validateYearPolicy(String baseYear, String defaultQueryYear, boolean copyBaselineEnabled, boolean resetEnabled) {
        int base = Integer.parseInt(baseYear);
        int query = Integer.parseInt(defaultQueryYear.trim());
        if (query > base) {
            throw new ApiException(
                HttpStatus.BAD_REQUEST,
                "VALIDATION_ERROR",
                "기본 조회연도는 기준연도보다 클 수 없습니다.",
                Map.of("defaultQueryYear", "기본 조회연도는 기준연도보다 클 수 없습니다.")
            );
        }
        if (base - query > 1) {
            throw new ApiException(
                HttpStatus.BAD_REQUEST,
                "VALIDATION_ERROR",
                "기본 조회연도는 기준연도 또는 직전연도만 허용됩니다.",
                Map.of("defaultQueryYear", "기본 조회연도는 기준연도 또는 직전연도만 허용됩니다.")
            );
        }
        if (resetEnabled && !copyBaselineEnabled) {
            throw new ApiException(
                HttpStatus.BAD_REQUEST,
                "VALIDATION_ERROR",
                "초기화는 기준정보 복사 허용 상태에서만 활성화할 수 있습니다.",
                Map.of("resetEnabled", "초기화는 기준정보 복사 허용 상태에서만 활성화할 수 있습니다.")
            );
        }
    }

    private static String jsonValue(BaseYearListItem item, String reason) {
        return "{\"baseYear\":\"" + escapeJson(item.baseYear())
            + "\",\"defaultQueryYear\":\"" + escapeJson(item.defaultQueryYear())
            + "\",\"copyBaselineEnabled\":" + item.copyBaselineEnabled()
            + ",\"resetEnabled\":" + item.resetEnabled()
            + ",\"enabled\":" + item.enabled()
            + (reason == null ? "" : ",\"reason\":\"" + escapeJson(reason) + "\"")
            + "}";
    }

    private static String escapeJson(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
