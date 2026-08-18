package kr.ac.knue.cms.permissions;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import kr.ac.knue.cms.auth.AuthenticatedUser;
import kr.ac.knue.cms.common.api.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MenuPermissionService {
    private final MenuPermissionAdminMapper mapper;

    public MenuPermissionService(MenuPermissionAdminMapper mapper) {
        this.mapper = mapper;
    }

    public List<Map<String, Object>> list(String targetType, String targetId, String filter) {
        String safeTargetType = normalizeTargetType(targetType == null || targetType.isBlank() ? "ROLE" : targetType);
        String safeTargetId = targetId == null || targetId.isBlank() ? "R09" : targetId;
        validateTarget(safeTargetType, safeTargetId);
        return mapper.findMatrix(safeTargetType, safeTargetId, filter);
    }

    @Transactional
    public List<Map<String, Object>> save(String targetType, String targetId, MenuPermissionUpdateRequest request) {
        String safeTargetType = normalizeTargetType(targetType);
        validateTarget(safeTargetType, targetId);
        for (MenuPermissionItem item : request.permissions()) {
            if (item.targetType() != null && !safeTargetType.equals(item.targetType())) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "TARGET_TYPE_MISMATCH", "path targetType과 요청 targetType이 일치해야 합니다.",
                    Map.of("targetType", "선택한 권한 대상 구분을 확인하세요."));
            }
            if (item.targetId() != null && !item.targetId().isBlank() && !targetId.equals(item.targetId())) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "TARGET_ID_MISMATCH", "path targetId와 요청 targetId가 일치해야 합니다.",
                    Map.of("targetId", "선택한 권한 대상 식별자를 확인하세요."));
            }
            if (!mapper.menuExists(item.menuId())) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "MENU_NOT_FOUND", "대상 메뉴를 찾을 수 없습니다.",
                    Map.of("permissions", "정의된 메뉴 범위 안에서 선택하세요."));
            }
            String afterValue = "targetType=" + safeTargetType + ",targetId=" + targetId + ",menuId=" + item.menuId() + ",isAllowed=" + item.isAllowed();
            UUID permissionId = item.permissionId() == null ? UUID.randomUUID() : item.permissionId();
            mapper.upsertPermission(safeTargetType, targetId, item, permissionId, afterValue, request.changeReason());
        }
        return mapper.findMatrix(safeTargetType, targetId, null);
    }

    public List<Map<String, Object>> effective(AuthenticatedUser user) {
        return mapper.findEffectiveForRoles(user.roleCodes());
    }

    private String normalizeTargetType(String targetType) {
        if (targetType == null || !targetType.matches("ROLE|ORGANIZATION|USER")) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_TARGET_TYPE", "targetType은 ROLE, ORGANIZATION, USER 중 하나여야 합니다.",
                Map.of("targetType", "ROLE, ORGANIZATION, USER만 허용됩니다."));
        }
        return targetType;
    }

    private void validateTarget(String targetType, String targetId) {
        if (targetId == null || targetId.isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "TARGET_ID_REQUIRED", "권한 대상을 선택하세요.",
                Map.of("targetId", "대상 식별자를 입력하세요."));
        }
        boolean exists = switch (targetType) {
            case "ROLE" -> mapper.roleTargetExists(targetId);
            case "ORGANIZATION" -> mapper.organizationTargetExists(targetId);
            case "USER" -> mapper.userTargetExists(targetId);
            default -> false;
        };
        if (!exists) {
            throw new ApiException(HttpStatus.NOT_FOUND, "TARGET_NOT_FOUND", "권한 대상을 찾을 수 없습니다.",
                Map.of("targetId", "존재하는 대상 식별자를 입력하세요."));
        }
    }
}
