package com.example.proyectoandroid.util;

import android.graphics.Color;
import android.view.View;

import androidx.activity.ComponentActivity;
import androidx.activity.EdgeToEdge;
import androidx.activity.SystemBarStyle;
import androidx.annotation.NonNull;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/** Con edge-to-edge la app dibuja bajo las barras del sistema: estos helpers dejan el contenido dentro del area visible. */
public final class WindowInsetsHelper {

    private WindowInsetsHelper() {
    }

    /** Pantallas sin barra superior (Login, Registro): padding en los cuatro lados, incluido el teclado. */
    public static void applySystemBarPadding(@NonNull View root) {
        ViewCompat.setOnApplyWindowInsetsListener(root, (view, insets) -> {
            Insets visible = insets.getInsets(WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.ime());
            view.setPadding(visible.left, visible.top, visible.right, visible.bottom);
            return insets;
        });
    }

    /**
     * Pantallas con barra superior de color: el padding de {@code root} deja libre el borde superior
     * (lo cubre {@link #applyStatusBarPadding} en la barra) y respeta los lados, la navegacion y el teclado.
     */
    public static void applyContentPadding(@NonNull View root) {
        ViewCompat.setOnApplyWindowInsetsListener(root, (view, insets) -> {
            Insets visible = insets.getInsets(WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.ime());
            view.setPadding(visible.left, 0, visible.right, visible.bottom);
            return insets;
        });
    }

    /** Agrega a {@code appBar} el alto de la barra de estado como padding superior, para que su color la cubra. */
    public static void applyStatusBarPadding(@NonNull View appBar) {
        ViewCompat.setOnApplyWindowInsetsListener(appBar, (view, insets) -> {
            Insets statusBar = insets.getInsets(WindowInsetsCompat.Type.statusBars());
            view.setPadding(view.getPaddingLeft(), statusBar.top, view.getPaddingRight(), view.getPaddingBottom());
            return insets;
        });
    }

    /** Edge-to-edge con iconos claros en la barra de estado, porque debajo hay una barra superior oscura o verde. */
    public static void enableEdgeToEdgeWithAppBar(@NonNull ComponentActivity activity) {
        EdgeToEdge.enable(activity,
                SystemBarStyle.dark(Color.TRANSPARENT),
                SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT));
    }
}
