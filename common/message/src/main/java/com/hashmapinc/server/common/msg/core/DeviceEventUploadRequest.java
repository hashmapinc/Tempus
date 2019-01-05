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
package com.hashmapinc.server.common.msg.core;

import com.fasterxml.jackson.databind.JsonNode;
import com.hashmapinc.server.common.msg.session.FromDeviceRequestMsg;
import com.hashmapinc.server.common.msg.session.MsgType;

public class DeviceEventUploadRequest extends BasicRequest implements FromDeviceRequestMsg {

    private JsonNode eventInfo;

    public DeviceEventUploadRequest() {
        this(DEFAULT_REQUEST_ID);
    }

    public JsonNode getEventInfo() {
        return eventInfo;
    }

    public void setEventInfo(JsonNode eventInfo) {
        this.eventInfo = eventInfo;
    }

    public DeviceEventUploadRequest(Integer requestId) {
        super(requestId);
    }

    @Override
    public MsgType getMsgType() {
        return MsgType.POST_DEVICE_EVENT;
    }


}
