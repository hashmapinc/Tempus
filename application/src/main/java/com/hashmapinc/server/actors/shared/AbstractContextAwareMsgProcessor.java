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
package com.hashmapinc.server.actors.shared;

import akka.actor.ActorContext;
import akka.actor.ActorRef;
import akka.actor.Scheduler;
import akka.event.LoggingAdapter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hashmapinc.server.actors.ActorSystemContext;
import com.hashmapinc.server.common.data.plugin.ComponentDescriptor;
import com.hashmapinc.server.common.data.plugin.ComponentType;
import com.hashmapinc.server.extensions.api.component.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import scala.concurrent.ExecutionContextExecutor;
import scala.concurrent.duration.Duration;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public abstract class AbstractContextAwareMsgProcessor {

    protected final ActorSystemContext systemContext;
    protected final LoggingAdapter logger;
    protected final ObjectMapper mapper = new ObjectMapper();

    protected AbstractContextAwareMsgProcessor(ActorSystemContext systemContext, LoggingAdapter logger) {
        super();
        this.systemContext = systemContext;
        this.logger = logger;
    }

    protected ActorRef getAppActor() {
        return systemContext.getAppActor();
    }

    protected Scheduler getScheduler() {
        return systemContext.getScheduler();
    }

    protected ExecutionContextExecutor getSystemDispatcher() {
        return systemContext.getActorSystem().dispatcher();
    }

    protected void schedulePeriodicMsgWithDelay(ActorContext ctx, Object msg, long delayInMs, long periodInMs) {
        schedulePeriodicMsgWithDelay(msg, delayInMs, periodInMs, ctx.self());
    }

    protected void schedulePeriodicMsgWithDelay(Object msg, long delayInMs, long periodInMs, ActorRef target) {
        logger.debug("Scheduling periodic msg {} every {} ms with delay {} ms", msg, periodInMs, delayInMs);
        getScheduler().schedule(Duration.create(delayInMs, TimeUnit.MILLISECONDS), Duration.create(periodInMs, TimeUnit.MILLISECONDS), target, msg, getSystemDispatcher(), null);
    }


    protected void scheduleMsgWithDelay(ActorContext ctx, Object msg, long delayInMs) {
        scheduleMsgWithDelay(msg, delayInMs, ctx.self());
    }

    protected void scheduleMsgWithDelay(Object msg, long delayInMs, ActorRef target) {
        logger.debug("Scheduling msg {} with delay {} ms", msg, delayInMs);
        getScheduler().scheduleOnce(Duration.create(delayInMs, TimeUnit.MILLISECONDS), target, msg, getSystemDispatcher(), null);
    }

    protected <T extends ConfigurableComponent> T initComponent(JsonNode componentNode) throws IOException, InstantiationException, IllegalAccessException, ClassNotFoundException {
        ComponentConfiguration configuration = new ComponentConfiguration(
                componentNode.get("clazz").asText(),
                componentNode.get("name").asText(),
                mapper.writeValueAsString(componentNode.get("configuration"))
        );
        logger.info("Initializing [{}][{}] component", configuration.getName(), configuration.getClazz());
        ComponentDescriptor componentDescriptor = systemContext.getComponentService().getComponent(configuration.getClazz())
                .orElseThrow(() -> new InstantiationException("Component Not found!"));
        return initComponent(componentDescriptor, configuration);
    }

    protected <T extends ConfigurableComponent> T initComponent(ComponentDescriptor componentDefinition, ComponentConfiguration configuration)
            throws ClassNotFoundException, IOException, InstantiationException, IllegalAccessException {
        return initComponent(componentDefinition.getClazz(), componentDefinition.getType(), configuration.getConfiguration());
    }

    protected <T extends ConfigurableComponent> T initComponent(String clazz, ComponentType type, String configuration)
            throws ClassNotFoundException, IllegalAccessException, InstantiationException, IOException {
        Class<?> componentClazz = Class.forName(clazz);
        T component = (T) (componentClazz.newInstance());
        Class<?> configurationClazz;
        switch (type) {
            case FILTER:
                configurationClazz = ((Filter) componentClazz.getAnnotation(Filter.class)).configuration();
                break;
            case PROCESSOR:
                configurationClazz = ((Processor) componentClazz.getAnnotation(Processor.class)).configuration();
                break;
            case ACTION:
                configurationClazz = ((Action) componentClazz.getAnnotation(Action.class)).configuration();
                break;
            case PLUGIN:
                configurationClazz = ((Plugin) componentClazz.getAnnotation(Plugin.class)).configuration();
                break;
            default:
                throw new IllegalStateException("Component with type: " + type + " is not supported!");
        }
        component.init(decode(configuration, configurationClazz));
        return component;
    }

    public <C> C decode(String configuration, Class<C> configurationClazz) throws IOException {
        logger.info("Initializing using configuration: {}", configuration);
        try {
            return mapper.readValue(configuration, configurationClazz);
        } catch (Exception e) {
            logger.info("Object mapping exception occurred {}", e);
        }
        return null;
    }

    @Data
    @AllArgsConstructor
    private static class ComponentConfiguration {
        private final String clazz;
        private final String name;
        private final String configuration;
    }

}
