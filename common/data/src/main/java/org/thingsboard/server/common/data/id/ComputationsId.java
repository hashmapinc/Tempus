package org.thingsboard.server.common.data.id;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.thingsboard.server.common.data.EntityType;

import java.util.UUID;

public class ComputationsId extends UUIDBased implements EntityId{

    private static final long serialVersionUID = 1L;

    @JsonCreator
    public ComputationsId(@JsonProperty("id") UUID id) {
        super(id);
    }

    public static ComputationsId fromString(String computationsId) {
        return new ComputationsId(UUID.fromString(computationsId));
    }


    @JsonIgnore
    @Override
    public EntityType getEntityType() {
        return EntityType.APPLICATION;
    }
}
