/* EForm - Electronic Form System
 *
 * Copyright (C) 2013 Wu Xiaohu. All rights reserved.
 * Copyright (C) 2013 Cansiny Trade Co.,Ltd. All rights reserved.
 */
package com.cansiny.eform;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.hardware.usb.UsbRequest;
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


public abstract class Magcard extends Utils.DialogFragment
{
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

    static public Magcard getMagcard() {
	Preferences prefs = Preferences.getPreferences();

	String driver = prefs.getDeviceDriver("Magcard");
	if (driver == null || driver.length() == 0) {
	    LogActivity.writeLog("磁条卡读卡器驱动未指定");
	    return null;
	}

	if (driver.equalsIgnoreCase("virtual")) {
	    return new MagcardVirtual();
	}
	if (driver.equalsIgnoreCase("usb")) {
	    try {
		int vid = Integer.decode(prefs.getDeviceNameOrVid("Magcard"));
		int pid = Integer.decode(prefs.getDevicePathOrPid("Magcard"));

		if (vid == MagcardUSBWBT1372.VID && pid == MagcardUSBWBT1372.PID) {
		    return new MagcardUSBWBT1372();
		}
		LogActivity.writeLog("不能找到厂商ID为%s和产品ID为%s的磁条卡驱动程序",
			prefs.getDeviceNameOrVid("Magcard"),
			prefs.getDevicePathOrPid("Magcard"));
		return null;
	    } catch (NumberFormatException e) {
		LogActivity.writeLog(e);
		return null;
	    }
	}
	if (driver.equalsIgnoreCase("serial") || driver.equalsIgnoreCase("usbserial")) {
	    return new MagcardSerial(prefs.getDevicePathOrPid("Magcard"));
	}
	LogActivity.writeLog("不能识别的磁条卡驱动: %s", driver);
	return null;
    }

    private int  totaltime = 30;
    private long starttime;
    private TextView timeview;
    private Handler handler;
    private Runnable runnable;
    private MagcardTask task;
    private MagcardListener listener;
    static private int error_count = 0;

    private View buildLayout() {
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
	view.setText("读取的卡号仅用来填写本凭条相关字段。");
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
	builder.setTitle("请刷磁条卡或存折");
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

    abstract protected boolean open();
    abstract protected String read();
    abstract protected void close();
    abstract protected void cancel();

    public void setListener(MagcardListener listener) {
	this.listener = listener;
    }

    public void read(FragmentManager manager) {
	task = new MagcardTask(manager);
	task.execute();
    }

    public interface MagcardListener
    {
	public void onMagcardRead(Magcard magcard, String cardno);
    }

    public class MagcardTask extends AsyncTask<Void, Void, String>
    {
	private FragmentManager manager;

	public MagcardTask(FragmentManager manager) {
	    this.manager = manager;
	}

	@Override
	protected String doInBackground(Void... args) {
	    return read();
	}

	@Override
	protected void onPreExecute() {
	    if (!open()) {
		Utils.showToast("打开刷卡设备失败", R.drawable.cry);
		cancel(true);
		return;
	    }
	    show(manager, "Magcard");
	}

	@Override
	protected void onPostExecute(String cardno) {
	    if (isVisible()) {
		dismiss();
	    }
	    close();

	    if (cardno != null) {
		if (listener != null) {
		    listener.onMagcardRead(Magcard.this, cardno);
		}
		Magcard.error_count = 0;
	    } else {
		if (++Magcard.error_count >= 3) {
		    Utils.showToast("已经连续 " + Magcard.error_count + " 次刷卡失败，"
			    + "请联系管理员检查设备配置", R.drawable.cry);
		} else {
		    Utils.showToast("读取卡号失败，请重试！", R.drawable.cry);
		}
	    }
	}
	
	@Override
	protected void onCancelled(String cardno) {
	    if (isVisible()) {
		Utils.showToast("操作被取消 ...");
		dismiss();
	    }
	    close();
	}
    }

}

class MagcardVirtual extends Magcard
{
    public MagcardVirtual() {
    }

    @Override
    protected boolean open() {
	return true;
    }

    @Override
    protected String read() {
	try {
	    Thread.sleep(2000);
	} catch (InterruptedException e) {
	    LogActivity.writeLog(e);
	}
	SecureRandom random = new SecureRandom();
	if (random.nextBoolean()) {
	    return new BigInteger(130, random).toString(10).substring(0, 18);
	} else {
	    return null;
	}
    }

    @Override
    protected void cancel() {
	LogActivity.writeLog("操作被取消");
    }

    @Override
    protected void close() {
    }
}

class MagcardSerial extends Magcard
{
    private String path;

    public MagcardSerial(String path) {
	this.path = path;
    }

    @Override
    protected boolean open() {
	return false;
    }

    @Override
    protected String read() {
	if (path == null) {
	    LogActivity.writeLog("不能得到刷卡器设备路径");
	    return null;
	}
	return null;
    }

    @Override
    protected void cancel() {
    }

    @Override
    protected void close() {
    }
}

class MagcardUSBWBT1372 extends Magcard
{
    public static final int VID = 0x16C0;
    public static final int PID = 0x06EA;

    private UsbInterface intf;
    private UsbDeviceConnection conn;

    public MagcardUSBWBT1372() {
	conn = null;
    }

    @Override
    protected boolean open() {
	UsbDevice usb_device = Utils.findUsbDevice(VID, PID);
	if (usb_device == null)
	    return false;

	LogActivity.writeLog("numIf: %d", usb_device.getInterfaceCount());
	for (int i = 0; i < usb_device.getInterfaceCount(); i++) {
	    UsbInterface uif = usb_device.getInterface(i);
	    LogActivity.writeLog("接口: %s", uif.toString());
	    for (int j = 0; j < uif.getEndpointCount(); j++) {
		UsbEndpoint ep = uif.getEndpoint(j);
		LogActivity.writeLog("端点：%s", ep.toString());
		if (ep.getDirection() == UsbConstants.USB_DIR_IN)
		    LogActivity.writeLog("方向：设备到主机");
		else
		    LogActivity.writeLog("方向：主机到设备");
		LogActivity.writeLog("类型：%d", ep.getType());
	    }
	}

	intf = usb_device.getInterface(1);
	if (intf == null) {
	    LogActivity.writeLog("不能活的USB接口");
	    return false;
	}

	Context context = EFormApplication.getContext();
	UsbManager mg = (UsbManager) context.getSystemService(Context.USB_SERVICE);
	conn = mg.openDevice(usb_device);
	if (conn == null) {
	    LogActivity.writeLog("不能打开设备连接");
	    return false;
	}
	if (!conn.claimInterface(intf, true)) {
	    LogActivity.writeLog("不能申请设备独占访问");
	    return false;
	}
	return true;
    }

    @Override
    protected String read() {
	if (conn == null) return null;

	UsbRequest request = new UsbRequest();
	request.initialize(conn, intf.getEndpoint(0));

	ByteBuffer buffer = ByteBuffer.allocate(60);
        if (!request.queue(buffer, 18)) {
            LogActivity.writeLog("排队请求失败");
            return null;
        }

        if (conn.requestWait() == request) {
            byte[] bytes = buffer.array();
            LogActivity.writeLog("请求返回 %d, %s", bytes.length, new String(bytes));
            request.close();

        }
	return null;
    }

    @Override
    protected void close() {
	if (conn != null) {
	    conn.close();
	    conn.releaseInterface(intf);
	}
    }

    @Override
    protected void cancel() {
    }
}
