package org.ecs.schedule.web.interceptor;

import org.ecs.schedule.constant.CuckooWebConstant;
import org.ecs.schedule.enums.CuckooAdminPages;
import org.ecs.schedule.service.auth.CuckooAuthService;
import org.ecs.schedule.vo.auth.CuckooLogonInfo;
import org.ecs.schedule.web.core.NoLoginCheck;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import org.springframework.web.servlet.resource.ResourceHttpRequestHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;

@Slf4j
@Component
public class LogonInterceptor extends HandlerInterceptorAdapter {

    @Autowired
    CuckooAuthService cuckooAuthService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {

        if (handler instanceof HandlerMethod) {
            HandlerMethod hm = (HandlerMethod) handler;
            if (null != hm.getMethodAnnotation(NoLoginCheck.class)) {
                return true;
            }
        }

        if (handler instanceof ResourceHttpRequestHandler) {
            return true;
        }

        Object logonInfo = request.getSession().getAttribute(CuckooWebConstant.ADMIN_WEB_SESSION_USER_KEY);

        // 如果没有登录或登录超时
        if (null == logonInfo) {
            String requestType = request.getHeader("X-Requested-With");
            // 判断用户请求方式是否为异步请求
            if (StringUtils.isNotBlank(requestType) && requestType.equals("XMLHttpRequest")) {
                String redirect = request.getContextPath() + CuckooAdminPages.LOGIN.getValue();
                response.sendRedirect(redirect);
                return false;
            } else {
                // 未登录时记录上一次操作地址
                String servletPath = request.getServletPath();
                String queryString = request.getQueryString();
                log.info("#@#@# preHandle {} , {}", servletPath, queryString);

                String redirectURL = servletPath;
                if (StringUtils.isNotBlank(queryString)) {
                    redirectURL = request.getContextPath() + servletPath + "?" + StringUtils.trimToEmpty(queryString);
                }
                redirectURL = URLEncoder.encode(redirectURL, CuckooWebConstant.ADMIN_WEB_ENCODING);
                String allUrl = request.getContextPath() + CuckooAdminPages.LOGIN.getValue() + "?redirectURL=" + redirectURL;
                // 转到登录页
                response.sendRedirect(allUrl);
            }
            return false;
        } else {
            // 设置threadLocal
            CuckooLogonInfo cuckooLogonInfo = (CuckooLogonInfo) logonInfo;
            cuckooAuthService.setLogonInfo(cuckooLogonInfo);
            log.info("#@#@# preHandleCuckooLogonInfo: {}", cuckooLogonInfo);
            request.setAttribute("logonInfo", cuckooLogonInfo);
        }

        return true;
    }


    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
            throws Exception {
        super.afterCompletion(request, response, handler, ex);
        cuckooAuthService.clearLogon();
    }

}
