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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.hashmapinc.server.common.msg.session.MsgType;
import lombok.Data;
import com.hashmapinc.server.common.msg.session.ToDeviceMsg;

/**
 * @author Andrew Shvayka
 */
@Data
public class ToDeviceRpcRequestMsg implements ToDeviceMsg {

    private final int requestId;
    private final String method;
    private final String params;

    @JsonIgnore
    @Override
    public MsgType getMsgType() {
        return MsgType.TO_DEVICE_RPC_REQUEST;
    }

    @JsonIgnore
    @Override
    public boolean isSuccess() {
        return true;
    }
}
