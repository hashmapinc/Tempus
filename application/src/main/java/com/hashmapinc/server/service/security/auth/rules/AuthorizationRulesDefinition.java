package com.hashmapinc.server.service.security.auth.rules;

import java.util.List;

public interface AuthorizationRulesDefinition {
    List<AuthorizationRule> rules();
}
