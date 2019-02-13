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
package ecd;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.KinesisEvent;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hashmapinc.tempus.InputParserUtility;
import com.hashmapinc.tempus.MqttConnector;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


/*
 * Equivalent Circulating Density (ECD) in N/m3 = (annular pressure loss in N/m2) / (true vertical depth in m)
 */
public class EquivalentCirculatingDensityCalculator implements RequestHandler<KinesisEvent, Void> {

    private static final String MQTT_URL = "tcp://tempus.hashmapinc.com:1883";
    private static final String ACCESS_TOKEN = "DEVICE_GATEWAY_TOKEN";


    private class Data {
        private String id;
        private Double annularPressureLoss;
        private Double ds;
    }

    @Override
    public Void handleRequest(KinesisEvent event, Context context) {
        event.getRecords().stream().forEach(kinesisEventRecord -> {
        try{
            byte[] bytes = kinesisEventRecord.getKinesis().getData().array();
            String inputJson = new String(bytes);

            if(new InputParserUtility().validateJson(inputJson, Arrays.asList("annularPressureLoss"))) {
                Data inputData = new Gson().fromJson(inputJson, Data.class);

                double ecd = inputData.annularPressureLoss / inputData.ds;

                Map<String, Double> data = new HashMap<>();
                data.put("ECD", ecd);

                String json = new GsonBuilder().create().toJson(data);
                Optional<Long> empty = Optional.empty();
                new MqttConnector(MQTT_URL, ACCESS_TOKEN).publish(json, empty, Optional.of(inputData.ds), inputData.id);
            }
        }catch (Exception e) {
            e.printStackTrace();
        }});
         return null;
    }

}
