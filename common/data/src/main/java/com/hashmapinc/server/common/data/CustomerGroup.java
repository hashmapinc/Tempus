package com.hashmapinc.server.common.data;

import com.hashmapinc.server.common.data.id.CustomerGroupId;
import com.hashmapinc.server.common.data.id.CustomerId;
import com.hashmapinc.server.common.data.id.TenantId;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString
public class CustomerGroup extends SearchTextBasedWithAdditionalInfo<CustomerGroupId> implements HasName {
    private static final long serialVersionUID = -5520737431477399572L;
    private String title;
    private TenantId tenantId;
    private CustomerId customerId;
    private List<String> policies;

    public CustomerGroup(CustomerGroupId id) {
        super(id);
    }

    public  CustomerGroup(CustomerGroup customerGroup){
        super(customerGroup);
        this.title = customerGroup.title;
        this.tenantId = customerGroup.tenantId;
        this.customerId = customerGroup.customerId;
        this.policies = customerGroup.policies;
    }

    @Override
    public String getName() {
        return getTitle();
    }

    @Override
    public String getSearchText() {
        return getTitle();
    }
}
