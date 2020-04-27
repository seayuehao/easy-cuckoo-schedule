package org.ecs.schedule.vo.job;

import org.ecs.schedule.enums.CuckooBooleanFlag;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

/**
 * 任务信息
 */
@Getter
@Setter
public class CuckooJobDetailVo {
    /**
     * 标准ID -- cuckoo_job_details.id
     */
    private Long id;

    /**
     * 分组ID -- cuckoo_job_details.group_id
     */
    private Long groupId;

    /**
     * 分组名称
     */
    private String groupName;

    /**
     * 任务名称 -- cuckoo_job_details.job_name
     */
    private String jobName;

    /**
     * 任务类型 -- cuckoo_job_detail.exec_job_type
     */
    private String execJobType;


    private String execJobTypeDesc;

    /**
     * 作业执行应用名 -- cuckoo_job_details.job_class_application
     */
    private String jobClassApplication;


    /**
     * 任务描述 -- cuckoo_job_details.job_desc
     */
    private String jobDesc;

    /**
     * 触发类型 -- cuckoo_job_details.trigger_type
     */
    private String triggerType;

    /**
     * cron任务表达式 -- cuckoo_job_details.cron_expression
     */
    private String cronExpression;

    /**
     * 是否未日切任务
     */
    private String typeDaily = CuckooBooleanFlag.NO.getValue();

    /**
     * 偏移量 -- cuckoo_job_details.offset
     */
    private Integer offset;

    /**
     * 任务状态 -- cuckoo_job_details.job_status
     */
    private String jobStatus;

    /**
     * 并发/集群任务参数 -- cuckoo_job_details.cuckoo_parallel_job_args
     */
    private String cuckooParallelJobArgs;


    /**
     * 触发任务ID
     */
    private Long preJobId;

    /**
     * 依赖任务ID
     */
    private String dependencyIds;

    /**
     * 是否初始化quartz(CRON类型任务，数据库直接初始化)
     */
    private boolean quartzInit;


    /**
     * 超时告警时间
     */
    private Long overTime;
    /**
     * 邮件接收人
     */
    private String mailTo;


    public String toString() {
        return ReflectionToStringBuilder.toString(this);
    }
}
