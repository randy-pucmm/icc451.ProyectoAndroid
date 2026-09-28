package com.example.proyectoandroid.ui.chat;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.proyectoandroid.R;

public class ChatActivity extends AppCompatActivity {

    public static final String EXTRA_OTHER_UID = "extra_other_uid";
    public static final String EXTRA_OTHER_NAME = "extra_other_name";

    /** Unica forma de abrir el chat: la usan UsersActivity y el PendingIntent de la notificacion. */
    public static Intent newIntent(Context context, String otherUid, String otherName) {
        Intent intent = new Intent(context, ChatActivity.class);
        intent.putExtra(EXTRA_OTHER_UID, otherUid);
        intent.putExtra(EXTRA_OTHER_NAME, otherName);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
    }
}
