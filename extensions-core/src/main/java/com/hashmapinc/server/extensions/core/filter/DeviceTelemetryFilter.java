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
package com.hashmapinc.server.extensions.core.filter;

import com.hashmapinc.server.extensions.api.component.Filter;
import com.hashmapinc.server.extensions.api.rules.RuleContext;
import lombok.extern.slf4j.Slf4j;
import com.hashmapinc.server.common.data.kv.KvEntry;
import com.hashmapinc.server.common.msg.core.TelemetryUploadRequest;
import com.hashmapinc.server.common.msg.device.ToDeviceActorMsg;
import com.hashmapinc.server.common.msg.session.FromDeviceMsg;

import javax.script.ScriptException;
import java.util.List;

/**
 * @author Andrew Shvayka
 */
@Filter(name = "Device Telemetry Filter", descriptor = "JsFilterDescriptor.json", configuration = JsFilterConfiguration.class)
@Slf4j
public class DeviceTelemetryFilter extends BasicJsFilter {

    @Override
    protected boolean doFilter(RuleContext ctx, ToDeviceActorMsg msg) throws ScriptException {
        FromDeviceMsg deviceMsg = msg.getPayload();
        if (deviceMsg instanceof TelemetryUploadRequest) {
            TelemetryUploadRequest telemetryMsg = (TelemetryUploadRequest) deviceMsg;
            for (List<KvEntry> entries : telemetryMsg.getData().values()) {
                if (evaluator.execute(NashornJsEvaluator.toBindings(entries))) {
                    return true;
                }
            }
        }
        return false;
    }

}
