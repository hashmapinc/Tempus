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
package com.hashmapinc.server.actors.application;

import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.hashmapinc.server.actors.ActorSystemContext;
import com.hashmapinc.server.actors.service.ContextAwareActor;
import com.hashmapinc.server.actors.service.ContextBasedCreator;
import com.hashmapinc.server.common.data.id.TenantId;

public class ApplicationActor extends ContextAwareActor {
    protected final TenantId tenantId;
    protected final LoggingAdapter logger = Logging.getLogger(getContext().system(), this);

    private ApplicationActor(ActorSystemContext systemContext, TenantId tenantId) {
        super(systemContext);
        this.tenantId = tenantId;
    }

    @Override
    public void onReceive(Object message) throws Exception {
        logger.debug("[{}] Received message: {}", tenantId, message);
        if(message instanceof RuleDeleteMessage) {
            logger.debug("msg type RuleDeleteMessage -->");
            systemContext.getApplicationService().updateApplicationOnRuleDelete(((RuleDeleteMessage) message).getRuleId(), tenantId);
        } else if(message instanceof DashboardDeleteMessage){
            logger.debug("msg type DashboardDeleteMessage -->");
            systemContext.getApplicationService().updateApplicationOnDashboardDelete(((DashboardDeleteMessage) message).getDashboardId(), tenantId);
        } else if(message instanceof ComputationJobDeleteMessage) {
            logger.debug("msg type ComputationJobDeleteMessage -->");
            systemContext.getApplicationService().updateApplicationOnComputationJobDelete(((ComputationJobDeleteMessage) message).getComputationJobId(), tenantId);
        } /*else if (message instanceof ComputationDeleteMessage) {
            logger.debug("msg type ComputationJobDeleteMessage -->");
            systemContext.getApplicationService().updateApplicationOnComputationDelete(((ComputationDeleteMessage) message).getComputationId(), tenantId);
        }*/
        else {
            logger.debug("[{}][{}] Unknown msg type.", tenantId, message.getClass().getName());
        }
    }

    public static class ActorCreator extends ContextBasedCreator<ApplicationActor> {

        private static final long serialVersionUID = 1L;

        private final TenantId tenantId;

        public ActorCreator(ActorSystemContext context, TenantId tenantId) {
            super(context);
            this.tenantId = tenantId;
        }

        @Override
        public ApplicationActor create() throws Exception {
            return new ApplicationActor(context, tenantId);
        }
    }
}
