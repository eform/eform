/* EForm - Electronic Form System
 *
 * Copyright (C) 2013 Wu Xiaohu. All rights reserved.
 * Copyright (C) 2013 Cansiny Trade Co.,Ltd. All rights reserved.
 */
package com.cansiny.eform;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import android.util.Log;


public abstract class Printer
{
    static public final int PRINT_WIDTH_NORMAL = 0;
    static public final int PRINT_WIDTH_HALF = 1;

    static public Printer getPrinter() {
	Preferences prefs = Preferences.getPreferences();

	String driver = prefs.getDeviceDriver("Printer");
	if (driver == null || driver.length() == 0) {
	    LogActivity.writeLog("打印机驱动未指定");
	    return null;
	}

	if (driver.equalsIgnoreCase("virtual")) {
	    return new PrinterVirtual();
	}
	if (driver.equalsIgnoreCase("usb")) {
	    return PrinterUSB.getUSBPrinter(prefs.getDeviceNameOrVid("Printer"),
		    prefs.getDevicePathOrPid("Printer"));
	}
	if (driver.equalsIgnoreCase("serial") || driver.equalsIgnoreCase("usbserial")) {
	    return new PrinterSerial(prefs.getDevicePathOrPid("Printer"));
	}
	LogActivity.writeLog("不能识别的打印机驱动: %s", driver);
	return null;
    }

    static public ArrayList<Utils.DeviceAdapter.Device> listUSBDevices() {
	return PrinterUSB.listUSBDevices();
    }

    abstract public boolean open();
    abstract public void close();
    abstract public boolean move(int x, int y);
    abstract public boolean write(String text, PrintParam param);

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

class PrinterSerial extends Printer
{
    private String path;

    public PrinterSerial(String path) {
	this.path = path;
    }

    @Override
    public boolean open() {
	// TODO Auto-generated method stub
	return false;
    }

    @Override
    public void close() {
	// TODO Auto-generated method stub
	
    }

    @Override
    public boolean move(int x, int y) {
	// TODO Auto-generated method stub
	return false;
    }

    @Override
    public boolean write(String text, PrintParam param) {
	// TODO Auto-generated method stub
	return false;
    }
}

abstract class PrinterUSB extends Printer
{
    public static PrinterUSB getUSBPrinter(String vid, String pid) {
	try {
	    int ivid = Integer.decode(vid);
	    int ipid = Integer.decode(pid);

	    if (ivid == PrinterUSBLQ90KP.VID && ipid == PrinterUSBLQ90KP.PID) {
		return new PrinterUSBLQ90KP();
	    }
	    LogActivity.writeLog("不能找到厂商ID为%s和产品ID为%s的驱动程序", vid, pid);
	    return null;
	} catch (NumberFormatException e) {
	    LogActivity.writeLog(e);
	    return null;
	}
    }

    public static ArrayList<Utils.DeviceAdapter.Device> listUSBDevices() {
	ArrayList<Utils.DeviceAdapter.Device> array =
		new ArrayList<Utils.DeviceAdapter.Device>();

	array.add(new Utils.DeviceAdapter.Device("usb",
		String.format("0x%04X", PrinterUSBLQ90KP.VID),
		String.format("0x%04X", PrinterUSBLQ90KP.PID)));

	return array;
    }
}

class PrinterUSBLQ90KP extends PrinterUSB
{
    public static final int VID = 0x3001;
    public static final int PID = 0x3002;

    public PrinterUSBLQ90KP() {
    }

    @Override
    public boolean open() {
	return false;
    }

    @Override
    public void close() {
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
