package com.cansiny.eform;

import android.app.Application;
import android.content.Context;
import android.util.Log;

public class EFormApplication extends Application
{
    private static EFormApplication application;

    public static EFormApplication getInstance() {
        return application;
    }

    public static Context getContext(){
        return application;
    }

    @Override
    public void onCreate() {
    	Log.d("", "Application created");
    	application = this;
        super.onCreate();
    }
}
