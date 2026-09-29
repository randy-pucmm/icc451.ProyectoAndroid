package com.example.proyectoandroid.util;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import com.example.proyectoandroid.R;

import java.util.regex.Pattern;

/**
 * Validaciones de los formularios de Login y Registro.
 * Cada metodo devuelve el id del mensaje de error, o {@link #VALID} si el valor es correcto.
 * Es Java puro (sin android.util.Patterns) para poder probarlo con tests unitarios de JVM.
 */
public final class Validator {

    public static final int VALID = 0;
    public static final int MIN_NAME_LENGTH = 2;
    /** Firebase Authentication exige como minimo 6 caracteres. */
    public static final int MIN_PASSWORD_LENGTH = 6;

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9._%+\\-]+@[A-Za-z0-9\\-]+(\\.[A-Za-z0-9\\-]+)*\\.[A-Za-z]{2,}$");

    private Validator() {
    }

    @StringRes
    public static int validateName(@Nullable String name) {
        String value = trim(name);
        if (value.isEmpty()) {
            return R.string.error_name_required;
        }
        if (value.length() < MIN_NAME_LENGTH) {
            return R.string.error_name_short;
        }
        return VALID;
    }

    @StringRes
    public static int validateEmail(@Nullable String email) {
        String value = trim(email);
        if (value.isEmpty()) {
            return R.string.error_email_required;
        }
        if (!EMAIL_PATTERN.matcher(value).matches()) {
            return R.string.error_email_invalid;
        }
        return VALID;
    }

    /** Al iniciar sesion solo se exige que la contrasena no este vacia. */
    @StringRes
    public static int validatePasswordRequired(@Nullable String password) {
        if (password == null || password.isEmpty()) {
            return R.string.error_password_required;
        }
        return VALID;
    }

    /** Al registrarse ademas se exige la longitud minima. */
    @StringRes
    public static int validateNewPassword(@Nullable String password) {
        int required = validatePasswordRequired(password);
        if (required != VALID) {
            return required;
        }
        if (password.length() < MIN_PASSWORD_LENGTH) {
            return R.string.error_password_short;
        }
        return VALID;
    }

    @StringRes
    public static int validatePasswordConfirmation(@Nullable String password, @Nullable String confirmation) {
        if (confirmation == null || confirmation.isEmpty()) {
            return R.string.error_confirm_required;
        }
        if (!confirmation.equals(password)) {
            return R.string.error_password_mismatch;
        }
        return VALID;
    }

    private static String trim(@Nullable String value) {
        return value == null ? "" : value.trim();
    }
}
