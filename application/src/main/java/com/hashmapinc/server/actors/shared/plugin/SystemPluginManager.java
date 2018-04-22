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
package com.hashmapinc.server.actors.shared.plugin;

import com.hashmapinc.server.actors.ActorSystemContext;
import com.hashmapinc.server.actors.service.DefaultActorService;
import com.hashmapinc.server.common.data.id.TenantId;
import com.hashmapinc.server.common.data.page.PageDataIterable;
import com.hashmapinc.server.dao.plugin.BasePluginService;
import com.hashmapinc.server.common.data.plugin.PluginMetaData;

public class SystemPluginManager extends PluginManager {

    public SystemPluginManager(ActorSystemContext systemContext) {
        super(systemContext);
    }

    @Override
    PageDataIterable.FetchFunction<PluginMetaData> getFetchPluginsFunction() {
        return pluginService::findSystemPlugins;
    }

    @Override
    TenantId getTenantId() {
        return BasePluginService.SYSTEM_TENANT;
    }

    @Override
    protected String getDispatcherName() {
        return DefaultActorService.SYSTEM_PLUGIN_DISPATCHER_NAME;
    }
}
