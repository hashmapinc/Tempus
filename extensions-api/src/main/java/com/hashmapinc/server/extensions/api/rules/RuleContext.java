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
package com.hashmapinc.server.extensions.api.rules;

import com.google.common.util.concurrent.ListenableFuture;
import com.hashmapinc.server.common.data.Event;
import com.hashmapinc.server.common.data.alarm.Alarm;
import com.hashmapinc.server.common.data.alarm.AlarmId;
import com.hashmapinc.server.common.data.id.EntityId;
import com.hashmapinc.server.common.data.id.RuleId;
import com.hashmapinc.server.extensions.api.device.DeviceMetaData;

import java.util.Optional;

public interface RuleContext {

    RuleId getRuleId();

    DeviceMetaData getDeviceMetaData();

    Event save(Event event);

    Optional<Event> saveIfNotExists(Event event);

    Optional<Event> findEvent(String eventType, String eventUid);

    Optional<Alarm> findLatestAlarm(EntityId originator, String alarmType);

    Alarm createOrUpdateAlarm(Alarm alarm);

    ListenableFuture<Boolean> clearAlarm(AlarmId id, long clearTs);
}
