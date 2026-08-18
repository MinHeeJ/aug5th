package kr.ac.knue.cms.common.api;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

public record ApiResponse<T>(boolean success, Map<String, Object> meta, T data, String message) {
    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, defaultMeta(), data, null);
    }

    public static ApiResponse<Map<String, Object>> message(String message) {
        return new ApiResponse<>(true, defaultMeta(), Map.of(), message);
    }

    public static Map<String, Object> defaultMeta() {
        Map<String, Object> meta = new LinkedHashMap<>();
        meta.put("timestamp", OffsetDateTime.now().toString());
        return meta;
    }
}
