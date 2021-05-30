importScripts("https://www.gstatic.com/firebasejs/7.14.1/firebase-app.js");
importScripts("https://www.gstatic.com/firebasejs/7.14.1/firebase-messaging.js");

firebase.initializeApp({
  apiKey: "AIzaSyDsvcsyBgUP9zPHRK3mFSLVXD3BUK1YC_4",
  authDomain: "daym-demo.firebaseapp.com",
  projectId: "daym-demo",
  storageBucket: "daym-demo.appspot.com",
  messagingSenderId: "163941423949",
  appId: "1:163941423949:web:0d3336c9113a2dd637f504",
  measurementId: "G-8H76D1J2YH"
});
const messaging = firebase.messaging();
// messaging.requestPermission().then(r => {
// //console.log('accepted');
//   messaging.getToken().then(
//     token => console.log('token',token)
//   )
// });
