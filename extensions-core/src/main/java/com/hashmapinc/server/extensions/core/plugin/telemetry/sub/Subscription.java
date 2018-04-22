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
package com.hashmapinc.server.extensions.core.plugin.telemetry.sub;

import lombok.AllArgsConstructor;
import lombok.Data;
import com.hashmapinc.server.common.data.id.EntityId;
import com.hashmapinc.server.common.msg.cluster.ServerAddress;

import java.util.Map;

@Data
@AllArgsConstructor
public class Subscription<T>{

    private final SubscriptionState sub;
    protected final boolean local;
    protected ServerAddress server;

    public Subscription(SubscriptionState sub, boolean local) {
        this(sub, local, null);
    }

    public String getWsSessionId() {
        return getSub().getWsSessionId();
    }

    public int getSubscriptionId() {
        return getSub().getSubscriptionId();
    }

    public EntityId getEntityId() {
        return getSub().getEntityId();
    }

    public SubscriptionType getType() {
        return getSub().getType();
    }

    public String getScope() {
        return getSub().getScope();
    }

    public boolean isAllKeys() {
        return getSub().isAllKeys();
    }

    public Map<String, T> getKeyStates() {
        return getSub().getKeyStates();
    }

    public void setKeyState(String key, T stamp) {
        getSub().getKeyStates().put(key, stamp);
    }

    @Override
    public String toString() {
        return "Subscription{" +
                "sub=" + sub +
                ", local=" + local +
                ", server=" + server +
                '}';
    }
}
