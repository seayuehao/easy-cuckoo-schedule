package org.ecs.schedule.component.quartz;

import org.ecs.schedule.constant.CuckooJobConstant;
import org.ecs.schedule.domain.exec.CuckooJobExecLog;
import org.ecs.schedule.enums.CuckooBooleanFlag;
import org.ecs.schedule.enums.CuckooJobStatus;
import org.ecs.schedule.exception.BaseException;
import lombok.extern.slf4j.Slf4j;
import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.SimpleTrigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;

@Slf4j
@Component
public class QuartzManager {

    @Resource(name = "quartzScheduler")
    private Scheduler scheduler;

    static final String quartzCronGroup = "quartz_cron";
    static final String quartzSimpleGroup = "quartz_simple";
    static final String quartzAutoGroup = "quartz_auto";

    /**
     * 增加自动执行任务
     */
    public void addAutoJob() {

        String quartzJobName = "autoCheckJob";
        // TriggerKey : name + group
        TriggerKey triggerKey = TriggerKey.triggerKey(quartzJobName, quartzAutoGroup);
        JobKey jobKey = new JobKey(quartzJobName, quartzAutoGroup);

        // JobDetail : jobClass
        Class<? extends Job> jobClass_ = QuartzAutoJobExecutor.class;

        JobDetail jobDetail = JobBuilder.newJob(jobClass_).withIdentity(jobKey).build();

        // 每十分钟
        CronScheduleBuilder cronScheduleBuilder = CronScheduleBuilder.cronSchedule("0 0/10 * * * ?")
                .withMisfireHandlingInstructionDoNothing();
        CronTrigger cronTrigger = TriggerBuilder.newTrigger().withIdentity(triggerKey).withSchedule(cronScheduleBuilder)
                .build();
        try {
            if (!scheduler.checkExists(jobKey)) {
                scheduler.scheduleJob(jobDetail, cronTrigger);
            }
        } catch (SchedulerException e) {
            log.error("init QuartzAutoJobExecutor error:{}", e.getMessage(), e);
            throw new BaseException(e.getMessage());
        }
    }


    public void addCronJob(String jobId, String cronExpression, CuckooJobStatus jobStatus, CuckooBooleanFlag typeDaily) {
        String quartzJobName = jobId;
        // TriggerKey : name + group
        TriggerKey triggerKey = TriggerKey.triggerKey(quartzJobName, quartzCronGroup);
        JobKey jobKey = new JobKey(quartzJobName, quartzCronGroup);

        // JobDetail : jobClass
        Class<? extends Job> jobClass_ = QuartzJobExecutor.class; // Class.forName(jobInfo.getJobClass());

        JobDetail jobDetail = JobBuilder.newJob(jobClass_).withIdentity(jobKey).build();

        // CronTrigger : TriggerKey + cronExpression //
        // withMisfireHandlingInstructionDoNothing 忽略掉调度终止过程中忽略的调度
        CronScheduleBuilder cronScheduleBuilder = CronScheduleBuilder.cronSchedule(cronExpression);
//				withMisfireHandlingInstructionDoNothing
//				——不触发立即执行
//				——等待下次Cron触发频率到达时刻开始按照Cron频率依次执行
//				withMisfireHandlingInstructionIgnoreMisfires
//				——以错过的第一个频率时间立刻开始执行
//				——重做错过的所有频率周期后
//				——当下一次触发频率发生时间大于当前时间后，再按照正常的Cron频率依次执行
//				withMisfireHandlingInstructionFireAndProceed
//				——以当前时间为触发频率立刻触发一次执行
//				——然后按照Cron频率依次执行
        if (CuckooBooleanFlag.YES.getValue().equals(typeDaily.getValue())) {
            // 日切任务遗漏任务自动触发
            cronScheduleBuilder.withMisfireHandlingInstructionIgnoreMisfires();
        } else {
            // 非日切任务忽略遗漏任务
            cronScheduleBuilder.withMisfireHandlingInstructionDoNothing();
        }

        CronTrigger cronTrigger = TriggerBuilder.newTrigger().withIdentity(triggerKey).withSchedule(cronScheduleBuilder)
                .build();

        try {
            scheduler.scheduleJob(jobDetail, cronTrigger);
            if (CuckooJobStatus.PAUSE.equals(jobStatus)) {

                scheduler.pauseTrigger(triggerKey);
            }
        } catch (SchedulerException e) {
            log.error("新增CRON任务调度失败:{}", e.getMessage(), e);
            throw new BaseException(e.getMessage());
        }
    }


    public void deleteCronJob(String jobId) {

        String quartzJobName = jobId;
        JobKey jobKey = new JobKey(quartzJobName, quartzCronGroup);
        try {
            if (checkCronExists(jobId)) {
                scheduler.deleteJob(jobKey);
            }
        } catch (SchedulerException e) {
            log.error("删除任务调度失败:{}", e.getMessage(), e);
            throw new BaseException(e.getMessage());
        }
    }


    public void modfyCronJob(String jobId, String cronExpression, CuckooJobStatus jobStatus, CuckooBooleanFlag typeDaily) {

        String quartzJobName = jobId;
        JobKey jobKey = new JobKey(quartzJobName, quartzCronGroup);
        try {
            if (checkCronExists(jobId)) {
                scheduler.deleteJob(jobKey);
                addCronJob(jobId, cronExpression, jobStatus, typeDaily);
            }
        } catch (SchedulerException e) {
            log.error("修改任务调度失败:{}", e.getMessage(), e);
            throw new BaseException(e.getMessage());
        }
    }


    public boolean checkCronExists(String jobId) {
        String quartzJobName = jobId;
        JobKey jobKey = new JobKey(quartzJobName, quartzCronGroup);
        TriggerKey triggerKey = TriggerKey.triggerKey(quartzJobName, quartzCronGroup);

        try {

            if (scheduler.checkExists(triggerKey)) {
                return true;
            } else {
                if (scheduler.checkExists(jobKey)) {
                    scheduler.deleteJob(jobKey);
                    return false;
                }
                return false;
            }

        } catch (SchedulerException e) {
            return false;
        }
    }

    public void pauseCronJob(String jobId) {

        String quartzJobName = jobId;
        JobKey jobKey = new JobKey(quartzJobName, quartzCronGroup);
        try {
            if (checkCronExists(jobId)) {
                scheduler.pauseJob(jobKey);
            }
        } catch (SchedulerException e) {
            log.error("暂停任务调度失败:jobGroup:{},jobName:{},{}", quartzCronGroup, quartzJobName, e.getMessage(), e);
            throw new BaseException(e.getMessage());
        }
    }


    public void pauseAll() {
        try {
            scheduler.pauseAll();
        } catch (SchedulerException e) {
            log.error("暂停所有任务调度失败:{}", e.getMessage(), e);
            throw new BaseException(e.getMessage());
        }
    }

    public void resumeCronJob(String jobId) {

        String quartzJobName = jobId;
        JobKey jobKey = new JobKey(quartzJobName, quartzCronGroup);
        try {
            if (checkCronExists(jobId)) {
                scheduler.resumeJob(jobKey);
            }
        } catch (SchedulerException e) {
            log.error("恢复任务调度失败:jobGroup:{},jobName:{},{}", quartzCronGroup, quartzJobName, e.getMessage(), e);
            throw new BaseException(e.getMessage());
        }
    }

    public void resumeAll() {

        try {
            scheduler.resumeAll();
        } catch (SchedulerException e) {
            log.error("恢复所有任务调度失败:{}", e.getMessage(), e);
            throw new BaseException(e.getMessage());
        }
    }


    public void addSimpleJob(CuckooJobExecLog jobLog, Long waitTime) {
        String quartzJobName = jobLog.getJobId() + CuckooJobConstant.QUARTZ_JOBNAME_JOINT + jobLog.getId();
        TriggerKey triggerKey = TriggerKey.triggerKey(quartzJobName, quartzSimpleGroup);
        JobKey jobKey = new JobKey(quartzJobName, quartzSimpleGroup);
        try {

            Class<? extends Job> jobClass_ = QuartzJobExecutor.class; // Class.forName(jobInfo.getJobClass());
            JobDetail jobDetail = JobBuilder.newJob(jobClass_).withIdentity(jobKey).build();

            SimpleScheduleBuilder simpleScheduleBuilder = SimpleScheduleBuilder
                    .repeatMinutelyForTotalCount(1) // 只触发一次

//					withMisfireHandlingInstructionFireNow
//					——以当前时间为触发频率立即触发执行
//					——执行至FinalTIme的剩余周期次数
//					——以调度或恢复调度的时刻为基准的周期频率，FinalTime根据剩余次数和当前时间计算得到
//					——调整后的FinalTime会略大于根据starttime计算的到的FinalTime值
//
//					withMisfireHandlingInstructionIgnoreMisfires
//					——以错过的第一个频率时间立刻开始执行
//					——重做错过的所有频率周期
//					——当下一次触发频率发生时间大于当前时间以后，按照Interval的依次执行剩下的频率
//					——共执行RepeatCount+1次
//
//					withMisfireHandlingInstructionNextWithExistingCount
//					——不触发立即执行
//					——等待下次触发频率周期时刻，执行至FinalTime的剩余周期次数
//					——以startTime为基准计算周期频率，并得到FinalTime
//					——即使中间出现pause，resume以后保持FinalTime时间不变
//
//
//					withMisfireHandlingInstructionNowWithExistingCount
//					——以当前时间为触发频率立即触发执行
//					——执行至FinalTIme的剩余周期次数
//					——以调度或恢复调度的时刻为基准的周期频率，FinalTime根据剩余次数和当前时间计算得到
//					——调整后的FinalTime会略大于根据starttime计算的到的FinalTime值
//
//					withMisfireHandlingInstructionNextWithRemainingCount
//					——不触发立即执行
//					——等待下次触发频率周期时刻，执行至FinalTime的剩余周期次数
//					——以startTime为基准计算周期频率，并得到FinalTime
//					——即使中间出现pause，resume以后保持FinalTime时间不变
//
//					withMisfireHandlingInstructionNowWithRemainingCount
//					——以当前时间为触发频率立即触发执行
//					——执行至FinalTIme的剩余周期次数
//					——以调度或恢复调度的时刻为基准的周期频率，FinalTime根据剩余次数和当前时间计算得到
//					——调整后的FinalTime会略大于根据starttime计算的到的FinalTime值

                    .withMisfireHandlingInstructionNowWithExistingCount();
            SimpleTrigger simpleTrigger = TriggerBuilder.newTrigger().withIdentity(triggerKey)
                    .withSchedule(simpleScheduleBuilder)
                    .startAt(new Date(System.currentTimeMillis() + waitTime)) //  设置起始时间
                    .build();

            if (checkExisting(jobLog)) {
                scheduler.deleteJob(jobKey);
            }
            scheduler.scheduleJob(jobDetail, simpleTrigger);

        } catch (SchedulerException e) {
            log.error("add simple job failed, groupName:{}, jobName:{},error:{}", quartzSimpleGroup, quartzJobName, e.getMessage(), e);
            throw new BaseException(e.getMessage());
        }
    }


    public boolean checkExisting(CuckooJobExecLog jobLog) {
        String quartzJobName = jobLog.getJobId() + CuckooJobConstant.QUARTZ_JOBNAME_JOINT + jobLog.getId();
        JobKey jobKey = new JobKey(quartzJobName, quartzSimpleGroup);
        TriggerKey triggerKey = TriggerKey.triggerKey(quartzJobName, quartzSimpleGroup);
        try {

            if (scheduler.checkExists(triggerKey)) {
                return true;
            } else {
                if (scheduler.checkExists(jobKey)) {
                    scheduler.deleteJob(jobKey);
                    return false;
                }
                return false;
            }
        } catch (SchedulerException e) {
            log.error("checkSimpleExistbErr: ", e);
            return false;
        }
    }


    public void deleteSimpleJob(CuckooJobExecLog jobLog) {
        String quartzJobName = jobLog.getJobId() + CuckooJobConstant.QUARTZ_JOBNAME_JOINT + jobLog.getId();
        JobKey jobKey = new JobKey(quartzJobName, quartzSimpleGroup);

        try {
            if (checkExisting(jobLog)) {
                scheduler.deleteJob(jobKey);
            }
        } catch (SchedulerException e) {
            log.error("deleteSimpleJobErr: ", e);
        }
    }


}
