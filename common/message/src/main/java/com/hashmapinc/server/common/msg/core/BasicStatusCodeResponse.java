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
import lombok.ToString;

@ToString
public class BasicStatusCodeResponse extends BasicResponseMsg<Integer> implements StatusCodeResponse {

    private static final long serialVersionUID = 1L;

    public static BasicStatusCodeResponse onSuccess(MsgType requestMsgType, Integer requestId) {
        return BasicStatusCodeResponse.onSuccess(requestMsgType, requestId, 0);
    }

    public static BasicStatusCodeResponse onSuccess(MsgType requestMsgType, Integer requestId, Integer data) {
        return new BasicStatusCodeResponse(requestMsgType, requestId, true, null, data);
    }

    public static BasicStatusCodeResponse onError(MsgType requestMsgType, Integer requestId, Exception error) {
        return new BasicStatusCodeResponse(requestMsgType, requestId, false, error, null);
    }

    @JsonCreator
    private BasicStatusCodeResponse(@JsonProperty("requestMsgType") MsgType requestMsgType,
                                    @JsonProperty("requestId") Integer requestId,
                                    @JsonProperty("success") boolean success,
                                    @JsonProperty("error") Exception error,
                                    @JsonProperty("data") Integer data) {
        super(requestMsgType, requestId, MsgType.STATUS_CODE_RESPONSE, success, error, data);
    }
}
