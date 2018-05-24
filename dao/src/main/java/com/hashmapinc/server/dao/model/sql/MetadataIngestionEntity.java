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
package com.hashmapinc.server.dao.model.sql;

import com.hashmapinc.server.common.data.kv.MetaDataKvEntry;
import com.hashmapinc.server.common.data.kv.StringDataEntry;
import com.hashmapinc.server.dao.model.ModelConstants;
import com.hashmapinc.server.dao.model.ToData;
import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;

@Data
@Entity
@Table(name = ModelConstants.METADATA_ENTRIES_TABLE)
@IdClass(MetadataIngestionEntityCompositeKey.class)
public class MetadataIngestionEntity implements ToData<MetaDataKvEntry>, Serializable {
    @Id
    @Column(name = ModelConstants.TENANT_ID_PROPERTY)
    private String tenantId;

    @Id
    @Column(name = ModelConstants.METADATA_CONFIG_ID)
    private String metadataConfigId;

    @Id
    @Column(name = ModelConstants.METADATA_INGESTION_KEY_COLUMN)
    private String key;

    @Id
    @Column(name = ModelConstants.METADATA_DATASOURCE_NAME_COLUMN)
    private String metadataSourceName;

    @Column(name = ModelConstants.METADATA_INGESTION_VALUE_COLUMN)
    private String value;

    @Column(name = ModelConstants.LAST_UPDATE_TS_COLUMN)
    private Long lastUpdateTs;

    @Override
    public MetaDataKvEntry toData() {
        return new MetaDataKvEntry(new StringDataEntry(key, value), lastUpdateTs);
    }
}
