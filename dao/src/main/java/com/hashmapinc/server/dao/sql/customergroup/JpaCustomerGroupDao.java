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
package com.hashmapinc.server.dao.sql.customergroup;

import com.hashmapinc.server.common.data.CustomerGroup;
import com.hashmapinc.server.common.data.UUIDConverter;
import com.hashmapinc.server.common.data.page.TextPageLink;
import com.hashmapinc.server.dao.DaoUtil;
import com.hashmapinc.server.dao.customergroup.CustomerGroupDao;
import com.hashmapinc.server.dao.model.ModelConstants;
import com.hashmapinc.server.dao.model.nosql.CustomerGroupEntity;
import com.hashmapinc.server.dao.sql.JpaAbstractSearchTextDao;
import com.hashmapinc.server.dao.util.SqlDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Component
@SqlDao
public class JpaCustomerGroupDao extends JpaAbstractSearchTextDao<CustomerGroupEntity, CustomerGroup> implements CustomerGroupDao {

    @Autowired
    private CustomerGroupRepository customerGroupRepository;

    @Override
    public List<CustomerGroup> findCustomerGroupsByTenantIdAndCustomerId(UUID tenantId, UUID customerId, TextPageLink pageLink) {
        return DaoUtil.convertDataList(customerGroupRepository.findByTenantIdAndCustomerId(
                UUIDConverter.fromTimeUUID(tenantId),
                UUIDConverter.fromTimeUUID(customerId),
                Objects.toString(pageLink.getTextSearch(), ""),
                pageLink.getIdOffset() == null ? ModelConstants.NULL_UUID_STR : UUIDConverter.fromTimeUUID(pageLink.getIdOffset()),
                new PageRequest(0, pageLink.getLimit())));
    }

    @Override
    public Optional<CustomerGroup> findCustomerGroupsByTenantIdAndCustomerIdAndTitle(UUID tenantId, UUID customerId, String title) {

        CustomerGroup customerGroup = DaoUtil.getData(
                customerGroupRepository.findByTenantIdAndCustomerIdAndTitle(UUIDConverter.fromTimeUUID(tenantId), UUIDConverter.fromTimeUUID(customerId), title)
        );
        return Optional.ofNullable(customerGroup);
    }

    @Override
    protected Class<CustomerGroupEntity> getEntityClass() {
        return CustomerGroupEntity.class;
    }

    @Override
    protected CrudRepository<CustomerGroupEntity, String> getCrudRepository() {
        return customerGroupRepository;
    }
}
