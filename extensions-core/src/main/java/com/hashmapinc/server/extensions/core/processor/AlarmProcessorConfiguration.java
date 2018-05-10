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
package com.hashmapinc.server.extensions.core.processor;

import lombok.Data;

import java.util.List;


@Data
public class AlarmProcessorConfiguration {

    private String newAlarmExpression;
    private String clearAlarmExpression;

    private String alarmTypeTemplate;
    private String alarmSeverity;
    private String alarmStatus;
    private boolean alarmPropagateFlag;
    private boolean newAlarmFlag;

    private String alarmDetailsTemplate;

}