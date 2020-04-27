package org.ecs.schedule.exception;

import org.slf4j.helpers.MessageFormatter;

public class JobCanNotRunningException extends Throwable{

	private static final long serialVersionUID = 1L;

	public JobCanNotRunningException() {
		super();
	}


	public JobCanNotRunningException(String message) {
		super(message);
	}
	

	public JobCanNotRunningException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}


	public JobCanNotRunningException(String message, Throwable cause) {
		super(message, cause);
	}



	public JobCanNotRunningException(Throwable cause) {
		super(cause);
	}
	
	public JobCanNotRunningException(String format, Object... arguments) {
		
		super(MessageFormatter.arrayFormat(format, arguments).getMessage());
	}
}
