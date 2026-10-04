package com.example.proyectoandroid.data.repository;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.proyectoandroid.data.model.User;
import com.example.proyectoandroid.util.AuthErrorMapper;
import com.example.proyectoandroid.util.Resource;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

/** Registro, inicio y cierre de sesion con Firebase Authentication. */
public class AuthRepository {

    private final Context context;
    private final FirebaseAuth auth;
    private final UserRepository userRepository;

    public AuthRepository(@NonNull Context context) {
        this(context, FirebaseAuth.getInstance(), new UserRepository());
    }

    public AuthRepository(@NonNull Context context, @NonNull FirebaseAuth auth, @NonNull UserRepository userRepository) {
        this.context = context.getApplicationContext();
        this.auth = auth;
        this.userRepository = userRepository;
    }

    @Nullable
    public FirebaseUser getCurrentUser() {
        return auth.getCurrentUser();
    }

    /**
     * Crea la cuenta, fija el displayName en FirebaseUser (lo lee el modulo de Chat) y escribe users/{uid}.
     * Si falla alguno de los pasos posteriores a la creacion, borra la cuenta para poder reintentar.
     */
    public LiveData<Resource<FirebaseUser>> register(@NonNull String name, @NonNull String email, @NonNull String password) {
        MutableLiveData<Resource<FirebaseUser>> result = new MutableLiveData<>(Resource.loading());
        String cleanName = name.trim();
        String cleanEmail = email.trim();

        auth.createUserWithEmailAndPassword(cleanEmail, password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser user = authResult.getUser();
                    UserProfileChangeRequest profile = new UserProfileChangeRequest.Builder()
                            .setDisplayName(cleanName)
                            .build();
                    user.updateProfile(profile)
                            .onSuccessTask(unused -> userRepository.saveUser(new User(user.getUid(), cleanName, cleanEmail)))
                            .addOnSuccessListener(unused -> result.setValue(Resource.success(user)))
                            .addOnFailureListener(error -> {
                                user.delete();
                                auth.signOut();
                                result.setValue(Resource.error(messageFor(error)));
                            });
                })
                .addOnFailureListener(error -> result.setValue(Resource.error(messageFor(error))));
        return result;
    }

    public LiveData<Resource<FirebaseUser>> login(@NonNull String email, @NonNull String password) {
        MutableLiveData<Resource<FirebaseUser>> result = new MutableLiveData<>(Resource.loading());

        auth.signInWithEmailAndPassword(email.trim(), password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser user = authResult.getUser();
                    // Crear el perfil es un extra: si falla, el inicio de sesion igual es valido.
                    userRepository.ensureUserDocument(user)
                            .addOnCompleteListener(task -> result.setValue(Resource.success(user)));
                })
                .addOnFailureListener(error -> result.setValue(Resource.error(messageFor(error))));
        return result;
    }

    public void logout() {
        auth.signOut();
    }

    private String messageFor(Exception error) {
        return context.getString(AuthErrorMapper.messageRes(error));
    }
}
