package com.hashmapinc.server.transport.mqtt.sparkplugB;

public class SparkPlugMetaData {
    private String topicName;
    private int seq;

    public SparkPlugMetaData(String topicName, int seq){
        this.topicName = topicName;
        this.seq = seq;
    }

    public String getTopicName() {
        return topicName;
    }

    public void setTopicName(String topicName) {
        this.topicName = topicName;
    }

    public int getSeq() {
        return seq;
    }

    public void setSeq(int seq) {
        this.seq = seq;
    }
}
