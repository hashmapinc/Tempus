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
import com.hashmapinc.server.common.data.Dashboard;
import com.hashmapinc.server.common.data.DashboardType;
import com.hashmapinc.server.dao.DaoUtil;
import com.hashmapinc.server.dao.model.ModelConstants;
import com.hashmapinc.server.dao.model.nosql.DashboardEntity;
import com.hashmapinc.server.dao.nosql.CassandraAbstractSearchTextDao;
import com.hashmapinc.server.dao.util.NoSqlDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.datastax.driver.core.querybuilder.QueryBuilder.eq;
import static com.datastax.driver.core.querybuilder.QueryBuilder.select;
import static com.hashmapinc.server.dao.model.ModelConstants.DASHBOARD_COLUMN_FAMILY_NAME;

@Component
@NoSqlDao
public class CassandraDashboardDao extends CassandraAbstractSearchTextDao<DashboardEntity, Dashboard> implements DashboardDao {

    @Override
    protected Class<DashboardEntity> getColumnFamilyClass() {
        return DashboardEntity.class;
    }

    @Override
    protected String getColumnFamilyName() {
        return DASHBOARD_COLUMN_FAMILY_NAME;
    }

    @Autowired
    AssetLandingInfoDao assetLandingInfoDao;

    @Override
    public List<Dashboard> findDashboardByDataModelObjectId(UUID dataModelObjectId) {
        List<AssetLandingInfo> assetLandingInfoList = assetLandingInfoDao.findByDataModelObjId(dataModelObjectId);
        List<Dashboard> dashboards = new ArrayList<>();
        for (AssetLandingInfo ali: assetLandingInfoList) {
            dashboards.add(findById(ali.getDashboardId().getId()));
        }
        return dashboards;
    }

    @Override
    public Dashboard save(Dashboard dashboard) {
        Dashboard savedDashboard = super.save(dashboard);
        if (savedDashboard.getType() == DashboardType.ASSET_LANDING_PAGE) {
            AssetLandingInfo savedAli = assetLandingInfoDao.save(savedDashboard.getAssetLandingInfo());
            savedDashboard.setAssetLandingInfo(savedAli);
        }
        return savedDashboard;
    }

    @Override
    public boolean removeById(UUID id) {
        if (assetLandingInfoDao.findById(id) != null) {
            if (assetLandingInfoDao.removeById(id)) {
                return super.removeById(id);
            }
        } else {
            return super.removeById(id);
        }
        return false;
    }

    @Override
    public Dashboard findById(UUID id) {
        Dashboard dashboard = super.findById(id);
        if (dashboard != null && dashboard.getType() == DashboardType.ASSET_LANDING_PAGE)
            dashboard.setAssetLandingInfo(assetLandingInfoDao.findById(id));
        return dashboard;
    }

    @Override
    public List<Dashboard> findDashboardBySearchText(String searchText) {
        Select select = select().from(ModelConstants.DASHBOARD_BY_TENANT_AND_SEARCH_TEXT_COLUMN_FAMILY_NAME).allowFiltering();
        Select.Where query = select.where();
        query.and(eq(ModelConstants.SEARCH_TEXT_PROPERTY, searchText));
        List<DashboardEntity> entities = findListByStatement(query);
        return DaoUtil.convertDataList(entities);
    }
}
