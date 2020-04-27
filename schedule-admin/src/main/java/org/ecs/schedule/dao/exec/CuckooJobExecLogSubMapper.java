package org.ecs.schedule.dao.exec;

import org.ecs.schedule.domain.exec.CuckooJobExecLog;
import org.ecs.schedule.qry.QryBase;

import java.util.List;

public interface CuckooJobExecLogSubMapper {

    Integer countOverTimeJobs(QryBase qry);

    List<CuckooJobExecLog> pageOverTimeJobs(QryBase qry);

    List<CuckooJobExecLog> pagePendingJobs(QryBase qry);

    Integer countPendingJobs(QryBase qry);
}