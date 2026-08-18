package kr.ac.knue.cms.organizations;

import java.util.UUID;

public record OrganizationDto(
    UUID organizationId,
    String organizationCode,
    String organizationName,
    String organizationType,
    boolean isUsed
) {
}
