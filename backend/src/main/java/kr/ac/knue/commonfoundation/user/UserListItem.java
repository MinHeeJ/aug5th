package kr.ac.knue.commonfoundation.user;

import java.time.LocalDateTime;

public record UserListItem(
    String userId,
    Boolean enabled,
    String roleSummary,
    String status,
    String employeeNo,
    String name,
    String departmentCode,
    String departmentName,
    String rankName,
    String employmentStatus,
    String positionSummary,
    String retirementDate,
    LocalDateTime lastSyncedAt
) {
}
