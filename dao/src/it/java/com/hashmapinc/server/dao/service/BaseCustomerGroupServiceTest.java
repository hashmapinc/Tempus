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
package com.hashmapinc.server.dao.service;

import com.datastax.driver.core.utils.UUIDs;
import com.hashmapinc.server.common.data.Customer;
import com.hashmapinc.server.common.data.CustomerGroup;
import com.hashmapinc.server.common.data.Tenant;
import com.hashmapinc.server.common.data.id.CustomerId;
import com.hashmapinc.server.common.data.id.TenantId;
import com.hashmapinc.server.common.data.page.TextPageData;
import com.hashmapinc.server.common.data.page.TextPageLink;
import com.hashmapinc.server.dao.exception.DataValidationException;
import com.hashmapinc.server.dao.service.AbstractServiceTest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
public abstract class BaseCustomerGroupServiceTest extends AbstractServiceTest {

    private IdComparator<CustomerGroup> idComparator = new IdComparator<>();

    private TenantId tenantId;
    private CustomerId customerId;

    @Before
    public void before() {
        Tenant tenant = new Tenant();
        tenant.setTitle("My tenant");
        Tenant savedTenant = tenantService.saveTenant(tenant);
        Assert.assertNotNull(savedTenant);
        tenantId = savedTenant.getId();

        Customer customer = new Customer();
        customer.setTitle("My Customer");
        customer.setTenantId(tenantId);
        Customer savedCustomer = customerService.saveCustomer(customer);
        Assert.assertNotNull(savedCustomer);
        customerId = savedCustomer.getId();
    }

    @After
    public void after() {
        customerService.deleteCustomer(customerId);
        tenantService.deleteTenant(tenantId);
    }

    @Test
    public void saveCustomerGroup() {
        CustomerGroup customerGroup = new CustomerGroup();
        customerGroup.setTenantId(tenantId);
        customerGroup.setCustomerId(customerId);
        customerGroup.setTitle("My Group");
        CustomerGroup savedCustomerGroup = customerGroupService.saveCustomerGroup(customerGroup);

        Assert.assertNotNull(savedCustomerGroup);
        Assert.assertNotNull(savedCustomerGroup.getId());
        Assert.assertTrue(savedCustomerGroup.getCreatedTime() > 0);
        Assert.assertEquals(customerGroup.getTenantId(), savedCustomerGroup.getTenantId());
        Assert.assertEquals(customerGroup.getCustomerId(), savedCustomerGroup.getCustomerId());
        Assert.assertEquals(customerGroup.getTitle(), savedCustomerGroup.getTitle());

        savedCustomerGroup.setTitle("My New Group");
        customerGroupService.saveCustomerGroup(savedCustomerGroup);
        CustomerGroup found = customerGroupService.findByCustomerGroupId(savedCustomerGroup.getId());
        Assert.assertEquals(savedCustomerGroup.getTitle(), found.getTitle());

        customerGroupService.deleteCustomerGroup(savedCustomerGroup.getId());
    }

    @Test
    public void findByCustomerGroupId() {
        CustomerGroup customerGroup = new CustomerGroup();
        customerGroup.setTenantId(tenantId);
        customerGroup.setCustomerId(customerId);
        customerGroup.setTitle("My Group");
        CustomerGroup savedCustomerGroup = customerGroupService.saveCustomerGroup(customerGroup);

        CustomerGroup found = customerGroupService.findByCustomerGroupId(savedCustomerGroup.getId());
        Assert.assertNotNull(found);
        Assert.assertEquals(savedCustomerGroup.getId(), found.getId());
        customerGroupService.deleteCustomerGroup(savedCustomerGroup.getId());
    }

    @Test(expected = DataValidationException.class)
    public void saveCustomerGroupWithEmptyTitle() {
        CustomerGroup customerGroup = new CustomerGroup();
        customerGroup.setTenantId(tenantId);
        customerGroup.setCustomerId(customerId);
        customerGroupService.saveCustomerGroup(customerGroup);
    }

    @Test(expected = DataValidationException.class)
    public void saveCustomerGroupWithEmptyTenant() {
        CustomerGroup customerGroup = new CustomerGroup();
        customerGroup.setTitle("My Group");
        customerGroupService.saveCustomerGroup(customerGroup);
    }

    @Test(expected = DataValidationException.class)
    public void saveCustomerGroupWithInvalidTenant() {
        CustomerGroup customerGroup = new CustomerGroup();
        customerGroup.setTitle("My Group");
        customerGroup.setTenantId(new TenantId(UUIDs.timeBased()));
        customerGroupService.saveCustomerGroup(customerGroup);
    }

    @Test(expected = DataValidationException.class)
    public void saveCustomerGroupWithEmptyCustomer() {
        CustomerGroup customerGroup = new CustomerGroup();
        customerGroup.setTenantId(tenantId);
        customerGroup.setTitle("My Group");
        customerGroupService.saveCustomerGroup(customerGroup);
    }

    @Test(expected = DataValidationException.class)
    public void saveCustomerGroupWithInvalidCustomer() {
        CustomerGroup customerGroup = new CustomerGroup();
        customerGroup.setTenantId(tenantId);
        customerGroup.setTitle("My Group");
        customerGroup.setCustomerId(new CustomerId(UUIDs.timeBased()));
        customerGroupService.saveCustomerGroup(customerGroup);
    }

    @Test
    public void deleteCustomerGroup() {
        CustomerGroup customerGroup = new CustomerGroup();
        customerGroup.setTenantId(tenantId);
        customerGroup.setCustomerId(customerId);
        customerGroup.setTitle("My Group");
        CustomerGroup savedCustomerGroup = customerGroupService.saveCustomerGroup(customerGroup);

        customerGroupService.deleteCustomerGroup(savedCustomerGroup.getId());
        CustomerGroup found = customerGroupService.findByCustomerGroupId(savedCustomerGroup.getId());
        Assert.assertNull(found);
    }

    @Test
    public void deleteCustomerGroupsByTenantIdAndCustomerId() {
        CustomerGroup customerGroup = new CustomerGroup();
        customerGroup.setTenantId(tenantId);
        customerGroup.setCustomerId(customerId);
        customerGroup.setTitle("My Group");
        CustomerGroup savedCustomerGroup = customerGroupService.saveCustomerGroup(customerGroup);

        customerGroupService.deleteCustomerGroupsByTenantIdAndCustomerId(tenantId, customerId);
        CustomerGroup found = customerGroupService.findByCustomerGroupId(savedCustomerGroup.getId());
        Assert.assertNull(found);
    }

    @Test
    public void findCustomerGroupsByTenantIdAndCustomerId() {
        Tenant tenant = new Tenant();
        tenant.setTitle("Test Tenant");
        tenant = tenantService.saveTenant(tenant);
        TenantId tenantId = tenant.getId();

        Customer customer = new Customer();
        customer.setTitle("Test Customer");
        customer.setTenantId(tenantId);
        customer = customerService.saveCustomer(customer);
        CustomerId customerId = customer.getId();

        List<CustomerGroup> customerGroups = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            CustomerGroup customerGroup = new CustomerGroup();
            customerGroup.setTenantId(tenantId);
            customerGroup.setCustomerId(customerId);
            customerGroup.setTitle("Group"+i);
            customerGroups.add(customerGroupService.saveCustomerGroup(customerGroup));
        }

        List<CustomerGroup> loadedCustomerGroup = new ArrayList<>();
        TextPageLink pageLink = new TextPageLink(3);
        TextPageData<CustomerGroup> pageData = null;
        do {
            pageData = customerGroupService.findCustomerGroupsByTenantIdAndCustomerId(tenantId, customerId, pageLink);
            loadedCustomerGroup.addAll(pageData.getData());
            if (pageData.hasNext()) {
                pageLink = pageData.getNextPageLink();
            }
        } while (pageData.hasNext());

        Collections.sort(customerGroups, idComparator);
        Collections.sort(loadedCustomerGroup, idComparator);

        Assert.assertEquals(customerGroups, loadedCustomerGroup);

        customerGroupService.deleteCustomerGroupsByTenantIdAndCustomerId(tenantId, customerId);

        pageLink = new TextPageLink(33);
        pageData = customerGroupService.findCustomerGroupsByTenantIdAndCustomerId(tenantId, customerId, pageLink);
        Assert.assertFalse(pageData.hasNext());
        Assert.assertTrue(pageData.getData().isEmpty());

        tenantService.deleteTenant(tenantId);
    }

    @Test
    public void findCustomerByTenantIdAndCustomerIdAndTitle() {
        String title1 = "Customer Group title 1";
        List<CustomerGroup> customerGroupsTitle1 = new ArrayList<>();
        for (int i = 0; i < 143; i++) {
            CustomerGroup customerGroup = new CustomerGroup();
            customerGroup.setTenantId(tenantId);
            customerGroup.setCustomerId(customerId);
            String suffix = RandomStringUtils.randomAlphanumeric((int)(5 + Math.random()*10));
            String title = title1 + suffix;
            title = i % 2 == 0 ? title.toLowerCase() : title.toUpperCase();
            customerGroup.setTitle(title);
            customerGroupsTitle1.add(customerGroupService.saveCustomerGroup(customerGroup));
        }

        String title2 = "Customer Group title 2";
        List<CustomerGroup> customersGroupTitle2 = new ArrayList<>();
        for (int i = 0; i < 175; i++) {
            CustomerGroup customerGroup = new CustomerGroup();
            customerGroup.setTenantId(tenantId);
            customerGroup.setCustomerId(customerId);
            String suffix = RandomStringUtils.randomAlphanumeric((int)(5 + Math.random()*10));
            String title = title2 + suffix;
            title = i % 2 == 0 ? title.toLowerCase() : title.toUpperCase();
            customerGroup.setTitle(title);
            customersGroupTitle2.add(customerGroupService.saveCustomerGroup(customerGroup));
        }

        List<CustomerGroup> loadedCustomerGroupsTitle1 = new ArrayList<>();
        TextPageLink pageLink = new TextPageLink(15, title1);
        TextPageData<CustomerGroup> pageData = null;
        do {
            pageData = customerGroupService.findCustomerGroupsByTenantIdAndCustomerId(tenantId, customerId, pageLink);
            loadedCustomerGroupsTitle1.addAll(pageData.getData());
            if (pageData.hasNext()) {
                pageLink = pageData.getNextPageLink();
            }
        } while (pageData.hasNext());

        Collections.sort(customerGroupsTitle1, idComparator);
        Collections.sort(loadedCustomerGroupsTitle1, idComparator);

        Assert.assertEquals(customerGroupsTitle1, loadedCustomerGroupsTitle1);

        List<CustomerGroup> loadedCustomerGroupsTitle2 = new ArrayList<>();
        pageLink = new TextPageLink(4, title2);
        do {
            pageData = customerGroupService.findCustomerGroupsByTenantIdAndCustomerId(tenantId, customerId, pageLink);
            loadedCustomerGroupsTitle2.addAll(pageData.getData());
            if (pageData.hasNext()) {
                pageLink = pageData.getNextPageLink();
            }
        } while (pageData.hasNext());

        Collections.sort(customersGroupTitle2, idComparator);
        Collections.sort(loadedCustomerGroupsTitle2, idComparator);

        Assert.assertEquals(customersGroupTitle2, loadedCustomerGroupsTitle2);

        for (CustomerGroup customerGroup : loadedCustomerGroupsTitle1) {
            customerGroupService.deleteCustomerGroup(customerGroup.getId());
        }

        pageLink = new TextPageLink(4, title1);
        pageData = customerGroupService.findCustomerGroupsByTenantIdAndCustomerId(tenantId, customerId, pageLink);
        Assert.assertFalse(pageData.hasNext());
        Assert.assertEquals(0, pageData.getData().size());

        for (CustomerGroup customerGroup : loadedCustomerGroupsTitle2) {
            customerGroupService.deleteCustomerGroup(customerGroup.getId());
        }

        pageLink = new TextPageLink(4, title2);
        pageData = customerGroupService.findCustomerGroupsByTenantIdAndCustomerId(tenantId, customerId, pageLink);
        Assert.assertFalse(pageData.hasNext());
        Assert.assertEquals(0, pageData.getData().size());
    }

}
