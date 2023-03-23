package com.backbase.authorization.security.token;

import lombok.Data;

@Data
public class AttributeClaim {

    private String attributeName;
    private Boolean toUserInfo = true;
    private Boolean toAccessToken = false;
    private Boolean toIdToken = false;
}
