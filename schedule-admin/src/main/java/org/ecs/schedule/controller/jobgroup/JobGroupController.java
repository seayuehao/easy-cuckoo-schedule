package org.ecs.schedule.controller.jobgroup;

import org.ecs.schedule.controller.BaseController;
import org.ecs.schedule.domain.exec.CuckooJobGroup;
import org.ecs.schedule.exception.BaseException;
import org.ecs.schedule.qry.job.GroupAuthQry;
import org.ecs.schedule.service.auth.CuckooAuthService;
import org.ecs.schedule.service.job.CuckooGroupService;
import org.ecs.schedule.vo.auth.CuckooGroupAuthVo;
import org.ecs.schedule.vo.job.JobGroup;
import org.ecs.util.dao.PageDataList;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Controller
@RequestMapping("/jobgroup")
public class JobGroupController extends BaseController {

    @Autowired
    private CuckooGroupService cuckooGroupService;

    @Autowired
    private CuckooAuthService cuckooAuthService;

    @RequestMapping
    public String index0(HttpServletRequest request) {
        return index(request);
    }

    @RequestMapping(value = "/index")
    public String index(HttpServletRequest request) {
        List<CuckooJobGroup> jobGroups = cuckooGroupService.listAllGroup();
        request.setAttribute("jobGroups", jobGroups);
        return "jobgroup/jobgroup.index";
    }

    @RequestMapping(value = "/save")
    @ResponseBody
    public Object save(JobGroup jobGroup) {
        CuckooJobGroup cuckooJobGroup = new CuckooJobGroup();
        BeanUtils.copyProperties(jobGroup, cuckooJobGroup);
        if (StringUtils.isEmpty(jobGroup.getGroupName())) {
            throw new BaseException("group name can not be null");
        }
        if (jobGroup.getId() != null) {
            cuckooGroupService.updateByPk(cuckooJobGroup);
        } else {
            cuckooGroupService.addGroup(cuckooJobGroup);
        }
        return success();
    }


    @RequestMapping(value = "/remove")
    @ResponseBody
    public Object remove(Long id) {
        if (null == id) {
            throw new BaseException("id can not be null");
        }
        cuckooGroupService.deleteById(id);
        return success();
    }


    @RequestMapping(value = "/groupauthlist")
    @ResponseBody
    public Object authList(GroupAuthQry qry) {
        if (null == qry.getGroupId()) {
            return dataTable(null);
        }
        PageDataList<CuckooGroupAuthVo> pageData = cuckooAuthService.pageGroupAuth(qry);
        return dataTable(pageData);
    }

    @RequestMapping(value = "/changeAuth")
    @ResponseBody
    public Object changeAuth(String type, Long authId, Long userId, Long groupId) {
        if (StringUtils.isEmpty(type)) {
            throw new BaseException("param error,type can not be null");
        }
        if (null != authId && (null == userId || null == groupId)) {
            throw new BaseException("param error,authId:{},userId:{},groupId:{}", authId, userId, groupId);
        }
        cuckooAuthService.changeAuth(type, authId, userId, groupId);
        return success();
    }

}
