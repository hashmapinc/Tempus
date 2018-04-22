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
package com.hashmapinc.server.controller;

import com.hashmapinc.server.common.data.alarm.*;
import com.hashmapinc.server.common.data.id.EntityId;
import com.hashmapinc.server.common.data.id.EntityIdFactory;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.hashmapinc.server.common.data.alarm.*;
import com.hashmapinc.server.common.data.id.*;
import com.hashmapinc.server.common.data.page.TimePageData;
import com.hashmapinc.server.common.data.page.TimePageLink;
import com.hashmapinc.server.exception.TempusErrorCode;
import com.hashmapinc.server.exception.TempusException;

@RestController
@RequestMapping("/api")
public class AlarmController extends BaseController {

    public static final String ALARM_ID = "alarmId";

    @PreAuthorize("hasAnyAuthority('SYS_ADMIN', 'TENANT_ADMIN', 'CUSTOMER_USER')")
    @RequestMapping(value = "/alarm/{alarmId}", method = RequestMethod.GET)
    @ResponseBody
    public Alarm getAlarmById(@PathVariable(ALARM_ID) String strAlarmId) throws TempusException {
        checkParameter(ALARM_ID, strAlarmId);
        try {
            AlarmId alarmId = new AlarmId(toUUID(strAlarmId));

            return checkAlarmId(alarmId);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @PreAuthorize("hasAnyAuthority('SYS_ADMIN', 'TENANT_ADMIN', 'CUSTOMER_USER')")
    @RequestMapping(value = "/alarm/info/{alarmId}", method = RequestMethod.GET)
    @ResponseBody
    public AlarmInfo getAlarmInfoById(@PathVariable(ALARM_ID) String strAlarmId) throws TempusException {
        checkParameter(ALARM_ID, strAlarmId);
        try {
            AlarmId alarmId = new AlarmId(toUUID(strAlarmId));
            return checkAlarmInfoId(alarmId);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @PreAuthorize("hasAnyAuthority('SYS_ADMIN', 'TENANT_ADMIN', 'CUSTOMER_USER')")
    @RequestMapping(value = "/alarm", method = RequestMethod.POST)
    @ResponseBody
    public Alarm saveAlarm(@RequestBody Alarm alarm) throws TempusException {
        try {
            alarm.setTenantId(getCurrentUser().getTenantId());
            return checkNotNull(alarmService.createOrUpdateAlarm(alarm));
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @PreAuthorize("hasAnyAuthority('SYS_ADMIN', 'TENANT_ADMIN', 'CUSTOMER_USER')")
    @RequestMapping(value = "/alarm/{alarmId}/ack", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.OK)
    public void ackAlarm(@PathVariable(ALARM_ID) String strAlarmId) throws TempusException {
        checkParameter(ALARM_ID, strAlarmId);
        try {
            AlarmId alarmId = new AlarmId(toUUID(strAlarmId));
            checkAlarmId(alarmId);
            alarmService.ackAlarm(alarmId, System.currentTimeMillis()).get();
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @PreAuthorize("hasAnyAuthority('SYS_ADMIN', 'TENANT_ADMIN', 'CUSTOMER_USER')")
    @RequestMapping(value = "/alarm/{alarmId}/clear", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.OK)
    public void clearAlarm(@PathVariable(ALARM_ID) String strAlarmId) throws TempusException {
        checkParameter(ALARM_ID, strAlarmId);
        try {
            AlarmId alarmId = new AlarmId(toUUID(strAlarmId));
            checkAlarmId(alarmId);
            alarmService.clearAlarm(alarmId, System.currentTimeMillis()).get();
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @PreAuthorize("hasAnyAuthority('SYS_ADMIN', 'TENANT_ADMIN', 'CUSTOMER_USER')")
    @RequestMapping(value = "/alarm/{entityType}/{entityId}", method = RequestMethod.GET)
    @ResponseBody
    public TimePageData<AlarmInfo> getAlarms(
            @PathVariable("entityType") String strEntityType,
            @PathVariable("entityId") String strEntityId,
            @RequestParam(required = false) String searchStatus,
            @RequestParam(required = false) String status,
            @RequestParam int limit,
            @RequestParam(required = false) Long startTime,
            @RequestParam(required = false) Long endTime,
            @RequestParam(required = false, defaultValue = "false") boolean ascOrder,
            @RequestParam(required = false) String offset,
            @RequestParam(required = false) Boolean fetchOriginator
    ) throws TempusException {
        checkParameter("EntityId", strEntityId);
        checkParameter("EntityType", strEntityType);
        EntityId entityId = EntityIdFactory.getByTypeAndId(strEntityType, strEntityId);
        AlarmSearchStatus alarmSearchStatus = StringUtils.isEmpty(searchStatus) ? null : AlarmSearchStatus.valueOf(searchStatus);
        AlarmStatus alarmStatus = StringUtils.isEmpty(status) ? null : AlarmStatus.valueOf(status);
        if (alarmSearchStatus != null && alarmStatus != null) {
            throw new TempusException("Invalid alarms search query: Both parameters 'searchStatus' " +
                    "and 'status' can't be specified at the same time!", TempusErrorCode.BAD_REQUEST_PARAMS);
        }
        checkEntityId(entityId);
        try {
            TimePageLink pageLink = createPageLink(limit, startTime, endTime, ascOrder, offset);
            return checkNotNull(alarmService.findAlarms(new AlarmQuery(entityId, pageLink, alarmSearchStatus, alarmStatus, fetchOriginator)).get());
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @PreAuthorize("hasAnyAuthority('SYS_ADMIN', 'TENANT_ADMIN', 'CUSTOMER_USER')")
    @RequestMapping(value = "/alarm/highestSeverity/{entityType}/{entityId}", method = RequestMethod.GET)
    @ResponseBody
    public AlarmSeverity getHighestAlarmSeverity(
            @PathVariable("entityType") String strEntityType,
            @PathVariable("entityId") String strEntityId,
            @RequestParam(required = false) String searchStatus,
            @RequestParam(required = false) String status
    ) throws TempusException {
        checkParameter("EntityId", strEntityId);
        checkParameter("EntityType", strEntityType);
        EntityId entityId = EntityIdFactory.getByTypeAndId(strEntityType, strEntityId);
        AlarmSearchStatus alarmSearchStatus = StringUtils.isEmpty(searchStatus) ? null : AlarmSearchStatus.valueOf(searchStatus);
        AlarmStatus alarmStatus = StringUtils.isEmpty(status) ? null : AlarmStatus.valueOf(status);
        if (alarmSearchStatus != null && alarmStatus != null) {
            throw new TempusException("Invalid alarms search query: Both parameters 'searchStatus' " +
                    "and 'status' can't be specified at the same time!", TempusErrorCode.BAD_REQUEST_PARAMS);
        }
        checkEntityId(entityId);
        try {
            return alarmService.findHighestAlarmSeverity(entityId, alarmSearchStatus, alarmStatus);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

}
