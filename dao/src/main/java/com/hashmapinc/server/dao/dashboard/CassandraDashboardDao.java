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
package com.hashmapinc.server.dao.dashboard;

import com.hashmapinc.server.common.data.Dashboard;
import org.springframework.stereotype.Component;
import com.hashmapinc.server.dao.model.nosql.DashboardEntity;
import com.hashmapinc.server.dao.nosql.CassandraAbstractSearchTextDao;
import com.hashmapinc.server.dao.util.NoSqlDao;

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
}
