package org.ecs.schedule.executor;

import lombok.extern.slf4j.Slf4j;
import org.ecs.schedule.bean.JobInfoBean;
import org.ecs.schedule.exception.BaseException;
import org.ecs.schedule.executor.framerwork.CuckooClient;
import org.ecs.schedule.executor.framerwork.bean.CuckooTaskBean;
import org.springframework.context.ApplicationContext;

import java.lang.reflect.Method;

@Slf4j
public class CuckooExecutor {


    public static void exec(CuckooTaskBean task, JobInfoBean taskParam) {
        if (null == task) {
            return;
        }

        ApplicationContext applicationContext = CuckooClient.getApplicationContext();
        if (null == applicationContext) {
            log.error("#@>>> CuckooClient not initialized!");
            return;
        }

        Object obj = applicationContext.getBean(task.getBeanName());
        if (null == obj) {
            log.error("#@>>> target bean not found with beanName: {}", task.getBeanName());
        }

        try {
            Class<?> clazz = obj.getClass();
            Method method = clazz.getMethod(task.getMethodName(), JobInfoBean.class);
            method.invoke(obj, taskParam);
        } catch (BaseException e) {
            log.error("task executor cause a biz exception:{}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("task executor cause a unknown error:{}", e.getMessage(), e);
            throw new BaseException("task executor cause a unknown error:{}", e.getMessage());
        }
    }

}
