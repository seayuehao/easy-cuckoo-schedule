package org.ecs.schedule.vo.net;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import java.util.Set;


@Getter
@Setter
public class CuckooNetRegistJobVo {
    /**
     * 标准ID -- cuckoo_net_regist_job.id
     */
    private Long id;

    /**
     * 作业执行应用名 -- cuckoo_net_regist_job.job_class_application
     */
    private String jobClassApplication;

    /**
     * 任务名称 -- cuckoo_net_regist_job.job_name
     */
    private String jobName;

    /**
     * 实现类名称 -- cuckoo_net_regist_job.bean_name
     */
    private String beanName;

    /**
     * 方法名称 -- cuckoo_net_regist_job.method_name
     */
    private String methodName;

    /**
     * 创建时间 -- cuckoo_net_regist_job.create_date
     */
    private Long createDate;

    /**
     * 修改时间 -- cuckoo_net_regist_job.modify_date
     */
    private Long modifyDate;

    private Set<String> clients;

    private Set<String> servers;

    public String toString() {
        return ReflectionToStringBuilder.toString(this);
    }
}
