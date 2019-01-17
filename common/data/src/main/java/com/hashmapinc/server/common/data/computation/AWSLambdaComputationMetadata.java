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
@JsonTypeName("LAMBDA")
public class AWSLambdaComputationMetadata extends ComputationMetadata {
    private String filePath;
    private String functionName;
    private String functionHandler;
    private String runtime;
    private String description;
    private int timeout;
    private int memorySize;
    private String region;

    public AWSLambdaComputationMetadata() {
        super();
    }

    public AWSLambdaComputationMetadata(AWSLambdaComputationMetadata md) {
        super(md);
        this.filePath = md.filePath;
        this.functionName = md.functionName;
        this.functionHandler = md.functionHandler;
        this.runtime = md.runtime;
        this.description = md.description;
        this.timeout = md.timeout;
        this.memorySize = md.memorySize;
        this.region = md.region;
    }


}
