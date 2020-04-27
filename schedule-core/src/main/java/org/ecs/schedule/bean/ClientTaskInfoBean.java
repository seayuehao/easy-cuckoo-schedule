package org.ecs.schedule.bean;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

@Getter
@Setter
public class ClientTaskInfoBean {
	
	private String appName;
	
	private String beanName;
	
	private String methodName;
	
	private String taskName;
	
	private String clientTag;

	@Override
	public String toString() {
		
		return ReflectionToStringBuilder.toString(this);
	}
}
