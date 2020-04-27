package org.ecs.schedule.vo.job;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;


@Getter
@Setter
public class CuckooClientJobExecResult {
	
	/**
	 * true 成功，FALSE失败
	 */
	private boolean success = false;
	
	/**
	 * 失败原因
	 */
	private String remark;

	@Override
	public String toString() {
		
		return ReflectionToStringBuilder.toString(this);
	}
}
