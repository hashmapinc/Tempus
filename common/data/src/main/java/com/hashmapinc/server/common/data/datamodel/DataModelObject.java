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
package com.hashmapinc.server.common.data.datamodel;

import com.fasterxml.jackson.databind.JsonNode;
import com.hashmapinc.server.common.data.HasName;
import com.hashmapinc.server.common.data.SearchTextBased;
import com.hashmapinc.server.common.data.id.*;

import java.util.ArrayList;
import java.util.List;

public class DataModelObject extends SearchTextBased<DataModelObjectId> implements HasName {
    private String name;
    private JsonNode decription;
    private DataModelId dataModelId;
    private String type;
    private DataModelObjectId parentId;
    private CustomerId customerId;
    private List<AttributeDefinition> attributeDefinitions = new ArrayList<AttributeDefinition>();

    public DataModelObject() {
        super();
    }

    public DataModelObject(DataModelObjectId id) {
        super(id);
    }

    public DataModelObject(DataModelObject dataModelObject) {
        super(dataModelObject);
        this.name = dataModelObject.name;
        this.decription = dataModelObject.decription;
        this.dataModelId = dataModelObject.dataModelId;
        this.type = dataModelObject.type;
        this.parentId = dataModelObject.parentId;
        this.customerId = dataModelObject.customerId;
        this.attributeDefinitions = dataModelObject.attributeDefinitions;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        DataModelObject that = (DataModelObject) o;
         if (name != null ? !name.equals(that.name) : that.name != null) return false;
         if (decription != null ? !decription.equals(that.decription) : that.decription != null) return false;
         if (dataModelId != null ? !dataModelId.equals(that.dataModelId) : that.dataModelId != null) return false;
         if (type != null ? !type.equals(that.type) : that.type != null) return false;
         if (parentId != null ? !parentId.equals(that.parentId) : that.parentId != null) return false;
         if (customerId != null ? !customerId.equals(that.customerId) : that.customerId != null) return false;
         if (attributeDefinitions != null ? !attributeDefinitions.equals(that.attributeDefinitions) : that.attributeDefinitions != null) return false;
        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (decription != null ? decription.hashCode() : 0);
        result = 31 * result + (dataModelId != null ? dataModelId.hashCode() : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (parentId != null ? parentId.hashCode() : 0);
        result = 31 * result + (customerId != null ? customerId.hashCode() : 0);
        result = 31 * result + (attributeDefinitions != null ? attributeDefinitions.hashCode() : 0);
        return result;
    }

    public void setName(String name) {
        this.name = name;
    }

    public JsonNode getDecription() {
        return decription;
    }

    public void setDecription(JsonNode decription) {
        this.decription = decription;
    }

    public DataModelId getDataModelId() {
        return dataModelId;
    }

    public void setDataModelId(DataModelId dataModelId) {
        this.dataModelId = dataModelId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public DataModelObjectId getParentId() {
        return parentId;
    }

    public void setParentId(DataModelObjectId parentId) {
        this.parentId = parentId;
    }

    public CustomerId getCustomerId() {
        return customerId;
    }

    public void setCustomerId(CustomerId customerId) {
        this.customerId = customerId;
    }

    public List<AttributeDefinition> getAttributeDefinitions() {
        return attributeDefinitions;
    }

    public void setAttributeDefinitions(List<AttributeDefinition> attributeDefinitions) {
        this.attributeDefinitions = attributeDefinitions;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getSearchText() {
        return name;
    }

}
