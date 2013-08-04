/* EForm - Electronic Form System
 *
 * Copyright (C) 2013 Wu Xiaohu. All rights reserved.
 * Copyright (C) 2013 Cansiny Trade Co.,Ltd. All rights reserved.
 */
package com.cansiny.eform;

import java.util.ArrayList;

import com.cansiny.eform.Member.MemberProfile;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabWidget;
import android.widget.TextView;


class PreferencesDialog extends Utils.DialogFragment
    implements OnCheckedChangeListener, OnTabChangeListener
{
    static final private String TAB_TAG_GENERIC = "Generic";
    static final private String TAB_TAG_DEVICE  = "Device";
    static final private String TAB_TAG_MEMBER  = "Member";
    static final private String TAB_TAG_ADVANCE = "Advance";

    private boolean generic_tab_is_active = false;
    private boolean device_tab_is_active = false;
    private boolean member_tab_is_active = false;
    private boolean advance_tab_is_active = false;

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

	    TabHost.TabSpec tabspec = tabhost.newTabSpec(TAB_TAG_GENERIC);
	    tabspec.setContent(R.id.generic_tab);
	    tabspec.setIndicator("常 规", null);
	    tabhost.addTab(tabspec);

	    tabspec = tabhost.newTabSpec(TAB_TAG_DEVICE);
	    tabspec.setContent(R.id.device_tab);
	    tabspec.setIndicator("设 备", null);
	    tabhost.addTab(tabspec);

	    tabspec = tabhost.newTabSpec(TAB_TAG_MEMBER);
	    tabspec.setContent(R.id.member_tab);
	    tabspec.setIndicator("会 员", null);
	    tabhost.addTab(tabspec);

	    tabspec = tabhost.newTabSpec(TAB_TAG_ADVANCE);
	    tabspec.setContent(R.id.advance_tab);
	    tabspec.setIndicator("高 级", null);
	    tabhost.addTab(tabspec);

	    tabwidget = tabhost.getTabWidget();
	    for (int i = 0; i < tabwidget.getTabCount(); i++) {
		View layout = tabwidget.getChildTabViewAt(i);
		TextView textview = (TextView) layout.findViewById(android.R.id.title);
		textview.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);
	    }

	    tabhost.setOnTabChangedListener(this);
	}

	onTabChanged(TAB_TAG_GENERIC);

	Preferences prefs = Preferences.getPreferences();
	prefs.beginTransaction();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
	super.onDismiss(dialog);

	Preferences prefs = Preferences.getPreferences();
	prefs.endTransaction();
    }

    private boolean setAftermarketContact() {
	TextView name = (TextView) getDialog().findViewById(R.id.aftermarket_name_edittext);
	if (name.length() == 0) {
	    name.setHint("姓名不能为空");
	    return false;
	}
	TextView phone = (TextView) getDialog().findViewById(R.id.aftermarket_phone_edittext);
	if (phone.length() == 0) {
	    phone.setHint("电话不能为空");
	    return false;
	}

	Preferences prefs = Preferences.getPreferences();
	prefs.setAftermarketName(name.getText().toString());
	prefs.setAftermarketPhone(phone.getText().toString());
	prefs.applyTransaction();

	return true;
    }

    @Override
    public void onClick(View view) {
	super.onClick(view);

	Administrator admin = Administrator.getAdministrator();
	Preferences prefs = Preferences.getPreferences();
	AlertDialog dialog = (AlertDialog) getDialog();

	View view2;
	TextView textview;

	switch(view.getId()) {
	case R.id.aftermarket_contact_button:
	    view2 = dialog.findViewById(R.id.aftermarket_contact_table);
	    view2.setVisibility(View.VISIBLE);
	    textview = (TextView) dialog.findViewById(R.id.aftermarket_name_edittext);
	    textview.setText(prefs.getAftermarketName());
	    textview = (TextView) dialog.findViewById(R.id.aftermarket_phone_edittext);
	    textview.setText(prefs.getAftermarketPhone());
	    break;
	case R.id.aftermarket_contact_set_button:
	    if (setAftermarketContact()) {
		hideIme(view);
		showToast("修改成功！", R.drawable.smile);
		view2 = dialog.findViewById(R.id.aftermarket_contact_table);
		view2.setVisibility(View.GONE);
	    }
	    break;
	case R.id.aftermarket_contact_cancel_button:
	    view2 = dialog.findViewById(R.id.aftermarket_contact_table);
	    view2.setVisibility(View.GONE);
	    break;
	case R.id.member_password_button:
	    view2 = dialog.findViewById(R.id.member_password_table);
	    view2.setVisibility(View.VISIBLE);
	    break;
	case R.id.member_list_button:
	    MemberListDialog dialog2 = new MemberListDialog();
	    dialog2.show(getFragmentManager(), "MemberListDialog");
	    break;
	case R.id.member_password_set_button:
	    view2 = dialog.findViewById(R.id.member_password_table);
	    view2.setVisibility(View.GONE);
	    break;
	case R.id.member_password_cancel_button:
	    view2 = dialog.findViewById(R.id.member_password_table);
	    view2.setVisibility(View.GONE);
	    break;
	case R.id.admin_logout_button:
	    admin.logout();
	    dismiss();
	    break;
	case R.id.admin_password_button:
	    admin.setPassword(getFragmentManager());
	    break;
	case R.id.system_settings_button:
	    startActivity(new Intent(android.provider.Settings.ACTION_SETTINGS));
	    break;
	}
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
	AlertDialog dialog = (AlertDialog) getDialog();
	Preferences prefs = Preferences.getPreferences();

	switch(buttonView.getId()) {
	case R.id.auto_logout_checkbox:
	    View time_edittext = dialog.findViewById(R.id.auto_logout_time_edittext);
	    time_edittext.setEnabled(isChecked);
	    prefs.setAdministratorAutoLogout(isChecked);
	    break;
	}
    }

    @Override
    public void onTabChanged(String tag) {
	if (tag.equals(TAB_TAG_GENERIC)) {
	    if (!generic_tab_is_active) {
		onGenericTabActived();
		generic_tab_is_active = true;
	    }
	} else if (tag.equals(TAB_TAG_DEVICE)) {
	    if (!device_tab_is_active) {
		onDeviceTabActived();
		device_tab_is_active = true;
	    }
	} else if (tag.equals(TAB_TAG_MEMBER)) {
	    if (!member_tab_is_active) {
		onMemberTabActived();
		member_tab_is_active = true;
	    }
	} else if (tag.equals(TAB_TAG_ADVANCE)) {
	    if (!advance_tab_is_active) {
		onAdvanceTabActived();
		advance_tab_is_active = true;
	    }
	}
    }

    private void onGenericTabActived() {
	AlertDialog dialog = (AlertDialog) getDialog();

	int[] ids = {
		R.id.aftermarket_contact_button,
		R.id.aftermarket_contact_set_button,
		R.id.aftermarket_contact_cancel_button,
	};
	for (int id : ids) {
	    Button button = (Button) dialog.findViewById(id);
	    button.setOnClickListener(this);
	}

	int[] ids2 = {
		R.id.aftermarket_name_clear_button,
		R.id.aftermarket_phone_clear_button,
	};
	for (int id : ids2) {
	    Button button = (Button) dialog.findViewById(id);
	    button.setTag(Utils.DialogFragment.CLEAR_BUTTON_TAG);
	    button.setOnClickListener(this);
	}

	View view = dialog.findViewById(R.id.aftermarket_contact_table);
	view.setVisibility(View.GONE);
    }

    private void onDeviceTabActived() {
	final Preferences prefs = Preferences.getPreferences();
	AlertDialog dialog = (AlertDialog) getDialog();

	final Utils.SerialDeviceAdapter adapter = new Utils.SerialDeviceAdapter();
	Spinner spinner = (Spinner) dialog.findViewById(R.id.magcard_spinner);
	spinner.setAdapter(adapter);
	String devpath = prefs.getMagcardDevpath();
	if (devpath != null) {
	    for (int i = 0; i < adapter.getCount(); i++) {
		if (devpath.equals(adapter.getItemDevpath(i))) {
		    spinner.setSelection(i);
		    break;
		}
	    }
	}
	spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
	    @Override
	    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		String devpath = adapter.getItemDevpath(position);
		prefs.setMagcardDevpath(devpath);
	    }
	    @Override
	    public void onNothingSelected(AdapterView<?> parent) {
		prefs.setMagcardDevpath(null);
	    }
	});
    }

    private void onMemberTabActived() {
	AlertDialog dialog = (AlertDialog) getDialog();

	int[] ids = {
		R.id.member_password_button,
		R.id.member_list_button,
		R.id.member_password_set_button,
		R.id.member_password_cancel_button
	};
	for (int id : ids) {
	    Button button = (Button) dialog.findViewById(id);
	    button.setOnClickListener(this);
	}

	int[] ids2 = {
		R.id.member_password_clear_button,
		R.id.member_password2_clear_button
	};
	for (int id : ids2) {
	    Button button = (Button) dialog.findViewById(id);
	    button.setTag(Utils.DialogFragment.CLEAR_BUTTON_TAG);
	    button.setOnClickListener(this);
	}

	View table = getDialog().findViewById(R.id.member_password_table);
	table.setVisibility(View.GONE);
    }

    private void onAdvanceTabActived() {
	final Preferences prefs = Preferences.getPreferences();
	AlertDialog dialog = (AlertDialog) getDialog();

	int[] ids = {
		R.id.admin_logout_button,
		R.id.admin_password_button,
		R.id.system_settings_button
	};
	for (int id : ids) {
	    Button button = (Button) dialog.findViewById(id);
	    button.setOnClickListener(this);
	}

	CheckBox checkbox = (CheckBox) dialog.findViewById(R.id.auto_logout_checkbox);
	checkbox.setOnCheckedChangeListener(this);
	checkbox.setChecked(prefs.getAdministratorAutoLogout());

	EditText edittext = (EditText) dialog.findViewById(R.id.auto_logout_time_edittext);
	edittext.setText(String.valueOf(prefs.getAdministratorAutoLogoutTime()));
	edittext.addTextChangedListener(new TextWatcher() {
	    @Override
	    public void afterTextChanged(Editable s) {
	    }
	    @Override
	    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
	    }
	    @Override
	    public void onTextChanged(CharSequence s, int start, int before, int count) {
		if (s.length() > 0)
		    prefs.setAdministratorAutoLogoutTime(Integer.parseInt(s.toString()));
		else
		    prefs.setAdministratorAutoLogoutTime(0);
	    }
	});
    }

}


class MemberListDialog extends Utils.DialogFragment
{
    private ListView listview;

    private View buildLayout() {
	listview = new ListView(getActivity());

	return listview;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
	super.onCreateDialog(savedInstanceState);

	AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

	builder.setTitle("会员列表");
	builder.setView(buildLayout());

	return builder.create();
    }

    @Override
    public void onStart() {
	super.onStart();

	AlertDialog dialog = (AlertDialog) getDialog();

	MemberListAdapter adapter = new MemberListAdapter();
	listview.setAdapter(adapter);
    }

    private class MemberListAdapter extends BaseAdapter
    {
	private ArrayList<Member.MemberProfile> members;

	public MemberListAdapter() {
	    members = EFormSQLite.Member.listAll(getActivity());
	}

	@Override
	public int getCount() {
	    return 2;
//	    if (members == null)
//		return 0;
//	    else
//		return members.size();
	}

	@Override
	public Object getItem(int position) {
	    if (members == null || position >= members.size())
		return null;
	    else
		return members.get(position);
	}

	@Override
	public long getItemId(int position) {
	    if (members == null || position >= members.size())
		return position;

	    MemberProfile profile = members.get(position);
	    return profile.rowid;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
	    TextView textview = new TextView(parent.getContext());
	    textview.setText("HELLO");
	    return textview;
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

    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;

    private Preferences() {
	prefs = EFormApplication.getSharedPreferences();
	editor = null;
    }

    public void showDialog(FragmentManager manager) {
	PreferencesDialog dialog = new PreferencesDialog();
	dialog.show(manager, "PreferencesDialog");
    }

    public void beginTransaction() {
	if (editor != null) {
	    LogActivity.writeLog("Preferences transaction has been reenter");
	    return;
	}
	editor = prefs.edit();
    }

    public void applyTransaction() {
	if (editor != null) {
	    editor.apply();
	    editor = null;
	}
    }

    public void endTransaction() {
	if (editor != null) {
	    editor.commit();
	    editor = null;
	}
    }

    public void setAdministratorAutoLogout(boolean value) {
	if (editor != null) {
	    editor.putBoolean("AutoLogout", value);
	}
    }

    public boolean getAdministratorAutoLogout() {
	return prefs.getBoolean("AutoLogout", true);
    }

    public void setAdministratorAutoLogoutTime(int minutes) {
	if (editor != null) {
	    if (minutes > 0)
		editor.putInt("AutoLogoutTime", minutes);
	    else
		editor.remove("AutoLogoutTime");
	}
    }

    public int getAdministratorAutoLogoutTime() {
	return prefs.getInt("AutoLogoutTime", 10);
    }

    public void setMagcardDevpath(String path) {
	if (editor != null) {
	    if (path != null)
		editor.putString("MagcardDevpath", path);
	    else
		editor.remove("MagcardDevpath");
	}
    }

    public String getMagcardDevpath() {
	return prefs.getString("MagcardDevpath", "/dev/ttyS0");
    }

    public void setAftermarketName(String name) {
	if (editor != null) {
	    editor.putString("AftermarketName", name);
	}
    }

    public String getAftermarketName() {
	return prefs.getString("AftermarketName", "");
    }

    public void setAftermarketPhone(String phone) {
	if (editor != null) {
	    editor.putString("AftermarketPhone", phone);
	}
    }

    public String getAftermarketPhone() {
	return prefs.getString("AftermarketPhone", "");
    }

}
