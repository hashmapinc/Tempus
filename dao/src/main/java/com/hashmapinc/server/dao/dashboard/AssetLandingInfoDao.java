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
package com.hashmapinc.server.dao.dashboard;

import com.datastax.driver.core.querybuilder.Select;
import com.hashmapinc.server.common.data.AssetLandingInfo;
import com.hashmapinc.server.dao.DaoUtil;
import com.hashmapinc.server.dao.model.ModelConstants;
import com.hashmapinc.server.dao.model.nosql.AssetLandingInfoEntity;
import com.hashmapinc.server.dao.nosql.CassandraAbstractModelDao;
import com.hashmapinc.server.dao.util.NoSqlDao;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

import static com.datastax.driver.core.querybuilder.QueryBuilder.eq;
import static com.datastax.driver.core.querybuilder.QueryBuilder.select;

@Service
@NoSqlDao
public class AssetLandingInfoDao extends CassandraAbstractModelDao <AssetLandingInfoEntity, AssetLandingInfo> {
    @Override
    protected Class<AssetLandingInfoEntity> getColumnFamilyClass() {
        return AssetLandingInfoEntity.class;
    }

    @Override
    protected String getColumnFamilyName() {
        return ModelConstants.ASSET_LANDING_COLUMN_FAMILY_NAME;
    }

    public List<AssetLandingInfo> findByDataModelObjId(UUID dataModelObjectId) {
        Select select = select().from(ModelConstants.ASSET_LANDING_VIEW_BY_DATA_MODEL_OBJ_ID);
        Select.Where query = select.where();
        query.and(eq(ModelConstants.ASSET_LANDING_DATAMODEL_OBJECT_ID, dataModelObjectId));
        List<AssetLandingInfoEntity> assetLandingInfoEntities = findListByStatement(query);
        return DaoUtil.convertDataList(assetLandingInfoEntities);
    }
}
