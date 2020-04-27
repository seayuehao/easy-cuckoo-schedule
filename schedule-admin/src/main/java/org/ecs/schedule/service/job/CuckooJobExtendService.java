package org.ecs.schedule.service.job;

import org.ecs.schedule.domain.exec.CuckooJobExtend;

public interface CuckooJobExtendService {

    CuckooJobExtend queryById(Long id);

}
