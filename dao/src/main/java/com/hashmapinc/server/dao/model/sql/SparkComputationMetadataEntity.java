package com.hashmapinc.server.dao.model.sql;

import com.hashmapinc.server.common.data.computation.SparkComputationMetadata;
import com.hashmapinc.server.dao.model.ModelConstants;

import javax.persistence.Column;
import javax.persistence.Entity;

@Entity
public class SparkComputationMetadataEntity extends ComputationMetadataEntity<SparkComputationMetadata>{

    @Column(name = ModelConstants.COMPUTATIONS_JAR_PATH)
    private String jarPath;

    @Column(name = ModelConstants.COMPUTATIONS_JAR)
    private String jarName;

    @Column(name = ModelConstants.COMPUTATIONS_MAIN_CLASS)
    private String mainClass;

    @Column(name = ModelConstants.COMPUTATIONS_ARGS_FORMAT)
    private String argsFormat;

    @Column(name = ModelConstants.COMPUTATIONS_ARGS_TYPE)
    private String argsType;

    @Override
    public SparkComputationMetadata toData() {
        return null;
    }
}
