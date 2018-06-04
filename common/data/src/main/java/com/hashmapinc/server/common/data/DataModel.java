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
package com.hashmapinc.server.common.data;

import com.hashmapinc.server.common.data.id.DataModelId;
import com.hashmapinc.server.common.data.id.TenantId;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
public class DataModel extends SearchTextBasedWithAdditionalInfo<DataModelId> implements HasName {
    private static final long serialVersionUID = 5100770312089703084L;

    private TenantId tenantId;
    private String name;
    private long lastUpdatedTs;

    public DataModel() {
        super();
    }

    public DataModel(DataModelId id) {
        super(id);
    }

    public DataModel(DataModel dataModel) {
        super(dataModel);
        this.tenantId = dataModel.getTenantId();
        this.name = dataModel.getName();
        this.lastUpdatedTs = dataModel.lastUpdatedTs;
    }

    public TenantId getTenantId() {
        return tenantId;
    }

    public void setTenantId(TenantId tenantId) {
        this.tenantId = tenantId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getLastUpdatedTs() {
        return lastUpdatedTs;
    }

    public void setLastUpdatedTs(long lastUpdatedTs) {
        this.lastUpdatedTs = lastUpdatedTs;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getSearchText() {
        return getName();
    }
}
