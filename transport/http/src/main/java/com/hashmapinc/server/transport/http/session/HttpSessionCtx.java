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
package com.hashmapinc.server.transport.http.session;

import com.google.gson.JsonObject;
import com.hashmapinc.server.common.data.id.SessionId;
import com.hashmapinc.server.common.msg.core.*;
import com.hashmapinc.server.common.msg.session.*;
import com.hashmapinc.server.common.msg.session.ex.SessionException;
import com.hashmapinc.server.common.transport.adaptor.JsonConverter;
import com.hashmapinc.server.common.transport.auth.DeviceAuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.async.DeferredResult;
import com.hashmapinc.server.common.msg.core.*;
import com.hashmapinc.server.common.msg.session.*;
import com.hashmapinc.server.common.transport.SessionMsgProcessor;
import com.hashmapinc.server.common.transport.session.DeviceAwareSessionContext;

import java.util.Optional;
import java.util.function.Consumer;


@Slf4j
public class HttpSessionCtx extends DeviceAwareSessionContext {

    private final SessionId sessionId;
    private final long timeout;
    private final DeferredResult<ResponseEntity> responseWriter;

    public HttpSessionCtx(SessionMsgProcessor processor, DeviceAuthService authService, DeferredResult<ResponseEntity> responseWriter, long timeout) {
        super(processor, authService);
        this.sessionId = new HttpSessionId();
        this.responseWriter = responseWriter;
        this.timeout = timeout;
    }

    @Override
    public SessionType getSessionType() {
        return SessionType.SYNC;
    }

    @Override
    public void onMsg(SessionActorToAdaptorMsg source) throws SessionException {
        ToDeviceMsg msg = source.getMsg();
        switch (msg.getMsgType()) {
            case GET_ATTRIBUTES_RESPONSE:
                reply((GetAttributesResponse) msg);
                return;
            case STATUS_CODE_RESPONSE:
                reply((StatusCodeResponse) msg);
                return;
            case ATTRIBUTES_UPDATE_NOTIFICATION:
                reply((AttributesUpdateNotification) msg);
                return;
            case TO_DEVICE_RPC_REQUEST:
                reply((ToDeviceRpcRequestMsg) msg);
                return;
            case TO_SERVER_RPC_RESPONSE:
                reply((ToServerRpcResponseMsg) msg);
                return;
            case RULE_ENGINE_ERROR:
                reply((RuleEngineErrorMsg) msg);
                return;
            default:
                break;
        }
    }

    private void reply(RuleEngineErrorMsg msg) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        switch (msg.getError()) {
            case PLUGIN_TIMEOUT:
                status = HttpStatus.REQUEST_TIMEOUT;
                break;
            default:
                if (msg.getInMsgType() == MsgType.TO_SERVER_RPC_REQUEST) {
                    status = HttpStatus.BAD_REQUEST;
                }
                break;
        }
        responseWriter.setResult(new ResponseEntity<>(JsonConverter.toErrorJson(msg.getErrorMsg()).toString(), status));
    }

    private <T> void reply(ResponseMsg<? extends T> msg, Consumer<T> f) {
        Optional<Exception> msgError = msg.getError();
        if (!msgError.isPresent()) {
            Optional<? extends T> msgData = msg.getData();
            if (msgData.isPresent()) {
                f.accept(msgData.get());
            }
        } else {
            Exception e = msgError.get();
            responseWriter.setResult(new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR));
        }
    }

    private void reply(ToDeviceRpcRequestMsg msg) {
        responseWriter.setResult(new ResponseEntity<>(JsonConverter.toJson(msg, true).toString(), HttpStatus.OK));
    }

    private void reply(ToServerRpcResponseMsg msg) {
        responseWriter.setResult(new ResponseEntity<>(JsonConverter.toJson(msg).toString(), HttpStatus.OK));
    }

    private void reply(AttributesUpdateNotification msg) {
        responseWriter.setResult(new ResponseEntity<>(JsonConverter.toJson(msg.getData(), false).toString(), HttpStatus.OK));
    }

    private void reply(GetAttributesResponse msg) {
        reply(msg, payload -> {
            if (payload.getClientAttributes().isEmpty() && payload.getSharedAttributes().isEmpty()) {
                responseWriter.setResult(new ResponseEntity<>(HttpStatus.NOT_FOUND));
            } else {
                JsonObject result = JsonConverter.toJson(payload, false);
                responseWriter.setResult(new ResponseEntity<>(result.toString(), HttpStatus.OK));
            }
        });
    }

    private void reply(StatusCodeResponse msg) {
        reply(msg, payload -> {
            if (payload == 0) {
                responseWriter.setResult(new ResponseEntity<>(HttpStatus.OK));
            } else {
                responseWriter.setResult(new ResponseEntity<>(HttpStatus.valueOf(payload)));
            }
        });
    }

    @Override
    public void onMsg(SessionCtrlMsg msg) throws SessionException {
        //Do nothing
    }

    @Override
    public boolean isClosed() {
        return false;
    }

    @Override
    public long getTimeout() {
        return timeout;
    }

    @Override
    public SessionId getSessionId() {
        return sessionId;
    }
}
