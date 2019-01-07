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
package com.hashmapinc.server.extensions.core.action.rpc;

import com.hashmapinc.server.common.msg.device.ToDeviceActorMsg;
import com.hashmapinc.server.common.msg.exception.TempusRuntimeException;
import com.hashmapinc.server.common.msg.session.ToDeviceMsg;
import com.hashmapinc.server.extensions.api.component.Action;
import com.hashmapinc.server.extensions.api.plugins.PluginAction;
import com.hashmapinc.server.extensions.api.plugins.msg.PluginToRuleMsg;
import com.hashmapinc.server.extensions.api.plugins.msg.ResponsePluginToRuleMsg;
import com.hashmapinc.server.extensions.api.plugins.msg.RuleToPluginMsg;
import com.hashmapinc.server.extensions.api.rules.RuleContext;
import com.hashmapinc.server.extensions.api.rules.RuleProcessingMetaData;
import com.hashmapinc.server.extensions.api.rules.SimpleRuleLifecycleComponent;
import com.hashmapinc.server.extensions.core.utils.VelocityUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.runtime.parser.ParseException;
import org.springframework.util.StringUtils;

import java.util.Optional;

/**
 * Created by ashvayka on 14.09.17.
 */
@Action(name = "Server Side RPC Call Action", descriptor = "ServerSideRpcCallActionDescriptor.json", configuration = ServerSideRpcCallActionConfiguration.class)
@Slf4j
public class ServerSideRpcCallAction extends SimpleRuleLifecycleComponent implements PluginAction<ServerSideRpcCallActionConfiguration> {

    private ServerSideRpcCallActionConfiguration configuration;
    private Optional<Template> deviceIdTemplate;
    private Optional<Template> fromDeviceRelationTemplate;
    private Optional<Template> toDeviceRelationTemplate;
    private Optional<Template> rpcCallMethodTemplate;
    private Optional<Template> rpcCallBodyTemplate;

    @Override
    public void init(ServerSideRpcCallActionConfiguration configuration) {
        this.configuration = configuration;
        try {
            deviceIdTemplate = toTemplate(configuration.getDeviceIdTemplate(), "Device Id Template");
            fromDeviceRelationTemplate = toTemplate(configuration.getFromDeviceRelationTemplate(), "From Device Relation Template");
            toDeviceRelationTemplate = toTemplate(configuration.getToDeviceRelationTemplate(), "To Device Relation Template");
            rpcCallMethodTemplate = toTemplate(configuration.getRpcCallMethodTemplate(), "RPC Call Method Template");
            rpcCallBodyTemplate = toTemplate(configuration.getRpcCallBodyTemplate(), "RPC Call Body Template");
        } catch (ParseException e) {
            log.error("Failed to create templates based on provided configuration!", e);
            throw new TempusRuntimeException("Failed to create templates based on provided configuration!", e);
        }
    }

    @Override
    public Optional<RuleToPluginMsg> convert(RuleContext ctx, ToDeviceActorMsg toDeviceActorMsg, RuleProcessingMetaData metadata) {
        String sendFlag = configuration.getSendFlag();
        if (StringUtils.isEmpty(sendFlag) || (Boolean) metadata.get(sendFlag).orElse(Boolean.FALSE)) {
            VelocityContext context = VelocityUtils.createContext(metadata);

            ServerSideRpcCallActionMsg.ServerSideRpcCallActionMsgBuilder builder = ServerSideRpcCallActionMsg.builder();

            deviceIdTemplate.ifPresent(t -> builder.deviceId(VelocityUtils.merge(t, context)));
            fromDeviceRelationTemplate.ifPresent(t -> builder.fromDeviceRelation(VelocityUtils.merge(t, context)));
            toDeviceRelationTemplate.ifPresent(t -> builder.toDeviceRelation(VelocityUtils.merge(t, context)));
            rpcCallMethodTemplate.ifPresent(t -> builder.rpcCallMethod(VelocityUtils.merge(t, context)));
            rpcCallBodyTemplate.ifPresent(t -> builder.rpcCallBody(VelocityUtils.merge(t, context)));
            builder.rpcCallTimeoutInSec(configuration.getRpcCallTimeoutInSec());
            return Optional.of(new ServerSideRpcCallRuleToPluginActionMsg(toDeviceActorMsg.getTenantId(), toDeviceActorMsg.getCustomerId(), toDeviceActorMsg.getDeviceId(),
                    builder.build()));
        } else {
            return Optional.empty();
        }
    }

    private Optional<Template> toTemplate(String source, String name) throws ParseException {
        if (!StringUtils.isEmpty(source)) {
            return Optional.of(VelocityUtils.create(source, name));
        } else {
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
        return true;
    }
}
