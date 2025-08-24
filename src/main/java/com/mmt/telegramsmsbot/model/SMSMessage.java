package com.mmt.telegramsmsbot.model;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SMSMessage {
    private String body;
    private String sender;
    private long timestamp;
    
    public SMSMessage(String body, String sender, long timestamp) {
        this.body = body;
        this.sender = sender;
        this.timestamp = timestamp;
    }
    
    public String getBody() {
        return body;
    }
    
    public String getSender() {
        return sender;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public String getFormattedDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }
}
