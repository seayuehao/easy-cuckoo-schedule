package org.ecs.schedule.service.impl;

import org.ecs.schedule.bean.JobInfoBean;
import org.ecs.schedule.exception.BaseException;
import org.ecs.schedule.executor.annotation.CuckooTask;
import org.ecs.schedule.service.CuckooTestOther;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class CuckooTestOtherImpl implements CuckooTestOther {


    @Override
    @CuckooTask("testCronJobAutoInit")
    public void testCronJobAutoInit(JobInfoBean jobInfo) {
        log.info("Client exec done ,testCronJobAutoInit:{}", jobInfo);
    }

    @Override
    @CuckooTask("testCronJobOverTime")
    public void testCronJobOverTime(JobInfoBean jobInfo) {
        try {
            Thread.sleep(60 * 60 * 1000);
        } catch (InterruptedException e) {
            throw new BaseException(e.getMessage());
        }
        log.info("Client exec done ,testCronJobOverTime:{}", jobInfo);
    }

}
