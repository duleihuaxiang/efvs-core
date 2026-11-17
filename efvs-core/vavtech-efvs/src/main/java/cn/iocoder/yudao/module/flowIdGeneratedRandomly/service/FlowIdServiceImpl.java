package cn.iocoder.yudao.module.flowIdGeneratedRandomly.service;

import cn.idev.excel.util.StringUtils;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.module.customerResponse.dal.dataobject.customerserviceissues.CustomerServiceIssuesDO;
import cn.iocoder.yudao.module.customerResponse.service.customerserviceissues.CustomerServiceIssuesService;
import cn.iocoder.yudao.module.flowIdGeneratedRandomly.mapper.FlowIdMapper;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Validated
public class FlowIdServiceImpl implements FlowIdService {
    @Resource
    private FlowIdMapper flowIdMapper;

    @Resource
    private CustomerServiceIssuesService customerServiceIssuesService;

    @Override
    public String generateFlowNumber(String type) {
        Long tenantId = TenantContextHolder.getTenantId();
        String result;
        switch (type) {
            case "training":
                result = generateTrainingNumber();
                break;
            case "customer":
                result = generateCustomerNumber(tenantId);
                break;
            default:
                result = "错误类型，请重试！";
                break;
        }
        return result;
    }
    /**
     * 生成培训编号
     *
     * @return 培训编号
     */
    private String generateTrainingNumber() {
        // 获取当前年月
        LocalDate now = LocalDate.now();
        int currentYear = now.getYear();
        int currentMonth = now.getMonthValue();
        // 获取当前租户ID
//        Long tenantId = TenantContextHolder.getTenantId();
        // 根据年份获取最大编号
        String maxNumber = flowIdMapper.selectMaxNumberByYear(currentYear);
        if (maxNumber != null && !maxNumber.isEmpty()) {
            // 提取编号中的两位流水并加1
            String sequenceStr = maxNumber.substring(7); // 获取年月后的两位数字
            int nextSequence = Integer.parseInt(sequenceStr) + 1;
            return String.format("T%d%02d%02d", currentYear, currentMonth, nextSequence);
        } else {
            // 如果没有编号则返回T+年月+01
            return String.format("T%d%02d%02d", currentYear, currentMonth, 1);
        }
    }
    /**
     * 生成客户编号
     *
     * @return 客户编号
     */
    private String generateCustomerNumber(Long tenantId) {
        String result = "";
        String maxQuestionNumber = customerServiceIssuesService.getMaxQuestionNumber(tenantId);

        // 1. 获取当前年份（动态，无需硬编码）
        int currentYear = LocalDate.now().getYear();
        // 拼接默认编号格式：I + 年份 + 001（比如2026年就是I2026001）
        String defaultNumber = String.format("I%d%03d", currentYear, 1);

        // 2. 检查查询到的最大编号是否非空
        if (StringUtils.isNotBlank(maxQuestionNumber)) {
            Pattern pattern = Pattern.compile("\\d+");
            Matcher matcher = pattern.matcher(maxQuestionNumber);
            if (matcher.find()) {
                // 提取数字并加1，保持5位补0（和原逻辑一致）
                int num = Integer.parseInt(matcher.group());
                result = String.format("I%05d", num + 1);
            } else {
                // 有值但无数字，使用带当前年份的默认编号
                result = defaultNumber;
            }
        } else {
            // 未查询到最大编号，使用带当前年份的默认编号
            result = defaultNumber;
        }
        return result;
    }

}
