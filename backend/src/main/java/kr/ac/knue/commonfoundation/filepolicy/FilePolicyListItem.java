package kr.ac.knue.commonfoundation.filepolicy;

public record FilePolicyListItem(
    Long filePolicyId,
    String businessArea,
    String businessAreaName,
    String allowedExtensions,
    Integer maxFileSizeMb,
    Integer maxFileCount,
    Integer maxTotalSizeMb,
    Integer maxFilenameLength,
    Boolean malwareScanEnabled,
    Boolean enabled,
    String uploadValidationRule,
    String fileOperationBoundary
) {
}
