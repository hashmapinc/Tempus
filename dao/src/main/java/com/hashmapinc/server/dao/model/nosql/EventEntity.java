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
package com.hashmapinc.server.dao.model.nosql;

import com.datastax.driver.core.utils.UUIDs;
import com.datastax.driver.mapping.annotations.*;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.hashmapinc.server.common.data.EntityType;
import com.hashmapinc.server.common.data.Event;
import com.hashmapinc.server.common.data.id.EntityIdFactory;
import com.hashmapinc.server.common.data.id.EventId;
import com.hashmapinc.server.common.data.id.TenantId;
import com.hashmapinc.server.dao.model.BaseEntity;
import com.hashmapinc.server.dao.model.type.EntityTypeCodec;
import com.hashmapinc.server.dao.model.type.JsonCodec;

import java.util.UUID;

import static com.hashmapinc.server.dao.model.ModelConstants.*;


@Data
@NoArgsConstructor
@Table(name = EVENT_COLUMN_FAMILY_NAME)
public class EventEntity implements BaseEntity<Event> {

    @Column(name = ID_PROPERTY)
    private UUID id;

    @PartitionKey()
    @Column(name = EVENT_TENANT_ID_PROPERTY)
    private UUID tenantId;

    @PartitionKey(value = 1)
    @Column(name = EVENT_ENTITY_TYPE_PROPERTY, codec = EntityTypeCodec.class)
    private EntityType entityType;

    @PartitionKey(value = 2)
    @Column(name = EVENT_ENTITY_ID_PROPERTY)
    private UUID entityId;

    @ClusteringColumn()
    @Column(name = EVENT_TYPE_PROPERTY)
    private String eventType;

    @ClusteringColumn(value = 1)
    @Column(name = EVENT_UID_PROPERTY)
    private String eventUid;

    @Column(name = EVENT_BODY_PROPERTY, codec = JsonCodec.class)
    private JsonNode body;

    public EventEntity(Event event) {
        if (event.getId() != null) {
            this.id = event.getId().getId();
        }
        if (event.getTenantId() != null) {
            this.tenantId = event.getTenantId().getId();
        }
        if (event.getEntityId() != null) {
            this.entityType = event.getEntityId().getEntityType();
            this.entityId = event.getEntityId().getId();
        }
        this.eventType = event.getType();
        this.eventUid = event.getUid();
        this.body = event.getBody();
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
    public Event toData() {
        Event event = new Event(new EventId(id));
        event.setCreatedTime(UUIDs.unixTimestamp(id));
        event.setTenantId(new TenantId(tenantId));
        event.setEntityId(EntityIdFactory.getByTypeAndUuid(entityType, entityId));
        event.setBody(body);
        event.setType(eventType);
        event.setUid(eventUid);
        return event;
    }
}
