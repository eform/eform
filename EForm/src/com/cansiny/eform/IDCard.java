/* EForm - Electronic Form System
 *
 * Copyright (C) 2013 Wu Xiaohu. All rights reserved.
 * Copyright (C) 2013 Cansiny Trade Co.,Ltd. All rights reserved.
 */
package com.cansiny.eform;

import java.security.SecureRandom;

import com.cansiny.eform.Utils.Device;

import android.app.Dialog;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


class IDCardDialog extends Device.DeviceDialog
{
    public IDCardDialog(Device device) {
	super(device);
    }

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
    public Dialog onCreateDialog(Bundle savedInstanceState) {
	super.onCreateDialog(savedInstanceState);

	builder.setTitle("请将身份证放在感应区域");
	builder.setView(buildLayout());

	return builder.create();
    }

    @Override
    public void onStart() {
	super.onStart();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
	super.onDismiss(dialog);
    }

}

public abstract class IDCard extends Utils.Device
{
    static final private int LEAVE_START = 3;
    static final private int LEAVE_END   = 4;

    static public String formatId(CharSequence idno) {
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

    static private int read_error_count = 0;
    protected IDCardTask task;

//    static public IDCard getIDCard() { return null; }

    @Override
    protected void cancel() {
	if (task != null && !task.isCancelled()) {
	    task.cancel(true);
	}
    }

    @Override
    public void read(FragmentManager manager) {
	if (!open()) {
	    Utils.showToast("打开身份证读卡器失败", R.drawable.cry);
	} else {
	    task = new IDCardTask(this, manager);
	    task.execute();
	}
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

    public class IDCardTask extends Device.Task<Void, Void, IDCardInfo>
    {
	private FragmentManager manager;
	IDCardDialog dialog;

	public IDCardTask(Device device, FragmentManager manager) {
	    super(device);
	    this.manager = manager;
	}

	@Override
	protected IDCardInfo doInBackground(Void... args) {
	    return (IDCardInfo) read();
	}

	@Override
	protected void onPreExecute() {
	    super.onPreExecute();
	    dialog = new IDCardDialog(IDCard.this);
	    dialog.show(manager, "IDCardDialog");
	}

	@Override
	protected void onPostExecute(IDCardInfo result) {
	    super.onPostExecute(result);
	    dialog.dismiss();
	    device.close();

	    if (result == null) {
		if (++IDCard.read_error_count >= 3) {
		    Utils.showToast("已经连续 " + IDCard.read_error_count +
			    " 次读身份证失败，请联系管理员检查设备配置", R.drawable.cry);
		} else {
		    Utils.showToast("读取身份证信息失败，请重试！", R.drawable.cry);
		}
	    } else {
		IDCard.read_error_count = 0;
	    }
	}
	
	@Override
	protected void onCancelled(IDCardInfo result) {
	    super.onCancelled(result);
	    dialog.dismiss();
	    device.close();
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
	    info.name = "小虎哥";
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

    @Override
    protected boolean open() {
	return true;
    }

    @Override
    protected void close() {
    }
}


class IDCardUSBGTICR100 extends IDCard
{
    public static final int VID = 0x2001;
    public static final int PID = 0x2002;

    public IDCardUSBGTICR100() {
    }

    @Override
    protected boolean open() {
	return false;
    }

    @Override
    protected void close() {
    }

    @Override
    protected void cancel() {
    }
    
    @Override
    protected IDCardInfo read() {
	return null;
    }
}
