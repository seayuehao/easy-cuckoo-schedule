package org.ecs.schedule.service.impl;

import org.ecs.schedule.bean.JobInfoBean;
import org.ecs.schedule.executor.annotation.CuckooTask;
import org.ecs.schedule.service.CuckooTestDailyJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class CuckooTestDailyJobImpl implements CuckooTestDailyJob {

    private static final int TIME = (int) TimeUnit.SECONDS.toMillis(10L);

    @Override
    @CuckooTask("testCronDailySucceed")
    public void testCronDailySucceed(JobInfoBean jobInfo) {
        try {
            Thread.sleep(new Random().nextInt(TIME));
        } catch (InterruptedException e) {
            log.error("testCronDailySucceed error: ", e);
        }
        log.info("Client exec done ,testCronDailySucceed:{}", jobInfo);
    }

    @Override
    @CuckooTask("testCronDailyFailed")
    public void testCronDailyFailed(JobInfoBean jobInfo) {
        try {
            Thread.sleep(new Random().nextInt(TIME));
        } catch (InterruptedException e) {
            log.error("testCronDailyFailed error: ", e);
        }
        log.info("Client exec done ,testCronDailyFailed:{}", jobInfo);
    }

    @Override
    @CuckooTask("testCronDailyDependencySucceed")
    public void testCronDailyDependencySucceed(JobInfoBean jobInfo) {
        try {
            Thread.sleep(new Random().nextInt(TIME));
        } catch (InterruptedException e) {
            log.error("testCronDailyDependencySucceed error: ", e);
        }
        log.info("Client exec done ,testCronDailyDependencySucceed:{}", jobInfo);
    }

    @Override
    @CuckooTask("testFlowDailySucceed")
    public void testFlowDailySucceed(JobInfoBean jobInfo) {
        try {
            Thread.sleep(new Random().nextInt(TIME));
        } catch (InterruptedException e) {
            log.error("testFlowDailySucceed error: ", e);
        }
        log.info("Client exec done ,testFlowDailySucceed:{}", jobInfo);
    }

    @Override
    @CuckooTask("testFlowDailyFailed")
    public void testFlowDailyFailed(JobInfoBean jobInfo) {
        try {
            Thread.sleep(new Random().nextInt(TIME));
        } catch (InterruptedException e) {
            log.error("testFlowDailyFailed error: ", e);
        }
        log.info("Client exec done ,testFlowDailyFailed:{}", jobInfo);
    }

    @Override
    @CuckooTask("testFlowDailyDependencySucceed")
    public void testFlowDailyDependencySucceed(JobInfoBean jobInfo) {
        try {
            Thread.sleep(new Random().nextInt(TIME));
        } catch (InterruptedException e) {
            log.error("testFlowDailyDependencySucceed error: ", e);
        }
        log.info("Client exec done ,testFlowDailyDependencySucceed:{}", jobInfo);
    }

}
