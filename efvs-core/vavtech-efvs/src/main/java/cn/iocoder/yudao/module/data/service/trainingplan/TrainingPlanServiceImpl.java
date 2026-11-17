package cn.iocoder.yudao.module.data.service.trainingplan;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import cn.iocoder.yudao.module.data.controller.admin.trainingplan.vo.*;
import cn.iocoder.yudao.module.data.dal.dataobject.trainingplan.TrainingPlanDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;

import cn.iocoder.yudao.module.data.dal.mysql.trainingplan.TrainingPlanMapper;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.diffList;
import static cn.iocoder.yudao.module.data.enums.ErrorCodeConstants.*;

/**
 * 培训计划 Service 实现类
 *
 * @author admin
 */
@Service
@Validated
public class TrainingPlanServiceImpl extends ServiceImpl<TrainingPlanMapper, TrainingPlanDO> implements TrainingPlanService {

    @Resource
    private TrainingPlanMapper trainingPlanMapper;

    @Override
    public Integer createTrainingPlan(TrainingPlanSaveReqVO createReqVO) {
        // 插入
        TrainingPlanDO trainingPlan = BeanUtils.toBean(createReqVO, TrainingPlanDO.class);
        trainingPlanMapper.insert(trainingPlan);

        // 返回
        return trainingPlan.getId();
    }

    @Override
    public void updateTrainingPlan(TrainingPlanSaveReqVO updateReqVO) {
        // 校验存在
        validateTrainingPlanExists(updateReqVO.getId());
        // 更新
        TrainingPlanDO updateObj = BeanUtils.toBean(updateReqVO, TrainingPlanDO.class);
        trainingPlanMapper.updateById(updateObj);
    }

    @Override
    public void deleteTrainingPlan(Integer id) {
        // 校验存在
        validateTrainingPlanExists(id);
        // 删除
        trainingPlanMapper.deleteById(id);
    }

    @Override
    public void deleteTrainingPlanListByIds(List<Integer> ids) {
        // 删除
        trainingPlanMapper.deleteByIds(ids);
        }

    private void validateTrainingPlanExists(Integer id) {
        if (trainingPlanMapper.selectById(id) == null) {
            throw exception(TRAINING_PLAN_NOT_EXISTS);
        }
    }

    @Override
    public TrainingPlanDO getTrainingPlan(Integer id) {
        return trainingPlanMapper.selectById(id);
    }

    @Override
    public PageResult<TrainingPlanDO> getTrainingPlanPage(TrainingPlanPageReqVO pageReqVO) {
        PageResult<TrainingPlanDO> trainingPlanDOPageResult = trainingPlanMapper.selectPage(pageReqVO);
        // 判断两个都为空
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        PageResult<TrainingPlanDO> result = new PageResult<>();
        if (Objects.nonNull(pageReqVO.getTrainingStartTime()) && Objects.nonNull(pageReqVO.getTrainingEndTime())) {
            List<TrainingPlanDO> trainingPlanDOS = new ArrayList<>();
            trainingPlanDOPageResult.getList().forEach(trainingPlanDO -> {
                // 使用 Hutool 的 DateUtil
//                Date trainingStartTime = DateUtil.parse(trainingPlanDO.getTrainingStartTime(), "yyyy-MM-dd");
//                Date trainingEndTime = DateUtil.parse(trainingPlanDO.getTrainingEndTime(), "yyyy-MM-dd");
                LocalDate trainingStartTime = LocalDate.parse(trainingPlanDO.getTrainingStartTime().substring(0, 10), formatter);
                LocalDate trainingEndTime = LocalDate.parse(trainingPlanDO.getTrainingEndTime().substring(0, 10), formatter);
                if (LocalDate.parse(pageReqVO.getTrainingStartTime().substring(0, 10), formatter).isBefore(trainingStartTime)
                || LocalDate.parse(pageReqVO.getTrainingStartTime().substring(0, 10), formatter).isEqual(trainingStartTime)) {
                    if(LocalDate.parse(pageReqVO.getTrainingEndTime().substring(0, 10), formatter).isAfter(trainingEndTime)
                    || LocalDate.parse(pageReqVO.getTrainingEndTime().substring(0, 10), formatter).isEqual(trainingEndTime)){
                        trainingPlanDOS.add(trainingPlanDO);
                    }
                }
            });
            result.setList(trainingPlanDOS);
            return result;
        }
        return trainingPlanDOPageResult;
    }

    @Override
    public TrainingPlanDO getTrainingPlanByProcessInstanceId(String processInstanceId) {
        TrainingPlanDO trainingPlanDO = trainingPlanMapper.selectOne(TrainingPlanDO::getProcessInstanceId, processInstanceId);
        Integer projectLeader = trainingPlanDO.getProjectLeader();
        String projectLeaderNickName = trainingPlanMapper.selectOneByProjectLeader(projectLeader);
        trainingPlanDO.setProjectLeaderNickName(projectLeaderNickName);
        return trainingPlanDO;
    }
    @Override
    public Map<Integer,String> gettrainingByid(List<Integer> ids) {
        Long requiredTenantId = TenantContextHolder.getRequiredTenantId();

        // 获取原始查询结果
        List<TrainingPlanVO> nicknames = trainingPlanMapper.gettrainingListByid(ids, requiredTenantId);
        Map<Integer, String> collect = nicknames.stream()
                .collect(Collectors.toMap(
                        TrainingPlanVO::getId,
                        Function.identity(),              // 先收集完整的 VO 对象
                        (existing, replacement) -> existing
                ))
                .values()                           // 获取去重后的 VO 列表
                .stream()
                .collect(Collectors.toMap(
                        TrainingPlanVO::getId,
                        TrainingPlanVO::getNickName
                ));

        return collect;
    }

    @Override
    public List<TrainingPlanDO> getTrainingPlansByProcessInstanceIds(Set<String> processInstanceIds) {
        return trainingPlanMapper.selectList(TrainingPlanDO::getProcessInstanceId, processInstanceIds);
    }

    @Override
    public Map<Integer, String> getUserNickNamesByIds(List<Integer> leaderIds) {
       List<TrainingPlanVO> userNickNames =  trainingPlanMapper.getUserNickNamesByIds(leaderIds);
        Map<Integer, String> userIdToNickNameMap = userNickNames.stream()
                // 过滤掉可能的空对象或空属性，避免空指针
                .filter(vo -> vo != null && vo.getId() != null && vo.getNickName() != null)
                // 转换为Map：key=id，value=nickName
                .collect(Collectors.toMap(
                        TrainingPlanVO::getId,    // 取VO的id作为Map的key
                        TrainingPlanVO::getNickName,  // 取VO的nickName作为Map的value
                        // 解决重复key的冲突（如果有重复ID，保留第一个值）
                        (existingValue, newValue) -> existingValue
                ));
        return userIdToNickNameMap;
    }
}
