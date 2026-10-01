package com.example.proyectoandroid.util;

import static org.junit.Assert.assertEquals;

import com.example.proyectoandroid.R;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;

import org.junit.Test;

public class AuthErrorMapperTest {

    @Test
    public void credencialesInvalidas_muestraCorreoOContrasenaIncorrectos() {
        Throwable error = new FirebaseAuthInvalidCredentialsException("ERROR_INVALID_CREDENTIAL", "x");
        assertEquals(R.string.error_auth_invalid_credentials, AuthErrorMapper.messageRes(error));
    }

    @Test
    public void correoMalFormado_muestraCorreoInvalido() {
        Throwable error = new FirebaseAuthInvalidCredentialsException("ERROR_INVALID_EMAIL", "x");
        assertEquals(R.string.error_email_invalid, AuthErrorMapper.messageRes(error));
    }

    @Test
    public void usuarioInexistente_noRevelaSiLaCuentaExiste() {
        Throwable error = new FirebaseAuthInvalidUserException("ERROR_USER_NOT_FOUND", "x");
        assertEquals(R.string.error_auth_invalid_credentials, AuthErrorMapper.messageRes(error));
    }

    @Test
    public void usuarioDeshabilitado_tieneMensajePropio() {
        Throwable error = new FirebaseAuthInvalidUserException("ERROR_USER_DISABLED", "x");
        assertEquals(R.string.error_auth_user_disabled, AuthErrorMapper.messageRes(error));
    }

    @Test
    public void correoYaRegistrado_avisaQueYaExisteLaCuenta() {
        Throwable error = new FirebaseAuthUserCollisionException("ERROR_EMAIL_ALREADY_IN_USE", "x");
        assertEquals(R.string.error_auth_email_in_use, AuthErrorMapper.messageRes(error));
    }

    @Test
    public void contrasenaDebil_seEvaluaAntesQueCredencialesInvalidas() {
        Throwable error = new FirebaseAuthWeakPasswordException("ERROR_WEAK_PASSWORD", "x", "corta");
        assertEquals(R.string.error_auth_weak_password, AuthErrorMapper.messageRes(error));
    }

    @Test
    public void errorDeRed_pideRevisarInternet() {
        assertEquals(R.string.error_auth_network,
                AuthErrorMapper.messageRes(new FirebaseNetworkException("sin red")));
    }

    @Test
    public void demasiadosIntentos_pideEsperar() {
        assertEquals(R.string.error_auth_too_many_requests,
                AuthErrorMapper.messageRes(new FirebaseTooManyRequestsException("limite")));
    }

    @Test
    public void errorDesconocidoONulo_usaMensajeGenerico() {
        assertEquals(R.string.error_auth_unknown, AuthErrorMapper.messageRes(new RuntimeException("boom")));
        assertEquals(R.string.error_auth_unknown, AuthErrorMapper.messageRes(null));
    }
}
