package org.ecs.schedule.web.resolver;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.ecs.schedule.enums.CuckooAdminPages;
import org.ecs.schedule.exception.BaseException;
import org.ecs.schedule.web.util.JsonResult;
import org.ecs.schedule.web.util.JsonResult.Status;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.SimpleMappingExceptionResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * 异常处理类
 */
@Slf4j
@Component
public class ExceptionResolver extends SimpleMappingExceptionResolver {

    private static final Gson gson = new GsonBuilder().create();

    @Override
    public ModelAndView doResolveException(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {

        // 把异常信息记入日志
        if (null != ex) {
            log.error("ERROR_MARK: {},message", ex.getClass(), ex.getMessage(), ex);
        }

        String message = "";
        if (ex instanceof BaseException) {
            message = ex.getMessage(); // 自定义异常特殊处理
        } else {
            message = "系统异常:" + ex.getMessage();
        }
        String requestType = request.getHeader("X-Requested-With");
        //判断用户请求方式是否为ajax
        if (StringUtils.isNotBlank(requestType) && requestType.equals("XMLHttpRequest")) {
            // Json返回数据
            JsonResult<String> json = new JsonResult<String>(Status.ERROR, message);
            // 打印输出json字符串
            printJson(response, gson.toJson(json));
            return null;
        } else {
            ModelAndView modelAndView = new ModelAndView(CuckooAdminPages.ERROR.getValue());
            modelAndView.addObject("message", StringUtils.trimToNull(message));
            return modelAndView;
        }
    }


    /**
     * @param response
     * @param json     输出文字
     * @return void 返回类型
     * @throws
     * @Title: printJson
     * @Description: 打印输出json字符串
     */
    private void printJson(final HttpServletResponse response, final String json) {
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = null;
        try {
            out = response.getWriter();
            out.print(json);

        } catch (IOException ex) {
            log.error(json, ex);
        } finally {
            if (out != null)
                out.flush();
            if (out != null)
                out.close();
        }
    }

    @Override
    public int getOrder() {
        return 0;
    }

}
