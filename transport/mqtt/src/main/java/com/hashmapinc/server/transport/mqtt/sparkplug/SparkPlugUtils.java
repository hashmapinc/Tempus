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
package com.hashmapinc.server.transport.mqtt.sparkplug;

public class SparkPlugUtils {

    public String extractTopicWithoutMsgType(String topicName){
        String[] splitTopic = topicName.split("/");
        StringBuilder topic = new StringBuilder();
        String delimiter = "";
        for (String str: splitTopic) {
            if(!sparkPlugMsgTypes(str)) {
                topic.append(delimiter);
                topic.append(str);
                delimiter = "/";
            }
        }
        return topic.toString();
    }

    protected boolean sparkPlugMsgTypes(String type){
        switch (type){
            case SparkPlugMsgTypes.DBIRTH : return true;
            case SparkPlugMsgTypes.DDEATH : return true;
            case SparkPlugMsgTypes.DDATA : return true;
            case SparkPlugMsgTypes.NBIRTH : return true;
            case SparkPlugMsgTypes.NDATA : return true;
            case SparkPlugMsgTypes.NDEATH : return true;
            case SparkPlugMsgTypes.NCMD : return true;
            case SparkPlugMsgTypes.DCMD : return true;
            default: return false;
        }
    }

    public String extractDeviceName(String topicName){
        String[] splitTopic = topicName.split("/");
        return splitTopic[splitTopic.length - 1];
    }

    public String extractMsgType(String topicName){
        String[] splitTopic = topicName.split("/");
        return splitTopic[2];
    }
}
