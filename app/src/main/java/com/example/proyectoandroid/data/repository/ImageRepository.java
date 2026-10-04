package com.example.proyectoandroid.data.repository;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.util.Base64;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.proyectoandroid.R;
import com.example.proyectoandroid.data.FirebasePaths;
import com.example.proyectoandroid.util.ImageCompressor;
import com.example.proyectoandroid.util.Resource;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Sube una imagen y devuelve la cadena que se guarda en Firestore (imageUrl / photoUrl).
 * Quien la use no depende del backend: Glide carga tanto una URL https como un data URI.
 */
public class ImageRepository {

    public enum Backend {
        /** Firebase Storage: lo que pide el PDF, pero exige el plan Blaze. */
        STORAGE,
        /** Respaldo: JPEG comprimido en Base64 dentro del documento (limite de 1 MiB). */
        BASE64
    }

    /** Backend activo. Cambiar a STORAGE cuando el proyecto tenga Blaze (o se use el emulador). */
    public static final Backend BACKEND = Backend.BASE64;

    /** Con STORAGE: usa el Storage Emulator de la maquina de desarrollo (solo emulador Android). */
    private static final boolean USE_STORAGE_EMULATOR = false;
    private static final String EMULATOR_HOST = "10.0.2.2";
    private static final int EMULATOR_PORT = 9199;

    private static final String JPEG = "image/jpeg";
    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();
    private static boolean emulatorConfigured;

    private final Context context;
    private final FirebaseAuth auth;

    public ImageRepository(@NonNull Context context) {
        this.context = context.getApplicationContext();
        this.auth = FirebaseAuth.getInstance();
    }

    /** Imagen para enviar por el chat. Guarda en Storage: chat_images/{uid}/{uuid}.jpg */
    public LiveData<Resource<String>> upload(@NonNull Uri uri) {
        return upload(uri, ImageCompressor.CHAT_MAX_DIMENSION, FirebasePaths.STORAGE_CHAT_IMAGES, UUID.randomUUID() + ".jpg");
    }

    /** Foto de perfil, reducida a un avatar. Guarda en Storage: profile_photos/{uid}/avatar.jpg */
    public LiveData<Resource<String>> uploadProfilePhoto(@NonNull Uri uri) {
        return upload(uri, ImageCompressor.AVATAR_MAX_DIMENSION, FirebasePaths.STORAGE_PROFILE_PHOTOS, "avatar.jpg");
    }

    private LiveData<Resource<String>> upload(Uri uri, int maxDimension, String folder, String fileName) {
        MutableLiveData<Resource<String>> result = new MutableLiveData<>(Resource.loading());
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            result.setValue(Resource.error(context.getString(R.string.error_image_no_session)));
            return result;
        }

        ContentResolver resolver = context.getContentResolver();
        EXECUTOR.execute(() -> {
            try {
                byte[] bytes = ImageCompressor.compress(resolver, uri, maxDimension);
                if (BACKEND == Backend.BASE64) {
                    String dataUri = "data:" + JPEG + ";base64," + Base64.encodeToString(bytes, Base64.NO_WRAP);
                    result.postValue(Resource.success(dataUri));
                } else {
                    uploadToStorage(bytes, folder + "/" + user.getUid() + "/" + fileName, result);
                }
            } catch (IOException e) {
                result.postValue(Resource.error(context.getString(R.string.error_image_read)));
            }
        });
        return result;
    }

    private void uploadToStorage(byte[] bytes, String path, MutableLiveData<Resource<String>> result) {
        StorageReference ref = storage().getReference().child(path);
        StorageMetadata metadata = new StorageMetadata.Builder().setContentType(JPEG).build();
        ref.putBytes(bytes, metadata)
                .continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    return ref.getDownloadUrl();
                })
                .addOnSuccessListener(url -> result.postValue(Resource.success(url.toString())))
                .addOnFailureListener(e ->
                        result.postValue(Resource.error(context.getString(R.string.error_image_upload))));
    }

    private static synchronized FirebaseStorage storage() {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        if (USE_STORAGE_EMULATOR && !emulatorConfigured) {
            storage.useEmulator(EMULATOR_HOST, EMULATOR_PORT);
            emulatorConfigured = true;
        }
        return storage;
    }
}
