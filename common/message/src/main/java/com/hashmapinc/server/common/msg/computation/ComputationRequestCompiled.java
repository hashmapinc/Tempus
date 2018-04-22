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
package com.hashmapinc.server.common.msg.computation;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.ToString;

@ToString
public class ComputationRequestCompiled implements ComputationMsg{

    private final String[] args;

    private final String name;
    private final JsonNode configurationDescriptor;
    private final String mainClazz;
    private final String argsType;

    public ComputationRequestCompiled(String[] args,
                                     String argsType,
                                     String name,
                                     JsonNode configurationDescriptor,
                                     String mainClazz) {
        this.args = args;
        this.argsType = argsType;
        this.name = name;
        this.configurationDescriptor = configurationDescriptor;
        this.mainClazz = mainClazz;
    }

    public String[] getArgs() {
        return args;
    }

    public String getName() {
        return name;
    }

    public JsonNode getConfigurationDescriptor() {
        return configurationDescriptor;
    }

    public String getMainClazz() {
        return mainClazz;
    }

    public String getArgsType() {
        return argsType;
    }
}
