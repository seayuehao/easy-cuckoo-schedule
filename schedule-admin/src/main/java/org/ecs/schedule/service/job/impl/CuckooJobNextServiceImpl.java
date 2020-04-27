package org.ecs.schedule.service.job.impl;

import org.ecs.schedule.dao.exec.CuckooJobNextJobMapper;
import org.ecs.schedule.domain.exec.CuckooJobNextJob;
import org.ecs.schedule.domain.exec.CuckooJobNextJobCriteria;
import org.ecs.schedule.exception.BaseException;
import org.ecs.schedule.service.job.CuckooJobNextService;
import org.ecs.schedule.util.CuckBeanUtil;
import org.ecs.schedule.vo.job.JobNext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CuckooJobNextServiceImpl implements CuckooJobNextService {

    @Autowired
    private CuckooJobNextJobMapper cuckooJobNextJobMapper;

    @Override
    @Transactional
    public void setNextJobConfig(List<JobNext> nextJobs) {
        if (CollectionUtils.isEmpty(nextJobs)) {
            throw new BaseException("next jobs should not be empty : ");
        }
        // 先删除触发
        CuckooJobNextJobCriteria curJobCrt = new CuckooJobNextJobCriteria();
        curJobCrt.createCriteria().andIdEqualTo(nextJobs.get(0).getJobId());
        cuckooJobNextJobMapper.deleteByExample(curJobCrt);

        // 再增加触发
        for (JobNext jobNext : nextJobs) {
            CuckooJobNextJob cuckooJobNextJob = CuckBeanUtil.parseJobNext(jobNext);
            try {
                cuckooJobNextJobMapper.insertSelective(cuckooJobNextJob);
            } catch (Exception e) {
                log.error("add next job error, may be more then one job triggler job:{},it's forbidden.{}", cuckooJobNextJob.getNextJobId(), cuckooJobNextJob);
                throw new BaseException("may be more then one job triggler job:{},it's forbidden.{}", cuckooJobNextJob.getNextJobId(), cuckooJobNextJob);
            }
        }
    }

    @Override
    public List<Long> findNextJobIdByJobId(Long jobId) {
        CuckooJobNextJobCriteria curJobCrt = new CuckooJobNextJobCriteria();
        curJobCrt.createCriteria().andJobIdEqualTo(jobId);
        List<CuckooJobNextJob> nextJobs = cuckooJobNextJobMapper.selectByExample(curJobCrt);
        return nextJobs.stream().map(j -> j.getNextJobId()).collect(Collectors.toList());
    }

    @Override
    public Long findJobIdByNextJobId(Long nextJobId) {
        CuckooJobNextJobCriteria preJobCrt = new CuckooJobNextJobCriteria();
        preJobCrt.createCriteria().andNextJobIdEqualTo(nextJobId);

        List<CuckooJobNextJob> preJobs = cuckooJobNextJobMapper.selectByExample(preJobCrt);
        if (CollectionUtils.isNotEmpty(preJobs)) {
            return preJobs.get(0).getJobId();
        }
        return null;
    }

    @Transactional
    @Override
    public void addOrUpdate(Long jobId, Long nextJobId) {
        // 一个任务只能有一个任务触发
        deletePreJob(nextJobId);

        CuckooJobNextJob cuckooJobNextJob = new CuckooJobNextJob();
        cuckooJobNextJob.setJobId(jobId);
        cuckooJobNextJob.setNextJobId(nextJobId);
        cuckooJobNextJobMapper.insert(cuckooJobNextJob);
    }

    @Transactional
    @Override
    public void deletePreJob(Long jobId) {
        CuckooJobNextJobCriteria preJobCrt = new CuckooJobNextJobCriteria();
        preJobCrt.createCriteria().andNextJobIdEqualTo(jobId);
        cuckooJobNextJobMapper.deleteByExample(preJobCrt);
    }

}
