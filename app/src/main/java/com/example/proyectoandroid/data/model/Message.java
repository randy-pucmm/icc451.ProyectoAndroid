package com.example.proyectoandroid.data.model;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

/** Documento chats/{chatId}/messages/{messageId}. Lo escribe el modulo de Chat. */
public class Message {

    public static final String TYPE_TEXT = "TEXT";
    public static final String TYPE_IMAGE = "IMAGE";

    private String senderId;
    private String senderName;
    private String text;
    private String type;
    private String imageUrl;
    @ServerTimestamp
    private Date timestamp;

    // Firestore necesita un constructor publico sin argumentos.
    public Message() {
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}
