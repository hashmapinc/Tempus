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
package com.hashmapinc.server.transport.mqtt.session;

import com.hashmapinc.server.common.data.id.SessionId;
import com.hashmapinc.server.common.msg.session.ctrl.SessionCloseMsg;
import com.hashmapinc.server.common.msg.session.ex.SessionException;
import com.hashmapinc.server.common.transport.auth.DeviceAuthService;
import com.hashmapinc.server.transport.mqtt.adaptors.MqttTransportAdaptor;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.mqtt.*;
import lombok.extern.slf4j.Slf4j;
import com.hashmapinc.server.common.msg.session.SessionActorToAdaptorMsg;
import com.hashmapinc.server.common.msg.session.SessionCtrlMsg;
import com.hashmapinc.server.common.msg.session.SessionType;
import com.hashmapinc.server.common.transport.SessionMsgProcessor;
import com.hashmapinc.server.common.transport.adaptor.AdaptorException;
import com.hashmapinc.server.common.transport.session.DeviceAwareSessionContext;

import java.util.concurrent.atomic.AtomicInteger;


@Slf4j
public class DeviceSessionCtx extends DeviceAwareSessionContext {

    private final MqttTransportAdaptor adaptor;
    private final MqttSessionId sessionId;
    private ChannelHandlerContext channel;
    private volatile boolean allowAttributeResponses;
    private AtomicInteger msgIdSeq = new AtomicInteger(0);

    public DeviceSessionCtx(SessionMsgProcessor processor, DeviceAuthService authService, MqttTransportAdaptor adaptor) {
        super(processor, authService);
        this.adaptor = adaptor;
        this.sessionId = new MqttSessionId();
    }

    @Override
    public SessionType getSessionType() {
        return SessionType.ASYNC;
    }

    @Override
    public void onMsg(SessionActorToAdaptorMsg msg) throws SessionException {
        try {
            adaptor.convertToAdaptorMsg(this, msg).ifPresent(this::pushToNetwork);
        } catch (AdaptorException e) {
            //TODO: close channel with disconnect;
            logAndWrap(e);
        }
    }

    private void logAndWrap(AdaptorException e) throws SessionException {
        log.warn("Failed to convert msg: {}", e.getMessage(), e);
        throw new SessionException(e);
    }

    private void pushToNetwork(MqttMessage msg) {
        channel.writeAndFlush(msg);
    }

    @Override
    public void onMsg(SessionCtrlMsg msg) throws SessionException {
        if (msg instanceof SessionCloseMsg) {
            pushToNetwork(new MqttMessage(new MqttFixedHeader(MqttMessageType.DISCONNECT, false, MqttQoS.AT_MOST_ONCE, false, 0)));
            channel.close();
        }
    }

    @Override
    public boolean isClosed() {
        return false;
    }

    @Override
    public long getTimeout() {
        return 0;
    }

    @Override
    public SessionId getSessionId() {
        return sessionId;
    }

    public void setChannel(ChannelHandlerContext channel) {
        this.channel = channel;
    }

    public void setAllowAttributeResponses() {
        allowAttributeResponses = true;
    }

    public void setDisallowAttributeResponses() {
        allowAttributeResponses = false;
    }

    public int nextMsgId() {
        return msgIdSeq.incrementAndGet();
    }
}
