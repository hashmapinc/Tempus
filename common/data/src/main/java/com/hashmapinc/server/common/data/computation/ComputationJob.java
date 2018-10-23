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
package com.hashmapinc.server.common.data.computation;

import com.hashmapinc.server.common.data.HasName;
import com.hashmapinc.server.common.data.SearchTextBased;
import com.hashmapinc.server.common.data.id.ComputationId;
import com.hashmapinc.server.common.data.id.ComputationJobId;
import com.hashmapinc.server.common.data.id.TenantId;
import com.hashmapinc.server.common.data.plugin.ComponentLifecycleState;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ComputationJob extends SearchTextBased<ComputationJobId> implements HasName {

    private static final long serialVersionUID = 7428719692971356844L;
    private TenantId tenantId;
    private ComputationId computationId;
    private String name;
    private ComponentLifecycleState state;
    private ComputationJobConfiguration configuration;

    public ComputationJob() {
        super();
    }

    public ComputationJob(ComputationJobId id) {
        super(id);
    }

    public ComputationJob(ComputationJob computationJob) {
        super(computationJob);
        this.computationId = computationJob.computationId;
        this.tenantId = computationJob.tenantId;
        this.name = computationJob.name;
        this.state = computationJob.state;
        this.configuration = computationJob.configuration;
    }

    @Override
    public String getName() {
        return name;
    }
    @Override
    public String getSearchText() {
        return getName();
    }

    public void suspend(){
        this.getConfiguration().markSuspended();
    }
}
