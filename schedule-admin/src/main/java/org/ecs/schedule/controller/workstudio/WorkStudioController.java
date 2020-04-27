package org.ecs.schedule.controller.workstudio;

import org.ecs.schedule.controller.BaseController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/workstudio")
public class WorkStudioController extends BaseController {

    @RequestMapping
    public String index0(HttpServletRequest request, Long groupId, Long jobId) {
        return index(request, groupId, jobId);
    }

    @RequestMapping(value = "/index")
    public String index(HttpServletRequest request, Long groupId, Long jobId) {
        request.setAttribute("overHours", 3);
        return "workstudio/workstudio.index";
    }

}
