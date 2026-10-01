package com.example.proyectoandroid.data.repository;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;

import com.example.proyectoandroid.data.FirebasePaths;
import com.example.proyectoandroid.data.model.User;
import com.example.proyectoandroid.util.Resource;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Acceso a la coleccion users/{uid}: alta de perfiles y lista de usuarios en tiempo real. */
public class UserRepository {

    private final FirebaseFirestore firestore;

    public UserRepository() {
        this(FirebaseFirestore.getInstance());
    }

    public UserRepository(FirebaseFirestore firestore) {
        this.firestore = firestore;
    }

    /** Escribe (o reemplaza) el perfil users/{uid}. Se usa al registrar una cuenta nueva. */
    public Task<Void> saveUser(@NonNull User user) {
        return users().document(user.getUid()).set(user);
    }

    /**
     * Crea users/{uid} solo si todavia no existe. Cubre las cuentas creadas a mano en la consola
     * de Firebase, que no tienen perfil y por eso no aparecerian en la lista de usuarios.
     */
    public Task<Void> ensureUserDocument(@NonNull FirebaseUser firebaseUser) {
        DocumentReference ref = users().document(firebaseUser.getUid());
        return ref.get().onSuccessTask(snapshot -> {
            if (snapshot.exists()) {
                return Tasks.forResult(null);
            }
            return ref.set(new User(firebaseUser.getUid(), displayNameOf(firebaseUser), firebaseUser.getEmail()));
        });
    }

    /** El perfil de un usuario, actualizado en tiempo real. */
    public LiveData<Resource<User>> getUser(@NonNull String uid) {
        return new UserLiveData(users().document(uid));
    }

    /** Guarda la foto de perfil (URL https o data URI, segun el backend de ImageRepository). */
    public Task<Void> updatePhotoUrl(@NonNull String uid, @NonNull String photoUrl) {
        return users().document(uid).update(User.FIELD_PHOTO_URL, photoUrl);
    }

    /** Todos los usuarios menos {@code excludeUid}, ordenados por nombre, actualizados en tiempo real. */
    public LiveData<Resource<List<User>>> getUsers(@NonNull String excludeUid) {
        return new UsersLiveData(users(), excludeUid);
    }

    /** Obtiene el token FCM de este dispositivo y lo guarda en users/{uid}.fcmToken. */
    public Task<Void> saveCurrentFcmToken(@NonNull String uid) {
        return FirebaseMessaging.getInstance().getToken().onSuccessTask(token -> updateFcmToken(uid, token));
    }

    /** Guarda el token (o lo borra si es null). Lo usa el notificador para saber a donde enviar el push. */
    public Task<Void> updateFcmToken(@NonNull String uid, @Nullable String token) {
        Object value = token == null ? FieldValue.delete() : token;
        return users().document(uid).update(Collections.singletonMap(User.FIELD_FCM_TOKEN, value));
    }

    /**
     * Marca al usuario en linea o desconectado y renueva lastSeen con la hora del servidor.
     * Usa update (no set) para no crear un perfil incompleto si users/{uid} aun no existe.
     */
    public Task<Void> setPresence(@NonNull String uid, boolean online) {
        Map<String, Object> fields = new HashMap<>();
        fields.put(User.FIELD_ONLINE, online);
        fields.put(User.FIELD_LAST_SEEN, FieldValue.serverTimestamp());
        return users().document(uid).update(fields);
    }

    /**
     * Prepara el cierre de sesion: pasa a desconectado y desvincula el token de notificaciones
     * (ambas escrituras necesitan que el usuario siga autenticado, por eso van antes del signOut).
     */
    public Task<Void> clearSessionData(@NonNull String uid) {
        return Tasks.whenAll(setPresence(uid, false), clearFcmToken(uid));
    }

    /**
     * Desvincula este dispositivo del usuario: borra el token de su perfil y el token local, para que
     * no sigan llegando notificaciones de su cuenta. Debe llamarse antes de cerrar la sesion.
     */
    public Task<Void> clearFcmToken(@NonNull String uid) {
        return updateFcmToken(uid, null)
                .continueWithTask(task -> FirebaseMessaging.getInstance().deleteToken());
    }

    private CollectionReference users() {
        return firestore.collection(FirebasePaths.USERS);
    }

    private static String displayNameOf(FirebaseUser firebaseUser) {
        String name = firebaseUser.getDisplayName();
        if (name != null && !name.trim().isEmpty()) {
            return name.trim();
        }
        String email = firebaseUser.getEmail();
        if (email != null && email.contains("@")) {
            return email.substring(0, email.indexOf('@'));
        }
        return firebaseUser.getUid();
    }

    /** Igual que UsersLiveData pero para un solo documento users/{uid}. */
    private static class UserLiveData extends LiveData<Resource<User>> {

        private final DocumentReference document;
        private ListenerRegistration registration;

        UserLiveData(DocumentReference document) {
            this.document = document;
        }

        @Override
        protected void onActive() {
            if (getValue() == null) {
                setValue(Resource.loading());
            }
            registration = document.addSnapshotListener((snapshot, error) -> {
                if (error != null) {
                    setValue(Resource.error(error.getMessage() != null ? error.getMessage() : error.toString()));
                } else if (snapshot != null && snapshot.exists()) {
                    setValue(Resource.success(snapshot.toObject(User.class)));
                }
            });
        }

        @Override
        protected void onInactive() {
            if (registration != null) {
                registration.remove();
                registration = null;
            }
        }
    }

    /** Registra el listener de Firestore solo mientras alguien observa el LiveData. */
    private static class UsersLiveData extends LiveData<Resource<List<User>>> {

        private final CollectionReference collection;
        private final String excludeUid;
        private ListenerRegistration registration;

        UsersLiveData(CollectionReference collection, String excludeUid) {
            this.collection = collection;
            this.excludeUid = excludeUid;
        }

        @Override
        protected void onActive() {
            if (getValue() == null) {
                setValue(Resource.loading());
            }
            registration = collection.addSnapshotListener((snapshot, error) -> {
                if (error != null) {
                    setValue(Resource.error(error.getMessage() != null ? error.getMessage() : error.toString()));
                    return;
                }
                if (snapshot == null) {
                    return;
                }
                List<User> result = new ArrayList<>();
                for (DocumentSnapshot document : snapshot.getDocuments()) {
                    if (document.getId().equals(excludeUid)) {
                        continue;
                    }
                    User user = document.toObject(User.class);
                    if (user == null) {
                        continue;
                    }
                    if (user.getUid() == null) {
                        user.setUid(document.getId());
                    }
                    result.add(user);
                }
                Collections.sort(result, (a, b) -> nameOf(a).compareToIgnoreCase(nameOf(b)));
                setValue(Resource.success(result));
            });
        }

        @Override
        protected void onInactive() {
            if (registration != null) {
                registration.remove();
                registration = null;
            }
        }

        private static String nameOf(User user) {
            return user.getDisplayName() == null ? "" : user.getDisplayName();
        }
    }
}
