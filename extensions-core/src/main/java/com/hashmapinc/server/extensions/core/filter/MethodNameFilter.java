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
package com.hashmapinc.server.extensions.core.filter;

import com.hashmapinc.server.common.msg.session.MsgType;
import com.hashmapinc.server.extensions.api.component.Filter;
import com.hashmapinc.server.extensions.api.rules.RuleContext;
import lombok.extern.slf4j.Slf4j;
import com.hashmapinc.server.common.msg.core.ToServerRpcRequestMsg;
import com.hashmapinc.server.common.msg.device.ToDeviceActorMsg;
import com.hashmapinc.server.extensions.api.rules.RuleFilter;
import com.hashmapinc.server.extensions.api.rules.SimpleRuleLifecycleComponent;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Andrew Shvayka
 */
@Filter(name = "Method Name Filter", descriptor = "MethodNameFilterDescriptor.json", configuration = MethodNameFilterConfiguration.class)
@Slf4j
public class MethodNameFilter extends SimpleRuleLifecycleComponent implements RuleFilter<MethodNameFilterConfiguration> {

    private Set<String> methods;

    @Override
    public void init(MethodNameFilterConfiguration configuration) {
        methods = Arrays.stream(configuration.getMethodNames())
                .map(m -> m.getName())
                .collect(Collectors.toSet());
    }

    @Override
    public boolean filter(RuleContext ctx, ToDeviceActorMsg msg) {
        if (msg.getPayload().getMsgType() == MsgType.TO_SERVER_RPC_REQUEST) {
            return methods.contains(((ToServerRpcRequestMsg) msg.getPayload()).getMethod());
        } else {
            return false;
        }
    }
}
