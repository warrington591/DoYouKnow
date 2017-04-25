package com.bloomfield.warrington.doyouknow;

import android.app.Application;
import android.content.Context;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by Warrington on 4/22/17.
 */

public class GlobalApplication extends Application {
    static Context globalContext;

    @Override
    public void onCreate() {
        super.onCreate();
        globalContext = getApplicationContext();
        if(!FirebaseApp.getApps(this).isEmpty()) {
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        }
    }

    public static Context globalContext(){
        return globalContext ;
    }
}
