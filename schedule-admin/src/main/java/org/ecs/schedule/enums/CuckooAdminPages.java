package org.ecs.schedule.enums;

import org.ecs.common.Describable;
import org.ecs.common.Valued;
import lombok.Getter;

@Getter
public enum CuckooAdminPages implements Valued, Describable {

    INDEX("/workstudio", "首页"),

    LOGIN("/logon/index", "登录页"),

    ERROR("/common/common.exception", "报错页面");

    private final String value;

    private final String description;

    CuckooAdminPages(String value, String description) {
        this.value = value;
        this.description = description;
    }

    public static CuckooAdminPages fromName(String input) {
        for (CuckooAdminPages item : CuckooAdminPages.values()) {
            if (item.name().equalsIgnoreCase(input))
                return item;
        }
        return null;
    }

}

