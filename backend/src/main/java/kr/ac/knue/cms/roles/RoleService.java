package kr.ac.knue.cms.roles;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import kr.ac.knue.cms.common.api.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RoleService {
    private final RoleMapper roleMapper;

    public RoleService(RoleMapper roleMapper) {
        this.roleMapper = roleMapper;
    }

    public List<Map<String, Object>> listRoles() {
        return roleMapper.findAll();
    }

    @Transactional
    public Map<String, Object> updateRole(String roleCode, RoleUpdateRequest request) {
        if (request.roleCode() != null && !request.roleCode().isBlank() && !roleCode.equals(request.roleCode())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "ROLE_CODE_IMMUTABLE", "역할코드는 변경할 수 없습니다.",
                Map.of("roleCode", "path roleCode와 요청 roleCode가 일치해야 합니다."));
        }
        if (!roleCode.matches("R0[1-9]")) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_ROLE_CODE", "R01~R09 역할코드만 저장할 수 있습니다.",
                Map.of("roleCode", "R01~R09 값만 허용됩니다."));
        }
        int updated = roleMapper.updateRole(roleCode, request);
        if (updated == 0) {
            throw new ApiException(HttpStatus.NOT_FOUND, "ROLE_NOT_FOUND", "대상 역할을 찾을 수 없습니다.",
                Map.of("roleCode", "존재하는 역할코드를 선택하세요."));
        }
        Map<String, Object> role = roleMapper.findByRoleCode(roleCode);
        if (role == null) {
            return new LinkedHashMap<>();
        }
        return role;
    }
}
