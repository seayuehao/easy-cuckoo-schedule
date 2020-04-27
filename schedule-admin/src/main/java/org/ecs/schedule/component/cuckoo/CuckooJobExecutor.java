package org.ecs.schedule.component.cuckoo;

import org.ecs.schedule.bean.JobInfoBean;
import org.ecs.schedule.dao.exec.CuckooJobDetailMapper;
import org.ecs.schedule.dao.exec.CuckooJobExecLogMapper;
import org.ecs.schedule.domain.exec.CuckooJobDetail;
import org.ecs.schedule.domain.exec.CuckooJobExecLog;
import org.ecs.schedule.domain.net.CuckooNetClientInfo;
import org.ecs.schedule.enums.CuckooBooleanFlag;
import org.ecs.schedule.enums.CuckooJobExecStatus;
import org.ecs.schedule.enums.CuckooJobExecType;
import org.ecs.schedule.enums.CuckooJobStatus;
import org.ecs.schedule.exception.JobCanNotRunningException;
import org.ecs.schedule.exception.JobRunningErrorException;
import org.ecs.schedule.service.job.CuckooJobDependencyService;
import org.ecs.schedule.service.job.CuckooJobLogService;
import org.ecs.schedule.service.job.CuckooJobService;
import org.ecs.schedule.service.net.CuckooNetService;
import org.ecs.schedule.vo.job.CuckooClientJobExecResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
public class CuckooJobExecutor {

    @Autowired
    private CuckooJobDetailMapper cuckooJobDetailMapper;

    @Autowired
    private CuckooJobExecLogMapper cuckooJobExecLogsMapper;

    @Autowired
    private CuckooNetService cuckooServerService;

    @Autowired
    private CuckooJobDependencyService cuckooJobDependencyService;

    @Autowired
    private CuckooJobLogService cuckooJobLogService;

    @Autowired
    private CuckooJobService cuckooJobService;


    /**
     * quartz任务执行器
     *
     * @param jobLog
     * @return 任务是否执行完成，如果是依赖关系没有结束（持续pending），返回false，否则不论成功失败，都返回true
     * @throws JobCanNotRunningException
     */
    @Transactional
    public boolean executeQuartzJob(CuckooJobExecLog jobLog) {
        if (!CuckooJobExecStatus.PENDING.getValue().equals(jobLog.getExecJobStatus())) {
            // 如果任务状态是非PENDING状态的，表示被用户修改过状态，此处记录ERROR日志。并且将当期Quartz丢弃（返回成功即可）
            log.warn("invalid job exec status:{},jobLogInfo:{}", jobLog.getExecJobStatus(), jobLog);
            return true;
        }

        if (checkJobCanRunning(jobLog)) {
            try {
                executeJob(jobLog);
                return true;
            } catch (JobCanNotRunningException e) {
                return false;
            }
        }
        return false;
    }


    // 执行任务
    private void executeJob(CuckooJobExecLog jobLog) throws JobCanNotRunningException {
        log.info("job start execjob,jobLog:{}", jobLog);
        cuckooJobExecLogsMapper.lockByPrimaryKey(jobLog.getId());

        String remark = "";
        // 初始化执行日志

        try {
            // 查询远程执行器-- 考虑负载均衡 ,如果可执行客户端没有的话，放到数据库队列里面去。用于客户端重连等操作完成后操作
            CuckooNetClientInfo cuckooNetClientInfo = cuckooServerService.getExecNetClientInfo(jobLog.getJobId());
            if (null == cuckooNetClientInfo) {
                throw new JobCanNotRunningException("no executor fund, add job into todo queue,jobLog:{}", jobLog);
            }
            jobLog.setCuckooClientIp(cuckooNetClientInfo.getIp());
            jobLog.setCuckooClientPort(cuckooNetClientInfo.getPort());
            jobLog.setJobExecTime(System.currentTimeMillis());

            // 调用日志执行单元(远程调用)
            JobInfoBean jobBean = new JobInfoBean();
            jobBean.setFlowCurrTime(jobLog.getFlowCurTime());
            jobBean.setFlowLastTime(jobLog.getFlowLastTime());
            jobBean.setJobId(jobLog.getJobId());
            jobBean.setExecType(CuckooJobExecType.fromName(jobLog.getExecJobType()));
            jobBean.setTypeDaily(CuckooBooleanFlag.YES.getValue().equals(jobLog.getTypeDaily()));
            jobBean.setJobName(jobLog.getJobName());
            jobBean.setTxDate(jobLog.getTxDate());
            jobBean.setJobLogId(jobLog.getId());
            jobBean.setCuckooParallelJobArgs(jobLog.getCuckooParallelJobArgs());
            jobBean.setNeedTrigglerNext(jobLog.getNeedTriggleNext());

            CuckooClientJobExecResult remoteExecutor = cuckooServerService.execRemoteJob(cuckooNetClientInfo, jobBean);

            remark = remoteExecutor.getRemark();
            log.info("job start running,remark:{},logInfo:{}", remark, jobLog);
            // 插入执行日志
            jobLog.setRemark(remark.length() > 490 ? remark.substring(0, 490) : remark);
            jobLog.setExecJobStatus(CuckooJobExecStatus.RUNNING.getValue());
            cuckooJobExecLogsMapper.updateByPrimaryKeySelective(jobLog);

        } catch (JobRunningErrorException e) {
            // 未知异常，报错处理
            remark = e.getMessage();
            log.error("running err job exec,err:{},jobInfo:{}", e.getMessage(), jobLog);
            // 插入执行日志
            jobLog.setRemark(remark.length() > 490 ? remark.substring(0, 490) : remark);
            jobLog.setExecJobStatus(CuckooJobExecStatus.FAILED.getValue());
            cuckooJobExecLogsMapper.updateByPrimaryKeySelective(jobLog);
        } catch (JobCanNotRunningException e) {
            remark = e.getMessage();
            log.error("cannot running job exec,err:{},jobInfo:{}", e.getMessage(), jobLog);
            // 插入执行日志
            jobLog.setRemark(remark.length() > 490 ? remark.substring(0, 490) : remark);
            jobLog.setExecJobStatus(CuckooJobExecStatus.PENDING.getValue());
            cuckooJobExecLogsMapper.updateByPrimaryKeySelective(jobLog);
            throw e;
        } catch (Exception e) {
            remark = e.getMessage();
            log.error("cannot running job exec,err:{},jobInfo:{}", e.getMessage(), jobLog, e);
            // 插入执行日志
            jobLog.setRemark(remark.length() > 490 ? remark.substring(0, 490) : remark);
            jobLog.setExecJobStatus(CuckooJobExecStatus.FAILED.getValue());
            cuckooJobExecLogsMapper.updateByPrimaryKeySelective(jobLog);
        }
    }


    /**
     * 下级任务触发，调用任务执行功能
     *
     * @param jobInfoBean
     */
    public void executeNextJob(JobInfoBean jobInfoBean) {
        CuckooJobExecLog jobLog = cuckooJobExecLogsMapper.selectByPrimaryKey(jobInfoBean.getJobLogId());

        // 根据jobInfoBean查询下一个任务
        List<CuckooJobDetail> jobInfoNexts = cuckooJobService.getNextJobById(jobLog.getJobId());

        if (CollectionUtils.isEmpty(jobInfoNexts)) return;

        for (CuckooJobDetail cuckooJobDetail : jobInfoNexts) {
            log.info("trigger next job:{}, father joblog:{}", cuckooJobDetail, jobLog);
            // 强制执行属性，不继承
            jobLog.setForceTriggle(false);
            //  判断任务类型，修改任务状态为PENDING，放入到PENDING任务队列中
            cuckooJobService.pendingJob(cuckooJobDetail, jobLog);
        }
    }


    private boolean checkJobCanRunning(CuckooJobExecLog jobLog) {
        // 查询任务信息
        CuckooJobDetail jobInfo = cuckooJobDetailMapper.selectByPrimaryKey(jobLog.getJobId());

        if (jobLog.getForceTriggle()) {
            // 强制执行的任务（手工调度），不需要校验
            return true;
        } else {
            // 非强制执行的任务（手工调度），状态为暂停，等待下次调度
            if (CuckooJobStatus.PAUSE.getValue().equals(jobInfo.getJobStatus())) {
                log.debug("job is paused, triggered next time, jobInfo:{}", jobInfo);
                jobLog.setRemark("job is paused, triggered next time");
                cuckooJobExecLogsMapper.updateByPrimaryKeySelective(jobLog);
                return false;
            }

            // 检查日志中，上一次执行任务(txdate/latest_time倒叙)未执行成功，那么当前任务不能执行
            if (!cuckooJobLogService.checkPreLogIsDone(jobLog)) {

                cuckooJobExecLogsMapper.updateByPrimaryKeySelective(jobLog);
                return false;
            }
            if (!cuckooJobDependencyService.checkDependencyJobFinished(jobLog)) {
                cuckooJobExecLogsMapper.updateByPrimaryKeySelective(jobLog);
                return false;
            }

            // 校验任务依赖状态
            return true;
        }
    }

}
