package org.ecs.schedule.manage;

import lombok.extern.slf4j.Slf4j;
import org.ecs.schedule.component.quartz.QuartzManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

/**
 * 调度器初始化控制器
 */
@Slf4j
@Component
public class CuckooContainerManager implements ApplicationListener<ContextRefreshedEvent> {

    @Autowired
    private QuartzManager quartzManager;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        log.info("#@ onApplicationEvent");

        if (event.getApplicationContext().getParent() == null) {
            // donothing
            log.info("项目启动完成");
            quartzManager.addAutoJob();
        }
    }

}
