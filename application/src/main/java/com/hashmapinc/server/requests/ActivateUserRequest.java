package com.hashmapinc.server.requests;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ActivateUserRequest {
    private String activateToken;
    private String password;
}
