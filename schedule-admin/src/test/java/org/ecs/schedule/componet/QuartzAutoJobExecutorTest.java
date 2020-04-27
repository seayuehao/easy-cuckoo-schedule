package org.ecs.schedule.componet;

import org.apache.commons.collections.CollectionUtils;
import org.ecs.schedule.ServiceUnitBaseTest;
import org.ecs.schedule.component.quartz.QuartzJobExecutor;
import org.ecs.schedule.component.quartz.QuartzManager;
import org.ecs.schedule.constant.CuckooJobConstant;
import org.ecs.schedule.domain.exec.CuckooJobExecLog;
import org.ecs.schedule.enums.CuckooJobExecStatus;
import org.ecs.schedule.exception.BaseException;
import org.ecs.schedule.qry.job.JobLogQry;
import org.ecs.schedule.service.job.CuckooJobLogService;
import org.ecs.util.dao.PageDataList;
import org.junit.Test;
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
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class QuartzAutoJobExecutorTest extends ServiceUnitBaseTest {

    @Autowired
    CuckooJobLogService cuckooJobLogService;

    @Autowired
    QuartzManager quartzManage;

    @Resource(name = "quartzScheduler")
    private Scheduler scheduler;

    @Test
    public void checkSimpleExist() {
        JobLogQry qry = new JobLogQry();
        qry.setJobStatus(Arrays.asList(new String[]{CuckooJobExecStatus.PENDING.getValue()}));
        qry.setLimit(1000);
        for (int i = 0; ; i++) {
            qry.setStart(i * qry.getLimit());
            PageDataList<CuckooJobExecLog> page = cuckooJobLogService.pageByQry(qry);
            List<CuckooJobExecLog> logs = page.getRows();
            if (CollectionUtils.isNotEmpty(logs)) {

                for (CuckooJobExecLog cuckooJobExecLog : logs) {
                    System.out.println(cuckooJobExecLog.getId());
                    if (!quartzManage.checkExisting(cuckooJobExecLog)) {
                        System.out.println("simple job not exist:" + cuckooJobExecLog.getId());
                    }
                }
            } else {
                break;
            }
        }
    }

    String quartzSimpleGroup = "quartz_simple";

    @Test
    public void addSimpleTest() {
        CuckooJobExecLog jobLog = cuckooJobLogService.getJobLogByLogId(3146L);
        String quartzJobName = jobLog.getJobId() + CuckooJobConstant.QUARTZ_JOBNAME_JOINT + jobLog.getId();
        TriggerKey triggerKey = TriggerKey.triggerKey(quartzJobName, quartzSimpleGroup);
        JobKey jobKey = new JobKey(quartzJobName, quartzSimpleGroup);
        try {

            Class<? extends Job> jobClass_ = QuartzJobExecutor.class;
            JobDetail jobDetail = JobBuilder.newJob(jobClass_).withIdentity(jobKey).build();

            SimpleScheduleBuilder simpleScheduleBuilder = SimpleScheduleBuilder
                    .repeatMinutelyForTotalCount(1)
                    .withMisfireHandlingInstructionNowWithExistingCount();
            SimpleTrigger simpleTrigger = TriggerBuilder.newTrigger().withIdentity(triggerKey)
                    .withSchedule(simpleScheduleBuilder)
                    .startAt(new Date(System.currentTimeMillis()))
                    .build();

            if (scheduler.checkExists(triggerKey)) {
                scheduler.deleteJob(jobKey);
            } else {
                if (scheduler.checkExists(jobKey)) {
                    scheduler.deleteJob(jobKey);
                }
            }
            scheduler.scheduleJob(jobDetail, simpleTrigger);
        } catch (SchedulerException e) {
            throw new BaseException(e.getMessage());
        }
    }
}
