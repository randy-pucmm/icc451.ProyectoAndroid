package com.example.proyectoandroid.ui.chat;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.proyectoandroid.R;
import com.example.proyectoandroid.data.model.Message;
import com.example.proyectoandroid.databinding.ActivityChatBinding;
import com.example.proyectoandroid.util.Resource;
import com.example.proyectoandroid.util.WindowInsetsHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

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
        WindowInsetsHelper.enableEdgeToEdgeWithAppBar(this);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        WindowInsetsHelper.applyContentPadding(binding.root);
        WindowInsetsHelper.applyStatusBarPadding(binding.appBar);
        binding.toolbar.setNavigationOnClickListener(v -> finish());

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
        observeSendState();
        binding.btnSend.setOnClickListener(v -> sendMessage());
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
            boolean loading = resource.getStatus() == Resource.Status.LOADING;
            binding.progress.setVisibility(loading ? View.VISIBLE : View.GONE);

            if (resource.getStatus() == Resource.Status.ERROR) {
                Toast.makeText(this, R.string.chat_error_load, Toast.LENGTH_LONG).show();
                return;
            }
            if (resource.getStatus() == Resource.Status.SUCCESS && resource.getData() != null) {
                List<Message> list = resource.getData();
                binding.tvEmpty.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
                adapter.submitList(list, () -> {
                    if (!list.isEmpty()) {
                        binding.rvMessages.scrollToPosition(list.size() - 1);
                    }
                });
            }
        });
    }

    private void sendMessage() {
        String text = binding.etMessage.getText().toString();
        // El ViewModel ignora los mensajes vacios; solo limpiamos el campo si se envio algo.
        if (viewModel.sendTextMessage(text)) {
            binding.etMessage.setText("");
        }
    }

    private void observeSendState() {
        viewModel.getSendState().observe(this, resource -> {
            if (resource.getStatus() == Resource.Status.ERROR) {
                Toast.makeText(this, R.string.chat_error_send, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
