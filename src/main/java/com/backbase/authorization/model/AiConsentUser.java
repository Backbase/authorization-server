package com.backbase.authorization.model;

import java.util.Collections;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiConsentUser {

    private String username;
    private List<String> roles = Collections.emptyList();

}
