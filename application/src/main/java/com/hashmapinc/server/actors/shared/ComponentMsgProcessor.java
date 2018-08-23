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
package com.hashmapinc.server.actors.shared;

import akka.actor.ActorContext;
import akka.event.LoggingAdapter;
import com.hashmapinc.server.actors.ActorSystemContext;
import com.hashmapinc.server.actors.stats.StatsPersistTick;
import com.hashmapinc.server.common.data.id.TenantId;
import com.hashmapinc.server.common.msg.cluster.ClusterEventMsg;
import com.hashmapinc.server.exception.TempusApplicationException;

public abstract class ComponentMsgProcessor<T> extends AbstractContextAwareMsgProcessor {

    protected final TenantId tenantId;
    protected final T entityId;

    protected ComponentMsgProcessor(ActorSystemContext systemContext, LoggingAdapter logger, TenantId tenantId, T id) {
        super(systemContext, logger);
        this.tenantId = tenantId;
        this.entityId = id;
    }

    public abstract void start() throws TempusApplicationException;

    public abstract void stop() throws TempusApplicationException;

    public abstract void onCreated(ActorContext context) throws TempusApplicationException;

    public abstract void onUpdate(ActorContext context) throws TempusApplicationException;

    public abstract void onActivate(ActorContext context) throws TempusApplicationException;

    public abstract void onSuspend(ActorContext context) throws TempusApplicationException;

    public abstract void onStop(ActorContext context) throws TempusApplicationException;

    public abstract void onClusterEventMsg(ClusterEventMsg msg) throws TempusApplicationException;

    public void scheduleStatsPersistTick(ActorContext context, long statsPersistFrequency) {
        schedulePeriodicMsgWithDelay(context, new StatsPersistTick(), statsPersistFrequency, statsPersistFrequency);
    }
}
