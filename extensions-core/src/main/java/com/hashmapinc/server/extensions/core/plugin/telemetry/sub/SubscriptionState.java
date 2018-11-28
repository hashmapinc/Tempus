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
package com.hashmapinc.server.extensions.core.plugin.telemetry.sub;

import com.hashmapinc.server.common.data.id.EntityId;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;


@AllArgsConstructor
public class SubscriptionState<T> {

    @Getter protected final String wsSessionId;
    @Getter protected final int subscriptionId;
    @Getter protected final EntityId entityId;
    @Getter protected final SubscriptionType type;
    @Getter protected final boolean allKeys;
    @Getter protected final Map<String, T> keyStates;
    @Getter protected final String scope;
    @Getter @Setter protected Long timeZoneDiff;

    public SubscriptionState(String wsSessionId, int subscriptionId, EntityId entityId, SubscriptionType type, boolean allKeys, Map<String, T> keyStates, String scope) {
        this.wsSessionId = wsSessionId;
        this.subscriptionId = subscriptionId;
        this.entityId = entityId;
        this.type = type;
        this.allKeys = allKeys;
        this.keyStates = keyStates;
        this.scope = scope;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SubscriptionState that = (SubscriptionState) o;

        if (subscriptionId != that.subscriptionId) return false;
        if (wsSessionId != null ? !wsSessionId.equals(that.wsSessionId) : that.wsSessionId != null) return false;
        if (entityId != null ? !entityId.equals(that.entityId) : that.entityId != null) return false;
        if (timeZoneDiff != null ? !timeZoneDiff.equals(that.timeZoneDiff) : that.timeZoneDiff != null) return false;
        return type == that.type;
    }

    @Override
    public int hashCode() {
        int result = wsSessionId != null ? wsSessionId.hashCode() : 0;
        result = 31 * result + subscriptionId;
        result = 31 * result + (entityId != null ? entityId.hashCode() : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (timeZoneDiff != null ? timeZoneDiff.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "SubscriptionState{" +
                "type=" + type +
                ", entityId=" + entityId +
                ", subscriptionId=" + subscriptionId +
                ", timeZoneDiff=" + timeZoneDiff +
                ", wsSessionId='" + wsSessionId + '\'' +
                '}';
    }
}
