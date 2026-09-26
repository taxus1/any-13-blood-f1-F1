package com.somepro.infrastructure.persistence.unit;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.somepro.infrastructure.persistence.unit.po.BloodUnitPO;
import com.somepro.infrastructure.persistence.unit.po.UnitListRowPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 血袋 Mapper（基础设施层）。阻塞 JDBC API，只能在 boundedElastic 线程上调用。
 *
 * 列表查询是手写 SQL：血袋名单要带献血者编号/姓名，用一条 LEFT JOIN 取，
 * 避免逐行回查献血者（N+1）。软删条件两边都要带：@TableLogic 只对单表 selectList 生效，
 * 手写 SQL 不会被它改写。
 */
@Mapper
public interface BloodUnitMapper extends BaseMapper<BloodUnitPO> {

    /**
     * 血袋分页列表（PageHelper 拦截它做 limit/count）。
     * 条件全部可空，空则不加该条件；编号精确、献血者姓名模糊，其余精确。
     */
    @Select("""
            <script>
            SELECT u.id, u.unit_no, u.donor_id, d.donor_no, d.donor_name,
                   u.component, u.blood_group, u.rh, u.volume_ml,
                   u.collected_at, u.expire_at, u.storage_temp, u.status
            FROM t_blood_unit u
            LEFT JOIN t_blood_donor d ON d.id = u.donor_id AND d.del_flag = 0
            WHERE u.del_flag = 0
            <if test="unitNo != null and unitNo != ''">
                AND u.unit_no = #{unitNo}
            </if>
            <if test="donorNo != null and donorNo != ''">
                AND d.donor_no = #{donorNo}
            </if>
            <if test="donorName != null and donorName != ''">
                AND d.donor_name LIKE CONCAT('%', #{donorName}, '%')
            </if>
            <if test="bloodGroup != null">AND u.blood_group = #{bloodGroup}</if>
            <if test="rh != null">AND u.rh = #{rh}</if>
            <if test="component != null">AND u.component = #{component}</if>
            <if test="status != null">AND u.status = #{status}</if>
            ORDER BY u.unit_no ASC, u.id ASC
            </script>
            """)
    List<UnitListRowPO> selectListPage(@Param("unitNo") String unitNo,
                                       @Param("donorNo") String donorNo,
                                       @Param("donorName") String donorName,
                                       @Param("bloodGroup") String bloodGroup,
                                       @Param("rh") String rh,
                                       @Param("component") String component,
                                       @Param("status") String status);

    /**
     * 取某年血袋编号的最大数字序号（BU-yyyy-序号），含软删记录（uk_unit_no 物理唯一，编号不可复用）。
     *
     * @return 最大序号；该年尚无记录返回 null
     */
    @Select("SELECT MAX(CAST(SUBSTRING_INDEX(unit_no, '-', -1) AS UNSIGNED)) "
            + "FROM t_blood_unit WHERE unit_no LIKE CONCAT('BU-', #{year}, '-%')")
    Integer maxSerialOfYear(@Param("year") int year);
}
