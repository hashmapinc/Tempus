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
package com.hashmapinc.server.extensions.kinesis.action.firehose;

import com.hashmapinc.server.common.msg.session.MsgType;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Mitesh Rathore
 */

@Data
@Builder
public class KinesisFirehoseActionPayload implements Serializable{


    private final String stream;
    private final String msgBody;
    private final boolean sync;
    private final Integer requestId;
    private final MsgType msgType;

}
