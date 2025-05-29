package com.example.waterconservationappblank;

import android.app.Application;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 *  firebaseInit is a custom application class used to initalize firebase services
 *  and connect to local emulators during app startup
 */

public class FirebaseInit extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // initialize firebase SDK for this application
        FirebaseApp.initializeApp(this);
        // Connect to Firebase Auth Emulator
        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.useEmulator("10.0.2.2", 9099);

        // Firestore Emulator (optional, if used)
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.useEmulator("10.0.2.2", 8080); //firebase emulators runs on port 8080
    }
}
