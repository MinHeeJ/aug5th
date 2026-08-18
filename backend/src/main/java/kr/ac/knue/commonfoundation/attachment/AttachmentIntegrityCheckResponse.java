package kr.ac.knue.commonfoundation.attachment;

public record AttachmentIntegrityCheckResponse(
    long totalCount,
    long abnormalCount,
    String status,
    String message
) {
}
