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
import com.hashmapinc.server.common.data.ModelObject;
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
@Table(name = ModelConstants.MODEL_OBJECT_CF)
public final class ModelObjectEntity  implements SearchTextEntity<ModelObject> {

    @PartitionKey
    @com.datastax.driver.mapping.annotations.Column(name = ID_PROPERTY)
    private UUID id;

    @Column(name = ModelConstants.MODEL_OBJECT_TENANT_ID_PROPERTY)
    private UUID tenantId;

    @Column(name = ModelConstants.MODEL_OBJECT_PARENT_ID)
    private UUID parentId;

    @Column(name = ModelConstants.MODEL_OBJECT_NAME_PROPERTY)
    private String name;

    @Column(name = ModelConstants.SEARCH_TEXT_PROPERTY)
    private String searchText;

    @Column(name = ModelConstants.MODEL_ID)
    private UUID modelId;

    @Column(name = ModelConstants.CUSTOMER_ID_PROPERTY)
    private UUID customerId;

    @Column(name = ModelConstants.MODEL_OBJECT_DESCRIPTION, codec = JsonCodec.class)
    private JsonNode description;

    public ModelObjectEntity() {
        super();
    }

    public ModelObjectEntity(ModelObject modelObject) {
        if (modelObject.getId() != null) {
            this.setId(modelObject.getId().getId());
        }
        if (modelObject.getTenantId() != null) {
            this.tenantId = modelObject.getTenantId().getId();
        }
        if (modelObject.getModelId()!= null) {
            this.modelId = modelObject.getModelId().getId();
        }
        if (modelObject.getParentId()!= null) {
            this.parentId = modelObject.getParentId().getId();
        }
        if (modelObject.getParentId()!= null) {
            this.customerId = modelObject.getParentId().getId();
        }
        this.name = modelObject.getName();
        this.description = modelObject.getDecription();
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

    public UUID getModelId() {
        return modelId;
    }

    public void setModelId(UUID modelId) {
        this.modelId = modelId;
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
    public ModelObject toData() {
        ModelObject modelObject = new ModelObject(new ModelObjectId(id));
        modelObject.setCreatedTime(UUIDs.unixTimestamp(id));
        if (tenantId != null) {
            modelObject.setTenantId(new TenantId(tenantId));
        }
        if (modelId != null) {
            modelObject.setModelId(new ModelId(modelId));
        }
        if (customerId != null) {
            modelObject.setCustomerId(new CustomerId(customerId));
        }
        if (parentId != null){
            modelObject.setParentId(new ModelObjectId(parentId));
        }
        modelObject.setName(name);
        modelObject.setDecription(description);
        return modelObject;
    }

}