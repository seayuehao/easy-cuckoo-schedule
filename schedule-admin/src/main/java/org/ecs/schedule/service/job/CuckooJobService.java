package org.ecs.schedule.service.job;

import org.ecs.schedule.domain.exec.CuckooJobDetail;
import org.ecs.schedule.domain.exec.CuckooJobExecLog;
import org.ecs.schedule.qry.job.JobInfoQry;
import org.ecs.schedule.vo.job.CuckooJobDetailVo;
import org.ecs.util.dao.PageDataList;

import java.util.List;
import java.util.Map;

/**
 * 任务执行服务接口
 */
public interface CuckooJobService {

    /**
     * 新增一个任务,返回任务id
     */
    Long addJob(CuckooJobDetailVo jobInfo);


    /**
     * 删除一个任务
     */
    void removeJob(Long id);

    /**
     * 修改一个任务
     */
    void modifyJob(CuckooJobDetailVo jobInfo);


    /**
     * 暂停一个任务
     */
    void pauseOneJob(Long id);

    /**
     * 暂停所有任务
     */
    void pauseAllJob(JobInfoQry jobInfo);

    /**
     * 恢复一个任务
     */
    void resumeOneJob(Long id);

    /**
     * 恢复所有任务
     */
    void resumeAllJob(JobInfoQry jobInfo);

    /**
     * 将任务重置为PENDING状态 -- 返回最新任务的ID
     *
     * @param jobDetail
     * @param fatherJobLog
     */
    Long pendingJob(CuckooJobDetail jobDetail, CuckooJobExecLog fatherJobLog);


    /**
     * Pending任务执行的时候，发现还不具备执行任务的条件，因此重新放回pending队列
     *
     * @param jobLog
     */
    void rependingJob(CuckooJobExecLog jobLog);


    /**
     * 根据ID查询任务明细
     *
     * @param jobId
     * @return
     */
    CuckooJobDetail getJobById(Long jobId);

    /**
     * 根据ID查询下级带触发任务
     *
     * @param jobId
     * @return
     */
    List<CuckooJobDetail> getNextJobById(Long jobId);

    /**
     * 分页查询任务数据
     *
     * @param jobInfo
     * @return
     */
    PageDataList<CuckooJobDetail> pageList(JobInfoQry jobInfo);


    /**
     * 查询所有客户端应用名称
     *
     * @return
     */
    Map<String, String> findAllApps();


    /**
     * 手工触发非日切任务
     *
     * @param jobId
     * @param needTriggleNext
     * @param lastTime
     * @param curTime
     * @param forceTrigger
     */
    void triggerUnDailyJob(Long jobId, Boolean needTriggleNext, Long lastTime, Long curTime, boolean forceTrigger);


    /**
     * 手工触发日切任务
     *
     * @param jobId
     * @param needTriggleNext
     * @param txDate
     * @param foreTriggle
     */
    void triggerDailyJob(Long jobId, Boolean needTriggleNext, Integer txDate, boolean foreTriggle);


    /**
     * 执行任务，无执行业务日期参数
     *
     * @param jobId
     * @param needTriggleNext
     * @param foreTriggle
     */
    void triggerJob(Long jobId, Boolean needTriggleNext, boolean foreTriggle);

    /**
     * 根据GroupId查询出任务信息
     *
     * @param groupId
     * @return
     */
    List<CuckooJobDetail> getJobsByGroupId(Long groupId);


    /**
     * 检查Cron任务是否初始化quartz信息
     *
     * @param jobDetail
     */
    boolean checkCronQuartzInit(CuckooJobDetail jobDetail);

}
