/* EForm - Electronic Form System
 *
 * Copyright (C) 2013 Wu Xiaohu. All rights reserved.
 * Copyright (C) 2013 Cansiny Trade Co.,Ltd. All rights reserved.
 */
package com.cansiny.eform;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;


public class LogActivity extends Activity implements OnClickListener
{
    static final private String LOG_TAGNAME = "Log";

    static final private int BUTTON_TAG_CLOSE = 1;

    static private String log_filepath = 
	HomeActivity.getAppContext().getCacheDir().getAbsolutePath() + 
	File.separator + "eform.log";

    static public void writeLog(Exception e) {
	if (e == null) return;

	e.printStackTrace();

	try {
	    RandomAccessFile file = new RandomAccessFile(log_filepath, "rws");
	    StackTraceElement[] elems = e.getStackTrace();
	    String message = e.toString() + "\n";
	    for (StackTraceElement elem : elems)
		message += elem.toString() + "\n";
	    message += "\n------------------------\n\n";
	    if (file.length() > 0) {
		message += file.readUTF();
		file.seek(0);
	    }
	    file.writeUTF(message);
	    file.close();
	} catch (FileNotFoundException e1) {
	    e1.printStackTrace();
	    Log.e(LOG_TAGNAME, String.format("打开文件'%s'失败", log_filepath));
	} catch (IOException e2) {
	    e.printStackTrace();
	    Log.e(LOG_TAGNAME, String.format("读写文件'%s'失败", log_filepath));
	}
    }
	
    static public void writeLog(String format, Object... args) {
	if (format == null || format.trim().length() == 0)
	    return;

	String message = String.format(format, args);
	Log.e(LOG_TAGNAME, message);

	try {
	    RandomAccessFile file = new RandomAccessFile(log_filepath, "rws");
	    message += "\n\n------------------------\n\n";
	    if (file.length() > 0) {
		message += file.readUTF();
		file.seek(0);
	    }
	    file.writeUTF(message);
	    file.close();
	} catch (FileNotFoundException e) {
	    e.printStackTrace();
	    Log.e(LOG_TAGNAME, String.format("打开文件'%s'失败", log_filepath));
	} catch (IOException e) {
	    e.printStackTrace();
	    Log.e(LOG_TAGNAME, String.format("读写文件'%s'失败", log_filepath));
	}
    }

    static public String readLog() {
	try {
	    RandomAccessFile file = new RandomAccessFile(log_filepath, "r");
	    String result = file.readUTF();
	    file.close();
	    return result;
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


    private View buildLayout() {
	LinearLayout linear = new LinearLayout(this);
	linear.setBackgroundResource(R.color.black);
	linear.setPadding(2, 2, 2, 2);
	
	ScrollView scroll = new ScrollView(this);
	LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
		ViewGroup.LayoutParams.WRAP_CONTENT,
		ViewGroup.LayoutParams.MATCH_PARENT);
	params.weight = 1;
	linear.addView(scroll, params);
	
	EditText edittext = new EditText(this);
	edittext.setEnabled(false);
	edittext.setSingleLine(false);
	edittext.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
	String buffer = LogActivity.readLog();
	if (buffer != null) {
	    edittext.setText(buffer);
	}
	scroll.addView(edittext);
	
	Button button = new Button(this);
	button.setText("关 闭");
	button.setTag(BUTTON_TAG_CLOSE);
	button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
	button.setTextColor(getResources().getColor(R.color.yellow));
	button.setBackgroundResource(R.drawable.button);
	button.setOnClickListener(this);
	params = new LinearLayout.LayoutParams(
		ViewGroup.LayoutParams.WRAP_CONTENT,
		ViewGroup.LayoutParams.WRAP_CONTENT);
	params.topMargin = 20;
	params.leftMargin = 20;
	params.rightMargin = 20;
	linear.addView(button, params);

	return linear;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);

	setContentView(buildLayout());

	getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }


    @Override
    public void onClick(View view) {
	switch(((Integer) view.getTag()).intValue()) {
	case BUTTON_TAG_CLOSE:
	    finish();
	    break;
	}
    }
}
