# Notificador FCM

Script de Node que envía las notificaciones push durante la demo. Existe porque desplegar Cloud Functions (lo habitual para esto) exige el plan Blaze; el envío de FCM en sí es gratuito.

Qué hace: escucha todos los documentos nuevos de `chats/{chatId}/messages/{id}`, deduce el destinatario a partir del `chatId` (los dos uid ordenados y unidos con `_`), lee su `users/{uid}.fcmToken` y le manda un push.

## Requisitos

- Node.js 22 o superior.
- La clave de cuenta de servicio de Firebase (ver abajo).

## Configuración

1. En la consola de Firebase: **Configuración del proyecto → Cuentas de servicio → Generar nueva clave privada**.
2. Guarda el archivo descargado como `tools/notifier/serviceAccountKey.json`.

   **No lo subas a Git ni lo compartas:** da acceso de administrador a todo el proyecto. Ya está en `.gitignore`.
3. Instala las dependencias:

   ```bash
   cd tools/notifier
   npm install
   ```

## Uso

Déjalo corriendo en una laptop mientras haces la demo:

```bash
cd tools/notifier
npm start
```

Verás `Escuchando mensajes nuevos (...)`. Cada mensaje enviado desde la app imprime una línea `[enviado]`, `[omitido]` (el destinatario no tiene token) o `[token vencido]`.

## Notas

- Al arrancar ignora los mensajes que ya existían; solo notifica los nuevos.
- Lee todo el historial de mensajes al iniciar. Con el plan gratuito (50 000 lecturas al día) es suficiente para una demo.
- Si `npm install` avisa que bloqueó scripts de instalación (`@firebase/util`, `protobufjs`), se puede ignorar: no son necesarios para este script.
- El mensaje es solo de datos (`senderId`, `senderName`, `body`), así que `ChatMessagingService` lo recibe siempre y construye la notificación, con la app abierta o cerrada.
- El token se guarda al abrir la lista de usuarios y se borra al cerrar sesión. Si un usuario cerró sesión en su dispositivo, no recibe notificaciones.
