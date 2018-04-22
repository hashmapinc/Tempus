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
package com.hashmapinc.server.common.msg.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.hashmapinc.server.common.msg.session.MsgType;
import lombok.ToString;
import com.hashmapinc.server.common.data.kv.KvEntry;

@ToString
public class BasicDepthTelemetryUploadRequest extends BasicRequest implements DepthTelemetryUploadRequest {

    private static final long serialVersionUID = 1L;
    //  telemetry DS
    private final Map<Double, List<KvEntry>> data;

    public BasicDepthTelemetryUploadRequest() {
        this(DEFAULT_REQUEST_ID);
    }

    public BasicDepthTelemetryUploadRequest(Integer requestId) {
        super(requestId);
        this.data = new HashMap<>();
    }

    //  telemetry ds
    public void addDs(double ds, KvEntry entry) {
        List<KvEntry> tsEntries = data.get(ds);
        if (tsEntries == null) {
            tsEntries = new ArrayList<>();
            data.put(ds, tsEntries);
        }
        tsEntries.add(entry);
    }

    @Override
    public MsgType getMsgType() {
        return MsgType.POST_TELEMETRY_REQUEST_DEPTH;
    }

    @Override
    public Map<Double, List<KvEntry>> getData() {
        return data;
    }

}
