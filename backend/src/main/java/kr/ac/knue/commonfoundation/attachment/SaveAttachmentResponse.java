package kr.ac.knue.commonfoundation.attachment;

public record SaveAttachmentResponse(
    Long attachmentId,
    String businessKey,
    String originalName,
    boolean deleted,
    String actionResult,
    String message
) {
}
