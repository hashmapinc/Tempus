package com.hashmapinc.server.common.data.computation;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class KubelessComputationMetadata extends ComputationMetadata {
    private String name;
    private String namespace;
    private String function;
    private String handler;
    private Runtimes runtime;
    private String dependencies;
}
