package com.arise.rapdroid;

import android.app.Notification;

import java.util.HashMap;
import java.util.Map;

public class NotificationOps {
    String title;
    String text;
    String channelId;
    Map<String, String> extra = new HashMap<>();
    int smallIcon;
    int id;

    String channelDescription;


    public NotificationOps setChannelDescription(String channelDescription) {
        this.channelDescription = channelDescription;
        return this;
    }

    int flags = Notification.FLAG_AUTO_CANCEL;

    public NotificationOps setId(int id) {
        this.id = id;
        return this;
    }

    public NotificationOps setTitle(String title) {
        this.title = title;
        return this;
    }

    public NotificationOps setText(String text) {
        this.text = text;
        return this;
    }

    public NotificationOps setChannelId(String channelId) {
        this.channelId = channelId;
        return this;
    }

    public NotificationOps addExtra(String key, String value) {
        this.extra.put(key, value);
        return this;
    }

    public NotificationOps setUncloseable() {
        this.flags = Notification.FLAG_ONGOING_EVENT;
        return this;
    }

    public NotificationOps setSmallIcon(int smallIcon) {
        this.smallIcon = smallIcon;
        return this;
    }
}
