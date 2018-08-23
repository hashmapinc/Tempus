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
package com.hashmapinc.server.dao.model.nosql;

import com.datastax.driver.core.utils.UUIDs;
import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.Table;
import com.fasterxml.jackson.databind.JsonNode;
import com.hashmapinc.server.common.data.EntityType;
import com.hashmapinc.server.common.data.audit.ActionType;
import com.hashmapinc.server.common.data.id.*;
import com.hashmapinc.server.dao.model.ModelConstants;
import com.hashmapinc.server.dao.model.type.ActionStatusCodec;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.hashmapinc.server.common.data.audit.ActionStatus;
import com.hashmapinc.server.common.data.audit.AuditLog;
import com.hashmapinc.server.common.data.id.*;
import com.hashmapinc.server.dao.model.BaseEntity;
import com.hashmapinc.server.dao.model.type.ActionTypeCodec;
import com.hashmapinc.server.dao.model.type.EntityTypeCodec;
import com.hashmapinc.server.dao.model.type.JsonCodec;

import java.util.UUID;

@Table(name = ModelConstants.AUDIT_LOG_COLUMN_FAMILY_NAME)
@Data
@NoArgsConstructor
public class AuditLogEntity implements BaseEntity<AuditLog> {

    @Column(name = ModelConstants.ID_PROPERTY)
    private UUID id;

    @Column(name = ModelConstants.AUDIT_LOG_TENANT_ID_PROPERTY)
    private UUID tenantId;

    @Column(name = ModelConstants.AUDIT_LOG_CUSTOMER_ID_PROPERTY)
    private UUID customerId;

    @Column(name = ModelConstants.AUDIT_LOG_ENTITY_TYPE_PROPERTY, codec = EntityTypeCodec.class)
    private EntityType entityType;

    @Column(name = ModelConstants.AUDIT_LOG_ENTITY_ID_PROPERTY)
    private UUID entityId;

    @Column(name = ModelConstants.AUDIT_LOG_ENTITY_NAME_PROPERTY)
    private String entityName;

    @Column(name = ModelConstants.AUDIT_LOG_USER_ID_PROPERTY)
    private UUID userId;

    @Column(name = ModelConstants.AUDIT_LOG_USER_NAME_PROPERTY)
    private String userName;

    @Column(name = ModelConstants.AUDIT_LOG_ACTION_TYPE_PROPERTY, codec = ActionTypeCodec.class)
    private ActionType actionType;

    @Column(name = ModelConstants.AUDIT_LOG_ACTION_DATA_PROPERTY, codec = JsonCodec.class)
    private JsonNode actionData;

    @Column(name = ModelConstants.AUDIT_LOG_ACTION_STATUS_PROPERTY, codec = ActionStatusCodec.class)
    private ActionStatus actionStatus;

    @Column(name = ModelConstants.AUDIT_LOG_ACTION_FAILURE_DETAILS_PROPERTY)
    private String actionFailureDetails;

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public void setId(UUID id) {
        this.id = id;
    }

    public AuditLogEntity(AuditLog auditLog) {
        if (auditLog.getId() != null) {
            this.id = auditLog.getId().getId();
        }
        if (auditLog.getTenantId() != null) {
            this.tenantId = auditLog.getTenantId().getId();
        }
        if (auditLog.getEntityId() != null) {
            this.entityType = auditLog.getEntityId().getEntityType();
            this.entityId = auditLog.getEntityId().getId();
        }
        if (auditLog.getCustomerId() != null) {
            this.customerId = auditLog.getCustomerId().getId();
        }
        if (auditLog.getUserId() != null) {
            this.userId = auditLog.getUserId().getId();
        }
        this.entityName = auditLog.getEntityName();
        this.userName = auditLog.getUserName();
        this.actionType = auditLog.getActionType();
        this.actionData = auditLog.getActionData();
        this.actionStatus = auditLog.getActionStatus();
        this.actionFailureDetails = auditLog.getActionFailureDetails();
    }

    @Override
    public AuditLog toData() {
        AuditLog auditLog = new AuditLog(new AuditLogId(id));
        auditLog.setCreatedTime(UUIDs.unixTimestamp(id));
        if (tenantId != null) {
            auditLog.setTenantId(new TenantId(tenantId));
        }
        if (entityId != null && entityType != null) {
            auditLog.setEntityId(EntityIdFactory.getByTypeAndUuid(entityType, entityId));
        }
        if (customerId != null) {
            auditLog.setCustomerId(new CustomerId(customerId));
        }
        if (userId != null) {
            auditLog.setUserId(new UserId(userId));
        }
        auditLog.setEntityName(this.entityName);
        auditLog.setUserName(this.userName);
        auditLog.setActionType(this.actionType);
        auditLog.setActionData(this.actionData);
        auditLog.setActionStatus(this.actionStatus);
        auditLog.setActionFailureDetails(this.actionFailureDetails);
        return auditLog;
    }
}
