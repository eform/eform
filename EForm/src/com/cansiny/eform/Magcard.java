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
import com.cansiny.eform.Utils.Device;
import android.app.Dialog;
import android.app.FragmentManager;
import android.content.Context;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.hardware.usb.UsbRequest;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


class MagcardDialog extends Device.DeviceDialog
{
    private int flags;

    public MagcardDialog(Device device, int flags) {
	super(device);
	this.flags = flags;
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
	layout2.setGravity(Gravity.BOTTOM);
    	params = new LinearLayout.LayoutParams(
    		ViewGroup.LayoutParams.WRAP_CONTENT,
    		ViewGroup.LayoutParams.WRAP_CONTENT);
    	params.gravity = Gravity.CENTER_HORIZONTAL;
	layout.addView(layout2, params);

	TextView view = new TextView(getActivity());
	view.setText("操作将在");
	view.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
	layout2.addView(view);

	timeview = new TextView(getActivity());
	timeview.setTextSize(TypedValue.COMPLEX_UNIT_SP, 28);
	timeview.setTextColor(getResources().getColor(R.color.red));
    	params = new LinearLayout.LayoutParams(
    		ViewGroup.LayoutParams.WRAP_CONTENT,
    		ViewGroup.LayoutParams.WRAP_CONTENT);
    	params.leftMargin = 5;
    	params.rightMargin = 5;
	layout2.addView(timeview, params);

	view = new TextView(getActivity());
	view.setText("秒后自动终止");
	view.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
	layout2.addView(view);

	view = new TextView(getActivity());
	switch (flags) {
	case Device.TASK_FLAG_MAGCARD_FORM:
	    view.setText("读取的卡号仅用于填写本凭条相关字段");
	    break;
	case Device.TASK_FLAG_MAGCARD_TEST:
	    view.setText("读取的卡号仅用于测试");
	    break;
	}
	view.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
	params = new LinearLayout.LayoutParams(
		ViewGroup.LayoutParams.WRAP_CONTENT,
		ViewGroup.LayoutParams.MATCH_PARENT);
	params.leftMargin = 10;
	params.topMargin = 12;
	params.bottomMargin = 4;
	layout.addView(view, params);

	return layout;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
	super.onCreateDialog(savedInstanceState);

	builder.setTitle("请刷磁条卡或存折");
	builder.setView(buildLayout());
	return builder.create();
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
    protected MagcardTask task;

    @Override
    protected void startTask(FragmentManager manager, int flags) {
	if (!open()) {
	    Utils.showToast("打开刷卡设备失败", R.drawable.cry);
	} else {
	    task = new MagcardTask(this, manager, flags);
	    task.execute();
	}
    }

    @Override
    protected void cancelTask() {
	if (task != null && !task.isCancelled()) {
	    task.cancel(true);
	}
    }

    public class MagcardTask extends Device.Task<Void, Void, String>
    {
	private FragmentManager manager;
	private int flags;
	private MagcardDialog dialog;

	public MagcardTask(Device device, FragmentManager manager, int flags) {
	    super(device);
	    this.manager = manager;
	    this.flags = flags;
	}

	@Override
	protected void onPreExecute() {
	    super.onPreExecute();
	    dialog = new MagcardDialog(Magcard.this, flags);
	    dialog.show(manager, "MagcardDialog");
	}

	@Override
	protected String doInBackground(Void... args) {
	    return (String) read();
	}

	@Override
	protected void onPostExecute(String result) {
	    super.onPostExecute(result);
	    dialog.dismiss();
	    device.close();

	    if (result == null) {
		if (++Magcard.swipe_error_count >= 3) {
		    Utils.showToast("已经连续 " + Magcard.swipe_error_count +
			    " 次刷卡失败，请联系管理员检查设备配置", R.drawable.cry);
		} else {
		    Utils.showToast("读取卡号失败，请重试！", R.drawable.cry);
		}
	    } else {
		Magcard.swipe_error_count = 0;
	    }
	}

	@Override
	protected void onCancelled(String result) {
	    super.onCancelled(result);
	    dialog.dismiss();
	    device.close();
	    Utils.showToast("操作被取消 ...");
	}
    }

}


class MagcardVirtual extends Magcard
{
    private boolean is_open;

    @Override
    public boolean probeDevice() {
	return true;
    }

    @Override
    protected boolean open() {
	is_open = true;
	return true;
    }

    @Override
    protected void close() {
	is_open = false;
    }

    @Override
    protected String read() {
	if (!is_open) {
	    LogActivity.writeLog("刷卡器未打开或已关闭");
	    return null;
	}

	try {
	    Thread.sleep(2000);
	    SecureRandom random = new SecureRandom();
	    if (random.nextBoolean()) {
		return new BigInteger(130, random).toString(10).substring(0, 18);
	    } else {
		return null;
	    }
	} catch (InterruptedException e) {
	    LogActivity.writeLog(e);
	    return null;
	}
    }

    @Override
    protected int write(String string) {
	return 0;
    }
}


class MagcardWBT1372 extends Magcard
{
    public static final int VID = 0x16C0;
    public static final int PID = 0x06EA;

    private UsbDeviceConnection connection;
    private UsbInterface iface;
    private UsbRequest request;

    public MagcardWBT1372() {
	iface = null;
	connection = null;
    }

    @Override
    public int getDeviceType() {
	return DEVICE_TYPE_USB;
    }

    @Override
    public boolean probeDevice() {
	return (getUsbDevice(VID, PID) != null) ? true : false;
    }

    @Override
    protected boolean open() {
	UsbDevice device = getUsbDevice(VID, PID);
	if (device == null)
	    return false;

	Context context = EFormApplication.getContext();
	UsbManager manager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
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
    protected void close() {
	if (connection != null) {
	    connection.releaseInterface(iface);
	    connection.close();
	}
    }

    @Override
    protected void cancelTask() {
	super.cancelTask();

	if (!request.cancel()) {
	    close();
	    connection = null;
	}
    }

    @Override
    protected String read() {
	if (connection == null) {
	    return null;
	}

	UsbEndpoint endpoint = iface.getEndpoint(0);
	if (endpoint == null ||
		endpoint.getDirection() != UsbConstants.USB_DIR_IN) {
	    LogActivity.writeLog("刷卡失败，设备端点选择错误");
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

    	String result = null;
    	if (connection.requestWait() == request) {
    	    if (!task.isCancelled()) {
    		result = extract(buffer.array());
    	    }
    	}
    	request.close();

	return result;
    }

    @Override
    protected int write(String string) {
	return 0;
    }

    private String extract(byte[] bytes) {
	if (bytes[0] != 0x02) {
	    LogActivity.writeLog("刷卡失败，读取数据头不是“0x02”，而是“0x%02X”", bytes[0]);
	    return null;
	}

	boolean endtag = false;
	StringBuilder builder = new StringBuilder();
	for (int i = 1; i < bytes.length; i++) {
	    if (bytes[i] == 0x03) {
		endtag = true;
		break;
	    }
	    builder.append((char) bytes[i]);
	}
	if (!endtag) {
	    LogActivity.writeLog("刷卡失败，数据结尾标志未找到");
	    return null;
	}

	String[] tracks = builder.toString().split("[\\r\\n]+");

	for (int i = 0; i < tracks.length; i++) {
	    if (tracks[i].startsWith("%")) {		// Track I
		String[] field = tracks[i].split("\\^");
		if (field.length == 3) {
		    String cardno = field[0].substring(2);
		    if (cardno.matches("[0-9]+")) {
			return cardno;
		    }
		}
	    } else if (tracks[i].startsWith(";")) {	// Track II
		String[] field = tracks[i].split("=");
		if (field.length == 2) {
		    String cardno = field[0].substring(1);
		    if (cardno.matches("[0-9]+")) {
			return cardno;
		    }
		}
	    } else if (tracks[i].startsWith("+")) {	// Track III
		String[] field = tracks[i].split("=");
		if (field.length == 5) {
		    String cardno = field[0].substring(3);
		    if (cardno.matches("[0-9]+")) {
			return cardno;
		    }
		}
	    }
	}
	return null;
    }
}

class MagcardWBT1370 extends Magcard
{
    static private final int BAUDRATE = 9600;

    private SerialPort serial;

    public MagcardWBT1370() {
	serial = null;
    }

    @Override
    public int getDeviceType() {
	return DEVICE_TYPE_SERIAL;
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
    protected void cancelTask() {
	super.cancelTask();
    }

    @Override
    protected int write(String string) {
	return 0;
    }

}
