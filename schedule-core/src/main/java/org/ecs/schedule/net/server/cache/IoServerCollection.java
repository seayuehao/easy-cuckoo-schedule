package org.ecs.schedule.net.server.cache;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.ecs.schedule.net.client.NettyClientSide;
import org.ecs.schedule.net.vo.IoServerBean;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

@Slf4j
public class IoServerCollection {

    private static Set<IoServerBean> set = new HashSet<>();

    private IoServerCollection() {
        super();
    }

    public static boolean add(IoServerBean bean) {
        return set.add(bean);
    }

    public static Set<IoServerBean> getSet() {
        return set;
    }

    public static void remove(InetSocketAddress clientAddr) {
        String ip = clientAddr.getAddress().getHostAddress();
        Integer port = clientAddr.getPort();
        remove(ip, port);
    }

    private static void remove(String ip, Integer port) {
        if (CollectionUtils.isEmpty(set)) return;
        for (Iterator<IoServerBean> it = set.iterator(); it.hasNext(); ) {
            IoServerBean bean = it.next();
            if (bean.getIp().equals(ip) && bean.getPort().equals(port)) {
                it.remove();
            }
        }
    }

    /*
     * retry connect to server,in case of server resart
     */
    public static void retryConnect() {
        new Thread(() -> {
            while (true) {
                try {
                    Set<IoServerBean> servers = IoServerCollection.getSet();
                    if (CollectionUtils.isNotEmpty(servers)) {
                        List<IoServerBean> unConnect = new ArrayList<>();
                        for (IoServerBean ioServerBean : servers) {
                            if (null != ioServerBean) {
                                if (!NettyClientSide.connect(ioServerBean)) {
                                    unConnect.add(ioServerBean);
                                }
                            }
                        }
                        if (CollectionUtils.isNotEmpty(unConnect)) {
                            log.warn("cuckoo disconnected to server: {}", unConnect);
                        }
                    }
                    Thread.sleep(20000);
                } catch (Exception e) {
                    log.error("unknown error:{}", e.getMessage(), e);
                }
            }
        }).start();
        log.info("retry connect thread start");
    }

}
