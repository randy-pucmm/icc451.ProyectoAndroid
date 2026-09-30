package com.example.proyectoandroid.data.repository;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.proyectoandroid.data.FirebasePaths;
import com.example.proyectoandroid.data.model.Message;
import com.example.proyectoandroid.util.Resource;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
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

    public LiveData<Resource<List<Message>>> listenMessages(String chatId) {
        Query query = db.collection(FirebasePaths.CHATS).document(chatId)
                .collection(FirebasePaths.MESSAGES)
                .orderBy("timestamp", Query.Direction.ASCENDING);
        return new MessagesLiveData(query);
    }

    @NonNull
    private static String errorMessage(Exception e) {
        return e.getMessage() != null ? e.getMessage() : "Error desconocido";
    }

    /** Registra el snapshot listener solo mientras alguien observa, y lo quita al quedar inactivo. */
    private static class MessagesLiveData extends LiveData<Resource<List<Message>>> {

        private final Query query;
        private ListenerRegistration registration;

        MessagesLiveData(Query query) {
            this.query = query;
        }

        @Override
        protected void onActive() {
            if (getValue() == null) {
                setValue(Resource.loading());
            }
            registration = query.addSnapshotListener((snapshot, error) -> {
                if (error != null || snapshot == null) {
                    setValue(Resource.error(error != null ? errorMessage(error) : "Error desconocido"));
                    return;
                }
                List<Message> list = new ArrayList<>();
                for (DocumentSnapshot doc : snapshot.getDocuments()) {
                    // ESTIMATE evita timestamp nulo en mensajes propios aun pendientes de confirmar.
                    Message message = doc.toObject(Message.class, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE);
                    if (message != null) {
                        list.add(message);
                    }
                }
                setValue(Resource.success(list));
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
}
