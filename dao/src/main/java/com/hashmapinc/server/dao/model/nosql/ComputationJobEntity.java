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

import com.datastax.driver.core.utils.UUIDs;
import com.datastax.driver.mapping.annotations.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.hashmapinc.server.common.data.computation.ComputationJob;
import com.hashmapinc.server.common.data.id.ComputationId;
import com.hashmapinc.server.common.data.id.ComputationJobId;
import com.hashmapinc.server.common.data.id.TenantId;
import com.hashmapinc.server.common.data.plugin.ComponentLifecycleState;
import com.hashmapinc.server.dao.model.ModelConstants;
import com.hashmapinc.server.dao.model.SearchTextEntity;
import com.hashmapinc.server.dao.model.type.ComponentLifecycleStateCodec;
import com.hashmapinc.server.dao.model.type.JsonCodec;

import java.util.UUID;

import static com.hashmapinc.server.dao.model.ModelConstants.*;
@Table(name = ModelConstants.COMPUTATION_JOB_COLUMN_FAMILY_NAME)
public class ComputationJobEntity implements SearchTextEntity<ComputationJob> {
    @Transient
    private static final long serialVersionUID = -4673737406462009037L;

    @PartitionKey()
    @Column(name = ID_PROPERTY)
    private UUID id;

    @Column(name = ModelConstants.COMPUTATION_JOB_NAME_PROPERTY)
    private String jobName;

    @PartitionKey(value = 1)
    @Column(name = ModelConstants.COMPUTATION_JOB_COMPUTATION_ID_PROPERTY)
    private UUID computationId;

    @Column(name = ModelConstants.COMPUTATION_JOB_ARG_PRS_PROPERTY, codec = JsonCodec.class)
    private JsonNode argParameters;

    @PartitionKey(value = 2)
    @Column(name = ModelConstants.COMPUTATION_JOB_TENANT_ID_PROPERTY)
    private UUID tenantId;

    @Column(name = ModelConstants.SEARCH_TEXT_PROPERTY)
    private String searchText;

    @Column(name = ModelConstants.COMPUTATION_JOB_ID_PROPERTY)
    private String jobId;

    @Column(name = ModelConstants.COMPUTATION_JOB_STATE_PROPERTY, codec = ComponentLifecycleStateCodec.class)
    private ComponentLifecycleState state;

    @Override
    public String getSearchTextSource() {
        return jobName;
    }

    public ComputationJobEntity() {

    }

    public ComputationJobEntity(ComputationJob computationJob){

        if(computationJob.getId() != null) {
            this.setId(computationJob.getId().getId());
        }
        if(computationJob.getName() != null) {
            this.jobName = computationJob.getName();
        }
        if(computationJob.getArgParameters() != null) {
            this.argParameters = computationJob.getArgParameters();
        }
        if(computationJob.getComputationId() != null) {
            this.computationId = computationJob.getComputationId().getId();
        }
        if(computationJob.getTenantId() != null) {
            this.tenantId = computationJob.getTenantId().getId();
        }
        if(computationJob.getJobId() != null){
            this.jobId = computationJob.getJobId();
        }
        this.state = computationJob.getState();

    }

    @Override
    public void setSearchText(String searchText) {
        this.searchText = searchText;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        ComputationJobEntity that = (ComputationJobEntity) o;
        if (jobName != null ? !jobName.equals(that.jobName) : that.jobName != null) return false;
        if (searchText != null ? !searchText.equals(that.searchText) : that.searchText != null) return false;
        if (argParameters != null ? !argParameters.equals(that.argParameters) : that.argParameters != null) return false;
        if (computationId != null ? !computationId.equals(that.computationId) : that.computationId != null) return false;
        if (tenantId != null ? !tenantId.equals(that.tenantId) : that.tenantId != null) return false;
        if (jobId != null ? !jobId.equals(that.jobId) : that.jobId != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (jobName != null ? jobName.hashCode() : 0);
        result = 31 * result + (searchText != null ? searchText.hashCode() : 0);
        result = 31 * result + (argParameters != null ? argParameters.hashCode() : 0);
        result = 31 * result + (computationId != null ? computationId.hashCode() : 0);
        result = 31 * result + (tenantId != null ? tenantId.hashCode() : 0);
        result = 31 * result + (jobId != null ? jobId.hashCode() : 0);
        return result;
    }

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public void setId(UUID id) {
        this.id = id;
    }


    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public UUID getComputationId() {
        return computationId;
    }

    public void setComputationId(UUID computationId) {
        this.computationId = computationId;
    }

    public JsonNode getArgParameters() {
        return argParameters;
    }

    public void setArgParameters(JsonNode argParameters) {
        this.argParameters = argParameters;
    }

    public UUID getTenantId() {
        return tenantId;
    }

    public void setTenantId(UUID tenantId) {
        this.tenantId = tenantId;
    }

    public String getSearchText() {
        return searchText;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public ComponentLifecycleState getState() {
        return state;
    }

    public void setState(ComponentLifecycleState state) {
        this.state = state;
    }

    @Override
    public ComputationJob toData() {
        ComputationJob computationJob = new ComputationJob(new ComputationJobId(getId()));
        computationJob.setCreatedTime(UUIDs.unixTimestamp(getId()));
        computationJob.setName(jobName);
        computationJob.setArgParameters(argParameters);
        computationJob.setState(state);
        if(computationId != null) {
            computationJob.setComputationId(new ComputationId(computationId));
        }
        if (tenantId != null) {
            computationJob.setTenantId(new TenantId(tenantId));
        }
        computationJob.setJobId(jobId);
        return computationJob;
    }
}
