/* EForm - Electronic Form System
 *
 * Copyright (C) 2013 Wu Xiaohu. All rights reserved.
 * Copyright (C) 2013 Cansiny Trade Co.,Ltd. All rights reserved.
 */
package com.cansiny.eform;

import java.util.HashMap;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;

class TTSUtteranceProgressListener extends UtteranceProgressListener
{
    @Override
    public void onStart(String utteranceId) {
	LogActivity.writeLog("开始发声");
    }
    
    @Override
    public void onDone(String utteranceId) {
	LogActivity.writeLog("结束发声");
    }

    @Override
    public void onError(String utteranceId) {
	LogActivity.writeLog("发声错误");
    }
}


public class EFormApplication extends Application
	implements TextToSpeech.OnInitListener
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

    private int udisk_count = 0;
    private TextToSpeech tts = null;
    private TTSUtteranceProgressListener utterance_listener = null;
    private boolean tts_ready = false;

    public void setUDiskCount(int count) {
	udisk_count = count;
    }

    public int getUDiskCount() {
	return udisk_count;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        application = this;

        Preferences prefs = Preferences.getPreferences();
        if (prefs.getVoiceTips()) {
            startTTS();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration new_config) {
	super.onConfigurationChanged(new_config);
    }


    @SuppressLint("NewApi")
    public void startTTS() {
	stopTTS();

	tts = new TextToSpeech(this, this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            utterance_listener = new TTSUtteranceProgressListener();
            tts.setOnUtteranceProgressListener(utterance_listener);
        }
    }

    public void stopTTS() {
	if (tts != null) {
	    tts.shutdown();
	    tts_ready = false;
	}
    }

    @Override
    public void onInit(int status) {
	tts_ready = true;
	speak("欢迎光临");
    }

    public void speak(String text) {
	if (!tts_ready || tts == null) {
	    LogActivity.writeLog("TTS 还未准备好！");
	    return;
	}
	tts.stop();

	int retv;
	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
	    HashMap<String, String> params = new HashMap<String, String>();
	    params.put("utteranceId", text);
	    retv = tts.speak(text, TextToSpeech.QUEUE_FLUSH, params);
	} else {
	    retv = tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
	}
	if (retv != TextToSpeech.SUCCESS) {
	    LogActivity.writeLog("播放语音 “%s“ 失败", text);
	}
    }
}
