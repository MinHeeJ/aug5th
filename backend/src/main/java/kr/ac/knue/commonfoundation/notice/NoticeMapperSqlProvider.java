package kr.ac.knue.commonfoundation.notice;

import org.apache.ibatis.jdbc.SQL;

public final class NoticeMapperSqlProvider {

    private NoticeMapperSqlProvider() {
    }

    public static String selectNotices(NoticeSearchCondition condition) {
        return baseSelect(condition)
            + " order by " + orderBy(condition.sort())
            + " limit #{size} offset #{offset}";
    }

    public static String countNotices(NoticeSearchCondition condition) {
        return "select count(*) from (" + baseSelect(condition) + ") counted_notices";
    }

    private static String baseSelect(NoticeSearchCondition condition) {
        SQL sql = new SQL()
            .SELECT("n.notice_id as \"noticeId\"")
            .SELECT("n.title as \"title\"")
            .SELECT("left(coalesce(n.content, ''), 120) as \"contentSummary\"")
            .SELECT("n.post_from as \"postFrom\"")
            .SELECT("n.post_to as \"postTo\"")
            .SELECT("n.target_roles as \"targetRoles\"")
            .SELECT("n.target_organizations as \"targetOrganizations\"")
            .SELECT("n.important as \"important\"")
            .SELECT("n.enabled as \"enabled\"")
            .SELECT("n.attachment_count as \"attachmentCount\"")
            .SELECT("'지정 대상 역할·조직과 게시기간에만 노출됩니다.' as \"exposureRule\"")
            .SELECT("'공지 열람은 업무 승인이나 확인처리로 간주하지 않습니다.' as \"readBoundary\"")
            .FROM("notices n");
        if (condition.q() != null) {
            sql.WHERE("(n.title ilike '%' || #{q} || '%' or coalesce(n.content, '') ilike '%' || #{q} || '%' or coalesce(n.target_roles, '') ilike '%' || #{q} || '%' or coalesce(n.target_organizations, '') ilike '%' || #{q} || '%')");
        }
        if (condition.targetRole() != null) {
            sql.WHERE("(',' || coalesce(n.target_roles, '') || ',') ilike '%,' || #{targetRole} || ',%'");
        }
        if (condition.important() != null) {
            sql.WHERE("n.important = #{important}");
        }
        if (condition.enabled() != null) {
            sql.WHERE("n.enabled = #{enabled}");
        }
        if (condition.activeOn() != null) {
            sql.WHERE("n.post_from <= #{activeOn}");
            sql.WHERE("n.post_to >= #{activeOn}");
        }
        return sql.toString();
    }

    private static String orderBy(String sort) {
        return switch (sort == null ? "" : sort) {
            case "title" -> "n.title asc, n.notice_id asc";
            case "postFrom" -> "n.post_from desc, n.notice_id desc";
            case "postTo" -> "n.post_to desc, n.notice_id desc";
            case "important" -> "n.important desc, n.post_from desc, n.notice_id desc";
            default -> "n.important desc, n.post_from desc, n.notice_id desc";
        };
    }
}
