package org.ecs.schedule.service;

import org.ecs.schedule.bean.JobInfoBean;

public interface CuckooTestUnDailyJob {

    // 测试Cron触发非日切任务 -- 代码执行成功
    void testCronUnDailySucceed(JobInfoBean jobInfo);

    // 测试Cron触发非日切任务 -- 代码执行失败
    void testCronUnDailyFailed(JobInfoBean jobInfo);

    // 测试上级任务触发非日切任务 -- 代码执行成功
    void testFlowUnDailySucceed(JobInfoBean jobInfo);

    // 测试上级任务触发非日切任务 -- 代码执行失败
    void testFlowUnDailyFailed(JobInfoBean jobInfo);

}
