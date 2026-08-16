package kr.ac.knue.commonfoundation.codedetail;

public record CodeDetailListItem(
    long codeDetailId,
    String groupId,
    String groupName,
    String codeValue,
    String codeName,
    String parentCodeValue,
    String parentCodeName,
    int displayOrder,
    boolean active,
    String detailUsageRule
) {
}
