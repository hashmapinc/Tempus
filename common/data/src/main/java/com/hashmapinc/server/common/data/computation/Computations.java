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
package com.hashmapinc.server.common.data.computation;

import com.fasterxml.jackson.databind.JsonNode;
import com.hashmapinc.server.common.data.SearchTextBased;
import com.hashmapinc.server.common.data.id.ComputationId;
import com.hashmapinc.server.common.data.HasName;
import com.hashmapinc.server.common.data.id.TenantId;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class Computations extends SearchTextBased<ComputationId> implements HasName {

    private String name;
    private String jarPath;
    private TenantId tenantId;
    private String jarName;
    private String mainClass;
    private JsonNode jsonDescriptor;
    private String argsformat;
    private String argsType;
    private ComputationType type;

    public Computations() {
        super();
    }

    public Computations(ComputationId id) {
        super(id);
    }

    public Computations(Computations computations) {
        super(computations);
        this.name = computations.name;
        this.jarPath = computations.jarPath;
        this.jarName = computations.jarName;
        this.tenantId = computations.tenantId;
        this.argsformat = computations.argsformat;
        this.jsonDescriptor = computations.jsonDescriptor;
        this.mainClass = computations.mainClass;
        this.argsType = computations.argsType;
    }

    @Override
    public String getName() {
        return name;
    }
    @Override
    public String getSearchText() {
        return name;
    }
}
