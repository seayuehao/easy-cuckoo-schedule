package org.ecs.schedule.service.job;

import org.ecs.schedule.domain.exec.CuckooJobDetail;
import org.ecs.schedule.domain.exec.CuckooJobExecLog;
import org.ecs.schedule.enums.CuckooJobExecStatus;
import org.ecs.schedule.exception.JobUndailyLogBreakException;
import org.ecs.schedule.qry.QryBase;
import org.ecs.schedule.qry.job.JobLogQry;
import org.ecs.util.dao.PageDataList;

import java.util.Date;
import java.util.List;

public interface CuckooJobLogService {

    /**
     * 新增日志
     *
     * @param log
     * @return
     */
    Long insertSelective(CuckooJobExecLog log);

    /**
     * 根据日志ID获取日志信息
     *
     * @param id
     * @return
     */
    CuckooJobExecLog getJobLogByLogId(Long id);

    /**
     * 根据主键更新日志状态
     *
     * @param id
     * @param succed
     * @param message
     */
    void updateJobLogStatusById(Long id, CuckooJobExecStatus cuckooJobExecStatus, String message);


    /**
     * 按主键修改日志
     *
     * @param cuckooJobExecLogs
     */
    void updateJobLogByPk(CuckooJobExecLog cuckooJobExecLogs);

    /**
     * 初始化Cron类型任务触日志(如果是依赖类型的任务，有上级任务触发下级任务的时候初始化执行日志)
     *
     * @param cuckooJobDetail
     * @param scheduledFireTime
     * @return
     */
    CuckooJobExecLog initSysCronJobLog(CuckooJobDetail cuckooJobDetail, Date scheduledFireTime) throws JobUndailyLogBreakException;

    /**
     * 控制台执行非日切任务，初始化日志
     *
     * @param cuckooJobDetail
     * @param needTriggleNext
     * @param flowLastTime
     * @param flowCurTime
     * @param foreTriggle
     * @return
     */
    CuckooJobExecLog initUnDailyJobLog(CuckooJobDetail cuckooJobDetail, Boolean needTriggleNext, Long flowLastTime, Long flowCurTime, boolean foreTriggle);

    /**
     * 控制台执行日切任务，初始化日志
     *
     * @param cuckooJobDetail
     * @param needTriggleNext
     * @param txDate
     * @param foreTriggle
     * @return
     */
    CuckooJobExecLog initDailyJobLog(CuckooJobDetail cuckooJobDetail, Boolean needTriggleNext, Integer txDate, boolean foreTriggle);

    /**
     * 控制台执行无业务日期任务，初始化日志
     *
     * @param cuckooJobDetail
     * @param needTriggleNext
     * @param foreTriggle
     * @return
     */
    CuckooJobExecLog initJobLog(CuckooJobDetail cuckooJobDetail, Boolean needTriggleNext, boolean foreTriggle);

    /**
     * 任务执行日志分页查询
     *
     * @param qry
     * @return
     */
    PageDataList<CuckooJobExecLog> pageByQry(JobLogQry qry);

    /**
     * 修改任务状态
     *
     * @param logId
     * @param cuckooJobExecStatus
     */
    void resetLogStatus(Long logId, CuckooJobExecStatus cuckooJobExecStatus);

    /**
     * 查询超时任务
     *
     * @param qry
     * @return
     */
    PageDataList<CuckooJobExecLog> pageOverTimeJobs(QryBase qry);

    /**
     * 检查上一个任务是否执行成功
     *
     * @param jobLog
     * @return
     */
    boolean checkPreLogIsDone(CuckooJobExecLog jobLog);

    /**
     * 查询Pending || Running 状态任务（任务状态不是暂停状态的）
     *
     * @param qry
     * @return
     */
    PageDataList<CuckooJobExecLog> pagePendingList(QryBase qry);

    /**
     * 获得触发任务执行状态
     *
     * @param cuckooJobExecLog
     * @return
     */
    CuckooJobExecLog getPreJobLogs(CuckooJobExecLog cuckooJobExecLog);

    /**
     * 获得依赖任务执行日志
     *
     * @param cuckooJobExecLog
     * @return
     */
    List<CuckooJobExecLog> getDependencyJobs(CuckooJobExecLog cuckooJobExecLog);

    /**
     * 获得下级任务日志
     *
     * @param cuckooJobExecLog
     * @return
     */
    List<CuckooJobExecLog> getNextJobs(CuckooJobExecLog cuckooJobExecLog);


}
