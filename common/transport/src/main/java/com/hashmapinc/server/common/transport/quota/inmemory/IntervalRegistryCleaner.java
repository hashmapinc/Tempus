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
package com.hashmapinc.server.common.transport.quota.inmemory;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


@Component
@Slf4j
public class IntervalRegistryCleaner {

    private final HostRequestIntervalRegistry intervalRegistry;
    private final long cleanPeriodMs;
    private ScheduledExecutorService executor;

    public IntervalRegistryCleaner(HostRequestIntervalRegistry intervalRegistry, @Value("${quota.host.cleanPeriodMs}") long cleanPeriodMs) {
        this.intervalRegistry = intervalRegistry;
        this.cleanPeriodMs = cleanPeriodMs;
    }

    public void schedule() {
        if (executor != null) {
            throw new IllegalStateException("Registry Cleaner already scheduled");
        }
        executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(this::clean, cleanPeriodMs, cleanPeriodMs, TimeUnit.MILLISECONDS);
    }

    public void stop() {
        if (executor != null) {
            executor.shutdown();
        }
    }

    public void clean() {
        try {
            intervalRegistry.clean();
        } catch (RuntimeException ex) {
            log.error("Could not clear Interval Registry", ex);
        }
    }

}
