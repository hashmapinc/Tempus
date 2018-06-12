package com.hashmapinc.server.dao.sql.DataModelObject;

import com.hashmapinc.server.dao.model.sql.AttributeDefinitionEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AttributeDefinitionRepository extends CrudRepository<AttributeDefinitionEntity, String> {
    List<AttributeDefinitionEntity> findByDataModelObjectId(@Param("dataModelObjectId") String dataModelObjectId);
}
