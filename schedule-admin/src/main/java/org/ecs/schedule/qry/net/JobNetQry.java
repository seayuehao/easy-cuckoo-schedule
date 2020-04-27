package org.ecs.schedule.qry.net;

import org.ecs.schedule.qry.QryBase;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

@Getter
@Setter
public class JobNetQry extends QryBase {

    private String jobClassApplication;

    private String jobName;

    @Override
    public String toString() {

        return ReflectionToStringBuilder.toString(this);
    }
}
