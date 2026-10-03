package com.example.proyectoandroid.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.proyectoandroid.databinding.ActivityRegisterBinding;
import com.example.proyectoandroid.ui.users.UsersActivity;
import com.example.proyectoandroid.util.Resource;
import com.example.proyectoandroid.util.TextInputUtils;
import com.example.proyectoandroid.util.WindowInsetsHelper;
import com.google.firebase.auth.FirebaseUser;

public class RegisterActivity extends AppCompatActivity {

    private ActivityRegisterBinding binding;
    private AuthViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        WindowInsetsHelper.applySystemBarPadding(binding.root);

        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        TextInputUtils.clearErrorOnEdit(binding.inputName);
        TextInputUtils.clearErrorOnEdit(binding.inputEmail);
        TextInputUtils.clearErrorOnEdit(binding.inputPassword);
        TextInputUtils.clearErrorOnEdit(binding.inputConfirmation);

        binding.buttonRegister.setOnClickListener(v -> submit());
        binding.editConfirmation.setOnEditorActionListener((view, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                submit();
                return true;
            }
            return false;
        });
        binding.buttonGoLogin.setOnClickListener(v -> finish());

        viewModel.getFormErrors().observe(this, this::showFormErrors);
        viewModel.getAuthState().observe(this, this::render);
    }

    private void submit() {
        viewModel.register(
                TextInputUtils.textOf(binding.inputName),
                TextInputUtils.textOf(binding.inputEmail),
                TextInputUtils.textOf(binding.inputPassword),
                TextInputUtils.textOf(binding.inputConfirmation));
    }

    private void showFormErrors(AuthViewModel.FormErrors errors) {
        if (errors == null) {
            return;
        }
        TextInputUtils.setError(binding.inputName, errors.name);
        TextInputUtils.setError(binding.inputEmail, errors.email);
        TextInputUtils.setError(binding.inputPassword, errors.password);
        TextInputUtils.setError(binding.inputConfirmation, errors.confirmation);
        viewModel.onFormErrorsShown();
    }

    private void render(Resource<FirebaseUser> state) {
        boolean loading = state.getStatus() == Resource.Status.LOADING;
        binding.progress.setVisibility(loading ? View.VISIBLE : View.GONE);
        binding.buttonRegister.setEnabled(!loading);
        binding.buttonGoLogin.setEnabled(!loading);

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
