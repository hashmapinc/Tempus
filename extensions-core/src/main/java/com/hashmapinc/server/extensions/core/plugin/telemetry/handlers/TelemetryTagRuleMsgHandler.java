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
package com.hashmapinc.server.extensions.core.plugin.telemetry.handlers;

import com.hashmapinc.server.common.data.DataConstants;
import com.hashmapinc.server.common.data.id.DeviceId;
import com.hashmapinc.server.common.data.id.RuleId;
import com.hashmapinc.server.common.data.id.TenantId;
import com.hashmapinc.server.common.data.kv.*;
import com.hashmapinc.server.common.msg.core.*;
import com.hashmapinc.server.common.msg.kv.BasicAttributeKVMsg;
import com.hashmapinc.server.extensions.api.plugins.PluginCallback;
import com.hashmapinc.server.extensions.api.plugins.PluginContext;
import com.hashmapinc.server.extensions.api.plugins.handlers.DefaultRuleMsgHandler;
import com.hashmapinc.server.extensions.api.plugins.msg.*;
import com.hashmapinc.server.extensions.core.plugin.telemetry.SubscriptionManager;
import com.hashmapinc.server.extensions.core.plugin.telemetry.dataquality.MetaDataAggregator;
import com.hashmapinc.server.extensions.core.plugin.telemetry.sub.Subscription;
import com.hashmapinc.server.extensions.core.plugin.telemetry.sub.SubscriptionType;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class TelemetryTagRuleMsgHandler extends DefaultRuleMsgHandler {

    private MetaDataAggregator metaDataAggregator;

    public TelemetryTagRuleMsgHandler() {
    }

    @Override
    public void handleTelemetryUploadRequest(PluginContext ctx, TenantId tenantId, RuleId ruleId, TelemetryUploadRequestRuleToPluginMsg msg) {
        TelemetryUploadRequest request = msg.getPayload();
        for (Map.Entry<Long, List<KvEntry>> entry : request.getData().entrySet()) {
            List<KvEntry> kvEntries = entry.getValue();
            metaDataAggregator = new MetaDataAggregator(ctx, msg.getQualityTimeWindow(), msg.getDeviceId());
            metaDataAggregator.aggregateMetaData(entry.getKey(), kvEntries);
        }

    }
}