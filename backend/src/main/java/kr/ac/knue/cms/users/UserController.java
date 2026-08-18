package kr.ac.knue.cms.users;

import java.util.List;
import kr.ac.knue.cms.common.api.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/api/admin/users")
    public ApiResponse<List<UserSummary>> listUsers(
        @RequestParam(required = false) String filter,
        @RequestParam(required = false) String staffId,
        @RequestParam(required = false) String staffName,
        @RequestParam(required = false) String organizationCode,
        @RequestParam(required = false) String rankTitle,
        @RequestParam(required = false) String employmentStatus,
        @RequestParam(required = false) String roleCode,
        @RequestParam(required = false) Boolean systemEnabled,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        return ApiResponse.ok(userService.listUsers(filter, staffId, staffName, organizationCode, rankTitle,
            employmentStatus, roleCode, systemEnabled, page, size));
    }
}
