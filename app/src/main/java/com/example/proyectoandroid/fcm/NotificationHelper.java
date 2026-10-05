package com.example.proyectoandroid.fcm;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.app.TaskStackBuilder;
import androidx.core.content.ContextCompat;

import com.example.proyectoandroid.R;
import com.example.proyectoandroid.ui.chat.ChatActivity;
import com.example.proyectoandroid.ui.users.UsersActivity;

/** Crea el canal de notificaciones y muestra el aviso de un mensaje nuevo. */
public final class NotificationHelper {

    public static final String CHANNEL_MESSAGES = "chat_messages";

    private NotificationHelper() {
    }

    /** Desde Android 8 toda notificacion necesita un canal. Es seguro llamarlo varias veces. */
    public static void ensureChannel(@NonNull Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_MESSAGES,
                context.getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_HIGH);
        channel.setDescription(context.getString(R.string.notification_channel_description));
        context.getSystemService(NotificationManager.class).createNotificationChannel(channel);
    }

    /** Quita la notificacion de {@code senderId} (al abrir su chat ya no hace falta). */
    public static void cancel(@NonNull Context context, @NonNull String senderId) {
        NotificationManagerCompat.from(context).cancel(senderId.hashCode());
    }

    /**
     * Muestra la notificacion de un mensaje. Al tocarla se abre el chat con {@code senderId}, y al
     * volver atras se llega a la lista de usuarios en lugar de salir de la app.
     */
    public static void showMessage(@NonNull Context context, @NonNull String senderId,
                                   @NonNull String senderName, @NonNull String body) {
        boolean permissionDenied = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED;
        if (permissionDenied) {
            return;
        }
        ensureChannel(context);

        // Un id por remitente: varios mensajes seguidos de la misma persona actualizan un solo aviso.
        int notificationId = senderId.hashCode();
        Intent users = new Intent(context, UsersActivity.class);
        Intent chat = ChatActivity.newIntent(context, senderId, senderName);
        PendingIntent tapIntent = TaskStackBuilder.create(context)
                .addNextIntent(users)
                .addNextIntent(chat)
                .getPendingIntent(notificationId, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_MESSAGES)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(senderName)
                .setContentText(body)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(tapIntent);

        NotificationManagerCompat.from(context).notify(notificationId, builder.build());
    }
}
