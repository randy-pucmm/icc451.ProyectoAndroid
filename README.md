# Chat Android con Firebase (ICC-451)

Aplicación de chat nativa para Android (Java, XML Views, MVVM) con Firebase: autenticación, mensajería en tiempo real en Firestore, imágenes y notificaciones push. Tema visual estilo WhatsApp, en claro y oscuro.

## Requisitos

- Android Studio reciente (JDK 11 o superior para compilar el proyecto).
- Un emulador o dispositivo con Android 7.0 (API 24) o superior y Google Play Services.
- Un proyecto de Firebase (plan Spark, gratuito).
- Node.js 22 o superior, solo para el notificador de push.

## Configuración de Firebase

1. Crea un proyecto en la [consola de Firebase](https://console.firebase.google.com/).
2. Registra una app **Android** con el paquete exacto `com.pucmm.proyectochat` (es el `applicationId`; el código Java sigue en `com.example.proyectoandroid`).
3. Descarga `google-services.json` y colócalo en `app/google-services.json`. El plugin `google-services` se activa solo cuando ese archivo existe; sin él el proyecto compila, pero la app no puede conectarse a Firebase.
4. **Authentication → Método de acceso:** habilita *Correo electrónico/contraseña*.
5. **Firestore Database:** crea la base de datos en modo producción.
6. Publica las reglas de [`firebase/firestore.rules`](firebase/firestore.rules) (pestaña *Reglas* de Firestore). Son la única copia: cubren `users`, `chats` y `messages`.
7. Opcional, solo si el proyecto tiene plan Blaze: crea *Storage* y publica [`firebase/storage.rules`](firebase/storage.rules).

## Cómo correr la app

1. Abre el proyecto en Android Studio y sincroniza Gradle.
2. Ejecuta la configuración `app` en un emulador o dispositivo.
3. Registra al menos dos usuarios (uno por dispositivo o emulador) para poder chatear.

Pruebas unitarias:

```bash
./gradlew testDebugUnitTest
```

## Flujo de pantallas

`Login` ⇄ `Registro` → `Usuarios` → `Chat`

`MainActivity` no tiene interfaz: al abrir la app lee la sesión que Firebase guardó en el dispositivo y abre `Usuarios` si hay sesión activa, o `Login` si no. La sesión se conserva hasta que el usuario cierra sesión desde el menú de `Usuarios`. Al tocar un usuario se abre su conversación con `ChatActivity.newIntent(...)`, la misma entrada que usa la notificación push.

## Arquitectura

MVVM con `LiveData<Resource<T>>`: las Activities solo pintan, los ViewModels validan y orquestan, y los Repositories son los únicos que hablan con Firebase.

| Capa | Clases |
|---|---|
| View | `LoginActivity`, `RegisterActivity`, `UsersActivity`, `UserAdapter`, `ChatActivity`, `MessageAdapter` |
| ViewModel | `AuthViewModel`, `UsersViewModel`, `ChatViewModel` (con `ChatViewModelFactory`) |
| Repository | `AuthRepository`, `UserRepository`, `ChatRepository`, `ImageRepository` |
| Utilidades | `Validator`, `AuthErrorMapper`, `DateFormatter`, `Presence`, `PresenceTracker`, `ActiveChat`, `ImagePicker`, `ImageCompressor` |
| Notificaciones | `ChatMessagingService`, `NotificationHelper` |

### Datos en Firestore

| Ruta | Campos |
|---|---|
| `users/{uid}` | `uid`, `displayName`, `email`, `fcmToken`, `createdAt`, y los opcionales `photoUrl`, `online`, `lastSeen` |
| `chats/{chatId}` | `participants[]`, `lastMessage`, `lastMessageAt` |
| `chats/{chatId}/messages/{id}` | `senderId`, `senderName`, `text`, `type` (`TEXT` o `IMAGE`), `imageUrl`, `timestamp` |

`chatId` = los dos uid ordenados y unidos con `_` (`Chat.buildChatId`). Enviar un mensaje escribe el mensaje y actualiza el documento del chat en un solo `WriteBatch`, y el historial se escucha con un `SnapshotListener` ordenado por `timestamp`.

## Imágenes: Storage o Base64

`ImageRepository.upload(Uri)` devuelve un `LiveData<Resource<String>>` con lo que se guarda en Firestore (`imageUrl` / `photoUrl`). El backend se elige con la constante `ImageRepository.BACKEND`:

- `BASE64` (por defecto): la imagen se reduce a un JPEG de hasta ~400 KB y se guarda como `data:image/jpeg;base64,...` dentro del documento (el límite de Firestore es 1 MiB por documento). Funciona sin plan Blaze.
- `STORAGE`: sube el archivo a Firebase Storage y devuelve la URL de descarga. Storage exige el plan Blaze; para probarlo sin pagar, activa `USE_STORAGE_EMULATOR` y arranca el *Storage Emulator* (`10.0.2.2:9199`, solo desde el emulador de Android).

Quien consume la cadena no distingue entre los dos casos: `Glide.with(view).load(cadena)` carga tanto una URL como un `data:` URI.

En el chat, el botón de clip abre `ImagePicker` (selector del sistema, sin permisos de almacenamiento); `ChatViewModel.sendImage(uri)` sube la imagen y, al terminar, la envía como mensaje de tipo `IMAGE`. En `UsersActivity`, tocar la cabecera cambia la foto de perfil con el mismo mecanismo.

## Notificaciones push

La app guarda su token FCM en `users/{uid}.fcmToken` al abrir la lista de usuarios y lo borra al cerrar sesión. Como desplegar Cloud Functions exige el plan Blaze, el envío lo hace un script de Node que corre en una laptop durante la demo: ver [`tools/notifier`](tools/notifier/README.md).

En Android 13 o superior la app pide el permiso de notificaciones al abrir `Usuarios`. Si llega un aviso de la persona cuyo chat ya está abierto no se muestra, y al abrir un chat se quita su notificación pendiente.

## Tema y colores

Paleta estilo WhatsApp definida solo con nombres semánticos en `res/values/colors.xml` (claro) y `res/values-night/colors.xml` (oscuro); el tema `Theme.ProyectoAndroid` y los layouts los usan, sin colores fijos. Para cambiar la paleta basta con editar esos dos archivos. En modo oscuro el chat usa el wallpaper garabateado; en claro, un fondo beige liso.

## Seguridad

No subas nunca a Git: `tools/notifier/serviceAccountKey.json` (clave de administrador de Firebase), `local.properties` ni claves o tokens. El `.gitignore` ya excluye estos archivos.
