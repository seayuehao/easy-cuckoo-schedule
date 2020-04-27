package org.ecs.schedule.service;

import org.ecs.schedule.bean.JobInfoBean;

public interface CuckooTestDailyJob {

    // 测试Cron触日切任务 -- 代码执行成功
    void testCronDailySucceed(JobInfoBean jobInfo);


    // 测试Cron触日切任务 -- 代码执行失败
    void testCronDailyFailed(JobInfoBean jobInfo);

    // 测试Cron触发日切有依赖任务
    void testCronDailyDependencySucceed(JobInfoBean jobInfo);

    // 测试上级任务触发日切任务 -- 代码执行成功
    void testFlowDailySucceed(JobInfoBean jobInfo);


    // 测试上级任务触发日切任务 -- 代码执行失败
    void testFlowDailyFailed(JobInfoBean jobInfo);


    // 测试上级任务触发日切有依赖任务
    void testFlowDailyDependencySucceed(JobInfoBean jobInfo);

}
