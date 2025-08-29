package com.arise.astox.net.clients;


@Deprecated
public class SSEEvent {
    private String event;
    private Long retry;
    private String data;
    private String comment;
    private String id;

    public String getComment() {
        return comment;
    }

    SSEEvent setComment(String comment) {
        this.comment = comment;
        return this;
    }

    public String getEvent() {
        return event;
    }

    SSEEvent setEvent(String event) {
        this.event = event;
        return this;
    }

    public Long getRetry() {
        return retry;
    }

    SSEEvent setRetry(Long retry) {
        this.retry = retry;
        return this;
    }


    public String getData() {
        return data;
    }

    SSEEvent setData(String data) {
        this.data = data;
        return this;
    }

    public String getId() {
        return id;
    }

    SSEEvent setId(String id) {
        this.id = id;
        return this;
    }

    @Override
    public String toString() {
        return "SSEEvent{" +
                "event='" + event + '\'' +
                ", retry=" + retry +
                ", comment='" + comment + '\'' +
                ", data='" + data + '\'' +
                ", id='" + id + '\'' +
                '}';
    }
}
