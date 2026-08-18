package kr.ac.knue.commonfoundation.systemconfig;

public record SaveSystemConfigurationResponse(
    String configKey,
    String configValue,
    String unit,
    String valueRange,
    boolean enabled,
    String message
) {
}
