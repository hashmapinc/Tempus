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
import com.hashmapinc.server.common.data.asset.Asset;
import com.hashmapinc.server.common.data.id.TenantId;
import com.hashmapinc.server.dao.model.ModelConstants;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import com.hashmapinc.server.common.data.id.AssetId;
import com.hashmapinc.server.common.data.id.CustomerId;
import com.hashmapinc.server.dao.model.SearchTextEntity;
import com.hashmapinc.server.dao.model.type.JsonCodec;

import java.util.UUID;

@Table(name = ModelConstants.ASSET_COLUMN_FAMILY_NAME)
@EqualsAndHashCode
@ToString
public final class AssetEntity implements SearchTextEntity<Asset> {

    @PartitionKey(value = 0)
    @Column(name = ModelConstants.ID_PROPERTY)
    private UUID id;

    @PartitionKey(value = 1)
    @Column(name = ModelConstants.ASSET_TENANT_ID_PROPERTY)
    private UUID tenantId;

    @PartitionKey(value = 2)
    @Column(name = ModelConstants.ASSET_CUSTOMER_ID_PROPERTY)
    private UUID customerId;

    @PartitionKey(value = 3)
    @Column(name = ModelConstants.ASSET_TYPE_PROPERTY)
    private String type;

    @Column(name = ModelConstants.ASSET_NAME_PROPERTY)
    private String name;

    @Column(name = ModelConstants.SEARCH_TEXT_PROPERTY)
    private String searchText;

    @Column(name = ModelConstants.ASSET_ADDITIONAL_INFO_PROPERTY, codec = JsonCodec.class)
    private JsonNode additionalInfo;

    public AssetEntity() {
        super();
    }

    public AssetEntity(Asset asset) {
        if (asset.getId() != null) {
            this.id = asset.getId().getId();
        }
        if (asset.getTenantId() != null) {
            this.tenantId = asset.getTenantId().getId();
        }
        if (asset.getCustomerId() != null) {
            this.customerId = asset.getCustomerId().getId();
        }
        this.name = asset.getName();
        this.type = asset.getType();
        this.additionalInfo = asset.getAdditionalInfo();
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

    public UUID getCustomerId() {
        return customerId;
    }

    public void setCustomerId(UUID customerId) {
        this.customerId = customerId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public JsonNode getAdditionalInfo() {
        return additionalInfo;
    }

    public void setAdditionalInfo(JsonNode additionalInfo) {
        this.additionalInfo = additionalInfo;
    }

    @Override
    public String getSearchTextSource() {
        return getName();
    }

    @Override
    public void setSearchText(String searchText) {
        this.searchText = searchText;
    }

    public String getSearchText() {
        return searchText;
    }

    @Override
    public Asset toData() {
        Asset asset = new Asset(new AssetId(id));
        asset.setCreatedTime(UUIDs.unixTimestamp(id));
        if (tenantId != null) {
            asset.setTenantId(new TenantId(tenantId));
        }
        if (customerId != null) {
            asset.setCustomerId(new CustomerId(customerId));
        }
        asset.setName(name);
        asset.setType(type);
        asset.setAdditionalInfo(additionalInfo);
        return asset;
    }

}