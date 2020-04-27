package org.ecs.schedule.dao.exec;

import org.ecs.schedule.domain.exec.CuckooJobNextJob;
import org.ecs.schedule.domain.exec.CuckooJobNextJobCriteria;
import org.ecs.util.dao.PageDataList;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface CuckooJobNextJobMapper {
    /**
     * cuckoo_job_next_job数据表的操作方法: countByExample  
     * 
     */
    int countByExample(CuckooJobNextJobCriteria example);

    /**
     * cuckoo_job_next_job数据表的操作方法: deleteByExample  
     * 
     */
    int deleteByExample(CuckooJobNextJobCriteria example);

    /**
     * cuckoo_job_next_job数据表的操作方法: deleteByPrimaryKey  
     * 
     */
    int deleteByPrimaryKey(Long id);

    /**
     * cuckoo_job_next_job数据表的操作方法: insert  
     * 
     */
    int insert(CuckooJobNextJob record);

    /**
     * cuckoo_job_next_job数据表的操作方法: insertSelective  
     * 
     */
    int insertSelective(CuckooJobNextJob record);

    /**
     * cuckoo_job_next_job数据表的操作方法: selectByExample  
     * 
     */
    List<CuckooJobNextJob> selectByExample(CuckooJobNextJobCriteria example);

    /**
     * cuckoo_job_next_job数据表的操作方法: selectByPrimaryKey  
     * 
     */
    CuckooJobNextJob selectByPrimaryKey(Long id);

    /**
     * cuckoo_job_next_job数据表的操作方法: lockByPrimaryKey  
     * 
     */
    CuckooJobNextJob lockByPrimaryKey(Long id);

    /**
     * cuckoo_job_next_job数据表的操作方法: lockByExample  
     * 
     */
    CuckooJobNextJob lockByExample(CuckooJobNextJobCriteria example);

    /**
     * cuckoo_job_next_job数据表的操作方法: pageByExample  
     * 
     */
    PageDataList<CuckooJobNextJob> pageByExample(CuckooJobNextJobCriteria example);

    /**
     * cuckoo_job_next_job数据表的操作方法: lastInsertId  
     * 线程安全的获得当前连接，最近一个自增长主键的值（针对insert操作）
     * 使用last_insert_id()时要注意，当一次插入多条记录时(批量插入)，只是获得第一次插入的id值，务必注意。
     * 
     */
    Long lastInsertId();

    /**
     * cuckoo_job_next_job数据表的操作方法: updateByExampleSelective  
     * 
     */
    int updateByExampleSelective(@Param("record") CuckooJobNextJob record, @Param("example") CuckooJobNextJobCriteria example);

    /**
     * cuckoo_job_next_job数据表的操作方法: updateByExample  
     * 
     */
    int updateByExample(@Param("record") CuckooJobNextJob record, @Param("example") CuckooJobNextJobCriteria example);

    /**
     * cuckoo_job_next_job数据表的操作方法: updateByPrimaryKeySelective  
     * 
     */
    int updateByPrimaryKeySelective(CuckooJobNextJob record);

    /**
     * cuckoo_job_next_job数据表的操作方法: updateByPrimaryKey  
     * 
     */
    int updateByPrimaryKey(CuckooJobNextJob record);
}