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
package com.hashmapinc.server.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hashmapinc.server.common.data.Customer;
import com.hashmapinc.server.common.data.CustomerGroup;
import com.hashmapinc.server.common.data.id.CustomerId;
import com.hashmapinc.server.common.data.page.TextPageData;
import com.hashmapinc.server.common.data.page.TextPageLink;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public abstract class BaseCustomerGroupControllerTest extends AbstractControllerTest {

    private IdComparator<CustomerGroup> idComparator = new IdComparator<>();

    private CustomerId customerId;
    @Before
    public void before() throws Exception{
        loginTenantAdmin();
        Customer customer = new Customer();
        customer.setTitle("My customer");
        Customer savedCustomer = doPost("/api/customer", customer, Customer.class);
        customerId = savedCustomer.getId();
    }

    @After
    public void tearDown() throws Exception{
        doDelete("/api/customer/"+customerId)
                .andExpect(status().isOk());
    }

    @Test
    public void saveCustomerGroup() throws Exception {

        CustomerGroup customerGroup = new CustomerGroup();
        customerGroup.setTitle("My Customer Group");
        customerGroup.setTenantId(tenantId);
        customerGroup.setCustomerId(customerId);
        CustomerGroup savedCustomerGroup = doPost("/api/customer/group", customerGroup, CustomerGroup.class);

        Assert.assertNotNull(savedCustomerGroup);
        Assert.assertNotNull(savedCustomerGroup.getId());
        Assert.assertTrue(savedCustomerGroup.getCreatedTime() > 0);
        Assert.assertEquals(customerGroup.getTitle(), savedCustomerGroup.getTitle());
        savedCustomerGroup.setTitle("My New Customer Group");
        doPost("/api/customer/group", savedCustomerGroup, CustomerGroup.class);

        CustomerGroup foundCustomerGroup = doGet("/api/customer/group/"+savedCustomerGroup.getId().getId().toString(), CustomerGroup.class);
        Assert.assertEquals(foundCustomerGroup.getTitle(), savedCustomerGroup.getTitle());

        doDelete("/api/customer/group/"+savedCustomerGroup.getId().getId().toString())
                .andExpect(status().isOk());
    }

    @Test
    public void findCustomerGroupById() throws Exception {

        CustomerGroup customerGroup = new CustomerGroup();
        customerGroup.setTitle("My Customer Group");
        customerGroup.setTenantId(tenantId);
        customerGroup.setCustomerId(customerId);
        CustomerGroup savedCustomerGroup = doPost("/api/customer/group", customerGroup, CustomerGroup.class);

        CustomerGroup foundCustomerGroup = doGet("/api/customer/group/"+ savedCustomerGroup.getId().getId().toString(), CustomerGroup.class);
        Assert.assertNotNull(foundCustomerGroup);
        Assert.assertEquals(savedCustomerGroup, foundCustomerGroup);

        doDelete("/api/customer/group/"+savedCustomerGroup.getId().getId().toString())
                .andExpect(status().isOk());
    }

    @Test
    public void deleteCustomerGroup() throws Exception {

        CustomerGroup customerGroup = new CustomerGroup();
        customerGroup.setTitle("My Customer Group");
        customerGroup.setTenantId(tenantId);
        customerGroup.setCustomerId(customerId);
        CustomerGroup savedCustomerGroup = doPost("/api/customer/group", customerGroup, CustomerGroup.class);

        doDelete("/api/customer/group/"+savedCustomerGroup.getId().getId().toString())
                .andExpect(status().isOk());

        doGet("/api/customer/group/"+savedCustomerGroup.getId().getId().toString())
                .andExpect(status().isNotFound());
    }

    @Test
    public void saveCustomerGroupWithEmptyTitle() throws Exception {

        CustomerGroup customerGroup = new CustomerGroup();
        customerGroup.setTenantId(tenantId);
        customerGroup.setCustomerId(customerId);
        doPost("/api/customer/group", customerGroup)
                .andExpect(status().isBadRequest())
                .andExpect(statusReason(containsString("Customer Group title should be specified")));
    }

    @Test
    public void testFindCustomerGroups() throws Exception {

        List<CustomerGroup> customerGroups = new ArrayList<>();
        for (int i=0;i<135;i++) {
            CustomerGroup customerGroup = new CustomerGroup();
            customerGroup.setTenantId(tenantId);
            customerGroup.setCustomerId(customerId);
            customerGroup.setTitle("CustomerGroup"+i);
            customerGroups.add(doPost("/api/customer/group", customerGroup, CustomerGroup.class));
        }

        List<CustomerGroup> loadedCustomerGroups = new ArrayList<>();
        TextPageLink pageLink = new TextPageLink(23);
        TextPageData<CustomerGroup> pageData = null;
        do {
            pageData = doGetTypedWithPageLink("/api/customer/"+customerId+"/groups?", new TypeReference<TextPageData<CustomerGroup>>(){}, pageLink);
            loadedCustomerGroups.addAll(pageData.getData());
            if (pageData.hasNext()) {
                pageLink = pageData.getNextPageLink();
            }
        } while (pageData.hasNext());

        Collections.sort(customerGroups, idComparator);
        Collections.sort(loadedCustomerGroups, idComparator);

        Assert.assertEquals(customerGroups, loadedCustomerGroups);

        for (CustomerGroup customerGroup : loadedCustomerGroups) {
            doDelete("/api/customer/group/"+customerGroup.getId().getId().toString())
                    .andExpect(status().isOk());
        }
    }

    @Test
    public void testFindCustomerGroupsByTitle() throws Exception {

        String title1 = "Customer Group title 1";
        List<CustomerGroup> customerGroupsTitle1 = new ArrayList<>();
        for (int i=0;i<143;i++) {
            CustomerGroup customerGroup = new CustomerGroup();
            customerGroup.setTenantId(tenantId);
            customerGroup.setCustomerId(customerId);
            String suffix = RandomStringUtils.randomAlphanumeric((int)(5 + Math.random()*10));
            String title = title1+suffix;
            title = i % 2 == 0 ? title.toLowerCase() : title.toUpperCase();
            customerGroup.setTitle(title);
            customerGroupsTitle1.add(doPost("/api/customer/group", customerGroup, CustomerGroup.class));
        }

        String title2 = "Customer Group title 2";
        List<CustomerGroup> customerGroupsTitle2 = new ArrayList<>();
        for (int i=0;i<175;i++) {
            CustomerGroup customerGroup = new CustomerGroup();
            customerGroup.setTenantId(tenantId);
            customerGroup.setCustomerId(customerId);
            String suffix = RandomStringUtils.randomAlphanumeric((int)(5 + Math.random()*10));
            String title = title2+suffix;
            title = i % 2 == 0 ? title.toLowerCase() : title.toUpperCase();
            customerGroup.setTitle(title);
            customerGroupsTitle2.add(doPost("/api/customer/group", customerGroup, CustomerGroup.class));
        }

        List<CustomerGroup> loadedCustomerGroupsTitle1 = new ArrayList<>();
        TextPageLink pageLink = new TextPageLink(15, title1);
        TextPageData<CustomerGroup> pageData = null;
        do {
            pageData = doGetTypedWithPageLink("/api/customer/"+customerId+"/groups?", new TypeReference<TextPageData<CustomerGroup>>(){}, pageLink);
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
            pageData = doGetTypedWithPageLink("/api/customer/"+customerId+"/groups?", new TypeReference<TextPageData<CustomerGroup>>(){}, pageLink);
            loadedCustomerGroupsTitle2.addAll(pageData.getData());
            if (pageData.hasNext()) {
                pageLink = pageData.getNextPageLink();
            }
        } while (pageData.hasNext());

        Collections.sort(customerGroupsTitle2, idComparator);
        Collections.sort(loadedCustomerGroupsTitle2, idComparator);

        Assert.assertEquals(customerGroupsTitle2, loadedCustomerGroupsTitle2);

        for (CustomerGroup customerGroup : loadedCustomerGroupsTitle1) {
            doDelete("/api/customer/group/"+customerGroup.getId().getId().toString())
                    .andExpect(status().isOk());
        }

        pageLink = new TextPageLink(4, title1);
        pageData = doGetTypedWithPageLink("/api/customer/"+customerId+"/groups?", new TypeReference<TextPageData<CustomerGroup>>(){}, pageLink);
        Assert.assertFalse(pageData.hasNext());
        Assert.assertEquals(0, pageData.getData().size());

        for (CustomerGroup customerGroup : loadedCustomerGroupsTitle2) {
            doDelete("/api/customer/group/"+customerGroup.getId().getId().toString())
                    .andExpect(status().isOk());
        }

        pageLink = new TextPageLink(4, title2);
        pageData = doGetTypedWithPageLink("/api/customer/"+customerId+"/groups?", new TypeReference<TextPageData<CustomerGroup>>(){}, pageLink);
        Assert.assertFalse(pageData.hasNext());
        Assert.assertEquals(0, pageData.getData().size());
    }
}
