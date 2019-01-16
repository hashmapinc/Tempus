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
package com.hashmapinc.server.transport.mqtt;

import com.hashmapinc.server.dao.asset.AssetService;
import com.hashmapinc.server.dao.attributes.AttributesService;
import com.hashmapinc.server.dao.mail.MailService;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.mqtt.MqttDecoder;
import io.netty.handler.codec.mqtt.MqttEncoder;
import io.netty.handler.ssl.SslHandler;
import com.hashmapinc.server.common.transport.SessionMsgProcessor;
import com.hashmapinc.server.common.transport.auth.DeviceAuthService;
import com.hashmapinc.server.common.transport.quota.QuotaService;
import com.hashmapinc.server.dao.device.DeviceService;
import com.hashmapinc.server.dao.relation.RelationService;
import com.hashmapinc.server.transport.mqtt.adaptors.MqttTransportAdaptor;


public class MqttTransportServerInitializer extends ChannelInitializer<SocketChannel> {

    private static final int MAX_PAYLOAD_SIZE = 64 * 1024 * 1024;

    private final SessionMsgProcessor processor;
    private final DeviceService deviceService;
    private final DeviceAuthService authService;
    private final RelationService relationService;
    private final MqttTransportAdaptor adaptor;
    private final MqttSslHandlerProvider sslHandlerProvider;
    private final QuotaService quotaService;
    private final AttributesService attributesService;
    private final AssetService assetService;
    private final MailService mailService;

    public MqttTransportServerInitializer(SessionMsgProcessor processor, DeviceService deviceService, DeviceAuthService authService, RelationService relationService,
                                          MqttTransportAdaptor adaptor, MqttSslHandlerProvider sslHandlerProvider,
                                          QuotaService quotaService, AttributesService attributesService ,AssetService assetService,MailService mailService) {
        this.processor = processor;
        this.deviceService = deviceService;
        this.authService = authService;
        this.relationService = relationService;
        this.adaptor = adaptor;
        this.sslHandlerProvider = sslHandlerProvider;
        this.quotaService = quotaService;
        this.attributesService = attributesService;
        this.assetService = assetService;
        this.mailService = mailService;
    }

    @Override
    public void initChannel(SocketChannel ch) {
        ChannelPipeline pipeline = ch.pipeline();
        SslHandler sslHandler = null;
        if (sslHandlerProvider != null) {
            sslHandler = sslHandlerProvider.getSslHandler();
            pipeline.addLast(sslHandler);
        }
        pipeline.addLast("decoder", new MqttDecoder(MAX_PAYLOAD_SIZE));
        pipeline.addLast("encoder", MqttEncoder.INSTANCE);

        MqttTransportHandler handler = new MqttTransportHandler(processor, deviceService, authService, relationService,
                adaptor, sslHandler, quotaService, attributesService, assetService, mailService);

        pipeline.addLast(handler);
        ch.closeFuture().addListener(handler);
    }

}
