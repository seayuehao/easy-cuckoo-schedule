package org.ecs.schedule.web.util;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

/**
 * 服务返回给客户端的json对象包装
 */
@Getter
@Setter
public class JsonResult<T> {
    /**
     * 状态
     */
    public enum Status {
        // 正确
        SUCCESS("success", "成功"),
        // 错误
        ERROR("fail", "失败"),

        WARN("warn", "警告"),

        REQUESTBINDERROR("binderror", "参数错误");

        private String resultCode = "success";

        Status(String resultCode, String resultMsg) {
            this.resultCode = resultCode;
        }

        public String getCode() {
            return resultCode;
        }

    }

    private String resultCode;

    private String resultMsg;

    private T data = null;

    public JsonResult(Status status, String resultMsg) {
        this.resultCode = status.resultCode;
        this.resultMsg = resultMsg;
    }

    public JsonResult(Status status, String resultMsg, T data) {
        this.resultCode = status.resultCode;
        this.resultMsg = resultMsg;
        this.data = data;
    }


    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this);
    }
}
