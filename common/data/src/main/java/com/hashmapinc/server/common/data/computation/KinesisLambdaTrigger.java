package com.hashmapinc.server.common.data.computation;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("LAMBDA-KINESIS")
public class KinesisLambdaTrigger extends ComputationJobConfiguration {
    private static final long serialVersionUID = -7910153605304892262L;

    private String functionName;
    private String streamName;
    private String region;
    private String startingPositions;
    private int batchSize;
}
