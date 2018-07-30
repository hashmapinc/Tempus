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

import com.google.common.util.concurrent.ListenableFuture;
import com.hashmapinc.server.common.data.Customer;
import com.hashmapinc.server.common.data.CustomerGroup;
import com.hashmapinc.server.common.data.Tenant;
import com.hashmapinc.server.common.data.id.CustomerGroupId;
import com.hashmapinc.server.common.data.id.CustomerId;
import com.hashmapinc.server.common.data.id.TenantId;
import com.hashmapinc.server.common.data.page.TextPageData;
import com.hashmapinc.server.common.data.page.TextPageLink;
import com.hashmapinc.server.dao.customer.CustomerDao;
import com.hashmapinc.server.dao.entity.AbstractEntityService;
import com.hashmapinc.server.dao.exception.DataValidationException;
import com.hashmapinc.server.dao.exception.IncorrectParameterException;
import com.hashmapinc.server.dao.service.DataValidator;
import com.hashmapinc.server.dao.service.Validator;
import com.hashmapinc.server.dao.tenant.TenantDao;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import static com.hashmapinc.server.dao.model.ModelConstants.NULL_UUID;
import static com.hashmapinc.server.dao.service.Validator.validateId;

@Service
@Slf4j
public class CustomerGroupServiceImpl extends AbstractEntityService implements CustomerGroupService {

    public static final String INCORRECT_CUSTOMER_ID = "Incorrect customerId ";
    public static final String INCORRECT_CUSTOMER_GROUP_ID = "Incorrect customerGroupId ";
    public static final String INCORRECT_TENANT_ID = "Incorrect tenantId ";

    @Autowired
    private CustomerGroupDao customerGroupDao;

    @Autowired
    private TenantDao tenantDao;

    @Autowired
    private CustomerDao customerDao;


    @Override
    public CustomerGroup findByCustomerGroupId(CustomerGroupId customerGroupId) {
        log.trace("Executing findByCustomerGroupId [{}]", customerGroupId);
        Validator.validateId(customerGroupId, INCORRECT_CUSTOMER_ID + customerGroupId);
        return customerGroupDao.findById(customerGroupId.getId());
    }

    @Override
    public ListenableFuture<CustomerGroup> findCustomerGroupByIdAsync(CustomerGroupId customerGroupId) {
        log.trace("Executing findCustomerGroupByIdAsync [{}]", customerGroupId);
        validateId(customerGroupId, INCORRECT_CUSTOMER_ID + customerGroupId);
        return customerGroupDao.findByIdAsync(customerGroupId.getId());
    }

    @Override
    public Optional<CustomerGroup> findCustomerByTenantIdAndCustomerIdAndTitle(TenantId tenantId , CustomerId customerId , String title) {
        log.trace("Executing findCustomerByTenantIdAndCustomerIdAndTitle [{}] [{}] [{}]", tenantId, customerId, title);
        validateId(tenantId, INCORRECT_TENANT_ID + tenantId);
        validateId(customerId, INCORRECT_CUSTOMER_ID + customerId);
        return customerGroupDao.findCustomerGroupsByTenantIdAndCustomerIdAndTitle(tenantId.getId(), customerId.getId(), title);
    }

    @Override
    public CustomerGroup saveCustomerGroup(CustomerGroup customerGroup) {
        log.trace("Executing saveCustomerGroup [{}]", customerGroup);
        customerGroupValidator.validate(customerGroup);
        CustomerGroup savedCustomerGroup = customerGroupDao.save(customerGroup);
//        dashboardService.updateCustomerDashboards(savedCustomer.getId());    TODO : Need to confirm this implementation
        return savedCustomerGroup;
    }

    @Override
    public void deleteCustomerGroup(CustomerGroupId customerGroupId) {
        log.trace("Executing deleteCustomerGroup [{}]", customerGroupId);
        Validator.validateId(customerGroupId, INCORRECT_CUSTOMER_GROUP_ID + customerGroupId);
        CustomerGroup customerGroup = findByCustomerGroupId(customerGroupId);
        if (customerGroup == null) {
            throw new IncorrectParameterException("Unable to delete non-existent customer group.");
        }
//        dashboardService.unassignCustomerDashboards(customerGroupId);
//        assetService.unassignCustomerAssets(customerGroup.getTenantId(), customerGroupId);
//        deviceService.unassignCustomerDevices(customerGroup.getTenantId(), customerGroupId);
//        userService.deleteCustomerUsers(customerGroup.getTenantId(), customerGroupId);  TODO : Need to confirm this implementation
        deleteEntityRelations(customerGroupId);
        customerGroupDao.removeById(customerGroupId.getId());
    }

    @Override
    public TextPageData<CustomerGroup> findCustomerGroupsByTenantIdAndCustomerId(TenantId tenantId , CustomerId customerId , TextPageLink pageLink) {
        log.trace("Executing findCustomerGroupsByTenantIdAndCustomerId, tenantId [{}], coustomerId [{}], pageLink [{}]", tenantId, customerId, pageLink);
        Validator.validateId(tenantId, INCORRECT_TENANT_ID + tenantId);
        Validator.validateId(customerId, INCORRECT_CUSTOMER_ID + customerId);
        Validator.validatePageLink(pageLink, "Incorrect page link " + pageLink);
        List<CustomerGroup> customerGroups = customerGroupDao.findCustomerGroupsByTenantIdAndCustomerId(tenantId.getId(), customerId.getId(), pageLink);
        return new TextPageData<>(customerGroups, pageLink);
    }

    @Override
    public void deleteCustomerGroupsByTenantIdAndCustomerId(TenantId tenantId , CustomerId customerId) {
        // TODO : Need to check implementation
    }

    private DataValidator<CustomerGroup> customerGroupValidator =
            new DataValidator<CustomerGroup>() {

                @Override
                protected void validateCreate(CustomerGroup customerGroup) {
                    customerGroupDao
                            .findCustomerGroupsByTenantIdAndCustomerIdAndTitle(
                                customerGroup.getTenantId().getId(), customerGroup.getCustomerId().getId(), customerGroup.getTitle()
                            )
                            .ifPresent(c -> {
                                    throw new DataValidationException("Customer Group with such title already exists!");
                            }
                    );
                }

                @Override
                protected void validateUpdate(CustomerGroup customerGroup) {
                    customerGroupDao
                            .findCustomerGroupsByTenantIdAndCustomerIdAndTitle(
                                    customerGroup.getTenantId().getId(), customerGroup.getCustomerId().getId(), customerGroup.getTitle()
                            )
                            .ifPresent(c -> {
                                if (!c.getId().equals(customerGroup.getId())) {
                                    throw new DataValidationException("Customer Group with such title already exists!");
                                }
                            }
                    );
                }

                @Override
                protected void validateDataImpl(CustomerGroup customerGroup) {
                    if (StringUtils.isEmpty(customerGroup.getTitle())) {
                        throw new DataValidationException("Customer Group title should be specified!");
                    }
                    if (customerGroup.getTenantId() == null) {
                        throw new DataValidationException("Customer Group should be assigned to tenant!");
                    } else {
                        Tenant tenant = tenantDao.findById(customerGroup.getTenantId().getId());
                        if (tenant == null) {
                            throw new DataValidationException("Customer Group is referencing to non-existent tenant!");
                        }
                    }
                    if (customerGroup.getCustomerId() == null) {
                        throw new DataValidationException("Customer Group should be assigned to Customer!");
                    } else {
                        Customer customer = customerDao.findById(customerGroup.getCustomerId().getId());
                        if (customer == null) {
                            throw new DataValidationException("Customer Group is referencing to non-existent Customer!");
                        }
                    }
                }
            };
}
