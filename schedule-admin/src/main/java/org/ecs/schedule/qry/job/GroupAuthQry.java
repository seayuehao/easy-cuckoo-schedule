package org.ecs.schedule.qry.job;

import org.ecs.schedule.qry.QryBase;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

@Getter
@Setter
public class GroupAuthQry extends QryBase {

    private Long groupId;

    @Override
    public String toString() {

        return ReflectionToStringBuilder.toString(this);
    }
}
