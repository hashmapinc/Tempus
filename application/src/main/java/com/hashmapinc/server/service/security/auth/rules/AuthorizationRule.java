package com.hashmapinc.server.service.security.auth.rules;

import lombok.Data;
import org.springframework.expression.Expression;

@Data
public class AuthorizationRule {
    private final String name;
    private final String description;
    private final Expression target;
    private final Expression condition;
}
