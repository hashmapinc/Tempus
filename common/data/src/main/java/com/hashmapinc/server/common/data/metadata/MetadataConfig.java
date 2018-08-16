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
package com.hashmapinc.server.common.data.metadata;

import com.hashmapinc.server.common.data.BaseData;
import com.hashmapinc.server.common.data.metadata.source.MetadataSource;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode(callSuper = true)
@Data
@ToString
public class MetadataConfig extends BaseData<MetadataConfigId> {

    private static final long serialVersionUID = -4951937006489620857L;

    private String ownerId;
    private String name;
    private MetadataSource source;
    private MetadataSource sink;
    private MetadataIngestionTriggerType triggerType;
    private String triggerSchedule;
}