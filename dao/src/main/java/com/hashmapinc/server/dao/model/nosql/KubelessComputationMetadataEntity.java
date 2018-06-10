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
import com.hashmapinc.server.common.data.computation.KubelessComputationMetadata;
import com.hashmapinc.server.common.data.computation.Runtimes;
import com.hashmapinc.server.common.data.id.ComputationId;
import com.hashmapinc.server.dao.model.ModelConstants;

import java.util.UUID;

import static com.hashmapinc.server.dao.model.ModelConstants.ID_PROPERTY;

@Table(name = ModelConstants.KUBELESS_COMPUTATIONS_META_DATA_COLUMN_FAMILY)
public class KubelessComputationMetadataEntity extends ComputationMetadataEntity<KubelessComputationMetadata> {

    @PartitionKey(value = 0)
    @Column(name = ID_PROPERTY)
    private UUID id;

    @Column(name = ModelConstants.KUBELESS_COMPUTATION_NAMESPACE_PROPERTY)
    private String namespace;

    @Column(name = ModelConstants.KUBELESS_COMPUTATION_FUNCTION_PROPERTY)
    private String function;

    @Column(name = ModelConstants.KUBELESS_COMPUTATION_HANDLER_PROPERTY)
    private String handler;

    @Column(name = ModelConstants.KUBELESS_COMPUTATION_RUNTIME_PROPERTY)
    private String runtime;

    @Column(name = ModelConstants.KUBELESS_COMPUTATION_DEPENDENCIES_PROPERTY)
    private String dependencies;

    @Column(name = ModelConstants.KUBELESS_COMPUTATION_FUNC_TYPE_PROPERTY)
    private String functionContentType;

    @Column(name = ModelConstants.KUBELESS_COMPUTATION_CHECKSUM_PROPERTY)
    private String checksum;

    @Column(name = ModelConstants.KUBELESS_COMPUTATION_TIMEOUT_PROPERTY)
    private String timeout;


    public KubelessComputationMetadataEntity(){
        super();
    }

    public KubelessComputationMetadataEntity(KubelessComputationMetadata md){
        if(md.getId() != null){
            this.setId(md.getId().getId());
        }
        if(md.getFunction() != null){
            this.function = md.getFunction();
        }
        if(md.getDependencies() != null){
            this.dependencies = md.getDependencies();
        }
        if(md.getHandler() != null){
            this.handler = md.getHandler();
        }
        if(md.getNamespace() != null){
            this.namespace = md.getNamespace();
        }
        if(md.getRuntime() != null){
            this.runtime = md.getRuntime().name();
        }
        if(md.getFunctionContentType() != null){
            this.functionContentType = md.getFunctionContentType();
        }
        if(md.getChecksum() != null)
        {
            this.checksum = md.getChecksum();
        }
        if(md.getTimeout() != null){
            this.timeout = md.getTimeout();
        }
    }

    @Override
    public UUID getId() {
        return id;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getFunction() {
        return function;
    }

    public void setFunction(String function) {
        this.function = function;
    }

    public String getHandler() {
        return handler;
    }

    public void setHandler(String handler) {
        this.handler = handler;
    }

    public String getRuntime() {
        return runtime;
    }

    public void setRuntime(String runtime) {
        this.runtime = runtime;
    }

    public String getDependencies() {
        return dependencies;
    }

    public void setDependencies(String dependencies) {
        this.dependencies = dependencies;
    }

    public String getFunctionContentType() {
        return functionContentType;
    }

    public void setFunctionContentType(String functionContentType) {
        this.functionContentType = functionContentType;
    }

    public String getChecksum() {
        return checksum;
    }

    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }

    public String getTimeout() {
        return timeout;
    }

    public void setTimeout(String timeout) {
        this.timeout = timeout;
    }

    @Override
    public void setId(UUID id) {
        this.id = id;
    }



    @Override
    public KubelessComputationMetadata toData() {
        KubelessComputationMetadata md = new KubelessComputationMetadata();
        md.setId(new ComputationId(getId()));
        md.setDependencies(this.dependencies);
        md.setFunction(this.function);
        md.setNamespace(this.namespace);
        md.setRuntime(Runtimes.valueOf(this.runtime));
        md.setHandler(this.handler);
        md.setFunctionContentType(this.functionContentType);
        md.setChecksum(this.checksum);
        md.setTimeout(this.timeout);

        return md;
    }
}
