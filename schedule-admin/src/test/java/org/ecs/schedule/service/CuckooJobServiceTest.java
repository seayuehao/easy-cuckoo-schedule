package org.ecs.schedule.service;

import org.ecs.schedule.ServiceUnitBaseTest;
import org.ecs.schedule.enums.CuckooBooleanFlag;
import org.ecs.schedule.enums.CuckooJobStatus;
import org.ecs.schedule.enums.CuckooJobTriggerType;
import org.ecs.schedule.service.job.CuckooJobService;
import org.ecs.schedule.vo.job.CuckooJobDetailVo;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class CuckooJobServiceTest extends ServiceUnitBaseTest {

    @Autowired
    CuckooJobService cuckooJobService;

    private static Long groupId = 5L;

    @Test
    public void testAddCronJob() {

        CuckooJobDetailVo jobInfo = new CuckooJobDetailVo();
        jobInfo.setGroupId(groupId);
        jobInfo.setCronExpression("0/5 * * * * ?");
        jobInfo.setJobClassApplication("member");
        jobInfo.setJobDesc("描述：测试cron任务");
        jobInfo.setJobName("testJob");
        jobInfo.setJobStatus(CuckooJobStatus.RUNNING.getValue());
        jobInfo.setTypeDaily(CuckooBooleanFlag.NO.getValue());
        jobInfo.setOffset(-1);
        jobInfo.setTriggerType(CuckooJobTriggerType.CRON.getValue());

        cuckooJobService.addJob(jobInfo);
    }


    @Test
    public void testAddSimpleJob() {

        CuckooJobDetailVo jobInfo = new CuckooJobDetailVo();
        jobInfo.setGroupId(groupId);
        jobInfo.setJobClassApplication("member");
        jobInfo.setJobDesc("描述：测试flow任务");
        jobInfo.setJobName("testJob2");
        jobInfo.setCronExpression("0 0 1 * * ?");
        jobInfo.setTypeDaily(CuckooBooleanFlag.YES.getValue());
        jobInfo.setJobStatus(CuckooJobStatus.RUNNING.getValue());
        jobInfo.setTriggerType(CuckooJobTriggerType.JOB.getValue());

        cuckooJobService.addJob(jobInfo);
    }

}