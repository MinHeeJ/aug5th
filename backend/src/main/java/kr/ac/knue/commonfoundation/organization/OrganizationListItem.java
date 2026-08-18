package kr.ac.knue.commonfoundation.organization;

public record OrganizationListItem(
    String organizationCode,
    String organizationName,
    String parentOrganizationCode,
    String parentOrganizationName,
    String validFrom,
    String validTo,
    Boolean enabled,
    long childCount,
    long assignedUserCount
) {
}
