/* EForm - Electronic Form System
 *
 * Copyright (C) 2013 Wu Xiaohu. All rights reserved.
 * Copyright (C) 2013 Cansiny Trade Co.,Ltd. All rights reserved.
 */
package com.cansiny.eform;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;

public class EFormApplication extends Application
{
    private static EFormApplication application;

    public static EFormApplication getInstance() {
        return application;
    }

    public static Context getContext(){
        return application;
    }

    public static SharedPreferences getSharedPreferences() {
	return application.getSharedPreferences("settings", MODE_PRIVATE);
    }

    public static boolean printer_first_print = true;

    @Override
    public void onCreate() {
        super.onCreate();

        application = this;

        LogActivity.writeLog("EFormApplication onCreate 应用程序启动了");
    }

    @Override
    public void onConfigurationChanged(Configuration new_config) {
	super.onConfigurationChanged(new_config);
    }

    @Override
    public void onTerminate() {
	super.onTerminate();
    }

}
