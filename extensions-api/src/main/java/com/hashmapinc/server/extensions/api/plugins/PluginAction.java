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
package com.hashmapinc.server.extensions.api.plugins;

import com.hashmapinc.server.extensions.api.component.ConfigurableComponent;
import com.hashmapinc.server.extensions.api.plugins.msg.PluginToRuleMsg;
import com.hashmapinc.server.extensions.api.plugins.msg.RuleToPluginMsg;
import com.hashmapinc.server.extensions.api.rules.RuleContext;
import com.hashmapinc.server.extensions.api.rules.RuleLifecycleComponent;
import com.hashmapinc.server.extensions.api.rules.RuleProcessingMetaData;
import com.hashmapinc.server.common.msg.device.ToDeviceActorMsg;
import com.hashmapinc.server.common.msg.session.ToDeviceMsg;

import java.util.Optional;

public interface PluginAction<T> extends ConfigurableComponent<T>, RuleLifecycleComponent {

    Optional<RuleToPluginMsg> convert(RuleContext ctx, ToDeviceActorMsg toDeviceActorMsg, RuleProcessingMetaData deviceMsgMd);

    Optional<ToDeviceMsg> convert(PluginToRuleMsg<?> response);

    boolean isOneWayAction();

}
