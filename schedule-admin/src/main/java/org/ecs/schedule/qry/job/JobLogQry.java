package org.ecs.schedule.qry.job;

import org.ecs.schedule.qry.QryBase;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import java.util.List;

@Getter
@Setter
public class JobLogQry extends QryBase {

    private Long logId;
    private Long groupId;
    private Long jobId;

    private List<String> jobStatus;

    private String filterTime;

    private Long startDateTime;

    private Long endDateTime;

    @Override
    public String toString() {

        return ReflectionToStringBuilder.toString(this);
    }
}
