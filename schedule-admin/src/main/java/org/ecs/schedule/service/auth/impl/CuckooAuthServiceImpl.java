package org.ecs.schedule.service.auth.impl;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.ecs.schedule.dao.auth.CuckooAuthJobgrpMapper;
import org.ecs.schedule.dao.auth.CuckooAuthJobgrpSubMapper;
import org.ecs.schedule.dao.auth.CuckooAuthUserMapper;
import org.ecs.schedule.domain.auth.CuckooAuthJobgrp;
import org.ecs.schedule.domain.auth.CuckooAuthJobgrpCriteria;
import org.ecs.schedule.domain.auth.CuckooAuthUser;
import org.ecs.schedule.domain.auth.CuckooAuthUserCriteria;
import org.ecs.schedule.domain.exec.CuckooJobGroup;
import org.ecs.schedule.enums.CuckooBooleanFlag;
import org.ecs.schedule.enums.CuckooUserAuthType;
import org.ecs.schedule.exception.BaseException;
import org.ecs.schedule.qry.auth.AuthUserQry;
import org.ecs.schedule.qry.job.GroupAuthQry;
import org.ecs.schedule.service.auth.CuckooAuthService;
import org.ecs.schedule.service.job.CuckooGroupService;
import org.ecs.schedule.vo.auth.CuckooGroupAuthVo;
import org.ecs.schedule.vo.auth.CuckooLogonInfo;
import org.ecs.util.CommonUtil;
import org.ecs.util.dao.PageDataList;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CuckooAuthServiceImpl implements CuckooAuthService {

    private ThreadLocal<CuckooLogonInfo> logonInfo = new ThreadLocal<>();

    @Autowired
    private CuckooAuthUserMapper cuckooAuthUserMapper;

    @Autowired
    private CuckooAuthJobgrpMapper cuckooAuthJobgrpMapper;

    @Autowired
    private CuckooGroupService cuckooGroupService;

    @Autowired
    private CuckooAuthJobgrpSubMapper cuckooAuthJobgrpSubMapper;

    @Override
    public CuckooLogonInfo getLogonInfo() {
        return logonInfo.get() == null ? new CuckooLogonInfo() : logonInfo.get();
    }

    @Override
    public void setLogonInfo(CuckooLogonInfo cuckooLogonInfo) {
        logonInfo.set(cuckooLogonInfo);
    }

    @Override
    public void clearLogon() {
        logonInfo.remove();
    }

    @Override
    public CuckooLogonInfo getLogonInfo(String userName, String userPwd) {
        CuckooLogonInfo cuckooLogonInfo = new CuckooLogonInfo();

        CuckooAuthUserCriteria userCriteria = new CuckooAuthUserCriteria();
        userCriteria.createCriteria().andUserNameEqualTo(userName);
        List<CuckooAuthUser> users = cuckooAuthUserMapper.selectByExample(userCriteria);
        if (CollectionUtils.isEmpty(users)) {
            throw new BaseException("user:{} is not exist", userName);
        }
        CuckooAuthUser user = users.get(0);
        if (!user.getUserPwd().equals(CommonUtil.md5(userPwd))) {
            throw new BaseException("user:{} password error", userName);
        }

        BeanUtils.copyProperties(user, cuckooLogonInfo);
        cuckooLogonInfo.setCuckooUserAuthType(CuckooUserAuthType.fromName(user.getUserAuthType()));
        refreshAuth(cuckooLogonInfo);
        logonInfo.set(cuckooLogonInfo);
        return cuckooLogonInfo;
    }

    @Override
    public void refreshAuth(CuckooLogonInfo cuckooLogonInfo) {
        List<Long> readableGroupIds = new ArrayList<>();
        List<Long> writableGroupIds = new ArrayList<>();
        List<Long> grantableGroupIds = new ArrayList<>();

        cuckooLogonInfo.setReadableGroupIds(readableGroupIds);
        cuckooLogonInfo.setWritableGroupIds(writableGroupIds);
        cuckooLogonInfo.setGrantableGroupIds(grantableGroupIds);

        // 获得各个权限的list
        if (CuckooUserAuthType.ADMIN.getValue().equals(cuckooLogonInfo.getCuckooUserAuthType().getValue())) {
            // 管理员  拥有所有读、写、赋权权限
            List<CuckooJobGroup> groups = cuckooGroupService.listAllGroup();
            List<Long> groupIds = groups.stream().map(g -> g.getId()).collect(Collectors.toList());
            readableGroupIds.addAll(groupIds);
            writableGroupIds.addAll(groupIds);
            grantableGroupIds.addAll(groupIds);
        } else if (CuckooUserAuthType.GUEST.getValue().equals(cuckooLogonInfo.getCuckooUserAuthType().getValue())) {
            // 游客  拥有说有读权限
            List<CuckooJobGroup> groups = cuckooGroupService.listAllGroup();
            List<Long> groupIds = groups.stream().map(g -> g.getId()).collect(Collectors.toList());
            readableGroupIds.addAll(groupIds);
        } else if (CuckooUserAuthType.NORMAL.getValue().equals(cuckooLogonInfo.getCuckooUserAuthType().getValue())) {
            // 普通用户 按照group配置进行赋权
            CuckooAuthJobgrpCriteria crt = new CuckooAuthJobgrpCriteria();
            crt.createCriteria().andUserIdEqualTo(cuckooLogonInfo.getId());
            List<CuckooAuthJobgrp> auths = cuckooAuthJobgrpMapper.selectByExample(crt);

            if (CollectionUtils.isNotEmpty(auths)) {

                for (CuckooAuthJobgrp cuckooAuthJobgrp : auths) {
                    if (CuckooBooleanFlag.YES.getValue().equals(cuckooAuthJobgrp.getReadable())) {
                        readableGroupIds.add(cuckooAuthJobgrp.getGroupId());
                    }
                    if (CuckooBooleanFlag.YES.getValue().equals(cuckooAuthJobgrp.getWritable())) {
                        writableGroupIds.add(cuckooAuthJobgrp.getGroupId());
                    }
                    if (CuckooBooleanFlag.YES.getValue().equals(cuckooAuthJobgrp.getGrantable())) {
                        grantableGroupIds.add(cuckooAuthJobgrp.getGroupId());
                    }
                }
            }
        } else {
            throw new BaseException("unknown auth type:{}", cuckooLogonInfo.getCuckooUserAuthType());
        }

        // 赋值一个不存在的ID，便于后续权限管理，if arr is null,select all,but arr is not null and not exist ,select none
        readableGroupIds.add(-1L);
        writableGroupIds.add(-1L);
        grantableGroupIds.add(-1L);

        cuckooLogonInfo.setReadableGroupIds(readableGroupIds);
        cuckooLogonInfo.setWritableGroupIds(writableGroupIds);
        cuckooLogonInfo.setGrantableGroupIds(grantableGroupIds);
    }

    @Override
    public void isUsernameExist(String userName) {
        CuckooAuthUserCriteria crt = new CuckooAuthUserCriteria();
        crt.createCriteria().andUserNameEqualTo(userName);

        List<CuckooAuthUser> list = cuckooAuthUserMapper.selectByExample(crt);
        if (CollectionUtils.isNotEmpty(list)) {
            throw new BaseException("{} aready exist,please change another one", userName);
        }
    }

    @Override
    public void addUser(CuckooAuthUser user) {
        cuckooAuthUserMapper.insertSelective(user);
    }

    @Override
    public void addAuthJobgrp(Long userId, Long groupId) {
        CuckooAuthJobgrp cuckooAuthJobgrp = new CuckooAuthJobgrp();
        cuckooAuthJobgrp.setGroupId(groupId);
        cuckooAuthJobgrp.setUserId(userId);
        cuckooAuthJobgrp.setGrantable(CuckooBooleanFlag.YES.getValue());
        cuckooAuthJobgrp.setReadable(CuckooBooleanFlag.YES.getValue());
        cuckooAuthJobgrp.setWritable(CuckooBooleanFlag.YES.getValue());
        cuckooAuthJobgrpMapper.insertSelective(cuckooAuthJobgrp);
    }

    @Override
    public PageDataList<CuckooGroupAuthVo> pageGroupAuth(GroupAuthQry qry) {
        return cuckooAuthJobgrpSubMapper.pageByExample(qry);
    }

    @Override
    public void changeAuth(String type, Long authId, Long userId, Long groupId) {

        if (CuckooUserAuthType.GUEST.getValue().equals(getLogonInfo().getCuckooUserAuthType().getValue())) {
            throw new BaseException("guest user have no right to modify auth");
        }

        CuckooAuthUser authUser = cuckooAuthUserMapper.selectByPrimaryKey(userId);
        if (null == authUser) {
            throw new BaseException("can not get user by id:{}", userId);
        }

        CuckooJobGroup jobGroup = cuckooGroupService.getGroupById(groupId);
        if (null == jobGroup) {
            throw new BaseException("can not get user by id:{}", groupId);
        }

        final String writable = "writable";
        final String readable = "readable";
        final String grantable = "grantable";

        if (!CuckooUserAuthType.NORMAL.getValue().equals(authUser.getUserAuthType())) {
            throw new BaseException("user type:{} have this operate right", authUser.getUserAuthType());
        }
        if (!getLogonInfo().getGrantableGroupIds().contains(groupId)) {
            throw new BaseException("you have no grant operate right in group:{}", groupId);
        }
        if (null != authId) {
            // 更新
            CuckooAuthJobgrp authGrp = cuckooAuthJobgrpMapper.selectByPrimaryKey(authId);
            authGrp.setId(authId);
            if (writable.equals(type)) {
                authGrp.setWritable(authGrp.getWritable().equals(CuckooBooleanFlag.YES.getValue()) ? CuckooBooleanFlag.NO.getValue() : CuckooBooleanFlag.YES.getValue());
            }
            if (readable.equals(type)) {
                authGrp.setReadable(authGrp.getReadable().equals(CuckooBooleanFlag.YES.getValue()) ? CuckooBooleanFlag.NO.getValue() : CuckooBooleanFlag.YES.getValue());
            }
            if (grantable.equals(type)) {
                authGrp.setGrantable(authGrp.getGrantable().equals(CuckooBooleanFlag.YES.getValue()) ? CuckooBooleanFlag.NO.getValue() : CuckooBooleanFlag.YES.getValue());
            }
            cuckooAuthJobgrpMapper.updateByPrimaryKeySelective(authGrp);
        } else {
            // 新增
            CuckooAuthJobgrp authGrp = new CuckooAuthJobgrp();
            authGrp.setGroupId(groupId);
            authGrp.setUserId(userId);

            if (writable.equals(type)) {
                authGrp.setGrantable(CuckooBooleanFlag.NO.getValue());
                authGrp.setReadable(CuckooBooleanFlag.YES.getValue());
                authGrp.setWritable(CuckooBooleanFlag.YES.getValue());
            }
            if (readable.equals(type)) {
                authGrp.setGrantable(CuckooBooleanFlag.NO.getValue());
                authGrp.setReadable(CuckooBooleanFlag.YES.getValue());
                authGrp.setWritable(CuckooBooleanFlag.NO.getValue());
            }
            if (grantable.equals(type)) {

                authGrp.setGrantable(CuckooBooleanFlag.YES.getValue());
                authGrp.setReadable(CuckooBooleanFlag.YES.getValue());
                authGrp.setWritable(CuckooBooleanFlag.NO.getValue());
            }
            cuckooAuthJobgrpMapper.insertSelective(authGrp);
        }
    }

    @Override
    public CuckooAuthUser getUserInfoById(Long id) {
        CuckooAuthUser auth = cuckooAuthUserMapper.selectByPrimaryKey(id);
        auth.setUserPwd("");
        return auth;
    }

    @Override
    public void update(CuckooAuthUser cuckooAuthUser) {
        cuckooAuthUserMapper.updateByPrimaryKeySelective(cuckooAuthUser);
    }

    @Override
    public PageDataList<CuckooAuthUser> pageAuthUser(AuthUserQry qry) {
        CuckooAuthUserCriteria crt = new CuckooAuthUserCriteria();
        if (StringUtils.isNotEmpty(qry.getUserAuthType())) {

            crt.createCriteria().andUserAuthTypeEqualTo(qry.getUserAuthType());
        }
        crt.setStart(qry.getStart());
        crt.setLimit(qry.getLimit());

        return cuckooAuthUserMapper.pageByExample(crt);
    }

}
