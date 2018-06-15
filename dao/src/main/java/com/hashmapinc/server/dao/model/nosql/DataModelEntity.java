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
package com.hashmapinc.server.dao.model.nosql;

import com.datastax.driver.core.utils.UUIDs;
import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;
import com.fasterxml.jackson.databind.JsonNode;
import com.hashmapinc.server.common.data.datamodel.DataModel;
import com.hashmapinc.server.common.data.id.DataModelId;
import com.hashmapinc.server.common.data.id.TenantId;
import com.hashmapinc.server.dao.model.ModelConstants;
import com.hashmapinc.server.dao.model.SearchTextEntity;
import com.hashmapinc.server.dao.model.type.JsonCodec;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.UUID;

import static com.hashmapinc.server.dao.model.ModelConstants.*;


@Table(name = DATA_MODEL_TABLE_NAME)
@EqualsAndHashCode
@ToString
@Data
public final class DataModelEntity implements SearchTextEntity<DataModel> {

    @PartitionKey(value = 0)
    @Column(name = ID_PROPERTY)
    private UUID id;

    @PartitionKey(value = 1)
    @Column(name = DATA_MODEL_TENANT_ID_PROPERTY)
    private UUID tenantId;

    @Column(name = DATA_MODEL_NAME_PROPERTY)
    private String name;

    @Column(name = SEARCH_TEXT_PROPERTY)
    private String searchText;

    @Column(name = DATA_MODEL_ADDITIONAL_INFO_PROPERTY, codec = JsonCodec.class)
    private JsonNode additionalInfo;

    @Column(name = ModelConstants.LAST_UPDATE_TS_COLUMN)
    private Long lastUpdateTs;

    public DataModelEntity() {
        super();
    }

    public DataModelEntity(DataModel dataModel) {
        if (dataModel.getId() != null) {
            this.id = dataModel.getId().getId();
        }
        if (dataModel.getTenantId() != null) {
            this.tenantId = dataModel.getTenantId().getId();
        }
        this.name = dataModel.getName();
        this.additionalInfo = dataModel.getAdditionalInfo();
        this.lastUpdateTs = dataModel.getLastUpdatedTs();
    }

    @Override
    public String getSearchTextSource() {
        return getName();
    }

    @Override
    public void setSearchText(String searchText) {
        this.searchText = searchText;
    }


    @Override
    public DataModel toData() {
        DataModel dataModel = new DataModel(new DataModelId(id));
        dataModel.setCreatedTime(UUIDs.unixTimestamp(id));
        if (tenantId != null) {
            dataModel.setTenantId(new TenantId(tenantId));
        }
        dataModel.setName(name);
        dataModel.setAdditionalInfo(additionalInfo);
        dataModel.setLastUpdatedTs(lastUpdateTs);
        return dataModel;
    }
}
