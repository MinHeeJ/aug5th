package kr.ac.knue.cms.menus;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import kr.ac.knue.cms.common.api.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MenuAdminService {
    private final MenuAdminMapper mapper;

    public MenuAdminService(MenuAdminMapper mapper) {
        this.mapper = mapper;
    }

    public List<Map<String, Object>> listMenus() {
        return mapper.findAllMenus();
    }

    @Transactional
    public Map<String, Object> saveStructure(UUID menuId, MenuRequest request) {
        validatePathMenu(menuId, request.menuId());
        if (request.parentMenuId() != null) {
            if (menuId.equals(request.parentMenuId())) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_PARENT_MENU", "자기 자신을 부모 메뉴로 지정할 수 없습니다.",
                    Map.of("parentMenuId", "다른 메뉴를 선택하세요."));
            }
            if (!mapper.parentExists(request.parentMenuId())) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "PARENT_MENU_NOT_FOUND", "부모 메뉴를 찾을 수 없습니다.",
                    Map.of("parentMenuId", "정의된 메뉴 범위 안에서 선택하세요."));
            }
        }
        int updated = mapper.updateStructure(menuId, request, structureValue(menuId, request));
        if (updated == 0) {
            throw new ApiException(HttpStatus.NOT_FOUND, "MENU_NOT_FOUND", "대상 메뉴를 찾을 수 없습니다.", Map.of("menuId", "존재하는 메뉴를 선택하세요."));
        }
        return mapper.findMenu(menuId);
    }

    @Transactional
    public List<Map<String, Object>> reorder(MenuReorderRequest request) {
        if (new HashSet<>(request.orderedMenuIds()).size() != request.orderedMenuIds().size()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "DUPLICATE_MENU_ORDER", "재정렬 메뉴가 중복되었습니다.",
                Map.of("orderedMenuIds", "중복 없는 menuId를 전달하세요."));
        }
        String idsSql = request.orderedMenuIds().stream().map(id -> "'" + id + "'::uuid").reduce((a, b) -> a + "," + b).orElse("null");
        int siblingCount = mapper.countSiblingsInParent(request.parentMenuId(), idsSql);
        if (siblingCount != request.orderedMenuIds().size()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "CROSS_PARENT_REORDER", "동일 계층 내 메뉴만 재정렬할 수 있습니다.",
                Map.of("orderedMenuIds", "같은 parentMenuId의 menuId만 선택하세요."));
        }
        int order = 1;
        for (UUID orderedMenuId : request.orderedMenuIds()) {
            mapper.updateDisplayOrder(orderedMenuId, order, "menuId=" + orderedMenuId + ",displayOrder=" + order, "동일 계층 재정렬");
            order++;
        }
        return mapper.findAllMenus();
    }

    @Transactional
    public Map<String, Object> saveInformation(UUID menuId, MenuRequest request) {
        validatePathMenu(menuId, request.menuId());
        int updated = mapper.updateInformation(menuId, request, informationValue(menuId, request));
        if (updated == 0) {
            throw new ApiException(HttpStatus.NOT_FOUND, "MENU_NOT_FOUND", "대상 메뉴를 찾을 수 없습니다.", Map.of("menuId", "존재하는 메뉴를 선택하세요."));
        }
        return mapper.findMenu(menuId);
    }

    @Transactional
    public Map<String, Object> updateStatus(UUID menuId, MenuStatusRequest request) {
        int updated = mapper.updateStatus(menuId, request, "menuId=" + menuId + ",isUsed=" + request.isUsed());
        if (updated == 0) {
            throw new ApiException(HttpStatus.NOT_FOUND, "MENU_NOT_FOUND", "대상 메뉴를 찾을 수 없습니다.", Map.of("menuId", "존재하는 메뉴를 선택하세요."));
        }
        return mapper.findMenu(menuId);
    }

    private void validatePathMenu(UUID pathMenuId, UUID payloadMenuId) {
        if (payloadMenuId != null && !pathMenuId.equals(payloadMenuId)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "MENU_ID_MISMATCH", "path menuId와 요청 menuId가 일치해야 합니다.",
                Map.of("menuId", "선택 메뉴 식별자를 확인하세요."));
        }
    }

    private String structureValue(UUID menuId, MenuRequest request) {
        return "menuId=" + menuId + ",parentMenuId=" + request.parentMenuId() + ",displayOrder=" + request.displayOrder();
    }

    private String informationValue(UUID menuId, MenuRequest request) {
        return "menuId=" + menuId + ",menuName=" + request.menuName() + ",screenId=" + request.screenId() + ",url=" + request.url()
            + ",icon=" + request.icon() + ",businessDivision=" + request.businessDivision();
    }
}
