package org.ecs.schedule.service.impl;

import org.ecs.schedule.bean.JobInfoBean;
import org.ecs.schedule.executor.annotation.CuckooTask;
import org.ecs.schedule.service.CuckooTestUnDailyJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class CuckooTestUnDailyJobImpl implements CuckooTestUnDailyJob {

    private static final int TIME = (int) TimeUnit.MINUTES.toMillis(60);

    @Override
    @CuckooTask("testCronUnDailySucced")
    public void testCronUnDailySucceed(JobInfoBean jobInfo) {
        try {
            Thread.sleep(new Random().nextInt(TIME));
        } catch (InterruptedException e) {
            log.error("testCronUnDailySucceed error:", e);
        }
        log.info("Client exec done ,testCronUnDailySucceed:{}", jobInfo);
    }

    @Override
    @CuckooTask("testCronUnDailyFailed")
    public void testCronUnDailyFailed(JobInfoBean jobInfo) {
        try {
            Thread.sleep(new Random().nextInt(TIME));
        } catch (InterruptedException e) {
            log.error("testCronUnDailyFailed error:", e);
        }
        log.info("Client exec done ,testCronUnDailyFailed:{}", jobInfo);
    }

    @Override
    @CuckooTask("testFlowUnDailySucced")
    public void testFlowUnDailySucceed(JobInfoBean jobInfo) {
        try {
            Thread.sleep(new Random().nextInt(TIME));
        } catch (InterruptedException e) {
            log.error("testFlowUnDailySucceed error:", e);
        }
        log.info("Client exec done ,testFlowUnDailySucceed:{}", jobInfo);
    }

    @Override
    @CuckooTask("testFlowUnDailyFailed")
    public void testFlowUnDailyFailed(JobInfoBean jobInfo) {
        try {
            Thread.sleep(new Random().nextInt(TIME));
        } catch (InterruptedException e) {
            log.error("testFlowUnDailyFailed error:", e);
        }
        log.info("Client exec done ,testFlowUnDailyFailed:{}", jobInfo);
    }

}
