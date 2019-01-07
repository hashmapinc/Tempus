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
package com.hashmapinc.server.extensions.kinesis;


import com.hashmapinc.server.common.data.id.CustomerId;
import com.hashmapinc.server.common.data.id.DeviceId;
import com.hashmapinc.server.common.data.id.RuleId;
import com.hashmapinc.server.common.data.id.TenantId;

import java.util.UUID;

/**
 * @author Mitesh Rathore
 */
public class TestUtils {

    static CustomerId aCustomerId() {
        return new CustomerId(UUID.randomUUID());
    }

    static DeviceId aDeviceId() {
        return new DeviceId(UUID.randomUUID());
    }

    static TenantId aTenantId() {
        return new TenantId(UUID.randomUUID());
    }

    static RuleId aRuleId() {
        return new RuleId(UUID.randomUUID());
    }
}
