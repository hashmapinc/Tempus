package com.hashmapinc.server.common.msg.core;

import com.hashmapinc.server.common.msg.session.FromDeviceMsg;
import com.hashmapinc.server.common.msg.session.MsgType;

public class TelemetryUnsubscribeMsg implements FromDeviceMsg {
    @Override
    public MsgType getMsgType() {
        return MsgType.UNSUBSCRIBE_SPARKPLUG_TELEMETRY_REQUEST;
    }
}
