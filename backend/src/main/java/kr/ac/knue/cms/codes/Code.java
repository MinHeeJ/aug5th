package kr.ac.knue.cms.codes;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.Map;

public record Code(
    String codeId,
    @NotBlank(message = "그룹ID는 필수입니다.") String groupId,
    @NotBlank(message = "코드값은 필수입니다.") String codeValue,
    @NotBlank(message = "코드명은 필수입니다.") String codeName,
    String parentCodeId,
    @NotNull(message = "정렬순서는 필수입니다.") Integer sortOrder,
    Map<String, Object> extraAttributes,
    LocalDate validFrom,
    LocalDate validTo,
    Boolean isUsed
) {
}
