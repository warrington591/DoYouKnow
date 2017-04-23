package com.bloomfield.warrington.doyouknow;

import android.app.Application;
import android.content.Context;

/**
 * Created by Warrington on 4/22/17.
 */

public class GlobalApplication extends Application {
    static Context globalContext;

    @Override
    public void onCreate() {
        super.onCreate();
        globalContext = getBaseContext();
    }

    public Context getBaseContext(){
        return globalContext;
    }
}
