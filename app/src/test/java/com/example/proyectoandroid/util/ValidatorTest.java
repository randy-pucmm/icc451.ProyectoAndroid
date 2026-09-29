package com.example.proyectoandroid.util;

import static org.junit.Assert.assertEquals;

import com.example.proyectoandroid.R;

import org.junit.Test;

public class ValidatorTest {

    @Test
    public void validateName_vacioONulo_pideNombre() {
        assertEquals(R.string.error_name_required, Validator.validateName(null));
        assertEquals(R.string.error_name_required, Validator.validateName("   "));
    }

    @Test
    public void validateName_unaLetra_esMuyCorto() {
        assertEquals(R.string.error_name_short, Validator.validateName("A"));
    }

    @Test
    public void validateName_nombreNormal_esValido() {
        assertEquals(Validator.VALID, Validator.validateName("  Ana  "));
    }

    @Test
    public void validateEmail_vacioONulo_pideCorreo() {
        assertEquals(R.string.error_email_required, Validator.validateEmail(null));
        assertEquals(R.string.error_email_required, Validator.validateEmail(""));
    }

    @Test
    public void validateEmail_formatosInvalidos_sonRechazados() {
        assertEquals(R.string.error_email_invalid, Validator.validateEmail("ana"));
        assertEquals(R.string.error_email_invalid, Validator.validateEmail("ana@"));
        assertEquals(R.string.error_email_invalid, Validator.validateEmail("ana@correo"));
        assertEquals(R.string.error_email_invalid, Validator.validateEmail("@correo.com"));
        assertEquals(R.string.error_email_invalid, Validator.validateEmail("ana@correo.c"));
        assertEquals(R.string.error_email_invalid, Validator.validateEmail("ana perez@correo.com"));
    }

    @Test
    public void validateEmail_formatosValidos_sonAceptados() {
        assertEquals(Validator.VALID, Validator.validateEmail("ana@correo.com"));
        assertEquals(Validator.VALID, Validator.validateEmail("  ana.perez+chat@ce.pucmm.edu.do "));
    }

    @Test
    public void validatePasswordRequired_soloExigeNoVacia() {
        assertEquals(R.string.error_password_required, Validator.validatePasswordRequired(null));
        assertEquals(R.string.error_password_required, Validator.validatePasswordRequired(""));
        assertEquals(Validator.VALID, Validator.validatePasswordRequired("1"));
    }

    @Test
    public void validateNewPassword_exigeLongitudMinima() {
        assertEquals(R.string.error_password_required, Validator.validateNewPassword(""));
        assertEquals(R.string.error_password_short, Validator.validateNewPassword("12345"));
        assertEquals(Validator.VALID, Validator.validateNewPassword("123456"));
    }

    @Test
    public void validatePasswordConfirmation_debeCoincidir() {
        assertEquals(R.string.error_confirm_required, Validator.validatePasswordConfirmation("123456", ""));
        assertEquals(R.string.error_password_mismatch, Validator.validatePasswordConfirmation("123456", "654321"));
        assertEquals(Validator.VALID, Validator.validatePasswordConfirmation("123456", "123456"));
    }
}
