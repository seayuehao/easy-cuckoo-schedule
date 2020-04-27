package org.ecs.schedule.service.job;

import org.ecs.schedule.domain.exec.CuckooJobExecLog;
import org.ecs.schedule.vo.job.JobDependency;

import java.util.List;

public interface CuckooJobDependencyService {

    /**
     * 设置依赖
     */
    void setDependencyJobConfig(List<JobDependency> dependencyJobs);

    /**
     * 检查任务依赖状态
     *
     * @param jobInfo
     */
    boolean checkDependencyJobFinished(CuckooJobExecLog jobInfo);

    /**
     * 根据任务ID查找任务依赖的其他任务ID
     *
     * @param jobId
     * @return
     */
    List<Long> listDependencyIdsByJobId(Long jobId);

    /**
     * 先删除后增加
     *
     * @param jobId
     * @param dependencyIds
     */
    void addOrUpdateJobDependency(Long jobId, String[] dependencyIds);


}
