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
package com.hashmapinc.server.common.data.computation;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("SPARK")
public class SparkComputationMetadata extends ComputationMetadata{
    private String jarPath;
    private String jarName;
    private String mainClass;
    private String argsformat;
    private String argsType;
    private JsonNode jsonDescriptor;

    public SparkComputationMetadata(){
        super();
    }

    public SparkComputationMetadata(SparkComputationMetadata md){
        super(md);
        this.jarPath = md.getJarPath();
        this.jarName = md.getJarName();
        this.mainClass = md.getMainClass();
        this.argsformat = md.getArgsformat();
        this.argsType = md.getArgsType();
        this.jsonDescriptor = md.getJsonDescriptor();
    }
}
