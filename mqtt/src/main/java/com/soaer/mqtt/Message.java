package com.soaer.mqtt;

/**
 * Created by Sunny on 2018/10/24.
 */
public class Message {
    private String topic;

    private int qos;

    private byte[] payload;

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public int getQos() {
        return qos;
    }

    public void setQos(int qos) {
        this.qos = qos;
    }

    public byte[] getPayload() {
        return payload;
    }

    public void setPayload(byte[] payload) {
        this.payload = payload;
    }
}
