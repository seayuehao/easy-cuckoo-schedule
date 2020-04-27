package org.ecs.schedule.service.job;

import org.ecs.schedule.domain.exec.CuckooJobGroup;

import java.util.List;

public interface CuckooGroupService {

    /**
     * 新增分组，返回新增分组自增长主键
     *
     * @param cuckooJobGroup
     */
    Long addGroup(CuckooJobGroup cuckooJobGroup);

    /**
     * 查询所有分组信息
     *
     * @return
     */
    List<CuckooJobGroup> listAllGroup();

    /**
     * 根据分组ID查询分组
     *
     * @param groupId
     * @return
     */
    CuckooJobGroup getGroupById(Long groupId);

    /**
     * 删除分组，还要删除分组下的任务
     *
     * @param id
     */
    void deleteById(Long id);

    /**
     * @param cuckooJobGroup
     */
    void updateByPk(CuckooJobGroup cuckooJobGroup);

}
