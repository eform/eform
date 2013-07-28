/* EForm - Electronic Form System
 *
 * Copyright (C) 2013 Wu Xiaohu. All rights reserved.
 * Copyright (C) 2013 Cansiny Trade Co.,Ltd. All rights reserved.
 */
package com.cansiny.eform;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
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
	
	TabWidget tabwidget = tabhost.getTabWidget();
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


    private void fillAftermartket() {
	TextView textview = (TextView) getDialog().findViewById(R.id.aftermarket_local_textview);
	textview.setText("姓名：吴小虎\n电话：18655953721");
    }


    private void fillDeviceInfo() {
	TextView textview = (TextView) getDialog().findViewById(R.id.product_type_textview);
	textview.setText("CANSINY-ZZTD-01");
	textview = (TextView) getDialog().findViewById(R.id.version_textview);
	textview.setText("Ver 1.3 Build 1323");
	textview = (TextView) getDialog().findViewById(R.id.identifier_textview);
	textview.setText("8FF2C0C31F");
	textview = (TextView) getDialog().findViewById(R.id.manufacture_date_textview);
	textview.setText("2013-10-24");
	textview = (TextView) getDialog().findViewById(R.id.environment_textview);
	textview.setText("ROSE 4.0.3");
    }

    
    @Override
    public void onDismiss(DialogInterface dialog) {
	super.onDismiss(dialog);
    }

}
