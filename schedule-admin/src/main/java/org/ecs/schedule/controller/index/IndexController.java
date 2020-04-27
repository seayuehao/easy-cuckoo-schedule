package org.ecs.schedule.controller.index;

import org.ecs.schedule.controller.BaseController;
import org.ecs.schedule.enums.CuckooAdminPages;
import org.ecs.schedule.exception.BaseException;
import org.ecs.schedule.web.core.NoLoginCheck;
import org.apache.commons.collections.CollectionUtils;
import org.quartz.CronExpression;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * index controller
 */
@Controller
public class IndexController extends BaseController {


    @NoLoginCheck
    @RequestMapping("/")
    public String index() {
        return redict(CuckooAdminPages.INDEX.getValue());
    }

    @RequestMapping("/crontab")
    public String crontab() {
        return "/crontab";
    }

    @ResponseBody
    @RequestMapping("/calcLastRuntime")
    public Object calcLastRuntime(String cronExpression) {
        List<String> list = new ArrayList<>();
        try {
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("YYYY-MM-dd HH:mm:ss");
            CronExpression exp = new CronExpression(cronExpression);
            ZoneId zoneId = ZoneId.systemDefault();

            Date d = new Date();
            int i = 0;
            // 循环得到接下来n此的触发时间点，供验证  
            while (i < 5) {
                d = exp.getNextValidTimeAfter(d);
                if (null == d) {
                    break;
                }
                list.add(dtf.format(LocalDateTime.ofInstant(d.toInstant(), zoneId)));
                ++i;
            }
        } catch (ParseException e) {

            throw new BaseException("ERROR:{}, cronExp:{}", e.getMessage(), cronExpression);
        }
        if (CollectionUtils.isEmpty(list)) {
            throw new BaseException("can not get nearest 5 exec time, cronExp:{}", cronExpression);
        }

        return success(list);
    }

}
