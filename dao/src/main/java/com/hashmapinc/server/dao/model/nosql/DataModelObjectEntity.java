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
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.fasterxml.jackson.databind.JsonNode;
import com.hashmapinc.server.common.data.DataModelObject.DataModelObject;
import com.hashmapinc.server.common.data.id.*;
import com.hashmapinc.server.dao.model.ModelConstants;
import com.hashmapinc.server.dao.model.type.JsonCodec;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import com.hashmapinc.server.dao.model.SearchTextEntity;

import com.datastax.driver.mapping.annotations.*;
import java.util.UUID;

import static com.hashmapinc.server.dao.model.ModelConstants.ID_PROPERTY;

@ToString
@EqualsAndHashCode
@Table(name = ModelConstants.DATA_MODEL_OBJECT_CF)
public final class DataModelObjectEntity implements SearchTextEntity<DataModelObject> {

    @PartitionKey
    @Column(name = ID_PROPERTY)
    private UUID id;

    @Column(name = ModelConstants.DATA_MODEL_OBJECT_TENANT_ID_PROPERTY)
    private UUID tenantId;

    @Column(name = ModelConstants.DATA_MODEL_OBJECT_PARENT_ID)
    private UUID parentId;

    @Column(name = ModelConstants.DATA_MODEL_OBJECT_NAME_PROPERTY)
    private String name;

    @Column(name = ModelConstants.SEARCH_TEXT_PROPERTY)
    private String searchText;

    @Column(name = ModelConstants.DATA_MODEL_ID)
    private UUID dataModelId;

    @Column(name = ModelConstants.CUSTOMER_ID_PROPERTY)
    private UUID customerId;

    @Column(name = ModelConstants.DATA_MODEL_OBJECT_DESCRIPTION, codec = JsonCodec.class)
    private JsonNode description;

    public DataModelObjectEntity() {
        super();
    }

    public DataModelObjectEntity(DataModelObject dataModelObject) {
        if (dataModelObject.getId() != null) {
            this.setId(dataModelObject.getId().getId());
        }
        if (dataModelObject.getTenantId() != null) {
            this.tenantId = dataModelObject.getTenantId().getId();
        }
        if (dataModelObject.getDataModelId()!= null) {
            this.dataModelId = dataModelObject.getDataModelId().getId();
        }
        if (dataModelObject.getParentId()!= null) {
            this.parentId = dataModelObject.getParentId().getId();
        }
        if (dataModelObject.getParentId()!= null) {
            this.customerId = dataModelObject.getParentId().getId();
        }
        this.name = dataModelObject.getName();
        this.description = dataModelObject.getDecription();
    }

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getTenantId() {
        return tenantId;
    }

    public void setTenantId(UUID tenantId) {
        this.tenantId = tenantId;
    }

    public UUID getParentId() {
        return parentId;
    }

    public void setParentId(UUID parentId) {
        this.parentId = parentId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public UUID getDataModelId() {
        return dataModelId;
    }

    public void setDataModelId(UUID dataModelId) {
        this.dataModelId = dataModelId;
    }

    public JsonNode getDescription() {
        return description;
    }

    public void setDescription(JsonNode description) {
        this.description = description;
    }

    public UUID getCustomerId() {
        return customerId;
    }

    public void setCustomerId(UUID customerId) {
        this.customerId = customerId;
    }

    @Override
    public String getSearchTextSource() {
        return name;
    }

    @Override
    public void setSearchText(String searchText) {
        this.searchText = searchText;
    }

    public String getSearchText() {
        return searchText;
    }

    @Override
    public DataModelObject toData() {
        DataModelObject dataModelObject = new DataModelObject(new DataModelObjectId(id));
        dataModelObject.setCreatedTime(UUIDs.unixTimestamp(id));
        if (tenantId != null) {
            dataModelObject.setTenantId(new TenantId(tenantId));
        }
        if (dataModelId != null) {
            dataModelObject.setDataModelId(new DataModelId(dataModelId));
        }
        if (customerId != null) {
            dataModelObject.setCustomerId(new CustomerId(customerId));
        }
        if (parentId != null){
            dataModelObject.setParentId(new DataModelObjectId(parentId));
        }
        dataModelObject.setName(name);
        dataModelObject.setDecription(description);
        return dataModelObject;
    }

}