package com.example.proyectoandroid.data.model;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ChatTest {

    @Test
    public void buildChatId_noDependeDelOrden() {
        assertEquals(Chat.buildChatId("abc", "xyz"), Chat.buildChatId("xyz", "abc"));
    }

    @Test
    public void buildChatId_unePorGuionBajo() {
        assertEquals("abc_xyz", Chat.buildChatId("xyz", "abc"));
    }
}
