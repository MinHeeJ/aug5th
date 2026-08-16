package kr.ac.knue.commonfoundation.baseyear;

public record BaseYearListItem(
    String baseYear,
    String defaultQueryYear,
    boolean copyBaselineEnabled,
    boolean resetEnabled,
    boolean enabled,
    String periodRule,
    String transitionRule
) {
}
