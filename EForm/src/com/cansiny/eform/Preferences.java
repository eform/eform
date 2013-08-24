/* EForm - Electronic Form System
 *
 * Copyright (C) 2013 Wu Xiaohu. All rights reserved.
 * Copyright (C) 2013 Cansiny Trade Co.,Ltd. All rights reserved.
 */
package com.cansiny.eform;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import com.cansiny.eform.Utils.Device;
import com.cansiny.eform.Utils.IDCardAdapter;
import com.cansiny.eform.Utils.MagcardAdapter;
import com.cansiny.eform.Utils.PrinterAdapter;
import com.cansiny.eform.Utils.ProductAdapter;
import com.cansiny.eform.Utils.SerialAdapter;
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
import android.view.Gravity;
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
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
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

    private long member_password_rowid = -1;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
	super.onCreateDialog(savedInstanceState);

	AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
	LayoutInflater inflater = getActivity().getLayoutInflater();
	builder.setView(inflater.inflate(R.layout.dialog_prefs, null));

	Preferences prefs = Preferences.getPreferences();
	prefs.beginTransaction();

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

    private void onMemberPasswordButtonClick() {
	showToast("请提供会员注册时的身份证件");

	Device idcard = Device.getDevice(Device.DEVICE_IDCARD);
	if (idcard == null) {
	    showToast("不能打开身份证阅读器，请联系管理员检查设备配置", R.drawable.cry);
	    return;
	}

	idcard.setListener(new Device.DeviceListener() {
	    @Override
	    public void onDeviceTaskSuccessed(Device device, Object result) {
		IDCard.IDCardInfo info = (IDCard.IDCardInfo) result;
		AlertDialog dialog = (AlertDialog) getDialog();

		View userid_view = dialog.findViewById(R.id.member_userid_textview);
		View table = dialog.findViewById(R.id.member_password_table);

		member_password_rowid = EFormSQLite.Member.getid(getActivity(), info.idno);
		if (member_password_rowid >= 0) {
		    View edit = dialog.findViewById(R.id.member_password_edittext);
		    ((TextView) edit).setText("");
		    edit = dialog.findViewById(R.id.member_password2_edittext);
		    ((TextView) edit).setText("");

		    ((TextView) userid_view).setText(info.idno);
		    table.setVisibility(View.VISIBLE);
		} else {
		    showToast("没有证件号码为“" + info.idno + "”的会员！");
		    ((TextView) userid_view).setText("");
		    table.setVisibility(View.GONE);
		}
	    }
	    @Override
	    public void onDeviceTaskStart(Device device) {
	    }
	    @Override
	    public void onDeviceTaskFailed(Device device) {
	    }
	    @Override
	    public void onDeviceTaskCancelled(Device device) {
	    }
	});
	idcard.startTask(getFragmentManager(), Device.TASK_FLAG_IDCARD_TEST);
    }

    private void onMemberPasswordSetButtonClick() {
	AlertDialog dialog = (AlertDialog) getDialog();

	if (member_password_rowid < 0) {
	    View view = dialog.findViewById(R.id.member_password_table);
	    view.setVisibility(View.GONE);
	}
	Administrator admin = Administrator.getAdministrator();
	String admin_passwd = admin.getPassword();
	if (admin_passwd == null) {
	    View view = dialog.findViewById(R.id.member_password_table);
	    view.setVisibility(View.GONE);
	}

	TextView password_edit = (TextView) dialog.findViewById(R.id.member_password_edittext);
	if (password_edit.length() != 6) {
	    password_edit.setText("");
	    password_edit.setHint("密码必须是6位");
	    password_edit.setHintTextColor(getResources().getColor(R.color.red));
	    password_edit.requestFocus();
	    return;
	}
	TextView password2_edit = (TextView) dialog.findViewById(R.id.member_password2_edittext);
	if (password2_edit.length() != 6) {
	    password2_edit.setText("");
	    password2_edit.setHint("密码必须是6位");
	    password2_edit.setHintTextColor(getResources().getColor(R.color.red));
	    password2_edit.requestFocus();
	    return;
	}

	String password = password_edit.getText().toString();
	String password2 = password2_edit.getText().toString();

	if (!password.equals(password2)) {
	    password2_edit.setText("");
	    password2_edit.setHint("确认密码不一致");
	    password2_edit.setHintTextColor(getResources().getColor(R.color.red));
	    password2_edit.requestFocus();
	    return;
	}

	if (EFormSQLite.Member.update(getActivity(), member_password_rowid,
		password, null, null, admin_passwd)) {
	    showToast("密码修改成功！", R.drawable.smile);

	    View view = dialog.findViewById(R.id.member_password_table);
	    view.setVisibility(View.GONE);

	    Member member = Member.getMember();
	    if (member.isLogin()) {
		Member.MemberProfile profile = member.getProfile();
		if (profile.rowid == member_password_rowid) {
		    profile.password = password;
		}
	    }
	    member_password_rowid = -1;
	} else {
	    showToast("密码修改失败！", R.drawable.cry);
	}
    }

    @Override
    public void onClick(View view) {
	super.onClick(view);

	Administrator admin = Administrator.getAdministrator();
	final Preferences prefs = Preferences.getPreferences();
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
	case R.id.logview_button:
	    Intent intent = new Intent(getActivity(), LogActivity.class);
	    startActivity(intent);
	    break;

	case R.id.magcard_testing_button:
	    Device magcard = Device.getDevice(Device.DEVICE_MAGCARD);
	    if (magcard == null) {
		showToast("测试失败！未找到刷卡设备驱动", R.drawable.cry);
		break;
	    }
	    magcard.setListener(new Device.DeviceListener() {
	        @Override
	        public void onDeviceTaskStart(Device device) {
	        }
	        @Override
	        public void onDeviceTaskCancelled(Device device) {
	        }
	        @Override
	        public void onDeviceTaskSuccessed(Device device, Object result) {
	            showToast("刷卡器配置正确！\n读取的卡号为 " + (String) result, R.drawable.smile);
	        }
	        @Override
	        public void onDeviceTaskFailed(Device device) {
	            showToast("测试刷卡驱动失败！", R.drawable.cry);
	        }
	    });
	    magcard.startTask(getFragmentManager(), Device.TASK_FLAG_MAGCARD_TEST);
	    break;
	case R.id.printer_testing_button:
	    Device printer = Device.getDevice(Device.DEVICE_PRINTER);
	    if (printer == null) {
		showToast("测试失败！未找到打印机驱动", R.drawable.cry);
		break;
	    }
	    ((Printer) printer).setForm(new FormPrintTest(getActivity(), "打印测试"));
	    printer.setListener(new Device.DeviceListener() {
	        @Override
	        public void onDeviceTaskStart(Device device) {
	        }
	        @Override
	        public void onDeviceTaskCancelled(Device device) {
	        }
	        @Override
	        public void onDeviceTaskSuccessed(Device device, Object result) {
	            showToast("打印机配置正确！", R.drawable.smile);
	        }
	        @Override
	        public void onDeviceTaskFailed(Device device) {
	            showToast("测试打印机驱动失败！", R.drawable.cry);
	        }
	    });
	    printer.startTask(getFragmentManager(), 0);
	    break;
	case R.id.idcard_testing_button:
	    Device idcard = Device.getDevice(Device.DEVICE_IDCARD);
	    if (idcard == null) {
		showToast("测试失败！未找到身份证读卡器驱动", R.drawable.cry);
		break;
	    }
	    idcard.setListener(new Device.DeviceListener() {
	        @Override
	        public void onDeviceTaskStart(Device device) {
	        }
	        @Override
	        public void onDeviceTaskCancelled(Device device) {
	        }
	        @Override
	        public void onDeviceTaskSuccessed(Device device, Object result) {
	            IDCard.IDCardInfo info = (IDCard.IDCardInfo) result;
	            showToast("身份证读卡器配置正确！\n读取的身份证号码为 " + info.idno, R.drawable.smile);
	        }
	        @Override
	        public void onDeviceTaskFailed(Device device) {
	            showToast("测试身份证读卡器驱动失败！", R.drawable.cry);
	        }
	    });
	    idcard.startTask(getFragmentManager(), Device.TASK_FLAG_IDCARD_TEST);
	    break;

	case R.id.member_password_button:
	    onMemberPasswordButtonClick();
	    break;
	case R.id.member_password_set_button:
	    onMemberPasswordSetButtonClick();
	    break;
	case R.id.member_password_cancel_button:
	    member_password_rowid = -1;
	    view2 = dialog.findViewById(R.id.member_password_table);
	    view2.setVisibility(View.GONE);
	    break;
	case R.id.member_list_button:
	    MemberListDialog dialog2 = new MemberListDialog();
	    dialog2.show(getFragmentManager(), "MemberListDialog");
	    break;

	case R.id.admin_logout_button:
	    admin.logout();
	    dismiss();
	    break;
	case R.id.admin_password_button:
	    admin.setPassword(getFragmentManager());
	    break;
	case R.id.upgrade_button:
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
		R.id.logview_button,
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

    private void selectDeviceDriver(String device, Spinner spinner) {
	Preferences prefs = Preferences.getPreferences();

	String driver = prefs.getDeviceDriver(device);
	if (driver != null) {
	    SpinnerAdapter adapter = spinner.getAdapter();
	    for (int i = 0; i < adapter.getCount(); i++) {
		Object product = adapter.getItem(i);
		if (driver.equals(((ProductAdapter.Product) product).getDriver())) {
		    spinner.setSelection(i);
		    break;
		}
	    }
	}
    }

    private void setupDeviceDriver(final String device, final Spinner spinner) {
	final Preferences prefs = Preferences.getPreferences();

	selectDeviceDriver(device, spinner);

	spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
	    @Override
	    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		Object product = parent.getItemAtPosition(position);
		String driver = ((ProductAdapter.Product) product).getDriver();

		try {
		    Device devobj = (Device) Class.forName(driver).newInstance();
		    Spinner spinner = (Spinner) parent.getTag();
		    if (devobj.getDeviceType() != Device.DEVICE_TYPE_SERIAL) {
			spinner.setEnabled(false);
		    } else {
			spinner.setEnabled(true);
		    }
		} catch (Exception e) {
		    LogActivity.writeLog(e);
		    return;
		}
		prefs.setDeviceDriver(device, driver);
		prefs.applyTransaction();
	    }
	    @Override
	    public void onNothingSelected(AdapterView<?> parent) {
		prefs.setDeviceDriver(device, null);
		prefs.applyTransaction();
	    }
	});
    }

    private void setupDevicePath(final String device, Spinner spinner) {
	final Preferences prefs = Preferences.getPreferences();

	String path = prefs.getDevicePath(device);
	if (path != null) {
	    SpinnerAdapter adapter = spinner.getAdapter();
	    for (int i = 0; i < adapter.getCount(); i++) {
		Object serial = adapter.getItem(i);
		if (path.equals(((SerialAdapter.Serial) serial).getPath())) {
		    spinner.setSelection(i);
		    break;
		}
	    }
	}

	spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
	    @Override
	    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		Object serial = parent.getItemAtPosition(position);
		prefs.setDevicePath(device, ((SerialAdapter.Serial) serial).getPath());
		prefs.applyTransaction();
	    }
	    @Override
	    public void onNothingSelected(AdapterView<?> parent) {
		prefs.setDevicePath(device, null);
		prefs.applyTransaction();
	    }
	});
    }

    private void onDeviceTabActived() {
	AlertDialog dialog = (AlertDialog) getDialog();

	int[] ids = {
		R.id.magcard_testing_button,
		R.id.printer_testing_button,
		R.id.idcard_testing_button,
	};
	for (int id : ids) {
	    Button button = (Button) dialog.findViewById(id);
	    button.setOnClickListener(this);
	}

	Utils.SerialAdapter device_adapter = new Utils.SerialAdapter();

	Spinner spinner = (Spinner) dialog.findViewById(R.id.magcard_driver_spinner);
	spinner.setTag(dialog.findViewById(R.id.magcard_device_spinner));
	MagcardAdapter magcard_adapter = new MagcardAdapter();
	spinner.setAdapter(magcard_adapter);
	setupDeviceDriver(Device.DEVICE_MAGCARD, spinner);

	spinner = (Spinner) dialog.findViewById(R.id.magcard_device_spinner);
	spinner.setAdapter(device_adapter);
	setupDevicePath(Device.DEVICE_MAGCARD, spinner);

	spinner = (Spinner) dialog.findViewById(R.id.printer_driver_spinner);
	spinner.setTag(dialog.findViewById(R.id.printer_device_spinner));
	PrinterAdapter printer_adapter = new PrinterAdapter();
	spinner.setAdapter(printer_adapter);
	setupDeviceDriver(Device.DEVICE_PRINTER, spinner);

	spinner = (Spinner) dialog.findViewById(R.id.printer_device_spinner);
	spinner.setAdapter(device_adapter);
	setupDevicePath(Device.DEVICE_PRINTER, spinner);

	spinner = (Spinner) dialog.findViewById(R.id.idcard_driver_spinner);
	spinner.setTag(dialog.findViewById(R.id.idcard_device_spinner));
	IDCardAdapter idcard_adapter = new IDCardAdapter();
	spinner.setAdapter(idcard_adapter);
	setupDeviceDriver(Device.DEVICE_IDCARD, spinner);

	spinner = (Spinner) dialog.findViewById(R.id.idcard_device_spinner);
	spinner.setAdapter(device_adapter);
	setupDevicePath(Device.DEVICE_IDCARD, spinner);
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
		R.id.upgrade_button,
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
    private static final int TAG_BUTTON_SCROLLUP   = 0x00010001;
    private static final int TAG_BUTTON_SCROLLDOWN = 0x00010002;

    private ListView listview;
    private TextView total_textview;

    private View customTitleView() {
	LinearLayout linear = new LinearLayout(getActivity());
	linear.setPadding(10, 5, 10, 5);
	linear.setGravity(Gravity.CENTER_VERTICAL);

	total_textview = new TextView(getActivity());
	total_textview.setTextColor(getResources().getColor(R.color.darkgray));
	total_textview.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
	LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
		0, ViewGroup.LayoutParams.WRAP_CONTENT);
	params.weight = 1;
	linear.addView(total_textview, params);

	Button button = new Button(getActivity());
	button.setBackgroundResource(R.drawable.up);
	button.setTag(TAG_BUTTON_SCROLLUP);
	button.setOnClickListener(this);
	params = new LinearLayout.LayoutParams(
		(int) Utils.convertDpToPixel(48),
		(int) Utils.convertDpToPixel(48));
	params.leftMargin = 20;
	params.rightMargin = 10;
	linear.addView(button, params);

	TextView textview = new TextView(getActivity());
	textview.setText("上下滚动");
	textview.setTextColor(getResources().getColor(R.color.black));
	textview.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
	linear.addView(textview);

	button = new Button(getActivity());
	button.setBackgroundResource(R.drawable.down);
	button.setTag(TAG_BUTTON_SCROLLDOWN);
	button.setOnClickListener(this);
	params = new LinearLayout.LayoutParams(
		(int) Utils.convertDpToPixel(48),
		(int) Utils.convertDpToPixel(48));
	params.leftMargin = 10;
	linear.addView(button, params);

	return linear;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
	super.onCreateDialog(savedInstanceState);

	AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

	builder.setCustomTitle(customTitleView());

	listview = new ListView(getActivity());
	builder.setView(listview);

	return builder.create();
    }

    @Override
    public void onStart() {
	super.onStart();

	MemberListAdapter adapter = new MemberListAdapter();
	listview.setAdapter(adapter);

	total_textview.setText("共 " + adapter.getCount() + " 位会员");

	if(adapter.getCount() > 8) {
	    View item = adapter.getView(0, null, listview);
	    item.measure(0, 0);
	    FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
		    ViewGroup.LayoutParams.MATCH_PARENT,
		    (int) (8.5 * item.getMeasuredHeight()));
	    listview.setLayoutParams(params);
	}
    }

    @Override
    public void onClick(View view) {
	super.onClick(view);

	ListAdapter adapter = listview.getAdapter();
	if (adapter.getCount() <= 0)
	    return;

	View item = adapter.getView(0, null, listview);
	item.measure(0, 0);
	int distance = (int) (2.2 * item.getMeasuredHeight());

	switch(((Integer) view.getTag()).intValue()) {
	case TAG_BUTTON_SCROLLUP:
	    listview.smoothScrollBy(0 - distance, 200);
	    break;
	case TAG_BUTTON_SCROLLDOWN:
	    listview.smoothScrollBy(distance, 200);
	    break;
	}
    }


    private class MemberListAdapter extends BaseAdapter
    {
	private ArrayList<Member.MemberProfile> members;

	public MemberListAdapter() {
	    members = EFormSQLite.Member.listAll(getActivity());
	}

	@Override
	public int getCount() {
	    if (members == null)
		return 0;
	    else
		return members.size();
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

	    Member.MemberProfile profile = members.get(position);
	    return profile.rowid;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
	    if (members == null || position >= members.size())
		return null;

	    Member.MemberProfile profile = members.get(position);
	    LinearLayout.LayoutParams params;

	    LinearLayout linear = new LinearLayout(parent.getContext());
	    linear.setOrientation(LinearLayout.HORIZONTAL);
	    linear.setPadding(10, 10, 10, 0);
	    linear.setGravity(Gravity.CENTER_VERTICAL);

	    TextView textview = new TextView(parent.getContext());
	    textview.setText(profile.username);
	    textview.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
	    linear.addView(textview, new LinearLayout.LayoutParams(
		    (int) Utils.convertDpToPixel(120),
		    ViewGroup.LayoutParams.WRAP_CONTENT));

	    LinearLayout linear2 = new LinearLayout(parent.getContext());
	    linear2.setOrientation(LinearLayout.VERTICAL);
	    params = new LinearLayout.LayoutParams(
		    ViewGroup.LayoutParams.MATCH_PARENT,
		    ViewGroup.LayoutParams.WRAP_CONTENT);
	    linear.addView(linear2, params);

	    textview = new TextView(parent.getContext());
	    if (profile.company == null || profile.company.length() == 0)
		textview.setText("-");
	    else
		textview.setText(profile.company);
	    textview.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
	    linear2.addView(textview);

	    LinearLayout linear3 = new LinearLayout(parent.getContext());
	    linear3.setOrientation(LinearLayout.HORIZONTAL);
	    linear3.setGravity(Gravity.CENTER_VERTICAL);
	    params = new LinearLayout.LayoutParams(
		    ViewGroup.LayoutParams.MATCH_PARENT,
		    ViewGroup.LayoutParams.WRAP_CONTENT);
	    linear2.addView(linear3, params);

	    ImageView image = new ImageView(parent.getContext());
	    image.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
	    image.setImageResource(R.drawable.phone);
	    params = new LinearLayout.LayoutParams(
		    (int) Utils.convertDpToPixel(16),
		    (int) Utils.convertDpToPixel(16));
	    params.rightMargin = (int) Utils.convertDpToPixel(2);
	    linear3.addView(image, params);

	    textview = new TextView(parent.getContext());
	    if (profile.phone == null || profile.phone.length() == 0)
		textview.setText("-");
	    else {
		if (profile.phone.matches("[0-9]+")) {
		    if (profile.phone.length() == 11 ||
			    profile.phone.length() == 8) {
			StringBuilder builder = new StringBuilder(profile.phone);
			if (profile.phone.length() == 11) {
			    builder.insert(3, "-");
			    builder.insert(8, "-");
			} else {
			    builder.insert(4, "-");
			}
			textview.setText(builder);
		    } else {
			textview.setText(profile.phone);
		    }
		} else {
		    textview.setText(profile.phone);
		}
	    }
	    textview.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
	    linear3.addView(textview);

	    textview = new TextView(parent.getContext());
	    SimpleDateFormat format = new SimpleDateFormat("yyyy年MM月dd日");
	    textview.setText(format.format(profile.datetime));
	    textview.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
	    textview.setGravity(Gravity.RIGHT);
	    params = new LinearLayout.LayoutParams(
		    ViewGroup.LayoutParams.MATCH_PARENT,
		    ViewGroup.LayoutParams.WRAP_CONTENT);
	    params.leftMargin = (int) Utils.convertDpToPixel(20);
	    linear3.addView(textview, params);

	    return linear;
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
	    LogActivity.writeLog("Preferences事务被重入，可能是程序错误。");
	    return;
	}
	editor = prefs.edit();
    }

    public void applyTransaction() {
	if (editor != null) {
	    editor.apply();
	}
    }

    public void endTransaction() {
	if (editor != null) {
	    editor.commit();
	    editor = null;
	}
    }

    private void putObject(String key, Object value) {
	boolean in_transaction = true;

	if (editor == null) {
	    beginTransaction();
	    in_transaction = false;
	}

	if (value == null) {
	    editor.remove(key);
	} else {
	    if (value instanceof Integer) {
		editor.putInt(key, (Integer) value);
	    } else if (value instanceof Long) {
		editor.putLong(key, (Long) value);
	    } else if (value instanceof Float) {
		editor.putFloat(key, (Float) value);
	    } else if (value instanceof String) {
		editor.putString(key, (String) value);
	    } else if (value instanceof Boolean) {
		editor.putBoolean(key, (Boolean) value);
	    } else {
		LogActivity.writeLog("首选项键 %s 的值类型 %s 不能支持，请检查",
			key, value.getClass().getName());
	    }
	}

	if (in_transaction == false) {
	    endTransaction();
	}
    }

    private void removeKey(String key) {
	boolean in_transaction = true;

	if (editor == null) {
	    beginTransaction();
	    in_transaction = false;
	}
	editor.remove(key);

	if (in_transaction == false) {
	    endTransaction();
	}
    }

    public void setAdministratorAutoLogout(boolean value) {
	putObject("AutoLogout", value);
    }

    public boolean getAdministratorAutoLogout() {
	return prefs.getBoolean("AutoLogout", true);
    }

    public void setAdministratorAutoLogoutTime(int minutes) {
	if (minutes > 0) {
	    putObject("AutoLogoutTime", minutes);
	} else {
	    removeKey("AutoLogoutTime");
	}
    }

    public int getAdministratorAutoLogoutTime() {
	return prefs.getInt("AutoLogoutTime", 10);
    }

    public void setDeviceDriver(String device, String driver) {
	putObject(device + ".Driver", driver);
    }

    public String getDeviceDriver(String device) {
	return prefs.getString(device + ".Driver", null);
    }

    public Object getDeviceDriverObject(String device) {
	try {
	    String driver = getDeviceDriver(device);
	    if (driver == null) {
		LogActivity.writeLog("设备%s的驱动没有设置", device);
		return null;
	    }
	    Object object = Class.forName(driver).newInstance();
	    if (object instanceof Utils.Device) {
		return object;
	    } else {
		LogActivity.writeLog("%s不是一个设备驱动", driver);
		return null;
	    }
	} catch (Exception e) {
	    LogActivity.writeLog(e);
	    return null;
	}
    }

    public void setDevicePath(String device, String path) {
	putObject(device + ".Path", path);
    }

    public String getDevicePath(String device) {
	return prefs.getString(device + ".Path", null);
    }

    public void setAftermarketName(String name) {
	putObject("AftermarketName", name);
    }

    public String getAftermarketName() {
	return prefs.getString("AftermarketName", "");
    }

    public void setAftermarketPhone(String phone) {
	putObject("AftermarketPhone", phone);
    }

    public String getAftermarketPhone() {
	return prefs.getString("AftermarketPhone", "");
    }

    public void setPageLeftMargin(Form form, int page_no, int value) {
	String key = form.getClass().getName() + "." + page_no + ".left";
	putObject(key, value);
    }

    public int getPageLeftMargin(Form form, int page_no) {
	String key = form.getClass().getName() + "." + page_no + ".left";
	return prefs.getInt(key, 0);
    }

    public void setPageTopMargin(Form form, int page_no, int value) {
	String key = form.getClass().getName() + "." + page_no + ".top";
	putObject(key, value);
    }

    public int getPageTopMargin(Form form, int page_no) {
	String key = form.getClass().getName() + "." + page_no + ".top";
	return prefs.getInt(key, 0);
    }

}
