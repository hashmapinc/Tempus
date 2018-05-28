package com.hashmapinc.server.common.data.computation;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class SparkComputationMetadata extends ComputationMetadata{
    private String jarPath;
    private String jarName;
    private String mainClass;
    private String argsformat;
    private String argsType;

    public SparkComputationMetadata(SparkComputationMetadata md){
        super(md);
        this.jarPath = md.getJarPath();
        this.jarName = md.getJarName();
        this.mainClass = md.getMainClass();
        this.argsformat = md.getArgsformat();
        this.argsType = md.getArgsType();
    }
}
