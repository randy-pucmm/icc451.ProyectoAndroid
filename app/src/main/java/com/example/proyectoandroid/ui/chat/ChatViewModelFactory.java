package com.example.proyectoandroid.ui.chat;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.proyectoandroid.data.repository.ChatRepository;

/** Crea ChatViewModel con los datos de la conversacion (el constructor no es vacio). */
public class ChatViewModelFactory implements ViewModelProvider.Factory {

    private final String currentUid;
    private final String currentName;
    private final String otherUid;

    public ChatViewModelFactory(String currentUid, String currentName, String otherUid) {
        this.currentUid = currentUid;
        this.currentName = currentName;
        this.otherUid = otherUid;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(ChatViewModel.class)) {
            return (T) new ChatViewModel(new ChatRepository(), currentUid, currentName, otherUid);
        }
        throw new IllegalArgumentException("ViewModel desconocido: " + modelClass.getName());
    }
}
