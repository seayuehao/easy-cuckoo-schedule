package org.ecs.schedule.net.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.ecs.schedule.bean.MessageInfo;
import org.ecs.schedule.enums.CuckooMessageType;
import org.ecs.schedule.exception.BaseException;
import org.ecs.schedule.net.server.handle.ServerChannelInitializer;
import org.ecs.schedule.net.vo.IoClientInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;

@Slf4j
@Component
public class NettyServerSide {

    private final EventLoopGroup bossGroup = new NioEventLoopGroup();

    private final EventLoopGroup workerGroup = new NioEventLoopGroup();

    private Channel channel;

    private static final Gson GSON = new GsonBuilder().create();

    @Autowired
    private ApplicationContext applicationContext;


    /**
     * 启动服务
     */
    public void start(InetSocketAddress address) {
        ChannelFuture channelFuture = null;
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ServerChannelInitializer(applicationContext))
                .option(ChannelOption.SO_BACKLOG, 128)
                .childOption(ChannelOption.SO_KEEPALIVE, true);

            channelFuture = bootstrap.bind(address).syncUninterruptibly();
            channel = channelFuture.channel();
            channelFuture.channel().closeFuture().syncUninterruptibly();
        } catch (Exception e) {
            log.error("Netty start error:", e);
        } finally {
            if (channelFuture != null && channelFuture.isSuccess()) {
                log.info("Netty server listening " + address.getHostName() + " on port " + address.getPort() + " and ready for connections...");
            } else {
                log.error("Netty server start up Error!");
            }

            // 注册 关闭 钩子
            Runtime.getRuntime().addShutdownHook(new Thread(() -> close()));
        }
    }


    public void close() {
        log.info("====>>>>> Shutdown Netty Server...");
        try {
            if (channel != null) {
                channel.close();
            }
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        } catch (Exception e) {
            log.error("#@##@# destroyNettyServerErr:", e);
        }
        log.info("====>>>>> Shutdown Netty Server Successfully!");
    }


    public void send(IoClientInfo clientInfo, CuckooMessageType messageType, Object message) {
        // 给服务端发消息
        MessageInfo msgInfo = new MessageInfo();
        msgInfo.setMessage(message);
        msgInfo.setMessageType(messageType);
        String msg = GSON.toJson(msgInfo);
        try {
            channel.writeAndFlush("ack: " + msg);
            log.info("server send message successfully: {}, {}, msg:{}", clientInfo.getIp(), clientInfo.getPort(), msg);
        } catch (Exception e) {
            log.info("server send message failed: msg:{},error:{}", msg, e.getMessage(), e);
            throw new BaseException("server send message failed:  msg:{},error:{}", msg, e.getMessage());
        }
    }

}
