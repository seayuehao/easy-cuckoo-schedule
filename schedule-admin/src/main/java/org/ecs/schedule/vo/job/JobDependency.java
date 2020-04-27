package org.ecs.schedule.vo.job;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

/**
 * 依赖任务信息
 */
@Getter
@Setter
public class JobDependency {
	
	/**
	 * 任务ID
	 */
	private Long jobId;
	
	/**
	 * 依赖的任务ID
	 */
	private Long dependencyId;


	@Override
	public String toString() {

		return ReflectionToStringBuilder.toString(this);
	}
	
}
