package org.ecs.schedule.net.client.handle;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import org.ecs.schedule.bean.JobInfoBean;
import org.ecs.schedule.bean.MessageInfo;
import org.ecs.schedule.constant.CuckooNetConstant;
import org.ecs.schedule.enums.CuckooJobExecType;
import org.ecs.schedule.enums.CuckooMessageType;
import org.ecs.schedule.exception.BaseException;
import org.ecs.schedule.executor.CuckooExecutor;
import org.ecs.schedule.executor.ScriptExecutor;
import org.ecs.schedule.executor.framerwork.bean.CuckooTaskBean;
import org.ecs.schedule.executor.framerwork.cache.CuckooTaskCache;
import org.ecs.schedule.net.client.NettyClientSide;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public class NettyClientHandler extends SimpleChannelInboundHandler<Object> {

    private static final Gson GSON = new GsonBuilder().create();

    private static final ExecutorService EXECUTOR_SERVICE = Executors.newFixedThreadPool(4);


    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Object message) throws Exception {
        log.info("#@>>> response_from_svr: {}", message);
        channelHandlerContext.channel().attr(AttributeKey.valueOf(CuckooNetConstant.ATTR_KEY)).set(message);

        String content = message.toString();
        if (CuckooMessageType.HEARTBEATSERVER.getValue().equals(content)) {
            // 收到心跳包
            log.info("heart_beat_message received!");
            channelHandlerContext.channel().writeAndFlush(CuckooMessageType.HEARTBEATCLIENT.getValue());
            return;
        }

        log.info("cuckoo client receive a message is :{}", content);
        MessageInfo msgInfo = null;

        try {
            msgInfo = GSON.fromJson(content, MessageInfo.class);
        } catch (Exception e) {
            log.error("gson fromJson error, json:{}, error:{}", content, e.getMessage());
        }

        if (msgInfo == null) {
            log.error("unknown message type: {}", message);
            return;
        }

        if (CuckooMessageType.JOBDOING.equals(msgInfo.getMessageType())) {

            final JobInfoBean jobInfo = GSON.fromJson(GSON.toJson(msgInfo.getMessage()), JobInfoBean.class);

            if (null == jobInfo || null == jobInfo.getExecType() || null == jobInfo.getJobName()) {
                log.error("jobInfo args error,jobInfo:{}", jobInfo);
                jobInfo.setErrMessage("args error,jobInfo:" + jobInfo);

                throw new BaseException("jobInfo args error,jobInfo:{}", jobInfo);
            }

            if (CuckooJobExecType.CUCKOO.getValue().equals(jobInfo.getExecType().getValue())) {
                final CuckooTaskBean task = CuckooTaskCache.get(jobInfo.getJobName());
                if (null == task) {
                    log.warn("can not find job by job name:{}, jobInfo:{}", jobInfo.getJobName(), jobInfo);
                    throw new BaseException("can not find job by job name:{}, jobInfo:{}", jobInfo.getJobName(), jobInfo);
                }
                EXECUTOR_SERVICE.submit(() -> CuckooExecutor.exec(task, jobInfo));

            } else if (CuckooJobExecType.SCRIPT.getValue().equals(jobInfo.getExecType().getValue())) {

                EXECUTOR_SERVICE.submit(() -> ScriptExecutor.exec(jobInfo));
            } else {

                log.error("unknown jobexec type:{},jobInfo:{}", jobInfo.getExecType(), jobInfo);
                jobInfo.setErrMessage("unknown jobexec type:" + jobInfo.getExecType());
                NettyClientSide.send(CuckooMessageType.JOBFAILED, jobInfo);
                throw new BaseException("unknown jobexec type:{},jobInfo:{}", jobInfo.getExecType(), jobInfo);
            }
        }
    }

}
