package com.arise.weland.dto;


import java.util.Map;

import static com.arise.core.tools.StringUtil.jsonVal;
import static com.arise.core.tools.StringUtil.sanitizeAppId;
import static com.arise.weland.dto.ContentInfo.decodeString;
import static com.arise.weland.dto.ContentInfo.encodePath;

public class Message {
    private String id;
    private String text;
    private String senderId;
    private String receiverId;
    private String conversationId;


    public Message wallMessage(){
        this.conversationId = "";
        this.receiverId = "";
        return this;
    }


    public String getConversationId() {
        return conversationId;
    }

    public Message setConversationId(String conversationId) {
        this.conversationId = sanitizeAppId(conversationId);
        return this;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public Message setReceiverId(String receiverId) {
        this.receiverId = sanitizeAppId(receiverId);
        return this;
    }


    public Message setSenderId(String sender) {
        this.senderId = sanitizeAppId(sender);
        return this;
    }

    public String getSenderId() {
        return senderId;
    }

    public String getId() {
        return id;
    }

    public Message setId(String id) {
        this.id = sanitizeAppId(id);
        return this;
    }

    public String getText() {
        return text;
    }

    public Message setText(String text) {
        this.text = text;
        return this;
    }



    public static Message fromMap(Map<String, Object> obj) {
        Message message = new Message();
        message.setId(decodeString(obj, "I"));
        message.setText(decodeString(obj, "T"));
        message.setSenderId(decodeString(obj, "S"));
        message.setReceiverId(decodeString(obj, "R"));
        message.setConversationId(decodeString(obj, "C"));
        return message;
    }

    public String toJson() {
        return "{" +
                "\"I\":" + jsonVal(encodePath(id)) +
                ",\"T\":" + jsonVal(encodePath(text))  +
                ",\"S\":" + jsonVal(encodePath(senderId))  +
                ",\"R\":" + jsonVal(encodePath(receiverId))  +
                ",\"C\":" + jsonVal(encodePath(conversationId))  +
                "}";
    }





}
