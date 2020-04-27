package org.ecs.schedule.executor.framerwork;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.ecs.schedule.exception.BaseException;
import org.ecs.schedule.executor.annotation.CuckooTask;
import org.ecs.schedule.executor.aspectj.CuckooTaskAspect;
import org.ecs.schedule.executor.framerwork.bean.ClientInfoBean;
import org.ecs.schedule.executor.framerwork.bean.CuckooTaskBean;
import org.ecs.schedule.executor.framerwork.cache.CuckooTaskCache;
import org.ecs.schedule.net.server.cache.IoServerCollection;
import org.ecs.schedule.net.server.cache.MessageSendQueue;
import org.ecs.schedule.net.vo.IoServerBean;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

@Slf4j
public class CuckooClient implements ApplicationContextAware, BeanPostProcessor, ApplicationListener<ContextRefreshedEvent> {

    private static ApplicationContext applicationContext;

    private String appName;

    private String clientTag;

    private String server;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        CuckooClient.applicationContext = applicationContext;
    }

    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        ClientInfoBean.setAppName(appName);
        this.appName = appName;
    }

    public String getClientTag() {
        return clientTag;
    }


    public void setClientTag(String clientTag) {
        ClientInfoBean.setClientTag(clientTag);
        this.clientTag = clientTag;
    }

    public String getServer() {
        return server;
    }


    public void setServer(String server) {
        this.server = server;
    }


    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (null == bean) {
            return null;
        }

        Method[] methods = bean.getClass().getMethods();
        for (Method method : methods) {
            Annotation[] anns = method.getAnnotations();
            if (ArrayUtils.isEmpty(anns)) continue;

            for (Annotation ann : anns) {
                if (!CuckooTask.class.equals(ann.annotationType())) continue;

                /*
                 * 1.扫描task注解，并且task.value不能为空，不能重复，否则报错
                 */
                CuckooTask task = (CuckooTask) ann;
                String taskName = task.value();
                if (StringUtils.isEmpty(taskName)) {
                    throw new BaseException("cuckoo taskName can not be null, beanName:{}, method:{}", beanName, method.getName());
                }

                if (CuckooTaskCache.contains(taskName)) {
                    CuckooTaskBean taskBean = CuckooTaskCache.get(taskName);
                    throw new BaseException("duplicate taskName,beanName1:{}, method1:{} , beanName2:{}, method2:{}",
                        beanName, method.getName(), taskBean.getBeanName(), taskBean.getMethodName());
                }

                // 在task注解上面，动态增加aspectj
                AspectJProxyFactory factory = new AspectJProxyFactory(bean);
                factory.addAspect(CuckooTaskAspect.class);
                bean = factory.getProxy();

                // 判断增加了CuckooTask注解的，需要增加到缓存里面
                CuckooTaskBean taskBean = new CuckooTaskBean();
                taskBean.setBeanName(beanName);
                taskBean.setMethodName(method.getName());
                taskBean.setTaskName(taskName);
                CuckooTaskCache.put(taskName, taskBean);
                log.info("init cuckooClient beanName: {}, method: {}, taskName:", beanName, method.getName(), task.value());
            }
        }

        return bean;
    }


    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        log.info("@@@ onApplicationEvent runs after spring container initialized successfully; begin connecting to netty-server: {},", server);
        if (StringUtils.isEmpty(server)) {
            log.error("server config is null, Cuckoo will not start, please check!");
            return;
        }

        // multiple servers
        String[] serverArrs = server.trim().split(",");
        if (ArrayUtils.isNotEmpty(serverArrs)) {
            for (String serverStr : serverArrs) {
                String[] serverArr = serverStr.split(":");
                IoServerBean bean = new IoServerBean();
                bean.setIp(serverArr[0]);
                bean.setPort(Integer.valueOf(serverArr[1]));
                IoServerCollection.add(bean);
            }
        }

        IoServerCollection.retryConnect();
        MessageSendQueue.trySendMessage();
    }

}
