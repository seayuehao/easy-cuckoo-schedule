package org.ecs.schedule.net.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.ecs.schedule.bean.ClientTaskInfoBean;
import org.ecs.schedule.bean.MessageInfo;
import org.ecs.schedule.constant.CuckooNetConstant;
import org.ecs.schedule.enums.CuckooMessageType;
import org.ecs.schedule.executor.framerwork.bean.ClientInfoBean;
import org.ecs.schedule.executor.framerwork.bean.CuckooTaskBean;
import org.ecs.schedule.executor.framerwork.cache.CuckooTaskCache;
import org.ecs.schedule.net.client.handle.NettyClientHandler;
import org.ecs.schedule.net.server.cache.IoServerCollection;
import org.ecs.schedule.net.server.cache.MessageSendQueue;
import org.ecs.schedule.net.vo.IoServerBean;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

@Slf4j
public class NettyClientSide {

    private static Bootstrap clientBootStrap;

    private static ChannelFuture channelFuture;

    private static final EventLoopGroup workerGroup = new NioEventLoopGroup();

    private static final Gson GSON = new GsonBuilder().create();

    private static volatile boolean alreadyInitialized = false;

    private static synchronized void init() {
        try {
            log.info("init...");
            clientBootStrap = new Bootstrap();
            clientBootStrap.group(workerGroup);
            clientBootStrap.channel(NioSocketChannel.class);
            clientBootStrap.option(ChannelOption.SO_KEEPALIVE, true);
            clientBootStrap.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel socketChannel) {
                    socketChannel.pipeline().addLast(new StringDecoder(StandardCharsets.UTF_8));
                    socketChannel.pipeline().addLast(new StringEncoder(StandardCharsets.UTF_8));
                    socketChannel.pipeline().addLast(new NettyClientHandler());
                }
            });
            alreadyInitialized = true;

            Runtime.getRuntime().addShutdownHook(new Thread(() -> close()));
        } catch (Exception e) {
            log.error("#@#@initErr:", e);
        }
    }


    public static boolean connect(IoServerBean bean) {
        if (bean == null || bean.getIp() == null || bean.getPort() == null) {
            return false;
        }
        if (!alreadyInitialized) init();
        boolean result = false;
        try {
            InetSocketAddress address = new InetSocketAddress(bean.getIp(), bean.getPort());
            channelFuture = clientBootStrap.connect(address).sync();
            log.info("cuckoo job succeed to connect server, Ip: {}, port: {}", bean.getIp(), bean.getPort());

            if (CollectionUtils.isNotEmpty(CuckooTaskCache.getCache())) {
                for (Iterator<CuckooTaskBean> it = CuckooTaskCache.getCache().iterator(); it.hasNext(); ) {
                    CuckooTaskBean taskBean = it.next();
                    ClientTaskInfoBean taskInfo = new ClientTaskInfoBean();
                    taskInfo.setAppName(ClientInfoBean.getAppName());
                    taskInfo.setClientTag(ClientInfoBean.getClientTag());
                    taskInfo.setBeanName(taskBean.getBeanName());
                    taskInfo.setMethodName(taskBean.getMethodName());
                    taskInfo.setTaskName(taskBean.getTaskName());
                    send(CuckooMessageType.REGIST, taskInfo);
                }
            }
            result = true;
        } catch (Exception e) {
            log.error("#@@#>>> err:", e);
        }
        return result;
    }


    public static void start(InetSocketAddress address) throws InterruptedException {
        if (!alreadyInitialized) init();
        channelFuture = clientBootStrap.connect(address).sync();
    }


    public static Object send(Object msg) throws InterruptedException {
        // 传数据给服务端
        channelFuture.channel().writeAndFlush(msg);
        channelFuture.channel().closeFuture().sync();
        return channelFuture.channel().attr(AttributeKey.valueOf(CuckooNetConstant.ATTR_KEY)).get();
    }


    public static boolean sendMessageInfo(MessageInfo msgInfo) {
        if (CollectionUtils.isNotEmpty(IoServerCollection.getSet())) {
            for (Iterator<IoServerBean> it = IoServerCollection.getSet().iterator(); it.hasNext(); ) {
                IoServerBean server = it.next();
                if (null != server) {
                    send(msgInfo.getMessageType(), msgInfo.getMessage());
                    return true;
                }
            }
        }
        return false;
    }

    public static void send(CuckooMessageType messageType, Object message) {
        try {
            // 给服务端发消息
            MessageInfo msgInfo = new MessageInfo();
            msgInfo.setMessage(message);
            msgInfo.setMessageType(messageType);
            String msg = GSON.toJson(msgInfo);

            log.info("客户端发送消息:server:{}, msg:{}", msg);
            channelFuture.channel().writeAndFlush(msg);
            channelFuture.channel().closeFuture().sync();

            if (!sendMessageInfo(msgInfo)) {
                MessageSendQueue.instance().getQueue().offer(msgInfo);
            }

        } catch (Exception e) {
            log.error("client message send error:{}", e.getMessage(), e);
        }
    }


    public static void close() {
        try {
            if (null != channelFuture && null != channelFuture.channel())
                channelFuture.channel().close();

            if (null != workerGroup)
                workerGroup.shutdownGracefully();

            log.info("clientSide closed!");
        } catch (Exception e) {
            log.error("#@#@# error: ", e);
        }
    }

}
