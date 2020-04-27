package org.ecs.schedule.vo.auth;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

@Getter
@Setter
public class CuckooAuthUserVo {

    private Long id;

    private String userName;

    private String userPwd;

    private String userPwd2;

    private String userAuthType;

    private String phone;

    private String email;

    private String orgName;

    public String toString() {
        return ReflectionToStringBuilder.toString(this);
    }
}
