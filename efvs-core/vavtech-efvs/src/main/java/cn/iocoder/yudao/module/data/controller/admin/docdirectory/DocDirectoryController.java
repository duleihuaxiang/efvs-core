package cn.iocoder.yudao.module.data.controller.admin.docdirectory;

import cn.iocoder.yudao.module.data.controller.admin.docdirectory.vo.DocDirectorySaveReqVO;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import java.util.*;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.data.dal.dataobject.docdirectory.DocDirectoryDO;
import cn.iocoder.yudao.module.data.service.docdirectory.DocDirectoryService;

@RestController
@RequestMapping("/data/doc-directory")
@Validated
public class DocDirectoryController {

    @Resource
    private DocDirectoryService docDirectoryService;
    /**
     * 获取完整目录树
     */
    @GetMapping("/tree")
    public CommonResult<List<DocDirectoryDO>> getDirectoryTree() {
        List<DocDirectoryDO> directoryTree = docDirectoryService.getDirectoryTree();
        CommonResult<List<DocDirectoryDO>> objectCommonResult = new CommonResult<>();
        objectCommonResult.setData(directoryTree);
        return objectCommonResult;
    }

    //新增目录
    @PostMapping("/addDirectory")
    public CommonResult<String> addDirectory(@RequestBody DocDirectorySaveReqVO saveReqVO) {
        docDirectoryService.addDirectory(saveReqVO);
        CommonResult<String> stringCommonResult = new CommonResult<>();
        stringCommonResult.setData("目录新增成功");
        return stringCommonResult;
    }

    //删除目录
    @DeleteMapping("/deleteDirectory")
    public CommonResult<String> deleteDirectory(@RequestParam Integer id) {
        docDirectoryService.deleteDirectory(id);
        CommonResult<String> stringCommonResult = new CommonResult<>();
        stringCommonResult.setData("删除成功");
        return stringCommonResult;
    }
    /**
     * 修改目录
     */
    @PostMapping("/updateDirectory")
    public CommonResult<String> updateDirectory(@RequestBody DocDirectorySaveReqVO saveReqVO) {
        docDirectoryService.updateDirectory(saveReqVO);
        CommonResult<String> stringCommonResult = new CommonResult<>();
        stringCommonResult.setData("修改成功");
        return stringCommonResult;
    }

}
