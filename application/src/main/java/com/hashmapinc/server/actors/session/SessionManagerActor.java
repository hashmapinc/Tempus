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
package com.hashmapinc.server.actors.session;

import java.util.*;

import akka.actor.*;
import com.hashmapinc.server.actors.ActorSystemContext;
import com.hashmapinc.server.actors.service.ContextAwareActor;
import com.hashmapinc.server.actors.service.ContextBasedCreator;
import com.hashmapinc.server.actors.service.DefaultActorService;
import com.hashmapinc.server.actors.shared.SessionTimeoutMsg;
import com.hashmapinc.server.common.data.id.SessionId;
import com.hashmapinc.server.common.msg.aware.SessionAwareMsg;
import com.hashmapinc.server.common.msg.cluster.ClusterEventMsg;
import com.hashmapinc.server.common.msg.core.BasicToDeviceSessionActorMsg;
import com.hashmapinc.server.common.msg.core.SparkPlugSubscribeTerminateMsg;
import com.hashmapinc.server.common.msg.core.ToDeviceSessionActorMsg;

import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.hashmapinc.server.common.msg.session.BasicToDeviceActorSessionMsg;
import com.hashmapinc.server.common.msg.session.MsgType;
import com.hashmapinc.server.common.msg.session.SessionCtrlMsg;
import com.hashmapinc.server.common.msg.session.ToDeviceMsg;
import com.hashmapinc.server.common.msg.session.ctrl.SessionCloseMsg;
import com.hashmapinc.server.transport.http.session.HttpSessionId;
import com.hashmapinc.server.transport.mqtt.session.MqttSessionId;

public class SessionManagerActor extends ContextAwareActor {

    private static final int INITIAL_SESSION_MAP_SIZE = 1024;

    private final LoggingAdapter log = Logging.getLogger(getContext().system(), this);

    private final Map<String, ActorRef> sessionActors;

    private Map<String, List<DeviceSessionInfo>> deviceSessionInfoMap;

    public SessionManagerActor(ActorSystemContext systemContext) {
        super(systemContext);
        this.sessionActors = new HashMap<>(INITIAL_SESSION_MAP_SIZE);
        this.deviceSessionInfoMap = new HashMap<>();
    }

    @Override
    public void onReceive(Object msg) throws Exception {
        if (msg instanceof SessionCtrlMsg) {
            onSessionCtrlMsg((SessionCtrlMsg) msg);
        } else if (msg instanceof SessionAwareMsg) {
            forwardToSessionActor((SessionAwareMsg) msg);
        } else if (msg instanceof SessionTerminationMsg) {
            onSessionTermination((SessionTerminationMsg) msg);
        } else if (msg instanceof Terminated) {
            onTermination((Terminated) msg);
        } else if (msg instanceof SessionTimeoutMsg) {
            onSessionTimeout((SessionTimeoutMsg) msg);
        } else if (msg instanceof ClusterEventMsg) {
            broadcast(msg);
        }
    }

    private void broadcast(Object msg) {
        sessionActors.values().forEach(actorRef -> actorRef.tell(msg, ActorRef.noSender()));
    }

    private void onSessionTimeout(SessionTimeoutMsg msg) {
        String sessionIdStr = msg.getSessionId().toUidStr();
        ActorRef sessionActor = sessionActors.get(sessionIdStr);
        if (sessionActor != null) {
            sessionActor.tell(msg, ActorRef.noSender());
        }
    }

    private void onSessionCtrlMsg(SessionCtrlMsg msg) {
        String sessionIdStr = msg.getSessionId().toUidStr();
        checkForSparkPlugSubscription(msg, sessionIdStr);
        ActorRef sessionActor = sessionActors.get(sessionIdStr);
        if (sessionActor != null) {
            sessionActor.tell(msg, ActorRef.noSender());
        }
    }

    private void checkForSparkPlugSubscription(SessionCtrlMsg msg, String sessionIdStr){
        boolean sparkPlugSubscribeTermination = false;
        if(msg instanceof SessionCloseMsg){
            SessionCloseMsg sessionCloseMsg = (SessionCloseMsg) msg;
            if(sessionCloseMsg.getDeviceId() != null) {
                String deviceId = sessionCloseMsg.getDeviceId().toString();
                if (deviceSessionInfoMap.containsKey(deviceId)) {
                    List<DeviceSessionInfo> deviceSessionInfoList = deviceSessionInfoMap.get(deviceId);
                    for (DeviceSessionInfo deviceSessionInfo : deviceSessionInfoList) {
                        if ((deviceSessionInfo.getDeviceSessionId().contentEquals(sessionIdStr))
                                && deviceSessionInfo.getMsgType() == MsgType.POST_TELEMETRY_REQUEST) {
                            sparkPlugSubscribeTermination = true;
                        }
                    }
                    if (sparkPlugSubscribeTermination) {
                        sendSparkPlugSubscribeTerminateToSessionActor(deviceSessionInfoList);
                    }
                    removeDeviceSessionInfoList(deviceId);
                }
            }
        }
    }

    private void sendSparkPlugSubscribeTerminateToSessionActor(List<DeviceSessionInfo> deviceSessionInfoList){
        for (DeviceSessionInfo deviceSessionInfo : deviceSessionInfoList){
            log.debug("Message type sendSparkPlugSubscribeTerminateToSessionActor " + deviceSessionInfo.getMsgType());
            if(deviceSessionInfo.getMsgType() == MsgType.SPARKPLUG_DEATH_SUBSCRIBE){
                ToDeviceMsg sparkPlugSubscribeTerminateMsg = new SparkPlugSubscribeTerminateMsg();
                SessionId sessionId = new MqttSessionId(deviceSessionInfo.getDeviceSessionId());
                ToDeviceSessionActorMsg response = new BasicToDeviceSessionActorMsg(sparkPlugSubscribeTerminateMsg,sessionId);
                ActorRef sessionActor = sessionActors.get(deviceSessionInfo.getDeviceSessionId());
                if(sessionActor != null){
                    sessionActor.tell(response, ActorRef.noSender());
                }
            }
        }
    }

    private void removeDeviceSessionInfoList(String deviceId){
        List<DeviceSessionInfo> deviceSessionInfoList= deviceSessionInfoMap.remove(deviceId);
        if (deviceSessionInfoList != null) {
            log.debug("[{}] sessions removed. ", deviceSessionInfoList);
        } else {
            log.debug("sessions were already removed.");
        }
    }

    private void onSessionTermination(SessionTerminationMsg msg) {
        String sessionIdStr = msg.getId().toUidStr();
        ActorRef sessionActor = sessionActors.remove(sessionIdStr);
        if (sessionActor != null) {
            log.debug("[{}] Removed session actor.", sessionIdStr);
            //TODO: onSubscriptionUpdate device actor about session close;
        } else {
            log.debug("[{}] Session actor was already removed.", sessionIdStr);
        }
    }

    private void forwardToSessionActor(SessionAwareMsg msg) {
        if (msg instanceof ToDeviceSessionActorMsg || msg instanceof SessionCloseMsg) {
            String sessionIdStr = msg.getSessionId().toUidStr();
            ActorRef sessionActor = sessionActors.get(sessionIdStr);
            if (sessionActor != null) {
                sessionActor.tell(msg, ActorRef.noSender());
            } else {
                log.debug("[{}] Session actor was already removed.", sessionIdStr);
            }
        } else {
            try {
                updateDeviceInfoMap(msg);
                getOrCreateSessionActor(msg.getSessionId()).tell(msg, self());
            } catch (InvalidActorNameException e) {
                log.info("Invalid msg : {}", msg);
            }
        }
    }

    private void updateDeviceInfoMap(SessionAwareMsg msg){
        if(msg instanceof BasicToDeviceActorSessionMsg){
            BasicToDeviceActorSessionMsg basicToDeviceActorSessionMsg = (BasicToDeviceActorSessionMsg)msg;
            String deviceId = basicToDeviceActorSessionMsg.getDeviceId().toString();
            MsgType msgType = basicToDeviceActorSessionMsg.getSessionMsg().getMsg().getMsgType();
            String sessionId = msg.getSessionId().toUidStr();
            DeviceSessionInfo deviceSessionInfo = new DeviceSessionInfo(sessionId, msgType);
            if(deviceSessionInfoMap.containsKey(deviceId))
            {
                List<DeviceSessionInfo> deviceSessionInfoList = deviceSessionInfoMap.get(deviceId);
                deviceSessionInfoList.add(deviceSessionInfo);
                deviceSessionInfoMap.put(deviceId, deviceSessionInfoList);
            }
            else {
                List<DeviceSessionInfo> deviceSessionInfoList = new ArrayList<>();
                deviceSessionInfoList.add(deviceSessionInfo);
                deviceSessionInfoMap.put(deviceId, deviceSessionInfoList);
            }
        }
    }

    private ActorRef getOrCreateSessionActor(SessionId sessionId) {
        String sessionIdStr = sessionId.toUidStr();
        ActorRef sessionActor = sessionActors.get(sessionIdStr);
        if (sessionActor == null) {
            log.debug("[{}] Creating session actor.", sessionIdStr);
            sessionActor = context().actorOf(
                    Props.create(new SessionActor.ActorCreator(systemContext, sessionId)).withDispatcher(DefaultActorService.SESSION_DISPATCHER_NAME),
                    sessionIdStr);
            sessionActors.put(sessionIdStr, sessionActor);
            log.debug("[{}] Created session actor.", sessionIdStr);
        }
        return sessionActor;
    }

    private void onTermination(Terminated message) {
        ActorRef terminated = message.actor();
        if (terminated instanceof LocalActorRef) {
            log.info("Removed actor: {}.", terminated);
            //TODO: cleanup session actors map
        } else {
            throw new IllegalStateException("Remote actors are not supported!");
        }
    }

    public static class ActorCreator extends ContextBasedCreator<SessionManagerActor> {
        private static final long serialVersionUID = 1L;

        public ActorCreator(ActorSystemContext context) {
            super(context);
        }

        @Override
        public SessionManagerActor create() throws Exception {
            return new SessionManagerActor(context);
        }
    }

}
