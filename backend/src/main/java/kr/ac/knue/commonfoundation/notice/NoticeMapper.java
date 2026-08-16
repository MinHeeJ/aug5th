package kr.ac.knue.commonfoundation.notice;

import java.time.LocalDate;
import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface NoticeMapper {

    @SelectProvider(type = NoticeMapperSqlProvider.class, method = "selectNotices")
    List<NoticeListItem> selectNotices(NoticeSearchCondition condition);

    @SelectProvider(type = NoticeMapperSqlProvider.class, method = "countNotices")
    long countNotices(NoticeSearchCondition condition);

    @Select("""
        select exists(select 1 from notices where notice_id = #{noticeId})
        """)
    boolean existsNotice(@Param("noticeId") Long noticeId);

    @Select("""
        select n.notice_id as "noticeId",
               n.title as "title",
               left(coalesce(n.content, ''), 120) as "contentSummary",
               n.post_from as "postFrom",
               n.post_to as "postTo",
               n.target_roles as "targetRoles",
               n.target_organizations as "targetOrganizations",
               n.important as "important",
               n.enabled as "enabled",
               n.attachment_count as "attachmentCount",
               '지정 대상 역할·조직과 게시기간에만 노출됩니다.' as "exposureRule",
               '공지 열람은 업무 승인이나 확인처리로 간주하지 않습니다.' as "readBoundary"
        from notices n
        where n.notice_id = #{noticeId}
        """)
    NoticeListItem selectNotice(@Param("noticeId") Long noticeId);

    @Update("""
        update notices
        set title = #{title},
            post_from = #{postFrom},
            post_to = #{postTo},
            target_roles = #{targetRoles},
            target_organizations = #{targetOrganizations},
            important = #{important},
            enabled = #{enabled},
            updated_at = now()
        where notice_id = #{noticeId}
        """)
    int updateNotice(
        @Param("noticeId") Long noticeId,
        @Param("title") String title,
        @Param("postFrom") LocalDate postFrom,
        @Param("postTo") LocalDate postTo,
        @Param("targetRoles") String targetRoles,
        @Param("targetOrganizations") String targetOrganizations,
        @Param("important") Boolean important,
        @Param("enabled") Boolean enabled
    );

    @Insert("""
        insert into audit_logs (log_type, target_key, actor_id, before_value, after_value, result)
        values (#{logType}, #{targetKey}, #{actorId}, #{beforeValue}::jsonb, #{afterValue}::jsonb, 'SUCCESS')
        """)
    void insertAudit(
        @Param("logType") String logType,
        @Param("targetKey") String targetKey,
        @Param("actorId") String actorId,
        @Param("beforeValue") String beforeValue,
        @Param("afterValue") String afterValue
    );
}
