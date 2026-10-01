package com.example.proyectoandroid.ui.chat;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.proyectoandroid.R;
import com.example.proyectoandroid.databinding.ActivityChatBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ChatActivity extends AppCompatActivity {

    public static final String EXTRA_OTHER_UID = "extra_other_uid";
    public static final String EXTRA_OTHER_NAME = "extra_other_name";

    private ActivityChatBinding binding;
    private ChatViewModel viewModel;
    private MessageAdapter adapter;

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
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String otherUid = getIntent().getStringExtra(EXTRA_OTHER_UID);
        String otherName = getIntent().getStringExtra(EXTRA_OTHER_NAME);
        if (user == null || otherUid == null) {
            Toast.makeText(this, R.string.chat_error_no_session, Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        String currentName = user.getDisplayName() != null ? user.getDisplayName() : user.getEmail();
        viewModel = new ViewModelProvider(this,
                new ChatViewModelFactory(user.getUid(), currentName, otherUid)).get(ChatViewModel.class);

        binding.toolbar.setTitle(otherName != null ? otherName : getString(R.string.chat_title));
        setupList(user.getUid());
        observeMessages();
    }

    private void setupList(String currentUid) {
        adapter = new MessageAdapter(currentUid);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        binding.rvMessages.setLayoutManager(layoutManager);
        binding.rvMessages.setAdapter(adapter);
    }

    private void observeMessages() {
        viewModel.getMessages().observe(this, resource -> {
            if (resource.getStatus() == com.example.proyectoandroid.util.Resource.Status.SUCCESS
                    && resource.getData() != null) {
                adapter.submitList(resource.getData());
            }
        });
    }
}
