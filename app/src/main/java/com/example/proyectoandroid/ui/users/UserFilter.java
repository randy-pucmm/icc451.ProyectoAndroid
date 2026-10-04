package com.example.proyectoandroid.ui.users;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.proyectoandroid.data.model.User;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Filtra usuarios por nombre o correo, sin distinguir mayusculas ni acentos. */
public final class UserFilter {

    private UserFilter() {
    }

    @NonNull
    public static List<User> filter(@NonNull List<User> users, @Nullable String query) {
        String needle = normalize(query);
        if (needle.isEmpty()) {
            return users;
        }
        List<User> result = new ArrayList<>();
        for (User user : users) {
            if (normalize(user.getDisplayName()).contains(needle) || normalize(user.getEmail()).contains(needle)) {
                result.add(user);
            }
        }
        return result;
    }

    /** "  José " -> "jose" */
    private static String normalize(@Nullable String text) {
        if (text == null) {
            return "";
        }
        String decomposed = Normalizer.normalize(text.trim(), Normalizer.Form.NFD);
        return decomposed.replaceAll("\\p{M}+", "").toLowerCase(Locale.ROOT);
    }
}
