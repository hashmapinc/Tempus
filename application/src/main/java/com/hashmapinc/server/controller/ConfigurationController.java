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
package com.hashmapinc.server.controller;


import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import com.hashmapinc.server.exception.TempusException;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/ui")
@Slf4j
@Component
@Configuration
@ConfigurationProperties(prefix = "configurations")
public class ConfigurationController extends BaseController {

    private Map<String, String> ui = new HashMap<>();

    public Map<String, String> getUi() {
        return ui;
    }

    public void setUi(Map<String, String> ui) {
        this.ui = ui;
    }

    @RequestMapping(value = "/configurations", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, String> getUiConfiguration() throws TempusException {
        if (!ui.isEmpty()) {
            return ui;
        } else {
            throw handleException(new Exception("UI Configuration not present"));
        }
    }
}


