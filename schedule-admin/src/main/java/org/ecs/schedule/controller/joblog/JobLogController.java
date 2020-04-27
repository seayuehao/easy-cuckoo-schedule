package org.ecs.schedule.controller.joblog;

import org.ecs.schedule.controller.BaseController;
import org.ecs.schedule.domain.exec.CuckooJobDetail;
import org.ecs.schedule.domain.exec.CuckooJobExecLog;
import org.ecs.schedule.domain.exec.CuckooJobGroup;
import org.ecs.schedule.enums.CuckooJobExecStatus;
import org.ecs.schedule.exception.BaseException;
import org.ecs.schedule.qry.QryBase;
import org.ecs.schedule.qry.job.JobLogQry;
import org.ecs.schedule.service.auth.CuckooAuthService;
import org.ecs.schedule.service.job.CuckooGroupService;
import org.ecs.schedule.service.job.CuckooJobLogService;
import org.ecs.schedule.service.job.CuckooJobService;
import org.ecs.schedule.vo.job.CuckooJobExecLogVo;
import org.ecs.util.DateUtil;
import org.ecs.util.dao.PageDataList;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/joblog")
public class JobLogController extends BaseController {

    @Autowired
    private CuckooGroupService cuckooGroupService;

    @Autowired
    private CuckooJobService cuckooJobService;

    @Autowired
    private CuckooJobLogService cuckooJobLogService;

    @Autowired
    private CuckooAuthService cuckooAuthService;

    @RequestMapping
    public String index0(HttpServletRequest request, Long groupId, Long jobId) {
        return index(request, groupId, jobId);
    }


    @RequestMapping(value = "/index")
    public String index(HttpServletRequest request, Long groupId, Long jobId) {
        request.setAttribute("groupId", groupId);
        request.setAttribute("jobId", jobId);
        // 任务类型
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

        // 执行状态

        CuckooJobExecStatus[] jobExecStatus = CuckooJobExecStatus.values();
        request.setAttribute("jobExecStatusList", jobExecStatus);

        return "joblog/joblog.index";
    }

    @ResponseBody
    @RequestMapping(value = "/getJobsByGroup")
    public Object getJobsByGroup(Long groupId) {
        return success(cuckooJobService.getJobsByGroupId(groupId));
    }

    @ResponseBody
    @RequestMapping(value = "/pageList")
    public Object pageList(JobLogQry qry, String jobStatusStr) {

        if (StringUtils.isNotEmpty(qry.getFilterTime())) {
            // 2017-03-20 00:00:00 - 2017-03-21 00:00:00
            String[] timeRange = qry.getFilterTime().split(" - ");
            qry.setStartDateTime(DateUtil.parseDate(timeRange[0], "yyyy-MM-dd HH:mm:ss").getTime());
            qry.setEndDateTime(DateUtil.parseDate(timeRange[1], "yyyy-MM-dd HH:mm:ss").getTime());
        }

        if (StringUtils.isNotEmpty(jobStatusStr)) {
            String[] jobStatusArr = jobStatusStr.split(",");
            qry.setJobStatus(Arrays.asList(jobStatusArr));
        }

        PageDataList<CuckooJobExecLog> pageLog = cuckooJobLogService.pageByQry(qry);

        PageDataList<CuckooJobExecLogVo> pageLogVo = new PageDataList<>();
        pageLogVo.setPage(pageLog.getPage());
        pageLogVo.setPageSize(pageLog.getPageSize());
        pageLogVo.setTotal(pageLog.getTotal());
        pageLogVo.setRows(converPageRows(pageLog.getRows()));
        return dataTable(pageLogVo);
    }

    @ResponseBody
    @RequestMapping(value = "/pagePendingList")
    public Object pagePendingList(QryBase qry) {

        PageDataList<CuckooJobExecLog> pageLog = cuckooJobLogService.pagePendingList(qry);

        PageDataList<CuckooJobExecLogVo> pageLogVo = new PageDataList<>();
        pageLogVo.setPage(pageLog.getPage());
        pageLogVo.setPageSize(pageLog.getPageSize());
        pageLogVo.setTotal(pageLog.getTotal());

        pageLogVo.setRows(converPageRows(pageLog.getRows()));

        return dataTable(pageLogVo);
    }


    @ResponseBody
    @RequestMapping(value = "/pageOverTimeList")
    public Object pageOverTimeList(QryBase qry) {

        PageDataList<CuckooJobExecLog> pageLog = cuckooJobLogService.pageOverTimeJobs(qry);

        PageDataList<CuckooJobExecLogVo> pageLogVo = new PageDataList<>();
        pageLogVo.setPage(pageLog.getPage());
        pageLogVo.setPageSize(pageLog.getPageSize());
        pageLogVo.setTotal(pageLog.getTotal());

        pageLogVo.setRows(converPageRows(pageLog.getRows()));

        return dataTable(pageLogVo);
    }

    private List<CuckooJobExecLogVo> converPageRows(List<CuckooJobExecLog> rows) {
        List<CuckooJobExecLogVo> list = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(rows)) {
            for (CuckooJobExecLog cuckooJobExecLog : rows) {
                CuckooJobExecLogVo vo = new CuckooJobExecLogVo();
                BeanUtils.copyProperties(cuckooJobExecLog, vo);
                list.add(vo);
            }
        }

        return list;
    }

    @ResponseBody
    @RequestMapping(value = "/reset")
    public Object reset(Long logId) {
        if (null == logId) {
            throw new BaseException("logid can not be null");
        }
        cuckooJobLogService.resetLogStatus(logId, CuckooJobExecStatus.SUCCED);
        return success();
    }

    @ResponseBody
    @RequestMapping(value = "/redo")
    @Transactional(rollbackFor = Exception.class)
    public Object redo(Long logId, Boolean needTriggleNext) {

        if (null == logId) {
            throw new BaseException("logid can not be null");
        }

        CuckooJobExecLog cuckooJobExecLog = cuckooJobLogService.getJobLogByLogId(logId);

        if (null == cuckooJobExecLog) {
            throw new BaseException("can not get jobLog by logid:{}", logId);
        }

        if (!cuckooAuthService.getLogonInfo().getWritableGroupIds().contains(cuckooJobExecLog.getGroupId())) {
            throw new BaseException("no writable right");
        }

        cuckooJobExecLog.setJobStartTime(System.currentTimeMillis());
        cuckooJobExecLog.setForceTriggle(true);
        cuckooJobExecLog.setNeedTriggleNext(needTriggleNext == null ? false : needTriggleNext);
        CuckooJobDetail jobDetail = cuckooJobService.getJobById(cuckooJobExecLog.getJobId());
        if (null == jobDetail) {
            throw new BaseException("can not get jobDetail by jobId:{}", cuckooJobExecLog.getJobId());
        }
        Long id = cuckooJobService.pendingJob(jobDetail, cuckooJobExecLog);

        // 设置当前任务为成功
        cuckooJobExecLog.setExecJobStatus(CuckooJobExecStatus.SUCCED.getValue());
        cuckooJobLogService.updateJobLogByPk(cuckooJobExecLog);

        return success(id);
    }


    @RequestMapping(value = "/logdetail")
    public String logdetail(HttpServletRequest request, Long logId) {

        CuckooJobExecLog cuckooJobExecLog = cuckooJobLogService.getJobLogByLogId(logId);
        if (null == cuckooJobExecLog) {
            throw new BaseException("can not get jobLog by logid:{}", logId);
        }

        request.setAttribute("log", converJobLogVo(cuckooJobExecLog));

        return "joblog/joblog.detail";
    }

    private CuckooJobExecLogVo converJobLogVo(CuckooJobExecLog cuckooJobExecLog) {
        CuckooJobExecLogVo vo = new CuckooJobExecLogVo();
        CuckooJobGroup jobGroup = cuckooGroupService.getGroupById(cuckooJobExecLog.getGroupId());
        CuckooJobDetail jobDetail = cuckooJobService.getJobById(cuckooJobExecLog.getJobId());
        vo.setGroupName(jobGroup.getGroupName());
        vo.setJobDesc(jobDetail.getJobDesc());
        BeanUtils.copyProperties(cuckooJobExecLog, vo);
        return vo;
    }


    @ResponseBody
    @RequestMapping(value = "/execview")
    public Object execview(Long logId) {
        if (null == logId) {
            throw new BaseException("logid can not be null");
        }

        CuckooJobExecLog cuckooJobExecLog = cuckooJobLogService.getJobLogByLogId(logId);
        if (null == cuckooJobExecLog) {
            throw new BaseException("can not get jobLog by logid:{}", logId);
        }

        Map<String, Object> rtn = new HashMap<>();
        // 查询
        CuckooJobExecLog preJobLog = cuckooJobLogService.getPreJobLogs(cuckooJobExecLog);
        List<CuckooJobExecLog> dependencyJobLogs = cuckooJobLogService.getDependencyJobs(cuckooJobExecLog);
        // 依赖任务中过滤掉上级触发任务
        if (CollectionUtils.isNotEmpty(dependencyJobLogs) && null != preJobLog) {
            for (Iterator<CuckooJobExecLog> it = dependencyJobLogs.iterator(); it.hasNext(); ) {
                CuckooJobExecLog depLog = it.next();
                if (preJobLog.getId().equals(depLog.getId())) {
                    it.remove();
                }
            }
        }

        List<CuckooJobExecLog> nextJobLogs = cuckooJobLogService.getNextJobs(cuckooJobExecLog);
        CuckooJobExecLog curJob = cuckooJobLogService.getJobLogByLogId(logId);
        rtn.put("curJob", convertLogVo(curJob));
        rtn.put("depJobs", convertLogVos(dependencyJobLogs));
        rtn.put("nextJobs", convertLogVos(nextJobLogs));
        rtn.put("preJob", convertLogVo(preJobLog));

        return success(rtn);
    }


    private CuckooJobExecLogVo convertLogVo(CuckooJobExecLog jobLog) {
        if (null == jobLog) {
            return null;
        }
        CuckooJobExecLogVo vo = new CuckooJobExecLogVo();
        BeanUtils.copyProperties(jobLog, vo);
        CuckooJobGroup group = cuckooGroupService.getGroupById(jobLog.getGroupId());
        vo.setGroupName(group == null ? "" : group.getGroupName());
        return vo;
    }


    private List<CuckooJobExecLogVo> convertLogVos(List<CuckooJobExecLog> nextJobLogs) {
        if (CollectionUtils.isEmpty(nextJobLogs)) return Collections.EMPTY_LIST;
        List<CuckooJobExecLogVo> vos = nextJobLogs.stream().map(this::convertLogVo).collect(Collectors.toList());
        return vos;
    }

}
