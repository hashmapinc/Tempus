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

package com.hashmapinc.server.dao.logo;

import com.hashmapinc.server.common.data.Logo;
import com.hashmapinc.server.dao.DaoUtil;
import com.hashmapinc.server.dao.model.ModelConstants;
import com.hashmapinc.server.dao.model.nosql.LogoEntity;
import com.hashmapinc.server.dao.nosql.CassandraAbstractModelDao;
import com.hashmapinc.server.dao.util.NoSqlDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import com.datastax.driver.core.querybuilder.Select;

import com.datastax.driver.core.querybuilder.Select.Where;
import static com.datastax.driver.core.querybuilder.QueryBuilder.select;
import static com.datastax.driver.core.querybuilder.QueryBuilder.eq;

@Component
@Slf4j
@NoSqlDao


public class CassandraLogoDao extends CassandraAbstractModelDao<LogoEntity,Logo> implements LogoDao{

    @Override
    protected Class<LogoEntity> getColumnFamilyClass() {
        return LogoEntity.class;
    }

    @Override
    protected String getColumnFamilyName() {
        return ModelConstants.LOGO_COLUMN_FAMILY_NAME;
    }

    @Override
    public Logo findById(String id) {
        Select select = select().from(ModelConstants.LOGO_COLUMN_FAMILY_NAME).allowFiltering();
        Where query = select.where();
        query.and(eq(ModelConstants.ID_PROPERTY,id));
        return DaoUtil.getData(findOneByStatement(query));
    }

    @Override
    public Logo findByName(String name) {
        Select select = select().from(ModelConstants.LOGO_COLUMN_FAMILY_NAME).allowFiltering();
        Where query = select.where();
        query.and(eq(ModelConstants.LOGO_NAME_PROPERTY,name));
        return DaoUtil.getData(findOneByStatement(query));
    }


}
