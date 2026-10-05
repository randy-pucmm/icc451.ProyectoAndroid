package com.example.proyectoandroid.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/** Formato de fecha y hora de los mensajes: solo hora si es de hoy, fecha completa si no. */
public final class DateFormatter {

    private static final String PATTERN_DIA = "yyyyMMdd";
    private static final String PATTERN_HORA = "HH:mm";
    private static final String PATTERN_FECHA_HORA = "dd/MM/yyyy HH:mm";

    private DateFormatter() {
    }

    public static String format(Date date) {
        return format(date, new Date(), TimeZone.getDefault());
    }

    /** Variante con "ahora" y zona horaria explicitos para poder probarla. */
    static String format(Date date, Date now, TimeZone zone) {
        if (date == null) {
            return "";
        }
        SimpleDateFormat dia = new SimpleDateFormat(PATTERN_DIA, Locale.US);
        dia.setTimeZone(zone);
        boolean esHoy = dia.format(date).equals(dia.format(now));
        SimpleDateFormat salida = new SimpleDateFormat(esHoy ? PATTERN_HORA : PATTERN_FECHA_HORA, Locale.US);
        salida.setTimeZone(zone);
        return salida.format(date);
    }
}
