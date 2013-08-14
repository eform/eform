/* EForm - Electronic Form System
 *
 * Copyright (C) 2013 Wu Xiaohu. All rights reserved.
 * Copyright (C) 2013 Cansiny Trade Co.,Ltd. All rights reserved.
 */
package com.cansiny.eform;

import java.io.UnsupportedEncodingException;

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
	Printer printer = null;

	if (driver.equalsIgnoreCase("virtual")) {
	    printer = new PrinterVirtual();
	} else if (driver.equalsIgnoreCase("usb")) {
	    printer = new PrinterUSB(prefs.getDeviceNameOrVid("Printer"),
		    prefs.getDevicePathOrPid("Printer"));
	} else if (driver.equalsIgnoreCase("serial") ||
		driver.equalsIgnoreCase("usbserial")) {
	    printer = new PrinterSerial(prefs.getDevicePathOrPid("Printer"));
	} else {
	    LogActivity.writeLog("不能识别的打印机驱动: %s", driver);
	    return null;
	}
	return printer;
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


class PrinterUSB extends Printer
{
    private int vid;
    private int pid;

    public PrinterUSB(String vid, String pid) throws NumberFormatException {
	this.vid = Integer.decode(vid);
	this.pid = Integer.decode(pid);
    }

    @Override
    public boolean open() {
	if (vid == 0 || pid == 0) {
	    LogActivity.writeLog("不能得到打印机厂商ID或产品ID");
	    return false;
	}
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