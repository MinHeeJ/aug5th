package kr.ac.knue.cms.menus;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class NavigationService {
    private final NavigationMapper navigationMapper;

    public NavigationService(NavigationMapper navigationMapper) {
        this.navigationMapper = navigationMapper;
    }

    public List<NavigationMenu> listVisibleMenus(List<String> roleCodes) {
        if (roleCodes == null || roleCodes.isEmpty()) {
            return List.of();
        }
        List<NavigationMenuRow> rows = navigationMapper.findVisibleMenus(roleCodes);
        Map<UUID, MutableNavigationMenu> byId = new LinkedHashMap<>();
        for (NavigationMenuRow row : rows) {
            byId.put(row.getMenuId(), new MutableNavigationMenu(row));
        }
        List<MutableNavigationMenu> roots = new ArrayList<>();
        for (MutableNavigationMenu menu : byId.values()) {
            if (menu.row.getParentMenuId() == null) {
                roots.add(menu);
                continue;
            }
            MutableNavigationMenu parent = byId.get(menu.row.getParentMenuId());
            if (parent != null) {
                parent.children.add(menu);
            }
        }
        return roots.stream().map(MutableNavigationMenu::toImmutable).toList();
    }

    private static final class MutableNavigationMenu {
        private final NavigationMenuRow row;
        private final List<MutableNavigationMenu> children = new ArrayList<>();

        private MutableNavigationMenu(NavigationMenuRow row) {
            this.row = row;
        }

        private NavigationMenu toImmutable() {
            return new NavigationMenu(
                row.getMenuId(),
                row.getParentMenuId(),
                row.getMenuLevel(),
                row.getDisplayOrder(),
                row.getMenuName(),
                row.getScreenId(),
                row.getUrl(),
                row.getIcon(),
                row.getBusinessDivision(),
                children.stream().map(MutableNavigationMenu::toImmutable).toList()
            );
        }
    }
}
