/**
 * Copyright © 2016-2017 The Thingsboard Authors
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
package org.thingsboard.server.actors.computation;

import akka.actor.ActorRef;
import org.thingsboard.server.actors.ActorSystemContext;
import org.thingsboard.server.actors.service.ContextAwareActor;
import org.thingsboard.server.actors.service.ContextBasedCreator;
import org.thingsboard.server.common.data.id.ComputationId;
import org.thingsboard.server.common.data.id.ComputationJobId;
import org.thingsboard.server.common.data.id.TenantId;
import java.util.HashMap;
import java.util.Map;

public class ComputationActor extends ContextAwareActor{

    private final TenantId tenantId;
    private final ComputationId computationId;
    private final Map<ComputationJobId, ActorRef> computationJobActors;

    public ComputationActor(ActorSystemContext systemContext, TenantId tenantId, ComputationId computationId) {
        super(systemContext);
        this.tenantId = tenantId;
        this.computationId = computationId;
        this.computationJobActors = new HashMap<>();
    }

    @Override
    public void preStart() throws Exception {
        //TODO: Fetch all Jobs for a computation, Check status of each JOB if status needs to be updated, update it for JOB
        // All active jobs will be started as actor with scheduler to poll staus
    }

    @Override
    public void onReceive(Object message) throws Exception {

    }

    public static class ActorCreator extends ContextBasedCreator<ComputationActor> {
        private static final long serialVersionUID = 1L;

        private final TenantId tenantId;
        private final ComputationId computationId;

        public ActorCreator(ActorSystemContext context, TenantId tenantId, ComputationId computationId) {
            super(context);
            this.tenantId = tenantId;
            this.computationId = computationId;
        }

        @Override
        public ComputationActor create() throws Exception {
            return new ComputationActor(context, tenantId, computationId);
        }
    }
}
