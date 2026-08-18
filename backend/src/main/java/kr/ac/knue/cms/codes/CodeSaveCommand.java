package kr.ac.knue.cms.codes;

import java.time.LocalDate;

public record CodeSaveCommand(
    String groupId,
    String codeValue,
    String codeName,
    String parentCodeId,
    Integer sortOrder,
    String extraAttributesJson,
    LocalDate validFrom,
    LocalDate validTo,
    Boolean isUsed,
    String beforeValue,
    String afterValue
) {
}
