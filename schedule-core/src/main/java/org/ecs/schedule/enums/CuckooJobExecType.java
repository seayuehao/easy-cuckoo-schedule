package org.ecs.schedule.enums;

import org.ecs.common.Describable;
import org.ecs.common.Valued;
import lombok.Getter;

import java.util.Arrays;

@Getter
public enum CuckooJobExecType  implements Valued, Describable {

    NULL("", "全部/无"),

    CUCKOO("CUCKOO", "CUCKOO")/*CUCKOO任务*/,

    SCRIPT("SCRIPT", "脚本")/*脚本任务*/;

    private final String value;

    private final String description;

    CuckooJobExecType(String value, String description) {

        this.value = value;
        this.description = description;
    }

    public static CuckooJobExecType fromName(String input) {

        for (CuckooJobExecType item : CuckooJobExecType.values()) {
            if (item.name().equalsIgnoreCase(input))
                return item;
        }

        return null;
    }

    public static CuckooJobExecType[] valuesNoNull() {
        CuckooJobExecType[] result = CuckooJobExecType.values();
        for (int i = 0; i < result.length; i++) {
            if (CuckooJobExecType.NULL.equals(result[i])) {
                // 提出null元素 --用最后一个元素替换，然后删除最后一个元素
                result[i] = result[result.length - 1];
                //数组缩容
                result = Arrays.copyOf(result, result.length - 1);
            }
        }
        return result;
    }

}

