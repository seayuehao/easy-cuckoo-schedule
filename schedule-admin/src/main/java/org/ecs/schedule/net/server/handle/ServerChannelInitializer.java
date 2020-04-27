package org.ecs.schedule.net.server.handle;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.timeout.IdleStateHandler;
import org.springframework.context.ApplicationContext;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

public class ServerChannelInitializer extends ChannelInitializer<SocketChannel> {

    private static final int READ_IDEL_TIME_OUT = 4; // 读超时

    private static final int WRITE_IDEL_TIME_OUT = 5;// 写超时

    private static final int ALL_IDEL_TIME_OUT = 7; // 所有超时

    private ApplicationContext applicationContext;

    public ServerChannelInitializer(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {

        socketChannel.pipeline().addLast(new StringDecoder(StandardCharsets.UTF_8));
        socketChannel.pipeline().addLast(new StringEncoder(StandardCharsets.UTF_8));

        socketChannel.pipeline().addLast(new NettyServerMsgHandler(applicationContext));

        socketChannel.pipeline().addLast(new IdleStateHandler(READ_IDEL_TIME_OUT,
            WRITE_IDEL_TIME_OUT, ALL_IDEL_TIME_OUT, TimeUnit.SECONDS)); // 1

        socketChannel.pipeline().addLast(new HeartbeatServerHandler());
    }

}
