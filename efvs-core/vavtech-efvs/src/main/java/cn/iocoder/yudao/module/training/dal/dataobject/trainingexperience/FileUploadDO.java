package cn.iocoder.yudao.module.training.dal.dataobject.trainingexperience;

import lombok.*;

import java.io.Serializable;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

/**
 * 文件下载 DO
 *
 * @author admin
 */
@TableName("efvs_file_upload")
@KeySequence("efvs_file_upload_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileUploadDO extends BaseDO implements Serializable {

    /**
     * ID
     */
    @TableId
    private Integer id;
    /**
     * 关联ID
     */
    private Integer bizId;
    /**
     * 文件名称
     */
    private String fileName;
    /**
     * 文件大小
     */
    private String fileSize;
    /**
     * 文件下载路径
     */
    private String fileUrl;
    /**
     * 文件绝对路径
     */
    private String fileAbsoluteUrl;
    /**
     * 备注
     */
    private String remark;
    /**
     * 文件预览路径
     */
    private String filePreviewPath;

    public FileUploadDO( String fileName, String fileSize, String fileUrl, String fileAbsoluteUrl, String remark,String typeName) {
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.fileUrl = fileUrl;
        this.fileAbsoluteUrl = fileAbsoluteUrl;
        this.remark = remark;
        this.typeName = typeName;
    }

    /**
     * 文件类型名称
     */
    private String typeName;
}