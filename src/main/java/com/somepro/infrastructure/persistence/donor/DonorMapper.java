package com.somepro.infrastructure.persistence.donor;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.somepro.infrastructure.persistence.donor.po.DonorPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 献血者 Mapper（基础设施层）。阻塞 JDBC API，只能在 boundedElastic 线程上调用。
 */
@Mapper
public interface DonorMapper extends BaseMapper<DonorPO> {

    /**
     * 取某年献血者编号的最大数字序号（编号格式 DNR-yyyy-序号）。
     *
     * 刻意不加 del_flag 条件：物理唯一索引 uk_donor_no 不认软删，已删除编号也不能复用，
     * 所以取号必须把软删记录算进去。用 SUBSTRING_INDEX 取最后一段转无符号整数比较，
     * 避免序号从 9999 涨到 10000 时字符串排序错位。
     *
     * @return 最大序号；该年尚无记录返回 null
     */
    @Select("SELECT MAX(CAST(SUBSTRING_INDEX(donor_no, '-', -1) AS UNSIGNED)) "
            + "FROM t_blood_donor WHERE donor_no LIKE CONCAT('DNR-', #{year}, '-%')")
    Integer maxSerialOfYear(@Param("year") int year);
}
