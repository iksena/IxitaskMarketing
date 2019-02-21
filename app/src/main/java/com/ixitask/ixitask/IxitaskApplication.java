package com.ixitask.ixitask;

import android.app.Application;

import com.google.firebase.FirebaseApp;

public class IxitaskApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseApp.initializeApp(this);
    }
}
