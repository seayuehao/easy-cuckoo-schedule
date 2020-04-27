package org.ecs.schedule.net.server.handle;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;
import org.ecs.schedule.bean.ClientTaskInfoBean;
import org.ecs.schedule.bean.JobInfoBean;
import org.ecs.schedule.bean.MessageInfo;
import org.ecs.schedule.component.cuckoo.CuckooJobCallBack;
import org.ecs.schedule.enums.CuckooMessageType;
import org.ecs.schedule.service.net.CuckooNetService;
import org.springframework.context.ApplicationContext;

import java.net.InetSocketAddress;

@Slf4j
//public class NettyServerMsgHandler extends SimpleChannelInboundHandler<Object>{
public class NettyServerMsgHandler extends ChannelInboundHandlerAdapter {

    private static final Gson gson = new GsonBuilder().create();

//    // Return a unreleasable view on the given ByteBuf
//    // which will just ignore release and retain calls.
//    private static final ByteBuf HEARTBEAT_SEQUENCE = Unpooled
//            .unreleasableBuffer(Unpooled.copiedBuffer(CuckooMessageType.HEARTBEATSERVER.getValue(), StandardCharsets.UTF_8));

    private CuckooNetService cuckooServerService;

    private CuckooJobCallBack cuckooJobCallBack;

    public NettyServerMsgHandler(ApplicationContext applicationContext) {
        if (null != applicationContext) {
            cuckooServerService = applicationContext.getBean(CuckooNetService.class);
            cuckooJobCallBack = applicationContext.getBean(CuckooJobCallBack.class);
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object message) {
        log.info("server receive message: " + message);
        String strMsg = message.toString();

        // 心跳信息
        if (CuckooMessageType.HEARTBEATCLIENT.getValue().equals(strMsg)) {

            // 打印客户端传来的消息内容
            log.info("Server Received HEARTBEATCLIENT : " + strMsg);

            InetSocketAddress clientAddr = (InetSocketAddress) ctx.channel().remoteAddress();
            // 心跳消息需要更新客户更新时间
            if (null != clientAddr)
                cuckooServerService.tryUpdateClientStatus(clientAddr);
            return;
        }

        // 打印客户端传来的消息内容
        log.info("Server Received Message : " + strMsg);

        MessageInfo msgInfo = null;
        try {
            msgInfo = gson.fromJson(strMsg, MessageInfo.class);
        } catch (Exception e) {
            log.error("cuckoo server can not read message:{}", strMsg, e);
            return;
        }

        if (CuckooMessageType.REGIST.getValue().equals(msgInfo.getMessageType().getValue())) {

            // 客户端任务注册.  {"messageType":"REGIST","message":{"appName":"member","beanName":"cuckooTestTaskImpl","methodName":"testJob","taskName":"testJob"}}
            ClientTaskInfoBean taskInfo = gson.fromJson(gson.toJson(msgInfo.getMessage()), ClientTaskInfoBean.class);
            log.info("#@#>> reading REGIST msg: {}", taskInfo);
        } else if (CuckooMessageType.JOBSUCCED.getValue().equals(msgInfo.getMessageType().getValue())) {

            // 客户端任务执行成功 .  {"messageType":"JOBSUCCED","message":{"jobName":"testJob2","txDate":20160101,"forceJob":false,"needTrigglerNext":true,"cuckooParallelJobArgs":""}}
            JobInfoBean jobInfo = gson.fromJson(gson.toJson(msgInfo.getMessage()), JobInfoBean.class);
            cuckooJobCallBack.execJobSuccedCallBack(jobInfo);
        } else if (CuckooMessageType.JOBFAILED.getValue().equals(msgInfo.getMessageType().getValue())) {

            // 客户端任务执行失败.  {"messageType":"JOBFAILED","message":{"jobName":"testJob2","txDate":20160101,"forceJob":false,"needTrigglerNext":true,"cuckooParallelJobArgs":""}}
            JobInfoBean jobInfo = gson.fromJson(gson.toJson(msgInfo.getMessage()), JobInfoBean.class);
            cuckooJobCallBack.execJobFailedCallBack(jobInfo);
        } else {
            log.error("cuckoo server get a unknown message type{}", strMsg);
        }
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("@$$ serverSideExceptionOccurred: {}", cause.getMessage(), cause);
        ctx.close();
    }


//    /**
//     * 担任 心跳功能
//     * @param ctx
//     * @param evt
//     * @throws Exception
//     */
//    @Override
//    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
//        if (evt instanceof IdleStateEvent) {
//            IdleStateEvent event = (IdleStateEvent) evt;
//
//            ctx.writeAndFlush(HEARTBEAT_SEQUENCE.duplicate()).addListener(
//                    ChannelFutureListener.CLOSE_ON_FAILURE);
//
//            log.info("userEventTriggered: {}, timeoutType: {}", ctx.channel().remoteAddress(), event.state().name());
//
//        } else {
//            super.userEventTriggered(ctx, evt);
//        }
//    }
}

