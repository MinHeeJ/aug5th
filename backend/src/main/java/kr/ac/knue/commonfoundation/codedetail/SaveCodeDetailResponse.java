package kr.ac.knue.commonfoundation.codedetail;

public record SaveCodeDetailResponse(
    long codeDetailId,
    String groupId,
    String codeValue,
    String codeName,
    int displayOrder,
    boolean active,
    String message
) {
}
