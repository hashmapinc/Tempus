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
package com.hashmapinc.server.dao.widget;

import com.datastax.driver.core.querybuilder.Select.Where;
import com.hashmapinc.server.common.data.widget.WidgetType;
import com.hashmapinc.server.dao.DaoUtil;
import com.hashmapinc.server.dao.model.ModelConstants;
import com.hashmapinc.server.dao.model.nosql.WidgetTypeEntity;
import com.hashmapinc.server.dao.nosql.CassandraAbstractModelDao;
import com.hashmapinc.server.dao.util.NoSqlDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

import static com.datastax.driver.core.querybuilder.QueryBuilder.eq;
import static com.datastax.driver.core.querybuilder.QueryBuilder.select;

@Component
@Slf4j
@NoSqlDao
public class CassandraWidgetTypeDao extends CassandraAbstractModelDao<WidgetTypeEntity, WidgetType> implements WidgetTypeDao {

    @Override
    protected Class<WidgetTypeEntity> getColumnFamilyClass() {
        return WidgetTypeEntity.class;
    }

    @Override
    protected String getColumnFamilyName() {
        return ModelConstants.WIDGET_TYPE_COLUMN_FAMILY_NAME;
    }

    @Override
    public List<WidgetType> findWidgetTypesByTenantIdAndBundleAlias(UUID tenantId, String bundleAlias) {
        log.debug("Try to find widget types by tenantId [{}] and bundleAlias [{}]", tenantId, bundleAlias);
        Where query = select().from(ModelConstants.WIDGET_TYPE_BY_TENANT_AND_ALIASES_COLUMN_FAMILY_NAME)
                .where()
                .and(eq(ModelConstants.WIDGET_TYPE_TENANT_ID_PROPERTY, tenantId))
                .and(eq(ModelConstants.WIDGET_TYPE_BUNDLE_ALIAS_PROPERTY, bundleAlias));
        List<WidgetTypeEntity> widgetTypesEntities = findListByStatement(query);
        log.trace("Found widget types [{}] by tenantId [{}] and bundleAlias [{}]", widgetTypesEntities, tenantId, bundleAlias);
        return DaoUtil.convertDataList(widgetTypesEntities);
    }

    @Override
    public WidgetType findByTenantIdBundleAliasAndAlias(UUID tenantId, String bundleAlias, String alias) {
        log.debug("Try to find widget type by tenantId [{}], bundleAlias [{}] and alias [{}]", tenantId, bundleAlias, alias);
        Where query = select().from(ModelConstants.WIDGET_TYPE_BY_TENANT_AND_ALIASES_COLUMN_FAMILY_NAME)
                .where()
                .and(eq(ModelConstants.WIDGET_TYPE_TENANT_ID_PROPERTY, tenantId))
                .and(eq(ModelConstants.WIDGET_TYPE_BUNDLE_ALIAS_PROPERTY, bundleAlias))
                .and(eq(ModelConstants.WIDGET_TYPE_ALIAS_PROPERTY, alias));
        log.trace("Execute query {}", query);
        WidgetTypeEntity widgetTypeEntity = findOneByStatement(query);
        log.trace("Found widget type [{}] by tenantId [{}], bundleAlias [{}] and alias [{}]",
                widgetTypeEntity, tenantId, bundleAlias, alias);
        return DaoUtil.getData(widgetTypeEntity);
    }

}
