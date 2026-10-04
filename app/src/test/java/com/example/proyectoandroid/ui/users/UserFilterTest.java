package com.example.proyectoandroid.ui.users;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import com.example.proyectoandroid.data.model.User;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class UserFilterTest {

    private List<User> users;

    @Before
    public void setUp() {
        users = Arrays.asList(
                new User("1", "José Pérez", "jose@correo.com"),
                new User("2", "Ana Gómez", "ana.gomez@ce.pucmm.edu.do"),
                new User("3", "Luis", null));
    }

    @Test
    public void consultaVaciaONula_devuelveLaMismaLista() {
        assertSame(users, UserFilter.filter(users, ""));
        assertSame(users, UserFilter.filter(users, "   "));
        assertSame(users, UserFilter.filter(users, null));
    }

    @Test
    public void filtraPorNombre_sinImportarMayusculasNiAcentos() {
        List<User> result = UserFilter.filter(users, "JOSE");
        assertEquals(1, result.size());
        assertEquals("1", result.get(0).getUid());

        assertEquals("1", UserFilter.filter(users, "pérez").get(0).getUid());
    }

    @Test
    public void filtraPorCorreo() {
        List<User> result = UserFilter.filter(users, "pucmm");
        assertEquals(1, result.size());
        assertEquals("2", result.get(0).getUid());
    }

    @Test
    public void usuarioSinCorreoNoRompeElFiltro() {
        List<User> result = UserFilter.filter(users, "luis");
        assertEquals(1, result.size());
        assertEquals("3", result.get(0).getUid());
    }

    @Test
    public void sinCoincidencias_devuelveListaVacia() {
        assertEquals(Collections.emptyList(), UserFilter.filter(users, "zzz"));
    }
}
