package kr.ac.knue.commonfoundation.codegroup;

public record SaveCodeGroupResponse(
    String groupId,
    String groupName,
    String managingDepartment,
    boolean enabled,
    String message
) {
}
