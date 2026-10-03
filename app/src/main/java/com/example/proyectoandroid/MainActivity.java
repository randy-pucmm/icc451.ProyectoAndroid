package com.example.proyectoandroid;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.proyectoandroid.ui.auth.AuthViewModel;
import com.example.proyectoandroid.ui.auth.LoginActivity;
import com.example.proyectoandroid.ui.users.UsersActivity;

/** Punto de entrada sin interfaz: decide si se abre la lista de usuarios (sesion activa) o el login. */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AuthViewModel viewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        Class<?> destination = viewModel.isLoggedIn() ? UsersActivity.class : LoginActivity.class;
        startActivity(new Intent(this, destination));
        finish();
    }
}
