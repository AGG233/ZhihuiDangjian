package com.rauio.smartdangjian.utils.spec;

public enum SSEMsgType {

    MESSAGE("message","普通消息"),
    ADD("add","追加消息"),
    FINISH("finish","消息发送完成"),
    DONE("done","消息发送完成");

    public final String type;
    public final String value;
    SSEMsgType(String type, String value) {
        this.type = type;
        this.value = value;
    }
}
