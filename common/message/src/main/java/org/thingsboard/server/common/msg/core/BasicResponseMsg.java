/**
 * Copyright © 2016-2018 Hashmap, Inc
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
package org.thingsboard.server.common.msg.core;

import java.io.Serializable;
import java.util.Optional;

import org.thingsboard.server.common.msg.session.MsgType;


public class BasicResponseMsg<T extends Serializable> implements ResponseMsg<T> {

    private static final long serialVersionUID = 1L;

    private final MsgType requestMsgType;
    private final Integer requestId;
    private final MsgType msgType;
    private final boolean success;
    private final T data;
    private final Exception error;

    protected BasicResponseMsg(MsgType requestMsgType, Integer requestId, MsgType msgType, boolean success, Exception error, T data) {
        super();
        this.requestMsgType = requestMsgType;
        this.requestId = requestId;
        this.msgType = msgType;
        this.success = success;
        this.error = error;
        this.data = data;
    }

    @Override
    public MsgType getRequestMsgType() {
        return requestMsgType;
    }

    @Override
    public Integer getRequestId() {
        return requestId;
    }

    @Override
    public boolean isSuccess() {
        return success;
    }

    @Override
    public Optional<Exception> getError() {
        return Optional.ofNullable(error);
    }

    @Override
    public Optional<T> getData() {
        return Optional.ofNullable(data);
    }

    @Override
    public String toString() {
        return "BasicResponseMsg [success=" + success + ", data=" + data + ", error=" + error + "]";
    }

    @Override
    public MsgType getMsgType() {
        return msgType;
    }
}
