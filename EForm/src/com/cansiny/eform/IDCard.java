/* EForm - Electronic Form System
 *
 * Copyright (C) 2013 Wu Xiaohu. All rights reserved.
 * Copyright (C) 2013 Cansiny Trade Co.,Ltd. All rights reserved.
 */
package com.cansiny.eform;

import java.math.BigInteger;
import java.security.SecureRandom;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


public abstract class IDCard extends Utils.DialogFragment
{
    static public final String USB_VID = "0x0001";
    static public final String USB_PID = "0x0002";

    static final private int LEAVE_START = 4;
    static final private int LEAVE_END   = 3;

    static public String formatCardno(CharSequence cardno) {
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

    static public IDCard getIDCard() {
	Preferences prefs = Preferences.getPreferences();

	String driver = prefs.getDeviceDriver("IDCard");
	if (driver == null || driver.length() == 0) {
	    LogActivity.writeLog("身份证读卡器驱动未指定");
	    return null;
	}
	IDCard IDCard = null;

	if (driver.equalsIgnoreCase("virtual")) {
	    IDCard = new IDCardVirtual();
	} else if (driver.equalsIgnoreCase("usb")) {
	    try {
		IDCard = new IDCardUSB(prefs.getDeviceNameOrVid("IDCard"),
			prefs.getDevicePathOrPid("IDCard"));
	    } catch (Exception e) {
		LogActivity.writeLog(e);
		return null;
	    }
	} else if (driver.equalsIgnoreCase("serial") ||
		driver.equalsIgnoreCase("usbserial")) {
	    IDCard = new IDCardSerial(prefs.getDevicePathOrPid("IDCard"));
	} else {
	    LogActivity.writeLog("不能识别的身份证读卡驱动: %s", driver);
	    return null;
	}
	return IDCard;
    }

    private int  totaltime = 30;
    private long starttime;
    private TextView timeview;
    private Handler handler;
    private Runnable runnable;
    private IDCardTask task;

    private View buildLayout() {
	LinearLayout layout = new LinearLayout(getActivity());
	layout.setOrientation(LinearLayout.VERTICAL);
	layout.setBackgroundResource(R.color.white);

	ImageView image = new ImageView(getActivity());
	image.setImageResource(R.drawable.read_idcard);
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

	timeview = new TextView(getActivity());
	timeview.setText("");
	timeview.setGravity(Gravity.CENTER_VERTICAL);
	timeview.setTextSize(TypedValue.COMPLEX_UNIT_SP, 28);
	timeview.setTextColor(getResources().getColor(R.color.red));
    	params = new LinearLayout.LayoutParams(
    		ViewGroup.LayoutParams.WRAP_CONTENT,
    		ViewGroup.LayoutParams.MATCH_PARENT);
    	params.leftMargin = 5;
    	params.rightMargin = 5;
	layout2.addView(timeview, params);

	view = new TextView(getActivity());
	view.setText("秒后自动终止");
	view.setGravity(Gravity.CENTER_VERTICAL);
	view.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
	layout2.addView(view, new LinearLayout.LayoutParams(
    		ViewGroup.LayoutParams.WRAP_CONTENT,
    		ViewGroup.LayoutParams.MATCH_PARENT));

	view = new TextView(getActivity());
	view.setText("读取的身份证信息仅用来填写本凭条相关字段。");
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
    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);

	handler = new Handler();
	runnable = new Runnable() {
	    @Override
	    public void run() {
		long currtime = System.currentTimeMillis();
		totaltime -= (int) ((currtime - starttime) / 1000);
		if (totaltime <= 0) {
		    if (task != null) {
			task.cancel(true);
		    }
		    dismiss();
		} else {
		    starttime = currtime;
		    timeview.setText("" + totaltime);
		    handler.postDelayed(this, 1000);
		}
	    }
	};
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
	super.onCreateDialog(savedInstanceState);

	AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
	builder.setTitle("请将身份证放在感应区域");
	builder.setView(buildLayout());
	builder.setNegativeButton("取 消", new DialogInterface.OnClickListener() {
	    @Override
	    public void onClick(DialogInterface dialog, int which) {
		if (task != null) {
		    task.cancel(true);
		}
	    }
	});
	return builder.create();
    }

    @Override
    public void onStart() {
	super.onStart();

	setCancelable(false);

	timeview.setText("" + totaltime);
	starttime = System.currentTimeMillis();
	handler.postDelayed(runnable, 0);
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
	super.onDismiss(dialog);
	handler.removeCallbacks(runnable);
    }

    abstract protected String read();
    abstract protected void cancel();

    public void read(FragmentManager manager, TextView textview) {
	task = new IDCardTask(manager, textview);
	task.execute();
    }

    public class IDCardTask extends AsyncTask<Void, Void, CharSequence>
    {
	private FragmentManager manager;
	private TextView textview;

	public IDCardTask(FragmentManager manager, TextView textview) {
	    this.manager = manager;
	    this.textview = textview;
	}

	@Override
	protected CharSequence doInBackground(Void... args) {
	    return read();
	}

	@Override
	protected void onPreExecute() {
	    show(manager, "IDCard");
	}

	@Override
	protected void onPostExecute(CharSequence cardno) {
	    if (cardno != null) {
		if (!(textview instanceof TextView)) {
		    LogActivity.writeLog("IDCardTask 参数必须是 TextView 实例");
		} else {
		    textview.setText(cardno);
		}
	    } else {
		Utils.showToast("读取卡号失败，请重试！", R.drawable.cry);
	    }
	    dismiss();
	}
	
	@Override
	protected void onCancelled(CharSequence cardno) {
	    Utils.showToast("操作被取消 ...");
	    dismiss();
	}
    }

    public class IDCardInfo
    {
	public String name;
	public boolean sex;
	public String nation;
    }
}

class IDCardVirtual extends IDCard
{
    public IDCardVirtual() {
    }

    @Override
    protected String read() {
	try {
	    Thread.sleep(3000);
	} catch (InterruptedException e) {
	    LogActivity.writeLog(e);
	}
	SecureRandom random = new SecureRandom();
	if (random.nextBoolean()) {
	    return new BigInteger(130, random).toString(10).substring(0, 19);
	} else {
	    return null;
	}
    }

    @Override
    protected void cancel() {
	LogActivity.writeLog("操作被取消");
    }
}

class IDCardSerial extends IDCard
{
    private String path;

    public IDCardSerial(String path) {
	this.path = path;
    }

    @Override
    protected String read() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    protected void cancel() {
	// TODO Auto-generated method stub
    }
}

class IDCardUSB extends IDCard
{
    private int vid = 0;
    private int pid = 0;

    public IDCardUSB(String vid, String pid) throws NumberFormatException {
	this.vid = Integer.decode(vid);
	this.pid = Integer.decode(pid);
    }

    @Override
    protected String read() {
	if (vid == 0 || pid == 0) {
	    LogActivity.writeLog("不能得到读卡器厂商ID和产品ID");
	    return null;
	}
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    protected void cancel() {
    }
}
