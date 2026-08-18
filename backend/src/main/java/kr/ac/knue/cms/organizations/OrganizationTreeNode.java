package kr.ac.knue.cms.organizations;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public record OrganizationTreeNode(
    UUID organizationId,
    String organizationCode,
    String organizationName,
    String organizationType,
    boolean isUsed,
    UUID relationshipId,
    UUID parentOrganizationId,
    String effectiveStartDate,
    String effectiveEndDate,
    List<OrganizationTreeNode> children
) {
    public OrganizationTreeNode withChildren(List<OrganizationTreeNode> newChildren) {
        return new OrganizationTreeNode(organizationId, organizationCode, organizationName, organizationType, isUsed,
            relationshipId, parentOrganizationId, effectiveStartDate, effectiveEndDate, newChildren);
    }

    public static List<OrganizationTreeNode> mutableChildren() {
        return new ArrayList<>();
    }
}
