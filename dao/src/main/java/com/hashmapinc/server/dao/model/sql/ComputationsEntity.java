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

import com.datastax.driver.core.utils.UUIDs;
import com.fasterxml.jackson.databind.JsonNode;
import com.hashmapinc.server.common.data.computation.*;
import com.hashmapinc.server.common.data.id.ComputationId;
import com.hashmapinc.server.common.data.id.TenantId;
import com.hashmapinc.server.dao.model.BaseSqlEntity;
import com.hashmapinc.server.dao.model.ModelConstants;
import com.hashmapinc.server.dao.model.SearchTextEntity;
import com.hashmapinc.server.dao.util.mapping.JsonStringType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.*;

import static com.hashmapinc.server.common.data.UUIDConverter.fromString;
import static com.hashmapinc.server.common.data.UUIDConverter.fromTimeUUID;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@TypeDef(name = "json", typeClass = JsonStringType.class)
@Table(name = ModelConstants.COMPUTATIONS_TABLE_NAME)
public class ComputationsEntity extends BaseSqlEntity<Computations> implements SearchTextEntity<Computations> {
    @Transient
    private static final long serialVersionUID = -4873737406462009031L;

    @Column(name = ModelConstants.COMPUTATIONS_NAME)
    private String name;

    @Column(name = ModelConstants.SEARCH_TEXT_PROPERTY)
    private String searchText;

    @Column(name = ModelConstants.COMPUTATIONS_TENANT_ID)
    private String tenantId;

    @Column(name = ModelConstants.COMPUTATIONS_TYPE)
    private String type;


    @OneToOne(cascade = CascadeType.ALL)
    @PrimaryKeyJoinColumn
    private ComputationMetadataEntity computationMetadataEntity;

    @Override
    public String getSearchTextSource() {
        return name;
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
            setSearchText(this.name);
        }
        if(computations.getTenantId() != null){
            this.tenantId = fromTimeUUID(computations.getTenantId().getId());
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
    public Computations toData() {
        Computations computations = new Computations(new ComputationId(getId()));
        computations.setCreatedTime(UUIDs.unixTimestamp(getId()));
        computations.setName(name);
        if (tenantId != null) {
            computations.setTenantId(new TenantId(fromString(tenantId)));
        }
        computations.setType(ComputationType.valueOf(this.type));
        if(this.type.contentEquals(ComputationType.SPARK.name()))
            computations.setComputationMetadata(((SparkComputationMetadataEntity)this.computationMetadataEntity).toData());
        else if(this.type.contentEquals(ComputationType.KUBELESS.name()))
            computations.setComputationMetadata(((KubelessComputationMetadataEntity)this.computationMetadataEntity).toData());

        return computations;
    }

}
