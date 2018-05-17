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

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import com.hashmapinc.server.common.data.id.DeviceId;
import com.hashmapinc.server.common.data.id.TenantId;
import com.hashmapinc.server.common.data.kv.TsKvEntry;

import java.util.List;

/**
 * @author Andrew Shvayka
 */
@ToString
@AllArgsConstructor
public class DeviceTelemetryEventNotificationMsg implements ToDeviceActorNotificationMsg {

    @Getter
    private final TenantId tenantId;
    @Getter
    private final DeviceId deviceId;
    @Getter
    private final List<TsKvEntry> values;

    public static DeviceTelemetryEventNotificationMsg onUpdate(TenantId tenantId, DeviceId deviceId, List<TsKvEntry> values) {
        return new DeviceTelemetryEventNotificationMsg(tenantId, deviceId, values);
    }

}
