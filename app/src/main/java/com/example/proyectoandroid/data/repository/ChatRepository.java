package com.example.proyectoandroid.data.repository;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.proyectoandroid.data.FirebasePaths;
import com.example.proyectoandroid.data.model.Message;
import com.example.proyectoandroid.util.Resource;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.WriteBatch;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Acceso a chats/{chatId} y chats/{chatId}/messages en Firestore. */
public class ChatRepository {

    private final FirebaseFirestore db;

    public ChatRepository() {
        this.db = FirebaseFirestore.getInstance();
    }

    /**
     * Guarda el mensaje y actualiza chats/{chatId} (participants, lastMessage, lastMessageAt)
     * en un solo batch, para que el chat y su ultimo mensaje nunca queden desfasados.
     * El timestamp del mensaje lo pone el servidor (@ServerTimestamp en Message).
     */
    public LiveData<Resource<Void>> sendMessage(String chatId, List<String> participants, Message message) {
        MutableLiveData<Resource<Void>> result = new MutableLiveData<>(Resource.loading());

        DocumentReference chatRef = db.collection(FirebasePaths.CHATS).document(chatId);
        DocumentReference messageRef = chatRef.collection(FirebasePaths.MESSAGES).document();

        Map<String, Object> chat = new HashMap<>();
        chat.put("participants", participants);
        chat.put("lastMessage", message.getText());
        chat.put("lastMessageAt", FieldValue.serverTimestamp());

        WriteBatch batch = db.batch();
        batch.set(chatRef, chat, SetOptions.merge());
        batch.set(messageRef, message);
        batch.commit()
                .addOnSuccessListener(v -> result.setValue(Resource.success(null)))
                .addOnFailureListener(e -> result.setValue(Resource.error(errorMessage(e))));
        return result;
    }

    @NonNull
    private static String errorMessage(Exception e) {
        return e.getMessage() != null ? e.getMessage() : "Error desconocido";
    }
}
