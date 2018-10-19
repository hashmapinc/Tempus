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
package com.hashmapinc.server.dao.model.nosql;

import com.datastax.driver.core.utils.UUIDs;
import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;
import com.datastax.driver.mapping.annotations.Transient;
import com.fasterxml.jackson.databind.JsonNode;
import com.hashmapinc.server.common.data.computation.ComputationJob;
import com.hashmapinc.server.common.data.computation.ComputationJobConfiguration;
import com.hashmapinc.server.common.data.id.ComputationId;
import com.hashmapinc.server.common.data.id.ComputationJobId;
import com.hashmapinc.server.common.data.id.TenantId;
import com.hashmapinc.server.common.data.plugin.ComponentLifecycleState;
import com.hashmapinc.server.dao.model.ModelConstants;
import com.hashmapinc.server.dao.model.SearchTextEntity;
import com.hashmapinc.server.dao.model.type.ComponentLifecycleStateCodec;
import com.hashmapinc.server.dao.model.type.ConfigurationJsonCodec;
import com.hashmapinc.server.dao.model.type.JsonCodec;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.UUID;

import static com.hashmapinc.server.dao.model.ModelConstants.ID_PROPERTY;

@Data
@EqualsAndHashCode
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

    @PartitionKey(value = 2)
    @Column(name = ModelConstants.COMPUTATION_JOB_TENANT_ID_PROPERTY)
    private UUID tenantId;

    @Column(name = ModelConstants.SEARCH_TEXT_PROPERTY)
    private String searchText;

    @Column(name = ModelConstants.COMPUTATION_JOB_STATE_PROPERTY, codec = ComponentLifecycleStateCodec.class)
    private ComponentLifecycleState state;

    @Column(name = ModelConstants.COMPUTATION_JOB_CONFIGURATION, codec = ConfigurationJsonCodec.class)
    private ComputationJobConfiguration configuration;

    @Override
    public String getSearchTextSource() {
        return getJobName();
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
        if(computationJob.getComputationId() != null) {
            this.computationId = computationJob.getComputationId().getId();
        }
        if(computationJob.getTenantId() != null) {
            this.tenantId = computationJob.getTenantId().getId();
        }
        this.state = computationJob.getState();
        this.configuration = computationJob.getConfiguration();
    }

    @Override
    public void setSearchText(String searchText) {
        this.searchText = searchText;
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
    public ComputationJob toData() {
        ComputationJob computationJob = new ComputationJob(new ComputationJobId(getId()));
        computationJob.setCreatedTime(UUIDs.unixTimestamp(getId()));
        computationJob.setName(jobName);
        computationJob.setState(state);
        if(computationId != null) {
            computationJob.setComputationId(new ComputationId(computationId));
        }
        if (tenantId != null) {
            computationJob.setTenantId(new TenantId(tenantId));
        }
        computationJob.setConfiguration(configuration);
        return computationJob;
    }
}
