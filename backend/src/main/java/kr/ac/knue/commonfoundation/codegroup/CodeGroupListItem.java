package kr.ac.knue.commonfoundation.codegroup;

public record CodeGroupListItem(
    String groupId,
    String groupName,
    String description,
    String managingDepartment,
    boolean enabled,
    long detailCount,
    long enabledDetailCount,
    String detailManagementRule
) {
}
