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
import com.hashmapinc.server.common.msg.kv.AttributesKVMsg;

@ToString
public class BasicGetAttributesResponse extends BasicResponseMsg<AttributesKVMsg> implements GetAttributesResponse {

    private static final long serialVersionUID = 1L;

    public static BasicGetAttributesResponse onSuccess(MsgType requestMsgType, int requestId, AttributesKVMsg code) {
        return new BasicGetAttributesResponse(requestMsgType, requestId, true, null, code);
    }

    public static BasicGetAttributesResponse onError(MsgType requestMsgType, int requestId, Exception error) {
        return new BasicGetAttributesResponse(requestMsgType, requestId, false, error, null);
    }

    @JsonCreator
    private BasicGetAttributesResponse(@JsonProperty("requestMsgType") MsgType requestMsgType,
                                       @JsonProperty("requestId") Integer requestId,
                                       @JsonProperty("success") boolean success,
                                       @JsonProperty("error") Exception error,
                                       @JsonProperty("data") AttributesKVMsg data) {
        super(requestMsgType, requestId, MsgType.GET_ATTRIBUTES_RESPONSE, success, error, data);
    }

}
