package com.hashmapinc.server.dao.model.sql;

import com.datastax.driver.core.utils.UUIDs;
import com.fasterxml.jackson.databind.JsonNode;
import com.hashmapinc.server.common.data.Customer;
import com.hashmapinc.server.common.data.CustomerGroup;
import com.hashmapinc.server.common.data.UUIDConverter;
import com.hashmapinc.server.common.data.id.CustomerGroupId;
import com.hashmapinc.server.common.data.id.CustomerId;
import com.hashmapinc.server.common.data.id.TenantId;
import com.hashmapinc.server.dao.model.BaseSqlEntity;
import com.hashmapinc.server.dao.model.ModelConstants;
import com.hashmapinc.server.dao.model.SearchTextEntity;
import com.hashmapinc.server.dao.util.mapping.JsonStringType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.*;
import java.util.List;

@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@TypeDef(name = "json", typeClass = JsonStringType.class)
@Table(name = ModelConstants.CUSTOMER_GROUP_TABLE_NAME)
public class CustomerGroupEntity extends BaseSqlEntity<CustomerGroup> implements SearchTextEntity<CustomerGroup> {

    @Column(name = ModelConstants.CUSTOMER_GROUP_TITLE)
    private String title;

    @Column(name = ModelConstants.CUSTOMER_GROUP_TENANT_ID)
    private String tenantId;

    @Column(name = ModelConstants.CUSTOMER_GROUP_CUSTOMER_ID)
    private String customerId;

    @ElementCollection()
    @CollectionTable(name = ModelConstants.CUSTOMER_GROUP_POLICY_TABLE_NAME, joinColumns = @JoinColumn(name = ModelConstants.CUSTOMER_GROUP_POLICY_ID))
    @Column(name = ModelConstants.CUSTOMER_GROUP_POLICY_COLUMN)
    private List<String> policies;

    @Type(type = "json")
    @Column(name = ModelConstants.CUSTOMER_ADDITIONAL_INFO_PROPERTY)
    private JsonNode additionalInfo;

    @Column(name = ModelConstants.SEARCH_TEXT_PROPERTY)
    private String searchText;

    public CustomerGroupEntity(CustomerGroup customerGroup) {
        if (customerGroup.getId() != null) {
            this.setId(customerGroup.getId().getId());
        }
        this.tenantId = UUIDConverter.fromTimeUUID(customerGroup.getTenantId().getId());
        this.customerId = UUIDConverter.fromTimeUUID(customerGroup.getCustomerId().getId());
        this.title = customerGroup.getTitle();
        this.policies = customerGroup.getPolicies();
        this.additionalInfo = customerGroup.getAdditionalInfo();
    }

    @Override
    public String getSearchTextSource() {
        return title;
    }

    @Override
    public void setSearchText(String searchText) {
        this.searchText = searchText;
    }

    @Override
    public CustomerGroup toData() {
        CustomerGroup customerGroup = new CustomerGroup(new CustomerGroupId(getId()));
        customerGroup.setCreatedTime(UUIDs.unixTimestamp(getId()));
        customerGroup.setTenantId(new TenantId(UUIDConverter.fromString(tenantId)));
        customerGroup.setCustomerId(new CustomerId(UUIDConverter.fromString(tenantId)));
        customerGroup.setTitle(title);
        customerGroup.setAdditionalInfo(additionalInfo);
        return customerGroup;
    }
}
