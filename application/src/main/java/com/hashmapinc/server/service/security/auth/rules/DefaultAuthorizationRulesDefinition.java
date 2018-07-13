/**
 * Copyright © 2017-2018 Hashmap, Inc
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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.expression.Expression;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
public class DefaultAuthorizationRulesDefinition implements AuthorizationRulesDefinition{

    private static final String DEFAULT_RULES_FILE_NAME = "default-rules.json";

    @Value("${authorization.rules.filePath}")
    private String rulesFilePath;

    private List<AuthorizationRule> rules;

    @PostConstruct
    private void init(){
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addDeserializer(Expression.class, new SpelDeserializer());
        mapper.registerModule(module);
        try {
            AuthorizationRule[] rulesArray;
            log.debug("[init] Checking rules file at: {}", rulesFilePath);
            if(rulesFilePath != null && !rulesFilePath.isEmpty()
                    && Paths.get(rulesFilePath).toFile().exists()) {
                log.info("[init] Loading rules from custom file: {}", rulesFilePath);
                rulesArray = mapper.readValue(new File(rulesFilePath), AuthorizationRule[].class);
            } else {
                log.info("[init] Custom rules file not found. Loading default rules");
                InputStream jsonStream = getClass().getClassLoader().getResourceAsStream(DEFAULT_RULES_FILE_NAME);
                rulesArray = mapper.readValue(jsonStream, AuthorizationRule[].class);
            }
            this.rules = (rulesArray != null? Arrays.asList(rulesArray) : null);
            log.info("[init] rules loaded successfully.");
        } catch (IOException e) {
            log.error("An error occurred while parsing the rules file.", e);
        }
    }

    @Override
    public List<AuthorizationRule> rules() {
        return rules;
    }
}
