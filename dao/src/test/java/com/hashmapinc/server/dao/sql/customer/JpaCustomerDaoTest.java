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
package com.hashmapinc.server.dao.sql.customer;

import com.datastax.driver.core.utils.UUIDs;
import com.hashmapinc.server.common.data.Customer;
import com.hashmapinc.server.common.data.id.TenantId;
import com.hashmapinc.server.common.data.page.TextPageLink;
import com.hashmapinc.server.dao.customer.CustomerDao;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import com.hashmapinc.server.common.data.id.CustomerId;
import com.hashmapinc.server.dao.AbstractJpaDaoTest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by Valerii Sosliuk on 5/6/2017.
 */
public class JpaCustomerDaoTest extends AbstractJpaDaoTest {

    @Autowired
    private CustomerDao customerDao;

    @Test
    public void testFindByTenantId() {
        UUID tenantId1 = UUIDs.timeBased();
        UUID tenantId2 = UUIDs.timeBased();

        for (int i = 0; i < 20; i++) {
            createCustomer(tenantId1, i);
            createCustomer(tenantId2, i * 2);
        }

        TextPageLink pageLink1 = new TextPageLink(15, "CUSTOMER");
        List<Customer> customers1 = customerDao.findCustomersByTenantId(tenantId1, pageLink1);
        assertEquals(15, customers1.size());

        TextPageLink pageLink2 = new TextPageLink(15, "CUSTOMER", customers1.get(14).getId().getId(), null);
        List<Customer> customers2 = customerDao.findCustomersByTenantId(tenantId1, pageLink2);
        assertEquals(5, customers2.size());
    }

    @Test
    public void testFindCustomersByTenantIdAndTitle() {
        UUID tenantId = UUIDs.timeBased();

        for (int i = 0; i < 10; i++) {
            createCustomer(tenantId, i);
        }

        Optional<Customer> customerOpt = customerDao.findCustomersByTenantIdAndTitle(tenantId, "CUSTOMER_5");
        assertTrue(customerOpt.isPresent());
        assertEquals("CUSTOMER_5", customerOpt.get().getTitle());
    }

    private void createCustomer(UUID tenantId, int index) {
        Customer customer = new Customer();
        customer.setId(new CustomerId(UUIDs.timeBased()));
        customer.setTenantId(new TenantId(tenantId));
        customer.setTitle("CUSTOMER_" + index);
        customerDao.save(customer);
    }
}
