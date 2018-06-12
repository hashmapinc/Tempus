package com.hashmapinc.server.dao.sql.DataModelObject;

import com.hashmapinc.server.common.data.DataModelObject.AttributeDefinition;
import com.hashmapinc.server.common.data.UUIDConverter;
import com.hashmapinc.server.common.data.id.DataModelObjectId;
import com.hashmapinc.server.dao.DaoUtil;
import com.hashmapinc.server.dao.datamodelobject.AttributeDefinitionDao;
import com.hashmapinc.server.dao.model.sql.AttributeDefinitionEntity;
import com.hashmapinc.server.dao.sql.JpaAbstractDao;
import com.hashmapinc.server.dao.util.SqlDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@SqlDao
public class JpaAttributeDefinitionDao extends JpaAbstractDao<AttributeDefinitionEntity, AttributeDefinition> implements AttributeDefinitionDao{

    @Autowired
    AttributeDefinitionRepository attributeDefinitionRepository;

    @Override
    protected Class<AttributeDefinitionEntity> getEntityClass() {
        return AttributeDefinitionEntity.class;
    }

    @Override
    protected CrudRepository<AttributeDefinitionEntity, String> getCrudRepository() {
        return attributeDefinitionRepository;
    }

    @Override
    public List<AttributeDefinition> findByDataModelObjectId(DataModelObjectId dataModelObjectId) {
        List<AttributeDefinitionEntity> entities = attributeDefinitionRepository.findByDataModelObjectId(UUIDConverter.fromTimeUUID(dataModelObjectId.getId()));
        return DaoUtil.convertDataList(entities);
    }

    @Override
    public boolean deleteById(UUID id) {
        return super.removeById(id);
    }
}
