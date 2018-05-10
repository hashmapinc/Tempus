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
package com.hashmapinc.server.common.transport.session;

import com.hashmapinc.server.common.data.Device;
import com.hashmapinc.server.common.data.security.DeviceCredentialsFilter;
import com.hashmapinc.server.common.transport.auth.DeviceAuthService;
import lombok.extern.slf4j.Slf4j;
import com.hashmapinc.server.common.msg.session.SessionContext;
import com.hashmapinc.server.common.transport.SessionMsgProcessor;
import com.hashmapinc.server.common.transport.auth.DeviceAuthResult;

import java.util.Optional;


@Slf4j
public abstract class DeviceAwareSessionContext implements SessionContext {

    protected final DeviceAuthService authService;
    protected final SessionMsgProcessor processor;

    protected volatile Device device;

    public DeviceAwareSessionContext(SessionMsgProcessor processor, DeviceAuthService authService) {
        this.processor = processor;
        this.authService = authService;
    }

    public DeviceAwareSessionContext(SessionMsgProcessor processor, DeviceAuthService authService, Device device) {
        this(processor, authService);
        this.device = device;
    }


    public boolean login(DeviceCredentialsFilter credentials) {
        DeviceAuthResult result = authService.process(credentials);
        if (result.isSuccess()) {
            Optional<Device> deviceOpt = authService.findDeviceById(result.getDeviceId());
            if (deviceOpt.isPresent()) {
                device = deviceOpt.get();
            }
            return true;
        } else {
            log.debug("Can't find device using credentials [{}] due to {}", credentials, result.getErrorMsg());
            return false;
        }
    }

    public DeviceAuthService getAuthService() {
        return authService;
    }

    public SessionMsgProcessor getProcessor() {
        return processor;
    }

    public Device getDevice() {
        return device;
    }
}
