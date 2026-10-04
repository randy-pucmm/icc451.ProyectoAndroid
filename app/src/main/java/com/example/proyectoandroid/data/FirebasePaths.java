package com.example.proyectoandroid.data;

/**
 * Nombres de colecciones y rutas compartidas entre Repositories.
 * users/{uid}
 * chats/{chatId}
 * chats/{chatId}/messages/{messageId}
 */
public final class FirebasePaths {

    public static final String USERS = "users";
    public static final String CHATS = "chats";
    public static final String MESSAGES = "messages";

    /** Carpeta de Storage para imagenes enviadas: chat_images/{uid del remitente}/{uuid}.jpg */
    public static final String STORAGE_CHAT_IMAGES = "chat_images";

    /** Carpeta de Storage para fotos de perfil: profile_photos/{uid}/avatar.jpg */
    public static final String STORAGE_PROFILE_PHOTOS = "profile_photos";

    private FirebasePaths() {
    }
}
