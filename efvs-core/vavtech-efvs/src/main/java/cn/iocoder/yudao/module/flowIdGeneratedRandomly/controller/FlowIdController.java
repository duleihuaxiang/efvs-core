package cn.iocoder.yudao.module.flowIdGeneratedRandomly.controller;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.flowIdGeneratedRandomly.service.FlowIdService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@RestController
@RequestMapping("/admin-api/flowId")
@Validated
public class FlowIdController {

    @Resource
    private FlowIdService flowIdService;

    @GetMapping("/generate-number")
    public CommonResult<String> generateFlowNumber(@RequestParam("type") String type) {
        String number = flowIdService.generateFlowNumber(type);
        return success(number);
    }

}
