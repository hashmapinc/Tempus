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
package com.hashmapinc.server.actors.rule;

import com.hashmapinc.server.extensions.api.plugins.msg.PluginToRuleMsg;
import com.hashmapinc.server.actors.ActorSystemContext;
import com.hashmapinc.server.actors.service.ComponentActor;
import com.hashmapinc.server.actors.service.ContextBasedCreator;
import com.hashmapinc.server.actors.stats.StatsPersistTick;
import com.hashmapinc.server.common.data.id.RuleId;
import com.hashmapinc.server.common.data.id.TenantId;
import com.hashmapinc.server.common.msg.cluster.ClusterEventMsg;
import com.hashmapinc.server.common.msg.plugin.ComponentLifecycleMsg;

public class RuleActor extends ComponentActor<RuleId, RuleActorMessageProcessor> {

    private RuleActor(ActorSystemContext systemContext, TenantId tenantId, RuleId ruleId) {
        super(systemContext, tenantId, ruleId);
        setProcessor(new RuleActorMessageProcessor(tenantId, ruleId, systemContext, logger));
    }

    @Override
    public void onReceive(Object msg) throws Exception {
        logger.debug("[{}] Received message: {}", id, msg);
        if (msg instanceof RuleProcessingMsg) {
            try {
                logger.debug("msg type RuleRrocessing -->");
                processor.onRuleProcessingMsg(context(), (RuleProcessingMsg) msg);
                increaseMessagesProcessedCount();
            } catch (Exception e) {
                logAndPersist("onDeviceMsg", e);
            }
        } else if (msg instanceof PluginToRuleMsg<?>) {
            try {
                logger.debug("msg type PluginToRuleMsg<?> -->");
                processor.onPluginMsg(context(), (PluginToRuleMsg<?>) msg);
            } catch (Exception e) {
                logAndPersist("onPluginMsg", e);
            }
        } else if (msg instanceof ComponentLifecycleMsg) {
            logger.debug("msg type ComponentLifecycleMsg -->");
            onComponentLifecycleMsg((ComponentLifecycleMsg) msg);
        } else if (msg instanceof ClusterEventMsg) {
            logger.debug("msg type ClusterEventMsg -->");
            onClusterEventMsg((ClusterEventMsg) msg);
        } else if (msg instanceof RuleToPluginTimeoutMsg) {
            try {
                logger.debug("msg type RuleToPluginTimeoutMsg -->");
                processor.onTimeoutMsg(context(), (RuleToPluginTimeoutMsg) msg);
            } catch (Exception e) {
                logAndPersist("onTimeoutMsg", e);
            }
        } else if (msg instanceof StatsPersistTick) {
            logger.debug("msg type StatsPersistTick -->");
            onStatsPersistTick(id);
        } else {
            logger.debug("[{}][{}] Unknown msg type.", tenantId, id, msg.getClass().getName());
        }
    }

    public static class ActorCreator extends ContextBasedCreator<RuleActor> {
        private static final long serialVersionUID = 1L;

        private final TenantId tenantId;
        private final RuleId ruleId;

        public ActorCreator(ActorSystemContext context, TenantId tenantId, RuleId ruleId) {
            super(context);
            this.tenantId = tenantId;
            this.ruleId = ruleId;
        }

        @Override
        public RuleActor create() throws Exception {
            return new RuleActor(context, tenantId, ruleId);
        }
    }

    @Override
    protected long getErrorPersistFrequency() {
        return systemContext.getRuleErrorPersistFrequency();
    }
}
