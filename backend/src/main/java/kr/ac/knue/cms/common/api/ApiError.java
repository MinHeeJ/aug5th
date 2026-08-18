package kr.ac.knue.cms.common.api;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

public record ApiError(boolean success, Map<String, Object> meta, ErrorBody error) {
    public static ApiError of(String code, String message) {
        return of(code, message, Map.of());
    }

    public static ApiError of(String code, String message, Map<String, String> fields) {
        return new ApiError(false, defaultMeta(), new ErrorBody(code, message, fields));
    }

    public static Map<String, Object> defaultMeta() {
        Map<String, Object> meta = new LinkedHashMap<>();
        meta.put("timestamp", OffsetDateTime.now().toString());
        return meta;
    }

    public record ErrorBody(String code, String message, Map<String, String> fields) {
    }
}
