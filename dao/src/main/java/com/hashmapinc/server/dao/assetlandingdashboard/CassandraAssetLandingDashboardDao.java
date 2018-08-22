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
package com.hashmapinc.server.dao.assetlandingdashboard;

import com.datastax.driver.core.*;
import com.hashmapinc.server.common.data.AssetLandingDashboardInfo;
import com.hashmapinc.server.common.data.id.DashboardId;
import com.hashmapinc.server.common.data.id.DataModelId;
import com.hashmapinc.server.common.data.id.DataModelObjectId;
import com.hashmapinc.server.dao.model.ModelConstants;
import com.hashmapinc.server.dao.nosql.CassandraAbstractAsyncDao;
import com.hashmapinc.server.dao.util.NoSqlDao;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.List;

@Service
@NoSqlDao
public class CassandraAssetLandingDashboardDao extends CassandraAbstractAsyncDao implements AssetLandingDashboardDao {

    public static final String INSERT_INTO = "INSERT INTO ";
    public static final String SELECT_PREFIX = "SELECT ";
    public static final String DELETE_PREFIX = "DELETE ";
    public static final String EQUALS_PARAM = " = ? ";
    public static final String WHERE = " WHERE ";
    public static final String ALLOW_FILTERING = " ALLOW FILTERING ";

    private PreparedStatement saveStmt;
    private PreparedStatement fetchByDataModelObjStmt;
    private PreparedStatement fetchByDashboardIdStmt;
    private PreparedStatement deleteByDashboardIdStmt;

    @PostConstruct
    public void init() {
        super.startExecutor();
    }

    @PreDestroy
    public void stop() {
        super.stopExecutor();
    }

    @Override
    public AssetLandingDashboardInfo save(AssetLandingDashboardInfo assetLandingDashboardInfo) {
        BoundStatement stmt = getSaveStmt().bind()
                .setUUID(0, assetLandingDashboardInfo.getDashboardId().getId())
                .setUUID(1, assetLandingDashboardInfo.getDataModelId().getId())
                .setUUID(2, assetLandingDashboardInfo.getDataModelObjectId().getId());

        ResultSet rs = executeWrite(stmt);
        if(rs.wasApplied())
            return assetLandingDashboardInfo;
        return null;
    }

    @Override
    public List<AssetLandingDashboardInfo> findByDataModelObjectId(DataModelObjectId dataModelObjectId) {
        BoundStatement stmt = getFetchByDataModelObjStmt().bind()
                .setUUID(0, dataModelObjectId.getId());
        ResultSet rs = executeRead(stmt);
        return convertToAssetLandingDashboarList(rs);
    }

    @Override
    public void removeByDashBoardId(DashboardId dashboardId) {
        BoundStatement stmt = getDeleteByDashboardIdStmt().bind()
                .setUUID(0, dashboardId.getId());
        executeWrite(stmt);
    }

    @Override
    public AssetLandingDashboardInfo findByDashboardId(DashboardId dashboardId) {
        BoundStatement stmt = getFetchByDashboardIdStmt().bind()
                .setUUID(0, dashboardId.getId());
        ResultSet rs = executeRead(stmt);
        Row row = rs.one();
        return convertToAssetLandingDashboard(row);
    }

    private PreparedStatement getSaveStmt() {
        if (saveStmt == null) {
            String strStatement = INSERT_INTO + ModelConstants.ASSET_LANDING_COLUMN_FAMILY_NAME +
                    "(" + ModelConstants.ASSET_LANDING_DASHBOARD_ID +
                    "," + ModelConstants.ASSET_LANDING_DATA_MODEL_ID +
                    "," + ModelConstants.ASSET_LANDING_DATAMODEL_OBJECT_ID + ")" +
                    " VALUES(?, ?, ?)";
            Session session = getSession();
            saveStmt = session.prepare(strStatement);
        }

        return saveStmt;
    }

    private PreparedStatement getFetchByDashboardIdStmt() {
        if (fetchByDashboardIdStmt == null) {
            String strStatement = SELECT_PREFIX + ModelConstants.ASSET_LANDING_DASHBOARD_ID +
                    "," + ModelConstants.ASSET_LANDING_DATA_MODEL_ID +
                    "," + ModelConstants.ASSET_LANDING_DATAMODEL_OBJECT_ID +
                    " FROM " + ModelConstants.ASSET_LANDING_COLUMN_FAMILY_NAME +
                    WHERE + ModelConstants.ASSET_LANDING_DASHBOARD_ID + EQUALS_PARAM;
            Session session = getSession();
            fetchByDashboardIdStmt = session.prepare(strStatement);
        }

        return fetchByDashboardIdStmt;
    }

    private PreparedStatement getDeleteByDashboardIdStmt() {
        if (deleteByDashboardIdStmt == null) {
            String strStatement = DELETE_PREFIX + "FROM " + ModelConstants.ASSET_LANDING_COLUMN_FAMILY_NAME +
                    WHERE + ModelConstants.ASSET_LANDING_DASHBOARD_ID + EQUALS_PARAM;
            Session session = getSession();
            deleteByDashboardIdStmt = session.prepare(strStatement);
        }

        return deleteByDashboardIdStmt;

    }

    private PreparedStatement getFetchByDataModelObjStmt() {
        if (fetchByDataModelObjStmt == null) {
            String strStatement = SELECT_PREFIX + ModelConstants.ASSET_LANDING_DASHBOARD_ID +
                    "," + ModelConstants.ASSET_LANDING_DATA_MODEL_ID +
                    "," + ModelConstants.ASSET_LANDING_DATAMODEL_OBJECT_ID +
                    " FROM " + ModelConstants.ASSET_LANDING_COLUMN_FAMILY_NAME +
                    WHERE + ModelConstants.ASSET_LANDING_DATAMODEL_OBJECT_ID + EQUALS_PARAM + ALLOW_FILTERING;
            Session session = getSession();
            fetchByDataModelObjStmt = session.prepare(strStatement);
        }

        return fetchByDataModelObjStmt;
    }

    private AssetLandingDashboardInfo convertToAssetLandingDashboard(Row row) {
        AssetLandingDashboardInfo dashboard = new AssetLandingDashboardInfo(new DashboardId(row.getUUID(0)));
        dashboard.setDataModelId(new DataModelId(row.getUUID(1)));
        dashboard.setDataModelObjectId(new DataModelObjectId(row.getUUID(2)));
        return dashboard;
    }

    private List<AssetLandingDashboardInfo> convertToAssetLandingDashboarList(ResultSet rs) {
        List<Row> rows = rs.all();
        List<AssetLandingDashboardInfo> list = new ArrayList<>();
        rows.forEach(row -> list.add(convertToAssetLandingDashboard(row)));
        return list;
    }
}
