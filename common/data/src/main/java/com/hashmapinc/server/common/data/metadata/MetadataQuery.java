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

import com.hashmapinc.server.common.data.SearchTextBased;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class MetadataQuery extends SearchTextBased<MetadataQueryId> {

    private static final long serialVersionUID = -4071001721651568365L;

    private MetadataConfigId metadataConfigId;
    private String queryStmt;
    private MetadataIngestionTriggerType triggerType;
    private String triggerSchedule;

    @Override
    public String getSearchText() {
        return queryStmt;
    }
}