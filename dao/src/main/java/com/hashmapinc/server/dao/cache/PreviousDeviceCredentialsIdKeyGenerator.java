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
package com.hashmapinc.server.dao.cache;

import com.hashmapinc.server.dao.device.DeviceCredentialsService;
import org.springframework.cache.interceptor.KeyGenerator;
import com.hashmapinc.server.common.data.security.DeviceCredentials;

import java.lang.reflect.Method;

public class PreviousDeviceCredentialsIdKeyGenerator implements KeyGenerator {

    private static final String NOT_VALID_DEVICE = "notValidDeviceCredentialsId";

    @Override
    public Object generate(Object o, Method method, Object... objects) {
        DeviceCredentialsService deviceCredentialsService = (DeviceCredentialsService) o;
        DeviceCredentials deviceCredentials = (DeviceCredentials) objects[0];
        if (deviceCredentials.getDeviceId() != null) {
            DeviceCredentials oldDeviceCredentials = deviceCredentialsService.findDeviceCredentialsByDeviceId(deviceCredentials.getDeviceId());
            if (oldDeviceCredentials != null) {
                return oldDeviceCredentials.getCredentialsId();
            }
        }
        return NOT_VALID_DEVICE;
    }
}
