package org.ecs.schedule.net.server.handle;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;
import org.ecs.schedule.enums.CuckooMessageType;

import java.nio.charset.StandardCharsets;


@Slf4j
public class HeartbeatServerHandler extends ChannelInboundHandlerAdapter {

    // Return a unreleasable view on the given ByteBuf
    // which will just ignore release and retain calls.
    private static final ByteBuf HEARTBEAT_SEQUENCE = Unpooled
        .unreleasableBuffer(Unpooled.copiedBuffer(CuckooMessageType.HEARTBEATSERVER.getValue(), StandardCharsets.UTF_8));

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt)
        throws Exception {

        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;

            ctx.writeAndFlush(HEARTBEAT_SEQUENCE.duplicate()).addListener(
                ChannelFutureListener.CLOSE_ON_FAILURE);

            log.info("userEventTriggered: {}, timeoutType: {}", ctx.channel().remoteAddress(), event.state().name());
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }


    /**
     * 业务逻辑处理
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        log.info("@#>>> channelRead: {}", msg);
        ctx.fireChannelRead(msg);
    }

}
