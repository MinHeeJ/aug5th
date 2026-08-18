package kr.ac.knue.commonfoundation.systemconfig;

public record SystemConfigurationListItem(
    String configKey,
    String configValue,
    String unit,
    String valueRange,
    boolean enabled,
    String applyScope,
    String validationRule
) {
}
