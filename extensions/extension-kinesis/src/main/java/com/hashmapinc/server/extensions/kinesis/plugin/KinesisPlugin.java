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
package com.hashmapinc.server.extensions.kinesis.plugin;


import com.amazonaws.services.kinesisfirehose.AmazonKinesisFirehoseAsync;
import com.amazonaws.services.kinesisfirehose.AmazonKinesisFirehoseClient;
import com.hashmapinc.server.extensions.api.plugins.PluginContext;
import com.hashmapinc.server.extensions.kinesis.action.KinesisPluginAction;
import com.hashmapinc.server.extensions.api.component.Plugin;
import com.hashmapinc.server.extensions.api.plugins.AbstractPlugin;
import com.hashmapinc.server.extensions.api.plugins.handlers.RuleMsgHandler;

import com.amazonaws.services.kinesis.AmazonKinesisAsync;
import lombok.extern.slf4j.Slf4j;
import com.hashmapinc.server.extensions.api.plugins.PluginInitializationException;

/**
 * @author Mitesh Rathore
 */

@Plugin(name = "Kinesis Plugin", actions = {KinesisPluginAction.class},
        descriptor = "KinesisPluginDescriptor.json", configuration = KinesisPluginConfiguration.class)
@Slf4j
public class KinesisPlugin extends AbstractPlugin<KinesisPluginConfiguration> {

    private KinesisMessageHandler kinesisHandler;
    private AmazonKinesisAsync kinesis;

    @Override
    public void init(KinesisPluginConfiguration configuration) {
        try {
            kinesis = KinesisAsyncFactory.INSTANCE.create(configuration);
            init();
        } catch (Exception e) {
            throw new PluginInitializationException("Could not initialize Kinesis plugin", e);
        }
    }

    private void init() {
        this.kinesisHandler = new KinesisMessageHandler(kinesis);
    }

    private void destroy() {
        try {
            this.kinesisHandler = null;
            this.kinesis.shutdown();
        } catch (Exception e) {
            log.error("Failed to shutdown Kinesis client during destroy()", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    protected RuleMsgHandler getRuleMsgHandler() {
        return kinesisHandler;
    }

    @Override
    public void resume(PluginContext ctx) {
        init();
    }

    @Override
    public void suspend(PluginContext ctx) {
        destroy();
    }

    @Override
    public void stop(PluginContext ctx) {
        destroy();
    }
}
