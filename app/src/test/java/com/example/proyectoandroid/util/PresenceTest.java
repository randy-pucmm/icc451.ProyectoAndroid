package com.example.proyectoandroid.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.example.proyectoandroid.data.model.User;

import org.junit.Test;

import java.util.Date;

public class PresenceTest {

    private static final long NOW = 1_000_000_000L;

    private static User user(boolean online, Long lastSeenMillis) {
        User user = new User("1", "Ana", "ana@correo.com");
        user.setOnline(online);
        user.setLastSeen(lastSeenMillis == null ? null : new Date(lastSeenMillis));
        return user;
    }

    @Test
    public void conectadoConSenalReciente_estaEnLinea() {
        assertTrue(Presence.isOnline(user(true, NOW - 30_000), NOW));
        assertTrue(Presence.isOnline(user(true, NOW - Presence.ONLINE_TIMEOUT_MS), NOW));
    }

    @Test
    public void conectadoPeroSinSenalReciente_seDaPorDesconectado() {
        assertFalse(Presence.isOnline(user(true, NOW - Presence.ONLINE_TIMEOUT_MS - 1), NOW));
    }

    @Test
    public void marcadoDesconectado_noEstaEnLinea() {
        assertFalse(Presence.isOnline(user(false, NOW), NOW));
    }

    @Test
    public void sinLastSeen_noEstaEnLinea() {
        assertFalse(Presence.isOnline(user(true, null), NOW));
    }
}
