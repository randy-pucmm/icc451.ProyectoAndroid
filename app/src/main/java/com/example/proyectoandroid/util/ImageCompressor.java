package com.example.proyectoandroid.util;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;

import androidx.annotation.NonNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Reduce una imagen elegida por el usuario a un JPEG liviano antes de subirla.
 * Es bloqueante: se debe llamar desde un hilo secundario.
 */
public final class ImageCompressor {

    /** Lado mayor para imagenes enviadas por el chat. */
    public static final int CHAT_MAX_DIMENSION = 1024;
    /** Lado mayor para fotos de perfil. */
    public static final int AVATAR_MAX_DIMENSION = 256;
    /**
     * Tamano maximo del JPEG. En Base64 ocupa un tercio mas (~530 KB), por debajo del limite
     * de 1 MiB de un documento de Firestore.
     */
    public static final int MAX_BYTES = 400_000;

    private static final int START_QUALITY = 85;
    private static final int MIN_QUALITY = 40;
    private static final int QUALITY_STEP = 10;
    private static final float SHRINK_FACTOR = 0.75f;
    private static final int MAX_SHRINK_ATTEMPTS = 4;

    private ImageCompressor() {
    }

    @NonNull
    public static byte[] compress(@NonNull ContentResolver resolver, @NonNull Uri uri, int maxDimension)
            throws IOException {
        int rotation = readRotation(resolver, uri);
        Bitmap decoded = decodeSampled(resolver, uri, maxDimension);
        Bitmap current = transform(decoded, rotation, maxDimension);

        try {
            for (int attempt = 0; attempt <= MAX_SHRINK_ATTEMPTS; attempt++) {
                byte[] bytes = encodeWithDecreasingQuality(current);
                if (bytes.length <= MAX_BYTES) {
                    return bytes;
                }
                // Ni con la calidad minima cabe: se reduce la resolucion y se reintenta.
                Bitmap smaller = Bitmap.createScaledBitmap(current,
                        Math.max(1, Math.round(current.getWidth() * SHRINK_FACTOR)),
                        Math.max(1, Math.round(current.getHeight() * SHRINK_FACTOR)), true);
                if (smaller != current) {
                    current.recycle();
                }
                current = smaller;
            }
            throw new IOException("La imagen es demasiado grande");
        } finally {
            current.recycle();
        }
    }

    private static byte[] encodeWithDecreasingQuality(Bitmap bitmap) {
        byte[] bytes = null;
        for (int quality = START_QUALITY; quality >= MIN_QUALITY; quality -= QUALITY_STEP) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, out);
            bytes = out.toByteArray();
            if (bytes.length <= MAX_BYTES) {
                break;
            }
        }
        return bytes;
    }

    private static Bitmap decodeSampled(ContentResolver resolver, Uri uri, int maxDimension) throws IOException {
        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        try (InputStream in = open(resolver, uri)) {
            BitmapFactory.decodeStream(in, null, bounds);
        }
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) {
            throw new IOException("No se pudo leer la imagen");
        }

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = sampleSizeFor(bounds.outWidth, bounds.outHeight, maxDimension);
        try (InputStream in = open(resolver, uri)) {
            Bitmap bitmap = BitmapFactory.decodeStream(in, null, options);
            if (bitmap == null) {
                throw new IOException("No se pudo leer la imagen");
            }
            return bitmap;
        }
    }

    /** Mayor potencia de 2 que mantiene el lado mayor por encima de {@code maxDimension}. */
    private static int sampleSizeFor(int width, int height, int maxDimension) {
        int sample = 1;
        int longest = Math.max(width, height);
        while (longest / (sample * 2) >= maxDimension) {
            sample *= 2;
        }
        return sample;
    }

    /** Aplica la rotacion EXIF y el escalado final; los PNG con transparencia se pintan sobre blanco. */
    private static Bitmap transform(Bitmap source, int rotation, int maxDimension) {
        Matrix matrix = new Matrix();
        if (rotation != 0) {
            matrix.postRotate(rotation);
        }
        int longest = Math.max(source.getWidth(), source.getHeight());
        if (longest > maxDimension) {
            float scale = (float) maxDimension / longest;
            matrix.postScale(scale, scale);
        }

        Bitmap result = Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
        if (result != source) {
            source.recycle();
        }
        if (!result.hasAlpha()) {
            return result;
        }
        Bitmap flattened = Bitmap.createBitmap(result.getWidth(), result.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(flattened);
        canvas.drawColor(Color.WHITE);
        canvas.drawBitmap(result, 0, 0, null);
        result.recycle();
        return flattened;
    }

    private static int readRotation(ContentResolver resolver, Uri uri) {
        try (InputStream in = open(resolver, uri)) {
            int orientation = new ExifInterface(in)
                    .getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    return 90;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    return 180;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    return 270;
                default:
                    return 0;
            }
        } catch (IOException | RuntimeException e) {
            return 0; // sin EXIF legible la imagen se usa tal cual
        }
    }

    private static InputStream open(ContentResolver resolver, Uri uri) throws IOException {
        InputStream in = resolver.openInputStream(uri);
        if (in == null) {
            throw new IOException("No se pudo abrir la imagen");
        }
        return in;
    }
}
