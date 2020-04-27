package org.ecs.schedule.component.mail;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class NotificationEmailSupport {

    public boolean sendEmail(String mailTo, String title, String content) throws Exception {
        log.info("#@>>>> mocking sending email: {}, {}, {}", mailTo, title, content);
        return true;
    }

}
