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
package com.hashmapinc.server.common.data;

import com.hashmapinc.server.common.data.kv.MetaDataKvEntry;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class MetadataIngestionEntries {

    private String tenantId;
    private String metadataConfigId;
    private String metadataSourceName;
    private List<MetaDataKvEntry> metaDataKvEntries;

    public MetadataIngestionEntries(String tenantId, String metadataConfigId, String metadataSourceName, List<MetaDataKvEntry> metaDataKvEntries) {
        this.tenantId = tenantId;
        this.metadataConfigId = metadataConfigId;
        this.metaDataKvEntries = metaDataKvEntries;
        this.metadataSourceName = metadataSourceName;
    }
}
