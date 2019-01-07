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

import com.fasterxml.jackson.databind.JsonNode;
import com.hashmapinc.server.common.data.computation.SparkComputationMetadata;
import com.hashmapinc.server.common.data.id.ComputationId;
import com.hashmapinc.server.common.data.id.UUIDBased;
import com.hashmapinc.server.dao.model.ModelConstants;
import com.hashmapinc.server.dao.util.mapping.JsonStringType;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Slf4j
@EqualsAndHashCode(callSuper = true)
@Entity
@TypeDef(name = "json", typeClass = JsonStringType.class)
@Table(name = ModelConstants.SPARK_COMPUTATIONS_META_DATA)
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

    @Type(type = "json")
    @Column(name = ModelConstants.COMPUTATIONS_DESCRIPTOR)
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
