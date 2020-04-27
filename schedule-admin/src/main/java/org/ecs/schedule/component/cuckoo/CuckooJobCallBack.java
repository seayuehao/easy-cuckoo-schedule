package org.ecs.schedule.component.cuckoo;

import org.ecs.schedule.bean.JobInfoBean;
import org.ecs.schedule.domain.exec.CuckooJobExecLog;
import org.ecs.schedule.enums.CuckooJobExecStatus;
import org.ecs.schedule.service.job.CuckooJobLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 客户端任务执行完成，调用方法
 */
@Slf4j
@Component
public class CuckooJobCallBack {

    @Autowired
    private CuckooJobLogService cuckooJobLogService;

    @Autowired
    private CuckooJobExecutor cuckooJobExecutor;

    /**
     * 客户端任务执行成功回调
     *
     * @param jobInfo
     */
    @Transactional
    public void execJobSuccedCallBack(JobInfoBean jobInfo) {
        log.info("#@#@# execJobSuccedCallBack runs");
        CuckooJobExecLog jobLog = cuckooJobLogService.getJobLogByLogId(jobInfo.getJobLogId());

        // 更新日志
        cuckooJobLogService.updateJobLogStatusById(jobLog.getId(), CuckooJobExecStatus.SUCCED, jobInfo.getErrMessage());

        // 触发下级任务
        if (jobInfo.getNeedTrigglerNext()) {
            cuckooJobExecutor.executeNextJob(jobInfo);
        }
    }

    /**
     * 客户端任务执行失败回调
     *
     * @param jobInfo
     */
    @Transactional
    public void execJobFailedCallBack(JobInfoBean jobInfo) {
        log.info("#@#@# execJobFailedCallBack runs");
        CuckooJobExecLog jobLog = cuckooJobLogService.getJobLogByLogId(jobInfo.getJobLogId());

        // 更新日志
        cuckooJobLogService.updateJobLogStatusById(jobLog.getId(), CuckooJobExecStatus.FAILED, jobInfo.getErrMessage());
    }

}
