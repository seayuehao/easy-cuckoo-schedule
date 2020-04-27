package org.ecs.schedule.service.auth;

import org.ecs.schedule.domain.auth.CuckooAuthUser;
import org.ecs.schedule.qry.auth.AuthUserQry;
import org.ecs.schedule.qry.job.GroupAuthQry;
import org.ecs.schedule.vo.auth.CuckooGroupAuthVo;
import org.ecs.schedule.vo.auth.CuckooLogonInfo;
import org.ecs.util.dao.PageDataList;

public interface CuckooAuthService {

    /**
     * 获得登录用户信息 -- 内部通过ThreadLocal获取
     *
     * @return
     */
    CuckooLogonInfo getLogonInfo();


    /**
     * @param cuckooLogonInfo
     */
    void setLogonInfo(CuckooLogonInfo cuckooLogonInfo);


    /**
     * 清空logon信息 - ThreadLocal
     */
    void clearLogon();


    /**
     * 通过用户名和密码获得登录信息
     *
     * @param userName
     * @param userPwd
     * @return
     */
    CuckooLogonInfo getLogonInfo(String userName, String userPwd);


    /**
     * 用户名是否存在
     *
     * @param userName
     */
    void isUsernameExist(String userName);


    /**
     * 新增用户
     *
     * @param user
     */
    void addUser(CuckooAuthUser user);


    /**
     * 刷新权限
     *
     * @param cuckooLogonInfo
     */
    void refreshAuth(CuckooLogonInfo cuckooLogonInfo);


    /**
     * 新增权限关系
     *
     * @param userId
     * @param groupId
     */
    void addAuthJobgrp(Long userId, Long groupId);

    /**
     * 分页查询分组级别各个用户权限
     *
     * @param qry
     * @return
     */
    PageDataList<CuckooGroupAuthVo> pageGroupAuth(GroupAuthQry qry);


    /**
     * @param type
     * @param authId
     * @param userId
     * @param groupId
     * @return
     */
    void changeAuth(String type, Long authId, Long userId, Long groupId);

    /**
     * 查询登录用户信息
     *
     * @param id
     * @return
     */
    CuckooAuthUser getUserInfoById(Long id);


    /**
     * 修改用户
     *
     * @param cuckooAuthUser
     */
    void update(CuckooAuthUser cuckooAuthUser);


    /**
     * 查询条件
     *
     * @param qry
     * @return
     */
    PageDataList<CuckooAuthUser> pageAuthUser(AuthUserQry qry);


}
