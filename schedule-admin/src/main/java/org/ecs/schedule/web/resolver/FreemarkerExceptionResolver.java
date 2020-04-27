package org.ecs.schedule.web.resolver;

import freemarker.core.Environment;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Writer;

/**
 * freemarker页面上的异常控制
 * 对应：freemarkerConfigurer
 **/
public class FreemarkerExceptionResolver implements TemplateExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(FreemarkerExceptionResolver.class);

    public void handleTemplateException(TemplateException te, Environment env, Writer out) throws TemplateException {

        LOGGER.error("[Freemarker Error: " + te.getMessage() + "]", te);
        throw te;
    }

}
