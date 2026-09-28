package com.example.proyectoandroid.data.model;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;
import java.util.List;

/** Documento chats/{chatId}. Lo escribe el modulo de Chat. */
public class Chat {

    private List<String> participants;
    private String lastMessage;
    @ServerTimestamp
    private Date lastMessageAt;

    // Firestore necesita un constructor publico sin argumentos.
    public Chat() {
    }

    /** Id determinista para una conversacion 1 a 1: los dos uid ordenados y unidos con "_". */
    public static String buildChatId(String uidA, String uidB) {
        return uidA.compareTo(uidB) <= 0 ? uidA + "_" + uidB : uidB + "_" + uidA;
    }

    public List<String> getParticipants() {
        return participants;
    }

    public void setParticipants(List<String> participants) {
        this.participants = participants;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public Date getLastMessageAt() {
        return lastMessageAt;
    }

    public void setLastMessageAt(Date lastMessageAt) {
        this.lastMessageAt = lastMessageAt;
    }
}
