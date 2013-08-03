/* EForm - Electronic Form System
 *
 * Copyright (C) 2013 Wu Xiaohu. All rights reserved.
 * Copyright (C) 2013 Cansiny Trade Co.,Ltd. All rights reserved.
 */
package com.cansiny.eform;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabWidget;
import android.widget.TextView;


class PreferencesDialog extends Utils.DialogFragment implements OnCheckedChangeListener
{
   static final private String TAB_TAG_DEVICE  = "Device";
   static final private String TAB_TAG_MEMBER  = "Member";
   static final private String TAB_TAG_ADVANCE = "Advance";

   @Override
   public Dialog onCreateDialog(Bundle savedInstanceState) {
	super.onCreateDialog(savedInstanceState);

	AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
	LayoutInflater inflater = getActivity().getLayoutInflater();
	builder.setView(inflater.inflate(R.layout.dialog_prefs, null));

	return builder.create();
   }

   @Override
   public void onStart() {
	super.onStart();

	Administrator admin = Administrator.getAdministrator();
	if (!admin.isLogin()) {
	    dismiss();
	    Utils.showToast("管理员未登录...");
	    return;
	}

	AlertDialog dialog = (AlertDialog) getDialog();

	final TabHost tabhost = (TabHost)dialog.findViewById(android.R.id.tabhost);
	TabWidget tabwidget = tabhost.getTabWidget();

	if (tabwidget == null || tabwidget.getTabCount() == 0) {
	    tabhost.setup();

	    TabHost.TabSpec tabspec = tabhost.newTabSpec(TAB_TAG_DEVICE);
	    tabspec.setContent(R.id.device_tab);
	    tabspec.setIndicator("设备", null);
	    tabhost.addTab(tabspec);

	    tabspec = tabhost.newTabSpec(TAB_TAG_MEMBER);
	    tabspec.setContent(R.id.member_tab);
	    tabspec.setIndicator("会员", null);
	    tabhost.addTab(tabspec);

	    tabspec = tabhost.newTabSpec(TAB_TAG_ADVANCE);
	    tabspec.setContent(R.id.advance_tab);
	    tabspec.setIndicator("高级", null);
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
	}

	Button button = (Button) dialog.findViewById(R.id.logout_button);
	button.setOnClickListener(this);

	button = (Button) dialog.findViewById(R.id.set_password_button);
	button.setOnClickListener(this);

	button = (Button) dialog.findViewById(R.id.system_settings_button);
	button.setOnClickListener(this);

	CheckBox checkbox = (CheckBox) dialog.findViewById(R.id.auto_logout_checkbox);
	checkbox.setOnCheckedChangeListener(this);
   }

   @Override
   public void onClick(View view) {
       super.onClick(view);

       if (view instanceof Button) {
	   Administrator admin = Administrator.getAdministrator();

	   switch(view.getId()) {
	   case R.id.logout_button:
	       admin.logout();
	       dismiss();
	       break;
	   case R.id.set_password_button:
	       admin.setPassword(getFragmentManager());
	       break;
	   case R.id.system_settings_button:
	       startActivity(new Intent(android.provider.Settings.ACTION_SETTINGS));
	       break;
	   }
       }
   }

   @Override
   public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
       AlertDialog dialog = (AlertDialog) getDialog();

       switch(buttonView.getId()) {
       case R.id.auto_logout_checkbox:
	   View time_edittext = dialog.findViewById(R.id.auto_logout_time_edittext);
	   time_edittext.setEnabled(isChecked);
	   break;
       }
   }

}


public class Preferences
{
    static private Preferences _singlePreferences = null;
    static public Preferences getPreferences() {
	if (_singlePreferences == null)
	    _singlePreferences = new Preferences();
	return _singlePreferences;
    }

    private Preferences() {

    }

    public void showDialog(FragmentManager manager) {
	PreferencesDialog dialog = new PreferencesDialog();
	dialog.show(manager, "PreferencesDialog");
    }

    public String magcardReaderPath() {
	return "/dev/ttyS0";
    }

}
