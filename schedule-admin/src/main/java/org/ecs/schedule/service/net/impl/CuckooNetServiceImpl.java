package org.ecs.schedule.service.net.impl;

import org.ecs.schedule.bean.ClientTaskInfoBean;
import org.ecs.schedule.bean.JobInfoBean;
import org.ecs.schedule.component.cache.JobClientSessionCache;
import org.ecs.schedule.constant.CuckooJobConstant;
import org.ecs.schedule.constant.CuckooNetConstant;
import org.ecs.schedule.dao.net.CuckooNetClientInfoMapper;
import org.ecs.schedule.dao.net.CuckooNetClientJobMapMapper;
import org.ecs.schedule.dao.net.CuckooNetRegistJobMapper;
import org.ecs.schedule.dao.net.CuckooNetServerInfoMapper;
import org.ecs.schedule.dao.net.CuckooNetServerJobMapMapper;
import org.ecs.schedule.domain.exec.CuckooJobDetail;
import org.ecs.schedule.domain.net.CuckooNetClientInfo;
import org.ecs.schedule.domain.net.CuckooNetClientInfoCriteria;
import org.ecs.schedule.domain.net.CuckooNetClientJobMap;
import org.ecs.schedule.domain.net.CuckooNetClientJobMapCriteria;
import org.ecs.schedule.domain.net.CuckooNetRegistJob;
import org.ecs.schedule.domain.net.CuckooNetRegistJobCriteria;
import org.ecs.schedule.domain.net.CuckooNetServerInfo;
import org.ecs.schedule.domain.net.CuckooNetServerInfoCriteria;
import org.ecs.schedule.domain.net.CuckooNetServerJobMap;
import org.ecs.schedule.domain.net.CuckooNetServerJobMapCriteria;
import org.ecs.schedule.enums.CuckooJobExecType;
import org.ecs.schedule.enums.CuckooMessageType;
import org.ecs.schedule.exception.BaseException;
import org.ecs.schedule.exception.JobCanNotRunningException;
import org.ecs.schedule.exception.JobRunningErrorException;
import org.ecs.schedule.net.server.NettyServerSide;
import org.ecs.schedule.net.vo.IoClientInfo;
import org.ecs.schedule.qry.net.JobNetQry;
import org.ecs.schedule.service.job.CuckooJobService;
import org.ecs.schedule.service.net.CuckooNetService;
import org.ecs.schedule.vo.job.CuckooClientJobExecResult;
import org.ecs.util.dao.PageDataList;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CuckooNetServiceImpl implements CuckooNetService {

    @Autowired
    private CuckooJobService cuckooJobService;

    @Autowired
    private CuckooNetClientInfoMapper cuckooNetClientInfoMapper;

    @Autowired
    private CuckooNetClientJobMapMapper cuckooNetClientJobMapMapper;

    @Autowired
    private CuckooNetRegistJobMapper cuckooNetRegistJobMapper;

    @Autowired
    private CuckooNetServerJobMapMapper cuckooNetServerJobMapMapper;

    @Autowired
    private CuckooNetServerInfoMapper cuckooNetServerInfoMapper;

    @Autowired
    private CuckooNetClientInfoMapper cuckooNetClinetInfoMapper;

    @Autowired
    private NettyServerSide nettyServerSide;

    @Override
    public CuckooClientJobExecResult execRemoteJob(CuckooNetClientInfo cuckooNetClientInfo, JobInfoBean jobBean)
            throws JobCanNotRunningException, JobRunningErrorException {

        CuckooClientJobExecResult result = new CuckooClientJobExecResult();
        result.setRemark("");
        // 根据remoteJobExec 获取socket,
        IoClientInfo socket = JobClientSessionCache.get(cuckooNetClientInfo.getId());
        // 意外情况获取不到socket
        if (socket == null) {
            result.setRemark("JobClientSessionCache can not get socket,netclient id:" + cuckooNetClientInfo.getId());
            throw new JobCanNotRunningException("JobClientSessionCache can not get socket,netclient id:{}",
                    cuckooNetClientInfo.getId());
        }

        // 更新远程服务器最新调用时间
        try {
            // socket写数据,触发客户端任务调度
            log.info("调用远程任务开始,jobApp:{},jobName:{},bean:{}", cuckooNetClientInfo.getIp(),
                    cuckooNetClientInfo.getPort(), jobBean);
            nettyServerSide.send(socket, CuckooMessageType.JOBDOING, jobBean);
            result.setSuccess(true);
            result.setRemark("running");
            return result;
        } catch (Exception e) {
            result.setRemark("error:" + e.getMessage());
            log.error("job exec error:{}", e.getMessage(), e);
            throw new JobRunningErrorException("job exec error:{}", e.getMessage());
        }
    }

    @Override
    public CuckooNetClientInfo getExecNetClientInfo(Long jobId) throws JobCanNotRunningException {
        if (null == CuckooJobConstant.curServerId) {
            // 服务器未准备好（刚刚重启）
            throw new JobCanNotRunningException("server is not ready");
        }

        // 查看任务详细信息
        CuckooJobDetail jobInfo = cuckooJobService.getJobById(jobId);

        if (CuckooJobExecType.CUCKOO.getValue().equals(jobInfo.getExecJobType())) {
            // 查询可执行服务器详细信息，并返回:CUCKOO任务需要校验appName和jobName，兼容升级的时候，存在部分机器有JobName，部分机器没有JobName的情况
            CuckooNetRegistJobCriteria registJobCrt = new CuckooNetRegistJobCriteria();
            registJobCrt.createCriteria().andJobClassApplicationEqualTo(jobInfo.getJobClassApplication())
                    .andJobNameEqualTo(jobInfo.getJobName());
            List<CuckooNetRegistJob> registJobs = cuckooNetRegistJobMapper.selectByExample(registJobCrt);

            if (CollectionUtils.isNotEmpty(registJobs)) {
                CuckooNetRegistJob cuckooNetRegistJob = registJobs.get(0);
                // 查询mapping数据
                CuckooNetClientJobMapCriteria clientMapCrt = new CuckooNetClientJobMapCriteria();
                clientMapCrt.createCriteria().andRegistIdEqualTo(cuckooNetRegistJob.getId());
                List<CuckooNetClientJobMap> clientMaps = cuckooNetClientJobMapMapper.selectByExample(clientMapCrt);
                if (CollectionUtils.isNotEmpty(clientMaps)) {
                    List<Long> clientIds = clientMaps.stream().map(jm -> jm.getClientId()).collect(Collectors.toList());
                    CuckooNetClientInfoCriteria clientCrt = new CuckooNetClientInfoCriteria();
                    clientCrt.createCriteria().andIdIn(clientIds).andServerIdEqualTo(CuckooJobConstant.curServerId);
                    List<CuckooNetClientInfo> clientInfos = cuckooNetClientInfoMapper.selectByExample(clientCrt);
                    if (CollectionUtils.isNotEmpty(clientInfos)) {

                        return clientInfos.get(new Random().nextInt(clientInfos.size()));
                    }
                }
            }

        } else if (CuckooJobExecType.SCRIPT.getValue().equals(jobInfo.getExecJobType())) {
            // 查询可执行服务器详细信息，并返回:SCRIPT任务只需要校验app的存在情况就可以了，script可以提前配置好
            CuckooNetRegistJobCriteria registJobCrt = new CuckooNetRegistJobCriteria();
            registJobCrt.setStart(0);
            registJobCrt.setLimit(1);
            registJobCrt.createCriteria().andJobClassApplicationEqualTo(jobInfo.getJobClassApplication());
            List<CuckooNetRegistJob> registJobs = cuckooNetRegistJobMapper.selectByExample(registJobCrt);

            if (CollectionUtils.isNotEmpty(registJobs)) {
                CuckooNetRegistJob cuckooNetRegistJob = registJobs.get(0);
                // 查询mapping数据
                CuckooNetClientJobMapCriteria clientMapCrt = new CuckooNetClientJobMapCriteria();
                clientMapCrt.createCriteria().andRegistIdEqualTo(cuckooNetRegistJob.getId());
                List<CuckooNetClientJobMap> clientMaps = cuckooNetClientJobMapMapper.selectByExample(clientMapCrt);

                if (CollectionUtils.isNotEmpty(clientMaps)) {
                    CuckooNetClientInfoCriteria clientCrt = new CuckooNetClientInfoCriteria();

                    clientCrt.createCriteria().andIdIn(clientMaps.stream().map(jm -> jm.getClientId()).collect(Collectors.toList())).
                            andServerIdEqualTo(CuckooJobConstant.curServerId);

                    List<CuckooNetClientInfo> clientInfos = cuckooNetClientInfoMapper.selectByExample(clientCrt);
                    if (CollectionUtils.isNotEmpty(clientInfos)) {
                        return clientInfos.get(new Random().nextInt(clientInfos.size()));
                    }
                }
            }
        }

        return null;
    }

    @Override
    @Transactional
    public Long addRemote(ClientTaskInfoBean clientTaskInfoBean) {

        if (StringUtils.isEmpty(clientTaskInfoBean.getAppName())
                || StringUtils.isEmpty(clientTaskInfoBean.getTaskName())) {
            throw new BaseException("clientTaskInfo error,AppName:{},TaskName:{}", clientTaskInfoBean.getAppName(),
                    clientTaskInfoBean.getTaskName());
        }

        // 判断是否增加 job_regist,Lock的方式
        CuckooNetRegistJobCriteria crtRegistJobLock = new CuckooNetRegistJobCriteria();
        crtRegistJobLock.createCriteria().andJobClassApplicationEqualTo(clientTaskInfoBean.getAppName())
                .andJobNameEqualTo(clientTaskInfoBean.getTaskName());
        CuckooNetRegistJob cuckooNetRegistJob = cuckooNetRegistJobMapper.lockByExample(crtRegistJobLock);
        if (null == cuckooNetRegistJob) {
            // 新增一条数据，并且加锁
            cuckooNetRegistJob = new CuckooNetRegistJob();
            cuckooNetRegistJob.setJobClassApplication(clientTaskInfoBean.getAppName());
            cuckooNetRegistJob.setJobName(clientTaskInfoBean.getTaskName());
            cuckooNetRegistJob.setBeanName(clientTaskInfoBean.getBeanName());
            cuckooNetRegistJob.setMethodName(clientTaskInfoBean.getMethodName());
            cuckooNetRegistJob.setCreateDate(System.currentTimeMillis());
            cuckooNetRegistJob.setModifyDate(System.currentTimeMillis());
            try {
                cuckooNetRegistJobMapper.insertSelective(cuckooNetRegistJob);
                cuckooNetRegistJob.setId(cuckooNetRegistJobMapper.lastInsertId());
                cuckooNetRegistJob = cuckooNetRegistJobMapper.lockByPrimaryKey(cuckooNetRegistJob.getId());
            } catch (Exception e) {
                // 并发情况下，抛出异常，表示前面加锁失败了，后面再锁一次，忽略此处错误
                log.error("concurrent insert cuckooNetRegistJob error,appName:{},jobName:{}",
                        clientTaskInfoBean.getAppName(), clientTaskInfoBean.getTaskName(), e);
                cuckooNetRegistJob = cuckooNetRegistJobMapper.lockByExample(crtRegistJobLock);
            }

        }

        // 增加 server_job关系
        InetSocketAddress serverSocket = null;
        // 是否存在server

        CuckooNetServerInfoCriteria serverCrt = new CuckooNetServerInfoCriteria();
        serverCrt.createCriteria().andIpEqualTo(serverSocket.getAddress().getHostAddress())
                .andPortEqualTo(serverSocket.getPort());
        CuckooNetServerInfo cuckooNetServerInfo = cuckooNetServerInfoMapper.lockByExample(serverCrt);

        if (null == cuckooNetServerInfo) {
            // 不存在，新增服务器
            cuckooNetServerInfo = new CuckooNetServerInfo();
            cuckooNetServerInfo.setIp(serverSocket.getAddress().getHostAddress());
            cuckooNetServerInfo.setPort(serverSocket.getPort());
            cuckooNetServerInfo.setModifyDate(System.currentTimeMillis());
            try {
                cuckooNetServerInfoMapper.insertSelective(cuckooNetServerInfo);
                cuckooNetServerInfo.setId(cuckooNetServerInfoMapper.lastInsertId());
                cuckooNetServerInfo = cuckooNetServerInfoMapper.lockByPrimaryKey(cuckooNetServerInfo.getId());
            } catch (Exception e) {
                // 并发情况下，抛出异常，表示前面加锁失败了，后面再锁一次，忽略此处错误
                log.error("concurrent insert cuckooNetServerInfo error,ip:{},port:{}",
                        serverSocket.getAddress().getHostAddress(), serverSocket.getPort(), e);
                cuckooNetServerInfo = cuckooNetServerInfoMapper.lockByExample(serverCrt);
            }
        }

        // 增加registjob - server关联关系
        CuckooNetServerJobMap cuckooNetServerJobMap = new CuckooNetServerJobMap();
        cuckooNetServerJobMap.setRegistId(cuckooNetRegistJob.getId());
        cuckooNetServerJobMap.setServerId(cuckooNetServerInfo.getId());
        try {
            cuckooNetServerJobMapMapper.insertSelective(cuckooNetServerJobMap);
        } catch (Exception e) {
            // 唯一索引，无法插入的情况(控制好的话理论上不会出现，出现不影响)
            log.error("mapping serverinfo-registjob error,registJobId:{},serverInfoId:{} ",
                    cuckooNetRegistJob.getId(), cuckooNetServerInfo.getId());
        }

        // 增加client_info
        InetSocketAddress clientSocket = null;
        // log.info("客户端IP："+ clientSocket.getAddress().getHostAddress());
        // log.info("客户端Port："+ clientSocket.getPort());

        CuckooNetClientInfoCriteria clientCrt = new CuckooNetClientInfoCriteria();
        clientCrt.createCriteria().andIpEqualTo(clientSocket.getAddress().getHostAddress())
                .andServerIdEqualTo(cuckooNetServerInfo.getId())
                .andPortEqualTo(clientSocket.getPort());
        CuckooNetClientInfo cuckooNetClientInfo = cuckooNetClientInfoMapper.lockByExample(clientCrt);
        if (null == cuckooNetClientInfo) {
            // 不存在改客户端信息，新增客户端
            cuckooNetClientInfo = new CuckooNetClientInfo();
            cuckooNetClientInfo.setIp(clientSocket.getAddress().getHostAddress());
            cuckooNetClientInfo.setPort(clientSocket.getPort());
            cuckooNetClientInfo.setModifyDate(System.currentTimeMillis());
            cuckooNetClientInfo.setServerId(cuckooNetServerInfo.getId());
            cuckooNetClientInfo.setClientTag(clientTaskInfoBean.getClientTag());
            try {
                cuckooNetClientInfoMapper.insertSelective(cuckooNetClientInfo);
                cuckooNetClientInfo.setId(cuckooNetClientInfoMapper.lastInsertId());
                cuckooNetClientInfo = cuckooNetClientInfoMapper.lockByPrimaryKey(cuckooNetClientInfo.getId());
            } catch (Exception e) {
                // 并发情况下，抛出异常，表示前面加锁失败了，后面再锁一次，忽略此处错误
                log.error("concurrent insert cuckooNetServerInfo error,ip:{},port:{}",
                        serverSocket.getAddress().getHostAddress(), serverSocket.getPort(), e);
                cuckooNetClientInfo = cuckooNetClientInfoMapper.lockByExample(clientCrt);
            }
        }

        // 增加client - job
        // 的关系（同时维护一份内存与session的关系：一个client对应一个session，一个client对应多个regist）
        CuckooNetClientJobMap cuckooNetClientJobMap = new CuckooNetClientJobMap();
        cuckooNetClientJobMap.setClientId(cuckooNetClientInfo.getId());
        cuckooNetClientJobMap.setRegistId(cuckooNetRegistJob.getId());
        try {
            cuckooNetClientJobMapMapper.insertSelective(cuckooNetClientJobMap);
        } catch (Exception e1) {
            // 唯一索引，无法插入的情况(控制好的话理论上不会出现，出现不影响)
            log.error("mapping clientInfo-registjob error,registJobId:{},clientInfoId:{} ",
                    cuckooNetRegistJob.getId(), cuckooNetClientInfo.getId());
        }


        // 链接缓存中增加缓存
        IoClientInfo socket = new IoClientInfo();
        socket.setIp(cuckooNetClientInfo.getIp());
        socket.setPort(cuckooNetClientInfo.getPort());
        socket.setServerId(cuckooNetClientInfo.getServerId());
        CuckooJobConstant.curServerId = cuckooNetClientInfo.getServerId();

        JobClientSessionCache.put(cuckooNetClientInfo.getId(), socket);
        log.info("successfully add client job ,clientTaskInfoBean:{}", clientTaskInfoBean);
        return cuckooNetClientInfo.getId();
    }

    @Override
    public PageDataList<CuckooNetRegistJob> pageRegistJob(JobNetQry qry) {
        CuckooNetRegistJobCriteria registCrt = new CuckooNetRegistJobCriteria();
        registCrt.setStart(qry.getStart());
        registCrt.setLimit(qry.getLimit());
        registCrt.setOrderByClause("id desc");
        CuckooNetRegistJobCriteria.Criteria crt = registCrt.createCriteria();

        if (StringUtils.isNotEmpty(qry.getJobClassApplication())) {
            crt.andJobClassApplicationEqualTo(qry.getJobClassApplication());
        }

        if (StringUtils.isNotEmpty(qry.getJobName())) {
            crt.andJobNameLike("%" + qry.getJobName() + "%");
        }

        return cuckooNetRegistJobMapper.pageByExample(registCrt);
    }

    @Override
    public List<CuckooNetServerInfo> getCuckooServersByRegistJob(CuckooNetRegistJob job) {
        CuckooNetServerJobMapCriteria mapCrt = new CuckooNetServerJobMapCriteria();
        mapCrt.createCriteria().andRegistIdEqualTo(job.getId());
        List<CuckooNetServerJobMap> maps = cuckooNetServerJobMapMapper.selectByExample(mapCrt);
        if (CollectionUtils.isEmpty(maps)) return Collections.EMPTY_LIST;

        List<Long> serverIds = maps.stream().map(jm -> jm.getServerId()).collect(Collectors.toList());

        CuckooNetServerInfoCriteria serCrt = new CuckooNetServerInfoCriteria();
        serCrt.createCriteria().andIdIn(serverIds).andModifyDateGreaterThan(
                System.currentTimeMillis() - CuckooNetConstant.CUCKOO_NET_SERVER_OVERTIME);
        return cuckooNetServerInfoMapper.selectByExample(serCrt);
    }


    @Override
    public List<CuckooNetClientInfo> getCuckooClientsByRegistJob(CuckooNetRegistJob job) {
        CuckooNetClientJobMapCriteria mapCrt = new CuckooNetClientJobMapCriteria();
        mapCrt.createCriteria().andRegistIdEqualTo(job.getId());
        List<CuckooNetClientJobMap> maps = cuckooNetClientJobMapMapper.selectByExample(mapCrt);
        if (CollectionUtils.isEmpty(maps)) return Collections.EMPTY_LIST;

        List<Long> clientIds = maps.stream().map(jm -> jm.getClientId()).collect(Collectors.toList());
        CuckooNetClientInfoCriteria cliCrt = new CuckooNetClientInfoCriteria();
        cliCrt.createCriteria().andIdIn(clientIds).andModifyDateGreaterThan(
                System.currentTimeMillis() - CuckooNetConstant.CUCKOO_NET_CLIENT_OVERTIME);

        return cuckooNetClientInfoMapper.selectByExample(cliCrt);
    }

    @Override
    public void removeUselessCuckooNetMessage() {
        CuckooNetClientInfoCriteria cliCrt = new CuckooNetClientInfoCriteria();
        cliCrt.createCriteria().andModifyDateLessThanOrEqualTo(
                System.currentTimeMillis() - CuckooNetConstant.CUCKOO_NET_CLIENT_OVERTIME);

        List<CuckooNetClientInfo> clients = cuckooNetClientInfoMapper.selectByExample(cliCrt);
        if (CollectionUtils.isNotEmpty(clients)) {
            for (CuckooNetClientInfo cuckooNetClientInfo : clients) {
                removeNetClient(cuckooNetClientInfo);
            }
        }

        CuckooNetServerInfoCriteria serCrt = new CuckooNetServerInfoCriteria();
        serCrt.createCriteria().andModifyDateLessThanOrEqualTo(
                System.currentTimeMillis() - CuckooNetConstant.CUCKOO_NET_SERVER_OVERTIME);

        List<CuckooNetServerInfo> servers = cuckooNetServerInfoMapper.selectByExample(serCrt);
        if (CollectionUtils.isNotEmpty(servers)) {
            for (CuckooNetServerInfo cuckooNetServerInfo : servers) {

                removeNetServer(cuckooNetServerInfo);
            }
        }
    }

    @Override
    public void removeNetClient(CuckooNetClientInfo cuckooNetClientInfo) {
        cuckooNetClientInfoMapper.deleteByPrimaryKey(cuckooNetClientInfo.getId());
        // 删除client-job关联关系
        CuckooNetClientJobMapCriteria clientMapCrt = new CuckooNetClientJobMapCriteria();
        clientMapCrt.createCriteria().andClientIdEqualTo(cuckooNetClientInfo.getId());
        List<CuckooNetClientJobMap> clientJobMaps = cuckooNetClientJobMapMapper.selectByExample(clientMapCrt);

        if (CollectionUtils.isNotEmpty(clientJobMaps)) {
            cuckooNetClientJobMapMapper.deleteByExample(clientMapCrt);
            for (CuckooNetClientJobMap cuckooNetClientJobMap : clientJobMaps) {
                // 如果一个job没有一个client关联，那么把这个任务也删除掉
                CuckooNetClientJobMapCriteria clientDelMapcrt = new CuckooNetClientJobMapCriteria();
                clientDelMapcrt.createCriteria().andRegistIdEqualTo(cuckooNetClientJobMap.getRegistId());
                List<CuckooNetClientJobMap> clientDelMaps = cuckooNetClientJobMapMapper
                        .selectByExample(clientDelMapcrt);

                if (CollectionUtils.isEmpty(clientDelMaps)) {
                    cuckooNetRegistJobMapper.deleteByPrimaryKey(cuckooNetClientJobMap.getRegistId());
                    // 如果registjob都删除了，那么server-job关联关系也可以删除
                    CuckooNetServerJobMapCriteria serverJobMapCrt = new CuckooNetServerJobMapCriteria();
                    serverJobMapCrt.createCriteria().andRegistIdEqualTo(cuckooNetClientJobMap.getRegistId());
                    cuckooNetServerJobMapMapper.deleteByExample(serverJobMapCrt);
                }
            }
        }

        // 连接缓存中删除缓存
        JobClientSessionCache.remove(cuckooNetClientInfo.getId());
    }


    @Override
    public void removeNetServer(CuckooNetServerInfo cuckooNetServerInfo) {
        // 删除server-job关联关系
        CuckooNetServerJobMapCriteria serverMapCrt = new CuckooNetServerJobMapCriteria();
        serverMapCrt.createCriteria().andServerIdEqualTo(cuckooNetServerInfo.getId());
        List<CuckooNetServerJobMap> serverJobMaps = cuckooNetServerJobMapMapper.selectByExample(serverMapCrt);

        if (CollectionUtils.isEmpty(serverJobMaps)) return;

        cuckooNetServerJobMapMapper.deleteByExample(serverMapCrt);

        for (CuckooNetServerJobMap cuckooNetServerJobMap : serverJobMaps) {
            // 如果一个job没有一个server关联，那么把这个任务也删除掉
            CuckooNetServerJobMapCriteria serverDelMapcrt = new CuckooNetServerJobMapCriteria();
            serverDelMapcrt.createCriteria().andRegistIdEqualTo(cuckooNetServerJobMap.getRegistId());
            List<CuckooNetServerJobMap> serverDelMaps = cuckooNetServerJobMapMapper
                    .selectByExample(serverDelMapcrt);

            if (CollectionUtils.isEmpty(serverDelMaps)) {
                cuckooNetRegistJobMapper.deleteByPrimaryKey(cuckooNetServerJobMap.getRegistId());
            }
        }
    }

    @Transactional
    @Override
    public void tryUpdateClientStatus(InetSocketAddress clientAddr) {
        CuckooNetClientInfoCriteria clientCrt = new CuckooNetClientInfoCriteria();
        clientCrt.createCriteria().andIpEqualTo(clientAddr.getAddress().getHostAddress())
                .andPortEqualTo(clientAddr.getPort());

        CuckooNetClientInfo updateClientInfo = new CuckooNetClientInfo();
        updateClientInfo.setModifyDate(System.currentTimeMillis());
        updateClientInfo.setIp(clientAddr.getAddress().getHostAddress());
        updateClientInfo.setPort(clientAddr.getPort());

        int num = cuckooNetClinetInfoMapper.updateByExampleSelective(updateClientInfo, clientCrt);

        if (num <= 0) {
            updateClientInfo.setServerId(1L); // ?
            cuckooNetClinetInfoMapper.insert(updateClientInfo);
            IoClientInfo socket = new IoClientInfo();
            socket.setIp(clientAddr.getAddress().getHostAddress());
            socket.setPort(clientAddr.getPort());
            JobClientSessionCache.put(queryLastInsertId(), socket);
        }
    }

    @Override
    public Long queryLastInsertId() {
        return cuckooNetClinetInfoMapper.lastInsertId();
    }

}
