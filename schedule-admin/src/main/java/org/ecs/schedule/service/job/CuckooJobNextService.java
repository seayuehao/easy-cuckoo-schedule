package org.ecs.schedule.service.job;

import org.ecs.schedule.vo.job.JobNext;

import java.util.List;

public interface CuckooJobNextService {


    /**
     * 设置触发的后续任务
     */
    void setNextJobConfig(List<JobNext> nextJobs);

    /**
     * 根据上级任务ID查询下级任务id
     *
     * @param jobId
     * @return
     */
    List<Long> findNextJobIdByJobId(Long jobId);

    /**
     * 根据下级任务ID查询上级任务ID
     *
     * @param id
     * @return
     */
    Long findJobIdByNextJobId(Long id);

    /**
     * 新增触发任务关系
     *
     * @param jobId
     * @param nextJobId
     */
    void addOrUpdate(Long jobId, Long nextJobId);

    /**
     * 删除某个任务的触发任务关系
     *
     * @param id
     */
    void deletePreJob(Long id);

}