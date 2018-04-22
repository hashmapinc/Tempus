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
package com.hashmapinc.server.dao.customer;

import com.datastax.driver.core.querybuilder.Select;
import com.hashmapinc.server.common.data.Customer;
import com.hashmapinc.server.common.data.page.TextPageLink;
import com.hashmapinc.server.dao.DaoUtil;
import com.hashmapinc.server.dao.model.ModelConstants;
import com.hashmapinc.server.dao.model.nosql.CustomerEntity;
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
public class CassandraCustomerDao extends CassandraAbstractSearchTextDao<CustomerEntity, Customer> implements CustomerDao {

    @Override
    protected Class<CustomerEntity> getColumnFamilyClass() {
        return CustomerEntity.class;
    }

    @Override
    protected String getColumnFamilyName() {
        return ModelConstants.CUSTOMER_COLUMN_FAMILY_NAME;
    }

    @Override
    public List<Customer> findCustomersByTenantId(UUID tenantId, TextPageLink pageLink) {
        log.debug("Try to find customers by tenantId [{}] and pageLink [{}]", tenantId, pageLink);
        List<CustomerEntity> customerEntities = findPageWithTextSearch(ModelConstants.CUSTOMER_BY_TENANT_AND_SEARCH_TEXT_COLUMN_FAMILY_NAME,
                Arrays.asList(eq(ModelConstants.CUSTOMER_TENANT_ID_PROPERTY, tenantId)),
                pageLink); 
        log.trace("Found customers [{}] by tenantId [{}] and pageLink [{}]", customerEntities, tenantId, pageLink);
        return DaoUtil.convertDataList(customerEntities);
    }

    @Override
    public Optional<Customer> findCustomersByTenantIdAndTitle(UUID tenantId, String title) {
        Select select = select().from(ModelConstants.CUSTOMER_BY_TENANT_AND_TITLE_VIEW_NAME);
        Select.Where query = select.where();
        query.and(eq(ModelConstants.CUSTOMER_TENANT_ID_PROPERTY, tenantId));
        query.and(eq(ModelConstants.CUSTOMER_TITLE_PROPERTY, title));
        CustomerEntity customerEntity = findOneByStatement(query);
        Customer customer = DaoUtil.getData(customerEntity);
        return Optional.ofNullable(customer);
    }

}
