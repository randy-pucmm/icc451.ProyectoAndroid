package com.example.proyectoandroid.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class DateFormatterTest {

    private static final TimeZone UTC = TimeZone.getTimeZone("UTC");

    private static Date fecha(int anio, int mes, int dia, int hora, int min) {
        Calendar cal = Calendar.getInstance(UTC);
        cal.clear();
        cal.set(anio, mes - 1, dia, hora, min);
        return cal.getTime();
    }

    @Test
    public void mismoDia_muestraSoloHora() {
        Date ahora = fecha(2026, 9, 28, 20, 0);
        assertEquals("09:05", DateFormatter.format(fecha(2026, 9, 28, 9, 5), ahora, UTC));
    }

    @Test
    public void otroDia_muestraFechaYHora() {
        Date ahora = fecha(2026, 9, 29, 8, 0);
        assertEquals("28/09/2026 21:30", DateFormatter.format(fecha(2026, 9, 28, 21, 30), ahora, UTC));
    }

    @Test
    public void fechaNula_devuelveVacio() {
        assertEquals("", DateFormatter.format(null, new Date(), UTC));
    }
}
