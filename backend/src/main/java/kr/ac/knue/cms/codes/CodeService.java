package kr.ac.knue.cms.codes;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import kr.ac.knue.cms.common.api.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CodeService {
    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {};
    private final CodeGroupMapper codeGroupMapper;
    private final CodeMapper codeMapper;
    private final ObjectMapper objectMapper;

    public CodeService(CodeGroupMapper codeGroupMapper, CodeMapper codeMapper, ObjectMapper objectMapper) {
        this.codeGroupMapper = codeGroupMapper;
        this.codeMapper = codeMapper;
        this.objectMapper = objectMapper;
    }

    public List<Map<String, Object>> listCodes(String groupId) {
        if (!codeGroupMapper.existsByGroupId(groupId)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "CODE_GROUP_NOT_FOUND", "코드그룹을 찾을 수 없습니다.",
                Map.of("groupId", "존재하는 코드그룹을 선택하세요."));
        }
        return codeMapper.findByGroupId(groupId).stream().map(this::inflateExtraAttributes).toList();
    }

    @Transactional
    public Map<String, Object> saveCode(String groupId, String codeValue, Code request) {
        validateIdentity(groupId, codeValue, request);
        validateDates(request.validFrom(), request.validTo());
        if (!codeGroupMapper.existsByGroupId(groupId)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "CODE_GROUP_NOT_FOUND", "코드그룹을 찾을 수 없습니다.",
                Map.of("groupId", "상세코드를 저장할 코드그룹이 필요합니다."));
        }
        if (request.parentCodeId() != null && !request.parentCodeId().isBlank() && !codeMapper.existsParentInGroup(groupId, request.parentCodeId())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "PARENT_CODE_NOT_FOUND", "같은 코드그룹의 상위코드를 선택하세요.",
                Map.of("parentCodeId", "상위코드는 같은 코드그룹의 상세코드여야 합니다."));
        }
        Map<String, Object> before = codeMapper.findByGroupIdAndCodeValue(groupId, codeValue);
        CodeSaveCommand command = new CodeSaveCommand(
            groupId,
            codeValue,
            request.codeName(),
            blankToNull(request.parentCodeId()),
            request.sortOrder(),
            toJson(request.extraAttributes()),
            request.validFrom(),
            request.validTo(),
            request.isUsed(),
            before == null ? null : safeJson(before),
            safeJson(Map.of("groupId", groupId, "codeValue", codeValue, "codeName", request.codeName(), "isUsed", request.isUsed() == null || request.isUsed()))
        );
        codeMapper.upsert(command);
        return inflateExtraAttributes(codeMapper.findByGroupIdAndCodeValue(groupId, codeValue));
    }

    private void validateIdentity(String groupId, String codeValue, Code request) {
        if (request.groupId() != null && !request.groupId().isBlank() && !groupId.equals(request.groupId())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "CODE_GROUP_ID_MISMATCH", "그룹ID는 경로와 요청 본문이 일치해야 합니다.",
                Map.of("groupId", "path groupId와 요청 groupId가 일치해야 합니다."));
        }
        if (request.codeValue() != null && !request.codeValue().isBlank() && !codeValue.equals(request.codeValue())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "CODE_VALUE_MISMATCH", "코드값은 경로와 요청 본문이 일치해야 합니다.",
                Map.of("codeValue", "path codeValue와 요청 codeValue가 일치해야 합니다."));
        }
    }

    private void validateDates(LocalDate validFrom, LocalDate validTo) {
        if (validFrom != null && validTo != null && validFrom.isAfter(validTo)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_CODE_PERIOD", "유효 시작일은 종료일보다 늦을 수 없습니다.",
                Map.of("validTo", "유효 종료일은 시작일 이후여야 합니다."));
        }
    }

    private String toJson(Map<String, Object> value) {
        try {
            return objectMapper.writeValueAsString(value == null ? Map.of() : value);
        } catch (JsonProcessingException exception) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_EXTRA_ATTRIBUTES", "추가속성은 JSON 객체여야 합니다.",
                Map.of("extraAttributes", "JSON 객체 형식으로 입력하세요."));
        }
    }

    private String safeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            return "{}";
        }
    }

    private Map<String, Object> inflateExtraAttributes(Map<String, Object> row) {
        if (row == null) {
            return new LinkedHashMap<>();
        }
        Map<String, Object> copy = new LinkedHashMap<>(row);
        if (!copy.containsKey("parentCodeId")) {
            copy.put("parentCodeId", null);
        }
        Object raw = copy.remove("extraAttributesJson");
        if (raw == null) {
            copy.put("extraAttributes", Map.of());
            return copy;
        }
        try {
            copy.put("extraAttributes", objectMapper.readValue(raw.toString(), MAP_TYPE));
        } catch (JsonProcessingException exception) {
            copy.put("extraAttributes", Map.of());
        }
        return copy;
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}
