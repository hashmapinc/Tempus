/**
 * Copyright © 2016-2018 Hashmap, Inc
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
package org.thingsboard.server.actors.tenant;

import org.thingsboard.server.actors.rule.RuleActorChain;
import org.thingsboard.server.common.msg.device.ToDeviceActorMsg;

public class RuleChainDeviceMsg {

    private final ToDeviceActorMsg toDeviceActorMsg;
    private final RuleActorChain ruleChain;

    public RuleChainDeviceMsg(ToDeviceActorMsg toDeviceActorMsg, RuleActorChain ruleChain) {
        super();
        this.toDeviceActorMsg = toDeviceActorMsg;
        this.ruleChain = ruleChain;
    }

    public ToDeviceActorMsg getToDeviceActorMsg() {
        return toDeviceActorMsg;
    }

    public RuleActorChain getRuleChain() {
        return ruleChain;
    }

}
