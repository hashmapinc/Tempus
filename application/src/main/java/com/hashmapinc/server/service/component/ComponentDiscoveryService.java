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
package com.hashmapinc.server.service.component;

import com.hashmapinc.server.common.data.plugin.ComponentDescriptor;
import com.hashmapinc.server.common.data.plugin.ComponentType;
import com.hashmapinc.server.common.msg.computation.ComputationActionCompiled;
import com.hashmapinc.server.common.msg.computation.ComputationActionDeleted;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;


public interface ComponentDiscoveryService {

    void discoverComponents();

    List<ComponentDescriptor> getComponents(ComponentType type);

    Optional<ComponentDescriptor> getComponent(String clazz);

    List<ComponentDescriptor> getPluginActions(String pluginClazz);

    void updateActionsForPlugin(List<ComputationActionCompiled> action, String pluginClazz);

    void deleteActionsFromPlugin(ComputationActionDeleted deleted, Path path, String pluginClazz);

}
