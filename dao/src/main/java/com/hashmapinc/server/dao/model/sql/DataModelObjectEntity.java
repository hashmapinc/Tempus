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
import com.hashmapinc.server.common.data.datamodel.DataModelObject;
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
import java.nio.ByteBuffer;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@TypeDef(name = "json", typeClass = JsonStringType.class)
@Table(name = ModelConstants.DATA_MODEL_OBJECT_TABLE)
public final class DataModelObjectEntity extends BaseSqlEntity<DataModelObject> implements SearchTextEntity<DataModelObject> {

    @Column(name = ModelConstants.DATA_MODEL_OBJECT_TYPE)
    private String type;

    @Column(name = ModelConstants.DATA_MODEL_OBJECT_PARENT_ID)
    private String parentId;

    @Column(name = ModelConstants.CUSTOMER_ID_PROPERTY)
    private String customerId;

    @Column(name = ModelConstants.DATA_MODEL_OBJECT_NAME_PROPERTY)
    private String name;

    @Column(name = ModelConstants.SEARCH_TEXT_PROPERTY)
    private String searchText;

    @Column(name = ModelConstants.DATA_MODEL_ID)
    private String dataModelId;

    @Column(name = ModelConstants.DATA_MODEL_LOGO_FILE)
    private String logoFile;

    @Type(type = "json")
    @Column(name = ModelConstants.DATA_MODEL_OBJECT_DESCRIPTION)
    private JsonNode description;

    public DataModelObjectEntity() {
        super();
    }

    public DataModelObjectEntity(DataModelObject dataModelObject) {
        if (dataModelObject.getId() != null) {
            this.setId(dataModelObject.getId().getId());
        }
        if (dataModelObject.getType() != null) {
            this.type = dataModelObject.getType();
        }
        if (dataModelObject.getDataModelId()!= null) {
            this.dataModelId = UUIDConverter.fromTimeUUID(dataModelObject.getDataModelId().getId());
        }
        if (dataModelObject.getParentId()!= null) {
            this.parentId = UUIDConverter.fromTimeUUID(dataModelObject.getParentId().getId());
        }
        if (dataModelObject.getCustomerId()!= null) {
            this.customerId = UUIDConverter.fromTimeUUID(dataModelObject.getCustomerId().getId());
        }
        this.logoFile = dataModelObject.getLogoFile();
        this.name = dataModelObject.getName();
        this.description = dataModelObject.getDescription();
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
        DataModelObject dataModelObject = new DataModelObject(new DataModelObjectId(UUIDConverter.fromString(id)));
        dataModelObject.setCreatedTime(UUIDs.unixTimestamp(UUIDConverter.fromString(id)));
        if (type != null) {
            dataModelObject.setType(type);
        }
        if (dataModelId != null) {
            dataModelObject.setDataModelId(new DataModelId(UUIDConverter.fromString(dataModelId)));
        }
        if (customerId != null){
            dataModelObject.setCustomerId(new CustomerId(UUIDConverter.fromString(customerId)));
        }
        if (parentId != null){
            dataModelObject.setParentId(new DataModelObjectId(UUIDConverter.fromString(parentId)));
        }
        dataModelObject.setLogoFile(logoFile);
        dataModelObject.setName(name);
        dataModelObject.setDescription(description);
        return dataModelObject;
    }

}