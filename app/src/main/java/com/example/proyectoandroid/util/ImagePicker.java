package com.example.proyectoandroid.util;

import android.net.Uri;

import androidx.activity.result.ActivityResultCaller;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;

/**
 * Selector de imagenes del dispositivo (Photo Picker, no requiere permisos de almacenamiento).
 *
 * <p>Se crea una sola vez como campo de la Activity o Fragment, antes de que llegue a STARTED:
 * <pre>
 *   private final ImagePicker imagePicker = new ImagePicker(this, uri -> viewModel.upload(uri));
 *   ...
 *   binding.buttonAttach.setOnClickListener(v -> imagePicker.pick());
 * </pre>
 */
public class ImagePicker {

    public interface Callback {
        void onImagePicked(@NonNull Uri uri);
    }

    private final ActivityResultLauncher<PickVisualMediaRequest> launcher;

    public ImagePicker(@NonNull ActivityResultCaller caller, @NonNull Callback callback) {
        launcher = caller.registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
            if (uri != null) { // null = el usuario cancelo la seleccion
                callback.onImagePicked(uri);
            }
        });
    }

    public void pick() {
        launcher.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build());
    }
}
