package org.ecs.schedule.vo.job;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

/**
 * 任务分组信息
 */
@Getter
@Setter
public class JobGroup {
	
	/**
	 * 分组id
	 */
	private Long id;
	
	/**
	 * 分组名称
	 */
	private String groupName;
	
	/**
	 * 分组描述
	 */
	private String groupDesc;


	@Override
	public String toString() {
		
		return ReflectionToStringBuilder.toString(this);
	}
	
	
	
}
