/**
 * Copyright © 2016-2018 The Thingsboard Authors
 * Modifications © 2017-2018 Hashmap, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hashmapinc.server.service.security.auth.rules;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.expression.EvaluationException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class DefaultRulesChecker implements RulesChecker{

    @Autowired
    private AuthorizationRulesDefinition rulesDefinition;


    @Override
    public boolean check(Object subject, Object resource, Object action) {
        List<AuthorizationRule> allRules = rulesDefinition.rules();
        SecurityAccessContext context = new SecurityAccessContext(subject, resource, action);
        List<AuthorizationRule> matchedRules = filterRules(allRules, context);
        return checkRules(matchedRules, context);
    }

    private List<AuthorizationRule> filterRules(List<AuthorizationRule> allRules, SecurityAccessContext context) {
        List<AuthorizationRule> matchedRules = new ArrayList<>();
        for(AuthorizationRule rule : allRules) {
            try {
                if(rule.getTarget().getValue(context, Boolean.class)) {
                    matchedRules.add(rule);
                }
            } catch(EvaluationException ex) {
                log.error("An error occurred while checking target for AuthorizationRule.", ex);
            }
        }
        return matchedRules;
    }

    private boolean checkRules(List<AuthorizationRule> matchedRules, SecurityAccessContext context) {
        for(AuthorizationRule rule : matchedRules) {
            try {
                if(rule.getCondition().getValue(context, Boolean.class)) {
                    return true;
                }
            } catch(EvaluationException ex) {
                log.error("An error occurred while evaluating AuthorizationRule.", ex);
            }
        }
        return false;
    }
}
