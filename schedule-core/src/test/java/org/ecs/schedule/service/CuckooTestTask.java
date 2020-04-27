package org.ecs.schedule.service;

import org.ecs.schedule.bean.JobInfoBean;

public interface CuckooTestTask {

    void testJob(JobInfoBean jobInfo);

    void testJobTmp(JobInfoBean jobInfo);

}
