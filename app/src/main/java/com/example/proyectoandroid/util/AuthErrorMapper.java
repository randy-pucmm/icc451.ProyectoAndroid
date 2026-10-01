package com.example.proyectoandroid.util;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import com.example.proyectoandroid.R;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;

/**
 * Traduce las excepciones de Firebase Authentication a mensajes comprensibles para el usuario.
 * Devuelve el id del string para que la clase siga siendo Java puro y se pueda probar sin Android.
 */
public final class AuthErrorMapper {

    private static final String CODE_INVALID_EMAIL = "ERROR_INVALID_EMAIL";
    private static final String CODE_USER_DISABLED = "ERROR_USER_DISABLED";

    private AuthErrorMapper() {
    }

    @StringRes
    public static int messageRes(@Nullable Throwable error) {
        // FirebaseAuthWeakPasswordException hereda de FirebaseAuthInvalidCredentialsException:
        // debe evaluarse primero.
        if (error instanceof FirebaseAuthWeakPasswordException) {
            return R.string.error_auth_weak_password;
        }
        if (error instanceof FirebaseAuthUserCollisionException) {
            return R.string.error_auth_email_in_use;
        }
        if (error instanceof FirebaseAuthInvalidCredentialsException) {
            return CODE_INVALID_EMAIL.equals(codeOf(error))
                    ? R.string.error_email_invalid
                    : R.string.error_auth_invalid_credentials;
        }
        if (error instanceof FirebaseAuthInvalidUserException) {
            return CODE_USER_DISABLED.equals(codeOf(error))
                    ? R.string.error_auth_user_disabled
                    : R.string.error_auth_invalid_credentials;
        }
        if (error instanceof FirebaseTooManyRequestsException) {
            return R.string.error_auth_too_many_requests;
        }
        if (error instanceof FirebaseNetworkException) {
            return R.string.error_auth_network;
        }
        return R.string.error_auth_unknown;
    }

    @Nullable
    private static String codeOf(Throwable error) {
        return error instanceof FirebaseAuthException ? ((FirebaseAuthException) error).getErrorCode() : null;
    }
}
