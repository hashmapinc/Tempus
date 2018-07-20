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

import com.datastax.driver.core.utils.UUIDs;
import com.fasterxml.jackson.databind.JsonNode;
import com.hashmapinc.server.common.data.computation.ComputationJob;
import com.hashmapinc.server.common.data.id.ComputationId;
import com.hashmapinc.server.common.data.id.ComputationJobId;
import com.hashmapinc.server.common.data.id.TenantId;
import com.hashmapinc.server.common.data.plugin.ComponentLifecycleState;
import com.hashmapinc.server.dao.model.BaseSqlEntity;
import com.hashmapinc.server.dao.util.mapping.JsonStringType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import com.hashmapinc.server.dao.model.ModelConstants;
import com.hashmapinc.server.dao.model.SearchTextEntity;

import javax.persistence.*;

import static com.hashmapinc.server.common.data.UUIDConverter.fromString;
import static com.hashmapinc.server.common.data.UUIDConverter.fromTimeUUID;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@TypeDef(name = "json", typeClass = JsonStringType.class)
@Table(name = ModelConstants.COMPUTATION_JOB_TABLE_NAME)
public class ComputationJobEntity extends BaseSqlEntity<ComputationJob> implements SearchTextEntity<ComputationJob> {
    @Transient
    private static final long serialVersionUID = -4673737406462009031L;

    @Column(name = ModelConstants.COMPUTATION_JOB_NAME)
    private String jobName;

    @Column(name = ModelConstants.COMPUTATION_JOB_COMPUTAION_ID)
    private String computationId;

    @Type(type = "json")
    @Column(name = ModelConstants.COMPUTATION_JOB_ARG_PRS)
    private JsonNode argParameters;

    @Column(name = ModelConstants.COMPUTATION_JOB_TENANT_ID)
    private String tenantId;

    @Column(name = ModelConstants.SEARCH_TEXT_PROPERTY)
    private String searchText;

    @Column(name = ModelConstants.COMPUTATION_JOB_ID)
    private String jobId;

    @Enumerated(EnumType.STRING)
    @Column(name = ModelConstants.COMPUTATION_JOB_STATE)
    private ComponentLifecycleState state;

    @Override
    public String getSearchTextSource() {
        return jobName;
    }

    public ComputationJobEntity() {
        super();
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
            this.computationId = fromTimeUUID(computationJob.getComputationId().getId());
        }
        if(computationJob.getTenantId() != null) {
            this.tenantId = fromTimeUUID(computationJob.getTenantId().getId());
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
    public ComputationJob toData() {
        ComputationJob computationJob = new ComputationJob(new ComputationJobId(getId()));
        computationJob.setCreatedTime(UUIDs.unixTimestamp(getId()));
        computationJob.setName(jobName);
        computationJob.setArgParameters(argParameters);
        computationJob.setState(state);
        if(computationId != null) {
            computationJob.setComputationId(new ComputationId(fromString(computationId)));
        }
        if (tenantId != null) {
            computationJob.setTenantId(new TenantId(fromString(tenantId)));
        }
        computationJob.setJobId(jobId);
        return computationJob;
    }

}
