package org.ecs.schedule.qry.auth;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import org.ecs.schedule.qry.QryBase;

@Getter
@Setter
public class AuthUserQry extends QryBase{
	
	private String userAuthType;

	@Override
	public String toString() {
		
		return ReflectionToStringBuilder.toString(this);
	}
}
