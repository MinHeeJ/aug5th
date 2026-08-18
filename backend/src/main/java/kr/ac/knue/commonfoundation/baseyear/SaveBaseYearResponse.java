package kr.ac.knue.commonfoundation.baseyear;

public record SaveBaseYearResponse(
    String baseYear,
    String defaultQueryYear,
    boolean copyBaselineEnabled,
    boolean resetEnabled,
    boolean enabled,
    String message
) {
}
