/* EForm - Electronic Form System
 *
 * Copyright (C) 2013 Wu Xiaohu. All rights reserved.
 * Copyright (C) 2013 Cansiny Trade Co.,Ltd. All rights reserved.
 * 
 * HomeActivity - The application entry
 *
 * Authors:
 *   Xiaohu <xiaohu417@gmail.com>, 2013.6.11, hefei
 */
package com.cansiny.eform;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;

public class LogActivity extends Activity
{
    static final private String LOG_TAGNAME = "Log";

    static private String log_filepath = 
	HomeActivity.getAppContext().getCacheDir().getAbsolutePath() + 
	File.separator + "eform.log";

    static public void writeLog(Exception e) {
	if (e == null)
	    return;

	e.printStackTrace();

	try {
	    StackTraceElement[] elems = e.getStackTrace();
	    RandomAccessFile file = new RandomAccessFile(log_filepath, "rws");
	    byte[] text = null;
	    if (file.length() > 0) {
		text = new byte[(int) file.length()];
		file.readFully(text);
		file.seek(0);
	    }
	    file.writeBytes(e.toString() + "\n");
	    for (StackTraceElement elem : elems)
		file.writeBytes(elem.toString() + "\n");
	    file.writeBytes("\n------------------------\n\n");
	    if (text != null) {
		file.write(text);
	    }
	    file.close();
	} catch (FileNotFoundException e1) {
	    e1.printStackTrace();
	    Log.e(LOG_TAGNAME,
		  String.format("Cannot open '%s' for write", log_filepath));
	} catch (IOException e2) {
	    e.printStackTrace();
	    Log.e(LOG_TAGNAME,
		  String.format("Write to '%s' failed", log_filepath));
	}
    }
	
    static public void writeLog(String format, Object... args) {
	if (format == null || format.trim().length() == 0)
	    return;

	String message = String.format(format, args);
	Log.e(LOG_TAGNAME, message);

	try {
	    RandomAccessFile file = new RandomAccessFile(log_filepath, "rws");
	    byte[] text = null;
	    if (file.length() > 0) {
		text = new byte[(int) file.length()];
		file.readFully(text);
		file.seek(0);
	    }
	    file.writeBytes(message + "\n");
	    file.writeBytes("\n------------------------\n\n");
	    if (text != null) {
		file.write(text);
	    }
	    file.close();
	} catch (FileNotFoundException e) {
	    e.printStackTrace();
	    Log.e(LOG_TAGNAME,
		  String.format("Cannot open '%s' for write", log_filepath));
	} catch (IOException e) {
	    e.printStackTrace();
	    Log.e(LOG_TAGNAME,
		  String.format("Write to '%s' failed", log_filepath));
	}
    }

    static public String readLog() {
	try {
	    StringBuilder contents = new StringBuilder();
	    FileInputStream stream = new FileInputStream(log_filepath);
	    byte[] buffer = new byte[1024];
	    while (true) {
		int count = stream.read(buffer);
		if (count == -1)
		    break;
		contents.append(new String(buffer, 0, count));
	    }
	    stream.close();
	    return contents.toString();
	} catch (FileNotFoundException e) {
	    Log.e(LOG_TAGNAME,
		  String.format("Cannot open '%s' for read", log_filepath));
	    e.printStackTrace();
	    return null;
	} catch (IOException e) {
	    Log.e(LOG_TAGNAME,
		  String.format("Read from '%s' failed", log_filepath));
	    e.printStackTrace();
	    return null;
	}
    }

    static public void clearLog() {
	try {
	    FileOutputStream stream;
	    stream = new FileOutputStream(log_filepath, false);
	    stream.close();
	} catch (FileNotFoundException e) {
	    e.printStackTrace();
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }

	
    @Override
    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
		
	setContentView(R.layout.activity_log);

	String buffer = LogActivity.readLog();
	if (buffer != null) {
	    EditText edit = (EditText) findViewById(R.id.log_text);
	    edit.setText(buffer);
	}
	/* hide the soft keyboard until user touch the edit text */
	getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    public void onCloseButtonClicked(View view) {
	finish();
    }
}
