package com.example.proyectoandroid.data.model;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

/** Documento users/{uid}. Lo escribe el modulo de Auth/Usuarios. */
public class User {

    public static final String FIELD_FCM_TOKEN = "fcmToken";
    public static final String FIELD_PHOTO_URL = "photoUrl";
    public static final String FIELD_ONLINE = "online";
    public static final String FIELD_LAST_SEEN = "lastSeen";

    private String uid;
    private String displayName;
    private String email;
    private String fcmToken;
    @ServerTimestamp
    private Date createdAt;
    /** URL https o data URI de la foto de perfil; null si no tiene. */
    private String photoUrl;
    /** true mientras la app esta en primer plano; ver util/Presence para interpretarlo. */
    private boolean online;
    /** Ultima senal de actividad (la escribe el servidor). */
    private Date lastSeen;

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

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    public Date getLastSeen() {
        return lastSeen;
    }

    public void setLastSeen(Date lastSeen) {
        this.lastSeen = lastSeen;
    }
}
