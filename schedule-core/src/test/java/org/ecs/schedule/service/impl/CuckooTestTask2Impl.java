package org.ecs.schedule.service.impl;

import org.springframework.stereotype.Service;

import org.ecs.schedule.bean.JobInfoBean;
import org.ecs.schedule.executor.annotation.CuckooTask;
import org.ecs.schedule.service.CuckooTestTask2;

@Service
public class CuckooTestTask2Impl implements CuckooTestTask2 {

	@Override
	@CuckooTask("testJob2")
	public void testJob(JobInfoBean jobInfo) {

		System.out.println("CuckooTestTask2Impl testJob");
	}

}
