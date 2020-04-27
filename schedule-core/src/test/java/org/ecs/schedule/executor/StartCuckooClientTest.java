package org.ecs.schedule.executor;

import org.ecs.schedule.ServiceUnitBaseTest;
import org.ecs.schedule.bean.JobInfoBean;
import org.ecs.schedule.executor.framerwork.CuckooClient;
import org.ecs.schedule.service.CuckooTestTask;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import java.lang.reflect.Method;

public class StartCuckooClientTest extends ServiceUnitBaseTest {

    @Autowired
    CuckooTestTask cuckooTestTask;

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    public void start() {

        try {
//			cuckooTestTask.testJob(new JobInfoBean());
//			cuckooTestTask.testJobTmp(new JobInfoBean());

            // 模拟客户端动态接收到消息
            // beanName:cuckooTestTask2Impl,method:testJob,taskName=testJob2
            ApplicationContext applicationContext = CuckooClient.getApplicationContext();
            Object obj = applicationContext.getBean("cuckooTestTask2Impl");
            JobInfoBean jobInfo = new JobInfoBean();
            jobInfo.setCuckooParallelJobArgs("");
            jobInfo.setFlowCurrTime(System.currentTimeMillis());
            jobInfo.setFlowLastTime(System.currentTimeMillis());
            jobInfo.setJobId(1L);
            jobInfo.setJobLogId(1L);
            jobInfo.setJobName("testJob2");
//			jobInfo.setNeedTrigglerNext(true);
//			jobInfo.setTriggerType(CuckooJobTriggerType.CRON);
            jobInfo.setTxDate(20160101);

            try {
                Class claz = obj.getClass();
                Method method = claz.getMethod("testJob", JobInfoBean.class);
                method.invoke(obj, jobInfo);
            } catch (Exception e) {
                e.printStackTrace();
            }

            Thread.sleep(300000);
        } catch (InterruptedException e) {
            // ignores
        }
    }

}
