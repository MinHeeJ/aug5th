package kr.ac.knue.commonfoundation.position;

import kr.ac.knue.commonfoundation.api.ApiException;
import kr.ac.knue.commonfoundation.auth.CurrentUserContext;
import kr.ac.knue.commonfoundation.auth.SessionPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PositionManagementService {

    private final PositionMapper positionMapper;
    private final CurrentUserContext currentUserContext;

    public PositionManagementService(PositionMapper positionMapper, CurrentUserContext currentUserContext) {
        this.positionMapper = positionMapper;
        this.currentUserContext = currentUserContext;
    }

    @Transactional(readOnly = true)
    public PositionListResponse listPositions(PositionSearchCondition condition) {
        return new PositionListResponse(
            positionMapper.selectPositions(condition),
            condition.page(),
            condition.size(),
            positionMapper.countPositions(condition),
            "SCR-POSITION-MGMT",
            "R09"
        );
    }

    @Transactional
    public SavePositionResponse savePosition(SavePositionRequest request) {
        SessionPrincipal principal = currentUserContext.current()
            .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "인증이 필요합니다."));
        Long positionId = request.positionId();
        if (positionId == null || !positionMapper.existsPosition(positionId)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "보직 배정을 찾을 수 없습니다.");
        }
        java.time.LocalDate validTo = Boolean.TRUE.equals(request.active()) ? request.validTo() : java.time.LocalDate.of(2026, 12, 31);
        positionMapper.updatePositionManagementFields(positionId, validTo);
        positionMapper.insertAudit(
            "position_assignments:" + positionId,
            principal.user().userId(),
            jsonAfterValue(positionId, request, validTo)
        );
        return new SavePositionResponse(
            positionId,
            request.active(),
            validTo == null ? null : validTo.toString(),
            "보직 관리 저장이 완료되었습니다."
        );
    }

    private static String jsonAfterValue(Long positionId, SavePositionRequest request, java.time.LocalDate validTo) {
        return "{\"positionId\":" + positionId
            + ",\"active\":" + request.active()
            + ",\"validTo\":" + nullableJson(validTo == null ? null : validTo.toString())
            + ",\"reason\":\"" + escapeJson(request.reason()) + "\"}";
    }

    private static String nullableJson(String value) {
        return value == null ? "null" : "\"" + escapeJson(value) + "\"";
    }

    private static String escapeJson(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

}
