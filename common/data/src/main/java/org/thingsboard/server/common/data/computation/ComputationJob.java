package org.thingsboard.server.common.data.computation;

import com.fasterxml.jackson.databind.JsonNode;
import org.thingsboard.server.common.data.HasName;
import org.thingsboard.server.common.data.SearchTextBased;
import org.thingsboard.server.common.data.id.ComputationId;
import org.thingsboard.server.common.data.id.ComputationJobId;
import org.thingsboard.server.common.data.id.TenantId;

import java.util.UUID;

public class ComputationJob extends SearchTextBased<ComputationJobId> implements HasName {

    private TenantId tenantId;
    private ComputationId computationId;
    private JsonNode argParameters;
    private String name;

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
        return true;
    }

    @Override

    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (tenantId != null ? tenantId.hashCode() : 0);
        result = 31 * result + (argParameters != null ? argParameters.hashCode() : 0);
        result = 31 * result + (computationId != null ? computationId.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
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

    @Override
    public String getName() {
        return name;
    }
    @Override
    public String getSearchText() {
        return name;
    }
}
