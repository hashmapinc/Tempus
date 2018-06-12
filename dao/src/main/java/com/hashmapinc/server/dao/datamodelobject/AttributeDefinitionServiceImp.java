package com.hashmapinc.server.dao.datamodelobject;

import com.hashmapinc.server.common.data.DataModelObject.AttributeDefinition;
import com.hashmapinc.server.common.data.DataModelObject.DataModelObject;
import com.hashmapinc.server.common.data.id.DataModelObjectId;
import com.hashmapinc.server.dao.exception.DataValidationException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

import static com.hashmapinc.server.dao.service.Validator.validateId;

@Service
public class AttributeDefinitionServiceImp implements  AttributeDefinitionService{

    public static final String INCORRECT_ATTRIBUTE_DEFINITION_ID = "Incorrect attributeDefinitionId ";

    public static final String INCORRECT_DATA_MODEL_OBJECT_ID = "Incorrect dataModelObjectId ";

    @Autowired
    AttributeDefinitionDao attributeDefinitionDao;

    @Autowired
    DataModelObjectDao dataModelObjectDao;

    @Override
    public AttributeDefinition save(AttributeDefinition attributeDefinition) {
        validateAttributeDefinition(attributeDefinition);
        return attributeDefinitionDao.save(attributeDefinition);
    }

    @Override
    public AttributeDefinition findById(UUID id) {
        validateId(id, INCORRECT_ATTRIBUTE_DEFINITION_ID);
        return attributeDefinitionDao.findById(id);
    }

    @Override
    public List<AttributeDefinition> findByDataModelObjectId(DataModelObjectId dataModelObjectId) {
        validateId(dataModelObjectId, INCORRECT_ATTRIBUTE_DEFINITION_ID);
        return attributeDefinitionDao.findByDataModelObjectId(dataModelObjectId);
    }

    @Override
    public boolean deleteById(UUID id) {
        validateId(id, INCORRECT_ATTRIBUTE_DEFINITION_ID);
        return attributeDefinitionDao.deleteById(id);
    }

    private void validateAttributeDefinition(AttributeDefinition attributeDefinition){
        if (StringUtils.isEmpty(attributeDefinition.getName())) {
            throw new DataValidationException("Attribute name should be specified!");
        }
        if (attributeDefinition.getDataModelObjectId() == null) {
            throw new DataValidationException("Attribute definition should be assigned to a data model object!");
        } else {
            DataModelObject dataModelObject = dataModelObjectDao.findById(attributeDefinition.getDataModelObjectId());
            if (dataModelObject == null) {
                throw new DataValidationException("Attribute definition is referencing to non-existent data model object!");
            }
        }
    }

}
