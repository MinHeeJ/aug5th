package kr.ac.knue.cms.common.history;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ChangeTraceFields(String beforeValue, String afterValue, UUID changedByUserId, OffsetDateTime changedAt, String changeReason) {
    public static ChangeTraceFields of(String beforeValue, String afterValue, UUID changedByUserId, String changeReason) {
        return new ChangeTraceFields(beforeValue, afterValue, changedByUserId, OffsetDateTime.now(), changeReason);
    }
}
