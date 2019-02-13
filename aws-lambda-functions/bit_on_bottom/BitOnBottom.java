
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

package bit_on_bottom;


import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.KinesisEvent;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hashmapinc.tempus.InputParserUtility;
import com.hashmapinc.tempus.MqttConnector;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class BitOnBottom implements RequestHandler<KinesisEvent, Void> {

    private static final String MQTT_URL = "tcp://tempus.hashmapinc.com:1883";
    private static final String ACCESS_TOKEN = "DEVICE_GATEWAY_TOKEN";

    private class Data {
        private String id;
        private Double holeDepth;
        private Double ds;
    }

    @Override
    public Void handleRequest(KinesisEvent event, Context context) {
        final double maxDelta = 0.33;

            event.getRecords().stream().forEach(kinesisEventRecord -> {
                try {
                    byte[] bytes = kinesisEventRecord.getKinesis().getData().array();
                    String inputJson = new String(bytes);

                    if (new InputParserUtility().validateJson(inputJson, Collections.singletonList("holeDepth"))) {
                        Data inputData = new Gson().fromJson(inputJson, Data.class);

                        Map<String, String> data = new HashMap<>();
                        if ((inputData.holeDepth - inputData.ds) < (-0.3048)) {
                            data.put("BONB", "-1");
                            data.put("BONBTEXT", "Bit deeper than hole depth");
                        } else if ((inputData.holeDepth - inputData.ds) < maxDelta) {
                            data.put("BONB", "1");
                            data.put("BONBTEXT", "yes");
                        } else {
                            data.put("BONB", "0");
                            data.put("BONBTEXT", "no");
                        }

                        String json = new GsonBuilder().create().toJson(data);
                        Optional<Long> empty = Optional.empty();
                        new MqttConnector(MQTT_URL, ACCESS_TOKEN).publish(json, empty, Optional.of(inputData.ds), inputData.id);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        return null;
    }
}
