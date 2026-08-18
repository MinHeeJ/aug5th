package kr.ac.knue.cms.users;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record UserSystemAccessRequest(
    @NotNull Boolean isSystemEnabled,
    @NotNull @Size(min = 1) List<String> roleCodes,
    String changeReason
) {
}
