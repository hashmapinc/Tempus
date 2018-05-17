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
import static org.junit.Assert.assertEquals;

import com.amazonaws.services.kinesis.AmazonKinesisClient;
import com.amazonaws.services.kinesis.model.PutRecordRequest;
import com.hashmapinc.server.extensions.kinesis.action.streams.KinesisStreamActionMsg;
import com.hashmapinc.server.extensions.kinesis.action.streams.KinesisStreamActionPayload;
import com.hashmapinc.server.extensions.kinesis.plugin.KinesisStreamPutRecordFactory;
import org.junit.Test;
import com.hashmapinc.server.common.data.id.CustomerId;
import com.hashmapinc.server.common.data.id.DeviceId;
import com.hashmapinc.server.common.data.id.TenantId;


import java.nio.ByteBuffer;

/**
 * @author Mitesh Rathore
 */
public class KinesisPutRecordRequestFactoryTest {


    private static final String ACCESS_KEY_ID = "test";
    private static final String SECRET_ACCESS_KEY = "test";
    private static final String REGION = "us-east-2";
    private static final String END_POINT_URL = "https://firehose.us-east-2.amazonaws.com";


    @Test
    public void shouldCreatePutRequestFactoryProperly() {
        //given
        TenantId tenantId = TestUtils.aTenantId();
        CustomerId customerId = TestUtils.aCustomerId();
        DeviceId deviceId = TestUtils.aDeviceId();
        KinesisStreamActionMsg msg = anActionMsg(tenantId, customerId, deviceId);


        //when
          PutRecordRequest actual = KinesisStreamPutRecordFactory.INSTANCE.create(msg);

        //then
        PutRecordRequest expected = new PutRecordRequest()
                .withData(ByteBuffer.wrap(msg.getPayload().getMsgBody().getBytes()))
                .withStreamName(msg.getPayload().getStream())
                .withPartitionKey(msg.getUid().toString());


        assertEquals(expected, actual);

    }

    private KinesisStreamActionMsg anActionMsg(TenantId tenantId, CustomerId customerId, DeviceId deviceId) {
        KinesisStreamActionPayload aPayload = KinesisStreamActionPayload.builder()
                .msgBody("{\"ticker_symbol\":\"local-test\", \"sector\":\"HEALTHCARE\", \"change\":-0.05, \"price\":284.51}")
              //  .stream("demo-hashmap-kinesis")
                .stream("demo-hashmap-kinesis")

                .build();

        return new KinesisStreamActionMsg(tenantId, customerId, deviceId, aPayload);
    }
}
