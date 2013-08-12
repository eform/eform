/* EForm - Electronic Form System
 *
 * Copyright (C) 2013 Wu Xiaohu. All rights reserved.
 * Copyright (C) 2013 Cansiny Trade Co.,Ltd. All rights reserved.
 */
package com.cansiny.eform;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


public class MagcardReader extends Utils.DialogFragment
{
    static final private int LEAVE_START = 4;
    static final private int LEAVE_END   = 3;

    static String formatCardno(CharSequence cardno) {
	StringBuilder builder = new StringBuilder();
	int length = cardno.length();

	for (int i = 0; i < length; i++) {
	    if (i < LEAVE_START || i >= length - LEAVE_END)
		builder.append(cardno.charAt(i));
	    else
		builder.append('*');

	    if ((i + 1) % 4 == 0)
		builder.append(' ');
	}
	return builder.toString();
    }


    private int  total_seconds = 30;
    private long starttime;
    private TextView seconds_textview;
    private Activity activity;
    private Handler handler;
    private Runnable runnable;

    @Override
    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);

	handler = new Handler();
	runnable = new Runnable() {
	    @Override
	    public void run() {
		long currtime = System.currentTimeMillis();
		total_seconds -= (int) ((currtime - starttime) / 1000);
		if (total_seconds <= 0) {
		    dismiss();
		    return;
		}
		starttime = currtime;

		seconds_textview.setText("" + total_seconds);

		handler.postDelayed(this, 1000);
	    }
	};
    }

    private LinearLayout buildLayout() {
	LinearLayout layout = new LinearLayout(getActivity());
	layout.setOrientation(LinearLayout.VERTICAL);
	layout.setBackgroundResource(R.color.white);

	ImageView image = new ImageView(getActivity());
	image.setImageResource(R.drawable.swipe_card);
	image.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
	LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(300, 200);
	params.gravity = Gravity.CENTER_HORIZONTAL;
	layout.addView(image, params);

	LinearLayout layout2 = new LinearLayout(getActivity());
	layout2.setOrientation(LinearLayout.HORIZONTAL);
    	params = new LinearLayout.LayoutParams(
    		ViewGroup.LayoutParams.WRAP_CONTENT,
    		ViewGroup.LayoutParams.WRAP_CONTENT);
    	params.gravity = Gravity.CENTER_HORIZONTAL;
	layout.addView(layout2, params);

	TextView view = new TextView(getActivity());
	view.setText("操作将在");
	view.setGravity(Gravity.CENTER_VERTICAL);
	view.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
	layout2.addView(view, new LinearLayout.LayoutParams(
    		ViewGroup.LayoutParams.WRAP_CONTENT,
    		ViewGroup.LayoutParams.MATCH_PARENT));

	seconds_textview = new TextView(getActivity());
	seconds_textview.setText("");
	seconds_textview.setGravity(Gravity.CENTER_VERTICAL);
	seconds_textview.setTextSize(TypedValue.COMPLEX_UNIT_SP, 28);
	seconds_textview.setTextColor(getResources().getColor(R.color.red));
    	params = new LinearLayout.LayoutParams(
    		ViewGroup.LayoutParams.WRAP_CONTENT,
    		ViewGroup.LayoutParams.MATCH_PARENT);
    	params.leftMargin = 5;
    	params.rightMargin = 5;
	layout2.addView(seconds_textview, params);

	view = new TextView(getActivity());
	view.setText("秒后自动终止");
	view.setGravity(Gravity.CENTER_VERTICAL);
	view.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
	layout2.addView(view, new LinearLayout.LayoutParams(
    		ViewGroup.LayoutParams.WRAP_CONTENT,
    		ViewGroup.LayoutParams.MATCH_PARENT));

	view = new TextView(getActivity());
	view.setText("读取的卡号仅用来填写本页相关要素，不作他用。");
	view.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
    	params = new LinearLayout.LayoutParams(
    		ViewGroup.LayoutParams.WRAP_CONTENT,
    		ViewGroup.LayoutParams.MATCH_PARENT);
    	params.leftMargin = 10;
    	params.topMargin = 8;
    	params.bottomMargin = 2;
	layout.addView(view, params);

	return layout;
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
	super.onCreateDialog(savedInstanceState);

	AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
	builder.setTitle("请刷磁条卡或存折");
	builder.setView(buildLayout());
	builder.setNegativeButton("取 消", null);
	return builder.create();
    }
		
    @Override
    public void onStart() {
	super.onStart();

	setCancelable(false);

	seconds_textview.setText("" + total_seconds);
	starttime = System.currentTimeMillis();
	handler.postDelayed(runnable, 0);
    }
		
    @Override
    public void onDismiss(DialogInterface dialog) {
	super.onDismiss(dialog);
	handler.removeCallbacks(runnable);
    }


    public void readCardno(Activity activity, TextView textview) {
	this.activity = activity;
	new MagcardReaderTask().execute(textview);
    }

    
    public class MagcardReaderTask extends AsyncTask<TextView, Void, CharSequence>
    {
	private TextView textview;

	@Override
	protected CharSequence doInBackground(TextView... args) {
	    textview = args[0];
	    if (!(textview instanceof TextView)) {
		Log.e("MagcardReaderTask", "参数必须是 TextView 实例");
		return null;
	    }
	    try {
		Thread.sleep(2000);
	    } catch (InterruptedException e) {
		e.printStackTrace();
	    }
	    return "12345678901234567890";
	}

	protected void onPreExecute() {
	    show(activity.getFragmentManager(), "MagcardDialogFragment");
	}

	protected void onPostExecute(CharSequence cardno) {
	    if (cardno != null) {
		textview.setText(cardno);
		dismiss();
	    } else {
		
	    }
	}
    }

}
