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
package com.hashmapinc.server.extensions.api.device;

import com.hashmapinc.server.common.data.id.DeviceId;
import com.hashmapinc.server.common.data.kv.AttributeKvEntry;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import com.hashmapinc.server.common.data.id.TenantId;
import com.hashmapinc.server.common.data.kv.AttributeKey;

import java.util.List;
import java.util.Set;

/**
 * @author Andrew Shvayka
 */
@ToString
@AllArgsConstructor
public class DeviceAttributesEventNotificationMsg implements ToDeviceActorNotificationMsg {

    @Getter
    private final TenantId tenantId;
    @Getter
    private final DeviceId deviceId;
    @Getter
    private final Set<AttributeKey> deletedKeys;
    @Getter
    private final String scope;
    @Getter
    private final List<AttributeKvEntry> values;
    @Getter
    private final boolean deleted;

    public static DeviceAttributesEventNotificationMsg onUpdate(TenantId tenantId, DeviceId deviceId, String scope, List<AttributeKvEntry> values) {
        return new DeviceAttributesEventNotificationMsg(tenantId, deviceId, null, scope, values, false);
    }

    public static DeviceAttributesEventNotificationMsg onDelete(TenantId tenantId, DeviceId deviceId, Set<AttributeKey> keys) {
        return new DeviceAttributesEventNotificationMsg(tenantId, deviceId, keys, null, null, true);
    }

}
