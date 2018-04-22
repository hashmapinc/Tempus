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
import com.hashmapinc.server.common.data.HasName;
import com.hashmapinc.server.common.data.id.ComputationId;
import com.hashmapinc.server.common.data.id.ComputationJobId;
import com.hashmapinc.server.common.data.id.TenantId;
import com.hashmapinc.server.common.data.plugin.ComponentLifecycleState;

public class ComputationJob extends SearchTextBased<ComputationJobId> implements HasName {

    private TenantId tenantId;
    private ComputationId computationId;
    private JsonNode argParameters;
    private String name;
    private ComponentLifecycleState state;
    private String jobId;

    public ComputationJob() {
        super();
    }

    public ComputationJob(ComputationJobId id) {
        super(id);
    }

    public ComputationJob(ComputationJob computationJob) {
        super(computationJob);
        this.argParameters = computationJob.argParameters;
        this.computationId = computationJob.computationId;
        this.tenantId = computationJob.tenantId;
        this.name = computationJob.name;
        this.state = computationJob.state;
        this.jobId = computationJob.jobId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        ComputationJob that = (ComputationJob) o;
        if (argParameters != null ? !argParameters.equals(that.argParameters) : that.argParameters != null) return false;
        if (computationId != null ? !computationId.equals(that.computationId) : that.computationId != null) return false;
        if (tenantId != null ? !tenantId.equals(that.tenantId) : that.tenantId != null) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (jobId != null ? !jobId.equals(that.jobId) : that.jobId != null) return false;
        return true;
    }

    @Override

    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (tenantId != null ? tenantId.hashCode() : 0);
        result = 31 * result + (argParameters != null ? argParameters.hashCode() : 0);
        result = 31 * result + (computationId != null ? computationId.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (jobId != null ? jobId.hashCode() : 0);
        return result;
    }

    public void setName(String name) {
        this.name = name;
    }

    public TenantId getTenantId() {
        return tenantId;
    }

    public void setTenantId(TenantId tenantId) {
        this.tenantId = tenantId;
    }

    public ComponentLifecycleState getState() {
        return state;
    }

    public void setState(ComponentLifecycleState state) {
        this.state = state;
    }

    public ComputationId getComputationId() {
        return computationId;
    }

    public void setComputationId(ComputationId computationId) {
        this.computationId = computationId;
    }

    public JsonNode getArgParameters() {
        return argParameters;
    }

    public void setArgParameters(JsonNode argParameters) {
        this.argParameters = argParameters;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
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
