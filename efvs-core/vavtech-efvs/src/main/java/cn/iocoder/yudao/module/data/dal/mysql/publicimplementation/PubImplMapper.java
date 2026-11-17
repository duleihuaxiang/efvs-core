package cn.iocoder.yudao.module.data.dal.mysql.publicimplementation;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface PubImplMapper{
    @Update("update ${tableName} set task_id = #{taskId} where process_instance_id = #{processInstanceId}")
    void updateTaskIdByProcessInstanceId(@Param("taskId") String taskId,@Param("processInstanceId") String processInstanceId,@Param("tableName") String tableName);
}
