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
package com.hashmapinc.server.common.transport.quota;

import com.hashmapinc.server.common.transport.quota.inmemory.IntervalRegistryCleaner;
import org.junit.Before;
import org.junit.Test;
import com.hashmapinc.server.common.transport.quota.inmemory.HostRequestIntervalRegistry;
import com.hashmapinc.server.common.transport.quota.inmemory.IntervalRegistryLogger;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;


public class HostRequestsQuotaServiceTest {

    private HostRequestsQuotaService quotaService;

    private HostRequestIntervalRegistry requestRegistry = mock(HostRequestIntervalRegistry.class);
    private HostRequestLimitPolicy requestsPolicy = mock(HostRequestLimitPolicy.class);
    private IntervalRegistryCleaner registryCleaner = mock(IntervalRegistryCleaner.class);
    private IntervalRegistryLogger registryLogger = mock(IntervalRegistryLogger.class);

    @Before
    public void init() {
        quotaService = new HostRequestsQuotaService(requestRegistry, requestsPolicy, registryCleaner, registryLogger, true);
    }

    @Test
    public void quotaExceededIfRequestCountBiggerThanAllowed() {
        when(requestRegistry.tick("key")).thenReturn(10L);
        when(requestsPolicy.isValid(10L)).thenReturn(false);

        assertTrue(quotaService.isQuotaExceeded("key"));

        verify(requestRegistry).tick("key");
        verify(requestsPolicy).isValid(10L);
        verifyNoMoreInteractions(requestRegistry, requestsPolicy);
    }

    @Test
    public void quotaNotExceededIfRequestCountLessThanAllowed() {
        when(requestRegistry.tick("key")).thenReturn(10L);
        when(requestsPolicy.isValid(10L)).thenReturn(true);

        assertFalse(quotaService.isQuotaExceeded("key"));

        verify(requestRegistry).tick("key");
        verify(requestsPolicy).isValid(10L);
        verifyNoMoreInteractions(requestRegistry, requestsPolicy);
    }

    @Test
    public void serviceCanBeDisabled() {
        quotaService = new HostRequestsQuotaService(requestRegistry, requestsPolicy, registryCleaner, registryLogger, false);
        assertFalse(quotaService.isQuotaExceeded("key"));
        verifyNoMoreInteractions(requestRegistry, requestsPolicy);
    }
}