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
package com.hashmapinc.server.dao.model.sql;

import com.hashmapinc.server.common.data.computation.KubelessComputationMetadata;
import com.hashmapinc.server.common.data.computation.Runtimes;
import com.hashmapinc.server.common.data.computation.SparkComputationMetadata;
import com.hashmapinc.server.common.data.id.ComputationId;
import com.hashmapinc.server.dao.model.ModelConstants;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = ModelConstants.KUBELESS_COMPUTATIONS_META_DATA)
public class KubelessComputationMetadataEntity extends ComputationMetadataEntity<KubelessComputationMetadata> {

    @Column(name = ModelConstants.KUBELESS_COMPUTATION_NAMESPACE)
    private String namespace;

    @Column(name = ModelConstants.KUBELESS_COMPUTATION_FUNCTION)
    private String function;

    @Column(name = ModelConstants.KUBELESS_COMPUTATION_HANDLER)
    private String handler;

    @Column(name = ModelConstants.KUBELESS_COMPUTATION_RUNTIME)
    private String runtime;

    @Column(name = ModelConstants.KUBELESS_COMPUTATION_DEPENDENCIES)
    private String dependencies;

    @Column(name = ModelConstants.KUBELESS_COMPUTATION_FUNC_TYPE)
    private String functionContentType;

    @Column(name = ModelConstants.KUBELESS_COMPUTATION_CHECKSUM)
    private String checksum;

    @Column(name = ModelConstants.KUBELESS_COMPUTATION_TIMEOUT)
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
