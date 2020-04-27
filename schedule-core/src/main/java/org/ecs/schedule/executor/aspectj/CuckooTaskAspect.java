package org.ecs.schedule.executor.aspectj;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.ecs.schedule.bean.JobInfoBean;
import org.ecs.schedule.enums.CuckooMessageType;
import org.ecs.schedule.exception.BaseException;
import org.ecs.schedule.executor.annotation.CuckooTask;
import org.ecs.schedule.net.client.NettyClientSide;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class CuckooTaskAspect {

    @Around("@annotation(task)")
    public Object lockWait(ProceedingJoinPoint pjp, CuckooTask task) throws Throwable {
        JobInfoBean jobinfo = null;
        Object rtn = null;
        try {
            Signature sign = pjp.getSignature();
            Object[] args = pjp.getArgs();
            if (null == args || args.length < 1) {
                log.error("unknown exception :can not get task param! pjp:{},task:{}", pjp, task.value());
                throw new BaseException("unknown exception :can not get task param! pjp:{},task:{}", pjp, task.value());
            }
            jobinfo = (JobInfoBean) args[0];
            log.info("task exec start taskName:{} , exector:{} , params :{}", task.value(), sign, jobinfo);

            rtn = pjp.proceed();

            // 发送服务端，任务执行完成
            jobinfo.setErrMessage("succeed!");
            NettyClientSide.send(CuckooMessageType.JOBSUCCED, jobinfo);
            log.info("task exec successfully taskName:{}, jobInfo:{}", task.value(), jobinfo);
        } catch (Throwable e) {
            log.error("task exec error taskName:{}", task.value(), e);
            // 发送服务端，任务执行失败
            jobinfo.setErrMessage(e.getMessage());
            NettyClientSide.send(CuckooMessageType.JOBFAILED, jobinfo);
            throw e;
        }
        return rtn;
    }

}
