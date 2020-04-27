package org.ecs.schedule.enums;

import org.ecs.common.Describable;
import org.ecs.common.Valued;
import lombok.Getter;

import java.util.Arrays;

/**
 * 公共枚举- 是、否
 */
@Getter
public enum CuckooBooleanFlag implements Valued, Describable {

    NULL("", "全部/无"),

    NO("NO", "否"),

    YES("YES", "是");

    private final String value;

    private final String description;

    CuckooBooleanFlag(String value, String description) {
        this.value = value;
        this.description = description;
    }

    public static CuckooBooleanFlag fromName(String input) {

        for (CuckooBooleanFlag item : CuckooBooleanFlag.values()) {
            if (item.name().equalsIgnoreCase(input))
                return item;
        }

        return null;
    }

    public static CuckooBooleanFlag[] valuesNoNull() {

        CuckooBooleanFlag[] result = CuckooBooleanFlag.values();
        for (int i = 0; i < result.length; i++) {
            if (CuckooBooleanFlag.NULL.equals(result[i])) {
                // 提出null元素 --用最后一个元素替换，然后删除最后一个元素
                result[i] = result[result.length - 1];
                //数组缩容
                result = Arrays.copyOf(result, result.length - 1);
            }
        }
        return result;
    }

}

