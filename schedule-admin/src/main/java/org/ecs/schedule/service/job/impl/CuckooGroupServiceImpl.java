package org.ecs.schedule.service.job.impl;

import org.ecs.schedule.dao.exec.CuckooJobDetailMapper;
import org.ecs.schedule.dao.exec.CuckooJobGroupMapper;
import org.ecs.schedule.domain.exec.CuckooJobDetailCriteria;
import org.ecs.schedule.domain.exec.CuckooJobGroup;
import org.ecs.schedule.domain.exec.CuckooJobGroupCriteria;
import org.ecs.schedule.enums.CuckooUserAuthType;
import org.ecs.schedule.exception.BaseException;
import org.ecs.schedule.service.auth.CuckooAuthService;
import org.ecs.schedule.service.job.CuckooGroupService;
import org.ecs.schedule.vo.auth.CuckooLogonInfo;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CuckooGroupServiceImpl implements CuckooGroupService {

    @Autowired
    private CuckooJobGroupMapper cuckooJobGroupMapper;

    @Autowired
    private CuckooJobDetailMapper cuckooJobDetailMapper;

    @Autowired
    private CuckooAuthService cuckooAuthService;

    @Override
    @Transactional
    public Long addGroup(CuckooJobGroup cuckooJobGroup) {
        if (null == cuckooJobGroup || StringUtils.isEmpty(cuckooJobGroup.getGroupName())) {
            throw new BaseException("group name should not be null");
        }

        if (CuckooUserAuthType.GUEST.getValue().equals(cuckooAuthService.getLogonInfo().getCuckooUserAuthType().getValue())) {
            throw new BaseException("Guest Users had no right to add group");
        }

        cuckooJobGroup.setId(null);
        cuckooJobGroupMapper.insertSelective(cuckooJobGroup);
        Long groupId = cuckooJobGroupMapper.lastInsertId();

        // 谁新增的，有直接所有权限
        cuckooAuthService.addAuthJobgrp(cuckooAuthService.getLogonInfo().getId(), groupId);
        cuckooAuthService.refreshAuth(cuckooAuthService.getLogonInfo());
        return groupId;
    }

    @Override
    public List<CuckooJobGroup> listAllGroup() {
        CuckooLogonInfo loginInfo = cuckooAuthService.getLogonInfo();
        CuckooJobGroupCriteria crt = new CuckooJobGroupCriteria();

        if (CollectionUtils.isNotEmpty(loginInfo.getReadableGroupIds())) {
            crt.createCriteria().andIdIn(loginInfo.getReadableGroupIds());
        }
        return cuckooJobGroupMapper.selectByExample(crt);
    }

    @Override
    public CuckooJobGroup getGroupById(Long groupId) {
        return cuckooJobGroupMapper.selectByPrimaryKey(groupId);
    }

    @Override
    public void deleteById(Long id) {
        if (!cuckooAuthService.getLogonInfo().getWritableGroupIds().contains(id)) {
            throw new BaseException("not writable right in this group");
        }

        cuckooJobGroupMapper.deleteByPrimaryKey(id);
        CuckooJobDetailCriteria crt = new CuckooJobDetailCriteria();
        crt.createCriteria().andGroupIdEqualTo(id);
        cuckooJobDetailMapper.deleteByExample(crt);
        cuckooAuthService.refreshAuth(cuckooAuthService.getLogonInfo());
    }

    @Override
    public void updateByPk(CuckooJobGroup cuckooJobGroup) {
        if (!cuckooAuthService.getLogonInfo().getWritableGroupIds().contains(cuckooJobGroup.getId())) {
            throw new BaseException("not writable right in this group {}", cuckooJobGroup.getGroupName());
        }
        cuckooAuthService.refreshAuth(cuckooAuthService.getLogonInfo());
        cuckooJobGroupMapper.updateByPrimaryKeySelective(cuckooJobGroup);
    }

}
