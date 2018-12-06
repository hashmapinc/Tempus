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
package com.hashmapinc.server.common.data.datamodel;

import com.hashmapinc.server.common.data.id.DataModelObjectId;
import lombok.ToString;

import java.io.Serializable;

@ToString
public class AttributeDefinition implements Serializable{

    private static final long serialVersionUID = -4403266614883680756L;
    
    private String name;
    private String value;
    private String valueType;
    private DataModelObjectId dataModelObjectId;
    private String source;
    private boolean keyAttribute;

    public AttributeDefinition(){

    }

    public AttributeDefinition(AttributeDefinition attributeDefinition){
        this.dataModelObjectId = attributeDefinition.dataModelObjectId;
        this.name = attributeDefinition.name;
        this.source = attributeDefinition.source;
        this.value = attributeDefinition.value;
        this.valueType = attributeDefinition.valueType;
        this.keyAttribute = attributeDefinition.keyAttribute;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        AttributeDefinition that = (AttributeDefinition) o;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (dataModelObjectId != null ? !dataModelObjectId.equals(that.dataModelObjectId) : that.dataModelObjectId != null) return false;
        if (source != null ? !source.equals(that.source) : that.source != null) return false;
        if (value != null ? !value.equals(that.value) : that.value != null) return false;
        if (keyAttribute != that.keyAttribute) return false;
        return (valueType != null ? valueType.equals(that.valueType) : that.valueType == null);

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (dataModelObjectId != null ? dataModelObjectId.hashCode() : 0);
        result = 31 * result + (source != null ? source.hashCode() : 0);
        result = 31 * result + (valueType != null ? valueType.hashCode() : 0);
        result = 31 * result + (value != null ? value.hashCode() : 0);
        return result;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getValueType() {
        return valueType;
    }

    public void setValueType(String valueType) {
        this.valueType = valueType;
    }

    public DataModelObjectId getDataModelObjectId() {
        return dataModelObjectId;
    }

    public void setDataModelObjectId(DataModelObjectId dataModelObjectId) {
        this.dataModelObjectId = dataModelObjectId;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public boolean isKeyAttribute() {
        return keyAttribute;
    }

    public void setKeyAttribute(boolean keyAttribute) {
        this.keyAttribute = keyAttribute;
    }
}
