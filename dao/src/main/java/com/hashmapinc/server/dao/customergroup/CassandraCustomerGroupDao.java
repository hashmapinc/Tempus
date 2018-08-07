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
package com.hashmapinc.server.dao.customergroup;

import com.datastax.driver.core.querybuilder.Select;
import com.hashmapinc.server.common.data.CustomerGroup;
import com.hashmapinc.server.common.data.id.UserId;
import com.hashmapinc.server.common.data.page.TextPageLink;
import com.hashmapinc.server.dao.DaoUtil;
import com.hashmapinc.server.dao.model.ModelConstants;
import com.hashmapinc.server.dao.model.nosql.CustomerGroupEntity;
import com.hashmapinc.server.dao.nosql.CassandraAbstractSearchTextDao;
import com.hashmapinc.server.dao.util.NoSqlDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.datastax.driver.core.querybuilder.QueryBuilder.eq;
import static com.datastax.driver.core.querybuilder.QueryBuilder.select;

@Component
@Slf4j
@NoSqlDao
public class CassandraCustomerGroupDao extends CassandraAbstractSearchTextDao<CustomerGroupEntity, CustomerGroup> implements CustomerGroupDao {
    @Override
    public List<CustomerGroup> findCustomerGroupsByTenantIdAndCustomerId(UUID tenantId, UUID customerId, TextPageLink pageLink) {
        log.debug("Try to find customer groups by tenantId [{}] and customerId [{}] and pageLink [{}]", tenantId, customerId, pageLink);
        List<CustomerGroupEntity> customerGroupEntities = findPageWithTextSearch(ModelConstants.CUSTOMER_GROUP_BY_TENANT_AND_CUSTOMER_SEARCH_TEXT_COLUMN_FAMILY_NAME,
                Arrays.asList(
                        eq(ModelConstants.CUSTOMER_GROUP_TENANT_ID_PROPERTY, tenantId),
                        eq(ModelConstants.CUSTOMER_GROUP_CUSTOMER_ID_PROPERTY, customerId)
                ),
                pageLink);
        log.trace("Found customer groups [{}] by tenantId [{}] and customerId [{}] and pageLink [{}]", customerGroupEntities, tenantId, customerId, pageLink);
        return DaoUtil.convertDataList(customerGroupEntities);
    }

    @Override
    public Optional<CustomerGroup> findCustomerGroupsByTenantIdAndCustomerIdAndTitle(UUID tenantId, UUID customerId, String title) {
        Select select = select().from(ModelConstants.CUSTOMER_GROUP_BY_TENANT_AND_CUSTOMER_AND_TITLE_VIEW_NAME);
        Select.Where query = select.where();
        query.and(eq(ModelConstants.CUSTOMER_GROUP_TENANT_ID_PROPERTY, tenantId));
        query.and(eq(ModelConstants.CUSTOMER_GROUP_CUSTOMER_ID_PROPERTY, customerId));
        query.and(eq(ModelConstants.CUSTOMER_GROUP_TITLE_PROPERTY, title));
        CustomerGroupEntity customerGroupEntity = findOneByStatement(query);
        CustomerGroup customerGroup = DaoUtil.getData(customerGroupEntity);
        return Optional.ofNullable(customerGroup);
    }

    @Override
    public List<UserId> findUserIdsByCustomerGroupId(UUID customerGroupId) {
        return null;
    }

    @Override
    public void deleteUserIdsForCustomerGroupId(UUID customerGroupId) {
        //TODO
    }

    @Override
    public List<CustomerGroup> findByUserId(UUID userId, TextPageLink textPageLink) {
        return null;
    }

    @Override
    protected Class<CustomerGroupEntity> getColumnFamilyClass() {
        return CustomerGroupEntity.class;
    }

    @Override
    protected String getColumnFamilyName() {
        return ModelConstants.CUSTOMER_GROUP_COLUMN_FAMILY_NAME;
    }
}
