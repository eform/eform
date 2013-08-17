/* EForm - Electronic Form System
 *
 * Copyright (C) 2013 Wu Xiaohu. All rights reserved.
 * Copyright (C) 2013 Cansiny Trade Co.,Ltd. All rights reserved.
 *
 * Copyright 2009 Cedric Priscal
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */
package com.cansiny.eform;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.LineNumberReader;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Vector;


public class SerialPort
{
    // Do not remove or rename the field mFd: it is used by native method close();
    private FileDescriptor mFd;
    private FileInputStream mFileInputStream;
    private FileOutputStream mFileOutputStream;

    public SerialPort(File device, int baudrate, int flags)
	throws SecurityException, IOException {
	/* Check access permission */
	if (!device.canRead() || !device.canWrite()) {
	    try {
		/* Missing read/write permission, trying to change file mode */
		Process su = Runtime.getRuntime().exec("/system/xbin/su");
		String cmd = "chmod 666 " + device.getAbsolutePath() + "\n" + "exit\n";
		su.getOutputStream().write(cmd.getBytes());
		if ((su.waitFor() != 0) || !device.canRead() || !device.canWrite()) {
		    throw new SecurityException();
		}
	    } catch (Exception e) {
		LogActivity.writeLog(e);
		throw new SecurityException();
	    }
	}

	mFd = open(device.getAbsolutePath(), baudrate, flags);
	if (mFd == null) {
	    LogActivity.writeLog("底层打开串口函数返回 null");
	    throw new IOException();
	}
	mFileInputStream  = new FileInputStream(mFd);
	mFileOutputStream = new FileOutputStream(mFd);
    }

    public InputStream getInputStream() {
	return mFileInputStream;
    }
    public OutputStream getOutputStream() {
	return mFileOutputStream;
    }

    private native static FileDescriptor open(String path, int baudrate, int flags);
    public native void close();
    static {
	System.loadLibrary("serialPort");
    }

    static public class SerialPortFinder
    {
        public class Driver {
	    public Driver(String name, String root) {
		mDriverName = name;
		mDeviceRoot = root;
	    }
	    private String mDriverName;
	    private String mDeviceRoot;
	    Vector<File> mDevices = null;
	    public Vector<File> getDevices() {
		if (mDevices == null) {
		    mDevices = new Vector<File>();
		    File dev = new File("/dev");
		    File[] files = dev.listFiles();
		    for (int i = 0; i < files.length; i++) {
			if (files[i].getAbsolutePath().startsWith(mDeviceRoot)) {
//			    LogActivity.writeLog("找到新串口设备: " + files[i]);
			    mDevices.add(files[i]);
			}
		    }
		}
		return mDevices;
	    }
	    public String getName() {
		return mDriverName;
	    }
        }

        private Vector<Driver> mDrivers = null;

        Vector<Driver> getDrivers() throws IOException {
	    if (mDrivers == null) {
		mDrivers = new Vector<Driver>();
		LineNumberReader r = new LineNumberReader(new FileReader("/proc/tty/drivers"));
		String l;
		while((l = r.readLine()) != null) {
		    // Issue 3:
		    // Since driver name may contain spaces, we do not extract driver name with split()
		    String drivername = l.substring(0, 0x15).trim();
		    String[] w = l.split(" +");
		    if ((w.length >= 5) && (w[w.length - 1].equals("serial"))) {
//			LogActivity.writeLog("找到串口驱动 " + drivername + " on " + w[w.length - 4]);
			mDrivers.add(new Driver(drivername, w[w.length-4]));
		    }
		}
		r.close();
	    }
	    return mDrivers;
        }

        public String[] getAllDevices() {
	    Vector<String> devices = new Vector<String>();
	    Iterator<Driver> itdriv;
	    try {
		itdriv = getDrivers().iterator();
		while(itdriv.hasNext()) {
		    Driver driver = itdriv.next();
		    Iterator<File> itdev = driver.getDevices().iterator();
		    while(itdev.hasNext()) {
			File file = itdev.next();
			String devicename = file.getName();
			String devicepath = file.getAbsolutePath();
			String value = String.format("%s %s %s", devicename, driver.getName(), devicepath);
			devices.add(value);
		    }
		}
	    } catch (IOException e) {
		LogActivity.writeLog(e);
	    }
	    return devices.toArray(new String[devices.size()]);
        }

        public String[] getAllDevicesPath() {
	    Vector<String> devices = new Vector<String>();
	    Iterator<Driver> itdriv;
	    try {
		itdriv = getDrivers().iterator();
		while(itdriv.hasNext()) {
		    Driver driver = itdriv.next();
		    Iterator<File> itdev = driver.getDevices().iterator();
		    while(itdev.hasNext()) {
			String device = itdev.next().getAbsolutePath();
			devices.add(device);
		    }
		}
	    } catch (IOException e) {
		LogActivity.writeLog(e);
	    }
	    return devices.toArray(new String[devices.size()]);
        }
    }

}
