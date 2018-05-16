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

import com.amazonaws.services.kinesis.model.PutRecordRequest;
import com.hashmapinc.server.extensions.kinesis.action.KinesisActionMsg;
import com.hashmapinc.server.extensions.kinesis.action.KinesisActionPayload;
import java.nio.ByteBuffer;

/**
 * @author Mitesh Rathore
 */
public class KinesisPutRecordRequestFactory {

    public static final KinesisPutRecordRequestFactory INSTANCE = new KinesisPutRecordRequestFactory();

    private KinesisPutRecordRequestFactory() {}

    public PutRecordRequest create(KinesisActionMsg msg) {
        KinesisActionPayload payload = msg.getPayload();

        return new PutRecordRequest()
                .withData(ByteBuffer.wrap(payload.getMsgBody().getBytes()))
                .withStreamName(payload.getStream())
                .withPartitionKey(msg.getUid().toString());
    }

}
