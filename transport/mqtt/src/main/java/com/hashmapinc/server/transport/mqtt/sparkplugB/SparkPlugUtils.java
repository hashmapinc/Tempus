package com.hashmapinc.server.transport.mqtt.sparkplugB;

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
