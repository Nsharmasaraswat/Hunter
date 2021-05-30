import React from 'react';
import ReactDOM from 'react-dom';
import AppWrapper from './AppWrapper';
import {HashRouter} from 'react-router-dom';
import 'core-js/stable';
import 'regenerator-runtime/runtime';
import 'prismjs/themes/prism-coy.css';
import "./i18nextInit";
import firebase from "firebase";

ReactDOM.render(
    <HashRouter>
        <AppWrapper/>
    </HashRouter>,
    document.getElementById('root')
);
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
    // console.log('accepted');
    messaging.getToken().then(
        token => {
            localStorage.setItem('ftk',token);
        }
    )
}).catch(console.log);
messaging.onMessage( payload => {
    try {  //try???
        // console.log('Message received. ', payload);

        const noteTitle = payload.notification.title;
        const noteOptions = {
            body: payload.notification.body,
            // icon: "typewriter.jpg", //this is my image in my public folder
        };

        // console.log("title ", noteTitle, " ", payload.notification.body);
        //var notification = //examples include this, seems not needed

        new Notification(noteTitle, noteOptions).onclick = function (event) {
            // console.log(event);
            // console.log(payload.notification.click_action);
            if(payload && payload.notification &&  payload.notification.click_action &&  payload.notification.click_action.length > 0)
            {
                window.open(payload.notification.click_action, '_blank');
            }
            this.close();
        };
    }
    catch (err) {
      //console.log('Caught error: ', err);
    }
});



