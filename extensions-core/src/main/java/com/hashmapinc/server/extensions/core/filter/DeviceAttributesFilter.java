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

import com.hashmapinc.server.common.msg.core.UpdateAttributesRequest;
import com.hashmapinc.server.common.msg.session.FromDeviceMsg;
import com.hashmapinc.server.extensions.api.component.Filter;
import com.hashmapinc.server.extensions.api.device.DeviceAttributes;
import com.hashmapinc.server.extensions.api.rules.RuleContext;
import lombok.extern.slf4j.Slf4j;
import com.hashmapinc.server.common.msg.device.ToDeviceActorMsg;

import javax.script.Bindings;
import javax.script.ScriptException;

/**
 * @author Andrew Shvayka
 */
@Filter(name = "Device Attributes Filter", descriptor = "JsFilterDescriptor.json", configuration = JsFilterConfiguration.class)
@Slf4j
public class DeviceAttributesFilter extends BasicJsFilter {

    @Override
    protected boolean doFilter(RuleContext ctx, ToDeviceActorMsg msg) throws ScriptException {
        return evaluator.execute(toBindings(ctx.getDeviceMetaData().getDeviceAttributes(), msg != null ? msg.getPayload() : null));
    }

    private Bindings toBindings(DeviceAttributes attributes, FromDeviceMsg msg) {
        Bindings bindings = NashornJsEvaluator.getAttributeBindings(attributes);

        if (msg != null) {
            switch (msg.getMsgType()) {
                case POST_ATTRIBUTES_REQUEST:
                    bindings = NashornJsEvaluator.updateBindings(bindings, (UpdateAttributesRequest) msg);
                    break;
                default:
                    break;
            }
        }

        return bindings;
    }

}
