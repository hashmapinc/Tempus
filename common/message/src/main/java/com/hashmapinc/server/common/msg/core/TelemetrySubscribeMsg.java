package com.hashmapinc.server.common.msg.core;

import com.hashmapinc.server.common.msg.session.FromDeviceMsg;
import com.hashmapinc.server.common.msg.session.MsgType;

public class TelemetrySubscribeMsg implements FromDeviceMsg {
    @Override
    public MsgType getMsgType() {
        return MsgType.SUBSCRIBE_SPARKPLUG_TELEMETRY_REQUEST;
    }
}
