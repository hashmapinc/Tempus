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
package com.hashmapinc.server.common.data.asset;

import com.hashmapinc.server.common.data.relation.EntityRelationsQuery;
import com.hashmapinc.server.common.data.relation.EntityTypeFilter;
import lombok.Data;
import com.hashmapinc.server.common.data.EntityType;
import com.hashmapinc.server.common.data.relation.EntityRelation;
import com.hashmapinc.server.common.data.relation.RelationsSearchParameters;

import java.util.Collections;
import java.util.List;

/**
 * Created by ashvayka on 03.05.17.
 */
@Data
public class AssetSearchQuery {

    private RelationsSearchParameters parameters;
    private String relationType;
    private List<String> assetTypes;

    public EntityRelationsQuery toEntitySearchQuery() {
        EntityRelationsQuery query = new EntityRelationsQuery();
        query.setParameters(parameters);
        query.setFilters(
                Collections.singletonList(new EntityTypeFilter(relationType == null ? EntityRelation.CONTAINS_TYPE : relationType,
                        Collections.singletonList(EntityType.ASSET))));
        return query;
    }
}
