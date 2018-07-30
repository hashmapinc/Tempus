package com.hashmapinc.server.dao.customergroup;

import com.google.common.util.concurrent.ListenableFuture;
import com.hashmapinc.server.common.data.CustomerGroup;
import com.hashmapinc.server.common.data.id.CustomerGroupId;
import com.hashmapinc.server.common.data.id.CustomerId;
import com.hashmapinc.server.common.data.id.TenantId;
import com.hashmapinc.server.common.data.page.TextPageData;
import com.hashmapinc.server.common.data.page.TextPageLink;

import java.util.Optional;

public interface CustomerGroupService {

    CustomerGroup findByCustomerGroupId(CustomerGroupId customerGroupId);

    ListenableFuture<CustomerGroup> findCustomerGroupByIdAsync(CustomerGroupId customerGroupId);

    Optional<CustomerGroup> findCustomerByTenantIdAndCustomerIdAndTitle(TenantId tenantId, CustomerId customerId, String title);

    CustomerGroup saveCustomerGroup(CustomerGroup customerGroup);

    void deleteCustomerGroup(CustomerGroupId customerGroupId);

    TextPageData<CustomerGroup> findCustomerGroupsByTenantIdAndCustomerId(TenantId tenantId, CustomerId customerId, TextPageLink pageLink);

    void deleteCustomerGroupsByTenantIdAndCustomerId(TenantId tenantId, CustomerId customerId);

}
