package com.example.proyectoandroid.util;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/** Con edge-to-edge la app dibuja bajo las barras del sistema: este helper deja el contenido dentro del area visible. */
public final class WindowInsetsHelper {

    private WindowInsetsHelper() {
    }

    /** Aplica como padding de {@code root} las barras del sistema y el teclado. */
    public static void applySystemBarPadding(@NonNull View root) {
        ViewCompat.setOnApplyWindowInsetsListener(root, (view, insets) -> {
            Insets visible = insets.getInsets(WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.ime());
            view.setPadding(visible.left, visible.top, visible.right, visible.bottom);
            return insets;
        });
    }
}
