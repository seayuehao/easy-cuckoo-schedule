package org.ecs.schedule.qry;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
public class QryBase implements Serializable {

    private Integer start;
    private Integer limit;

    /**
     * 公共分组权限
     */
    private List<Long> groupIds;

    @Override
    public String toString() {

        return ReflectionToStringBuilder.toString(this);
    }
}
