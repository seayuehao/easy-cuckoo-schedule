package org.ecs.schedule.web.util;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * 数据分页信息
 *
 * @param <T>
 * @param
 * @ClassName: PageDataList
 * @Description:
 */
@Getter
@Setter
public class JqueryDataTable<T> implements java.io.Serializable {

    /**
     * 总个数
     */
    private long recordsTotal = 0;
    /**
     * 总个数
     */
    private long recordsFiltered = 0;

    /**
     * 返回的数据集
     */
    private List<T> data = new ArrayList<T>();


    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this);
    }

}
