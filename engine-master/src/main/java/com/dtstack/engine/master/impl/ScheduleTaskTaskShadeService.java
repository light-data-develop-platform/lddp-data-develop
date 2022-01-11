/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dtstack.engine.master.impl;

import com.alibaba.fastjson.JSONObject;
import com.dtstack.engine.common.enums.DisplayDirect;
import com.dtstack.engine.common.env.EnvironmentContext;
import com.dtstack.engine.domain.ScheduleTaskShade;
import com.dtstack.engine.domain.ScheduleTaskTaskShade;
import com.dtstack.engine.mapper.ScheduleTaskTaskShadeDao;
import com.dtstack.engine.master.druid.DtDruidRemoveAbandoned;
import com.dtstack.engine.master.vo.ScheduleTaskVO;
import com.dtstack.engine.master.vo.task.SaveTaskTaskVO;
import com.dtstack.engine.common.enums.EScheduleJobType;
import com.dtstack.engine.pluginapi.exception.ExceptionUtil;
import com.dtstack.engine.pluginapi.exception.RdosDefineException;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * company: www.dtstack.com
 * author: toutian
 * create: 2019/10/22
 */
@Service
public class ScheduleTaskTaskShadeService {

    private static final Long IS_WORK_FLOW_SUBNODE = 0L;

    private static final Logger LOGGER = LoggerFactory.getLogger(ScheduleTaskTaskShadeService.class);

    @Autowired
    private ScheduleTaskTaskShadeDao scheduleTaskTaskShadeDao;

    @Autowired
    private ScheduleTaskShadeService taskShadeService;

    @Autowired
    private EnvironmentContext context;


    public void clearDataByTaskId( Long taskId,Integer appType) {
        scheduleTaskTaskShadeDao.deleteByTaskId(taskId,appType);
    }

    @Transactional(rollbackFor = Exception.class)
    @DtDruidRemoveAbandoned
    public SaveTaskTaskVO saveTaskTaskList(String taskLists) {
        if(StringUtils.isBlank(taskLists)){
            return SaveTaskTaskVO.save();
        }
        try {
            List<ScheduleTaskTaskShade> taskTaskList = JSONObject.parseArray(taskLists, ScheduleTaskTaskShade.class);
            if(CollectionUtils.isEmpty(taskTaskList)){
                return SaveTaskTaskVO.save();
            }

            // 保存时成环检测
            for (ScheduleTaskTaskShade scheduleTaskTaskShade : taskTaskList) {
                List<ScheduleTaskTaskShade> shades = Lists.newArrayList(taskTaskList);
                if (checkTaskTaskIsLoop(scheduleTaskTaskShade, shades)) {
                    return SaveTaskTaskVO.noSave("任务依赖成环，无法提交");
                }
            }

            Map<String, ScheduleTaskTaskShade> keys = new HashMap<>(16);
            // 去重
            for (ScheduleTaskTaskShade scheduleTaskTaskShade : taskTaskList) {
                keys.put(String.format("%s.%s.%s", scheduleTaskTaskShade.getTaskId(), scheduleTaskTaskShade.getParentTaskId(), null), scheduleTaskTaskShade);
                Preconditions.checkNotNull(scheduleTaskTaskShade.getTaskId());
                // 清除原来关系
                scheduleTaskTaskShadeDao.deleteByTaskId(scheduleTaskTaskShade.getTaskId(),null);
            }
            // 保存现有任务关系
            for (ScheduleTaskTaskShade taskTaskShade : keys.values()) {
                scheduleTaskTaskShadeDao.insert(taskTaskShade);
            }
        } catch (Exception e) {
            LOGGER.error("saveTaskTaskList error:{}", ExceptionUtil.getErrorMessage(e));
            throw new RdosDefineException("保存任务依赖列表异常");
        }
        return SaveTaskTaskVO.save();
    }

    /**
     * 判断是否成环
     *
     * @param scheduleTaskTaskShade
     * @return
     */
    private Boolean checkTaskTaskIsLoop(ScheduleTaskTaskShade scheduleTaskTaskShade,List<ScheduleTaskTaskShade> taskTask) {
        if (scheduleTaskTaskShade == null || scheduleTaskTaskShade.getParentTaskId() == null) {
            // 节点或者是父节点是null，都表示没有成环
            return Boolean.FALSE;
        }
        Map<Side,Set<String>> sideMap = Maps.newHashMap();
        Side side = new Side();
//        side.setDown(scheduleTaskTaskShade.getTaskKey());
//        side.setUp(scheduleTaskTaskShade.getParentTaskKey());
//        sideMap.put(side,Sets.newHashSet(scheduleTaskTaskShade.getTaskKey()));

        // 向上查询,向上查询会查询出自己的边
        Integer loopUp = 0;
        List<ScheduleTaskTaskShade> scheduleParentTaskTaskShades = addParentTaskTask(Lists.newArrayList(),taskTask);
        while (CollectionUtils.isNotEmpty(scheduleParentTaskTaskShades)) {
            List<String> parentKeys = Lists.newArrayList();

            if (setKeyAndJudgedLoop(sideMap, scheduleParentTaskTaskShades, parentKeys,Boolean.FALSE)) {
                return Boolean.TRUE;
            }

            if (CollectionUtils.isEmpty(parentKeys)) {
                // 说明集合里面全部都是头节点
                break;
            }
            loopUp++;

            LOGGER.info("loopUp:{} select key:{}",loopUp,parentKeys);
            scheduleParentTaskTaskShades = addParentTaskTask(parentKeys,taskTask);
        }

        // 向下查询
        Integer loopUnder = 0;
        List<ScheduleTaskTaskShade> scheduleChildTaskTaskShades = addChildTaskTask(Lists.newArrayList(), taskTask);
        while (CollectionUtils.isNotEmpty(scheduleChildTaskTaskShades)) {
            List<String> childKeys = Lists.newArrayList();

            if (setKeyAndJudgedLoop(sideMap, scheduleChildTaskTaskShades, childKeys,Boolean.TRUE)) {
                return Boolean.TRUE;
            }

            if (CollectionUtils.isEmpty(childKeys)) {
                // 说明集合里面全部都是头节点
                break;
            }
            loopUnder++;

            LOGGER.info("loopUnder:{} select key:{}",loopUnder,childKeys);
            scheduleChildTaskTaskShades = addChildTaskTask(childKeys, taskTask);

        }

        return Boolean.FALSE;
    }

    static class Side {
        private String up;
        private String down;

        public String getUp() {
            return up;
        }

        public void setUp(String up) {
            this.up = up;
        }

        public String getDown() {
            return down;
        }

        public void setDown(String down) {
            this.down = down;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Side side = (Side) o;
            return up.equals(side.up) &&
                    down.equals(side.down);
        }

        @Override
        public int hashCode() {
            return Objects.hash(up, down);
        }
    }

    private boolean setKeyAndJudgedLoop(Map<Side, Set<String>> sideMap, List<ScheduleTaskTaskShade> scheduleChildTaskTaskShades, List<String> parentKeys, Boolean isChild) {
        Map<Side, Set<String>> newSideMap = Maps.newHashMap(sideMap);

        if (isChild) {
            // 向下查询
//            Map<String, List<ScheduleTaskTaskShade>> parentKey = scheduleChildTaskTaskShades.stream()
//                    .filter(scheduleTaskTaskShade -> StringUtils.isNotBlank())
//                    .collect(Collectors.groupingBy(ScheduleTaskTaskShade::getParentTaskKey));
//
//            for (Map.Entry<Side, Set<String>> entry : newSideMap.entrySet()) {
//                Side side = entry.getKey();
//                String taskKey = side.getDown();
//                List<ScheduleTaskTaskShade> taskTaskShades = parentKey.get(taskKey);
//
//                if (CollectionUtils.isNotEmpty(taskTaskShades)) {
//
//                    for (ScheduleTaskTaskShade taskTaskShade : taskTaskShades) {
//                        Side sideSon = new Side();
//                        Set<String> element = entry.getValue();
//
//                        sideSon.setUp(side.up);
//                        sideSon.setDown(taskTaskShade.getTaskKey());
//
//                        Set<String> newElement = Sets.newHashSet(element);
//
//                        if (!newElement.add(taskTaskShade.getTaskKey())) {
//                            // 添加不进去，说明边重复了，已经成环
//                            LOGGER.warn("saveTaskTask is loop,loop:{} -------- repeat side:{}, map: {}", taskTaskShade.getTaskKey(),newElement, sideMap);
//                            return Boolean.TRUE;
//                        }
//                        sideMap.remove(side);
//                        sideMap.put(sideSon,newElement);
//                        parentKeys.add(taskTaskShade.getTaskKey());
//                    }
//                }
//            }
        } else {
            // 向上查询
//            Map<String, List<ScheduleTaskTaskShade>> parentKey = scheduleChildTaskTaskShades.stream()
//                    .filter(scheduleTaskTaskShade -> StringUtils.isNotBlank(scheduleTaskTaskShade.getParentTaskKey()))
//                    .collect(Collectors.groupingBy(ScheduleTaskTaskShade::getParentTaskKey));
//
//            for (Map.Entry<Side, Set<String>> entry : newSideMap.entrySet()) {
//                Side side = entry.getKey();
//                String taskKey = side.getUp();
//
//                List<ScheduleTaskTaskShade> taskTaskShades = parentKey.get(taskKey);
//
//                if (CollectionUtils.isNotEmpty(taskTaskShades)) {
//                    for (ScheduleTaskTaskShade taskTaskShade : taskTaskShades) {
//                        Side sideSon = new Side();
//                        Set<String> element = entry.getValue();
//
//                        sideSon.setUp(taskTaskShade.getParentTaskKey());
//                        sideSon.setDown(side.down);
//
//                        Set<String> newElement = Sets.newHashSet(element);
//
//                        if (!newElement.add(taskTaskShade.getParentTaskKey())) {
//                            // 添加不进去，说明边重复了，已经成环
//                            LOGGER.warn("saveTaskTask is loop,loop:{} -------- repeat side:{}, map: {}", taskTaskShade.getTaskKey(),newElement, sideMap);
//                            return Boolean.TRUE;
//                        }
//                        sideMap.remove(side);
//                        sideMap.put(sideSon,newElement);
//                        parentKeys.add(taskTaskShade.getParentTaskKey());
//                    }
//                }
//            }
        }

        // 未成环
        return Boolean.FALSE;
    }

    private List<ScheduleTaskTaskShade> addChildTaskTask(List<String> childKey, List<ScheduleTaskTaskShade> taskTasks) {
        List<ScheduleTaskTaskShade> scheduleChildTaskTaskShades = scheduleTaskTaskShadeDao.listParentTaskKeys(childKey);
        List<String> sides = Lists.newArrayList();
        if (CollectionUtils.isNotEmpty(scheduleChildTaskTaskShades)) {
//            sides = scheduleChildTaskTaskShades.stream().map(taskShade -> taskShade.getTaskKey()+"&"+taskShade.getParentTaskKey()).collect(Collectors.toList());
        }

        if (CollectionUtils.isNotEmpty(taskTasks)) {
            List<String> finalSides = sides;
//            List<ScheduleTaskTaskShade> shades = taskTasks.stream()
//                    .filter(taskTask -> childKey.contains(taskTask.getParentTaskKey()) && !finalSides.contains(taskTask.getTaskKey()+"&"+taskTask.getParentTaskKey()))
//                    .collect(Collectors.toList());
//            scheduleChildTaskTaskShades.addAll(shades);
        }
        return scheduleChildTaskTaskShades;
    }

    private List<ScheduleTaskTaskShade> addParentTaskTask(List<String> parentKeys, List<ScheduleTaskTaskShade> taskTasks) {
        List<ScheduleTaskTaskShade> taskTaskShades = scheduleTaskTaskShadeDao.listTaskKeys(parentKeys);
        List<String> sides = Lists.newArrayList();
        if (CollectionUtils.isNotEmpty(taskTaskShades)) {
//            sides = taskTaskShades.stream().map(taskShade -> taskShade.getTaskKey()+"&"+taskShade.getParentTaskKey()).collect(Collectors.toList());
        }

        if (CollectionUtils.isNotEmpty(taskTasks)) {
//            List<String> finalSides = sides;
//            List<ScheduleTaskTaskShade> shades = taskTasks.stream()
//                    .filter(taskTask -> parentKeys.contains(taskTask.getTaskKey()) && !finalSides.contains(taskTask.getTaskKey()+"&"+taskTask.getParentTaskKey()))
//                    .collect(Collectors.toList());
//            taskTaskShades.addAll(shades);
        }
        return taskTaskShades;
    }

    public List<ScheduleTaskTaskShade> getAllParentTask( Long taskId,Integer appType) {
        return scheduleTaskTaskShadeDao.listParentTask(taskId,appType);
    }


    public com.dtstack.engine.master.impl.vo.ScheduleTaskVO displayOffSpring(Long taskId,
                                                                             Long projectId,
                                                                             Integer level,
                                                                             Integer directType, Integer appType) {
        ScheduleTaskShade task = taskShadeService.getBatchTaskById(taskId);
        if(null == task){
            return null;
        }

        if (level == null || level < 1) {
            level = 1;
        }
        if( level > context.getMaxLevel()){
            level = context.getMaxLevel();
        }
        if(directType == null){
            directType = 0;
        }
        if(context.getUseOptimize()) {
            return this.getOffSpringNew(task, level, directType, projectId, appType, new ArrayList<>());
        }else{
            return this.getOffSpring(task,level,directType,projectId,appType);
        }
    }

    /**
     * 展开依赖节点,优化后
     * 0 展开上下游, 1:展开上游 2:展开下游
     *
     * @author toutian
     */
    private com.dtstack.engine.master.impl.vo.ScheduleTaskVO getOffSpringNew(ScheduleTaskShade taskShade, int level,
                                                                             Integer directType, Long currentProjectId, Integer appType, List<String> taskIdRelations) {
        //1、如果是工作流子节点,则展开全部工作流子节点
        if (!taskShade.getTaskType().equals(EScheduleJobType.WORK_FLOW.getVal()) &&
                !taskShade.getFlowId().equals(IS_WORK_FLOW_SUBNODE)) {
            //若为工作流子节点，则展开工作流全部子节点
            return getOnlyAllFlowSubTasksNew(taskShade.getFlowId(), appType);
        }
        com.dtstack.engine.master.impl.vo.ScheduleTaskVO vo = new com.dtstack.engine.master.impl.vo.ScheduleTaskVO(taskShade, true);
        if (EScheduleJobType.WORK_FLOW.getVal().equals(taskShade.getTaskType())) {
            //2、如果是工作流，则获取工作流子节点,包括工作流本身
            //构建父节点信息
            com.dtstack.engine.master.impl.vo.ScheduleTaskVO parentNode = new com.dtstack.engine.master.impl.vo.ScheduleTaskVO(taskShade, true);
            com.dtstack.engine.master.impl.vo.ScheduleTaskVO onlyAllFlowSubTasksNew = getOnlyAllFlowSubTasksNew(taskShade.getTaskId(), null);
            parentNode.setSubTaskVOS(Arrays.asList(onlyAllFlowSubTasksNew));
            vo.setSubNodes(parentNode);
        }
            vo.setExistsOnRule(Boolean.FALSE);


        if (level == 0) {
            //控制最多展示多少层，防止一直循环。
            return vo;
        }
        level--;
        List<ScheduleTaskTaskShade> taskTasks = null;
        List<ScheduleTaskTaskShade> childTaskTasks = null;
        //展开上游节点
        if (DisplayDirect.FATHER_CHILD.getType().equals(directType) || DisplayDirect.FATHER.getType().equals(directType)) {
            taskTasks = scheduleTaskTaskShadeDao.listParentTask(taskShade.getTaskId(), null);
            if (checkIsLoop(taskIdRelations, taskTasks)) {
                //成环了，直接返回
                return vo;
            }
        }
        //展开下游节点
        if (DisplayDirect.FATHER_CHILD.getType().equals(directType) || DisplayDirect.CHILD.getType().equals(directType)) {
            childTaskTasks = scheduleTaskTaskShadeDao.listChildTask(taskShade.getTaskId(), null);
            if (checkIsLoop(taskIdRelations, childTaskTasks)) {
                //成环了，直接返回
                return vo;
            }
        }
        if (CollectionUtils.isEmpty(taskTasks) && CollectionUtils.isEmpty(childTaskTasks)) {
            return vo;
        }
        List<ScheduleTaskVO> parentTaskList = null;
        List<ScheduleTaskVO> childTaskList = null;
        List<ScheduleTaskVO> taskRuleList = Lists.newArrayList();
        if (!CollectionUtils.isEmpty(taskTasks)) {
            //向上展开
//            Map<Integer, List<ScheduleTaskTaskShade>> listMap = taskTasks.stream().collect(Collectors.groupingBy(ScheduleTaskTaskShade::getParentAppType));
//            parentTaskList = getRefTaskNew(listMap, level, DisplayDirect.FATHER.getType(), currentProjectId, taskIdRelations,taskRuleList);
//            if (CollectionUtils.isNotEmpty(parentTaskList) && parentTaskList.get(0) != null) {
//                vo.setTaskVOS(parentTaskList);
//            }
//
//            if (CollectionUtils.isNotEmpty(taskRuleList) && taskRuleList.get(0) != null) {
//                vo.setTaskRuleList(taskRuleList);
//            }
        }
        if (!CollectionUtils.isEmpty(childTaskTasks)) {
            //向下展开
//            Map<Integer, List<ScheduleTaskTaskShade>> listMap = childTaskTasks.stream().collect(Collectors.groupingBy(ScheduleTaskTaskShade::getAppType));
//            childTaskList = getRefTaskNew(listMap, level, DisplayDirect.CHILD.getType(), currentProjectId, taskIdRelations,taskRuleList);
//            if (CollectionUtils.isNotEmpty(childTaskList) && childTaskList.get(0) != null) {
//                vo.setSubTaskVOS(childTaskList);
//            }
//
//            if (CollectionUtils.isNotEmpty(taskRuleList) && taskRuleList.get(0) != null) {
//                vo.setTaskRuleList(taskRuleList);
//            }
        }
        return vo;
    }

    /**
     * @author newman
     * @Description 检测任务是否成环
     * @Date 2021/1/6 8:23 下午
     * @param taskIdRelations:
     * @param taskTasks:
     * @return: void
     **/
    private Boolean checkIsLoop(List<String> taskIdRelations, List<ScheduleTaskTaskShade> taskTasks) {

        if(CollectionUtils.isNotEmpty(taskTasks)) {
            for (ScheduleTaskTaskShade taskTask : taskTasks) {
//                String taskRelation = taskTask.getTaskId() + "&" + taskTask.getAppType() + "-" + taskTask.getParentTaskId() + "&" + taskTask.getParentAppType();
//                if (taskIdRelations.contains(taskRelation)) {
//                    LOGGER.error("该任务成环了,taskRelation:{} 所有的关系视图:{}", taskRelation,taskIdRelations.toString());
//                    return true;
//                } else {
//                    taskIdRelations.add(taskRelation);
//                }
            }
        }
        return false;
    }

    /**
     * 展开依赖节点
     * 0 展开上下游, 1:展开上游 2:展开下游
     * @author toutian
     */
    private com.dtstack.engine.master.impl.vo.ScheduleTaskVO getOffSpring(ScheduleTaskShade taskShade, int level, Integer directType, Long currentProjectId, Integer appType) {

        com.dtstack.engine.master.impl.vo.ScheduleTaskVO vo = new com.dtstack.engine.master.impl.vo.ScheduleTaskVO(taskShade, true);
        if (EScheduleJobType.WORK_FLOW.getVal().equals(taskShade.getTaskType())) {
            //如果是工作流，则获取工作流及其子节点
            com.dtstack.engine.master.impl.vo.ScheduleTaskVO subTaskVO = getAllFlowSubTasks(taskShade.getTaskId(),null);
            vo.setSubNodes(subTaskVO);
        }
        if (level == 0) {
            //控制最多展示多少层，防止一直循环。
            return vo;
        }
        level--;
        List<ScheduleTaskTaskShade> taskTasks = null;
        List<ScheduleTaskTaskShade> childTaskTasks = null;
        if(taskShade.getTaskType().intValue() != EScheduleJobType.WORK_FLOW.getVal() &&
                !taskShade.getFlowId().equals(IS_WORK_FLOW_SUBNODE)){
            //若为工作流子节点，则展开工作流全部子节点
            return getOnlyAllFlowSubTasks(taskShade.getFlowId(),appType);
        }
        //展开上游节点
        if(DisplayDirect.FATHER_CHILD.getType().equals(directType) || DisplayDirect.FATHER.getType().equals(directType)){
            taskTasks = scheduleTaskTaskShadeDao.listParentTask(taskShade.getTaskId(),null);
        }
        //展开下游节点
        if(DisplayDirect.FATHER_CHILD.getType().equals(directType) || DisplayDirect.CHILD.getType().equals(directType)){
            childTaskTasks = scheduleTaskTaskShadeDao.listChildTask(taskShade.getTaskId(),null);
        }
        if (CollectionUtils.isEmpty(taskTasks) && CollectionUtils.isEmpty(childTaskTasks)) {
            return vo;
        }
        List<ScheduleTaskVO> parentTaskList = null;
        List<ScheduleTaskVO> childTaskList = null;
        List<ScheduleTaskVO> ruleTaskList = Lists.newArrayList();
        if(!CollectionUtils.isEmpty(taskTasks)){
            //向上展开
            Set<Long> taskIds = new HashSet<>(taskTasks.size());
            taskTasks.forEach(taskTask -> taskIds.add(taskTask.getParentTaskId()));
            parentTaskList = getRefTask(taskIds, level, DisplayDirect.FATHER.getType(), currentProjectId,appType,ruleTaskList);
            if(parentTaskList != null){
                vo.setTaskVOS(parentTaskList);
            }
        }
        if(!CollectionUtils.isEmpty(childTaskTasks)){
            //向下展开
            Set<Long> taskIds = new HashSet<>(childTaskTasks.size());
            childTaskTasks.forEach(taskTask -> taskIds.add(taskTask.getTaskId()));
            childTaskList = getRefTask(taskIds, level, DisplayDirect.CHILD.getType(), currentProjectId,appType,ruleTaskList);
            if(childTaskList != null){
                vo.setSubTaskVOS(childTaskList);
            }
        }
        if (CollectionUtils.isEmpty(ruleTaskList)) {
            vo.setTaskRuleList(ruleTaskList);
        }

        return vo;
    }

    public List<ScheduleTaskVO> getRefTask(Set<Long> taskIds, int level, Integer directType, Long currentProjectId, Integer appType, List<ScheduleTaskVO> ruleTaskList){

        if (CollectionUtils.isEmpty(ruleTaskList)) {
            ruleTaskList = Lists.newArrayList();
        }

        //获得所有父节点task
        List<ScheduleTaskShade> tasks = taskShadeService.getTaskByIds(new ArrayList<>(taskIds),appType);
        if (CollectionUtils.isEmpty(tasks)) {
            return null;
        }
        List<ScheduleTaskVO> refTaskVoList = new ArrayList<>(tasks.size());
        return refTaskVoList;
    }

    /**
     * 获取工作流全部子节点信息 -- 依赖树
     *  ps- 不包括工作流父节点
     * 优化新
     * @param flowId 工作流父节点id
     * @return
     */
    private com.dtstack.engine.master.impl.vo.ScheduleTaskVO getOnlyAllFlowSubTasksNew(Long flowId, Integer appType) {

        //工作流最多展开多少层
        Integer level = context.getWorkFlowLevel();
        com.dtstack.engine.master.impl.vo.ScheduleTaskVO vo = new com.dtstack.engine.master.impl.vo.ScheduleTaskVO();
        //获取工作流顶部节点
        ScheduleTaskShade beginTaskShade = taskShadeService.getWorkFlowTopNode(flowId,appType);
        if(beginTaskShade!=null) {
            //展开工作流全部节点，不包括工作流父节点
            vo = getFlowWorkOffSpringNew(beginTaskShade,appType,level,new ArrayList<>());
        }
        return vo;
    }

    /**
     * 获取工作流全部子节点信息 -- 依赖树
     *  ps- 不包括工作流父节点
     *
     * @param flowId 工作流父节点id
     * @return
     */
    private com.dtstack.engine.master.impl.vo.ScheduleTaskVO getOnlyAllFlowSubTasks(Long flowId, Integer appType) {

        com.dtstack.engine.master.impl.vo.ScheduleTaskVO vo = new com.dtstack.engine.master.impl.vo.ScheduleTaskVO();
        //获取工作流顶部节点
        ScheduleTaskShade beginTaskShade = taskShadeService.getWorkFlowTopNode(flowId,appType);
        if(beginTaskShade!=null) {
            //展开工作流全部节点，不包括工作流父节点
            Integer workFlowLevel = context.getWorkFlowLevel();
            vo = getFlowWorkOffSpring(beginTaskShade, 1,appType,workFlowLevel);
        }
        return vo;
    }




    /**
     * 查询工作流全部节点信息 -- 依赖树
     *
     * @param taskId
     * @return
     */
    public com.dtstack.engine.master.impl.vo.ScheduleTaskVO getAllFlowSubTasks(Long taskId, Integer appType) {

        //工作流任务信息
        ScheduleTaskShade task = taskShadeService.getBatchTaskById(taskId);
        //构建父节点信息
        com.dtstack.engine.master.impl.vo.ScheduleTaskVO parentNode = new com.dtstack.engine.master.impl.vo.ScheduleTaskVO(task, true);
        com.dtstack.engine.master.impl.vo.ScheduleTaskVO vo = new com.dtstack.engine.master.impl.vo.ScheduleTaskVO();
        //获取工作流最顶层结点
        ScheduleTaskShade beginTaskShade = taskShadeService.getWorkFlowTopNode(taskId,appType);
        if(beginTaskShade!=null) {
            //获取工作流下游结点
            Integer workFlowLevel = context.getWorkFlowLevel();
            vo = getFlowWorkOffSpring(beginTaskShade, 1,appType,workFlowLevel);
        }
        parentNode.setSubTaskVOS(Arrays.asList(vo));
        return parentNode;
    }

    /**
     * 向下展开工作流全部节点,增加max参数，万一循环依赖,优化的新方法
     * 最多查询10层就返回，防止内存溢出
     * @param taskShade
     * @param level
     * @param taskIdRelations 任务id关联列表，用来做成环检测
     * @return
     */
    private com.dtstack.engine.master.impl.vo.ScheduleTaskVO getFlowWorkOffSpringNew(ScheduleTaskShade taskShade, Integer appType, int level, List<String> taskIdRelations) {


        com.dtstack.engine.master.impl.vo.ScheduleTaskVO vo = new com.dtstack.engine.master.impl.vo.ScheduleTaskVO(taskShade, true);
        //查询子任务列表
        List<ScheduleTaskTaskShade> childTaskTasks = scheduleTaskTaskShadeDao.listChildTask(taskShade.getTaskId(),null);
        if (CollectionUtils.isEmpty(childTaskTasks)) {
            return vo;
        }
        if(checkIsLoop(taskIdRelations,childTaskTasks)){
            //如果成环，则返回null
            return vo;
        }
        if(level<=0){
            return vo;
        }
        //获取子任务taskId集合
        Set<Long> taskIds = childTaskTasks.stream().map(ScheduleTaskTaskShade::getTaskId).collect(Collectors.toSet());
        level--;
        //获得所有父节点task
        List<ScheduleTaskShade> tasks = taskShadeService.getTaskByIds(new ArrayList<>(taskIds),appType);
        if (CollectionUtils.isEmpty(tasks)) {
            return vo;
        }
        List<ScheduleTaskVO> refTaskVoList = new ArrayList<>(tasks.size());
        for (ScheduleTaskShade task : tasks) {
            refTaskVoList.add(this.getFlowWorkOffSpringNew(task,appType,level,taskIdRelations));
        }
        if (CollectionUtils.isNotEmpty(refTaskVoList) && refTaskVoList.get(0)!=null) {
            vo.setSubTaskVOS(refTaskVoList);
        }
        return vo;
    }

    /**
     * 向下展开工作流全部节点,增加max参数，万一循环依赖
     * 最多查询10层就返回，防止内存溢出
     * @param taskShade
     * @param level
     * @return
     */
    private com.dtstack.engine.master.impl.vo.ScheduleTaskVO getFlowWorkOffSpring(ScheduleTaskShade taskShade, int level, Integer appType, int max) {

        if(max<=0){
            return null;
        }
        com.dtstack.engine.master.impl.vo.ScheduleTaskVO vo = new com.dtstack.engine.master.impl.vo.ScheduleTaskVO(taskShade, true);
        List<ScheduleTaskTaskShade> childTaskTasks = null;
        //查询子任务列表
        childTaskTasks = scheduleTaskTaskShadeDao.listChildTask(taskShade.getTaskId(),null);
        if (CollectionUtils.isEmpty(childTaskTasks)) {
            return vo;
        }
        Set<Long> taskIds = new HashSet<>(childTaskTasks.size());
        //获取子任务id集合
        childTaskTasks.forEach(taskTask -> taskIds.add(taskTask.getTaskId()));
        max--;
        List<ScheduleTaskVO> childTaskList = getFlowWorkSubTasksRefTask(taskIds, level, DisplayDirect.CHILD.getType(),appType,max);
        if (childTaskList != null) {
            vo.setSubTaskVOS(childTaskList);
        }
        return vo;
    }

    /**
     * @author newman
     * @Description 获取所有工作流子节点的子任务
     * @Date 2020-12-17 10:25
     * @param taskIds:
     * @param level:
     * @param directType:
     * @param appType:
     * @return: java.util.List<com.dtstack.engine.master.vo.ScheduleTaskVO>
     **/
    public List<ScheduleTaskVO> getFlowWorkSubTasksRefTask(Set<Long> taskIds, int level, Integer directType, Integer appType,int max) {

        //获得所有父节点task
        List<ScheduleTaskShade> tasks = taskShadeService.getTaskByIds(new ArrayList<>(taskIds),appType);
        if (CollectionUtils.isEmpty(tasks)) {
            return null;
        }
        List<ScheduleTaskVO> refTaskVoList = new ArrayList<>(tasks.size());
        for (ScheduleTaskShade task : tasks) {
            refTaskVoList.add(this.getFlowWorkOffSpring(task, level,appType,max));
        }

        return refTaskVoList;
    }

}