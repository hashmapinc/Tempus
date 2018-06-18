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
package com.hashmapinc.server.dao.model.nosql;

import com.datastax.driver.mapping.annotations.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.hashmapinc.server.common.data.computation.SparkComputationMetadata;
import com.hashmapinc.server.common.data.id.ComputationId;
import com.hashmapinc.server.dao.model.ModelConstants;
import com.hashmapinc.server.dao.model.type.JsonCodec;

import java.util.UUID;

import static com.hashmapinc.server.dao.model.ModelConstants.ID_PROPERTY;

@Table(name = ModelConstants.SPARK_COMPUTATIONS_META_DATA_PROPERTY)
public class SparkComputationMetadataEntity implements ComputationMetadataEntity<SparkComputationMetadata>{
    @PartitionKey(value = 0)
    @Column(name = ID_PROPERTY)
    private UUID id;

    @Column(name = ModelConstants.COMPUTATIONS_JAR_PATH_PROPERTY)
    private String jarPath;

    @Column(name = ModelConstants.COMPUTATIONS_JAR_PROPERTY)
    private String jarName;

    @Column(name = ModelConstants.COMPUTATIONS_MAIN_CLASS_PROPERTY)
    private String mainClass;

    @Column(name = ModelConstants.COMPUTATIONS_ARGS_FORMAT_PROPERTY)
    private String argsFormat;

    @Column(name = ModelConstants.COMPUTATIONS_ARGS_TYPE_PROPERTY)
    private String argsType;

    @Column(name = ModelConstants.COMPUTATIONS_DESCRIPTOR_PROPERTY, codec = JsonCodec.class)
    private JsonNode jsonDescriptor;

    public SparkComputationMetadataEntity(){
        super();
    }

    public SparkComputationMetadataEntity(SparkComputationMetadata md){
        if(md.getId() != null){
            this.setId(md.getId().getId());
        }
        if(md.getJarName() != null)
            this.jarName = md.getJarName();
        if(md.getJarPath() != null)
            this.jarPath = md.getJarPath();
        if(md.getMainClass() != null)
            this.mainClass = md.getMainClass();
        if(md.getArgsformat() != null)
            this.argsFormat = md.getArgsformat();
        if(md.getArgsType() != null)
            this.argsType = md.getArgsType();
        if(md.getJsonDescriptor() != null)
            this.jsonDescriptor = md.getJsonDescriptor();
    }

    public String getJarPath() {
        return jarPath;
    }

    public void setJarPath(String jarPath) {
        this.jarPath = jarPath;
    }

    public String getJarName() {
        return jarName;
    }

    public void setJarName(String jarName) {
        this.jarName = jarName;
    }

    public String getMainClass() {
        return mainClass;
    }

    public void setMainClass(String mainClass) {
        this.mainClass = mainClass;
    }

    public String getArgsFormat() {
        return argsFormat;
    }

    public void setArgsFormat(String argsFormat) {
        this.argsFormat = argsFormat;
    }

    public String getArgsType() {
        return argsType;
    }

    public void setArgsType(String argsType) {
        this.argsType = argsType;
    }

    public JsonNode getJsonDescriptor() {
        return jsonDescriptor;
    }

    public void setJsonDescriptor(JsonNode jsonDescriptor) {
        this.jsonDescriptor = jsonDescriptor;
    }

    @Override

    public UUID getId() {
        return id;
    }

    @Override
    public void setId(UUID id) {
        this.id = id;
    }



    @Override
    public SparkComputationMetadata toData() {
        SparkComputationMetadata md = new SparkComputationMetadata();
        md.setId(new ComputationId(getId()));
        md.setJarName(this.jarName);
        md.setArgsType(this.argsType);
        md.setJarPath(this.jarPath);
        md.setMainClass(this.mainClass);
        md.setArgsformat(this.argsFormat);
        md.setJsonDescriptor(this.jsonDescriptor);
        return md;
    }

}
