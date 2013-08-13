package com.cansiny.eform;

import java.io.UnsupportedEncodingException;

import android.util.Log;


public abstract class Printer
{
    static public final int PRINT_WIDTH_NORMAL = 0;
    static public final int PRINT_WIDTH_HALF = 1;

    static public Printer getPrinter() {
	try {
	    Preferences prefs = Preferences.getPreferences();
	    String printer_class = prefs.getPrinterClass();
	    return (Printer) Class.forName(printer_class).newInstance();
	} catch (Exception e) {
	    LogActivity.writeLog(e);
	    return null;
	}
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


class PrinterLog extends Printer
{
    private static final String TAG = "PrinterLog";

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


class PrinterPLQ20K extends Printer
{
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
