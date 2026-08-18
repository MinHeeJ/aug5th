package kr.ac.knue.commonfoundation.datascope;

public record SaveDataScopeResponse(
    Long dataScopeId,
    String roleCode,
    String scopeType,
    String organizationCode,
    String businessArea,
    String message
) {
}
