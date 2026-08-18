package kr.ac.knue.cms.organizations;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import kr.ac.knue.cms.auth.AuthenticatedUser;
import kr.ac.knue.cms.common.api.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrganizationService {
    private final OrganizationMapper organizationMapper;

    public OrganizationService(OrganizationMapper organizationMapper) {
        this.organizationMapper = organizationMapper;
    }

    public List<OrganizationDto> listOrganizations(String filter, String organizationCode, String organizationType, int page, int size) {
        int safeSize = Math.max(1, Math.min(size, 100));
        int offset = Math.max(0, page) * safeSize;
        return organizationMapper.findOrganizations(filter, organizationCode, organizationType, safeSize, offset)
            .stream().map(this::toOrganization).toList();
    }

    public List<OrganizationTreeNode> getTree() {
        Map<UUID, OrganizationTreeNode> nodes = new LinkedHashMap<>();
        Map<UUID, List<OrganizationTreeNode>> childrenByParent = new LinkedHashMap<>();
        for (Map<String, Object> row : organizationMapper.findTreeRows()) {
            OrganizationTreeNode node = toTreeNode(row, new ArrayList<>());
            nodes.put(node.organizationId(), node);
            if (node.parentOrganizationId() != null) {
                childrenByParent.computeIfAbsent(node.parentOrganizationId(), key -> new ArrayList<>()).add(node);
            }
        }
        List<OrganizationTreeNode> roots = new ArrayList<>();
        for (OrganizationTreeNode node : nodes.values()) {
            OrganizationTreeNode withChildren = node.withChildren(childrenByParent.getOrDefault(node.organizationId(), List.of()));
            if (node.parentOrganizationId() == null || !nodes.containsKey(node.parentOrganizationId())) {
                roots.add(withChildren);
            } else {
                replaceChild(childrenByParent.get(node.parentOrganizationId()), withChildren);
            }
        }
        return roots;
    }

    @Transactional
    public OrganizationRelationshipDto saveRelationship(UUID relationshipId, OrganizationRelationshipRequest request, AuthenticatedUser actor) {
        if (request.effectiveEndDate() != null && request.effectiveStartDate().isAfter(request.effectiveEndDate())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "적용기간을 확인해 주세요.",
                Map.of("effectiveEndDate", "적용 종료일은 적용 시작일보다 빠를 수 없습니다."));
        }
        String beforeValue = Optional.ofNullable(organizationMapper.findRelationship(relationshipId)).map(Object::toString).orElse(null);
        String afterValue = "organizationId=" + request.organizationId() + ", parentOrganizationId=" + request.parentOrganizationId()
            + ", effectiveStartDate=" + request.effectiveStartDate() + ", effectiveEndDate=" + request.effectiveEndDate();
        organizationMapper.upsertRelationship(relationshipId, request.organizationId(), request.parentOrganizationId(),
            request.effectiveStartDate(), request.effectiveEndDate(), actor.userId(), request.changeReason(), beforeValue, afterValue);
        return Optional.ofNullable(organizationMapper.findRelationship(relationshipId)).map(this::toRelationship)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "RELATIONSHIP_NOT_FOUND", "조직 관계를 찾을 수 없습니다."));
    }

    private void replaceChild(List<OrganizationTreeNode> siblings, OrganizationTreeNode replacement) {
        if (siblings == null) {
            return;
        }
        for (int index = 0; index < siblings.size(); index++) {
            if (siblings.get(index).organizationId().equals(replacement.organizationId())) {
                siblings.set(index, replacement);
                return;
            }
        }
    }

    private OrganizationDto toOrganization(Map<String, Object> row) {
        return new OrganizationDto((UUID) row.get("organizationId"), (String) row.get("organizationCode"),
            (String) row.get("organizationName"), (String) row.get("organizationType"), Boolean.TRUE.equals(row.get("isUsed")));
    }

    private OrganizationTreeNode toTreeNode(Map<String, Object> row, List<OrganizationTreeNode> children) {
        LocalDate start = toLocalDate(row.get("effectiveStartDate"));
        LocalDate end = toLocalDate(row.get("effectiveEndDate"));
        return new OrganizationTreeNode((UUID) row.get("organizationId"), (String) row.get("organizationCode"),
            (String) row.get("organizationName"), (String) row.get("organizationType"), Boolean.TRUE.equals(row.get("isUsed")),
            (UUID) row.get("relationshipId"), (UUID) row.get("parentOrganizationId"),
            start == null ? null : start.toString(), end == null ? null : end.toString(), children);
    }

    private OrganizationRelationshipDto toRelationship(Map<String, Object> row) {
        return new OrganizationRelationshipDto((UUID) row.get("relationshipId"), (UUID) row.get("organizationId"),
            (UUID) row.get("parentOrganizationId"), toLocalDate(row.get("effectiveStartDate")),
            toLocalDate(row.get("effectiveEndDate")), (String) row.get("changeReason"));
    }

    private LocalDate toLocalDate(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof LocalDate localDate) {
            return localDate;
        }
        if (value instanceof java.sql.Date sqlDate) {
            return sqlDate.toLocalDate();
        }
        return LocalDate.parse(value.toString());
    }
}
