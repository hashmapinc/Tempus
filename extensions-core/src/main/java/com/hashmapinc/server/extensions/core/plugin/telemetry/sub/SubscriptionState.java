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

import com.hashmapinc.server.common.data.id.EntityId;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;


@AllArgsConstructor
public class SubscriptionState<T> {

    @Getter protected final String wsSessionId;
    @Getter protected final int subscriptionId;
    @Getter protected final EntityId entityId;
    @Getter protected final SubscriptionType type;
    @Getter protected final boolean allKeys;
    @Getter protected final Map<String, Long> keyStates;
    @Getter protected final String scope;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SubscriptionState that = (SubscriptionState) o;

        if (subscriptionId != that.subscriptionId) return false;
        if (wsSessionId != null ? !wsSessionId.equals(that.wsSessionId) : that.wsSessionId != null) return false;
        if (entityId != null ? !entityId.equals(that.entityId) : that.entityId != null) return false;
        return type == that.type;
    }

    @Override
    public int hashCode() {
        int result = wsSessionId != null ? wsSessionId.hashCode() : 0;
        result = 31 * result + subscriptionId;
        result = 31 * result + (entityId != null ? entityId.hashCode() : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "SubscriptionState{" +
                "type=" + type +
                ", entityId=" + entityId +
                ", subscriptionId=" + subscriptionId +
                ", wsSessionId='" + wsSessionId + '\'' +
                '}';
    }
}
