package org.ecs.schedule.util;

import org.ecs.schedule.domain.exec.CuckooJobDependency;
import org.ecs.schedule.domain.exec.CuckooJobNextJob;
import org.ecs.schedule.vo.job.JobDependency;
import org.ecs.schedule.vo.job.JobNext;

public class CuckBeanUtil {

    public static CuckooJobNextJob parseJobNext(JobNext jobNext) {
        CuckooJobNextJob cuckooJobNextJob = new CuckooJobNextJob();
        cuckooJobNextJob.setJobId(jobNext.getJobId());
        cuckooJobNextJob.setNextJobId(jobNext.getNextJobId());
        return cuckooJobNextJob;
    }

    public static CuckooJobDependency parseJobDependency(JobDependency jobDependency) {
        CuckooJobDependency cuckooJobDependency = new CuckooJobDependency();
        cuckooJobDependency.setJobId(jobDependency.getJobId());
        cuckooJobDependency.setDependencyJobId(jobDependency.getDependencyId());
        return cuckooJobDependency;
    }

}
