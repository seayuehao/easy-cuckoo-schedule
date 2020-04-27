package org.ecs.schedule.vo.auth;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

@Getter
@Setter
public class CuckooGroupAuthVo {

    private Long id;

    private Long userId;

    private Long groupId;

    private String userName;

    private String userAuthType;

    private String readable;

    private String writable;

    private String grantable;

    @Override
    public String toString() {

        return ReflectionToStringBuilder.toString(this);
    }

}
