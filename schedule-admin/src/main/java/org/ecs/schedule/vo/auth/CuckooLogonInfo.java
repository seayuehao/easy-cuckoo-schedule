package org.ecs.schedule.vo.auth;

import org.ecs.schedule.enums.CuckooUserAuthType;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import java.util.List;

@Getter
@Setter
public class CuckooLogonInfo {

    private Long id;

    private String phone;

    private String email;

    private String orgName;

    private String userName;

    private CuckooUserAuthType cuckooUserAuthType;

    private List<Long> readableGroupIds;

    private List<Long> writableGroupIds;

    private List<Long> grantableGroupIds;

    @Override
    public String toString() {

        return ReflectionToStringBuilder.toString(this);
    }
}
