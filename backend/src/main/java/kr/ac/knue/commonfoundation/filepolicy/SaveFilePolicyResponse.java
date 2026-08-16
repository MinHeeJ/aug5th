package kr.ac.knue.commonfoundation.filepolicy;

public record SaveFilePolicyResponse(
    Long filePolicyId,
    String businessArea,
    String allowedExtensions,
    Integer maxFileSizeMb,
    Integer maxFileCount,
    Integer maxTotalSizeMb,
    Integer maxFilenameLength,
    Boolean malwareScanEnabled,
    Boolean enabled,
    String message
) {
}
