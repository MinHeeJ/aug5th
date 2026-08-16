package kr.ac.knue.commonfoundation.user;

import jakarta.validation.Valid;
import kr.ac.knue.commonfoundation.api.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
public class UserManagementController {

    private final UserManagementService userManagementService;

    public UserManagementController(UserManagementService userManagementService) {
        this.userManagementService = userManagementService;
    }

    @GetMapping("/api/admin/users")
    public ApiResponse<UserListResponse> listUsers(
        @RequestParam(required = false) Integer page,
        @RequestParam(required = false) Integer size,
        @RequestParam(required = false) String sort,
        @RequestParam(required = false) String q,
        @RequestParam(required = false) String filter
    ) {
        return ApiResponse.ok(userManagementService.listUsers(UserSearchCondition.of(page, size, sort, q, filter)));
    }

    @PostMapping("/api/admin/users")
    public ApiResponse<SaveUserResponse> saveUser(@Valid @RequestBody SaveUserRequest request) {
        return ApiResponse.ok(userManagementService.saveUser(request));
    }
}
