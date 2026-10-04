package com.example.proyectoandroid.fcm;

import androidx.annotation.NonNull;

import com.example.proyectoandroid.R;
import com.example.proyectoandroid.data.repository.UserRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

/**
 * Recibe las notificaciones push.
 *
 * <p>Contrato con tools/notifier: el servidor envia un mensaje <b>solo de datos</b> (sin bloque
 * "notification"), asi {@link #onMessageReceived} se ejecuta siempre, con la app abierta o cerrada,
 * y la notificacion se ve igual en ambos casos. Claves de {@code data}:
 * senderId (uid del remitente), senderName y body (texto a mostrar).
 */
public class ChatMessagingService extends FirebaseMessagingService {

    public static final String KEY_SENDER_ID = "senderId";
    public static final String KEY_SENDER_NAME = "senderName";
    public static final String KEY_BODY = "body";

    /** FCM renueva el token de vez en cuando: hay que actualizarlo en el perfil del usuario. */
    @Override
    public void onNewToken(@NonNull String token) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            new UserRepository().updateFcmToken(user.getUid(), token);
        }
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        Map<String, String> data = message.getData();
        String senderId = data.get(KEY_SENDER_ID);
        if (senderId == null || senderId.isEmpty()) {
            return;
        }
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null || senderId.equals(user.getUid())) {
            return; // sin sesion, o es un mensaje propio
        }

        String senderName = data.get(KEY_SENDER_NAME);
        if (senderName == null || senderName.isEmpty()) {
            senderName = getString(R.string.notification_default_title);
        }
        String body = data.get(KEY_BODY);
        NotificationHelper.showMessage(this, senderId, senderName, body == null ? "" : body);
    }
}
