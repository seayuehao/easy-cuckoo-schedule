package org.ecs.schedule.config;

import org.ecs.schedule.web.interceptor.LogonInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


@Configuration
public class GlobalWebConfig implements WebMvcConfigurer {

    @Autowired
    private LogonInterceptor logonInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(logonInterceptor).addPathPatterns("/**");
    }

}

