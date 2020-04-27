package org.ecs.schedule.component.quartz;

import org.ecs.schedule.component.cuckoo.CuckooJobExecutor;
import org.ecs.schedule.constant.CuckooJobConstant;
import org.ecs.schedule.dao.exec.CuckooJobDetailMapper;
import org.ecs.schedule.dao.exec.CuckooJobExecLogMapper;
import org.ecs.schedule.domain.exec.CuckooJobDetail;
import org.ecs.schedule.domain.exec.CuckooJobExecLog;
import org.ecs.schedule.exception.BaseException;
import org.ecs.schedule.exception.JobUndailyLogBreakException;
import org.ecs.schedule.service.job.CuckooJobLogService;
import org.ecs.schedule.service.job.CuckooJobService;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.quartz.Trigger;
import org.quartz.impl.triggers.CronTriggerImpl;
import org.quartz.impl.triggers.SimpleTriggerImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Slf4j
@Component
public class QuartzJobExecutor extends QuartzJobBean {

    @Autowired
    private CuckooJobDetailMapper cuckooJobDetailMapper;

    @Autowired
    private CuckooJobExecLogMapper cuckooJobExecLogMapper;

    @Autowired
    private CuckooJobExecutor cuckooJobExecutor;

    @Autowired
    private CuckooJobService cuckooJobService;

    @Autowired
    private CuckooJobLogService cuckooJobLogService;

    @Override
    @Transactional
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {

        Trigger trigger = context.getTrigger();
        JobKey jobKey = trigger.getJobKey();

        Date scheduledFireTime = context.getScheduledFireTime();

        CuckooJobExecLog cuckooJobExecLog = null;
        String quartzJobGroup = jobKey.getGroup();
        String[] quartzJobNameArr = jobKey.getName().split(CuckooJobConstant.QUARTZ_JOBNAME_JOINT);
        if (quartzJobNameArr.length < 1) {
            log.error("Unformat quartz Job ,group:{},name:{} ", quartzJobGroup, jobKey.getName());
            throw new BaseException("Unformat quartz Job ,group:{},name:{} ", quartzJobGroup, jobKey.getName());
        }

        if (trigger instanceof CronTriggerImpl) {
            // cron 任务jobName == jobId
            // 如果日志ID为空，表示当前任务为CRON触发，新增执行日志(一般情况为任务调度节点的第一个任务)
            log.info("quartz trigger cron job, jobGroup:{},jobName:{},triggerType:{}", jobKey.getGroup(), quartzJobNameArr, trigger.getClass());


            Long cuckooJobId = Long.valueOf(quartzJobNameArr[0]);

            // 根据jobId找到任务信息
            final CuckooJobDetail cuckooJobDetail = cuckooJobDetailMapper.lockByPrimaryKey(cuckooJobId);
            if (null == cuckooJobDetail) {
                log.error("can not find cuckoojob in quartzExecutor by jobGroup:{},jobName:{}", jobKey.getGroup(),
                        jobKey.getName());
                throw new BaseException("can not find cuckoojob in quartzExecutor by jobGroup:{},jobName:{}",
                        jobKey.getGroup(), jobKey.getName());
            }
            try {
                cuckooJobExecLog = cuckooJobLogService.initSysCronJobLog(cuckooJobDetail, scheduledFireTime);
            } catch (JobUndailyLogBreakException e) {

                log.error("init log error:{}", e.getMessage());
                return;
            }

        } else if (trigger instanceof SimpleTriggerImpl) {
            // Simple jobName == jobId_logId
            Long execIdObj = Long.valueOf(quartzJobNameArr[1]);
            log.debug("quartz trigger flow job, jobGroup:{},jobName:{},execIdObj:{},triggerType:{}", jobKey.getGroup(), jobKey.getName(), execIdObj, trigger.getClass());

            Long execId = Long.valueOf(String.valueOf(execIdObj));
            // 如果日志ID不为空，表示当前日志是通过上级任务触发或者是有等待执行的任务
            cuckooJobExecLog = cuckooJobExecLogMapper.selectByPrimaryKey(execId);
        }

        if (!cuckooJobExecutor.executeQuartzJob(cuckooJobExecLog)) {
            cuckooJobService.rependingJob(cuckooJobExecLog);
        }
    }

}
