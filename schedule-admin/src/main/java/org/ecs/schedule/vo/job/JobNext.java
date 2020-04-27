package org.ecs.schedule.vo.job;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

/**
 * 下级任务信息
 */
@Getter
@Setter
public class JobNext {

    /**
     * 当前任务id
     */
    private Long jobId;

    /**
     * 触发的下一级任务ID
     */
    private Long nextJobId;


    @Override
    public String toString() {

        return ReflectionToStringBuilder.toString(this);
    }
}
