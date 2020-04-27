package org.ecs.schedule.support;

import org.ecs.schedule.net.client.handle.NettyClientHandler;
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
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;


/**
 * 测试时使用
 * <p>
 * 启用 如下注解 @Component, @PostConstruct; 在 mainApp 进行编码测试
 */
@Slf4j
//@Component
public class NettyClientDemo {

    private static Bootstrap clientBootStrap;

    private static ChannelFuture channelFuture;

    private static final EventLoopGroup workerGroup = new NioEventLoopGroup();

    //    @PostConstruct
    protected synchronized void init() {
        try {
            log.info("init NettyClientDemo...");
            clientBootStrap = new Bootstrap();
            clientBootStrap.group(workerGroup);
            clientBootStrap.option(ChannelOption.SO_KEEPALIVE, true);
            clientBootStrap.channel(NioSocketChannel.class);

            clientBootStrap.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel socketChannel) {

                    socketChannel.pipeline().addLast(new StringDecoder(StandardCharsets.UTF_8));
                    socketChannel.pipeline().addLast(new StringEncoder(StandardCharsets.UTF_8));
                    socketChannel.pipeline().addLast(new NettyClientHandler());
                }
            });
        } catch (Exception e) {
            log.error("#@#@ initError:");
        }
    }


    public ChannelFuture start(InetSocketAddress address) throws InterruptedException {
        channelFuture = clientBootStrap.connect(address).sync();
        return channelFuture;
    }


    public void shutdown() {
        try {
            if (null != channelFuture && null != channelFuture.channel()) {
                channelFuture.channel().close();
            }
            workerGroup.shutdownGracefully();
        } catch (Exception e) {
            log.error("#@#@ shutdownError:");
        }
    }

}
