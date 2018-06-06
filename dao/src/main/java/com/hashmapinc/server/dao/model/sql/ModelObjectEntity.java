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
import com.hashmapinc.server.common.data.ModelObject;
import com.hashmapinc.server.common.data.id.*;
import com.hashmapinc.server.dao.model.BaseSqlEntity;
import com.hashmapinc.server.dao.model.ModelConstants;
import com.hashmapinc.server.dao.util.mapping.JsonStringType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import com.hashmapinc.server.common.data.UUIDConverter;
import com.hashmapinc.server.dao.model.SearchTextEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@TypeDef(name = "json", typeClass = JsonStringType.class)
@Table(name = ModelConstants.MODEL_OBJECT_TABLE)
public final class ModelObjectEntity extends BaseSqlEntity<ModelObject> implements SearchTextEntity<ModelObject> {

    @Column(name = ModelConstants.MODEL_OBJECT_TENANT_ID_PROPERTY)
    private String tenantId;

    @Column(name = ModelConstants.MODEL_OBJECT_PARENT_ID)
    private String parentId;

    @Column(name = ModelConstants.CUSTOMER_ID_PROPERTY)
    private String customerId;

    @Column(name = ModelConstants.MODEL_OBJECT_NAME_PROPERTY)
    private String name;

    @Column(name = ModelConstants.SEARCH_TEXT_PROPERTY)
    private String searchText;

    @Column(name = ModelConstants.MODEL_ID)
    private String modelId;

    @Type(type = "json")
    @Column(name = ModelConstants.MODEL_OBJECT_DESCRIPTION)
    private JsonNode description;

    public ModelObjectEntity() {
        super();
    }

    public ModelObjectEntity(ModelObject modelObject) {
        if (modelObject.getId() != null) {
            this.setId(modelObject.getId().getId());
        }
        if (modelObject.getTenantId() != null) {
            this.tenantId = UUIDConverter.fromTimeUUID(modelObject.getTenantId().getId());
        }
        if (modelObject.getModelId()!= null) {
            this.modelId = UUIDConverter.fromTimeUUID(modelObject.getModelId().getId());
        }
        if (modelObject.getParentId()!= null) {
            this.parentId = UUIDConverter.fromTimeUUID(modelObject.getParentId().getId());
        }
        if (modelObject.getCustomerId()!= null) {
            this.customerId = UUIDConverter.fromTimeUUID(modelObject.getCustomerId().getId());
        }
        this.name = modelObject.getName();
        this.description = modelObject.getDecription();
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
        ModelObject modelObject = new ModelObject(new ModelObjectId(UUIDConverter.fromString(id)));
        modelObject.setCreatedTime(UUIDs.unixTimestamp(UUIDConverter.fromString(id)));
        if (tenantId != null) {
            modelObject.setTenantId(new TenantId(UUIDConverter.fromString(tenantId)));
        }
        if (modelId != null) {
            modelObject.setModelId(new ModelId(UUIDConverter.fromString(modelId)));
        }
        if (customerId != null){
            modelObject.setCustomerId(new CustomerId(UUIDConverter.fromString(customerId)));
        }
        if (parentId != null){
            modelObject.setParentId(new ModelObjectId(UUIDConverter.fromString(parentId)));
        }
        modelObject.setName(name);
        modelObject.setDecription(description);
        return modelObject;
    }

}