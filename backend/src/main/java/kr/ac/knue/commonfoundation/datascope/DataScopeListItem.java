package kr.ac.knue.commonfoundation.datascope;

public record DataScopeListItem(
    Long dataScopeId,
    String roleCode,
    String roleName,
    String scopeType,
    String scopeName,
    String organizationCode,
    String organizationName,
    String businessArea,
    String businessAreaName,
    String enforcementRule,
    Integer displayOrder
) {
}
