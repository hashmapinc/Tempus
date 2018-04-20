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
package org.thingsboard.server.transport.coap;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.CoapServer;
import org.eclipse.californium.core.network.CoapEndpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.thingsboard.server.common.transport.SessionMsgProcessor;
import org.thingsboard.server.common.transport.auth.DeviceAuthService;
import org.thingsboard.server.common.transport.quota.QuotaService;
import org.thingsboard.server.transport.coap.adaptors.CoapTransportAdaptor;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

@Service("CoapTransportService")
@ConditionalOnProperty(prefix = "coap", value = "enabled", havingValue = "true", matchIfMissing = true)
@Slf4j
public class CoapTransportService {

    private static final String V1 = "v1";
    private static final String API = "api";

    private CoapServer server;

    @Autowired(required = false)
    private ApplicationContext appContext;

    @Autowired(required = false)
    private SessionMsgProcessor processor;

    @Autowired(required = false)
    private DeviceAuthService authService;

    @Autowired(required = false)
    private QuotaService quotaService;


    @Value("${coap.bind_address}")
    private String host;
    @Value("${coap.bind_port}")
    private Integer port;
    @Value("${coap.adaptor}")
    private String adaptorName;
    @Value("${coap.timeout}")
    private Long timeout;

    private CoapTransportAdaptor adaptor;

    @PostConstruct
    public void init() throws UnknownHostException {
        log.info("Starting CoAP transport...");
        log.info("Lookup CoAP transport adaptor {}", adaptorName);
        this.adaptor = (CoapTransportAdaptor) appContext.getBean(adaptorName);
        log.info("Starting CoAP transport server");
        this.server = new CoapServer();
        createResources();
        InetAddress addr = InetAddress.getByName(host);
        InetSocketAddress sockAddr = new InetSocketAddress(addr, port);
        server.addEndpoint(new CoapEndpoint(sockAddr));
        server.start();
        log.info("CoAP transport started!");
    }

    private void createResources() {
        CoapResource api = new CoapResource(API);
        api.add(new CoapTransportResource(processor, authService, adaptor, V1, timeout, quotaService));
        server.add(api);
    }

    @PreDestroy
    public void shutdown() {
        log.info("Stopping CoAP transport!");
        this.server.destroy();
        log.info("CoAP transport stopped!");
    }
}
