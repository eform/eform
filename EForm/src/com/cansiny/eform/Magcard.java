/* EForm - Electronic Form System
 *
 * Copyright (C) 2013 Wu Xiaohu. All rights reserved.
 * Copyright (C) 2013 Cansiny Trade Co.,Ltd. All rights reserved.
 */
package com.cansiny.eform;

import java.io.File;
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


class MagcardDialog extends Utils.DialogFragment
{
    private Magcard magcard;
    private int  totaltime = 30;
    private long starttime;
    private TextView timeview;
    private Handler handler;
    private Runnable runnable;

    public MagcardDialog(Magcard magcard) {
	this.magcard = magcard;
    }

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
		    magcard.cancel();
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
		magcard.cancel();
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

}


public abstract class Magcard extends Utils.Device
{
    static final private int LEAVE_START = 4;
    static final private int LEAVE_END   = 4;

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

    static private int swipe_error_count = 0;

    private MagcardListener listener;
    protected MagcardTask task;

    public void setListener(MagcardListener listener) {
	this.listener = listener;
    }

    abstract protected String read();

    protected void cancel() {
	if (task != null && !task.isCancelled()) {
	    task.cancel(true);
	}
    }

    public void read(FragmentManager manager) {
	if (!open()) {
	    Utils.showToast("打开刷卡设备失败", R.drawable.cry);
	} else {
	    task = new MagcardTask(manager);
	    task.execute();
	}
    }

    public interface MagcardListener
    {
	public void onMagcardRead(Magcard magcard, String cardno);
    }

    public class MagcardTask extends AsyncTask<Void, Void, String>
    {
	private FragmentManager manager;
	private MagcardDialog dialog;

	public MagcardTask(FragmentManager manager) {
	    this.manager = manager;
	}

	@Override
	protected String doInBackground(Void... args) {
	    return read();
	}

	@Override
	protected void onPreExecute() {
	    dialog = new MagcardDialog(Magcard.this);
	    dialog.show(manager, "MagcardDialog");
	}

	@Override
	protected void onPostExecute(String cardno) {
	    dialog.dismiss();
	    close();

	    if (cardno != null) {
		if (listener != null) {
		    listener.onMagcardRead(Magcard.this, cardno);
		}
		Magcard.swipe_error_count = 0;
	    } else {
		if (++Magcard.swipe_error_count >= 3) {
		    Utils.showToast("已经连续 " + Magcard.swipe_error_count +
			    " 次刷卡失败，请联系管理员检查设备配置", R.drawable.cry);
		} else {
		    Utils.showToast("读取卡号失败，请重试！", R.drawable.cry);
		}
	    }
	}
	
	@Override
	protected void onCancelled(String cardno) {
	    dialog.dismiss();
	    close();
	    Utils.showToast("操作被取消 ...");
	}
    }

    protected String extractCardno(String string) {
	String[] fields = string.split("\\?", 4);

	for (int i = 0; i < fields.length - 1; i++) {
	    String track = fields[i].trim();

	    if (track.startsWith("%B")) {	// Track I
		String[] fields2 = fields[i].split("\\^");
		if (fields2.length == 3) {
		    String cardno = fields2[0].substring(3).trim();
		    if (cardno.matches("[0-9]+")) {
			return cardno;
		    }
		}
	    } else if (track.startsWith(";")) {	// Track II
		String[] fields2 = fields[i].split("=");
		if (fields2.length == 2) {
		    String cardno = fields2[0].substring(2).trim();
		    if (cardno.matches("[0-9]+")) {
			return cardno;
		    }
		}
	    } else if (track.startsWith("+")) {	// Track III
		String[] fields2 = fields[i].split("=");
		if (fields2.length == 5) {
		    String cardno = fields2[0].substring(4).trim();
		    if (cardno.matches("[0-9]+")) {
			return cardno;
		    }
		}
	    }
	}
	return null;
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
    protected void close() {
    }

    @Override
    public boolean probe() {
	return true;
    }
}

class MagcardWBT1372 extends Magcard
{
    public static final String PRODUCT = "WBT1372";
    public static final int VID = 0x16C0;
    public static final int PID = 0x06EA;

    private UsbManager manager;
    private UsbDeviceConnection connection;
    private UsbInterface iface;
    private UsbRequest request;

    public MagcardWBT1372() {
	Context context = EFormApplication.getContext();
	manager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
	iface = null;
	connection = null;
    }

    @Override
    protected boolean open() {
	UsbDevice device = getUsbDevice(VID, PID);
	if (device == null)
	    return false;

	connection = manager.openDevice(device);
	if (connection == null) {
	    LogActivity.writeLog("刷卡失败，打开USB刷卡设备失败");
	    return false;
	}

	iface = device.getInterface(1);
	if (!connection.claimInterface(iface, true)) {
	    LogActivity.writeLog("刷卡失败，申请USB接口独占访问失败");
	    return false;
	}
	return true;
    }

    @Override
    protected String read() {
	if (connection == null) return null;

	UsbEndpoint endpoint = iface.getEndpoint(0);
	if (endpoint == null ||
		endpoint.getDirection() != UsbConstants.USB_DIR_IN) {
	    LogActivity.writeLog("刷卡失败，端点选择错误");
	    return null;
	}

	request = new UsbRequest();
	if (!request.initialize(connection, endpoint)) {
	    LogActivity.writeLog("刷卡失败，初始化请求错误");
	    return null;
	}

	ByteBuffer buffer = ByteBuffer.allocate(512);
    	if (!request.queue(buffer, 512)) {
    	    LogActivity.writeLog("刷卡失败，刷卡请求排队错误");
    	    return null;
    	}

    	String cardno = null;

    	if (connection.requestWait() == request) {
    	    if (!task.isCancelled()) {
    		cardno = extractCardno(new String(buffer.array()));
    	    }
    	}
    	request.close();

	return cardno;
    }

    @Override
    protected void close() {
	if (connection != null) {
	    connection.releaseInterface(iface);
	    connection.close();
	}
    }

    @Override
    protected void cancel() {
	super.cancel();

	if (!request.cancel()) {
	    close();
	    connection = null;
	}
    }

    @Override
    public boolean probe() {
	return (getUsbDevice(VID, PID) == null) ? false : true;
    }

}

class MagcardWBT1370 extends Magcard
{
    static private final int BAUDRATE = 9600;

    private SerialPort serial;

    public MagcardWBT1370() {
    }

    @Override
    protected boolean open() {
	Preferences prefs = Preferences.getPreferences();

	String path = prefs.getDevicePath(DEVICE_MAGCARD);
	if (path == null) {
	    LogActivity.writeLog("打开刷卡器失败，串口设备未配置");
	    return false;
	}
	try {
	    serial = new SerialPort(new File(path), BAUDRATE, 0);
	    return true;
	} catch (Exception e) {
	    LogActivity.writeLog(e);
	    return false;
	}
    }

    @Override
    protected String read() {
	if (serial == null) return null;

	return null;
    }

    @Override
    protected void close() {
	serial.close();
    }

    @Override
    protected void cancel() {
	super.cancel();
    }

    @Override
    public boolean probe() {
	if (!open()) {
	    return false;
	} else {
	    close();
	    return true;
	}
    }

}
