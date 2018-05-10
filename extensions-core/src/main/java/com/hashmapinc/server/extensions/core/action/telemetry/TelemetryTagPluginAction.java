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
package com.hashmapinc.server.extensions.core.action.telemetry;

import com.hashmapinc.server.common.msg.core.DepthTelemetryUploadRequest;
import com.hashmapinc.server.common.msg.core.GetAttributesRequest;
import com.hashmapinc.server.common.msg.core.TelemetryUploadRequest;
import com.hashmapinc.server.common.msg.core.UpdateAttributesRequest;
import com.hashmapinc.server.common.msg.session.FromDeviceMsg;
import com.hashmapinc.server.common.msg.session.MsgType;
import com.hashmapinc.server.common.msg.session.ToDeviceMsg;
import com.hashmapinc.server.extensions.api.component.Action;
import com.hashmapinc.server.extensions.api.plugins.PluginAction;
import com.hashmapinc.server.extensions.api.plugins.msg.*;
import com.hashmapinc.server.extensions.api.rules.RuleContext;
import com.hashmapinc.server.extensions.api.rules.RuleProcessingMetaData;
import com.hashmapinc.server.extensions.api.rules.SimpleRuleLifecycleComponent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import com.hashmapinc.server.common.msg.device.ToDeviceActorMsg;
import com.hashmapinc.server.extensions.api.plugins.msg.*;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
@Slf4j
@Action(name = "Telemetry Tag Plugin Action", descriptor = "TelemetryTagPluginActionDescriptor.json", configuration = TelemetryTagPluginActionConfiguration.class)
public class TelemetryTagPluginAction extends SimpleRuleLifecycleComponent implements PluginAction<TelemetryTagPluginActionConfiguration> {

    protected TelemetryTagPluginActionConfiguration configuration;
    protected long ttl = 0;
    protected long qualityTimeWindow;
    protected long qualityDepthWindow;

    @Override
    public void init(TelemetryTagPluginActionConfiguration configuration) {
        this.configuration = configuration;
        this.qualityTimeWindow = configuration.getQualityTimeWindow();
        this.qualityDepthWindow = configuration.getQualityDepthWindow();
    }

    @Override
    public Optional<RuleToPluginMsg> convert(RuleContext ctx, ToDeviceActorMsg toDeviceActorMsg, RuleProcessingMetaData deviceMsgMd) {
        FromDeviceMsg msg = toDeviceActorMsg.getPayload();
        log.debug("ToDeviceActorMsg : " + toDeviceActorMsg);
        if (msg.getMsgType() == MsgType.POST_TELEMETRY_REQUEST) {
            log.debug("Post telemetry request : " + msg);
            TelemetryUploadRequest payload = (TelemetryUploadRequest) msg;
            return Optional.of(new TelemetryUploadRequestRuleToPluginMsg(toDeviceActorMsg.getTenantId(), toDeviceActorMsg.getCustomerId(),
                    toDeviceActorMsg.getDeviceId(), payload, ttl, qualityTimeWindow));
        }
        else if (msg.getMsgType() == MsgType.POST_TELEMETRY_REQUEST_DEPTH) {
            log.debug("Post telemetry requestDs : " + msg);
            DepthTelemetryUploadRequest payload = (DepthTelemetryUploadRequest) msg;
            return Optional.of(new DepthTelemetryUploadRequestRuleToPluginMsg(toDeviceActorMsg.getTenantId(), toDeviceActorMsg.getCustomerId(),
                    toDeviceActorMsg.getDeviceId(), payload, ttl));
        }
        else {
            return Optional.empty();
        }
    }

    @Override
    public Optional<ToDeviceMsg> convert(PluginToRuleMsg<?> response) {
        if (response instanceof ResponsePluginToRuleMsg) {
            return Optional.of(((ResponsePluginToRuleMsg) response).getPayload());
        }
        return Optional.empty();
    }

    @Override
    public boolean isOneWayAction() {
        return false;
    }
}
