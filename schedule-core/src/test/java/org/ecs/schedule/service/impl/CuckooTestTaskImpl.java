package org.ecs.schedule.service.impl;

import org.springframework.stereotype.Service;

import org.ecs.schedule.bean.JobInfoBean;
import org.ecs.schedule.executor.annotation.CuckooTask;
import org.ecs.schedule.service.CuckooTestTask;

@Service
public class CuckooTestTaskImpl implements CuckooTestTask {

	@Override
	@CuckooTask("testJob")
	public void testJob(JobInfoBean jobInfo) {
		System.out.println("CuckooTestTaskImpl testJob");
	}

	@Override
	public void testJobTmp(JobInfoBean jobInfo) {
		System.out.println("CuckooTestTaskImpl testJobTmp");
	}

}
