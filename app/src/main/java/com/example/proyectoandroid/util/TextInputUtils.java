package com.example.proyectoandroid.util;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

import com.google.android.material.textfield.TextInputLayout;

/** Utilidades para los formularios con TextInputLayout. */
public final class TextInputUtils {

    private TextInputUtils() {
    }

    /** Quita el mensaje de error del campo en cuanto el usuario empieza a corregirlo. */
    public static void clearErrorOnEdit(@NonNull TextInputLayout layout) {
        EditText editText = layout.getEditText();
        if (editText == null) {
            return;
        }
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                layout.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    /** Muestra el mensaje en el campo, o limpia el error si {@code messageRes} es {@link Validator#VALID}. */
    public static void setError(@NonNull TextInputLayout layout, @StringRes int messageRes) {
        layout.setError(messageRes == Validator.VALID ? null : layout.getContext().getString(messageRes));
    }

    /** Texto del campo o cadena vacia; nunca null. */
    @NonNull
    public static String textOf(@NonNull TextInputLayout layout) {
        EditText editText = layout.getEditText();
        return editText == null || editText.getText() == null ? "" : editText.getText().toString();
    }
}
