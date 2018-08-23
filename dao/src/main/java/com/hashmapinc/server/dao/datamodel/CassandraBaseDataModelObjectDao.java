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
package com.hashmapinc.server.dao.datamodel;

import com.datastax.driver.core.querybuilder.Select;
import com.hashmapinc.server.common.data.datamodel.DataModelObject;
import com.hashmapinc.server.common.data.id.DataModelId;
import com.hashmapinc.server.common.data.id.DataModelObjectId;
import com.hashmapinc.server.dao.DaoUtil;
import com.hashmapinc.server.dao.model.ModelConstants;
import com.hashmapinc.server.dao.model.nosql.DataModelObjectEntity;
import com.hashmapinc.server.dao.nosql.CassandraAbstractSearchTextDao;
import com.hashmapinc.server.dao.util.NoSqlDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.datastax.driver.core.querybuilder.QueryBuilder.eq;
import static com.datastax.driver.core.querybuilder.QueryBuilder.select;

@Service
@Slf4j
@NoSqlDao
public class CassandraBaseDataModelObjectDao extends CassandraAbstractSearchTextDao<DataModelObjectEntity, DataModelObject> implements DataModelObjectDao {

    @Override
    protected Class<DataModelObjectEntity> getColumnFamilyClass() {
        return DataModelObjectEntity.class;
    }

    @Override
    protected String getColumnFamilyName() {
        return ModelConstants.DATA_MODEL_OBJECT_CF;
    }

    @Override
    public DataModelObject findById(DataModelObjectId id) {
        return super.findById(id.getId());
    }

    @Override
    public List<DataModelObject> findByDataModelId(DataModelId dataModelId) {
        Select select = select().from(ModelConstants.DATA_MODEL_OBJECT_CF).allowFiltering();
        Select.Where query = select.where();
        query.and(eq(ModelConstants.DATA_MODEL_ID, dataModelId.getId()));
        List<DataModelObjectEntity> entities = findListByStatement(query);
        return DaoUtil.convertDataList(entities);
    }

    @Override
    public DataModelObject findByDataModeIdAndName(DataModelObject dataModelObject) {
        Select select = select().from(ModelConstants.DATA_MODEL_OBJECT_CF).allowFiltering();
        Select.Where query = select.where();
        query.and(eq(ModelConstants.DATA_MODEL_OBJECT_NAME_PROPERTY, dataModelObject.getName()))
                .and(eq(ModelConstants.DATA_MODEL_ID, dataModelObject.getDataModelId().getId()));
        return DaoUtil.getData(findOneByStatement(query));
    }
}
