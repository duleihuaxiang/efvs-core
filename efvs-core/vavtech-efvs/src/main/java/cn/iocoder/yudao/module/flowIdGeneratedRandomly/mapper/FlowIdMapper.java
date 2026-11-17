package cn.iocoder.yudao.module.flowIdGeneratedRandomly.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface FlowIdMapper {
    /**
     * 根据年份查询最大编号
     *
     * @param year 年份
     * @return 最大编号
     */
//    @Select("SELECT \n" +
//            "  COALESCE(\n" +
//            "    (SELECT annual_plan_project_number_id \n" +
//            "     FROM efvs_training_plan \n" +
//            "     WHERE annual_plan_project_number_id LIKE CONCAT('T',#{year},'%')\n" +
//            "     ORDER BY CAST(SUBSTRING(annual_plan_project_number_id, LENGTH(CONCAT('T',#{year})) + 1) AS UNSIGNED) DESC\n" +
//            "     LIMIT 1),\n" +
//            "    ''\n" +
//            "  ) AS max_full_project_id\n" +
//            "FROM DUAL;")
    @InterceptorIgnore(tenantLine = "true") // 跳过租户拦截器
     String selectMaxNumberByYear(@Param("year") Integer year);
}
