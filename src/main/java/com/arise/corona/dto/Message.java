package com.arise.corona.dto;

import com.arise.core.tools.MapUtil;

import java.util.Map;

import static com.arise.core.tools.StringUtil.jsonVal;

public class Message {
    private String id;
    private String text;
    private String senderId;
    private String receiverId;
    private String conversationId;
    private Type type = Type.TEXT;


    public static Message fromMap(Map<String, Object> obj) {
        Message message = new Message();
        message.setType(Type.search(MapUtil.getString(obj, "type")));
        message.setId(MapUtil.getString(obj, "id"));
        message.setText(MapUtil.getString(obj, "text"));
        message.setSenderId(MapUtil.getString(obj, "senderId"));
        message.setReceiverId(MapUtil.getString(obj, "receiverId"));
        message.setConversationId(MapUtil.getString(obj, "conversationId"));
        return message;
    }

    public String getConversationId() {
        return conversationId;
    }

    public Message setConversationId(String conversationId) {
        this.conversationId = sanitize(conversationId);
        return this;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public Message setReceiverId(String receiverId) {
        this.receiverId = sanitize(receiverId);
        return this;
    }

    public Message setType(Type type) {
        this.type = type;
        return this;
    }

    public Message setSenderId(String sender) {
        this.senderId = sanitize(sender);
        return this;
    }

    public String getSenderId() {
        return senderId;
    }

    public Type getType() {
        return type;
    }

    public String getId() {
        return id;
    }

    public Message setId(String id) {
        this.id = sanitize(id);
        return this;
    }

    public String getText() {
        return text;
    }

    public Message setText(String text) {
        this.text = text;
        return this;
    }

    public static String sanitize(String s){
        System.out.println(s);
       return  ("" + s).replaceAll("\\s+","")
               .replaceAll("http:", "L")
               .replaceAll("https:", "U")
               .replaceAll(":", "Q")
               .replaceAll("=", "v")
               .replaceAll("\\?", "z")
               .replaceAll("\\.", "d")
               .replaceAll("\\+", "5")
               .replaceAll("/", "")
               .replaceAll("-", "9")
               .replaceAll("inux", "Xx")
               .replaceAll("samsung", "sG")
               .replaceAll("aarch", "yH")
               .replaceAll("\\\\", "")
               .replaceAll("//", "g")
               .replaceAll("storage", "y")
               .replaceAll("ovies", "W")
               .replaceAll("usic", "R")
               .replaceAll("/", "7")
               .replaceAll("mate", "M3")
               .replaceAll("generic", "89")
               .replaceAll("alex", "SAP")
               ;
    }



    public String toJson() {
        return "{" +
                "\"id\":" + jsonVal(id) +
                ",\"text\":" + jsonVal(text)  +
                ",\"senderId\":" + jsonVal(senderId)  +
                ",\"receiverId\":" + jsonVal(receiverId)  +
                ",\"conversationId\":" + jsonVal(conversationId)  +
                ",\"type\":" + jsonVal(type.name())  +
                "}";
    }

    public enum Type {
        TEXT, WALL_TEXT;

        public static Type search(String text){
            for (Type t: values()){
                if (t.name().equalsIgnoreCase(text)){
                    return t;
                }
            }
            return TEXT;
        }
    }
}
