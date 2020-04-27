package org.ecs.schedule.service.net;

import org.ecs.schedule.bean.ClientTaskInfoBean;
import org.ecs.schedule.bean.JobInfoBean;
import org.ecs.schedule.domain.net.CuckooNetClientInfo;
import org.ecs.schedule.domain.net.CuckooNetRegistJob;
import org.ecs.schedule.domain.net.CuckooNetServerInfo;
import org.ecs.schedule.exception.JobCanNotRunningException;
import org.ecs.schedule.exception.JobRunningErrorException;
import org.ecs.schedule.qry.net.JobNetQry;
import org.ecs.schedule.vo.job.CuckooClientJobExecResult;
import org.ecs.util.dao.PageDataList;

import java.net.InetSocketAddress;
import java.util.List;

public interface CuckooNetService {

    /**
     * 调用远程任务
     *
     * @param jobBean
     */
    CuckooClientJobExecResult execRemoteJob(CuckooNetClientInfo cuckooNetClientInfo, JobInfoBean jobBean) throws JobCanNotRunningException, JobRunningErrorException;

    /**
     * 查询可执行远程执行器列表 -- 考虑负载均衡
     *
     * @param jobId
     * @return
     * @throws JobCanNotRunningException
     */
    CuckooNetClientInfo getExecNetClientInfo(Long jobId) throws JobCanNotRunningException;

    /**
     * 新增可执行远程执行器
     *
     * @param taskInfoCuckooClientJobDetail
     */
    Long addRemote(ClientTaskInfoBean taskInfoCuckooClientJobDetail);

    /**
     * 按条件查询注册任务
     *
     * @param qry
     * @return
     */
    PageDataList<CuckooNetRegistJob> pageRegistJob(JobNetQry qry);

    /**
     * 查询某个注册任务的服务器
     *
     * @param job
     * @return
     */
    List<CuckooNetServerInfo> getCuckooServersByRegistJob(CuckooNetRegistJob job);


    /**
     * 查询某个注册任务的执行器信息
     *
     * @param job
     * @return
     */
    List<CuckooNetClientInfo> getCuckooClientsByRegistJob(CuckooNetRegistJob job);

    /**
     * 删除超时服务器、执行器信息（modify_date：心跳检测的时候会更新）
     */
    void removeUselessCuckooNetMessage();

    /**
     * 删除执行器信息
     *
     * @param cuckooNetClientInfo
     */
    void removeNetClient(CuckooNetClientInfo cuckooNetClientInfo);

    /**
     * 删除服务器信息
     */
    void removeNetServer(CuckooNetServerInfo cuckooNetServerInfo);


    void tryUpdateClientStatus(InetSocketAddress clientAddr);


    Long queryLastInsertId();


}
