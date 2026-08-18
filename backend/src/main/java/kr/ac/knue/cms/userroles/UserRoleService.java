package kr.ac.knue.cms.userroles;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import kr.ac.knue.cms.auth.AuthenticatedUser;
import kr.ac.knue.cms.common.api.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserRoleService {
    private final UserRoleMapper userRoleMapper;

    public UserRoleService(UserRoleMapper userRoleMapper) {
        this.userRoleMapper = userRoleMapper;
    }

    public List<Map<String, Object>> listUserRoles(UUID userId, String roleCode, String filter) {
        return userRoleMapper.findUserRoles(userId, roleCode, filter);
    }

    @Transactional
    public List<Map<String, Object>> saveUserRoles(UUID userId, UserRolesUpdateRequest request, AuthenticatedUser actor) {
        if (!userRoleMapper.userExists(userId)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "대상 사용자를 찾을 수 없습니다.",
                Map.of("userId", "존재하는 사용자를 선택하세요."));
        }
        for (UserRoleRequest role : request.roles()) {
            validateRole(userId, role);
            UUID approverId = role.approvedByUserId() == null ? actor.userId() : role.approvedByUserId();
            String afterValue = afterValue(userId, role, approverId);
            String changeReason = Boolean.FALSE.equals(role.isUsed()) ? "역할 회수" : "역할 저장";
            if (Boolean.FALSE.equals(role.isUsed()) || role.revokedAt() != null) {
                int revoked = userRoleMapper.revokeCurrentRole(userId, role.roleCode(), afterValue, changeReason);
                if (revoked == 0) {
                    userRoleMapper.insertRevokedMarker(userId, role, approverId, afterValue, changeReason);
                }
            } else {
                UUID currentUserRoleId = userRoleMapper.findCurrentUserRoleId(userId, role.roleCode());
                if (currentUserRoleId == null) {
                    userRoleMapper.insertActiveRole(userId, role, approverId, afterValue, changeReason);
                } else {
                    userRoleMapper.updateActiveRole(currentUserRoleId, role, approverId, afterValue, changeReason);
                }
            }
        }
        return userRoleMapper.findUserRoles(userId, null, null);
    }

    private void validateRole(UUID pathUserId, UserRoleRequest role) {
        if (role.userId() != null && !pathUserId.equals(role.userId())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "USER_ID_MISMATCH", "path userId와 요청 userId가 일치해야 합니다.",
                Map.of("userId", "선택 사용자 식별자를 확인하세요."));
        }
        if (!userRoleMapper.activeRoleExists(role.roleCode())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "ROLE_NOT_FOUND", "사용 가능한 역할을 찾을 수 없습니다.",
                Map.of("roleCode", "R01~R09 중 사용 중인 역할을 선택하세요."));
        }
        LocalDate validFrom = role.validFrom();
        LocalDate validTo = role.validTo();
        if (validFrom != null && validTo != null && validFrom.isAfter(validTo)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_VALID_PERIOD", "유효 시작일은 종료일보다 늦을 수 없습니다.",
                Map.of("validTo", "종료일은 시작일 이후여야 합니다."));
        }
        if (role.approvedByUserId() != null && !userRoleMapper.userExists(role.approvedByUserId())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "APPROVER_NOT_FOUND", "승인자를 찾을 수 없습니다.",
                Map.of("approvedByUserId", "존재하는 승인자 식별자를 입력하세요."));
        }
    }

    private String afterValue(UUID userId, UserRoleRequest role, UUID approverId) {
        return "userId=" + userId + ",roleCode=" + role.roleCode() + ",assignmentType=" + role.assignmentType()
            + ",validFrom=" + role.validFrom() + ",validTo=" + role.validTo() + ",approvedByUserId=" + approverId
            + ",isUsed=" + role.isUsed();
    }
}
