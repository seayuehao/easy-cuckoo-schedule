package org.ecs.schedule.service;

import org.ecs.schedule.ServiceUnitBaseTest;
import org.ecs.schedule.qry.QryBase;
import org.ecs.schedule.service.job.CuckooJobLogService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

public class CuckooJobLogServiceTest extends ServiceUnitBaseTest {

    @Autowired
    CuckooJobLogService cuckooJobLogService;

    @Test
    public void pagePendingList() {

        QryBase qry = new QryBase();
        qry.setStart(0);
        qry.setLimit(1);
        List<Long> groupIds = new ArrayList<>();
        groupIds.add(1L);
        groupIds.add(2L);
        groupIds.add(3L);
        qry.setGroupIds(groupIds);
        System.out.println(cuckooJobLogService.pagePendingList(qry));
    }


    @Test
    public void pageOverTimeJobs() {
        QryBase qry = new QryBase();
        qry.setStart(0);
        qry.setLimit(1);
        List<Long> groupIds = new ArrayList<>();
        groupIds.add(1L);
        groupIds.add(2L);
        groupIds.add(3L);
        qry.setGroupIds(groupIds);
        System.out.println(cuckooJobLogService.pageOverTimeJobs(qry));
    }
}
