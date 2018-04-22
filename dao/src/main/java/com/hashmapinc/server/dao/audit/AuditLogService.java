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
package com.hashmapinc.server.dao.audit;

import com.google.common.util.concurrent.ListenableFuture;
import com.hashmapinc.server.common.data.BaseData;
import com.hashmapinc.server.common.data.HasName;
import com.hashmapinc.server.common.data.audit.ActionType;
import com.hashmapinc.server.common.data.id.*;
import com.hashmapinc.server.common.data.page.TimePageLink;
import com.hashmapinc.server.common.data.audit.AuditLog;
import com.hashmapinc.server.common.data.id.*;
import com.hashmapinc.server.common.data.page.TimePageData;

import java.util.List;

public interface AuditLogService {

    TimePageData<AuditLog> findAuditLogsByTenantIdAndCustomerId(TenantId tenantId, CustomerId customerId, TimePageLink pageLink);

    TimePageData<AuditLog> findAuditLogsByTenantIdAndUserId(TenantId tenantId, UserId userId, TimePageLink pageLink);

    TimePageData<AuditLog> findAuditLogsByTenantIdAndEntityId(TenantId tenantId, EntityId entityId, TimePageLink pageLink);

    TimePageData<AuditLog> findAuditLogsByTenantId(TenantId tenantId, TimePageLink pageLink);

    <E extends BaseData<I> & HasName,
            I extends UUIDBased & EntityId> ListenableFuture<List<Void>> logEntityAction(
                                                        TenantId tenantId,
                                                        CustomerId customerId,
                                                        UserId userId,
                                                        String userName,
                                                        I entityId,
                                                        E entity,
                                                        ActionType actionType,
                                                        Exception e, Object... additionalInfo);

}
