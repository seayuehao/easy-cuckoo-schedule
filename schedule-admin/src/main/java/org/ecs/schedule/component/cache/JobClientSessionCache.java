package org.ecs.schedule.component.cache;

import org.ecs.schedule.net.vo.IoClientInfo;

import java.util.concurrent.ConcurrentHashMap;

public class JobClientSessionCache {

    /**
     * 客户端连接缓存 Map<clientId,Socket>
     */
    private static ConcurrentHashMap<Long, IoClientInfo> CHANNEL_MAP = new ConcurrentHashMap<Long, IoClientInfo>();

    private JobClientSessionCache() {
        super();
    }

    public static IoClientInfo get(Long clientId) {

        return CHANNEL_MAP.get(clientId);
    }

    public static void put(Long clientId, IoClientInfo socket) {
        CHANNEL_MAP.put(clientId, socket);
    }

    public static void remove(Long clientId) {
        CHANNEL_MAP.remove(clientId);
    }

}
