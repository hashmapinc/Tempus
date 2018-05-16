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
package com.hashmapinc.server.extensions.kinesis;

import com.amazonaws.services.kinesis.model.PutRecordRequest;
import com.amazonaws.services.kinesis.model.PutRecordResult;
import com.amazonaws.services.kinesisfirehose.AmazonKinesisFirehoseAsync;
import com.hashmapinc.server.extensions.api.plugins.msg.ResponsePluginToRuleMsg;
import com.hashmapinc.server.extensions.api.rules.RuleException;
import com.hashmapinc.server.extensions.kinesis.action.streams.KinesisStreamActionMsg;
import com.hashmapinc.server.extensions.kinesis.action.streams.KinesisStreamActionPayload;
import com.hashmapinc.server.extensions.kinesis.plugin.KinesisMessageHandler;
import com.hashmapinc.server.extensions.kinesis.plugin.KinesisStreamPutRecordFactory;
import org.junit.Test;
import org.mockito.Mockito;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.amazonaws.services.kinesis.AmazonKinesisAsync;

import com.hashmapinc.server.common.data.id.CustomerId;
import com.hashmapinc.server.common.data.id.DeviceId;
import com.hashmapinc.server.common.data.id.RuleId;
import com.hashmapinc.server.common.data.id.TenantId;
import com.hashmapinc.server.extensions.api.plugins.PluginContext;
import com.hashmapinc.server.extensions.api.plugins.msg.RuleToPluginMsg;


import java.io.Serializable;
import java.util.UUID;
import java.util.concurrent.Future;

import com.amazonaws.handlers.AsyncHandler;

/**
 * @author Mitesh Rathore
 */
public class KinesisStreamHandlerTest {



    private final PluginContext pluginContextMock = Mockito.mock(PluginContext.class);
    private final AmazonKinesisAsync kinesisStreamAsync = Mockito.mock(AmazonKinesisAsync.class);
    private final AmazonKinesisFirehoseAsync kinesisFirehoseAsync = Mockito.mock(AmazonKinesisFirehoseAsync.class);
    private final KinesisMessageHandler msgHandler = new KinesisMessageHandler(kinesisStreamAsync,kinesisFirehoseAsync);

    @Test
    public void shouldFailWhenMessageFormatUnsupported() {
        //given
        TenantId tenantId = TestUtils.aTenantId();
        RuleId ruleId = TestUtils.aRuleId();

        //when
        try {
            msgHandler.process(pluginContextMock, tenantId, ruleId, aRuleToPluginMsg());
            fail();
        } catch (RuleException e) {
            // then exception expected
        }
    }

    @Test
    public void shouldSendSuccesfullyWhenFormatCorrect() {
        //given
        TenantId tenantId = TestUtils.aTenantId();
        RuleId ruleId = TestUtils.aRuleId();
        DeviceId deviceId = TestUtils.aDeviceId();
        CustomerId customerId = TestUtils.aCustomerId();
        boolean sync = false;
        KinesisStreamActionMsg msg = anActionMsg(tenantId, customerId, deviceId, sync);

        //when
        try {
            msgHandler.process(pluginContextMock, tenantId, ruleId, msg);
        } catch (RuleException e) {
            fail(e.getMessage());
        }


        //then
        verify(kinesisStreamAsync).putRecordAsync(eq(KinesisStreamPutRecordFactory.INSTANCE.create(msg)), any(AsyncHandler.class));
    }

    @Test
    public void shouldSendSuccesfullyAndReplyItToPluginContext() {
        //given
        TenantId tenantId = TestUtils.aTenantId();
        RuleId ruleId = TestUtils.aRuleId();
        DeviceId deviceId = TestUtils.aDeviceId();
        CustomerId customerId = TestUtils.aCustomerId();
        boolean sync = true;
        KinesisStreamActionMsg msg = anActionMsg(tenantId, customerId, deviceId, sync);
        PutRecordRequest putRecordRequest = KinesisStreamPutRecordFactory.INSTANCE.create(msg);

        setUpMockToAnswerKinesisAsSuccess(putRecordRequest);

        //when
        try {
            msgHandler.process(pluginContextMock, tenantId, ruleId, msg);
        } catch (RuleException e) {
            fail(e.getMessage());
        }

        //then
        verify(pluginContextMock).reply(any(ResponsePluginToRuleMsg.class));
    }

    @Test
    public void shouldFailAndReplyItToPluginContext() {
        //given
        TenantId tenantId = TestUtils.aTenantId();
        RuleId ruleId = TestUtils.aRuleId();
        DeviceId deviceId = TestUtils.aDeviceId();
        CustomerId customerId = TestUtils.aCustomerId();
        boolean sync = true;
        KinesisStreamActionMsg msg = anActionMsg(tenantId, customerId, deviceId, sync);
        PutRecordRequest putRecordRequest = KinesisStreamPutRecordFactory.INSTANCE.create(msg);

      //  setUpMockToAnswerKinesisAsFailed(putRecordRequest);

        //when
        try {
            msgHandler.process(pluginContextMock, tenantId, ruleId, msg);
        } catch (RuleException e) {
            fail(e.getMessage());
        }

        //then
     //   verify(pluginContextMock).reply(any(ResponsePluginToRuleMsg.class));
    }

    private void setUpMockToAnswerKinesisAsSuccess(PutRecordRequest putRecordRequest) {
        when(kinesisStreamAsync.putRecordAsync(eq(putRecordRequest), any(AsyncHandler.class)))
                .thenAnswer(invocationOnMock -> {
                    final AsyncHandler asyncHandler = (AsyncHandler) (invocationOnMock.getArguments())[1];
                    asyncHandler.onSuccess(putRecordRequest, new PutRecordResult());
                    return Mockito.mock(Future.class);
                });
    }

    private void setUpMockToAnswerKinesisAsFailed(PutRecordRequest putRecordRequest) {
        when(kinesisStreamAsync.putRecordAsync(eq(putRecordRequest), any(AsyncHandler.class)))
                .thenAnswer(invocationOnMock -> {
                    final AsyncHandler asyncHandler = (AsyncHandler) (invocationOnMock.getArguments())[1];
                    asyncHandler.onError(new Exception());
                    return Mockito.mock(Future.class);
                });
    }

    private KinesisStreamActionMsg anActionMsg(TenantId tenantId, CustomerId customerId, DeviceId deviceId, boolean sync) {
        KinesisStreamActionPayload aPayload = KinesisStreamActionPayload.builder()
                .msgBody("{\"ticker_symbol\":\"local-test\", \"sector\":\"HEALTHCARE\", \"change\":-0.05, \"price\":284.51}")
                .sync(sync)
                .stream("demo-hashmap-kinesis")
                .requestId(1)
                .build();

        return new KinesisStreamActionMsg(tenantId, customerId, deviceId, aPayload);
    }


    private RuleToPluginMsg aRuleToPluginMsg() {
        return new RuleToPluginMsg() {
            @Override
            public UUID getUid() {
                return null;
            }

            @Override
            public DeviceId getDeviceId() {
                return null;
            }

            @Override
            public CustomerId getCustomerId() {
                return null;
            }

            @Override
            public Serializable getPayload() {
                return null;
            }
        };
    }

}
