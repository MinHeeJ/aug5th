package kr.ac.knue.commonfoundation.organization;

public record SaveOrganizationResponse(
    String organizationCode,
    Boolean enabled,
    String validTo,
    String message
) {
}
