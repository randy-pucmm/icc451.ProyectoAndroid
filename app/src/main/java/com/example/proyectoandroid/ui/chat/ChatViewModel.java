package com.example.proyectoandroid.ui.chat;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.ViewModel;

import com.example.proyectoandroid.data.model.Chat;
import com.example.proyectoandroid.data.model.Message;
import com.example.proyectoandroid.data.repository.ChatRepository;
import com.example.proyectoandroid.util.Resource;

import java.util.Arrays;
import java.util.List;

/** Estado del chat 1 a 1: lista de mensajes en tiempo real y envio. */
public class ChatViewModel extends ViewModel {

    private final ChatRepository repository;
    private final String chatId;
    private final String currentUid;
    private final String currentName;
    private final List<String> participants;

    private final LiveData<Resource<List<Message>>> messages;
    private final MediatorLiveData<Resource<Void>> sendState = new MediatorLiveData<>();

    public ChatViewModel(ChatRepository repository, String currentUid, String currentName, String otherUid) {
        this.repository = repository;
        this.currentUid = currentUid;
        this.currentName = currentName;
        this.chatId = Chat.buildChatId(currentUid, otherUid);
        this.participants = Arrays.asList(currentUid, otherUid);
        this.messages = repository.listenMessages(chatId);
    }

    public LiveData<Resource<List<Message>>> getMessages() {
        return messages;
    }

    public LiveData<Resource<Void>> getSendState() {
        return sendState;
    }

    public String getCurrentUid() {
        return currentUid;
    }

    @Nullable
    static String normalize(@Nullable String raw) {
        if (raw == null) {
            return null;
        }
        String text = raw.trim();
        return text.isEmpty() ? null : text;
    }

    public boolean sendTextMessage(String raw) {
        String text = normalize(raw);
        if (text == null) {
            return false;
        }
        Message message = buildMessage(Message.TYPE_TEXT);
        message.setText(text);
        send(message);
        return true;
    }

    /**
     * Mensaje de imagen: la URL la entrega ImageRepository.upload (ya subida a su destino).
     *
     * @return false si la URL esta vacia y no se envio nada.
     */
    public boolean sendImageMessage(String imageUrl) {
        if (normalize(imageUrl) == null) {
            return false;
        }
        Message message = buildMessage(Message.TYPE_IMAGE);
        message.setImageUrl(imageUrl.trim());
        send(message);
        return true;
    }

    private Message buildMessage(String type) {
        Message message = new Message();
        message.setSenderId(currentUid);
        message.setSenderName(currentName);
        message.setType(type);
        return message;
    }

    private void send(Message message) {
        LiveData<Resource<Void>> source = repository.sendMessage(chatId, participants, message);
        sendState.addSource(source, resource -> {
            sendState.setValue(resource);
            if (resource.getStatus() != Resource.Status.LOADING) {
                sendState.removeSource(source);
            }
        });
    }
}
