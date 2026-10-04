package com.example.proyectoandroid.ui.users;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;

import java.util.Locale;

/** Pinta el avatar de un usuario: su foto si tiene, y si no un circulo con la inicial del nombre. */
final class AvatarBinder {

    private AvatarBinder() {
    }

    static void bind(@NonNull TextView initialView, @NonNull ImageView photoView,
                     @Nullable String name, @Nullable String photoUrl) {
        String trimmed = name == null ? "" : name.trim();
        initialView.setText(trimmed.isEmpty() ? "?" : trimmed.substring(0, 1).toUpperCase(Locale.getDefault()));

        if (photoUrl == null || photoUrl.isEmpty()) {
            // Las filas se reciclan: hay que cancelar la carga anterior para no mostrar la foto de otro usuario.
            Glide.with(photoView).clear(photoView);
            photoView.setVisibility(View.GONE);
            return;
        }
        photoView.setVisibility(View.VISIBLE);
        Glide.with(photoView).load(photoUrl).circleCrop().into(photoView);
    }
}
