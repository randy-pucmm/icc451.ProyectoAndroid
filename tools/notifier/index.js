'use strict';

const path = require('path');
const { initializeApp, cert } = require('firebase-admin/app');
const { getFirestore, FieldValue } = require('firebase-admin/firestore');
const { getMessaging } = require('firebase-admin/messaging');

const KEY_PATH = path.join(__dirname, 'serviceAccountKey.json');
const MAX_BODY_LENGTH = 200;
const STALE_TOKEN_ERROR = 'messaging/registration-token-not-registered';

function loadServiceAccount() {
  try {
    return require(KEY_PATH);
  } catch (error) {
    console.error(`No se pudo leer ${KEY_PATH}`);
    console.error('Descargala en Firebase > Configuracion del proyecto > Cuentas de servicio > Generar nueva clave privada.');
    process.exit(1);
  }
}

initializeApp({ credential: cert(loadServiceAccount()) });
const db = getFirestore();
const messaging = getMessaging();

/** Texto que se muestra en la notificacion segun el tipo de mensaje. */
function bodyOf(message) {
  if (message.type === 'IMAGE') {
    return 'Imagen';
  }
  const text = String(message.text || '');
  return text.length > MAX_BODY_LENGTH ? `${text.slice(0, MAX_BODY_LENGTH)}...` : text;
}

async function notifyRecipient(doc) {
  const message = doc.data();
  const chatId = doc.ref.parent.parent.id;
  // chatId = uid de los dos participantes ordenados y unidos con "_" (Chat.buildChatId).
  const recipientUid = chatId.split('_').find((uid) => uid !== message.senderId);
  if (!recipientUid) {
    return;
  }

  const userRef = db.collection('users').doc(recipientUid);
  const token = (await userRef.get()).get('fcmToken');
  if (!token) {
    console.log(`[omitido] ${recipientUid} no tiene token FCM (sesion cerrada o sin permiso)`);
    return;
  }

  try {
    await messaging.send({
      token,
      data: {
        senderId: String(message.senderId || ''),
        senderName: String(message.senderName || ''),
        body: bodyOf(message),
      },
      android: { priority: 'high' },
    });
    console.log(`[enviado] ${message.senderName} -> ${recipientUid}`);
  } catch (error) {
    if (error.code === STALE_TOKEN_ERROR) {
      console.log(`[token vencido] se borra el de ${recipientUid}`);
      await userRef.update({ fcmToken: FieldValue.delete() });
    } else {
      throw error;
    }
  }
}

// El primer snapshot trae el historial completo: se ignora para no reenviar mensajes viejos.
let isInitialSnapshot = true;

db.collectionGroup('messages').onSnapshot(
  (snapshot) => {
    if (isInitialSnapshot) {
      isInitialSnapshot = false;
      console.log(`Escuchando mensajes nuevos (${snapshot.size} existentes ignorados). Ctrl+C para salir.`);
      return;
    }
    snapshot.docChanges()
      .filter((change) => change.type === 'added')
      .forEach((change) => {
        notifyRecipient(change.doc).catch((error) => {
          console.error(`[error] ${change.doc.ref.path}: ${error.message}`);
        });
      });
  },
  (error) => {
    console.error(`No se pudo escuchar Firestore: ${error.message}`);
    process.exit(1);
  }
);
