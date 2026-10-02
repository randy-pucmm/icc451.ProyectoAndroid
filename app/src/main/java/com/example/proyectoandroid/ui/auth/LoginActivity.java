package com.example.proyectoandroid.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.proyectoandroid.databinding.ActivityLoginBinding;
import com.example.proyectoandroid.ui.users.UsersActivity;
import com.example.proyectoandroid.util.Resource;
import com.example.proyectoandroid.util.TextInputUtils;
import com.example.proyectoandroid.util.WindowInsetsHelper;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private AuthViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        WindowInsetsHelper.applySystemBarPadding(binding.root);

        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        TextInputUtils.clearErrorOnEdit(binding.inputEmail);
        TextInputUtils.clearErrorOnEdit(binding.inputPassword);

        binding.buttonLogin.setOnClickListener(v -> submit());
        binding.editPassword.setOnEditorActionListener((view, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                submit();
                return true;
            }
            return false;
        });
        binding.buttonGoRegister.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));

        viewModel.getFormErrors().observe(this, this::showFormErrors);
        viewModel.getAuthState().observe(this, this::render);
    }

    private void submit() {
        viewModel.login(
                TextInputUtils.textOf(binding.inputEmail),
                TextInputUtils.textOf(binding.inputPassword));
    }

    private void showFormErrors(AuthViewModel.FormErrors errors) {
        if (errors == null) {
            return;
        }
        TextInputUtils.setError(binding.inputEmail, errors.email);
        TextInputUtils.setError(binding.inputPassword, errors.password);
        viewModel.onFormErrorsShown();
    }

    private void render(Resource<FirebaseUser> state) {
        boolean loading = state.getStatus() == Resource.Status.LOADING;
        binding.progress.setVisibility(loading ? View.VISIBLE : View.GONE);
        binding.buttonLogin.setEnabled(!loading);
        binding.buttonGoRegister.setEnabled(!loading);

        boolean failed = state.getStatus() == Resource.Status.ERROR;
        binding.textError.setVisibility(failed ? View.VISIBLE : View.GONE);
        if (failed) {
            binding.textError.setText(state.getMessage());
        }

        if (state.getStatus() == Resource.Status.SUCCESS) {
            Intent intent = new Intent(this, UsersActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
    }
}
