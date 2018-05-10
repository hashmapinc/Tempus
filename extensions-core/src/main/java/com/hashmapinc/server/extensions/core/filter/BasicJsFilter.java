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

import com.hashmapinc.server.common.msg.device.ToDeviceActorMsg;
import com.hashmapinc.server.extensions.api.rules.RuleContext;
import com.hashmapinc.server.extensions.api.rules.RuleFilter;
import lombok.extern.slf4j.Slf4j;

import javax.script.ScriptException;


@Slf4j
public abstract class BasicJsFilter implements RuleFilter<JsFilterConfiguration> {

    protected JsFilterConfiguration configuration;
    protected NashornJsEvaluator evaluator;

    @Override
    public void init(JsFilterConfiguration configuration) {
        this.configuration = configuration;
        initEvaluator(configuration);
    }

    @Override
    public boolean filter(RuleContext ctx, ToDeviceActorMsg msg) {
        try {
            return doFilter(ctx, msg);
        } catch (ScriptException e) {
            log.warn("RuleFilter evaluation exception: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    protected abstract boolean doFilter(RuleContext ctx, ToDeviceActorMsg msg) throws ScriptException;

    @Override
    public void resume() {
        initEvaluator(configuration);
    }

    @Override
    public void suspend() {
        destroyEvaluator();
    }

    @Override
    public void stop() {
        destroyEvaluator();
    }

    private void initEvaluator(JsFilterConfiguration configuration) {
        evaluator = new NashornJsEvaluator(configuration.getFilter());
    }

    private void destroyEvaluator() {
        if (evaluator != null) {
            evaluator.destroy();
        }
    }

}
