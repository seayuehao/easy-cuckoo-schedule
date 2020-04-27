package org.ecs.schedule.controller.jobinfo;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.ecs.schedule.controller.BaseController;
import org.ecs.schedule.domain.exec.CuckooJobDetail;
import org.ecs.schedule.domain.exec.CuckooJobExtend;
import org.ecs.schedule.domain.exec.CuckooJobGroup;
import org.ecs.schedule.enums.CuckooBooleanFlag;
import org.ecs.schedule.enums.CuckooJobExecStatus;
import org.ecs.schedule.enums.CuckooJobExecType;
import org.ecs.schedule.enums.CuckooJobStatus;
import org.ecs.schedule.enums.CuckooJobTriggerType;
import org.ecs.schedule.exception.BaseException;
import org.ecs.schedule.qry.job.JobInfoQry;
import org.ecs.schedule.service.job.CuckooGroupService;
import org.ecs.schedule.service.job.CuckooJobDependencyService;
import org.ecs.schedule.service.job.CuckooJobExtendService;
import org.ecs.schedule.service.job.CuckooJobNextService;
import org.ecs.schedule.service.job.CuckooJobService;
import org.ecs.schedule.vo.job.CuckooJobDetailVo;
import org.ecs.util.DateUtil;
import org.ecs.util.dao.PageDataList;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * index controller
 */
@Controller
@RequestMapping("/jobinfo")
public class JobInfoController extends BaseController {

    @Autowired
    private CuckooJobService cuckooJobService;

    @Autowired
    private CuckooGroupService cuckooGroupService;

    @Autowired
    private CuckooJobNextService cuckooJobNextService;

    @Autowired
    private CuckooJobDependencyService cuckooJobDependencyService;

    @Autowired
    private CuckooJobExtendService cuckooJobExtendService;

    @RequestMapping
    public String index0(HttpServletRequest request) {
        return index(request);
    }

    @RequestMapping(value = "/index")
    public String index(HttpServletRequest request) {

        // 默认超时时间
        request.setAttribute("overTime", 3);

        // 默认邮件接收人
        request.setAttribute("defaltMailTo", "dummyadmin@ecs.org");

        // 任务分组
        List<CuckooJobGroup> jobGroupList = cuckooGroupService.listAllGroup();
        if (CollectionUtils.isNotEmpty(jobGroupList)) {
            for (CuckooJobGroup cuckooJobGroup : jobGroupList) {
                cuckooJobGroup.setGroupName(cuckooJobGroup.getId() + "-" + cuckooJobGroup.getGroupName());
            }
        }
        request.setAttribute("jobGroupList", jobGroupList);

        List<CuckooJobGroup> jobGroupsWithNull = new ArrayList<CuckooJobGroup>();
        CuckooJobGroup groupNull = new CuckooJobGroup();
        groupNull.setGroupName("全部/无");
        jobGroupsWithNull.add(0, groupNull);
        jobGroupsWithNull.addAll(jobGroupList);
        request.setAttribute("jobGroupsWithNull", jobGroupsWithNull);

        CuckooJobExecType[] jobExecTypes = CuckooJobExecType.valuesNoNull();
        request.setAttribute("execJobTypes", jobExecTypes);

        // APP应用
        Map<String, String> jobAppList = cuckooJobService.findAllApps();
        request.setAttribute("jobAppList", jobAppList);
        Map<String, String> jobAppWithNull = new HashMap<>();
        jobAppWithNull.put("", "全部/无");
        jobAppWithNull.putAll(jobAppList);
        request.setAttribute("jobAppWithNull", jobAppWithNull);


        // 任务状态
        CuckooJobStatus[] jobStatusList = CuckooJobStatus.values();
        request.setAttribute("jobStatusList", jobStatusList);

        // 任务执行状态
        CuckooJobExecStatus[] jobExecStatus = CuckooJobExecStatus.values();
        request.setAttribute("jobExecStatusList", jobExecStatus);

        // 任务触发方式
        CuckooJobTriggerType[] jobTriggerType = CuckooJobTriggerType.valuesNoNull();
        request.setAttribute("jobTriggerTypeNoNull", jobTriggerType);

        // 是否为日切任务
        CuckooBooleanFlag[] jobIsTypeDaily = CuckooBooleanFlag.valuesNoNull();
        request.setAttribute("jobIsTypeDailyNoNull", jobIsTypeDaily);


        return "jobinfo/jobinfo.index";
    }

    /**
     * 分页查询任务
     *
     * @param jobInfo
     * @return
     */
    @RequestMapping(value = "/pageList")
    @ResponseBody
    public Object pageList(JobInfoQry jobInfo) {
        PageDataList<CuckooJobDetail> page = cuckooJobService.pageList(jobInfo);

        PageDataList<CuckooJobDetailVo> pageVo = convertJobDetailPageVo(page);
        return dataTable(pageVo);
    }

    private PageDataList<CuckooJobDetailVo> convertJobDetailPageVo(PageDataList<CuckooJobDetail> page) {
        PageDataList<CuckooJobDetailVo> pageVo = new PageDataList<>();

        pageVo.setPage(page.getPage());
        pageVo.setPageSize(page.getPageSize());
        pageVo.setTotal(page.getTotal());
        List<CuckooJobDetailVo> rows = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(page.getRows())) {
            for (CuckooJobDetail jobDetail : page.getRows()) {
                CuckooJobDetailVo vo = new CuckooJobDetailVo();
                BeanUtils.copyProperties(jobDetail, vo);
                if (CuckooJobTriggerType.CRON.getValue().equals(jobDetail.getTriggerType())) {
                    // 查看Cron是否有这个任务
                    vo.setQuartzInit(cuckooJobService.checkCronQuartzInit(jobDetail));
                }

                // 查询其他扩展信息
                CuckooJobExtend cuckooJobExtend = cuckooJobExtendService.queryById(jobDetail.getId());
                if (null != cuckooJobExtend) {
                    vo.setOverTime(cuckooJobExtend.getOverTimeLong());
                    vo.setMailTo(cuckooJobExtend.getEmailList());
                }
                CuckooJobGroup jobGroup = cuckooGroupService.getGroupById(vo.getGroupId());
                vo.setGroupName(jobGroup == null ? "" : jobGroup.getGroupName());

                rows.add(vo);
            }
        }
        pageVo.setRows(rows);

        return pageVo;
    }


    /**
     * 根据jobId获取触发任务的id
     *
     * @param jobId
     * @return
     */
    @RequestMapping(value = "/getPreJobIdByJobId")
    @ResponseBody
    public Object getPreJobIdByJobId(Long jobId) {
        return success(cuckooJobNextService.findJobIdByNextJobId(jobId));
    }


    /**
     * 根据jobId获取任务依赖的任务IDs
     *
     * @param jobId
     * @return
     */
    @RequestMapping(value = "/getDependencyIdsByJobId")
    @ResponseBody
    public Object getDependencyIdsByJobId(Long jobId) {
        return success(cuckooJobDependencyService.listDependencyIdsByJobId(jobId));
    }

    /**
     * 暂停全部
     *
     * @return
     */
    @RequestMapping(value = "/paushAll")
    @ResponseBody
    public Object paushAll(JobInfoQry jobInfo) {
        cuckooJobService.pauseAllJob(jobInfo);
        return success();
    }

    /**
     * 回复全部
     *
     * @return
     */
    @RequestMapping(value = "/resumeAll")
    @ResponseBody
    public Object resumeAll(JobInfoQry jobInfo) {
        cuckooJobService.resumeAllJob(jobInfo);
        return success();
    }

    /**
     * 执行
     *
     * @param id
     * @return
     */
    @RequestMapping(value = "/trigger")
    @ResponseBody
    public Object trigger(Long id, String typeDaily, Boolean needTriggleNext, Integer txDate, String flowLastTime, String flowCurTime) {
        if (CuckooBooleanFlag.NO.getValue().equals(typeDaily)) {
            cuckooJobService.triggerUnDailyJob(id, needTriggleNext, DateUtil.getLongTime(flowLastTime, "yyyy-MM-dd HH:mm:ss:SSS"),
                    DateUtil.getLongTime(flowCurTime, "yyyy-MM-dd HH:mm:ss:SSS"), false);
        } else if (CuckooBooleanFlag.YES.getValue().equals(typeDaily)) {
            cuckooJobService.triggerDailyJob(id, needTriggleNext, txDate, false);
        } else {
            cuckooJobService.triggerJob(id, needTriggleNext, false);
        }
        return success();
    }


    /**
     * 暂停
     *
     * @param id
     * @return
     */
    @RequestMapping(value = "/pause")
    @ResponseBody
    public Object pause(Long id) {
        cuckooJobService.pauseOneJob(id);
        return success();
    }

    /**
     * 恢复
     *
     * @param id
     * @return
     */
    @RequestMapping(value = "/resume")
    @ResponseBody
    public Object resume(Long id) {
        cuckooJobService.resumeOneJob(id);
        return success();
    }

    /**
     * 删除
     *
     * @param id
     * @return
     */
    @RequestMapping(value = "/remove")
    @ResponseBody
    public Object remove(Long id) {
        cuckooJobService.removeJob(id);
        return success();
    }

    /**
     * 删除
     *
     * @param jobDetail
     * @return
     */
    @RequestMapping(value = "/add")
    @ResponseBody
    public Object add(CuckooJobDetailVo jobDetail) {
        // 增加执行类型必要条件判断
        if (CuckooJobExecType.CUCKOO.getValue().equals(jobDetail.getExecJobType())) {
            if (StringUtils.isEmpty(jobDetail.getJobClassApplication())) {
                throw new BaseException("CuckooJob's appName can not be null");
            }
        }

        if (CuckooJobTriggerType.JOB.getValue().equals(jobDetail.getTriggerType())) {
            // 任务触发的任务，需要配置触发任务和依赖任务
            if (null == jobDetail.getPreJobId()) {
                throw new BaseException("the job Triggered by another should have a preJob");
            }
            if (StringUtils.isEmpty(jobDetail.getDependencyIds())) {
                throw new BaseException("the job Triggered by another should have more then one dependency job(the prejob can be dependencyjob)");
            }
        }

        if (CuckooBooleanFlag.NO.getValue().equals(jobDetail.getTypeDaily())) {
            // 非日切任务，不建议有太多的依赖
            if (StringUtils.isNotEmpty(jobDetail.getDependencyIds()) && jobDetail.getDependencyIds().contains(",")) {

                throw new BaseException("undaily job should not have too many dependency jobs.");
            }
        }

        if (null == jobDetail.getId()) {
            cuckooJobService.addJob(jobDetail);
        } else {
            cuckooJobService.modifyJob(jobDetail);
        }

        return success();
    }


    @RequestMapping(value = "/execview")
    @ResponseBody
    public Object execView(HttpServletRequest request, Long jobId) {
        if (null == jobId) {
            throw new BaseException("logid can not be null");
        }

        CuckooJobDetail cuckooJobDetail = cuckooJobService.getJobById(jobId);
        if (null == cuckooJobDetail) {
            throw new BaseException("can not get jobInfo by logid:{}", jobId);
        }

        Map<String, Object> rtn = new HashMap<>();
        // 查询
        Long preJobId = cuckooJobNextService.findJobIdByNextJobId(jobId);

        List<Long> dependencyIds = cuckooJobDependencyService.listDependencyIdsByJobId(jobId);

        // 依赖任务中过滤掉上级触发任务
        if (CollectionUtils.isNotEmpty(dependencyIds) && null != preJobId) {
            for (Iterator<Long> it = dependencyIds.iterator(); it.hasNext(); ) {
                Long id = it.next();
                if (preJobId.equals(id)) {
                    it.remove();
                }
            }
        }

        List<Long> nextJobIds = cuckooJobNextService.findNextJobIdByJobId(jobId);
        rtn.put("curJob", convertJobVo(jobId));
        rtn.put("depJobs", convertJobVos(dependencyIds));
        rtn.put("nextJobs", convertJobVos(nextJobIds));
        rtn.put("preJob", convertJobVo(preJobId));

        return success(rtn);
    }


    private List<CuckooJobDetailVo> convertJobVos(List<Long> dependencyIds) {
        if (CollectionUtils.isEmpty(dependencyIds)) return Collections.EMPTY_LIST;
        List<CuckooJobDetailVo> result = dependencyIds.stream().map(this::convertJobVo).collect(Collectors.toList());
        return result;
    }


    private CuckooJobDetailVo convertJobVo(Long jobId) {
        CuckooJobDetailVo vo = new CuckooJobDetailVo();
        CuckooJobDetail cuckooJobDetail = cuckooJobService.getJobById(jobId);
        if (null == cuckooJobDetail) {
            return null;
        }
        BeanUtils.copyProperties(cuckooJobDetail, vo);
        CuckooJobGroup group = cuckooGroupService.getGroupById(cuckooJobDetail.getGroupId());
        vo.setGroupName(group.getGroupName());
        return vo;
    }

}
