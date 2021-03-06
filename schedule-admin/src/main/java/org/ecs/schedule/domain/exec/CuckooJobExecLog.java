package org.ecs.schedule.domain.exec;

import java.io.Serializable;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

public class CuckooJobExecLog implements Serializable {
    /**
     * 标准ID -- cuckoo_job_exec_log.id
     * 
     */
    private Long id;

    /**
     * 任务ID -- cuckoo_job_exec_log.job_id
     * 
     */
    private Long jobId;

    /**
     * 分组ID -- cuckoo_job_exec_log.group_id
     * 
     */
    private Long groupId;

    /**
     * 任务类型 -- cuckoo_job_exec_log.exec_job_type
     * 
     */
    private String execJobType;

    /**
     * 作业执行应用名 -- cuckoo_job_exec_log.job_class_application
     * 
     */
    private String jobClassApplication;

    /**
     * 任务名称 -- cuckoo_job_exec_log.job_name
     * 
     */
    private String jobName;

    /**
     * 触发类型 -- cuckoo_job_exec_log.trigger_type
     * 
     */
    private String triggerType;

    /**
     * 是否为日切任务 -- cuckoo_job_exec_log.type_daily
     * 
     */
    private String typeDaily;

    /**
     * cron任务表达式 -- cuckoo_job_exec_log.cron_expression
     * 
     */
    private String cronExpression;

    /**
     * 任务执行业务日期 -- cuckoo_job_exec_log.tx_date
     * 
     */
    private Integer txDate;

    /**
     * 流式任务上一次时间参数 -- cuckoo_job_exec_log.flow_last_time
     * 
     */
    private Long flowLastTime;

    /**
     * 流式任务当前时间参数 -- cuckoo_job_exec_log.flow_cur_time
     * 
     */
    private Long flowCurTime;

    /**
     * 并发/集群任务参数 -- cuckoo_job_exec_log.cuckoo_parallel_job_args
     * 
     */
    private String cuckooParallelJobArgs;

    /**
     * 任务开始时间 -- cuckoo_job_exec_log.job_start_time
     * 
     */
    private Long jobStartTime;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column cuckoo_job_exec_log.job_exec_time
     * 
     */
    private Long jobExecTime;

    /**
     * 任务结束时间 -- cuckoo_job_exec_log.job_end_time
     * 
     */
    private Long jobEndTime;

    /**
     * 执行状态 -- cuckoo_job_exec_log.exec_job_status
     * 
     */
    private String execJobStatus;

    /**
     * 执行器IP -- cuckoo_job_exec_log.cuckoo_client_ip
     * 
     */
    private String cuckooClientIp;

    /**
     * 客户端标识 -- cuckoo_job_exec_log.cuckoo_client_port
     * 
     */
    private Integer cuckooClientPort;

    /**
     * 最近检查时间 -- cuckoo_job_exec_log.latest_check_time
     * 
     */
    private Long latestCheckTime;

    /**
     * 是否触发下级任务 -- cuckoo_job_exec_log.need_triggle_next
     * 
     */
    private Boolean needTriggleNext;

    /**
     * 是否强制触发 -- cuckoo_job_exec_log.force_triggle
     * 
     */
    private Boolean forceTriggle;

    /**
     * 备注 -- cuckoo_job_exec_log.remark
     * 
     */
    private String remark;

    /**
     * cuckoo_job_exec_log表的操作属性:serialVersionUID
     * 
     */
    private static final long serialVersionUID = 1L;

    /**
     * 数据字段 cuckoo_job_exec_log.id的get方法 
     * 
     */
    public Long getId() {
        return id;
    }

    /**
     * 数据字段 cuckoo_job_exec_log.id的set方法
     * 
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * 数据字段 cuckoo_job_exec_log.job_id的get方法 
     * 
     */
    public Long getJobId() {
        return jobId;
    }

    /**
     * 数据字段 cuckoo_job_exec_log.job_id的set方法
     * 
     */
    public void setJobId(Long jobId) {
        this.jobId = jobId;
    }

    /**
     * 数据字段 cuckoo_job_exec_log.group_id的get方法 
     * 
     */
    public Long getGroupId() {
        return groupId;
    }

    /**
     * 数据字段 cuckoo_job_exec_log.group_id的set方法
     * 
     */
    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    /**
     * 数据字段 cuckoo_job_exec_log.exec_job_type的get方法 
     * 
     */
    public String getExecJobType() {
        return execJobType;
    }

    /**
     * 数据字段 cuckoo_job_exec_log.exec_job_type的set方法
     * 
     */
    public void setExecJobType(String execJobType) {
        this.execJobType = execJobType == null ? null : execJobType.trim();
    }

    /**
     * 数据字段 cuckoo_job_exec_log.job_class_application的get方法 
     * 
     */
    public String getJobClassApplication() {
        return jobClassApplication;
    }

    /**
     * 数据字段 cuckoo_job_exec_log.job_class_application的set方法
     * 
     */
    public void setJobClassApplication(String jobClassApplication) {
        this.jobClassApplication = jobClassApplication == null ? null : jobClassApplication.trim();
    }

    /**
     * 数据字段 cuckoo_job_exec_log.job_name的get方法 
     * 
     */
    public String getJobName() {
        return jobName;
    }

    /**
     * 数据字段 cuckoo_job_exec_log.job_name的set方法
     * 
     */
    public void setJobName(String jobName) {
        this.jobName = jobName == null ? null : jobName.trim();
    }

    /**
     * 数据字段 cuckoo_job_exec_log.trigger_type的get方法 
     * 
     */
    public String getTriggerType() {
        return triggerType;
    }

    /**
     * 数据字段 cuckoo_job_exec_log.trigger_type的set方法
     * 
     */
    public void setTriggerType(String triggerType) {
        this.triggerType = triggerType == null ? null : triggerType.trim();
    }

    /**
     * 数据字段 cuckoo_job_exec_log.type_daily的get方法 
     * 
     */
    public String getTypeDaily() {
        return typeDaily;
    }

    /**
     * 数据字段 cuckoo_job_exec_log.type_daily的set方法
     * 
     */
    public void setTypeDaily(String typeDaily) {
        this.typeDaily = typeDaily == null ? null : typeDaily.trim();
    }

    /**
     * 数据字段 cuckoo_job_exec_log.cron_expression的get方法 
     * 
     */
    public String getCronExpression() {
        return cronExpression;
    }

    /**
     * 数据字段 cuckoo_job_exec_log.cron_expression的set方法
     * 
     */
    public void setCronExpression(String cronExpression) {
        this.cronExpression = cronExpression == null ? null : cronExpression.trim();
    }

    /**
     * 数据字段 cuckoo_job_exec_log.tx_date的get方法 
     * 
     */
    public Integer getTxDate() {
        return txDate;
    }

    /**
     * 数据字段 cuckoo_job_exec_log.tx_date的set方法
     * 
     */
    public void setTxDate(Integer txDate) {
        this.txDate = txDate;
    }

    /**
     * 数据字段 cuckoo_job_exec_log.flow_last_time的get方法 
     * 
     */
    public Long getFlowLastTime() {
        return flowLastTime;
    }

    /**
     * 数据字段 cuckoo_job_exec_log.flow_last_time的set方法
     * 
     */
    public void setFlowLastTime(Long flowLastTime) {
        this.flowLastTime = flowLastTime;
    }

    /**
     * 数据字段 cuckoo_job_exec_log.flow_cur_time的get方法 
     * 
     */
    public Long getFlowCurTime() {
        return flowCurTime;
    }

    /**
     * 数据字段 cuckoo_job_exec_log.flow_cur_time的set方法
     * 
     */
    public void setFlowCurTime(Long flowCurTime) {
        this.flowCurTime = flowCurTime;
    }

    /**
     * 数据字段 cuckoo_job_exec_log.cuckoo_parallel_job_args的get方法 
     * 
     */
    public String getCuckooParallelJobArgs() {
        return cuckooParallelJobArgs;
    }

    /**
     * 数据字段 cuckoo_job_exec_log.cuckoo_parallel_job_args的set方法
     * 
     */
    public void setCuckooParallelJobArgs(String cuckooParallelJobArgs) {
        this.cuckooParallelJobArgs = cuckooParallelJobArgs == null ? null : cuckooParallelJobArgs.trim();
    }

    /**
     * 数据字段 cuckoo_job_exec_log.job_start_time的get方法 
     * 
     */
    public Long getJobStartTime() {
        return jobStartTime;
    }

    /**
     * 数据字段 cuckoo_job_exec_log.job_start_time的set方法
     * 
     */
    public void setJobStartTime(Long jobStartTime) {
        this.jobStartTime = jobStartTime;
    }

    /**
     * 数据字段 cuckoo_job_exec_log.job_exec_time的get方法 
     * 
     */
    public Long getJobExecTime() {
        return jobExecTime;
    }

    /**
     * 数据字段 cuckoo_job_exec_log.job_exec_time的set方法
     * 
     */
    public void setJobExecTime(Long jobExecTime) {
        this.jobExecTime = jobExecTime;
    }

    /**
     * 数据字段 cuckoo_job_exec_log.job_end_time的get方法 
     * 
     */
    public Long getJobEndTime() {
        return jobEndTime;
    }

    /**
     * 数据字段 cuckoo_job_exec_log.job_end_time的set方法
     * 
     */
    public void setJobEndTime(Long jobEndTime) {
        this.jobEndTime = jobEndTime;
    }

    /**
     * 数据字段 cuckoo_job_exec_log.exec_job_status的get方法 
     * 
     */
    public String getExecJobStatus() {
        return execJobStatus;
    }

    /**
     * 数据字段 cuckoo_job_exec_log.exec_job_status的set方法
     * 
     */
    public void setExecJobStatus(String execJobStatus) {
        this.execJobStatus = execJobStatus == null ? null : execJobStatus.trim();
    }

    /**
     * 数据字段 cuckoo_job_exec_log.cuckoo_client_ip的get方法 
     * 
     */
    public String getCuckooClientIp() {
        return cuckooClientIp;
    }

    /**
     * 数据字段 cuckoo_job_exec_log.cuckoo_client_ip的set方法
     * 
     */
    public void setCuckooClientIp(String cuckooClientIp) {
        this.cuckooClientIp = cuckooClientIp == null ? null : cuckooClientIp.trim();
    }

    /**
     * 数据字段 cuckoo_job_exec_log.cuckoo_client_port的get方法 
     * 
     */
    public Integer getCuckooClientPort() {
        return cuckooClientPort;
    }

    /**
     * 数据字段 cuckoo_job_exec_log.cuckoo_client_port的set方法
     * 
     */
    public void setCuckooClientPort(Integer cuckooClientPort) {
        this.cuckooClientPort = cuckooClientPort;
    }

    /**
     * 数据字段 cuckoo_job_exec_log.latest_check_time的get方法 
     * 
     */
    public Long getLatestCheckTime() {
        return latestCheckTime;
    }

    /**
     * 数据字段 cuckoo_job_exec_log.latest_check_time的set方法
     * 
     */
    public void setLatestCheckTime(Long latestCheckTime) {
        this.latestCheckTime = latestCheckTime;
    }

    /**
     * 数据字段 cuckoo_job_exec_log.need_triggle_next的get方法 
     * 
     */
    public Boolean getNeedTriggleNext() {
        return needTriggleNext;
    }

    /**
     * 数据字段 cuckoo_job_exec_log.need_triggle_next的set方法
     * 
     */
    public void setNeedTriggleNext(Boolean needTriggleNext) {
        this.needTriggleNext = needTriggleNext;
    }

    /**
     * 数据字段 cuckoo_job_exec_log.force_triggle的get方法 
     * 
     */
    public Boolean getForceTriggle() {
        return forceTriggle;
    }

    /**
     * 数据字段 cuckoo_job_exec_log.force_triggle的set方法
     * 
     */
    public void setForceTriggle(Boolean forceTriggle) {
        this.forceTriggle = forceTriggle;
    }

    /**
     * 数据字段 cuckoo_job_exec_log.remark的get方法 
     * 
     */
    public String getRemark() {
        return remark;
    }

    /**
     * 数据字段 cuckoo_job_exec_log.remark的set方法
     * 
     */
    public void setRemark(String remark) {
        this.remark = remark == null ? null : remark.trim();
    }

    public String toString() {
        return ReflectionToStringBuilder.toString(this);
    }
}