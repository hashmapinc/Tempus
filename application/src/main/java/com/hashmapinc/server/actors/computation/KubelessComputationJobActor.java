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
package com.hashmapinc.server.actors.computation;

import com.hashmapinc.server.ComputationJobActor;
import com.hashmapinc.server.actors.ActorSystemContext;
import com.hashmapinc.server.actors.service.ContextBasedCreator;
import com.hashmapinc.server.common.data.computation.Computations;
import com.hashmapinc.server.common.data.id.ComputationJobId;
import com.hashmapinc.server.common.data.id.TenantId;

public class KubelessComputationJobActor extends ComputationJobActor<KubelessComputationJobActorMessageProcessor> {

    public KubelessComputationJobActor(ActorSystemContext systemContext, TenantId tenantId,
                                    Computations computation, ComputationJobId computationJobId) {
        super(systemContext, tenantId, computationJobId);
        setProcessor(new KubelessComputationJobActorMessageProcessor(tenantId, computationJobId, systemContext,
                logger, context().parent(), context().self(), computation));
    }

    public static class ActorCreator extends ContextBasedCreator<KubelessComputationJobActor> {
        private static final long serialVersionUID = 1L;

        private final TenantId tenantId;
        private final Computations computation;
        private final ComputationJobId computationJobId;

        public ActorCreator(ActorSystemContext context, TenantId tenantId, Computations computation, ComputationJobId computationJobId) {
            super(context);
            this.tenantId = tenantId;
            this.computation = computation;
            this.computationJobId = computationJobId;
        }

        @Override
        public KubelessComputationJobActor create() throws Exception {
            return new KubelessComputationJobActor(context, tenantId, computation, computationJobId);
        }
    }
}
