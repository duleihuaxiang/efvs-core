package cn.iocoder.yudao.module.qar.controller.admin.qardata.vo;

import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * 性能分析：多架次 taxi-phase 分页请求。
 * <p>每页条数与 {@code /taxi-phase/play} 内部批大小一致（10）。</p>
 */
@Data
public class QarTaxiComparePageReqVO {

    public static final int DEFAULT_PAGE_SIZE = 10;

    private Integer pageSize;

    @NotEmpty(message = "sorties 不能为空")
    @Size(max = 20, message = "最多 20 个架次")
    @Valid
    private List<QarTaxiCompareSortieStateVO> sorties;
}
