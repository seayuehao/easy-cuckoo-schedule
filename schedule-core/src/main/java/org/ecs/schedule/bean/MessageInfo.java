package org.ecs.schedule.bean;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import org.ecs.schedule.enums.CuckooMessageType;

@Getter
@Setter
public class MessageInfo implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private CuckooMessageType messageType;

	private Object message;

	@Override
	public String toString() {
		return ReflectionToStringBuilder.toString(this);
	}
}
