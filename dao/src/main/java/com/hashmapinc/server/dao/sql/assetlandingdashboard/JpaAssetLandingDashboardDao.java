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
package com.hashmapinc.server.dao.sql.assetlandingdashboard;

import com.hashmapinc.server.common.data.AssetLandingDashboardInfo;
import com.hashmapinc.server.common.data.UUIDConverter;
import com.hashmapinc.server.common.data.id.DashboardId;
import com.hashmapinc.server.common.data.id.DataModelObjectId;
import com.hashmapinc.server.dao.DaoUtil;
import com.hashmapinc.server.dao.assetlandingdashboard.AssetLandingDashboardDao;
import com.hashmapinc.server.dao.model.sql.AssetLandingDashboardEntity;
import com.hashmapinc.server.dao.sql.JpaAbstractDaoListeningExecutorService;
import com.hashmapinc.server.dao.util.SqlDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@SqlDao
public class JpaAssetLandingDashboardDao extends JpaAbstractDaoListeningExecutorService implements AssetLandingDashboardDao {

    @Autowired
    AssetLandingDashboardRepository assetLandingDashboardRepository;

    @Override
    public AssetLandingDashboardInfo save(AssetLandingDashboardInfo assetLandingDashboardInfo) {
        AssetLandingDashboardEntity assetLandingDashboardEntity = new AssetLandingDashboardEntity(assetLandingDashboardInfo);
         assetLandingDashboardEntity = assetLandingDashboardRepository.save(assetLandingDashboardEntity);
         return assetLandingDashboardEntity.toData();
    }

    @Override
    public List<AssetLandingDashboardInfo> findByDataModelObjectId(DataModelObjectId dataModelObjectId) {
        String dmoIdStr = UUIDConverter.fromTimeUUID(dataModelObjectId.getId());
        return DaoUtil.convertDataList(assetLandingDashboardRepository.findByDataModelObjectId(dmoIdStr));
    }

    @Override
    public AssetLandingDashboardInfo findByDashboardId(DashboardId dashboardId) {
        String dashboardIdStr = UUIDConverter.fromTimeUUID(dashboardId.getId());
        AssetLandingDashboardEntity entity = assetLandingDashboardRepository.findOne(dashboardIdStr);
        return entity.toData();
    }

    @Override
    public void removeByDashBoardId(DashboardId dashboardId) {
        String dashboardIdStr = UUIDConverter.fromTimeUUID(dashboardId.getId());
        assetLandingDashboardRepository.delete(dashboardIdStr);
    }
}
