importScripts("https://www.gstatic.com/firebasejs/7.14.1/firebase-app.js");
importScripts("https://www.gstatic.com/firebasejs/7.14.1/firebase-messaging.js");

firebase.initializeApp({
  apiKey: "AIzaSyCyMDrz7dQuCR_jmKghV1OQf4bmHpWLZL4",
  authDomain: "daym-217cc.firebaseapp.com",
  projectId: "daym-217cc",
  storageBucket: "daym-217cc.appspot.com",
  messagingSenderId: "644539427557",
  appId: "1:644539427557:web:4651ee7c6802279d18420c",
  measurementId: "G-HK1LK3YLNV"
});
const messaging = firebase.messaging();
messaging.requestPermission().then(r => {
//console.log('accepted');
  messaging.getToken().then(
    token => {
    //console.log('token', token);
      localStorage.setItem('ftk',token);
    }
  )
}).catch(console.log);
