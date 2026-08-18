package kr.ac.knue.cms.organizations;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.UUID;
import kr.ac.knue.cms.auth.SessionService;
import kr.ac.knue.cms.common.api.ApiException;
import kr.ac.knue.cms.common.api.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OrganizationRelationshipController {
    private final OrganizationService organizationService;
    private final SessionService sessionService;

    public OrganizationRelationshipController(OrganizationService organizationService, SessionService sessionService) {
        this.organizationService = organizationService;
        this.sessionService = sessionService;
    }

    @PutMapping("/api/admin/organization-relationships/{relationshipId}")
    public ApiResponse<OrganizationRelationshipDto> saveRelationship(@PathVariable UUID relationshipId,
                                                                     @Valid @RequestBody OrganizationRelationshipRequest request,
                                                                     HttpServletRequest httpRequest) {
        return ApiResponse.ok(organizationService.saveRelationship(relationshipId, request, sessionService.findByRequest(httpRequest)
            .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "AUTHENTICATION_REQUIRED", "인증 세션이 필요합니다."))));
    }
}
