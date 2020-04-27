package org.ecs.schedule.service;

import org.ecs.schedule.ServiceUnitBaseTest;
import org.ecs.schedule.domain.exec.CuckooJobGroup;
import org.ecs.schedule.service.job.CuckooGroupService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class CuckooGroupServiceTest extends ServiceUnitBaseTest {

    @Autowired
    CuckooGroupService cuckooGroupService;

    @Test
    public void testAddGroup() {

        CuckooJobGroup group = new CuckooJobGroup();
        group.setGroupName("单测分组");
        group.setGroupDesc("单测分组说明");
        Long id = cuckooGroupService.addGroup(group);
        System.out.println(id);
    }

}
