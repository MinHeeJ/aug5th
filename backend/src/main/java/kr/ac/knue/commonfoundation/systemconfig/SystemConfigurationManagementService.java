package kr.ac.knue.commonfoundation.systemconfig;

import java.math.BigDecimal;

import kr.ac.knue.commonfoundation.api.ApiException;
import kr.ac.knue.commonfoundation.auth.CurrentUserContext;
import kr.ac.knue.commonfoundation.auth.SessionPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SystemConfigurationManagementService {

    private final SystemConfigurationMapper systemConfigurationMapper;
    private final CurrentUserContext currentUserContext;

    public SystemConfigurationManagementService(SystemConfigurationMapper systemConfigurationMapper, CurrentUserContext currentUserContext) {
        this.systemConfigurationMapper = systemConfigurationMapper;
        this.currentUserContext = currentUserContext;
    }

    @Transactional(readOnly = true)
    public SystemConfigurationListResponse getSystemConfigurations(SystemConfigurationSearchCondition condition) {
        return new SystemConfigurationListResponse(
            systemConfigurationMapper.selectSystemConfigurations(condition),
            condition.page(),
            condition.size(),
            systemConfigurationMapper.countSystemConfigurations(condition),
            "SCR-SYSTEM-CONFIG",
            "R09"
        );
    }

    @Transactional
    public SaveSystemConfigurationResponse saveSystemConfiguration(SaveSystemConfigurationRequest request) {
        SessionPrincipal principal = currentUserContext.current()
            .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "인증이 필요합니다."));
        String configKey = request.configKey();
        if (!systemConfigurationMapper.existsSystemConfiguration(configKey)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "공통 환경설정을 찾을 수 없습니다.");
        }
        SystemConfigurationListItem before = systemConfigurationMapper.selectSystemConfiguration(configKey);
        validateValueRange(request.configValue(), before.valueRange());
        systemConfigurationMapper.updateSystemConfiguration(configKey, request.configValue().trim(), request.enabled());
        SystemConfigurationListItem saved = systemConfigurationMapper.selectSystemConfiguration(configKey);
        systemConfigurationMapper.insertAudit(
            "UPDATE",
            "system_configurations:" + configKey,
            principal.user().userId(),
            jsonAfterValue(saved, request.reason())
        );
        return new SaveSystemConfigurationResponse(
            saved.configKey(),
            saved.configValue(),
            saved.unit(),
            saved.valueRange(),
            saved.enabled(),
            "공통 환경설정 저장이 완료되었습니다."
        );
    }

    private static void validateValueRange(String configValue, String valueRange) {
        if (valueRange == null || valueRange.isBlank() || !valueRange.contains("-")) {
            return;
        }
        String[] bounds = valueRange.split("-", 2);
        try {
            BigDecimal value = new BigDecimal(configValue.trim());
            BigDecimal min = new BigDecimal(bounds[0].trim());
            BigDecimal max = new BigDecimal(bounds[1].trim());
            if (value.compareTo(min) < 0 || value.compareTo(max) > 0) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "설정값이 허용 범위를 벗어났습니다.");
            }
        } catch (NumberFormatException ignored) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "설정값은 숫자 범위 형식이어야 합니다.");
        }
    }

    private static String jsonAfterValue(SystemConfigurationListItem saved, String reason) {
        return "{\"configKey\":\"" + escapeJson(saved.configKey())
            + "\",\"configValue\":\"" + escapeJson(saved.configValue())
            + "\",\"unit\":\"" + escapeJson(saved.unit())
            + "\",\"valueRange\":" + nullableJson(saved.valueRange())
            + ",\"enabled\":" + saved.enabled()
            + ",\"reason\":\"" + escapeJson(reason) + "\"}";
    }

    private static String nullableJson(String value) {
        return value == null || value.isBlank() ? "null" : "\"" + escapeJson(value) + "\"";
    }

    private static String escapeJson(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
