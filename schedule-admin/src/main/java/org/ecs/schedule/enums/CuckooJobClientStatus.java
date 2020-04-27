package org.ecs.schedule.enums;

import org.ecs.common.Describable;
import org.ecs.common.Valued;
import lombok.Getter;

/**
 * 任务状态
 */
@Getter
public enum CuckooJobClientStatus implements Valued, Describable {

    NULL("", "全部/无"),

    RUNNING("RUNNING", "运行中"),

    OFFLINE("OFFLINE", "断线");

    private final String value;

    private final String description;

    CuckooJobClientStatus(String value, String description) {
        this.value = value;
        this.description = description;
    }

    public static CuckooJobClientStatus fromName(String input) {
        for (CuckooJobClientStatus item : CuckooJobClientStatus.values()) {
            if (item.name().equalsIgnoreCase(input))
                return item;
        }
        return null;
    }

}

