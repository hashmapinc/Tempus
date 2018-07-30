package com.hashmapinc.server.common.data.id;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.hashmapinc.server.common.data.EntityType;

import java.util.UUID;

public class CustomerGroupId extends UUIDBased implements EntityId {

    private static final long serialVersionUID = 5646703518988164203L;

    @JsonCreator
    public CustomerGroupId(@JsonProperty("id") UUID id) {
        super(id);
    }

    public static CustomerGroupId fromString(String customerGroupId) {
        return new CustomerGroupId(UUID.fromString(customerGroupId));
    }

    @JsonIgnore
    @Override
    public EntityType getEntityType() {
        return EntityType.CUSTOMER_GROUP;
    }
}
