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
package com.hashmapinc.server.dao.modelobjects;

import com.datastax.driver.core.querybuilder.Select;
import com.hashmapinc.server.common.data.ModelObject;
import com.hashmapinc.server.common.data.id.ModelObjectId;
import com.hashmapinc.server.common.data.id.TenantId;
import com.hashmapinc.server.dao.DaoUtil;
import com.hashmapinc.server.dao.model.ModelConstants;
import com.hashmapinc.server.dao.model.nosql.ComputationsEntity;
import com.hashmapinc.server.dao.model.nosql.ModelObjectEntity;
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
public class CassandraBaseModelObjectDao extends CassandraAbstractSearchTextDao<ModelObjectEntity, ModelObject> implements ModelObjectDao {

    @Override
    protected Class<ModelObjectEntity> getColumnFamilyClass() {
        return ModelObjectEntity.class;
    }

    @Override
    protected String getColumnFamilyName() {
        return ModelConstants.MODEL_OBJECT_CF;
    }

    @Override
    public ModelObject findById(ModelObjectId id) {
        return super.findById(id.getId());
    }

    @Override
    public List<ModelObject> findByTenantId(TenantId tenantId) {
        Select select = select().from(ModelConstants.MODEL_OBJECT_CF).allowFiltering();
        Select.Where query = select.where();
        query.and(eq(ModelConstants.MODEL_OBJECT_TENANT_ID_PROPERTY, tenantId.getId()));
        List<ModelObjectEntity> entities = findListByStatement(query);
        return DaoUtil.convertDataList(entities);
    }

}
