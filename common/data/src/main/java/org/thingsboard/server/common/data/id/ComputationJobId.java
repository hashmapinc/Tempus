package org.thingsboard.server.common.data.id;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.thingsboard.server.common.data.EntityType;

import java.util.UUID;

public class ComputationJobId extends UUIDBased implements EntityId {
    private static final long serialVersionUID = 1L;

    @JsonCreator
    public ComputationJobId(@JsonProperty("id") UUID id) {
        super(id);
    }

    public static ComputationJobId fromString(String computationId) {
        return new ComputationJobId(UUID.fromString(computationId));
    }

    @JsonIgnore
    @Override
    public EntityType getEntityType() {
        return EntityType.COMPUTATION_JOB;
    }
}
