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
package com.hashmapinc.server.dao.model.nosql;

import com.datastax.driver.core.utils.UUIDs;
import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;
import com.fasterxml.jackson.databind.JsonNode;
import com.hashmapinc.server.common.data.Customer;
import com.hashmapinc.server.common.data.id.DataModelId;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import com.hashmapinc.server.common.data.id.CustomerId;
import com.hashmapinc.server.common.data.id.TenantId;
import com.hashmapinc.server.dao.model.SearchTextEntity;
import com.hashmapinc.server.dao.model.type.JsonCodec;

import java.util.UUID;

import static com.hashmapinc.server.dao.model.ModelConstants.*;

@Table(name = CUSTOMER_COLUMN_FAMILY_NAME)
@EqualsAndHashCode
@ToString
public final class CustomerEntity implements SearchTextEntity<Customer> {

    @PartitionKey(value = 0)
    @Column(name = ID_PROPERTY)
    private UUID id;
    
    @PartitionKey(value = 1)
    @Column(name = CUSTOMER_TENANT_ID_PROPERTY)
    private UUID tenantId;

    @Column(name = CUSTOMER_DATA_MODEL_ID_PROPERTY)
    private UUID dataModelId;
    
    @Column(name = CUSTOMER_TITLE_PROPERTY)
    private String title;
    
    @Column(name = SEARCH_TEXT_PROPERTY)
    private String searchText;
    
    @Column(name = COUNTRY_PROPERTY)
    private String country;
    
    @Column(name = STATE_PROPERTY)
    private String state;

    @Column(name = CITY_PROPERTY)
    private String city;

    @Column(name = ADDRESS_PROPERTY)
    private String address;

    @Column(name = ADDRESS2_PROPERTY)
    private String address2;

    @Column(name = ZIP_PROPERTY)
    private String zip;

    @Column(name = PHONE_PROPERTY)
    private String phone;

    @Column(name = EMAIL_PROPERTY)
    private String email;

    @Column(name = CUSTOMER_ADDITIONAL_INFO_PROPERTY, codec = JsonCodec.class)
    private JsonNode additionalInfo;

    public CustomerEntity() {
        super();
    }

    public CustomerEntity(Customer customer) {
        if (customer.getId() != null) {
            this.id = customer.getId().getId();
        }
        this.tenantId = customer.getTenantId().getId();
        this.dataModelId = customer.getDataModelId().getId();
        this.title = customer.getTitle();
        this.country = customer.getCountry();
        this.state = customer.getState();
        this.city = customer.getCity();
        this.address = customer.getAddress();
        this.address2 = customer.getAddress2();
        this.zip = customer.getZip();
        this.phone = customer.getPhone();
        this.email = customer.getEmail();
        this.additionalInfo = customer.getAdditionalInfo();
    }
    
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getTenantId() {
        return tenantId;
    }

    public void setTenantId(UUID tenantId) {
        this.tenantId = tenantId;
    }

    public UUID getDataModelId() {
        return dataModelId;
    }

    public void setDataModelId(UUID dataModelId) {
        this.dataModelId = dataModelId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getAddress2() {
        return address2;
    }

    public void setAddress2(String address2) {
        this.address2 = address2;
    }

    public String getZip() {
        return zip;
    }

    public void setZip(String zip) {
        this.zip = zip;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public JsonNode getAdditionalInfo() {
        return additionalInfo;
    }

    public void setAdditionalInfo(JsonNode additionalInfo) {
        this.additionalInfo = additionalInfo;
    }
    
    @Override
    public String getSearchTextSource() {
        return getTitle();
    }

    @Override
    public void setSearchText(String searchText) {
        this.searchText = searchText;
    }
    
    public String getSearchText() {
        return searchText;
    }

    @Override
    public Customer toData() {
        Customer customer = new Customer(new CustomerId(id));
        customer.setCreatedTime(UUIDs.unixTimestamp(id));
        if(dataModelId != null) {
            customer.setDataModelId(new DataModelId(dataModelId));
        } else {
            customer.setDataModelId(new DataModelId(NULL_UUID));
        }
        customer.setTenantId(new TenantId(tenantId));
        customer.setTitle(title);
        customer.setCountry(country);
        customer.setState(state);
        customer.setCity(city);
        customer.setAddress(address);
        customer.setAddress2(address2);
        customer.setZip(zip);
        customer.setPhone(phone);
        customer.setEmail(email);
        customer.setAdditionalInfo(additionalInfo);
        return customer;
    }

}