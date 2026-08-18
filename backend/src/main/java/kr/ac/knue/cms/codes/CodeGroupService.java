package kr.ac.knue.cms.codes;

import java.util.List;
import java.util.Map;
import kr.ac.knue.cms.common.api.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CodeGroupService {
    private final CodeGroupMapper codeGroupMapper;

    public CodeGroupService(CodeGroupMapper codeGroupMapper) {
        this.codeGroupMapper = codeGroupMapper;
    }

    public List<Map<String, Object>> listCodeGroups(String filter) {
        return codeGroupMapper.findAll(filter);
    }

    @Transactional
    public Map<String, Object> saveCodeGroup(String groupId, CodeGroup request) {
        if (request.groupId() != null && !request.groupId().isBlank() && !groupId.equals(request.groupId())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "CODE_GROUP_ID_MISMATCH", "그룹ID는 경로와 요청 본문이 일치해야 합니다.",
                Map.of("groupId", "path groupId와 요청 groupId가 일치해야 합니다."));
        }
        CodeGroup normalized = new CodeGroup(groupId, request.groupName(), request.description(), request.managingDepartment(), request.isUsed());
        codeGroupMapper.upsert(normalized);
        return codeGroupMapper.findByGroupId(groupId);
    }
}
