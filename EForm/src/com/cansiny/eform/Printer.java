/* EForm - Electronic Form System
 *
 * Copyright (C) 2013 Wu Xiaohu. All rights reserved.
 * Copyright (C) 2013 Cansiny Trade Co.,Ltd. All rights reserved.
 */
package com.cansiny.eform;

import java.io.UnsupportedEncodingException;
import com.cansiny.eform.Utils.Device;

import android.util.Log;


public abstract class Printer extends Device
{
    static public final int PRINT_WIDTH_NORMAL = 0;
    static public final int PRINT_WIDTH_HALF = 1;

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


class PrinterUSBLQ90KP extends Printer
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
    protected void cancel() {
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
