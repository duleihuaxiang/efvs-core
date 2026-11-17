package cn.iocoder.yudao.module.data.controller.admin.product.vo;

import lombok.Data;

/**
 * 产品档案「LRU构建信息」前端返回结构
 *
 * 前端表格字段约定：
 * - partName（零部件名称）
 * - drawingNumber（零部件图号）
 * - drawingVersion（图纸版本）
 * - productionNumber（生产编号/序列号）
 */
@Data
public class ProductArchiveConstructionRespVO {

    private Integer id;

    private String partNumber;

    private String sequenceNumber;

    private String partName;

    private String drawingNumber;

    private String drawingVersion;

    private String productionNumber;

    private String baseline;

    private String productBaseline;

    private String fileBaseline;

    private String customerNumber;

    private String customerName;

    private String remark;
}

