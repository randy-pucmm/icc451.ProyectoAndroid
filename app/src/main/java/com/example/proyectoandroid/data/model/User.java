package com.example.proyectoandroid.data.model;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

/** Documento users/{uid}. Lo escribe el modulo de Auth/Usuarios. */
public class User {

    private String uid;
    private String displayName;
    private String email;
    private String fcmToken;
    @ServerTimestamp
    private Date createdAt;

    // Firestore necesita un constructor publico sin argumentos.
    public User() {
    }

    public User(String uid, String displayName, String email) {
        this.uid = uid;
        this.displayName = displayName;
        this.email = email;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFcmToken() {
        return fcmToken;
    }

    public void setFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
}
