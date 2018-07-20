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
import com.hashmapinc.server.common.data.computation.ComputationType;
import com.hashmapinc.server.common.data.computation.Computations;
import com.hashmapinc.server.common.data.computation.KubelessComputationMetadata;
import com.hashmapinc.server.common.data.computation.SparkComputationMetadata;
import com.hashmapinc.server.common.data.id.ComputationId;
import com.hashmapinc.server.common.data.id.TenantId;
import com.hashmapinc.server.dao.model.ModelConstants;
import com.hashmapinc.server.dao.model.SearchTextEntity;
import lombok.EqualsAndHashCode;

import java.util.UUID;

import static com.hashmapinc.server.dao.model.ModelConstants.ID_PROPERTY;

@EqualsAndHashCode
@Table(name = ModelConstants.COMPUTATIONS_COLUMN_FAMILY_NAME)
public class ComputationsEntity implements SearchTextEntity<Computations> {
    @Transient
    private static final long serialVersionUID = -4873737406462009036L;

    @PartitionKey
    @Column(name = ID_PROPERTY)
    private UUID id;

    @Column(name = ModelConstants.COMPUTATIONS_NAME_PROPERTY)
    private String name;

    @Column(name = ModelConstants.SEARCH_TEXT_PROPERTY)
    private String searchText;

    @Column(name = ModelConstants.COMPUTATIONS_TYPE)
    private String type;

    @ClusteringColumn
    @Column(name = ModelConstants.COMPUTATIONS_TENANT_ID_PROPERTY)
    private UUID tenantId;

    @Transient
    private ComputationMetadataEntity computationMetadataEntity;

    @Override
    public String getSearchTextSource() {
        return getName();
    }

    public ComputationsEntity() {
        super();
    }

    public ComputationsEntity(Computations computations){
        if(computations.getId() != null) {
            this.setId(computations.getId().getId());
        }
        if(computations.getName() != null) {
            this.name = computations.getName();
        }
        if(computations.getTenantId() != null){
            this.tenantId = computations.getTenantId().getId();
        }
        if(computations.getType() != null){
            this.type = computations.getType().name();
        }
        if(computations.getComputationMetadata() != null){
            if(computations.getType() == ComputationType.SPARK){
                computationMetadataEntity = new SparkComputationMetadataEntity((SparkComputationMetadata) computations.getComputationMetadata());
            } else if(computations.getType() == ComputationType.KUBELESS){
                computationMetadataEntity = new KubelessComputationMetadataEntity((KubelessComputationMetadata) computations.getComputationMetadata());
            }

        }
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSearchText() {
        return searchText;
    }

    public UUID getTenantId() {
        return tenantId;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public ComputationMetadataEntity getComputationMetadataEntity() {
        return computationMetadataEntity;
    }

    public void setComputationMetadataEntity(ComputationMetadataEntity computationMetadataEntity) {
        this.computationMetadataEntity = computationMetadataEntity;
    }

    public void setTenantId(UUID tenantId) {
        this.tenantId = tenantId;
    }

    @Override
    public Computations toData() {
        Computations computations = new Computations(new ComputationId(getId()));
        computations.setCreatedTime(UUIDs.unixTimestamp(getId()));
        computations.setName(name);
        if (tenantId != null) {
            computations.setTenantId(new TenantId(tenantId));
        }
        computations.setType(ComputationType.valueOf(this.type));
        if(this.type.contentEquals(ComputationType.SPARK.name()) && this.computationMetadataEntity != null)
            computations.setComputationMetadata(((SparkComputationMetadataEntity)this.computationMetadataEntity).toData());
        else if(this.type.contentEquals(ComputationType.KUBELESS.name()) && this.computationMetadataEntity != null)
            computations.setComputationMetadata(((KubelessComputationMetadataEntity)this.computationMetadataEntity).toData());

        return computations;
    }

}
