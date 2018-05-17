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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.hashmapinc.server.common.msg.session.MsgType;

public class BasicCommandAckResponse extends BasicResponseMsg<Integer> implements StatusCodeResponse {

    private static final long serialVersionUID = 1L;

    public static BasicCommandAckResponse onSuccess(MsgType requestMsgType, Integer requestId) {
        return BasicCommandAckResponse.onSuccess(requestMsgType, requestId, 200);
    }

    public static BasicCommandAckResponse onSuccess(MsgType requestMsgType, Integer requestId, Integer code) {
        return new BasicCommandAckResponse(requestMsgType, requestId, true, null, code);
    }

    public static BasicCommandAckResponse onError(MsgType requestMsgType, Integer requestId, Exception error) {
        return new BasicCommandAckResponse(requestMsgType, requestId, false, error, null);
    }

    @JsonCreator
    private BasicCommandAckResponse(@JsonProperty("requestMsgType") MsgType requestMsgType,
                                    @JsonProperty("requestId") Integer requestId,
                                    @JsonProperty("success") boolean success,
                                    @JsonProperty("error") Exception error,
                                    @JsonProperty("data") Integer data) {
        super(requestMsgType, requestId, MsgType.TO_DEVICE_RPC_RESPONSE_ACK, success, error, data);
    }

    @Override
    public String toString() {
        return "BasicStatusCodeResponse []";
    }
}
