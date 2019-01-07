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
package com.hashmapinc.server.extensions.core.plugin.rpc.handlers;

import com.hashmapinc.server.common.data.id.DeviceId;
import com.hashmapinc.server.common.data.id.EntityId;
import com.hashmapinc.server.common.data.id.RuleId;
import com.hashmapinc.server.common.data.id.TenantId;
import com.hashmapinc.server.common.msg.exception.TempusRuntimeException;
import com.hashmapinc.server.extensions.api.plugins.PluginContext;
import com.hashmapinc.server.extensions.api.plugins.msg.RuleToPluginMsg;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import com.hashmapinc.server.common.data.relation.EntityRelation;
import com.hashmapinc.server.extensions.api.plugins.PluginCallback;
import com.hashmapinc.server.extensions.api.plugins.handlers.RuleMsgHandler;
import com.hashmapinc.server.extensions.api.plugins.msg.ToDeviceRpcRequest;
import com.hashmapinc.server.extensions.api.plugins.msg.ToDeviceRpcRequestBody;
import com.hashmapinc.server.extensions.api.rules.RuleException;
import com.hashmapinc.server.extensions.core.action.rpc.ServerSideRpcCallActionMsg;
import com.hashmapinc.server.extensions.core.action.rpc.ServerSideRpcCallRuleToPluginActionMsg;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Created by ashvayka on 14.09.17.
 */
@Slf4j
public class RpcRuleMsgHandler implements RuleMsgHandler {

    @Override
    public void process(PluginContext ctx, TenantId tenantId, RuleId ruleId, RuleToPluginMsg<?> msg) throws RuleException {
        if (msg instanceof ServerSideRpcCallRuleToPluginActionMsg) {
            handle(ctx, tenantId, ((ServerSideRpcCallRuleToPluginActionMsg) msg).getPayload());
        } else {
            throw new TempusRuntimeException("Not supported msg: " + msg + "!");
        }
    }

    private void handle(final PluginContext ctx, TenantId tenantId, ServerSideRpcCallActionMsg msg) {
        DeviceId deviceId = new DeviceId(UUID.fromString(msg.getDeviceId()));
        ctx.checkAccess(deviceId, new PluginCallback<Void>() {
            @Override
            public void onSuccess(PluginContext dummy, Void value) {
                try {
                    List<EntityId> deviceIds;
                    if (StringUtils.isEmpty(msg.getFromDeviceRelation()) && StringUtils.isEmpty(msg.getToDeviceRelation())) {
                        deviceIds = Collections.singletonList(deviceId);
                    } else if (!StringUtils.isEmpty(msg.getFromDeviceRelation())) {
                        List<EntityRelation> relations = ctx.findByFromAndType(deviceId, msg.getFromDeviceRelation()).get();
                        deviceIds = relations.stream().map(EntityRelation::getTo).collect(Collectors.toList());
                    } else {
                        List<EntityRelation> relations = ctx.findByToAndType(deviceId, msg.getFromDeviceRelation()).get();
                        deviceIds = relations.stream().map(EntityRelation::getFrom).collect(Collectors.toList());
                    }
                    ToDeviceRpcRequestBody body = new ToDeviceRpcRequestBody(msg.getRpcCallMethod(), msg.getRpcCallBody());
                    long expirationTime = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(msg.getRpcCallTimeoutInSec());
                    for (EntityId address : deviceIds) {
                        DeviceId tmpId = new DeviceId(address.getId());
                        ctx.checkAccess(tmpId, new PluginCallback<Void>() {
                            @Override
                            public void onSuccess(PluginContext ctx, Void value) {
                                ctx.sendRpcRequest(new ToDeviceRpcRequest(UUID.randomUUID(),
                                        null, tenantId, tmpId, true, expirationTime, body)
                                );
                                log.trace("[{}] Sent RPC Call Action msg", tmpId);
                            }

                            @Override
                            public void onFailure(PluginContext ctx, Exception e) {
                                log.info("[{}] Failed to process RPC Call Action msg", tmpId, e);
                            }
                        });
                    }
                } catch (Exception e) {
                    log.info("Failed to process RPC Call Action msg", e);
                }
            }

            @Override
            public void onFailure(PluginContext dummy, Exception e) {
                log.info("[{}] Failed to process RPC Call Action msg", deviceId, e);
            }
        });
    }
}
