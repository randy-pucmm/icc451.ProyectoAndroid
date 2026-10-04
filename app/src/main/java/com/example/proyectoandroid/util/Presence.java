package com.example.proyectoandroid.util;

import androidx.annotation.NonNull;

import com.example.proyectoandroid.data.model.User;

/**
 * Interpreta los campos de presencia de un usuario.
 *
 * <p>Firestore no tiene un "onDisconnect" como Realtime Database: si la app muere sin avisar,
 * {@code online} queda en true. Por eso mientras la app esta abierta se renueva {@code lastSeen}
 * cada minuto (ver {@link PresenceTracker}) y aqui se considera en linea solo a quien tenga
 * {@code online} y una senal reciente.
 */
public final class Presence {

    /** Dos latidos perdidos (de 60 s) y se da al usuario por desconectado. */
    public static final long ONLINE_TIMEOUT_MS = 2 * 60 * 1000L;

    private Presence() {
    }

    public static boolean isOnline(@NonNull User user, long nowMillis) {
        return user.isOnline()
                && user.getLastSeen() != null
                && nowMillis - user.getLastSeen().getTime() <= ONLINE_TIMEOUT_MS;
    }
}
