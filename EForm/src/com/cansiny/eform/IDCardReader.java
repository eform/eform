/* EForm - Electronic Form System
 *
 * Copyright (C) 2013 Wu Xiaohu. All rights reserved.
 * Copyright (C) 2013 Cansiny Trade Co.,Ltd. All rights reserved.
 */
package com.cansiny.eform;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.widget.TextView;

class IDCardReaderDialog extends Utils.DialogFragment
{
    private int  total_seconds = 30;
    private long starttime;

    private Handler  handler = new Handler();
    private Runnable runable = new Runnable() {
	    @Override
	    public void run() {
		long currtime = System.currentTimeMillis();
		long millis = currtime - starttime;
		starttime = currtime;
		int seconds = (int) (millis / 1000);

		total_seconds -= seconds;
		if (total_seconds <= 0) {
		    dismiss();
		    return;
		}
			
		TextView textview = (TextView) getDialog().findViewById(R.id.second_textview);
		textview.setText("" + total_seconds);
			
		handler.postDelayed(this, 1000);
	    }
	};

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
	super.onCreateDialog(savedInstanceState);

	AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
	builder.setTitle("请将“身份证”放在感应区域");
	LayoutInflater inflater = getActivity().getLayoutInflater();
	builder.setView(inflater.inflate(R.layout.dialog_idcard, null));
	builder.setNegativeButton("取 消", new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int which) {
		}
	    });
	return builder.create();
    }
	
    @Override
    public void onStart() {
	super.onStart();

	starttime = System.currentTimeMillis();
	TextView textview = (TextView) getDialog().findViewById(R.id.second_textview);
	textview.setText("" + total_seconds);
	handler.postDelayed(runable, 0);
    }
	
    @Override
    public void onDismiss (DialogInterface dialog) {
	super.onDismiss(dialog);
	handler.removeCallbacks(runable);
    }
}


public class IDCardReader extends Utils.DialogFragment
{
    private int  total_seconds = 30;
    private long starttime;

    private Handler  handler = new Handler();
    private Runnable runable = new Runnable() {
	    @Override
	    public void run() {
		long currtime = System.currentTimeMillis();
		long millis = currtime - starttime;
		starttime = currtime;
		int seconds = (int) (millis / 1000);

		total_seconds -= seconds;
		if (total_seconds <= 0) {
		    dismiss();
		    return;
		}
			
		TextView textview = (TextView) getDialog().findViewById(R.id.second_textview);
		textview.setText("" + total_seconds);
			
		handler.postDelayed(this, 1000);
	    }
	};

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
	super.onCreateDialog(savedInstanceState);

	AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
	builder.setTitle("请将“身份证”放在感应区域");
	LayoutInflater inflater = getActivity().getLayoutInflater();
	builder.setView(inflater.inflate(R.layout.dialog_idcard, null));
	builder.setNegativeButton("取 消", new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int which) {
		}
	    });
	return builder.create();
    }

    @Override
    public void onStart() {
	super.onStart();

	starttime = System.currentTimeMillis();
	TextView textview = (TextView) getDialog().findViewById(R.id.second_textview);
	textview.setText("" + total_seconds);
	handler.postDelayed(runable, 0);
    }

    public IDCardInfo getCardInfo() {
	IDCardInfo info = new IDCardInfo();
	return info;
    }

    public class IDCardInfo
    {
	public String name;
	public boolean sex;
	public String nation;
    }
}
