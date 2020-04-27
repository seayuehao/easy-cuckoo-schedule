package org.ecs.schedule.component.quartz;

import lombok.extern.slf4j.Slf4j;
import org.quartz.spi.TriggerFiredBundle;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.quartz.AdaptableJobFactory;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import javax.sql.DataSource;

@Slf4j
@Configuration
public class QuartzSchedulerConfig {

    @Bean
    public QuartzBeanFactory adaptableJobFactory(AutowireCapableBeanFactory capableBeanFactory) {
        QuartzBeanFactory factory = new QuartzBeanFactory(capableBeanFactory);
        return factory;
    }

    @Bean("quartzScheduler")
    public SchedulerFactoryBean schedulerFactoryBean(@Qualifier("dataSource") DataSource dataSource,
                                                     QuartzBeanFactory quartzBeanFactory,
                                                     ApplicationContext applicationContext) {
        SchedulerFactoryBean factoryBean = new SchedulerFactoryBean();
        try {
            factoryBean.setDataSource(dataSource);
            factoryBean.setJobFactory(quartzBeanFactory);
            factoryBean.setApplicationContext(applicationContext);
            factoryBean.setAutoStartup(false);
            factoryBean.setStartupDelay(3000000); // seconds
            factoryBean.setApplicationContextSchedulerContextKey("applicationContextKey");
            factoryBean.setConfigLocation(new ClassPathResource("/quartz.properties"));
            factoryBean.afterPropertiesSet();
        } catch (Exception e) {
            log.error("#@#@ init schedulerFactoryBean error:", e);
        }
        return factoryBean;
    }


    static class QuartzBeanFactory extends AdaptableJobFactory {

        private AutowireCapableBeanFactory capableBeanFactory;

        public QuartzBeanFactory(AutowireCapableBeanFactory capableBeanFactory) {
            this.capableBeanFactory = capableBeanFactory;
        }

        @Override
        protected Object createJobInstance(TriggerFiredBundle bundle) throws Exception {
            Object jobInstance = super.createJobInstance(bundle);
            capableBeanFactory.autowireBean(jobInstance);
            return jobInstance;
        }
    }

}
