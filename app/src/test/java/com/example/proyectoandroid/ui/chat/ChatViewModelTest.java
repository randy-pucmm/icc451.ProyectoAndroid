package com.example.proyectoandroid.ui.chat;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class ChatViewModelTest {

    @Test
    public void normalize_textoNulo_devuelveNull() {
        assertNull(ChatViewModel.normalize(null));
    }

    @Test
    public void normalize_soloEspacios_devuelveNull() {
        assertNull(ChatViewModel.normalize("   \n\t "));
    }

    @Test
    public void normalize_recortaEspaciosDeLosBordes() {
        assertEquals("hola mundo", ChatViewModel.normalize("  hola mundo \n"));
    }
}
