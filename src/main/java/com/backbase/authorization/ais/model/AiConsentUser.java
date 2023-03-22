package com.backbase.authorization.ais.model;

import java.util.Collections;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AiConsentUser {

    private String username;
    private List<String> roles = Collections.emptyList();

}
