/* EForm - Electronic Form System
 *
 * Copyright (C) 2013 Wu Xiaohu. All rights reserved.
 * Copyright (C) 2013 Cansiny Trade Co.,Ltd. All rights reserved.
 */
package com.cansiny.eform;

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
    static final private int LEAVE_START = 3;
    static final private int LEAVE_END   = 4;

    static public String formatIdno(CharSequence idno) {
	StringBuilder builder = new StringBuilder();
	int length = idno.length();

	for (int i = 0; i < length; i++) {
	    if (i < LEAVE_START || i >= length - LEAVE_END)
		builder.append(idno.charAt(i));
	    else
		builder.append('*');

	    if ((i + 1) % 4 == 0)
		builder.append(' ');
	}
	return builder.toString();
    }

    static public IDCard getIDCard() {
//	Preferences prefs = Preferences.getPreferences();
//
//	String driver = prefs.getDeviceDriver("IDCard");
//	if (driver == null || driver.length() == 0) {
//	    LogActivity.writeLog("身份证阅读器驱动未指定");
//	    return null;
//	}
//
//	if (driver.equalsIgnoreCase("virtual")) {
//	    return new IDCardVirtual();
//	}
//	if (driver.equalsIgnoreCase("usb")) {
//	    try {
//		int vid = Integer.decode(prefs.getDeviceNameOrVid("IDCard"));
//		int pid = Integer.decode(prefs.getDevicePathOrPid("IDCard"));
//
//		if (vid == IDCardUSBGTICR100.VID && pid == IDCardUSBGTICR100.PID) {
//		    return new IDCardUSBGTICR100();
//		}
//		LogActivity.writeLog("不能找到厂商ID为%s和产品ID为%s的驱动程序",
//			prefs.getDeviceNameOrVid("IDCard"),
//			prefs.getDevicePathOrPid("IDCard"));
//		return null;
//	    } catch (NumberFormatException e) {
//		LogActivity.writeLog(e);
//		return null;
//	    }
//	}
//	if (driver.equalsIgnoreCase("serial") || driver.equalsIgnoreCase("usbserial")) {
//	    return new IDCardSerial(prefs.getDevicePathOrPid("IDCard"));
//	}
//	LogActivity.writeLog("不能识别的身份证阅读器驱动: %s", driver);
	return null;
    }

    private int  totaltime = 30;
    private long starttime;
    private TextView timeview;
    private Handler handler;
    private Runnable runnable;
    private IDCardTask task;
    private IDCardListener listener;
    static private int error_count = 0;

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
		cancel();
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

    abstract protected IDCardInfo read();
    abstract protected void cancel();

    public void setListener(IDCardListener listener) {
	this.listener = listener;
    }

    public void read(FragmentManager manager) {
	task = new IDCardTask(manager);
	task.execute();
    }

    static public class IDCardInfo
    {
	public String name;
	public byte gender;
	public String idno;
	public String grant_dept;
	public String due_year;
	public String due_month;
	public String due_day;
	public String born_year;
	public String born_month;
	public String born_day;
	public String address;
	public String nation;
	public String nationality;
    }

    public interface IDCardListener
    {
	public void onIDCardRead(IDCard IDCard, IDCardInfo info);
    }

    public class IDCardTask extends AsyncTask<Void, Void, IDCardInfo>
    {
	private FragmentManager manager;

	public IDCardTask(FragmentManager manager) {
	    this.manager = manager;
	}

	@Override
	protected IDCardInfo doInBackground(Void... args) {
	    return read();
	}

	@Override
	protected void onPreExecute() {
	    show(manager, "IDCard");
	}

	@Override
	protected void onPostExecute(IDCardInfo info) {
	    if (info != null) {
		if (listener != null) {
		    listener.onIDCardRead(IDCard.this, info);
		}
		IDCard.error_count = 0;
	    } else {
		if (++IDCard.error_count >= 3) {
		    Utils.showToast("已经连续 " + IDCard.error_count + " 次读身份证失败，"
			    + "请联系管理员检查设备配置", R.drawable.cry);
		} else {
		    Utils.showToast("读取身份证信息失败，请重试！", R.drawable.cry);
		}
	    }
	    dismiss();
	}
	
	@Override
	protected void onCancelled(IDCardInfo info) {
	    Utils.showToast("操作被取消 ...");
	    dismiss();
	}
    }

}

class IDCardVirtual extends IDCard
{
    public IDCardVirtual() {
    }

    @Override
    protected IDCardInfo read() {
	try {
	    Thread.sleep(3000);
	} catch (InterruptedException e) {
	    LogActivity.writeLog(e);
	}

	SecureRandom random = new SecureRandom();
	if (random.nextBoolean()) {
	    IDCardInfo info = new IDCardInfo();
	    info.name = "吴小虎";
	    info.gender = 1;
	    info.address = "安徽省庐阳区濉溪路万豪广场";
	    info.born_year = "1980";
	    info.born_month = "5";
	    info.born_day = "30";
	    info.due_year = "2020";
	    info.due_month = "12";
	    info.due_day = "20";
	    info.idno = "429005198005300614";
	    info.grant_dept = "湖北省公安局";
	    info.nation = "汉";
	    info.nationality = "中国";
	    return info;
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
    protected IDCardInfo read() {
	if (path == null) {
	    LogActivity.writeLog("不能得到身份证阅读器设备路径");
	    return null;
	}
	return null;
    }

    @Override
    protected void cancel() {
	// TODO Auto-generated method stub
    }
}

class IDCardUSBGTICR100 extends IDCard
{
    public static final int VID = 0x2001;
    public static final int PID = 0x2002;

    public IDCardUSBGTICR100() {
    }
    
    @Override
    protected IDCardInfo read() {
	return null;
    }

    @Override
    protected void cancel() {
    }
}
