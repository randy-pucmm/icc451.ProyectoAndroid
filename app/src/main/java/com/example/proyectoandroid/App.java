package com.example.proyectoandroid;

import android.app.Application;

import com.example.proyectoandroid.data.repository.UserRepository;
import com.example.proyectoandroid.util.PresenceTracker;

/** Application: registra el seguimiento de presencia (en linea / desconectado) para toda la app. */
public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        registerActivityLifecycleCallbacks(new PresenceTracker(new UserRepository()));
    }
}
