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
package com.hashmapinc.server.common.data.computation;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("KUBELESS")
public class KubelessComputationMetadata extends ComputationMetadata {
    private String namespace;
    private String function;
    private transient String functionContent;
    private String functionContentType;
    private String handler;
    private String runtime;
    private String dependencies;
    private transient String dependencyFileName;
    private String checksum;
    private String timeout;


    public KubelessComputationMetadata(){
        super();
    }

    public KubelessComputationMetadata(KubelessComputationMetadata md){
        super(md);
        this.namespace = md.namespace;
        this.function = md.function;
        this.runtime = md.runtime;
        this.handler = md.handler;
        this.dependencies = md.dependencies;
        this.functionContentType = md.functionContentType;
        this.checksum = md.checksum;
        this.timeout = md.timeout;
    }
}
