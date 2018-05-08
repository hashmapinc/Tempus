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
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.hashmapinc.server.common.msg.session.MsgType;
import lombok.ToString;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

@ToString
public class BasicGetAttributesRequest extends BasicRequest implements GetAttributesRequest {

    private static final long serialVersionUID = 1L;

    @JsonProperty
    private final Set<String> clientKeys;
    @JsonProperty
    private final Set<String> sharedKeys;

    public BasicGetAttributesRequest(Integer requestId) {
        this(requestId, Collections.emptySet(), Collections.emptySet());
    }

    @JsonCreator
    public BasicGetAttributesRequest(@JsonProperty("requestId") Integer requestId,
                                     @JsonProperty("clientKeys") Set<String> clientKeys,
                                     @JsonProperty("sharedKeys") Set<String> sharedKeys) {
        super(requestId);
        this.clientKeys = clientKeys;
        this.sharedKeys = sharedKeys;
    }

    @JsonIgnore
    @Override
    public MsgType getMsgType() {
        return MsgType.GET_ATTRIBUTES_REQUEST;
    }

    @JsonIgnore
    @Override
    public Optional<Set<String>> getClientAttributeNames() {
        return Optional.ofNullable(clientKeys);
    }

    @JsonIgnore
    @Override
    public Optional<Set<String>> getSharedAttributeNames() {
        return Optional.ofNullable(sharedKeys);
    }

}
