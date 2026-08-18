package kr.ac.knue.commonfoundation.organization;

import kr.ac.knue.commonfoundation.api.ApiException;
import kr.ac.knue.commonfoundation.auth.CurrentUserContext;
import kr.ac.knue.commonfoundation.auth.SessionPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrganizationManagementService {

    private final OrganizationMapper organizationMapper;
    private final CurrentUserContext currentUserContext;

    public OrganizationManagementService(OrganizationMapper organizationMapper, CurrentUserContext currentUserContext) {
        this.organizationMapper = organizationMapper;
        this.currentUserContext = currentUserContext;
    }

    @Transactional(readOnly = true)
    public OrganizationListResponse listOrganizations(OrganizationSearchCondition condition) {
        return new OrganizationListResponse(
            organizationMapper.selectOrganizations(condition),
            condition.page(),
            condition.size(),
            organizationMapper.countOrganizations(condition),
            "SCR-ORG-MGMT",
            "R09"
        );
    }

    @Transactional
    public SaveOrganizationResponse saveOrganization(SaveOrganizationRequest request) {
        SessionPrincipal principal = currentUserContext.current()
            .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "인증이 필요합니다."));
        if (!organizationMapper.existsOrganization(request.id())) {
            throw new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "조직을 찾을 수 없습니다.");
        }
        organizationMapper.updateOrganizationManagementFields(request.id(), request.enabled(), request.validTo());
        organizationMapper.insertAudit(
            "organizations:" + request.id(),
            principal.user().userId(),
            jsonAfterValue(request)
        );
        return new SaveOrganizationResponse(
            request.id(),
            request.enabled(),
            request.validTo() == null ? null : request.validTo().toString(),
            "조직 관리 저장이 완료되었습니다."
        );
    }

    private static String jsonAfterValue(SaveOrganizationRequest request) {
        return "{\"enabled\":" + request.enabled()
            + ",\"validTo\":" + nullableJson(request.validTo() == null ? null : request.validTo().toString())
            + ",\"reason\":\"" + escapeJson(request.reason()) + "\"}";
    }

    private static String nullableJson(String value) {
        return value == null ? "null" : "\"" + escapeJson(value) + "\"";
    }

    private static String escapeJson(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

}
