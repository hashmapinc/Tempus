package com.hashmapinc.server.dao.datamodelobject;

import com.hashmapinc.server.common.data.DataModelObject.AttributeDefinition;
import com.hashmapinc.server.common.data.id.DataModelObjectId;

import java.util.List;
import java.util.UUID;

public interface AttributeDefinitionService {
    AttributeDefinition save(AttributeDefinition attributeDefinition);
    AttributeDefinition findById(UUID id);
    List<AttributeDefinition> findByDataModelObjectId(DataModelObjectId dataModelObjectId);
    boolean deleteById(UUID id);
}
