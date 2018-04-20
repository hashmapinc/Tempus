/**
 * Copyright © 2016-2018 Hashmap, Inc
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
package org.thingsboard.server.controller;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.RandomStringUtils;
import org.thingsboard.server.common.data.Customer;
import org.thingsboard.server.common.data.Tenant;
import org.thingsboard.server.common.data.User;
import org.thingsboard.server.common.data.id.TenantId;
import org.thingsboard.server.common.data.page.TextPageData;
import org.thingsboard.server.common.data.page.TextPageLink;
import org.thingsboard.server.common.data.security.Authority;
import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.core.type.TypeReference;

public abstract class BaseCustomerControllerTest extends AbstractControllerTest {

    private IdComparator<Customer> idComparator = new IdComparator<>();
    
    @Test
    public void testSaveCustomer() throws Exception {
        loginSysAdmin();

        Tenant tenant = new Tenant();
        tenant.setTitle("My tenant");
        Tenant savedTenant = doPost("/api/tenant", tenant, Tenant.class);
        Assert.assertNotNull(savedTenant);
        
        User tenantAdmin = new User();
        tenantAdmin.setAuthority(Authority.TENANT_ADMIN);
        tenantAdmin.setTenantId(savedTenant.getId());
        tenantAdmin.setEmail("tenant2@thingsboard.org");
        tenantAdmin.setFirstName("Joe");
        tenantAdmin.setLastName("Downs");
        
        tenantAdmin = createUserAndLogin(tenantAdmin, "testPassword1");
        
        Customer customer = new Customer();
        customer.setTitle("My customer");
        Customer savedCustomer = doPost("/api/customer", customer, Customer.class);
        Assert.assertNotNull(savedCustomer);
        Assert.assertNotNull(savedCustomer.getId());
        Assert.assertTrue(savedCustomer.getCreatedTime() > 0);
        Assert.assertEquals(customer.getTitle(), savedCustomer.getTitle());
        savedCustomer.setTitle("My new customer");
        doPost("/api/customer", savedCustomer, Customer.class);
        
        Customer foundCustomer = doGet("/api/customer/"+savedCustomer.getId().getId().toString(), Customer.class); 
        Assert.assertEquals(foundCustomer.getTitle(), savedCustomer.getTitle());
        
        doDelete("/api/customer/"+savedCustomer.getId().getId().toString())
        .andExpect(status().isOk());
        
        loginSysAdmin();
        
        doDelete("/api/tenant/"+savedTenant.getId().getId().toString())
        .andExpect(status().isOk());
    }
    
    @Test
    public void testFindCustomerById() throws Exception {
        
        loginSysAdmin();

        Tenant tenant = new Tenant();
        tenant.setTitle("My tenant");
        Tenant savedTenant = doPost("/api/tenant", tenant, Tenant.class);
        Assert.assertNotNull(savedTenant);
        
        User tenantAdmin = new User();
        tenantAdmin.setAuthority(Authority.TENANT_ADMIN);
        tenantAdmin.setTenantId(savedTenant.getId());
        tenantAdmin.setEmail("tenant2@thingsboard.org");
        tenantAdmin.setFirstName("Joe");
        tenantAdmin.setLastName("Downs");
        
        tenantAdmin = createUserAndLogin(tenantAdmin, "testPassword1");
        
        Customer customer = new Customer();
        customer.setTitle("My customer");
        Customer savedCustomer = doPost("/api/customer", customer, Customer.class);
        
        Customer foundCustomer = doGet("/api/customer/"+savedCustomer.getId().getId().toString(), Customer.class);
        Assert.assertNotNull(foundCustomer);
        Assert.assertEquals(savedCustomer, foundCustomer);
        
        doDelete("/api/customer/"+savedCustomer.getId().getId().toString())
        .andExpect(status().isOk());
        
        loginSysAdmin();
        
        doDelete("/api/tenant/"+savedTenant.getId().getId().toString())
        .andExpect(status().isOk());
    }
    
    @Test
    public void testDeleteCustomer() throws Exception {
        
        loginSysAdmin();

        Tenant tenant = new Tenant();
        tenant.setTitle("My tenant");
        Tenant savedTenant = doPost("/api/tenant", tenant, Tenant.class);
        Assert.assertNotNull(savedTenant);
        
        User tenantAdmin = new User();
        tenantAdmin.setAuthority(Authority.TENANT_ADMIN);
        tenantAdmin.setTenantId(savedTenant.getId());
        tenantAdmin.setEmail("tenant2@thingsboard.org");
        tenantAdmin.setFirstName("Joe");
        tenantAdmin.setLastName("Downs");
        
        tenantAdmin = createUserAndLogin(tenantAdmin, "testPassword1");
        
        Customer customer = new Customer();
        customer.setTitle("My customer");
        Customer savedCustomer = doPost("/api/customer", customer, Customer.class);
        
        doDelete("/api/customer/"+savedCustomer.getId().getId().toString())
        .andExpect(status().isOk());

        doGet("/api/customer/"+savedCustomer.getId().getId().toString())
        .andExpect(status().isNotFound());
        
        loginSysAdmin();
        
        doDelete("/api/tenant/"+savedTenant.getId().getId().toString())
        .andExpect(status().isOk());
    }
    
    @Test
    public void testSaveCustomerWithEmptyTitle() throws Exception {
        
        loginSysAdmin();

        Tenant tenant = new Tenant();
        tenant.setTitle("My tenant");
        Tenant savedTenant = doPost("/api/tenant", tenant, Tenant.class);
        Assert.assertNotNull(savedTenant);
        
        User tenantAdmin = new User();
        tenantAdmin.setAuthority(Authority.TENANT_ADMIN);
        tenantAdmin.setTenantId(savedTenant.getId());
        tenantAdmin.setEmail("tenant2@thingsboard.org");
        tenantAdmin.setFirstName("Joe");
        tenantAdmin.setLastName("Downs");
        
        tenantAdmin = createUserAndLogin(tenantAdmin, "testPassword1");
        
        Customer customer = new Customer();
        doPost("/api/customer", customer)
        .andExpect(status().isBadRequest())
        .andExpect(statusReason(containsString("Customer title should be specified")));
        
        loginSysAdmin();
        
        doDelete("/api/tenant/"+savedTenant.getId().getId().toString())
        .andExpect(status().isOk());
    }
    
    @Test
    public void testSaveCustomerWithInvalidEmail() throws Exception {
        
        loginSysAdmin();

        Tenant tenant = new Tenant();
        tenant.setTitle("My tenant");
        Tenant savedTenant = doPost("/api/tenant", tenant, Tenant.class);
        Assert.assertNotNull(savedTenant);
        
        User tenantAdmin = new User();
        tenantAdmin.setAuthority(Authority.TENANT_ADMIN);
        tenantAdmin.setTenantId(savedTenant.getId());
        tenantAdmin.setEmail("tenant2@thingsboard.org");
        tenantAdmin.setFirstName("Joe");
        tenantAdmin.setLastName("Downs");
        
        tenantAdmin = createUserAndLogin(tenantAdmin, "testPassword1");
        
        Customer customer = new Customer();
        customer.setTitle("My customer");
        customer.setEmail("invalid@mail");
        doPost("/api/customer", customer)
        .andExpect(status().isBadRequest())
        .andExpect(statusReason(containsString("Invalid email address format 'invalid@mail'")));
        
        loginSysAdmin();
        
        doDelete("/api/tenant/"+savedTenant.getId().getId().toString())
        .andExpect(status().isOk());
    }
    
    @Test
    public void testFindCustomers() throws Exception {
        loginSysAdmin();

        Tenant tenant = new Tenant();
        tenant.setTitle("My tenant");
        Tenant savedTenant = doPost("/api/tenant", tenant, Tenant.class);
        Assert.assertNotNull(savedTenant);
        
        TenantId tenantId = savedTenant.getId();
        
        User tenantAdmin = new User();
        tenantAdmin.setAuthority(Authority.TENANT_ADMIN);
        tenantAdmin.setTenantId(tenantId);
        tenantAdmin.setEmail("tenant2@thingsboard.org");
        tenantAdmin.setFirstName("Joe");
        tenantAdmin.setLastName("Downs");
        
        tenantAdmin = createUserAndLogin(tenantAdmin, "testPassword1");
        
        List<Customer> customers = new ArrayList<>();
        for (int i=0;i<135;i++) {
            Customer customer = new Customer();
            customer.setTenantId(tenantId);
            customer.setTitle("Customer"+i);
            customers.add(doPost("/api/customer", customer, Customer.class));
        }
        
        List<Customer> loadedCustomers = new ArrayList<>();
        TextPageLink pageLink = new TextPageLink(23);
        TextPageData<Customer> pageData = null;
        do {
            pageData = doGetTypedWithPageLink("/api/customers?", new TypeReference<TextPageData<Customer>>(){}, pageLink);
            loadedCustomers.addAll(pageData.getData());
            if (pageData.hasNext()) {
                pageLink = pageData.getNextPageLink();
            }
        } while (pageData.hasNext());
        
        Collections.sort(customers, idComparator);
        Collections.sort(loadedCustomers, idComparator);
        
        Assert.assertEquals(customers, loadedCustomers);
        
        loginSysAdmin();
        
        doDelete("/api/tenant/"+savedTenant.getId().getId().toString())
        .andExpect(status().isOk());
    }
    
    @Test
    public void testFindCustomersByTitle() throws Exception {
        
        loginSysAdmin();
        
        Tenant tenant = new Tenant();
        tenant.setTitle("My tenant");
        Tenant savedTenant = doPost("/api/tenant", tenant, Tenant.class);
        Assert.assertNotNull(savedTenant);
        
        TenantId tenantId = savedTenant.getId();
        
        User tenantAdmin = new User();
        tenantAdmin.setAuthority(Authority.TENANT_ADMIN);
        tenantAdmin.setTenantId(tenantId);
        tenantAdmin.setEmail("tenant2@thingsboard.org");
        tenantAdmin.setFirstName("Joe");
        tenantAdmin.setLastName("Downs");
        
        tenantAdmin = createUserAndLogin(tenantAdmin, "testPassword1");
        
        String title1 = "Customer title 1";
        List<Customer> customersTitle1 = new ArrayList<>();
        for (int i=0;i<143;i++) {
            Customer customer = new Customer();
            customer.setTenantId(tenantId);
            String suffix = RandomStringUtils.randomAlphanumeric((int)(5 + Math.random()*10));
            String title = title1+suffix;
            title = i % 2 == 0 ? title.toLowerCase() : title.toUpperCase();
            customer.setTitle(title);
            customersTitle1.add(doPost("/api/customer", customer, Customer.class));
        }
        String title2 = "Customer title 2";
        List<Customer> customersTitle2 = new ArrayList<>();
        for (int i=0;i<175;i++) {
            Customer customer = new Customer();
            customer.setTenantId(tenantId);
            String suffix = RandomStringUtils.randomAlphanumeric((int)(5 + Math.random()*10));
            String title = title2+suffix;
            title = i % 2 == 0 ? title.toLowerCase() : title.toUpperCase();
            customer.setTitle(title);
            customersTitle2.add(doPost("/api/customer", customer, Customer.class));
        }
        
        List<Customer> loadedCustomersTitle1 = new ArrayList<>();
        TextPageLink pageLink = new TextPageLink(15, title1);
        TextPageData<Customer> pageData = null;
        do {
            pageData = doGetTypedWithPageLink("/api/customers?", new TypeReference<TextPageData<Customer>>(){}, pageLink);
            loadedCustomersTitle1.addAll(pageData.getData());
            if (pageData.hasNext()) {
                pageLink = pageData.getNextPageLink();
            }
        } while (pageData.hasNext());
        
        Collections.sort(customersTitle1, idComparator);
        Collections.sort(loadedCustomersTitle1, idComparator);
        
        Assert.assertEquals(customersTitle1, loadedCustomersTitle1);
        
        List<Customer> loadedCustomersTitle2 = new ArrayList<>();
        pageLink = new TextPageLink(4, title2);
        do {
            pageData = doGetTypedWithPageLink("/api/customers?", new TypeReference<TextPageData<Customer>>(){}, pageLink);
            loadedCustomersTitle2.addAll(pageData.getData());
            if (pageData.hasNext()) {
                pageLink = pageData.getNextPageLink();
            }
        } while (pageData.hasNext());

        Collections.sort(customersTitle2, idComparator);
        Collections.sort(loadedCustomersTitle2, idComparator);
        
        Assert.assertEquals(customersTitle2, loadedCustomersTitle2);
        
        for (Customer customer : loadedCustomersTitle1) {
            doDelete("/api/customer/"+customer.getId().getId().toString())
            .andExpect(status().isOk());    
        }
        
        pageLink = new TextPageLink(4, title1);
        pageData = doGetTypedWithPageLink("/api/customers?", new TypeReference<TextPageData<Customer>>(){}, pageLink);
        Assert.assertFalse(pageData.hasNext());
        Assert.assertEquals(0, pageData.getData().size());
        
        for (Customer customer : loadedCustomersTitle2) {
            doDelete("/api/customer/"+customer.getId().getId().toString())
            .andExpect(status().isOk());    
        }
        
        pageLink = new TextPageLink(4, title2);
        pageData = doGetTypedWithPageLink("/api/customers?", new TypeReference<TextPageData<Customer>>(){}, pageLink);
        Assert.assertFalse(pageData.hasNext());
        Assert.assertEquals(0, pageData.getData().size());
        
        loginSysAdmin();
        
        doDelete("/api/tenant/"+savedTenant.getId().getId().toString())
        .andExpect(status().isOk());
    }
    
}
