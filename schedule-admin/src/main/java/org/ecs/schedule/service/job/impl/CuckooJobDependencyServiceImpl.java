package org.ecs.schedule.service.job.impl;

import org.ecs.schedule.dao.exec.CuckooJobDependencyMapper;
import org.ecs.schedule.dao.exec.CuckooJobDetailMapper;
import org.ecs.schedule.dao.exec.CuckooJobExecLogMapper;
import org.ecs.schedule.domain.exec.CuckooJobDependency;
import org.ecs.schedule.domain.exec.CuckooJobDependencyCriteria;
import org.ecs.schedule.domain.exec.CuckooJobDetail;
import org.ecs.schedule.domain.exec.CuckooJobExecLog;
import org.ecs.schedule.domain.exec.CuckooJobExecLogCriteria;
import org.ecs.schedule.enums.CuckooBooleanFlag;
import org.ecs.schedule.enums.CuckooJobExecStatus;
import org.ecs.schedule.exception.BaseException;
import org.ecs.schedule.service.job.CuckooJobDependencyService;
import org.ecs.schedule.util.CuckBeanUtil;
import org.ecs.schedule.vo.job.JobDependency;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CuckooJobDependencyServiceImpl implements CuckooJobDependencyService {

    @Autowired
    private CuckooJobDependencyMapper cuckooJobDependencyMapper;

    @Autowired
    private CuckooJobDetailMapper cuckooJobDetailMapper;

    @Autowired
    private CuckooJobExecLogMapper cuckooJobExecLogMapper;

    @Override
    @Transactional
    public void setDependencyJobConfig(List<JobDependency> dependencyJobs) {
        if (CollectionUtils.isEmpty(dependencyJobs)) {
            throw new BaseException("dependency jobs should not be empty : ");
        }
        // 先删除触发
        CuckooJobDependencyCriteria curJobCrt = new CuckooJobDependencyCriteria();
        curJobCrt.createCriteria().andIdEqualTo(dependencyJobs.get(0).getJobId());
        cuckooJobDependencyMapper.deleteByExample(curJobCrt);

        // 再增加触发
        for (JobDependency jobDependency : dependencyJobs) {
            CuckooJobDependency cuckooJobDependency = CuckBeanUtil.parseJobDependency(jobDependency);
            cuckooJobDependencyMapper.insertSelective(cuckooJobDependency);
        }
    }

    @Override
    public boolean checkDependencyJobFinished(CuckooJobExecLog jobLog) {
        CuckooJobDependencyCriteria depJobMapCrt = new CuckooJobDependencyCriteria();
        depJobMapCrt.createCriteria().andJobIdEqualTo(jobLog.getJobId());
        List<CuckooJobDependency> depJobMaps = cuckooJobDependencyMapper.selectByExample(depJobMapCrt);
        List<Long> depJobIds = depJobMaps.stream().map(d -> d.getDependencyJobId()).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(depJobIds)) {
            return true;
        }

        // 依赖执行任务完成条件： 1.依赖的任务状态都为成功；2.日切任务的txdate需要一致、非日切任务的latestTime一致
        List<CuckooJobExecLog> readyDepJobs = null;
        if (CuckooBooleanFlag.NO.getValue().equals(jobLog.getTypeDaily())) {
            CuckooJobExecLogCriteria depJobCrt = new CuckooJobExecLogCriteria();
            depJobCrt.createCriteria().andJobIdIn(depJobIds)
                    // 1.依赖的任务状态都为成功
                    .andExecJobStatusEqualTo(CuckooJobExecStatus.SUCCED.getValue())
                    // 非日切任务的latestTime一致
                    .andFlowLastTimeEqualTo(jobLog.getFlowLastTime());
            readyDepJobs = cuckooJobExecLogMapper.selectByExample(depJobCrt);

        } else if (CuckooBooleanFlag.YES.getValue().equals(jobLog.getTypeDaily())) {

            // 2.日切任务的txdate需要一致
            CuckooJobExecLogCriteria depLogTxdateCrt = new CuckooJobExecLogCriteria();
            depLogTxdateCrt.createCriteria().andJobIdIn(depJobIds)
                    // 1.依赖的任务状态都为成功
                    .andExecJobStatusEqualTo(CuckooJobExecStatus.SUCCED.getValue())
                    // 日切任务的txdate需要一致
                    .andTxDateEqualTo(jobLog.getTxDate());
            readyDepJobs = cuckooJobExecLogMapper.selectByExample(depLogTxdateCrt);

        } else {
            // 无业务日期参数，什么都不校验
            return true;
        }

        Set<Long> readydepJobs = readyDepJobs.stream().map(l -> l.getJobId()).collect(Collectors.toSet());

        if (readydepJobs.size() != depJobIds.size()) {
            log.debug("dependency was not ready,jobLog:{},dependyJobs:{},readydepJobs:{}", jobLog, depJobIds, readydepJobs);
            jobLog.setRemark("dependency was not ready,dependyJobs:" + depJobIds + ",readydepJobs:" + readydepJobs);
            return false;
        }

        return true;
    }

    @Override
    public List<Long> listDependencyIdsByJobId(Long jobId) {
        CuckooJobDependencyCriteria crt = new CuckooJobDependencyCriteria();
        crt.createCriteria().andJobIdEqualTo(jobId);
        List<CuckooJobDependency> result = cuckooJobDependencyMapper.selectByExample(crt);
        if (CollectionUtils.isEmpty(result)) return Collections.EMPTY_LIST;

        return result.stream().map(d -> d.getDependencyJobId()).collect(Collectors.toList());
    }

    @Override
    public void addOrUpdateJobDependency(Long jobId, String[] dependencyIds) {
        // 1.先删除

        CuckooJobDependencyCriteria crtDel = new CuckooJobDependencyCriteria();
        crtDel.createCriteria().andJobIdEqualTo(jobId);
        cuckooJobDependencyMapper.deleteByExample(crtDel);

        // 2.再增加
        for (String dependencyId : dependencyIds) {
            CuckooJobDependency jobDependency = new CuckooJobDependency();
            jobDependency.setJobId(jobId);
            CuckooJobDetail cuckooJobDetail = cuckooJobDetailMapper.selectByPrimaryKey(Long.valueOf(dependencyId));
            if (null == cuckooJobDetail) {
                throw new BaseException("can not find dependency job ,jobId:{}", dependencyId);
            }
            jobDependency.setDependencyJobId(Long.valueOf(dependencyId));
            cuckooJobDependencyMapper.insertSelective(jobDependency);
        }
    }

}
