package org.ecs.schedule.dao.auth;

import org.ecs.schedule.qry.job.GroupAuthQry;
import org.ecs.schedule.vo.auth.CuckooGroupAuthVo;
import org.ecs.util.dao.PageDataList;

public interface CuckooAuthJobgrpSubMapper {
    

   /**
    * CuckooAuthJobgrp分页查询
    * 
    */
   PageDataList<CuckooGroupAuthVo> pageByExample(GroupAuthQry qry);

  
}