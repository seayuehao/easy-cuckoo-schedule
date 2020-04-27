package org.ecs.schedule.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Slf4j
@Aspect
@Component
public class ServiceExecutionAspect {

    private static final Gson GSON = new GsonBuilder().create();

    @Pointcut("execution(* org.ecs.schedule.service.*.impl.*.*(..))")
    public void pointcut() {
    }

    @Around("pointcut()")
    public Object exec(ProceedingJoinPoint pjp) throws Throwable {
        long before = System.currentTimeMillis();
        MethodSignature methodSignature = (MethodSignature) pjp.getSignature();
        Method method = methodSignature.getMethod();
        Object[] arr = pjp.getArgs();
        Object result = pjp.proceed(arr);
        String fullMethodName = methodSignature.getDeclaringType().getSimpleName() + "." + method.getName() + "()";
        long delta = System.currentTimeMillis() - before;
        String jsonResult = (result == null) ? null : GSON.toJson(result);
        log.info("@#@@#@@# executingMethod {} with {} params: {} with result: {},  duration of {} ms", fullMethodName,
                arr == null ? 0 : arr.length, GSON.toJson(arr), jsonResult, delta);
        return result;
    }

}