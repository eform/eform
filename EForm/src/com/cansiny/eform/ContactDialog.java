/* EForm - Electronic Form System
 *
 * Copyright (C) 2013 Wu Xiaohu. All rights reserved.
 * Copyright (C) 2013 Cansiny Trade Co.,Ltd. All rights reserved.
 */
package com.cansiny.eform;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabWidget;
import android.widget.TextView;


public class ContactDialog extends DialogFragment
{
    static final private String TAB_TAG_AFTERMARKET = "AfterMarket";
    static final private String TAB_TAG_COMPANYINFO = "CompanyInfo";
    static final private String TAB_TAG_DEVICEINFO  = "DeviceInfo";
    static final private String TAB_TAG_COPYRIGHT   = "Copyright";


    static public UUID getDeviceId(Context context) {
	UUID uuid = null;

	SharedPreferences prefs = context.getSharedPreferences("deviceid.xml", 0);
	String id = prefs.getString("deviceid", null);
	if (id != null)
	    return UUID.fromString(id);

	try {
	    String androidId = Secure.getString(context.getContentResolver(), Secure.ANDROID_ID);
	    if (!"9774d56d682e549c".equals(androidId)) {
		uuid = UUID.nameUUIDFromBytes(androidId.getBytes("utf-8"));
	    } else {
		String deviceId = ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
		uuid = (deviceId != null) ? UUID.nameUUIDFromBytes(deviceId.getBytes("utf8")) : UUID.randomUUID();
	    }
	    prefs.edit().putString("deviceid", uuid.toString()).commit();
	    return uuid;
	} catch (UnsupportedEncodingException e) {
	    LogActivity.writeLog(e);
	    return UUID.randomUUID();
	}
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
	super.onCreateDialog(savedInstanceState);

	AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
	LayoutInflater inflater = getActivity().getLayoutInflater();
	builder.setView(inflater.inflate(R.layout.dialog_contact, null));

	return builder.create();
    }

    @Override
    public void onStart() {
	super.onStart();

	final TabHost tabhost = (TabHost)getDialog().findViewById(R.id.tabhost);
	TabWidget tabwidget = tabhost.getTabWidget();

	if (tabwidget == null || tabwidget.getTabCount() == 0) {
	    tabhost.setup();

	    TabHost.TabSpec tabspec = tabhost.newTabSpec(TAB_TAG_AFTERMARKET);
	    tabspec.setContent(R.id.aftermarket_tab);
	    tabspec.setIndicator("售后服务", null);
	    tabhost.addTab(tabspec);

	    tabspec = tabhost.newTabSpec(TAB_TAG_COMPANYINFO);
	    tabspec.setContent(R.id.companyinfo_tab);
	    tabspec.setIndicator("公司信息",null);
	    tabhost.addTab(tabspec);

	    tabspec = tabhost.newTabSpec(TAB_TAG_DEVICEINFO);
	    tabspec.setContent(R.id.deviceinfo_tab);
	    tabspec.setIndicator("设备信息",null);
	    tabhost.addTab(tabspec);

	    tabspec = tabhost.newTabSpec(TAB_TAG_COPYRIGHT);
	    tabspec.setContent(R.id.copyright_tab);
	    tabspec.setIndicator("版权信息",null);
	    tabhost.addTab(tabspec);

	    tabwidget = tabhost.getTabWidget();
	    for (int i = 0; i < tabwidget.getTabCount(); i++) {
		View layout = tabwidget.getChildTabViewAt(i);
		TextView textview = (TextView) layout.findViewById(android.R.id.title);
		textview.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);
	    }

	    tabhost.setOnTabChangedListener(new OnTabChangeListener() {
		@Override
		public void onTabChanged(String tag) {
		}
	    });

	    fillAftermartket();
	    fillDeviceInfo();
	}
    }


    private void fillAftermartket() {
	TextView textview = (TextView) getDialog().findViewById(R.id.aftermarket_local_textview);
	textview.setText("姓名：吴小虎\n电话：18655953721");
    }


    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private void fillDeviceInfo() {
	Configuration config = getResources().getConfiguration();
	PackageInfo pInfo = null;

	try {
	    pInfo = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0);
	} catch (NameNotFoundException e) {
	    LogActivity.writeLog(e);
	}

	TextView textview = (TextView) getDialog().findViewById(R.id.product_textview);
	textview.setText(Build.PRODUCT);

	textview = (TextView) getDialog().findViewById(R.id.model_textview);
	textview.setText(Build.MODEL);

	if (pInfo != null) {
	    textview = (TextView) getDialog().findViewById(R.id.version_textview);
	    textview.setText(pInfo.versionName + "-" + pInfo.versionCode);
	}

	textview = (TextView) getDialog().findViewById(R.id.identifier_textview);
	textview.setText(getDeviceId(getActivity()).toString());

	textview = (TextView) getDialog().findViewById(R.id.environment_textview);
	textview.setText(Build.DISPLAY);

//	Log.d("", "product: " + Build.PRODUCT);
//	Log.d("", "Manufacturer: " + Build.MANUFACTURER);
//	Log.d("", "Radio: " + Build.getRadioVersion());
//	Log.d("", "SERIAL: " + Build.SERIAL);
//	Log.d("", "User: " + Build.USER);
//	Log.d("", "Device: " + Build.DEVICE);
//	Log.d("", "Display: " + Build.DISPLAY);
//	Log.d("", "Board: " + Build.BOARD);
//	Log.d("", "Bootloader: " + Build.BOOTLOADER);
//	Log.d("", "barnd: " + Build.BRAND);
//	Log.d("", "Id: " + Build.ID);
//	Log.d("", "Hardware: " + Build.HARDWARE);
//	Log.d("", "FingerPrint: " + Build.FINGERPRINT);
//	Log.d("", "Tags: " + Build.TAGS);
//	Log.d("", "Host: " + Build.HOST);
//	Log.d("", "CPU ABI: " + Build.CPU_ABI);
//	Log.d("", "CPU ABI2: " + Build.CPU_ABI2);

	Date date = new Date(Build.TIME);
	String text = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINESE).format(date);
	if (pInfo != null) {
	    date = new Date(pInfo.lastUpdateTime);
	    text += "  /  ";
	    text += new SimpleDateFormat("yyyy-MM-dd", Locale.CHINESE).format(date);
	}
	textview = (TextView) getDialog().findViewById(R.id.manufacture_date_textview);
	textview.setText(text);

	textview = (TextView) getDialog().findViewById(R.id.screendp_textview);
	textview.setText("" + config.screenWidthDp + " X " + config.screenHeightDp);

	textview = (TextView) getDialog().findViewById(R.id.keyboard_textview);
	switch(config.keyboard) {
	case Configuration.KEYBOARD_NOKEYS:
	    textview.setText("无");
	    break;
	case Configuration.KEYBOARD_QWERTY:
	    textview.setText("QWERTY键盘");
	    break;
	case Configuration.KEYBOARD_12KEY:
	    textview.setText("12键键盘");
	    break;
	}
    }


    @Override
    public void onDismiss(DialogInterface dialog) {
	super.onDismiss(dialog);
    }

}
