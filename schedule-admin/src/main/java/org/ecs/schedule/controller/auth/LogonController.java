package org.ecs.schedule.controller.auth;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.ecs.schedule.constant.CuckooWebConstant;
import org.ecs.schedule.controller.BaseController;
import org.ecs.schedule.domain.auth.CuckooAuthUser;
import org.ecs.schedule.enums.CuckooAdminPages;
import org.ecs.schedule.enums.CuckooUserAuthType;
import org.ecs.schedule.exception.BaseException;
import org.ecs.schedule.service.auth.CuckooAuthService;
import org.ecs.schedule.vo.auth.CuckooAuthUserVo;
import org.ecs.schedule.vo.auth.CuckooLogonInfo;
import org.ecs.schedule.web.core.NoLoginCheck;
import org.ecs.util.CommonUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@Controller
@RequestMapping("/logon")
public class LogonController extends BaseController {

    @Autowired
    private CuckooAuthService cuckooAuthService;

    @NoLoginCheck
    @RequestMapping("/index")
    public String logon(HttpServletRequest request, String redirectUrl) {
        String requestUri = request.getRequestURI();
        String contextPath = request.getContextPath();
        log.info("#@@# logon: {}, {}", requestUri, contextPath);
        return CuckooAdminPages.LOGIN.getValue();
    }


    @NoLoginCheck
    @RequestMapping("/register")
    @ResponseBody
    public Object register(CuckooAuthUserVo user) {
        if (StringUtils.isEmpty(user.getUserPwd())) {
            throw new BaseException("password should not be null");
        }

        if (null == user.getId()) {
            // 用户新增
            // 用户名是否存在
            cuckooAuthService.isUsernameExist(user.getUserName());

            // 外部注册的默认为普通用户
            CuckooAuthUser cuckooAuthUser = new CuckooAuthUser();
            BeanUtils.copyProperties(user, cuckooAuthUser);
            cuckooAuthUser.setUserAuthType(CuckooUserAuthType.GUEST.getValue());
            cuckooAuthUser.setUserPwd(CommonUtil.md5(user.getUserPwd()));
            cuckooAuthService.addUser(cuckooAuthUser);

        } else {
            //用户修改
            CuckooAuthUser cuckooAuthUser = cuckooAuthService.getUserInfoById(user.getId());
            cuckooAuthUser.setEmail(user.getEmail());
            cuckooAuthUser.setOrgName(user.getOrgName());
            cuckooAuthUser.setPhone(user.getPhone());
            cuckooAuthUser.setUserAuthType(user.getUserAuthType());
            // cuckooAuthUser.setUserName(user.getUserName());
            if (StringUtils.isNotEmpty(user.getUserPwd())) {

                cuckooAuthUser.setUserPwd(CommonUtil.md5(user.getUserPwd()));
            }
            cuckooAuthService.update(cuckooAuthUser);
        }

        return success(CuckooAdminPages.INDEX.getValue());
    }


    @NoLoginCheck
    @RequestMapping("/in")
    @ResponseBody
    public Object login(HttpServletRequest request, String user, String pwd) {
        CuckooLogonInfo logonInfo = null;
        logonInfo = cuckooAuthService.getLogonInfo(user, pwd);
        // 查询数据库
        if (null == logonInfo) {
            throw new BaseException("unknown userName:{},password:{},please try using [guest,]", user, pwd);
        }
        request.getSession().setAttribute(CuckooWebConstant.ADMIN_WEB_SESSION_USER_KEY, logonInfo);
        return success(CuckooAdminPages.INDEX.getValue());
    }


    @NoLoginCheck
    @RequestMapping("/out")
    @ResponseBody
    public Object logout(HttpServletRequest request, String redirectUrl) {
        request.getSession().invalidate();
        request.setAttribute("redirectUrl", redirectUrl);
        if (null != redirectUrl) {
            return success(redirectUrl);
        }
        return success(CuckooAdminPages.INDEX.getValue());
    }

}
