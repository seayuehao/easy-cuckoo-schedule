package org.ecs.schedule.service.job.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.ecs.schedule.component.mail.NotificationEmailSupport;
import org.ecs.schedule.component.quartz.QuartzManager;
import org.ecs.schedule.dao.exec.CuckooJobExecLogMapper;
import org.ecs.schedule.dao.exec.CuckooJobExecLogSubMapper;
import org.ecs.schedule.dao.exec.CuckooJobExtendMapper;
import org.ecs.schedule.domain.exec.CuckooJobDetail;
import org.ecs.schedule.domain.exec.CuckooJobExecLog;
import org.ecs.schedule.domain.exec.CuckooJobExecLogCriteria;
import org.ecs.schedule.domain.exec.CuckooJobExtend;
import org.ecs.schedule.enums.CuckooBooleanFlag;
import org.ecs.schedule.enums.CuckooJobExecStatus;
import org.ecs.schedule.exception.BaseException;
import org.ecs.schedule.exception.JobUndailyLogBreakException;
import org.ecs.schedule.qry.QryBase;
import org.ecs.schedule.qry.job.JobLogQry;
import org.ecs.schedule.service.auth.CuckooAuthService;
import org.ecs.schedule.service.job.CuckooJobDependencyService;
import org.ecs.schedule.service.job.CuckooJobLogService;
import org.ecs.schedule.service.job.CuckooJobNextService;
import org.ecs.schedule.service.job.CuckooJobService;
import org.ecs.util.DateUtil;
import org.ecs.util.dao.PageDataList;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Slf4j
@Service
public class CuckooJobLogServiceImpl implements CuckooJobLogService {

    @Autowired
    private CuckooJobExecLogMapper cuckooJobExecLogMapper;

    @Autowired
    private CuckooJobExecLogSubMapper cuckooJobExecLogSubMapper;

    @Autowired
    private NotificationEmailSupport notificationEmailSupport;

    @Autowired
    private CuckooJobExtendMapper cuckooJobExtendMapper;

    @Autowired
    private QuartzManager quartzManager;

    @Autowired
    private CuckooJobDependencyService cuckooJobDependencyService;

    @Autowired
    private CuckooJobNextService cuckooJobNextService;

    @Autowired
    private CuckooJobService cuckooJobService;

    @Autowired
    private CuckooAuthService cuckooAuthService;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Long insertSelective(CuckooJobExecLog log) {
        cuckooJobExecLogMapper.insertSelective(log);
        return cuckooJobExecLogMapper.lastInsertId();
    }

    @Override
    public CuckooJobExecLog getJobLogByLogId(Long jobLogId) {
        return cuckooJobExecLogMapper.selectByPrimaryKey(jobLogId);
    }

    @Override
    public void updateJobLogStatusById(Long id, CuckooJobExecStatus jobStatus, String message) {
        CuckooJobExecLog log = new CuckooJobExecLog();
        log.setId(id);
        log.setExecJobStatus(jobStatus.getValue());
        log.setJobEndTime(System.currentTimeMillis());
        log.setRemark(message == null ? "" : message.length() < 2000 ? message : message.substring(0, 1999));
        cuckooJobExecLogMapper.updateByPrimaryKeySelective(log);

        if (CuckooJobExecStatus.FAILED.getValue().equals(jobStatus.getValue()) || CuckooJobExecStatus.BREAK.getValue().equals(jobStatus.getValue())) {
            // 失败或者终端，需要发送告警邮件
            try {
                sendJobErrorMessage(id, jobStatus);
            } catch (Exception e) {
                CuckooJobLogServiceImpl.log.error("waring");
            }
        }
    }

    private void sendJobErrorMessage(Long logId, CuckooJobExecStatus jobStatus) {
        CuckooJobExecLog cuckooJobExecLog = cuckooJobExecLogMapper.selectByPrimaryKey(logId);
        CuckooJobExtend cuckooJobExtend = cuckooJobExtendMapper.selectByPrimaryKey(cuckooJobExecLog.getJobId());

        if (null == cuckooJobExtend || StringUtils.isEmpty(cuckooJobExtend.getEmailList())) return;


        String mailList = cuckooJobExtend.getEmailList();
        String[] mailArr = mailList.split(",");
        for (String to : mailArr) {
            try {
                notificationEmailSupport.sendEmail(to, "任务调度平台执行" + jobStatus.getDescription(), "任务【id:" + cuckooJobExecLog.getId() + ",groupId:" + cuckooJobExecLog.getGroupId() + ",jobName:" + cuckooJobExecLog.getJobName() + "】执行失败,logDetail:" + cuckooJobExecLog.toString());
            } catch (Exception e) {
                log.error("mail send errorjob exception,to:{}, joblog:{}", to, cuckooJobExecLog, e);
            }
        }
    }

    @Override
    public void updateJobLogByPk(CuckooJobExecLog cuckooJobExecLogs) {
        cuckooJobExecLogMapper.updateByPrimaryKeySelective(cuckooJobExecLogs);
    }


    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public CuckooJobExecLog initSysCronJobLog(CuckooJobDetail cuckooJobDetail, Date scheduledFireTime) throws JobUndailyLogBreakException {
        CuckooJobExecLog cuckooJobExecLog = new CuckooJobExecLog();
        Long runTime = System.currentTimeMillis();

        Long curTime = scheduledFireTime.getTime();
        // 写入初始化任务执行信息
        BeanUtils.copyProperties(cuckooJobDetail, cuckooJobExecLog);
        cuckooJobExecLog.setId(null);
        cuckooJobExecLog.setJobId(cuckooJobDetail.getId());
        cuckooJobExecLog.setJobStartTime(runTime);
        cuckooJobExecLog.setExecJobStatus(CuckooJobExecStatus.PENDING.getValue());
        cuckooJobExecLog.setLatestCheckTime(curTime);
        cuckooJobExecLog.setNeedTriggleNext(true);
        cuckooJobExecLog.setForceTriggle(false);

        if (CuckooBooleanFlag.YES.getValue().equals(cuckooJobDetail.getTypeDaily())) {
            // 如果是日切任务，那么计算出TxDate
            cuckooJobExecLog.setTxDate(DateUtil.addIntDate(DateUtil.getIntDay(scheduledFireTime), cuckooJobDetail.getOffset()));
        } else {
            // 获得上一次任务执行时间最大的截止时间为当前任务的开始时间
            CuckooJobExecLogCriteria crt = new CuckooJobExecLogCriteria();
            crt.createCriteria().andJobIdEqualTo(cuckooJobExecLog.getJobId());
            crt.setOrderByClause(" flow_cur_time desc ");
            crt.setStart(0);
            crt.setLimit(1);
            List<CuckooJobExecLog> latestJobLog = cuckooJobExecLogMapper.selectByExample(crt);
            if (CollectionUtils.isEmpty(latestJobLog)) {

                cuckooJobExecLog.setFlowLastTime(0L);
            } else {
                if (curTime.equals(latestJobLog.get(0).getFlowCurTime())) {
                    throw new JobUndailyLogBreakException("undaily job timeline same error, cuckooJobId:{},latestJobLog:{},curTime:{}", cuckooJobDetail.getId(), latestJobLog.get(0).getFlowCurTime(), curTime);
                }
                cuckooJobExecLog.setFlowLastTime(latestJobLog.get(0).getFlowCurTime());
            }

            cuckooJobExecLog.setFlowCurTime(curTime);
        }

        cuckooJobExecLogMapper.insertSelective(cuckooJobExecLog);
        cuckooJobExecLog.setId(cuckooJobExecLogMapper.lastInsertId());

        return cuckooJobExecLog;
    }

    @Override
    public CuckooJobExecLog initUnDailyJobLog(CuckooJobDetail cuckooJobDetail, Boolean needTriggleNext, Long flowLastTime, Long flowCurTime, boolean foreTriggle) {
        CuckooJobExecLog cuckooJobExecLog = new CuckooJobExecLog();
        Long curTime = System.currentTimeMillis();

        // 写入初始化任务执行信息
        BeanUtils.copyProperties(cuckooJobDetail, cuckooJobExecLog);
        cuckooJobExecLog.setId(null);
        cuckooJobExecLog.setJobId(cuckooJobDetail.getId());
        cuckooJobExecLog.setJobStartTime(curTime);
        cuckooJobExecLog.setExecJobStatus(CuckooJobExecStatus.PENDING.getValue());
        cuckooJobExecLog.setLatestCheckTime(curTime);
        cuckooJobExecLog.setNeedTriggleNext(needTriggleNext);
        cuckooJobExecLog.setForceTriggle(foreTriggle);

        cuckooJobExecLog.setFlowLastTime(flowLastTime);
        cuckooJobExecLog.setFlowCurTime(flowCurTime);

        cuckooJobExecLogMapper.insertSelective(cuckooJobExecLog);
        cuckooJobExecLog.setId(cuckooJobExecLogMapper.lastInsertId());

        return cuckooJobExecLog;
    }

    @Override
    public CuckooJobExecLog initJobLog(CuckooJobDetail cuckooJobDetail, Boolean needTriggleNext, boolean foreTriggle) {
        CuckooJobExecLog cuckooJobExecLog = new CuckooJobExecLog();
        Long curTime = System.currentTimeMillis();

        // 写入初始化任务执行信息
        BeanUtils.copyProperties(cuckooJobDetail, cuckooJobExecLog);
        cuckooJobExecLog.setId(null);
        cuckooJobExecLog.setJobId(cuckooJobDetail.getId());
        cuckooJobExecLog.setJobStartTime(curTime);
        cuckooJobExecLog.setExecJobStatus(CuckooJobExecStatus.PENDING.getValue());
        cuckooJobExecLog.setLatestCheckTime(curTime);
        cuckooJobExecLog.setNeedTriggleNext(needTriggleNext);
        cuckooJobExecLog.setForceTriggle(foreTriggle);

        cuckooJobExecLog.setFlowLastTime(null);
        cuckooJobExecLog.setFlowCurTime(null);

        cuckooJobExecLogMapper.insertSelective(cuckooJobExecLog);
        cuckooJobExecLog.setId(cuckooJobExecLogMapper.lastInsertId());

        return cuckooJobExecLog;
    }

    @Override
    public CuckooJobExecLog initDailyJobLog(CuckooJobDetail cuckooJobDetail, Boolean needTriggleNext, Integer txDate, boolean foreTriggle) {
        CuckooJobExecLog cuckooJobExecLog = new CuckooJobExecLog();
        Long curTime = System.currentTimeMillis();

        // 写入初始化任务执行信息
        BeanUtils.copyProperties(cuckooJobDetail, cuckooJobExecLog);

        cuckooJobExecLog.setId(null);
        cuckooJobExecLog.setJobId(cuckooJobDetail.getId());
        cuckooJobExecLog.setJobStartTime(curTime);
        cuckooJobExecLog.setExecJobStatus(CuckooJobExecStatus.PENDING.getValue());
        cuckooJobExecLog.setLatestCheckTime(curTime);
        cuckooJobExecLog.setNeedTriggleNext(needTriggleNext);
        cuckooJobExecLog.setForceTriggle(foreTriggle);

        cuckooJobExecLog.setTxDate(txDate);
        cuckooJobExecLogMapper.insertSelective(cuckooJobExecLog);
        cuckooJobExecLog.setId(cuckooJobExecLogMapper.lastInsertId());

        return cuckooJobExecLog;
    }

    @Override
    public PageDataList<CuckooJobExecLog> pageByQry(JobLogQry qry) {
        CuckooJobExecLogCriteria crtPi = new CuckooJobExecLogCriteria();
        crtPi.setStart(qry.getStart());
        crtPi.setLimit(qry.getLimit());
        crtPi.setOrderByClause(" id desc ");

        CuckooJobExecLogCriteria.Criteria crt = crtPi.createCriteria();

        if (null != qry.getLogId()) {
            crt.andIdEqualTo(qry.getLogId());
        }

        if (null != qry.getGroupId()) {
            crt.andGroupIdEqualTo(qry.getGroupId());
        } else {
            if (CollectionUtils.isNotEmpty(cuckooAuthService.getLogonInfo().getReadableGroupIds())) {
                crt.andGroupIdIn(cuckooAuthService.getLogonInfo().getReadableGroupIds());
            }
        }
        if (null != qry.getJobId()) {
            crt.andJobIdEqualTo(qry.getJobId());
        }

        if (null != qry.getStartDateTime()) {
            crt.andJobStartTimeGreaterThanOrEqualTo(qry.getStartDateTime());
        }
        if (null != qry.getEndDateTime()) {
            crt.andJobStartTimeLessThanOrEqualTo(qry.getEndDateTime());
        }

        if (null != qry.getJobStatus()) {
            crt.andExecJobStatusIn(qry.getJobStatus());
        }

        return cuckooJobExecLogMapper.pageByExample(crtPi);
    }

    @Override
    public void resetLogStatus(Long logId, CuckooJobExecStatus status) {
        CuckooJobExecLog cuckooJobExecLog = new CuckooJobExecLog();
        cuckooJobExecLog.setId(logId);
        cuckooJobExecLog.setExecJobStatus(status.getValue());

        CuckooJobExecLog jobLog = cuckooJobExecLogMapper.selectByPrimaryKey(logId);
        if (!cuckooAuthService.getLogonInfo().getWritableGroupIds().contains(jobLog.getGroupId())) {
            throw new BaseException("no writable right");
        }

        cuckooJobExecLogMapper.updateByPrimaryKeySelective(cuckooJobExecLog);
        if (CuckooJobExecStatus.SUCCED.getValue().equals(status.getValue())) {
            // 任务重置为成功，需要删除cron中的数据
            if (CuckooBooleanFlag.NO.getValue().equals(jobLog.getTypeDaily())) {
                // 非日切任务，要把cron中的simpleTrigger删除
                quartzManager.deleteSimpleJob(jobLog);
            }
        }
    }

    @Override
    public PageDataList<CuckooJobExecLog> pageOverTimeJobs(QryBase qry) {
        PageDataList<CuckooJobExecLog> page = new PageDataList<>();
        page.setPage(qry.getStart() / qry.getLimit() + 1);
        page.setPageSize(qry.getLimit());

        if (CollectionUtils.isNotEmpty(cuckooAuthService.getLogonInfo().getReadableGroupIds())) {
            qry.setGroupIds(cuckooAuthService.getLogonInfo().getReadableGroupIds());
        }
        page.setRows(cuckooJobExecLogSubMapper.pageOverTimeJobs(qry));
        page.setTotal(cuckooJobExecLogSubMapper.countOverTimeJobs(qry));

        return page;
    }

    @Override
    public boolean checkPreLogIsDone(CuckooJobExecLog jobLog) {
        if ("1".equals("1")) {
            // 此处取消任务连续性的校验（上个任务成功、上个任务和本次任务的时间连续）
            return true;
        }

        if (CuckooBooleanFlag.YES.getValue().equals(jobLog.getTypeDaily())) {
            // 第一次执行的情况，返回true
            CuckooJobExecLogCriteria firstCrt = new CuckooJobExecLogCriteria();
            firstCrt.setOrderByClause(" tx_date desc ,id desc ");
            firstCrt.createCriteria().andGroupIdEqualTo(jobLog.getGroupId())
                    .andJobIdEqualTo(jobLog.getJobId())
                    // 考虑到修改任务的时候，如果修改触发方式会造成后续判断条件不一致，如果发生这种情况返回失败
                    .andTriggerTypeEqualTo(jobLog.getTriggerType())
                    // 首次任务重新执行的情况,没有时间更早的任务
                    .andTxDateLessThan(jobLog.getTxDate());
            List<CuckooJobExecLog> firstResult = cuckooJobExecLogMapper.selectByExample(firstCrt);
            if (CollectionUtils.isEmpty(firstResult)) {
                return true;
            } else {
                // 如果不为空,最近一条的执行是必须要连续的,(最近一条是0的话，也可以通过)
                if (!DateUtil.addIntDate(jobLog.getTxDate(), -1).equals(firstResult.get(0).getTxDate()) && !Integer.valueOf(0).equals(firstResult.get(0).getTxDate())) {
                    log.info("job log exec is not continuous, curlogId:{},txdate:{},prelogId:{},txdate:{}"
                            , jobLog.getId(), jobLog.getTxDate(), firstResult.get(0).getId(), firstResult.get(0).getTxDate());
                    jobLog.setRemark("job log exec is not continuous, curlogId:" + jobLog.getId() + ",txdate:" + jobLog.getTxDate() + ",prelogId:" + firstResult.get(0).getId() + ",txdate:" + firstResult.get(0).getTxDate());
                    return false;
                } else {
                    if (Integer.valueOf(0).equals(firstResult.get(0).getTxDate())) {
                        // 一开始UnDaily任务，后来修改为Daily的情况会出现
                        return true;
                    }
                }
            }

            // 有上级任务， 日切任务根据txdate-1获得上一个任务最终(ID倒序)执行状态
            CuckooJobExecLogCriteria cronCrt = new CuckooJobExecLogCriteria();
            cronCrt.setOrderByClause(" id desc ");
            cronCrt.createCriteria().andGroupIdEqualTo(jobLog.getGroupId())
                    .andJobIdEqualTo(jobLog.getJobId())
                    .andTxDateEqualTo(DateUtil.addIntDate(jobLog.getTxDate(), -1));
            List<CuckooJobExecLog> cronResult = cuckooJobExecLogMapper.selectByExample(cronCrt);
            if (CollectionUtils.isNotEmpty(cronResult)) {
                if (CuckooJobExecStatus.SUCCED.getValue().equals(cronResult.get(0).getExecJobStatus())) {
                    return true;
                } else {
                    log.info("prelog is not succeeded, curlogId:{}, prelogId:{},status:{}", jobLog.getId(), cronResult.get(0).getId(), cronResult.get(0).getExecJobStatus());
                    jobLog.setRemark("prelog is not succeeded, curlogId:" + jobLog.getId() + ", prelogId:" + cronResult.get(0).getId() + ",status:" + cronResult.get(0).getExecJobStatus());
                    return false;
                }
            }

        } else if (CuckooBooleanFlag.NO.getValue().equals(jobLog.getTypeDaily())) {
            // 第一次执行的情况，返回true
            CuckooJobExecLogCriteria firstCrt = new CuckooJobExecLogCriteria();
            firstCrt.setOrderByClause(" flow_cur_time desc ,id desc ");
            firstCrt.createCriteria().andGroupIdEqualTo(jobLog.getGroupId())
                    .andJobIdEqualTo(jobLog.getJobId())
                    // 考虑到修改任务的时候，如果修改触发方式会造成后续判断条件不一致，如果发生这种情况返回失败
                    .andTriggerTypeEqualTo(jobLog.getTriggerType())
                    // 首次任务重新执行的情况,没有时间更早的任务
                    .andFlowCurTimeLessThan(jobLog.getFlowCurTime());
            List<CuckooJobExecLog> firstResult = cuckooJobExecLogMapper.selectByExample(firstCrt);
            if (CollectionUtils.isEmpty(firstResult)) {
                return true;
            } else {
                // 如果不为空,最近一条的执行是必须要连续的
                if (!jobLog.getFlowLastTime().equals(firstResult.get(0).getFlowCurTime())) {
                    log.error("job log exec is not continuous, curlogId:{},lastTime:{},prelogId:{},curTime:{}"
                            , jobLog.getId(), jobLog.getFlowLastTime(), firstResult.get(0).getId(), firstResult.get(0).getFlowCurTime());
                    jobLog.setRemark("job log exec is not continuous, curlogId:" + jobLog.getId() + ",txdate:" + jobLog.getTxDate() + ",prelogId:" + firstResult.get(0).getId() + ",curTime:" + firstResult.get(0).getTxDate());
                    return false;
                }

            }

            // 非日切任务，根据lastTime获得上一个任务最终(ID倒序)执行状态
            CuckooJobExecLogCriteria jobCrt = new CuckooJobExecLogCriteria();
            jobCrt.setOrderByClause(" id desc ");
            jobCrt.createCriteria().andGroupIdEqualTo(jobLog.getGroupId())
                    .andJobIdEqualTo(jobLog.getJobId())
                    .andFlowCurTimeEqualTo(jobLog.getFlowLastTime());
            List<CuckooJobExecLog> jobResult = cuckooJobExecLogMapper.selectByExample(jobCrt);
            if (CollectionUtils.isNotEmpty(jobResult)) {
                if (CuckooJobExecStatus.SUCCED.getValue().equals(jobResult.get(0).getExecJobStatus())) {
                    return true;
                } else {
                    log.debug("prelog is not succeeded, curlogId:{}, prelogId:{},status:{}", jobLog.getId(), jobResult.get(0).getId(), jobResult.get(0).getExecJobStatus());
                    jobLog.setRemark("prelog is not succeeded, curlogId:" + jobLog.getId() + ", prelogId:" + jobResult.get(0).getId() + ",status:" + jobResult.get(0).getExecJobStatus());
                    return false;
                }
            }
        } else {
            // 无业务日期参数，什么都不校验
            return true;
        }

        log.info("unknown pending result,  curlogId:{}", jobLog.getId());
        jobLog.setRemark("unknown pending result,  curlogId:" + jobLog.getId());
        return false;
    }


    @Override
    public PageDataList<CuckooJobExecLog> pagePendingList(QryBase qry) {
        PageDataList<CuckooJobExecLog> page = new PageDataList<>();
        page.setPage(qry.getStart() / qry.getLimit() + 1);
        page.setPageSize(qry.getLimit());

        if (CollectionUtils.isNotEmpty(cuckooAuthService.getLogonInfo().getReadableGroupIds())) {
            qry.setGroupIds(cuckooAuthService.getLogonInfo().getReadableGroupIds());
        }

        page.setRows(cuckooJobExecLogSubMapper.pagePendingJobs(qry));
        page.setTotal(cuckooJobExecLogSubMapper.countPendingJobs(qry));

        return page;
    }

    @Override
    public CuckooJobExecLog getPreJobLogs(CuckooJobExecLog cuckooJobExecLog) {
        Long preJobId = cuckooJobNextService.findJobIdByNextJobId(cuckooJobExecLog.getJobId());
        if (null == preJobId) return null;

        CuckooJobExecLogCriteria crt = new CuckooJobExecLogCriteria();
        crt.setOrderByClause("id desc");
        crt.setStart(0);
        crt.setLimit(1);

        if (CuckooBooleanFlag.YES.getValue().equals(cuckooJobExecLog.getTypeDaily())) {
            // 日期任务根据txdate判断
            crt.createCriteria().andJobIdEqualTo(preJobId)
                    .andTxDateEqualTo(cuckooJobExecLog.getTxDate());
        } else if (CuckooBooleanFlag.NO.getValue().equals(cuckooJobExecLog.getTypeDaily())) {
            // 非日切任务根据flow_last_time来判断
            crt.createCriteria().andJobIdEqualTo(preJobId)
                    .andFlowLastTimeEqualTo(cuckooJobExecLog.getFlowLastTime());
        }
        List<CuckooJobExecLog> rtn = cuckooJobExecLogMapper.selectByExample(crt);
        return CollectionUtils.isEmpty(rtn) ? null : rtn.get(0);
    }


    @Override
    public List<CuckooJobExecLog> getNextJobs(CuckooJobExecLog cuckooJobExecLog) {
        List<Long> nextJobIds = cuckooJobNextService.findNextJobIdByJobId(cuckooJobExecLog.getJobId());
        List<CuckooJobExecLog> results = queryAndAssembleBy(cuckooJobExecLog, nextJobIds);
        return results;
    }


    private CuckooJobExecLog emptyDummyLog(Long nextJobId) {
        CuckooJobDetail jobInfo = cuckooJobService.getJobById(nextJobId);
        CuckooJobExecLog log = new CuckooJobExecLog();
        BeanUtils.copyProperties(jobInfo, log);
        log.setJobId(nextJobId);
        return log;
    }


    private List<CuckooJobExecLog> queryAndAssembleBy(CuckooJobExecLog cuckooJobExecLog, List<Long> dependencyIds) {
        if (CollectionUtils.isEmpty(dependencyIds)) return Collections.EMPTY_LIST;
        List<CuckooJobExecLog> results = new ArrayList<>();
        for (Long dependencyId : dependencyIds) {
            CuckooJobExecLogCriteria crt = new CuckooJobExecLogCriteria();
            crt.setOrderByClause("id desc");
            crt.setStart(0);
            crt.setLimit(1);

            if (CuckooBooleanFlag.YES.getValue().equals(cuckooJobExecLog.getTypeDaily())) {
                // 日期任务根据txdate判断
                crt.createCriteria().andJobIdEqualTo(dependencyId)
                        .andTxDateEqualTo(cuckooJobExecLog.getTxDate());

            } else if (CuckooBooleanFlag.NO.getValue().equals(cuckooJobExecLog.getTypeDaily())) {
                // 非日切任务根据flow_last_time来判断
                crt.createCriteria().andJobIdEqualTo(dependencyId)
                        .andFlowLastTimeEqualTo(cuckooJobExecLog.getFlowLastTime());
            }

            List<CuckooJobExecLog> rtn = cuckooJobExecLogMapper.selectByExample(crt);
            if (CollectionUtils.isNotEmpty(rtn)) {
                results.add(rtn.get(0));
            } else {
                results.add(emptyDummyLog(dependencyId));
            }
        }
        return results;
    }

    @Override
    public List<CuckooJobExecLog> getDependencyJobs(CuckooJobExecLog cuckooJobExecLog) {
        List<Long> dependencyIds = cuckooJobDependencyService.listDependencyIdsByJobId(cuckooJobExecLog.getJobId());
        return queryAndAssembleBy(cuckooJobExecLog, dependencyIds);
    }

}
