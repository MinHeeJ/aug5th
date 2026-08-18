package kr.ac.knue.cms.userroles;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record UserRoleRequest(
    UUID userRoleId,
    UUID userId,
    @NotBlank(message = "역할코드를 입력하세요.") @Pattern(regexp = "R0[1-9]", message = "R01~R09 값만 허용됩니다.") String roleCode,
    @NotBlank(message = "역할 구분을 입력하세요.") @Pattern(regexp = "POSITION|MANUAL", message = "POSITION 또는 MANUAL만 허용됩니다.") String assignmentType,
    @NotNull(message = "유효 시작일을 입력하세요.") LocalDate validFrom,
    LocalDate validTo,
    UUID approvedByUserId,
    LocalDateTime revokedAt,
    @NotNull(message = "사용여부를 입력하세요.") Boolean isUsed
) {
}
