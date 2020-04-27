package org.ecs.schedule.service.job.impl;

import org.ecs.schedule.dao.exec.CuckooJobExtendMapper;
import org.ecs.schedule.domain.exec.CuckooJobExtend;
import org.ecs.schedule.service.job.CuckooJobExtendService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class CuckooJobExtendServiceImpl implements CuckooJobExtendService {

    @Autowired
    private CuckooJobExtendMapper cuckooJobExtendMapper;

    @Override
    public CuckooJobExtend queryById(Long id) {
        return cuckooJobExtendMapper.selectByPrimaryKey(id);
    }

}
