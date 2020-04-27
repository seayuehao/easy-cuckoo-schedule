package org.ecs.schedule.controller.manage;

import org.ecs.schedule.controller.BaseController;
import org.ecs.schedule.domain.auth.CuckooAuthUser;
import org.ecs.schedule.enums.CuckooUserAuthType;
import org.ecs.schedule.exception.BaseException;
import org.ecs.schedule.qry.auth.AuthUserQry;
import org.ecs.schedule.service.auth.CuckooAuthService;
import org.ecs.schedule.vo.auth.CuckooLogonInfo;
import org.ecs.util.dao.PageDataList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/manage")
public class ManageController extends BaseController {

    @Autowired
    private CuckooAuthService cuckooAuthService;

    @RequestMapping
    public String index0(HttpServletRequest request) {
        return index(request);
    }

    @RequestMapping(value = "/index")
    public String index(HttpServletRequest request) {
        request.setAttribute("userTypes", CuckooUserAuthType.values());
        if (cuckooAuthService.getLogonInfo().getCuckooUserAuthType().getValue().equals(CuckooUserAuthType.ADMIN.getValue())) {
            // 管理员跳转管理页面
            return "manage/manage.index";
        } else {
            return "redirect:manage/userdetail?id=" + cuckooAuthService.getLogonInfo().getId() + "&from=mine";
        }
    }

    @RequestMapping(value = "/userdetail")
    public String userDetail(HttpServletRequest request, Long id) {
        if (null != id) {
            CuckooAuthUser user = cuckooAuthService.getUserInfoById(id);
            if (null == user) {
                throw new BaseException("can not get user by id:{}", id);
            }

            CuckooLogonInfo logonInfo = cuckooAuthService.getLogonInfo();
            if (!CuckooUserAuthType.ADMIN.getValue().equals(logonInfo.getCuckooUserAuthType().getValue())) {
                // 非管理员，只能查看自己的信息
                if (!logonInfo.getId().equals(user.getId())) {
                    throw new BaseException("user have no right to see other's infomation");
                }
            }
            request.setAttribute("userInfo", user);
        }
        request.setAttribute("userTypes", CuckooUserAuthType.values());
        return "manage/manage.userdetail";
    }

    @RequestMapping("/userList")
    @ResponseBody
    public Object userList(AuthUserQry qry) {
        PageDataList<CuckooAuthUser> userPage = cuckooAuthService.pageAuthUser(qry);
        return dataTable(userPage);
    }

}
