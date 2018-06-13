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
package com.hashmapinc.server.dao.model.sql;


import com.datastax.driver.core.utils.UUIDs;
import com.fasterxml.jackson.databind.JsonNode;
import com.hashmapinc.server.common.data.datamodel.DataModel;
import com.hashmapinc.server.common.data.id.DataModelId;
import com.hashmapinc.server.common.data.id.TenantId;
import com.hashmapinc.server.dao.model.BaseSqlEntity;
import com.hashmapinc.server.dao.model.ModelConstants;
import com.hashmapinc.server.dao.model.SearchTextEntity;
import com.hashmapinc.server.dao.util.mapping.JsonStringType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@TypeDef(name = "json", typeClass = JsonStringType.class)
@Table(name = ModelConstants.DATA_MODEL_TABLE_NAME)
public class DataModelEntity extends BaseSqlEntity<DataModel> implements SearchTextEntity<DataModel> {


    @Column(name = ModelConstants.DATA_MODEL_TENANT_ID_PROPERTY)
    private String tenantId;

    @Column(name = ModelConstants.DATA_MODEL_NAME_PROPERTY)
    private String name;

    @Column(name = ModelConstants.SEARCH_TEXT_PROPERTY)
    private String searchText;

    @Type(type = "json")
    @Column(name = ModelConstants.DATA_MODEL_ADDITIONAL_INFO_PROPERTY)
    private JsonNode additionalInfo;

    @Column(name = ModelConstants.LAST_UPDATE_TS_COLUMN)
    private Long lastUpdateTs;


    public DataModelEntity() {
        super();
    }

    public DataModelEntity(DataModel dataModel) {
        if (dataModel.getId() != null) {
            this.setId(dataModel.getId().getId());
        }
        if (dataModel.getTenantId() != null) {
            this.tenantId = toString(dataModel.getTenantId().getId());
        }
        this.name = dataModel.getName();
        this.additionalInfo = dataModel.getAdditionalInfo();
        this.lastUpdateTs = dataModel.getLastUpdatedTs();
    }

    @Override
    public String getSearchTextSource() {
        return name;
    }

    @Override
    public void setSearchText(String searchText) {
        this.searchText = searchText;
    }

    @Override
    public DataModel toData() {
        DataModel dataModel = new DataModel(new DataModelId(getId()));
        dataModel.setCreatedTime(UUIDs.unixTimestamp(getId()));
        if (tenantId != null) {
            dataModel.setTenantId(new TenantId(toUUID(tenantId)));
        }
        dataModel.setName(name);
        dataModel.setAdditionalInfo(additionalInfo);
        dataModel.setLastUpdatedTs(lastUpdateTs);
        return dataModel;
    }
}
