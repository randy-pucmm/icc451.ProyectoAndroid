package com.example.proyectoandroid.ui.auth;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.proyectoandroid.data.repository.AuthRepository;
import com.example.proyectoandroid.util.Resource;
import com.example.proyectoandroid.util.Validator;
import com.google.firebase.auth.FirebaseUser;

/** Logica de presentacion de Login y Registro: valida los campos y delega en AuthRepository. */
public class AuthViewModel extends AndroidViewModel {

    /** Id del mensaje de error de cada campo; {@link Validator#VALID} (0) si el campo es correcto. */
    public static class FormErrors {
        @StringRes
        public int name = Validator.VALID;
        @StringRes
        public int email = Validator.VALID;
        @StringRes
        public int password = Validator.VALID;
        @StringRes
        public int confirmation = Validator.VALID;

        boolean hasErrors() {
            return name != Validator.VALID || email != Validator.VALID
                    || password != Validator.VALID || confirmation != Validator.VALID;
        }
    }

    private final AuthRepository repository;
    private final MediatorLiveData<Resource<FirebaseUser>> authState = new MediatorLiveData<>();
    private final MutableLiveData<FormErrors> formErrors = new MutableLiveData<>();
    private LiveData<Resource<FirebaseUser>> currentSource;

    public AuthViewModel(@NonNull Application application) {
        super(application);
        repository = new AuthRepository(application);
    }

    /** Firebase conserva la sesion en disco: es true mientras el usuario no haya cerrado sesion. */
    public boolean isLoggedIn() {
        return repository.getCurrentUser() != null;
    }

    /** Estado de la ultima operacion de login o registro (LOADING, SUCCESS o ERROR). */
    public LiveData<Resource<FirebaseUser>> getAuthState() {
        return authState;
    }

    /** Errores de validacion pendientes de mostrar; es null cuando ya fueron mostrados. */
    public LiveData<FormErrors> getFormErrors() {
        return formErrors;
    }

    /** La Activity lo llama despues de pintar los errores para no repetirlos al rotar la pantalla. */
    public void onFormErrorsShown() {
        formErrors.setValue(null);
    }

    public void login(@Nullable String email, @Nullable String password) {
        if (isLoading()) {
            return; // ya hay una operacion en curso: ignora el doble toque
        }
        FormErrors errors = new FormErrors();
        errors.email = Validator.validateEmail(email);
        errors.password = Validator.validatePasswordRequired(password);
        if (!publishErrors(errors)) {
            submit(repository.login(email, password));
        }
    }

    public void register(@Nullable String name, @Nullable String email,
                         @Nullable String password, @Nullable String confirmation) {
        if (isLoading()) {
            return;
        }
        FormErrors errors = new FormErrors();
        errors.name = Validator.validateName(name);
        errors.email = Validator.validateEmail(email);
        errors.password = Validator.validateNewPassword(password);
        errors.confirmation = Validator.validatePasswordConfirmation(password, confirmation);
        if (!publishErrors(errors)) {
            submit(repository.register(name, email, password));
        }
    }

    /** @return true si hay errores (y los publica), false si el formulario es valido. */
    private boolean publishErrors(FormErrors errors) {
        if (errors.hasErrors()) {
            formErrors.setValue(errors);
            return true;
        }
        return false;
    }

    private boolean isLoading() {
        Resource<FirebaseUser> state = authState.getValue();
        return state != null && state.getStatus() == Resource.Status.LOADING;
    }

    private void submit(LiveData<Resource<FirebaseUser>> source) {
        if (currentSource != null) {
            authState.removeSource(currentSource);
        }
        currentSource = source;
        authState.addSource(source, authState::setValue);
    }
}
