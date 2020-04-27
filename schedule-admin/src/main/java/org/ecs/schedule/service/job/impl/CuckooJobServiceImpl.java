package org.ecs.schedule.service.job.impl;

import org.ecs.schedule.component.quartz.QuartzManager;
import org.ecs.schedule.dao.exec.CuckooJobDetailMapper;
import org.ecs.schedule.dao.exec.CuckooJobExecLogMapper;
import org.ecs.schedule.dao.exec.CuckooJobExtendMapper;
import org.ecs.schedule.dao.exec.CuckooJobGroupMapper;
import org.ecs.schedule.dao.net.CuckooNetRegistJobMapper;
import org.ecs.schedule.domain.exec.CuckooJobDetail;
import org.ecs.schedule.domain.exec.CuckooJobDetailCriteria;
import org.ecs.schedule.domain.exec.CuckooJobExecLog;
import org.ecs.schedule.domain.exec.CuckooJobExtend;
import org.ecs.schedule.domain.exec.CuckooJobGroup;
import org.ecs.schedule.domain.net.CuckooNetRegistJob;
import org.ecs.schedule.domain.net.CuckooNetRegistJobCriteria;
import org.ecs.schedule.enums.CuckooBooleanFlag;
import org.ecs.schedule.enums.CuckooJobExecStatus;
import org.ecs.schedule.enums.CuckooJobStatus;
import org.ecs.schedule.enums.CuckooJobTriggerType;
import org.ecs.schedule.exception.BaseException;
import org.ecs.schedule.qry.job.JobInfoQry;
import org.ecs.schedule.service.auth.CuckooAuthService;
import org.ecs.schedule.service.job.CuckooJobDependencyService;
import org.ecs.schedule.service.job.CuckooJobLogService;
import org.ecs.schedule.service.job.CuckooJobNextService;
import org.ecs.schedule.service.job.CuckooJobService;
import org.ecs.schedule.vo.job.CuckooJobDetailVo;
import org.ecs.util.dao.PageDataList;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.quartz.CronExpression;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CuckooJobServiceImpl implements CuckooJobService {

    @Autowired
    private CuckooJobGroupMapper cuckooJobGroupMapper;

    @Autowired
    private CuckooJobDetailMapper cuckooJobDetailMapper;

    @Autowired
    private CuckooJobExecLogMapper cuckooJobExecLogMapper;

    @Autowired
    private QuartzManager quartzManager;

    @Autowired
    private CuckooJobLogService cuckooJobLogService;

    @Autowired
    private CuckooNetRegistJobMapper cuckooNetRegistJobMapper;

    @Autowired
    private CuckooJobNextService cuckooJobNextService;

    @Autowired
    private CuckooJobDependencyService cuckooJobDependencyService;

    @Autowired
    private CuckooJobExtendMapper cuckooJobExtendMapper;

    @Autowired
    private CuckooAuthService cuckooAuthService;

    @Value("${cuckoo.pending.job.retry}")
    private long pendingJobRetry;


    @Override
    @Transactional
    public Long addJob(CuckooJobDetailVo jobDetail) {
        if (null == jobDetail || null == jobDetail.getGroupId()) {
            throw new BaseException("jobInfo should not be null");
        }

        // 校验分组是否存在
        CuckooJobGroup cuckooJobGroup = cuckooJobGroupMapper.selectByPrimaryKey(jobDetail.getGroupId());
        if (null == cuckooJobGroup) {
            throw new BaseException("can not find jobgroup by groupId:" + jobDetail.getGroupId());
        }

        if (!cuckooAuthService.getLogonInfo().getWritableGroupIds().contains(cuckooJobGroup.getId())) {
            throw new BaseException("no writable right");
        }

        // 如果是cron，校验cron是否正确
        if (CuckooJobTriggerType.CRON.getValue().equals(jobDetail.getTriggerType())) {
            jobDetail.setCronExpression(StringUtils.trim(jobDetail.getCronExpression()));
            if (!CronExpression.isValidExpression(jobDetail.getCronExpression())) {

                throw new BaseException("cronexpression is not valid:" + jobDetail.getCronExpression());
            }
        }
        // 新增wjs_schedule_cockoo_job_details 数据，默认启动
        CuckooJobDetail cuckooJobDetail = new CuckooJobDetail();
        BeanUtils.copyProperties(jobDetail, cuckooJobDetail);
        // 默认执行
        cuckooJobDetail.setJobStatus(CuckooJobStatus.RUNNING.getValue());
        cuckooJobDetailMapper.insertSelective(cuckooJobDetail);
        Long jobId = cuckooJobDetailMapper.lastInsertId();
        if (jobId == null) {
            throw new BaseException("cuckoo_job_details insert error,can not get autoincriment id");
        }

        if (null != jobDetail.getPreJobId()) {
            // 触发任务
            CuckooJobDetail jobPreTriggle = getJobById(jobDetail.getPreJobId());
            if (null == jobPreTriggle) {
                throw new BaseException("can not find pre trigger job by preJobId:{}", jobDetail.getPreJobId());
            }
            cuckooJobNextService.addOrUpdate(jobDetail.getPreJobId(), jobId);
        }
        if (StringUtils.isNotEmpty(jobDetail.getDependencyIds())) {
            // 依赖任务
            String[] dependencyIds = jobDetail.getDependencyIds().split(",");
            cuckooJobDependencyService.addOrUpdateJobDependency(jobId, dependencyIds);
        }

        // 邮件接收人
        String mailTo = jobDetail.getMailTo();
        Long overTime = jobDetail.getOverTime();
        Long overTimeLong = overTime * 60 * 60 * 1000;
        addJobExtendInfo(jobId, mailTo, overTimeLong);

        if (CuckooJobTriggerType.CRON.getValue().equals(jobDetail.getTriggerType())) {
            quartzManager.addCronJob(String.valueOf(jobId), jobDetail.getCronExpression(),
                    CuckooJobStatus.fromName(jobDetail.getJobStatus()),
                    CuckooBooleanFlag.fromName(jobDetail.getTypeDaily()));
        }

        return jobId;
    }

    private void addJobExtendInfo(Long jobId, String mailTo, Long overTime) {
        CuckooJobExtend cuckooJobExtend = cuckooJobExtendMapper.selectByPrimaryKey(jobId);
        if (null == cuckooJobExtend) {
            // 新增
            CuckooJobExtend ext = new CuckooJobExtend();
            ext.setJobId(jobId);
            ext.setEmailList(mailTo);
            ext.setOverTimeLong(overTime);
            cuckooJobExtendMapper.insertSelective(ext);

        } else {
            // 删除
            cuckooJobExtend.setOverTimeLong(overTime);
            cuckooJobExtend.setEmailList(mailTo);
            cuckooJobExtendMapper.updateByPrimaryKeySelective(cuckooJobExtend);
        }
    }


    @Override
    @Transactional
    public void modifyJob(CuckooJobDetailVo jobInfo) {
        if (null == jobInfo || null == jobInfo.getId()) {
            throw new BaseException("jobinfo should not be null");
        }

        if (StringUtils.isNotEmpty(jobInfo.getCronExpression())) {
            if (!CronExpression.isValidExpression(jobInfo.getCronExpression())) {

                throw new BaseException("cron expression is not valid:" + jobInfo.getCronExpression());
            }
        }

        // 根据ID查询任务信息
        CuckooJobDetail orginJobDetail = cuckooJobDetailMapper.selectByPrimaryKey(jobInfo.getId());

        if (null == orginJobDetail) {
            throw new BaseException("can not find jobinfo by id : " + jobInfo.getId());
        }


        if (!cuckooAuthService.getLogonInfo().getWritableGroupIds().contains(orginJobDetail.getGroupId())) {
            throw new BaseException("no writable right");
        }

        CuckooJobDetail targetJobDetail = new CuckooJobDetail();

        BeanUtils.copyProperties(orginJobDetail, targetJobDetail);

        BeanUtils.copyProperties(jobInfo, targetJobDetail);

        cuckooJobDetailMapper.updateByPrimaryKeySelective(targetJobDetail);
        if (CuckooJobTriggerType.JOB.getValue().equals(orginJobDetail.getTriggerType())
                || CuckooJobTriggerType.NONE.getValue().equals(orginJobDetail.getTriggerType())) {
            // 原来任务类型为job触发 且新任务为Cron，那么需要新增quartz。否则不做处理
            if (CuckooJobTriggerType.CRON.getValue().equals(targetJobDetail.getTriggerType())) {

                quartzManager.addCronJob(String.valueOf(targetJobDetail.getId()), jobInfo.getCronExpression(),
                        CuckooJobStatus.fromName(targetJobDetail.getJobStatus()),
                        CuckooBooleanFlag.fromName(targetJobDetail.getTypeDaily()));
            }
        } else if (CuckooJobTriggerType.CRON.getValue().equals(orginJobDetail.getTriggerType())) {
            // 如果原来任务类型为Cron，那么修改一条任务
            if (CuckooJobTriggerType.CRON.getValue().equals(targetJobDetail.getTriggerType())) {
                // 且新任务为Cron，那么需要修改quartz
                quartzManager.modfyCronJob(String.valueOf(targetJobDetail.getId()), jobInfo.getCronExpression(),
                        CuckooJobStatus.fromName(targetJobDetail.getJobStatus()),
                        CuckooBooleanFlag.fromName(targetJobDetail.getTypeDaily()));
            } else {
                // 且新任务为NORMAL，那么需要删除quartz
                quartzManager.deleteCronJob(String.valueOf(orginJobDetail.getId()));
            }
        } else {
            throw new BaseException("unknown job trigger type : " + jobInfo.getTriggerType());
        }

        if (CuckooJobTriggerType.CRON.getValue().equals(targetJobDetail.getTriggerType())) {
            // CRON触发的任务，删除被触发任务关系
            cuckooJobNextService.deletePreJob(targetJobDetail.getId());
        } else {
            if (null != jobInfo.getPreJobId()) {
                // 触发任务
                CuckooJobDetail jobPreTriggle = getJobById(jobInfo.getPreJobId());
                if (null == jobPreTriggle) {
                    throw new BaseException("can not find pre trigger job by preJobId:{}", jobInfo.getPreJobId());
                }
                cuckooJobNextService.addOrUpdate(jobInfo.getPreJobId(), jobInfo.getId());
            }
        }

        String[] dependencyIds = {};
        if (StringUtils.isNotEmpty(jobInfo.getDependencyIds())) {
            // 依赖任务
            dependencyIds = jobInfo.getDependencyIds().split(",");
        }
        cuckooJobDependencyService.addOrUpdateJobDependency(jobInfo.getId(), dependencyIds);

        // 邮件接收人
        String mailTo = jobInfo.getMailTo() == null ? "" : jobInfo.getMailTo();
        Long overTime = jobInfo.getOverTime() == null ? Long.MAX_VALUE : jobInfo.getOverTime() * 60 * 60 * 1000;

        addJobExtendInfo(targetJobDetail.getId(), mailTo, overTime);
        cuckooJobDetailMapper.updateByPrimaryKeySelective(targetJobDetail);
    }

    @Override
    @Transactional
    public void removeJob(Long id) {
        if (null == id) {
            throw new BaseException("id should not be null");
        }
        // 根据ID查询任务信息
        CuckooJobDetail cuckooJobDetail = cuckooJobDetailMapper.selectByPrimaryKey(id);
        if (!cuckooAuthService.getLogonInfo().getWritableGroupIds().contains(cuckooJobDetail.getGroupId())) {
            throw new BaseException("no writable right");
        }
        // 根据id删除cuckoo数据
        if (null != cuckooJobDetail) {
            cuckooJobDetailMapper.deleteByPrimaryKey(id);
            // 根据任务信息删除quartz信息
            quartzManager.deleteCronJob(String.valueOf(cuckooJobDetail.getId()));
        }
    }

    @Override
    @Transactional
    public void pauseOneJob(Long id) {
        // 根据ID查询Cuckoo
        if (null == id) {
            throw new BaseException("id should not be null");
        }

        // 根据ID查询任务信息
        CuckooJobDetail orginJobDetail = cuckooJobDetailMapper.selectByPrimaryKey(id);
        orginJobDetail.setJobStatus(CuckooJobStatus.PAUSE.getValue());
        if (!cuckooAuthService.getLogonInfo().getWritableGroupIds().contains(orginJobDetail.getGroupId())) {
            throw new BaseException("no writable right");
        }
        // 更新cuckoo状态
        cuckooJobDetailMapper.updateByPrimaryKeySelective(orginJobDetail);

        // 更新quartz任务状态
        if (CuckooJobTriggerType.CRON.getValue().equals(orginJobDetail.getTriggerType())) {
            quartzManager.pauseCronJob(String.valueOf(orginJobDetail.getId()));
        }
    }

    @Override
    @Transactional
    public void pauseAllJob(JobInfoQry jobInfo) {
        // 不能使用quartzManage.pauseAll()，该方法会将后续增加的任务或者强制执行的SimpleTrigger也锁住，此处对任务循环进行锁定
        CuckooJobDetailCriteria crt = new CuckooJobDetailCriteria();
        crt.setOrderByClause(" id desc ");
        CuckooJobDetailCriteria.Criteria exp = crt.createCriteria();
        if (null != jobInfo.getGroupId()) {
            exp.andGroupIdEqualTo(jobInfo.getGroupId());
        } else {
            if (CollectionUtils.isNotEmpty(cuckooAuthService.getLogonInfo().getWritableGroupIds())) {
                exp.andGroupIdIn(cuckooAuthService.getLogonInfo().getWritableGroupIds());
            }
        }
        if (null != jobInfo.getJobId()) {
            exp.andIdEqualTo(jobInfo.getJobId());
        }
        if (StringUtils.isNotEmpty(jobInfo.getJobClassApplication())) {
            exp.andJobClassApplicationEqualTo(jobInfo.getJobClassApplication());
        }
        if (StringUtils.isNotEmpty(jobInfo.getJobStatus())) {
            exp.andJobStatusEqualTo(jobInfo.getJobStatus());
        }

        List<CuckooJobDetail> jobs = cuckooJobDetailMapper.selectByExample(crt);
        if (CollectionUtils.isNotEmpty(jobs)) {
            for (CuckooJobDetail cuckooJobDetail : jobs) {
                // 更新cuckoo状态
                cuckooJobDetail.setJobStatus(CuckooJobStatus.PAUSE.getValue());
                cuckooJobDetailMapper.updateByPrimaryKeySelective(cuckooJobDetail);
                // 更新quartz任务状态
                if (CuckooJobTriggerType.CRON.getValue().equals(cuckooJobDetail.getTriggerType())) {
                    quartzManager.pauseCronJob(String.valueOf(cuckooJobDetail.getId()));
                }
            }
        }
    }

    @Override
    @Transactional
    public void resumeOneJob(Long id) {
        // 根据ID查询Cuckoo
        if (null == id) {
            throw new BaseException("id should not be null");
        }

        // 根据ID查询任务信息
        CuckooJobDetail orginJobDetail = cuckooJobDetailMapper.selectByPrimaryKey(id);
        orginJobDetail.setJobStatus(CuckooJobStatus.RUNNING.getValue());
        if (!cuckooAuthService.getLogonInfo().getWritableGroupIds().contains(orginJobDetail.getGroupId())) {
            throw new BaseException("no writable right");
        }

        // 更新cuckoo状态
        cuckooJobDetailMapper.updateByPrimaryKeySelective(orginJobDetail);

        // 更新quartz任务状态
        if (CuckooJobTriggerType.CRON.getValue().equals(orginJobDetail.getTriggerType())) {
            quartzManager.resumeCronJob(String.valueOf(orginJobDetail.getId()));
        }
    }

    @Override
    @Transactional
    public void resumeAllJob(JobInfoQry jobInfo) {
        CuckooJobDetailCriteria crt = new CuckooJobDetailCriteria();
        crt.setOrderByClause(" id desc ");
        CuckooJobDetailCriteria.Criteria exp = crt.createCriteria();
        if (null != jobInfo.getGroupId()) {
            exp.andGroupIdEqualTo(jobInfo.getGroupId());
        } else {
            if (CollectionUtils.isNotEmpty(cuckooAuthService.getLogonInfo().getWritableGroupIds())) {
                exp.andGroupIdIn(cuckooAuthService.getLogonInfo().getWritableGroupIds());
            }
        }

        if (null != jobInfo.getJobId()) {
            exp.andIdEqualTo(jobInfo.getJobId());
        }
        if (StringUtils.isNotEmpty(jobInfo.getJobClassApplication())) {
            exp.andJobClassApplicationEqualTo(jobInfo.getJobClassApplication());
        }
        if (StringUtils.isNotEmpty(jobInfo.getJobStatus())) {
            exp.andJobStatusEqualTo(jobInfo.getJobStatus());
        }

        List<CuckooJobDetail> jobs = cuckooJobDetailMapper.selectByExample(crt);
        if (CollectionUtils.isNotEmpty(jobs)) {
            for (CuckooJobDetail cuckooJobDetail : jobs) {
                cuckooJobDetail.setJobStatus(CuckooJobStatus.RUNNING.getValue());
                cuckooJobDetailMapper.updateByPrimaryKeySelective(cuckooJobDetail);
                quartzManager.resumeCronJob(String.valueOf(cuckooJobDetail.getId()));
            }
        }
    }

    @Override
    public CuckooJobDetail getJobById(Long jobId) {
        return cuckooJobDetailMapper.selectByPrimaryKey(jobId);
    }

    @Override
    public List<CuckooJobDetail> getNextJobById(Long jobId) {

        List<Long> nextJobids = cuckooJobNextService.findNextJobIdByJobId(jobId);
        if (CollectionUtils.isEmpty(nextJobids)) {
            return new ArrayList<>(0);
        }
        CuckooJobDetailCriteria jobCrt = new CuckooJobDetailCriteria();
        jobCrt.createCriteria().andIdIn(nextJobids);

        return cuckooJobDetailMapper.selectByExample(jobCrt);
    }

    @Override
    public PageDataList<CuckooJobDetail> pageList(JobInfoQry jobInfo) {

        CuckooJobDetailCriteria crt = new CuckooJobDetailCriteria();
        crt.setOrderByClause(" id desc ");
        CuckooJobDetailCriteria.Criteria exp = crt.createCriteria();
        if (null != jobInfo.getGroupId()) {
            exp.andGroupIdEqualTo(jobInfo.getGroupId());
        } else {
            // 所有有权限的group
            if (CollectionUtils.isNotEmpty(cuckooAuthService.getLogonInfo().getReadableGroupIds())) {
                exp.andGroupIdIn(cuckooAuthService.getLogonInfo().getReadableGroupIds());
            }

        }
        if (null != jobInfo.getJobId()) {
            exp.andIdEqualTo(jobInfo.getJobId());
        }
        if (StringUtils.isNotEmpty(jobInfo.getJobClassApplication())) {
            exp.andJobClassApplicationEqualTo(jobInfo.getJobClassApplication());
        }
        if (StringUtils.isNotEmpty(jobInfo.getJobStatus())) {
            exp.andJobStatusEqualTo(jobInfo.getJobStatus());
        }
        crt.setStart(jobInfo.getStart());
        crt.setLimit(jobInfo.getLimit());

        return cuckooJobDetailMapper.pageByExample(crt);
    }

    @Override
    public Map<String, String> findAllApps() {
        List<CuckooNetRegistJob> jobs = cuckooNetRegistJobMapper
                .selectByExample(new CuckooNetRegistJobCriteria());
        Map<String, String> rtn = jobs.stream().collect(Collectors.toMap(r -> r.getJobClassApplication(), r -> r.getJobClassApplication()));
        return rtn;
    }

    @Override
    @Transactional
    public Long pendingJob(CuckooJobDetail jobDetail, CuckooJobExecLog fatherJobLog) {
        CuckooJobExecLog jobLog = new CuckooJobExecLog();
        Long curTime = System.currentTimeMillis();
        // 初始化任务日志信息
        BeanUtils.copyProperties(jobDetail, jobLog);

        jobLog.setId(null);
        jobLog.setJobId(jobDetail.getId());
        jobLog.setJobStartTime(curTime);

        jobLog.setExecJobStatus(CuckooJobExecStatus.PENDING.getValue());
        jobLog.setLatestCheckTime(curTime);
        jobLog.setNeedTriggleNext(fatherJobLog.getNeedTriggleNext());

        jobLog.setForceTriggle(fatherJobLog.getForceTriggle());
        jobLog.setTxDate(fatherJobLog.getTxDate());
        jobLog.setFlowLastTime(fatherJobLog.getFlowLastTime());
        jobLog.setFlowCurTime(fatherJobLog.getFlowCurTime());

        cuckooJobExecLogMapper.insertSelective(jobLog);

        jobLog.setId(cuckooJobExecLogMapper.lastInsertId());
        // 使用Quartz.simpleJob进行触发
        quartzManager.addSimpleJob(jobLog, 0L);
        return jobLog.getId();
    }

    @Override
    public void rependingJob(CuckooJobExecLog jobLog) {
        log.debug("repending job ,jobLog:{} ", jobLog);
        quartzManager.addSimpleJob(jobLog, pendingJobRetry);
    }

    @Override
    public void triggerUnDailyJob(Long jobId, Boolean needTriggleNext, Long lastTime, Long curTime, boolean foreTriggle) {
        CuckooJobDetail cuckooJobDetail = cuckooJobDetailMapper.selectByPrimaryKey(jobId);
        if (null == cuckooJobDetail) {
            throw new BaseException("can not get jobinfo by id:{}", jobId);
        }

        if (!cuckooAuthService.getLogonInfo().getWritableGroupIds().contains(cuckooJobDetail.getGroupId())) {
            throw new BaseException("no writable right");
        }
        CuckooJobExecLog jobLog = cuckooJobLogService.initUnDailyJobLog(cuckooJobDetail, needTriggleNext, lastTime,
                curTime, foreTriggle);
        quartzManager.addSimpleJob(jobLog, 0L);
    }

    @Override
    public void triggerDailyJob(Long jobId, Boolean needTriggleNext, Integer txDate, boolean foreTriggle) {
        CuckooJobDetail cuckooJobDetail = cuckooJobDetailMapper.selectByPrimaryKey(jobId);
        if (null == cuckooJobDetail) {
            throw new BaseException("can not get jobinfo by id:{}", jobId);
        }

        if (!cuckooAuthService.getLogonInfo().getWritableGroupIds().contains(cuckooJobDetail.getGroupId())) {
            throw new BaseException("no writable right");
        }

        CuckooJobExecLog jobLog = cuckooJobLogService.initDailyJobLog(cuckooJobDetail, needTriggleNext, txDate,
                foreTriggle);
        quartzManager.addSimpleJob(jobLog, 0L);
    }

    @Override
    public void triggerJob(Long jobId, Boolean needTriggleNext, boolean foreTriggle) {
        CuckooJobDetail cuckooJobDetail = cuckooJobDetailMapper.selectByPrimaryKey(jobId);

        if (null == cuckooJobDetail) {
            throw new BaseException("can not get jobinfo by id:{}", jobId);
        }

        if (!cuckooAuthService.getLogonInfo().getWritableGroupIds().contains(cuckooJobDetail.getGroupId())) {
            throw new BaseException("no writable right");
        }
        CuckooJobExecLog jobLog = cuckooJobLogService.initJobLog(cuckooJobDetail, needTriggleNext, foreTriggle);
        quartzManager.addSimpleJob(jobLog, 0L);
    }


    @Override
    public List<CuckooJobDetail> getJobsByGroupId(Long groupId) {
        CuckooJobDetailCriteria crt = new CuckooJobDetailCriteria();
        if (null != groupId) {

            crt.createCriteria().andGroupIdEqualTo(groupId);
        } else {
            // 用户权限控制
            if (CollectionUtils.isNotEmpty(cuckooAuthService.getLogonInfo().getReadableGroupIds())) {
                crt.createCriteria().andGroupIdIn(cuckooAuthService.getLogonInfo().getReadableGroupIds());
            }
        }
        return cuckooJobDetailMapper.selectByExample(crt);
    }

    @Override
    public boolean checkCronQuartzInit(CuckooJobDetail jobDetail) {
        return quartzManager.checkCronExists(String.valueOf(jobDetail.getId()));
    }

}
