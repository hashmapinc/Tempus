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
package com.hashmapinc.server.actors.device;

import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.hashmapinc.server.extensions.api.device.*;
import com.hashmapinc.server.extensions.api.plugins.msg.TimeoutMsg;
import com.hashmapinc.server.actors.ActorSystemContext;
import com.hashmapinc.server.actors.rule.RulesProcessedMsg;
import com.hashmapinc.server.actors.service.ContextAwareActor;
import com.hashmapinc.server.actors.service.ContextBasedCreator;
import com.hashmapinc.server.actors.tenant.RuleChainDeviceMsg;
import com.hashmapinc.server.common.data.id.DeviceId;
import com.hashmapinc.server.common.data.id.TenantId;
import com.hashmapinc.server.common.msg.cluster.ClusterEventMsg;
import com.hashmapinc.server.common.msg.device.ToDeviceActorMsg;
import com.hashmapinc.server.extensions.api.plugins.msg.ToDeviceRpcRequestPluginMsg;

public class DeviceActor extends ContextAwareActor {

    private final LoggingAdapter logger = Logging.getLogger(getContext().system(), this);

    private final TenantId tenantId;
    private final DeviceId deviceId;
    private final DeviceActorMessageProcessor processor;

    private DeviceActor(ActorSystemContext systemContext, TenantId tenantId, DeviceId deviceId) {
        super(systemContext);
        this.tenantId = tenantId;
        this.deviceId = deviceId;
        this.processor = new DeviceActorMessageProcessor(systemContext, logger, deviceId);
    }

    @Override
    public void onReceive(Object msg) throws Exception {
        if (msg instanceof RuleChainDeviceMsg) {
            processor.process(context(), (RuleChainDeviceMsg) msg);
        } else if (msg instanceof RulesProcessedMsg) {
            processor.onRulesProcessedMsg((RulesProcessedMsg) msg);
        } else if (msg instanceof ToDeviceActorMsg) {
            processor.process(context(), (ToDeviceActorMsg) msg);
        } else if (msg instanceof ToDeviceActorNotificationMsg) {
            if (msg instanceof DeviceAttributesEventNotificationMsg) {
                processor.processAttributesUpdate((DeviceAttributesEventNotificationMsg) msg);
            }else if (msg instanceof DeviceTelemetryEventNotificationMsg) {
                processor.processTelemetryUpdate((DeviceTelemetryEventNotificationMsg) msg);
            }else if (msg instanceof ToDeviceRpcRequestPluginMsg) {
                processor.processRpcRequest(context(), (ToDeviceRpcRequestPluginMsg) msg);
            } else if (msg instanceof DeviceCredentialsUpdateNotificationMsg){
                processor.processCredentialsUpdate();
            } else if (msg instanceof DeviceNameOrTypeUpdateMsg){
                processor.processNameOrTypeUpdate((DeviceNameOrTypeUpdateMsg) msg);
            }
        } else if (msg instanceof TimeoutMsg) {
            processor.processTimeout(context(), (TimeoutMsg) msg);
        } else if (msg instanceof ClusterEventMsg) {
            processor.processClusterEventMsg((ClusterEventMsg) msg);
        } else {
            logger.debug("[{}][{}] Unknown msg type.", tenantId, deviceId, msg.getClass().getName());
        }
    }

    public static class ActorCreator extends ContextBasedCreator<DeviceActor> {
        private static final long serialVersionUID = 1L;

        private final TenantId tenantId;
        private final DeviceId deviceId;

        public ActorCreator(ActorSystemContext context, TenantId tenantId, DeviceId deviceId) {
            super(context);
            this.tenantId = tenantId;
            this.deviceId = deviceId;
        }

        @Override
        public DeviceActor create() throws Exception {
            return new DeviceActor(context, tenantId, deviceId);
        }
    }

}
