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

import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;
import com.hashmapinc.server.common.data.DataModelObject.AttributeDefinition;
import com.hashmapinc.server.common.data.id.DataModelObjectId;
import com.hashmapinc.server.dao.model.BaseEntity;
import com.hashmapinc.server.dao.model.ModelConstants;

import java.util.UUID;

@Table(name = ModelConstants.ATTRIBUTE_DEFINITION_COLUMN_FAMILY_NAME)
public class AttributeDefinitionEntity implements BaseEntity<AttributeDefinition> {

    @PartitionKey(value = 0)
    @Column(name = ModelConstants.ID_PROPERTY)
    private UUID id;

    @Column(name = ModelConstants.ATTRIBUTE_DEFINITION_NAME)
    private String name;

    @Column(name = ModelConstants.ATTRIBUTE_DEFINITION_VALUE)
    private String value;

    @Column(name = ModelConstants.ATTRIBUTE_DEFINITION_VALUE_TYPE)
    private String valueType;

    @Column(name = ModelConstants.ATTRIBUTE_DEFINITION_MODEL_OBJECT_ID)
    private UUID modelObjectId;

    @Column(name = ModelConstants.ATTRIBUTE_DEFINITION_SOURCE)
    private String source;

    public AttributeDefinitionEntity(){
        super();
    }

    public AttributeDefinitionEntity(AttributeDefinition attributeDefinition){
        if (attributeDefinition.getId() != null) {
            this.setId(attributeDefinition.getId());
        }
        if (attributeDefinition.getDataModelObjectId() !=null ) {
            this.modelObjectId = attributeDefinition.getDataModelObjectId().getId();
        }

        this.name = attributeDefinition.getName();
        this.source = attributeDefinition.getSource();
        this.value = attributeDefinition.getValue();
        this.valueType = attributeDefinition.getValueType();
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
    public AttributeDefinition toData() {
        AttributeDefinition attributeDefinition = new AttributeDefinition();
        if (id != null ){
            attributeDefinition.setId(id);
        }
        if (value != null ){
            attributeDefinition.setValue(value);
        }
        if (name != null ){
            attributeDefinition.setName(name);
        }
        if (source != null ){
            attributeDefinition.setSource(source);
        }
        if (valueType != null ){
            attributeDefinition.setValueType(valueType);
        }
        if (modelObjectId != null ){
            attributeDefinition.setDataModelObjectId(new DataModelObjectId(modelObjectId));
        }
        return attributeDefinition;
    }
}