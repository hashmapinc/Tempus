package com.hashmapinc.server.transport.mqtt.sparkplugB.data;

public class SparkPlugMetaData {
    private String msgType;
    private int seq;

    public SparkPlugMetaData(String msgType, int seq){
        this.msgType = msgType;
        this.seq = seq;
    }

    public String getMsgType() {
        return msgType;
    }

    public void setMsgType(String msgType) {
        this.msgType = msgType;
    }

    public int getSeq() {
        return seq;
    }

    public void setSeq(int seq) {
        this.seq = seq;
    }
}
