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
package com.hashmapinc.server.dao.model.sql;

import com.hashmapinc.server.common.data.RegionType;
import com.hashmapinc.server.common.data.computation.AWSLambdaComputationMetadata;
import com.hashmapinc.server.common.data.id.ComputationId;
import com.hashmapinc.server.dao.model.ModelConstants;

import javax.persistence.*;


@Entity
@Table(name = ModelConstants.AWS_LAMBDA_COMPUTATIONS_META_DATA)
public class AWSLambdaComputationMetadataEntity extends ComputationMetadataEntity<AWSLambdaComputationMetadata> {

    @Column(name = ModelConstants.AWS_LAMBDA_FILE_PATH)
    private String filePath;

    @Column(name = ModelConstants.AWS_LAMBDA_FUNCTION_NAME)
    private String functionName;

    @Column(name = ModelConstants.AWS_LAMBDA_FUNCTION_HANDLER)
    private String functionHandler;

    @Column(name = ModelConstants.AWS_LAMBDA_DESCRIPTION)
    private String description;

    @Column(name = ModelConstants.AWS_LAMBDA_RUNTIME)
    private String runtime;

    @Column(name = ModelConstants.AWS_LAMBDA_TIMEOUT)
    private String timeout;

    @Column(name = ModelConstants.AWS_LAMBDA_MEMORY_SIZE)
    private String memorySize;

    @Enumerated(EnumType.STRING)
    @Column(name = ModelConstants.AWS_LAMBDA_REGION)
    private RegionType region;

    public AWSLambdaComputationMetadataEntity(){
        super();
    }

    public AWSLambdaComputationMetadataEntity(AWSLambdaComputationMetadata awsLambdaComputationMetadata){
        if(awsLambdaComputationMetadata.getId() != null){
            this.setId(awsLambdaComputationMetadata.getId().getId());
        }
        if(awsLambdaComputationMetadata.getFilePath() != null){
            this.filePath = awsLambdaComputationMetadata.getFilePath();
        }
        if(awsLambdaComputationMetadata.getFunctionName() != null){
            this.functionName = awsLambdaComputationMetadata.getFunctionName();
        }
        if(awsLambdaComputationMetadata.getFunctionHandler() != null){
            this.functionHandler = awsLambdaComputationMetadata.getFunctionHandler();
        }
        if(awsLambdaComputationMetadata.getDescription() != null){
            this.description = awsLambdaComputationMetadata.getDescription();
        }
        if(awsLambdaComputationMetadata.getRuntime() != null){
            this.runtime = awsLambdaComputationMetadata.getRuntime();
        }
        if(awsLambdaComputationMetadata.getTimeout() != null){
            this.timeout = awsLambdaComputationMetadata.getTimeout();
        }
        if(awsLambdaComputationMetadata.getMemorySize() != null){
            this.memorySize = awsLambdaComputationMetadata.getMemorySize();
        }
        if(awsLambdaComputationMetadata.getRegion() != null){
            this.region = awsLambdaComputationMetadata.getRegion();
        }
    }

    @Override
    public AWSLambdaComputationMetadata toData() {
        AWSLambdaComputationMetadata awsLambdaComputationMetadata = new AWSLambdaComputationMetadata();
        awsLambdaComputationMetadata.setId(new ComputationId(getId()));
        awsLambdaComputationMetadata.setFilePath(this.filePath);
        awsLambdaComputationMetadata.setFunctionName(this.functionName);
        awsLambdaComputationMetadata.setFunctionHandler(this.functionHandler);
        awsLambdaComputationMetadata.setRuntime(this.runtime);
        awsLambdaComputationMetadata.setDescription(this.description);
        awsLambdaComputationMetadata.setTimeout(this.timeout);
        awsLambdaComputationMetadata.setMemorySize(this.memorySize);
        awsLambdaComputationMetadata.setRegion(this.region);

        return awsLambdaComputationMetadata;
    }
}
