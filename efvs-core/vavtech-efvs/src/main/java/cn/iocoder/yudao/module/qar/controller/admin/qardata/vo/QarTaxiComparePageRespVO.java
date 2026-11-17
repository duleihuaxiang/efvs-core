package cn.iocoder.yudao.module.qar.controller.admin.qardata.vo;

import lombok.Data;

import java.util.List;

@Data
public class QarTaxiComparePageRespVO {

    private Integer pageSize;

    private List<QarTaxiCompareSortiePageRespVO> sorties;
}
