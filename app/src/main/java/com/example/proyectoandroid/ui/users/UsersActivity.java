package com.example.proyectoandroid.ui.users;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MenuItem;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.proyectoandroid.R;
import com.example.proyectoandroid.data.model.User;
import com.example.proyectoandroid.databinding.ActivityUsersBinding;
import com.example.proyectoandroid.fcm.NotificationHelper;
import com.example.proyectoandroid.ui.auth.LoginActivity;
import com.example.proyectoandroid.ui.chat.ChatActivity;
import com.example.proyectoandroid.util.ImagePicker;
import com.example.proyectoandroid.util.Resource;
import com.example.proyectoandroid.util.WindowInsetsHelper;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;

/** Lista de usuarios registrados; al tocar uno se abre la conversacion con el. */
public class UsersActivity extends AppCompatActivity {

    /** Cada cuanto se recalcula quien sigue en linea (ver UserAdapter#refreshPresence). */
    private static final long PRESENCE_REFRESH_MS = 30_000;

    private ActivityUsersBinding binding;
    private UsersViewModel viewModel;
    private UserAdapter adapter;

    private final ImagePicker imagePicker = new ImagePicker(this, uri -> viewModel.changeProfilePhoto(uri));

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable presenceRefresh = new Runnable() {
        @Override
        public void run() {
            if (adapter != null) {
                adapter.refreshPresence();
            }
            handler.postDelayed(this, PRESENCE_REFRESH_MS);
        }
    };

    // Si el usuario la rechaza, la app sigue funcionando; simplemente no se muestran notificaciones.
    private final ActivityResultLauncher<String> notificationPermission =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> { });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowInsetsHelper.enableEdgeToEdgeWithAppBar(this);
        binding = ActivityUsersBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        WindowInsetsHelper.applyContentPadding(binding.root);
        WindowInsetsHelper.applyStatusBarPadding(binding.appBar);

        viewModel = new ViewModelProvider(this).get(UsersViewModel.class);
        if (!viewModel.isLoggedIn()) {
            goToLogin();
            return;
        }

        binding.toolbar.inflateMenu(R.menu.menu_users);
        binding.toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_logout) {
                viewModel.logout();
                return true;
            }
            return false;
        });

        setUpSearch();

        adapter = new UserAdapter(this::openChat);
        binding.recyclerUsers.setAdapter(adapter);

        NotificationHelper.ensureChannel(this);
        if (savedInstanceState == null) {
            requestNotificationPermission();
        }

        binding.headerProfile.setOnClickListener(v -> imagePicker.pick());
        viewModel.getCurrentUser().observe(this, this::renderProfile);
        viewModel.getPhotoState().observe(this, this::renderPhotoState);

        viewModel.getUsers().observe(this, this::render);
        viewModel.getLoggedOut().observe(this, loggedOut -> {
            if (Boolean.TRUE.equals(loggedOut)) {
                goToLogin();
            }
        });
    }

    private void setUpSearch() {
        MenuItem searchItem = binding.toolbar.getMenu().findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setQueryHint(getString(R.string.users_search_hint));

        // Tras rotar la pantalla se vuelve a mostrar la busqueda en curso.
        if (!viewModel.getQuery().isEmpty()) {
            searchItem.expandActionView();
            searchView.setQuery(viewModel.getQuery(), false);
        }
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                viewModel.setQuery(newText);
                return true;
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        handler.postDelayed(presenceRefresh, PRESENCE_REFRESH_MS);
    }

    @Override
    protected void onStop() {
        super.onStop();
        handler.removeCallbacks(presenceRefresh);
    }

    /** Desde Android 13 mostrar notificaciones requiere permiso en tiempo de ejecucion. */
    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS);
        }
    }

    private void openChat(User user) {
        startActivity(ChatActivity.newIntent(this, user.getUid(), user.getDisplayName()));
    }

    private void goToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void renderProfile(Resource<User> state) {
        User me = state.getData();
        if (me == null) {
            return;
        }
        binding.textProfileName.setText(me.getDisplayName());
        AvatarBinder.bind(binding.textProfileInitial, binding.imageProfilePhoto, me.getDisplayName(), me.getPhotoUrl());
    }

    private void renderPhotoState(Resource<String> state) {
        if (state == null) {
            return;
        }
        boolean loading = state.getStatus() == Resource.Status.LOADING;
        binding.progressPhoto.setVisibility(loading ? View.VISIBLE : View.GONE);
        binding.headerProfile.setEnabled(!loading);
        if (state.getStatus() == Resource.Status.ERROR) {
            Snackbar.make(binding.root, state.getMessage(), Snackbar.LENGTH_LONG).show();
        }
        if (!loading) {
            viewModel.onPhotoStateHandled();
        }
    }

    private void render(Resource<List<User>> state) {
        boolean loading = state.getStatus() == Resource.Status.LOADING;
        boolean failed = state.getStatus() == Resource.Status.ERROR;
        List<User> users = state.getData();
        boolean empty = state.getStatus() == Resource.Status.SUCCESS && (users == null || users.isEmpty());

        binding.progress.setVisibility(loading ? View.VISIBLE : View.GONE);
        binding.textError.setVisibility(failed ? View.VISIBLE : View.GONE);
        binding.textEmpty.setText(viewModel.getQuery().isEmpty() ? R.string.users_empty : R.string.users_no_results);
        binding.textEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
        binding.recyclerUsers.setVisibility(state.getStatus() == Resource.Status.SUCCESS && !empty ? View.VISIBLE : View.GONE);

        if (users != null) {
            adapter.submitList(users);
        }
    }
}
