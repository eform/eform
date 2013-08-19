/* EForm - Electronic Form System
 *
 * Copyright (C) 2013 Wu Xiaohu. All rights reserved.
 * Copyright (C) 2013 Cansiny Trade Co.,Ltd. All rights reserved.
 */
package com.cansiny.eform;

import java.io.UnsupportedEncodingException;
import com.cansiny.eform.Utils.Device;

import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.hardware.usb.UsbRequest;
import android.util.Log;


public abstract class Printer extends Device
{
    static public final int PRINT_WIDTH_NORMAL = 0;
    static public final int PRINT_WIDTH_HALF = 1;

    abstract public boolean move(int x, int y);
    abstract public boolean write(String text, PrintParam param);

    @Override
    protected void cancel() {
//	if (task != null && !task.isCancelled()) {
//	    task.cancel(true);
//	}
    }

    public class PrintParam
    {
	public int spacing;
	public int width;
    }
}


class PrinterVirtual extends Printer
{
    private static final String TAG = "VirtualPrinter";

    @Override
    public boolean open() {
	return true;
    }

    @Override
    public void close() {
    }

    @Override
    protected void cancel() {
	// TODO Auto-generated method stub
	
    }
    
    @Override
    public boolean move(int x, int y) {
	Log.d(TAG, "移到 X: " + x + " Y: " + y);
	return true;
    }

    @Override
    public boolean write(String text, PrintParam param) {
	Log.d(TAG, "打印数据：" + text);
	return true;
    }

}


class PrinterLQ90KP extends Printer
{
    public static final int VID = 0x3001;
    public static final int PID = 0x3002;

    private UsbDeviceConnection connection;
    private UsbInterface iface;
    private UsbRequest request;

    public PrinterLQ90KP() {
    }

    @Override
    public boolean open() {
	UsbDevice device = this.getUsbDevice(VID, PID);
	if (device == null)
	    return false;

	Context context = EFormApplication.getContext();
	UsbManager manager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
	connection = manager.openDevice(device);
	if (connection == null) {
	    LogActivity.writeLog("打印失败，打开USB打印机失败");
	    return false;
	}

	iface = device.getInterface(0);
	if (!connection.claimInterface(iface, true)) {
	    LogActivity.writeLog("打印失败，申请USB接口独占访问失败");
	    return false;
	}
	return true;
    }

    @Override
    public void close() {
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
    public boolean move(int x, int y) {
	return false;
    }

    @Override
    public boolean write(String text, PrintParam param) {
	if (text == null) {
	    LogActivity.writeLog("没有要打印的数据");
	    return false;
	}
	try {
	    byte[] bytes = text.getBytes("GB2312");
	} catch (UnsupportedEncodingException e) {
	    LogActivity.writeLog(e);
	    return false;
	}
	return false;
    }

}
