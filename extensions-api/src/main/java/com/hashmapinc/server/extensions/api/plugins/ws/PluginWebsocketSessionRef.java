/**
 * Copyright © 2016-2018 The Thingsboard Authors
 * Modifications © 2017-2018 Hashmap, Inc
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
package com.hashmapinc.server.extensions.api.plugins.ws;

import com.hashmapinc.server.common.data.id.PluginId;
import com.hashmapinc.server.common.data.id.TenantId;
import com.hashmapinc.server.extensions.api.plugins.PluginApiCallSecurityContext;

import java.io.Serializable;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.Map;

public interface PluginWebsocketSessionRef extends Serializable {

    String getSessionId();

    TenantId getPluginTenantId();

    PluginId getPluginId();

    URI getUri();

    Map<String, Object> getAttributes();

    InetSocketAddress getLocalAddress();

    InetSocketAddress getRemoteAddress();

    PluginApiCallSecurityContext getSecurityCtx();

}
