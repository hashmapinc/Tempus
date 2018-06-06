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
package com.hashmapinc.server.common.data;

import com.fasterxml.jackson.databind.JsonNode;
import com.hashmapinc.server.common.data.id.*;

public class ModelObject extends SearchTextBased<ModelObjectId> implements HasName {
    private String name;
    private JsonNode decription;
    private ModelId modelId;
    private TenantId tenantId;
    private ModelObjectId parentId;
    private CustomerId customerId;

    public ModelObject() {
        super();
    }

    public ModelObject(ModelObjectId id) {
        super(id);
    }

    public ModelObject(ModelObject modelObject) {
        super(modelObject);
        this.name = modelObject.name;
        this.decription = modelObject.decription;
        this.modelId = modelObject.modelId;
        this.tenantId = modelObject.tenantId;
        this.parentId = modelObject.parentId;
        this.customerId = modelObject.customerId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        ModelObject that = (ModelObject) o;
         if (name != null ? !name.equals(that.name) : that.name != null) return false;
         if (decription != null ? !decription.equals(that.decription) : that.decription != null) return false;
         if (modelId != null ? !modelId.equals(that.modelId) : that.modelId != null) return false;
         if (tenantId != null ? !tenantId.equals(that.tenantId) : that.tenantId != null) return false;
         if (parentId != null ? !parentId.equals(that.parentId) : that.parentId != null) return false;
         if (customerId != null ? !customerId.equals(that.customerId) : that.customerId != null) return false;
        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (decription != null ? decription.hashCode() : 0);
        result = 31 * result + (modelId != null ? modelId.hashCode() : 0);
        result = 31 * result + (tenantId != null ? tenantId.hashCode() : 0);
        result = 31 * result + (parentId != null ? parentId.hashCode() : 0);
        result = 31 * result + (customerId != null ? customerId.hashCode() : 0);
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

    public ModelId getModelId() {
        return modelId;
    }

    public void setModelId(ModelId modelId) {
        this.modelId = modelId;
    }

    public TenantId getTenantId() {
        return tenantId;
    }

    public void setTenantId(TenantId tenantId) {
        this.tenantId = tenantId;
    }

    public ModelObjectId getParentId() {
        return parentId;
    }

    public void setParentId(ModelObjectId parentId) {
        this.parentId = parentId;
    }

    public CustomerId getCustomerId() {
        return customerId;
    }

    public void setCustomerId(CustomerId customerId) {
        this.customerId = customerId;
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
