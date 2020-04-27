package org.ecs.schedule.service;

import org.ecs.schedule.bean.JobInfoBean;

public interface CuckooTestOther {


    void testCronJobAutoInit(JobInfoBean jobInfo);


    void testCronJobOverTime(JobInfoBean jobInfo);

}
