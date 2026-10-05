package com.example.proyectoandroid.util;

import androidx.annotation.Nullable;

/**
 * Recuerda con quien es la conversacion que esta en pantalla. Si llega una notificacion push de esa
 * persona no tiene sentido mostrarla: los mensajes ya se ven en el chat.
 */
public final class ActiveChat {

    private static volatile String openUid;

    private ActiveChat() {
    }

    public static void open(@Nullable String otherUid) {
        openUid = otherUid;
    }

    public static void close() {
        openUid = null;
    }

    public static boolean isOpen(@Nullable String otherUid) {
        return otherUid != null && otherUid.equals(openUid);
    }
}
