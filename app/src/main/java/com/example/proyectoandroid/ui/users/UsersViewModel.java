package com.example.proyectoandroid.ui.users;

import android.app.Application;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.proyectoandroid.R;
import com.example.proyectoandroid.data.model.User;
import com.example.proyectoandroid.data.repository.AuthRepository;
import com.example.proyectoandroid.data.repository.ImageRepository;
import com.example.proyectoandroid.data.repository.UserRepository;
import com.example.proyectoandroid.util.Resource;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

/** Logica de presentacion de la lista de usuarios, el token de notificaciones y el cierre de sesion. */
public class UsersViewModel extends AndroidViewModel {

    /** Si Firestore no responde (sin internet), el cierre de sesion no debe quedarse esperando. */
    private static final long LOGOUT_TIMEOUT_MS = 3000;

    private final AuthRepository authRepository;
    private final UserRepository userRepository = new UserRepository();
    private final ImageRepository imageRepository;
    private final LiveData<Resource<List<User>>> users;
    private final LiveData<Resource<User>> currentUser;
    private final MediatorLiveData<Resource<String>> photoState = new MediatorLiveData<>();
    private final MutableLiveData<String> query = new MutableLiveData<>("");
    private final MediatorLiveData<Resource<List<User>>> visibleUsers = new MediatorLiveData<>();
    private final MutableLiveData<Boolean> loggedOut = new MutableLiveData<>(false);
    private boolean loggingOut;

    public UsersViewModel(@NonNull Application application) {
        super(application);
        authRepository = new AuthRepository(application);
        imageRepository = new ImageRepository(application);
        FirebaseUser current = authRepository.getCurrentUser();
        if (current != null) {
            users = userRepository.getUsers(current.getUid());
            currentUser = userRepository.getUser(current.getUid());
            // Se guarda en cada apertura: el token puede haber cambiado desde la ultima vez.
            userRepository.saveCurrentFcmToken(current.getUid());
            // Evita esperar al siguiente latido de PresenceTracker despues de iniciar sesion.
            userRepository.setPresence(current.getUid(), true);
        } else {
            users = new MutableLiveData<>(Resource.error(application.getString(R.string.users_error_no_session)));
            currentUser = new MutableLiveData<>();
        }
        visibleUsers.addSource(users, resource -> applyFilter());
        visibleUsers.addSource(query, text -> applyFilter());
    }

    public boolean isLoggedIn() {
        return authRepository.getCurrentUser() != null;
    }

    /** Usuarios registrados, sin el usuario actual, filtrados por la busqueda y en tiempo real. */
    public LiveData<Resource<List<User>>> getUsers() {
        return visibleUsers;
    }

    /** Perfil del usuario que inicio sesion (nombre y foto de la cabecera). */
    public LiveData<Resource<User>> getCurrentUser() {
        return currentUser;
    }

    /** Progreso del cambio de foto de perfil; es null cuando el resultado ya fue mostrado. */
    public LiveData<Resource<String>> getPhotoState() {
        return photoState;
    }

    public void onPhotoStateHandled() {
        photoState.setValue(null);
    }

    /** Comprime y sube la imagen elegida, y si sale bien la guarda como photoUrl del perfil. */
    public void changeProfilePhoto(@NonNull Uri uri) {
        FirebaseUser current = authRepository.getCurrentUser();
        Resource<String> state = photoState.getValue();
        if (current == null || (state != null && state.getStatus() == Resource.Status.LOADING)) {
            return;
        }
        LiveData<Resource<String>> upload = imageRepository.uploadProfilePhoto(uri);
        photoState.addSource(upload, result -> {
            if (result.getStatus() == Resource.Status.LOADING) {
                photoState.setValue(result);
                return;
            }
            photoState.removeSource(upload);
            if (result.getStatus() == Resource.Status.ERROR) {
                photoState.setValue(result);
                return;
            }
            String photoUrl = result.getData();
            userRepository.updatePhotoUrl(current.getUid(), photoUrl).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    photoState.setValue(Resource.success(photoUrl));
                } else {
                    photoState.setValue(Resource.error(getApplication().getString(R.string.error_photo_save)));
                }
            });
        });
    }

    public void setQuery(String text) {
        query.setValue(text == null ? "" : text);
    }

    public String getQuery() {
        String text = query.getValue();
        return text == null ? "" : text;
    }

    /** Pasa a true cuando la sesion ya se cerro y hay que volver al Login. */
    public LiveData<Boolean> getLoggedOut() {
        return loggedOut;
    }

    /**
     * Antes de cerrar la sesion hay que pasar a desconectado y desvincular el token FCM (necesitan
     * estar autenticado); si no, el dispositivo seguiria recibiendo las notificaciones de esa cuenta.
     */
    public void logout() {
        if (loggingOut) {
            return;
        }
        loggingOut = true;
        FirebaseUser current = authRepository.getCurrentUser();
        if (current == null) {
            finishLogout();
            return;
        }
        userRepository.clearSessionData(current.getUid()).addOnCompleteListener(task -> finishLogout());
        new Handler(Looper.getMainLooper()).postDelayed(this::finishLogout, LOGOUT_TIMEOUT_MS);
    }

    private void applyFilter() {
        Resource<List<User>> current = users.getValue();
        if (current == null) {
            return;
        }
        if (current.getStatus() == Resource.Status.SUCCESS && current.getData() != null) {
            visibleUsers.setValue(Resource.success(UserFilter.filter(current.getData(), getQuery())));
        } else {
            visibleUsers.setValue(current);
        }
    }

    private void finishLogout() {
        if (Boolean.TRUE.equals(loggedOut.getValue())) {
            return; // el temporizador y la tarea pueden llegar los dos
        }
        authRepository.logout();
        loggedOut.setValue(true);
    }
}
