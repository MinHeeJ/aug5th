package kr.ac.knue.commonfoundation.systemconfig;

import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface SystemConfigurationMapper {

    @SelectProvider(type = SystemConfigurationMapperSqlProvider.class, method = "selectSystemConfigurations")
    List<SystemConfigurationListItem> selectSystemConfigurations(SystemConfigurationSearchCondition condition);

    @SelectProvider(type = SystemConfigurationMapperSqlProvider.class, method = "countSystemConfigurations")
    long countSystemConfigurations(SystemConfigurationSearchCondition condition);

    @Select("""
        select exists(select 1 from system_configurations where config_key = #{configKey})
        """)
    boolean existsSystemConfiguration(@Param("configKey") String configKey);

    @Select("""
        select sc.config_key as "configKey",
               sc.config_value as "configValue",
               sc.unit as "unit",
               sc.value_range as "valueRange",
               sc.enabled as "enabled",
               '전체 사용자 공통 적용' as "applyScope",
               '설정값은 항목별 단위와 값 범위를 서버에서 검증합니다.' as "validationRule"
        from system_configurations sc
        where sc.config_key = #{configKey}
        """)
    SystemConfigurationListItem selectSystemConfiguration(@Param("configKey") String configKey);

    @Update("""
        update system_configurations
        set config_value = #{configValue},
            enabled = #{enabled}
        where config_key = #{configKey}
        """)
    int updateSystemConfiguration(
        @Param("configKey") String configKey,
        @Param("configValue") String configValue,
        @Param("enabled") boolean enabled
    );

    @Insert("""
        insert into audit_logs (log_type, target_key, actor_id, before_value, after_value, result)
        values (#{logType}, #{targetKey}, #{actorId}, null, #{afterValue}::jsonb, 'SUCCESS')
        """)
    void insertAudit(
        @Param("logType") String logType,
        @Param("targetKey") String targetKey,
        @Param("actorId") String actorId,
        @Param("afterValue") String afterValue
    );
}
