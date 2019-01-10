package com.hashmapinc.server.common.data.computation;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("LAMBDA")
public class AWSLambdaComputationMetadata extends ComputationMetadata {
    private String filePath;
    private String functionHandler;
    private String description;
    private long timeout;
    private long memorySize;
    private String roleArn;
    private String region;

    public AWSLambdaComputationMetadata() {
        super();
    }
}
