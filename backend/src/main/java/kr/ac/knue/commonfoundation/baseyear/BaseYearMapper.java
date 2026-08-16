package kr.ac.knue.commonfoundation.baseyear;

import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface BaseYearMapper {

    @SelectProvider(type = BaseYearMapperSqlProvider.class, method = "selectBaseYears")
    List<BaseYearListItem> selectBaseYears(BaseYearSearchCondition condition);

    @SelectProvider(type = BaseYearMapperSqlProvider.class, method = "countBaseYears")
    long countBaseYears(BaseYearSearchCondition condition);

    @Select("""
        select exists(select 1 from base_years where base_year = #{baseYear})
        """)
    boolean existsBaseYear(@Param("baseYear") String baseYear);

    @Select("""
        select byr.base_year as "baseYear",
               byr.default_query_year as "defaultQueryYear",
               byr.copy_baseline_enabled as "copyBaselineEnabled",
               byr.reset_enabled as "resetEnabled",
               byr.enabled as "enabled",
               '기준연도는 4자리 연도이며 기본 조회연도는 기준연도 이하로 관리합니다.' as "periodRule",
               '기준정보 복사 후 초기화 실행 여부를 서버에서 검증합니다.' as "transitionRule"
        from base_years byr
        where byr.base_year = #{baseYear}
        """)
    BaseYearListItem selectBaseYear(@Param("baseYear") String baseYear);

    @Update("""
        update base_years
        set default_query_year = #{defaultQueryYear},
            copy_baseline_enabled = #{copyBaselineEnabled},
            reset_enabled = #{resetEnabled},
            enabled = #{enabled}
        where base_year = #{baseYear}
        """)
    int updateBaseYear(
        @Param("baseYear") String baseYear,
        @Param("defaultQueryYear") String defaultQueryYear,
        @Param("copyBaselineEnabled") boolean copyBaselineEnabled,
        @Param("resetEnabled") boolean resetEnabled,
        @Param("enabled") boolean enabled
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
