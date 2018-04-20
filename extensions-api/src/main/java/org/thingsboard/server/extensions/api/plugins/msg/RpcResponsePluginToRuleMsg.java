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
package org.thingsboard.server.extensions.api.plugins.msg;

import org.thingsboard.server.common.data.id.RuleId;
import org.thingsboard.server.common.data.id.TenantId;
import org.thingsboard.server.common.msg.core.ToServerRpcResponseMsg;

import java.util.UUID;

public class RpcResponsePluginToRuleMsg extends AbstractPluginToRuleMsg<ToServerRpcResponseMsg> {

    private static final long serialVersionUID = 1L;

    public RpcResponsePluginToRuleMsg(UUID uid, TenantId tenantId, RuleId ruleId, ToServerRpcResponseMsg payload) {
        super(uid, tenantId, ruleId, payload);
    }

}
