package com.backbase.authorization.security.token;

import lombok.Data;

@Data
public class AttributeClaimMapping {

    private String attributeName;
    private Boolean toUserInfo = true;
    private Boolean toAccessToken = false;
    private Boolean toIdToken = false;
}
