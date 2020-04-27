package org.ecs.schedule.net.server.cache;

import lombok.extern.slf4j.Slf4j;
import org.ecs.schedule.bean.MessageInfo;
import org.ecs.schedule.net.client.NettyClientSide;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

@Slf4j
public class MessageSendQueue {

    private static final MessageSendQueue INSTANCE = new MessageSendQueue();

    private MessageSendQueue() {
    }

    public static MessageSendQueue instance() {
        return INSTANCE;
    }

    private volatile BlockingQueue<MessageInfo> queue = new LinkedBlockingQueue<>();

    public BlockingQueue<MessageInfo> getQueue() {
        return queue;
    }

    public static void trySendMessage() {
        log.info("#@#>>> trySendMessage@ {}", System.currentTimeMillis());
        new Thread(() -> {
            BlockingQueue<MessageInfo> queue = instance().getQueue();
            while (true) {
                try {
                    // ** poll 移除并返问队列头部的元素 如果队列为空，则返回null
                    final MessageInfo message = queue.poll(2, TimeUnit.SECONDS);
                    if (null == message) {
                        break;
                    }

                    log.info("messageSendQueue resend:{}", message);
                    NettyClientSide.sendMessageInfo(message);
                    Thread.sleep(30000);
                } catch (Exception e) {
                    log.error("unknown error:{}", e.getMessage(), e);
                }
            }
        }).start();
        log.info("retry Send Message thread start");
    }

}
