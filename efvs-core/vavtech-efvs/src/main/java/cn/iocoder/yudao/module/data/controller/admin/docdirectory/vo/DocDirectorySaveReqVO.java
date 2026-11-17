package cn.iocoder.yudao.module.data.controller.admin.docdirectory.vo;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.*;
import org.apache.ibatis.type.JdbcType;

import java.util.*;
import javax.validation.constraints.*;

@Data
public class DocDirectorySaveReqVO{
    private Integer id;

    private String name;

    private Integer parentId;

    private Integer level;

    @TableField(fill = FieldFill.INSERT, jdbcType = JdbcType.VARCHAR)
    private String creator;

    @TableField(fill = FieldFill.INSERT_UPDATE, jdbcType = JdbcType.VARCHAR)
    private String updater;

    private String remark;

//    @NotNull(message = "状态不能为空")
    private Integer status;

}
