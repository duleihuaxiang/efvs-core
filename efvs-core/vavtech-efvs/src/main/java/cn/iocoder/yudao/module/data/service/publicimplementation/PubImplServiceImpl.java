package cn.iocoder.yudao.module.data.service.publicimplementation;

import cn.iocoder.yudao.module.data.dal.mysql.publicimplementation.PubImplMapper;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;

@Service
@Validated
public class PubImplServiceImpl implements PubImplService{
    @Resource
    private PubImplMapper pubImplMapper;

    @Override
    public void updateTaskIdByProcessInstanceId(String taskId, String processInstanceId, String tableName) {
        pubImplMapper.updateTaskIdByProcessInstanceId(taskId, processInstanceId, tableName);
    }
}
