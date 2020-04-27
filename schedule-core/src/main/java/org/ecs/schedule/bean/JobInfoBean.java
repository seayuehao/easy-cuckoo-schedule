package org.ecs.schedule.bean;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import org.ecs.schedule.enums.CuckooJobExecType;

@Getter
@Setter
public class JobInfoBean implements Serializable{

	private static final long serialVersionUID = 1L;
	
	/**
	 * 任务执行流水ID，用于回改状态等
	 */
	private Long jobLogId;
	
	/**
	 * 任务ID，冗余，便于操作
	 */
	private Long jobId;
	
	/**
	 * 任务类型:CUCKOO,SCRIPT
	 */
	private CuckooJobExecType execType;
	
	/**
	 * 是否为日切任务
	 */
	private boolean typeDaily;
	
	/**
	 * 任务/脚本名称，用于与客户端寻找可执行器
	 */
	private String jobName;
	
	/**
	 * 日批任务执行日期参数
	 */
	private Integer txDate;
	
	/**
	 * 流式任务上一次执行开始参数
	 */
	private Long flowLastTime;
	
	/**
	 * 流式任务本次执行参数
	 */
	private Long flowCurrTime;
	
	/**
	 * 是否被强制触发任务。在手工调用时可以选择，默认为否
	 */
//	private Boolean forceJob = false;
	
	/**
	 * 是否需要触发下次任务，默认是。在手工调用的时候，可以设置否 
	 */
	private Boolean needTrigglerNext = true;
	
	/**
	 * 任务执行分片参数
	 */
	private String cuckooParallelJobArgs = "";
	
	/**
	 * 任务触发类型
	 */
//	private CuckooJobTriggerType triggerType;
	
	private String errMessage;

	@Override
	public String toString() {
		return ReflectionToStringBuilder.toString(this);
	}

}
