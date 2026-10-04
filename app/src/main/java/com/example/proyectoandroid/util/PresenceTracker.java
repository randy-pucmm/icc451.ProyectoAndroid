package com.example.proyectoandroid.util;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import com.example.proyectoandroid.data.repository.UserRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * Marca al usuario como en linea mientras la app tiene alguna pantalla visible.
 * Cuenta las activities iniciadas: pasar de 0 a 1 es "app en primer plano" y de 1 a 0, "en segundo plano".
 */
public class PresenceTracker implements Application.ActivityLifecycleCallbacks {

    /** Intervalo con el que se renueva lastSeen mientras la app esta abierta. */
    private static final long HEARTBEAT_MS = 60_000;
    /** Al rotar la pantalla la activity se detiene y reinicia: este margen evita marcar "desconectado" en falso. */
    private static final long OFFLINE_DELAY_MS = 2_000;

    private final UserRepository repository;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private int startedActivities;
    private boolean heartbeatRunning;

    private final Runnable heartbeat = new Runnable() {
        @Override
        public void run() {
            sendPresence(true);
            handler.postDelayed(this, HEARTBEAT_MS);
        }
    };

    private final Runnable goOffline = () -> {
        heartbeatRunning = false;
        handler.removeCallbacks(heartbeat);
        sendPresence(false);
    };

    public PresenceTracker(@NonNull UserRepository repository) {
        this.repository = repository;
    }

    private void sendPresence(boolean online) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            repository.setPresence(user.getUid(), online);
        }
    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {
        startedActivities++;
        handler.removeCallbacks(goOffline);
        if (!heartbeatRunning) {
            heartbeatRunning = true;
            handler.post(heartbeat);
        }
    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {
        startedActivities--;
        if (startedActivities == 0) {
            handler.postDelayed(goOffline, OFFLINE_DELAY_MS);
        }
    }

    @Override
    public void onActivityCreated(@NonNull Activity activity, Bundle savedInstanceState) {
    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {
    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {
    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {
    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {
    }
}
