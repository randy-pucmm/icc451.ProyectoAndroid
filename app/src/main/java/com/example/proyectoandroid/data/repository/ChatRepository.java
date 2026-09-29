package com.example.proyectoandroid.data.repository;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.proyectoandroid.data.FirebasePaths;
import com.example.proyectoandroid.data.model.Message;
import com.example.proyectoandroid.util.Resource;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

/** Acceso a chats/{chatId}/messages en Firestore. */
public class ChatRepository {

    private final FirebaseFirestore db;

    public ChatRepository() {
        this.db = FirebaseFirestore.getInstance();
    }

    private CollectionReference messages(String chatId) {
        return db.collection(FirebasePaths.CHATS).document(chatId).collection(FirebasePaths.MESSAGES);
    }

    public LiveData<Resource<Void>> sendMessage(String chatId, Message message) {
        MutableLiveData<Resource<Void>> result = new MutableLiveData<>(Resource.loading());
        messages(chatId).add(message)
                .addOnSuccessListener(ref -> result.setValue(Resource.success(null)))
                .addOnFailureListener(e -> result.setValue(Resource.error(errorMessage(e))));
        return result;
    }

    @NonNull
    private static String errorMessage(Exception e) {
        return e.getMessage() != null ? e.getMessage() : "Error desconocido";
    }
}
