package org.thingsboard.server.dao.model.nosql;

import com.datastax.driver.core.utils.UUIDs;
import com.datastax.driver.mapping.annotations.*;
import com.fasterxml.jackson.databind.JsonNode;
import org.thingsboard.server.common.data.computation.ComputationJob;
import org.thingsboard.server.common.data.id.ComputationId;
import org.thingsboard.server.common.data.id.ComputationJobId;
import org.thingsboard.server.common.data.id.TenantId;
import org.thingsboard.server.common.data.plugin.ComponentLifecycleState;
import org.thingsboard.server.dao.model.ModelConstants;
import org.thingsboard.server.dao.model.SearchTextEntity;
import org.thingsboard.server.dao.model.type.ComponentLifecycleStateCodec;
import org.thingsboard.server.dao.model.type.JsonCodec;

import java.util.UUID;

import static org.thingsboard.server.dao.model.ModelConstants.*;
@Table(name = ModelConstants.COMPUTATION_JOB_COLUMN_FAMILY_NAME)
public class ComputationJobEntity implements SearchTextEntity<ComputationJob> {
    @Transient
    private static final long serialVersionUID = -4673737406462009037L;

    @PartitionKey
    @Column(name = ID_PROPERTY)
    private UUID id;

    @Column(name = ModelConstants.COMPUTATION_JOB_NAME)
    private String jobName;

    @Column(name = ModelConstants.COMPUTATION_JOB_COMPUTAION_ID)
    private UUID computationId;

    @Column(name = ModelConstants.COMPUTATION_JOB_ARG_PRS, codec = JsonCodec.class)
    private JsonNode argParameters;

    @ClusteringColumn
    @Column(name = ModelConstants.COMPUTATION_JOB_TENANT_ID)
    private UUID tenantId;

    @Column(name = ModelConstants.SEARCH_TEXT_PROPERTY)
    private String searchText;

    @Column(name = ModelConstants.COMPUTATION_JOB_ID)
    private String jobId;

    @Column(name = ModelConstants.COMPUTATION_JOB_STATE, codec = ComponentLifecycleStateCodec.class)
    private ComponentLifecycleState state;

    @Override
    public String getSearchTextSource() {
        return jobName;
    }

    public ComputationJobEntity() {

    }

    public ComputationJobEntity(ComputationJob computationJob){

        if(computationJob.getId() != null) {
            this.setId(computationJob.getId().getId());
        }
        if(computationJob.getName() != null) {
            this.jobName = computationJob.getName();
        }
        if(computationJob.getArgParameters() != null) {
            this.argParameters = computationJob.getArgParameters();
        }
        if(computationJob.getComputationId() != null) {
            this.computationId = computationJob.getComputationId().getId();
        }
        if(computationJob.getTenantId() != null) {
            this.tenantId = computationJob.getTenantId().getId();
        }
        if(computationJob.getJobId() != null){
            this.jobId = computationJob.getJobId();
        }
        this.state = computationJob.getState();

    }

    @Override
    public void setSearchText(String searchText) {
        this.searchText = searchText;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        ComputationJobEntity that = (ComputationJobEntity) o;
        if (jobName != null ? !jobName.equals(that.jobName) : that.jobName != null) return false;
        if (searchText != null ? !searchText.equals(that.searchText) : that.searchText != null) return false;
        if (argParameters != null ? !argParameters.equals(that.argParameters) : that.argParameters != null) return false;
        if (computationId != null ? !computationId.equals(that.computationId) : that.computationId != null) return false;
        if (tenantId != null ? !tenantId.equals(that.tenantId) : that.tenantId != null) return false;
        if (jobId != null ? !jobId.equals(that.jobId) : that.jobId != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (jobName != null ? jobName.hashCode() : 0);
        result = 31 * result + (searchText != null ? searchText.hashCode() : 0);
        result = 31 * result + (argParameters != null ? argParameters.hashCode() : 0);
        result = 31 * result + (computationId != null ? computationId.hashCode() : 0);
        result = 31 * result + (tenantId != null ? tenantId.hashCode() : 0);
        result = 31 * result + (jobId != null ? jobId.hashCode() : 0);
        return result;
    }

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public void setId(UUID id) {
        this.id = id;
    }

    @Override
    public ComputationJob toData() {
        ComputationJob computationJob = new ComputationJob(new ComputationJobId(getId()));
        computationJob.setCreatedTime(UUIDs.unixTimestamp(getId()));
        computationJob.setName(jobName);
        computationJob.setArgParameters(argParameters);
        computationJob.setState(state);
        if(computationId != null) {
            computationJob.setComputationId(new ComputationId(computationId));
        }
        if (tenantId != null) {
            computationJob.setTenantId(new TenantId(tenantId));
        }
        computationJob.setJobId(jobId);
        return computationJob;
    }
}
