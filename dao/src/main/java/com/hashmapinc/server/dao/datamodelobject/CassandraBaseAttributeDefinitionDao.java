package com.hashmapinc.server.dao.datamodelobject;

import com.datastax.driver.core.querybuilder.Select;
import com.hashmapinc.server.common.data.DataModelObject.AttributeDefinition;
import com.hashmapinc.server.common.data.id.DataModelObjectId;
import com.hashmapinc.server.dao.DaoUtil;
import com.hashmapinc.server.dao.model.ModelConstants;
import com.hashmapinc.server.dao.model.nosql.AttributeDefinitionEntity;
import com.hashmapinc.server.dao.nosql.CassandraAbstractModelDao;
import com.hashmapinc.server.dao.util.NoSqlDao;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

import static com.datastax.driver.core.querybuilder.QueryBuilder.eq;
import static com.datastax.driver.core.querybuilder.QueryBuilder.select;

@Service
@NoSqlDao
public class CassandraBaseAttributeDefinitionDao extends CassandraAbstractModelDao<AttributeDefinitionEntity, AttributeDefinition> implements AttributeDefinitionDao{

    @Override
    protected Class getColumnFamilyClass() {
        return AttributeDefinitionEntity.class;
    }

    @Override
    protected String getColumnFamilyName() {
        return ModelConstants.ATTRIBUTE_DEFINITION_COLUMN_FAMILY_NAME;
    }


    @Override
    public List<AttributeDefinition> findByDataModelObjectId(DataModelObjectId dataModelObjectId) {
        Select select = select().from(ModelConstants.ATTRIBUTE_DEFINITION_COLUMN_FAMILY_NAME).allowFiltering();
        Select.Where query = select.where();
        query.and(eq(ModelConstants.ATTRIBUTE_DEFINITION_MODEL_OBJECT_ID, dataModelObjectId.getId()));
        List<AttributeDefinitionEntity> entities = findListByStatement(query);
        return DaoUtil.convertDataList(entities);
    }

    @Override
    public boolean deleteById(UUID id) {
        return super.removeById(id);
    }
}
