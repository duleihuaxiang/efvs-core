package cn.iocoder.yudao.module.qar.controller.admin.qardata.vo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * QAR数据映射关系VO
 * <p>
 * 用于从Excel映射模板中读取映射关系
 *
 * @author admin
 */
@Data
@ExcelIgnoreUnannotated
public class QarDataMappingVO {

    /**
     * 数据库字段名（如Time）
     */
    @ExcelProperty(index = 0)
    private String fieldName;

    /**
     * 列名字
     */
    @ExcelProperty(index = 1)
    private String columnName;

}
