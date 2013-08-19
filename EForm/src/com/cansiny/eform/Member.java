/* EForm - Electronic Form System
 *
 * Copyright (C) 2013 Wu Xiaohu. All rights reserved.
 * Copyright (C) 2013 Cansiny Trade Co.,Ltd. All rights reserved.
 */
package com.cansiny.eform;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import com.cansiny.eform.Utils.Device;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.FragmentManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.text.method.ScrollingMovementMethod;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;


class MemberLoginDialog extends Utils.DialogFragment
	implements Administrator.AdministratorLoginDialogListener
{
    private static final int TAG_IDNO_TEXTVIEW = 1;
    private static final int TAG_IDCARD_BUTTON = 2;

    private EditText userid_edittext;
    private EditText password_edittext;

    private View buildLayout() {
	TableLayout table = new TableLayout(getActivity());
	table.setPadding(20, 20, 20, 20);
	table.setColumnStretchable(1, true);

	TableRow row = new TableRow(getActivity());
	row.setGravity(Gravity.CENTER_VERTICAL);
	table.addView(row);

	TextView textview = new TextView(getActivity());
	textview.setTag(TAG_IDNO_TEXTVIEW);
	textview.setText("证件号码：");
	textview.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
	textview.setOnClickListener(this);
	TableRow.LayoutParams params = new TableRow.LayoutParams();
	row.addView(textview, params);

	userid_edittext = new EditText(getActivity());
	userid_edittext.setInputType(InputType.TYPE_CLASS_PHONE);
	userid_edittext.setFilters(new InputFilter[] {new InputFilter.LengthFilter(18)});
	userid_edittext.setMinEms(10);
	userid_edittext.setTextColor(getResources().getColor(R.color.black));
	userid_edittext.setHintTextColor(getResources().getColor(R.color.silver));
	userid_edittext.setEnabled(false);
	row.addView(userid_edittext);

	addClearButton(row);

	Button button = new Button(getActivity());
	button.setTag(TAG_IDCARD_BUTTON);
	button.setText("读身份证");
	button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
	button.setTextColor(getResources().getColor(R.color.purple));
	button.setBackgroundResource(R.drawable.button);
	button.setOnClickListener(this);
	params = new TableRow.LayoutParams(
		ViewGroup.LayoutParams.WRAP_CONTENT,
		ViewGroup.LayoutParams.WRAP_CONTENT);
	params.leftMargin = (int) Utils.convertDpToPixel(20);
	row.addView(button, params);


	row = new TableRow(getActivity());
	row.setGravity(Gravity.CENTER_VERTICAL);
	TableLayout.LayoutParams table_params = new TableLayout.LayoutParams();
	table_params.topMargin = (int) Utils.convertDpToPixel(20);
	table.addView(row, table_params);

	textview = new TextView(getActivity());
	textview.setText("登录密码：");
	textview.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
	row.addView(textview);

	password_edittext = new EditText(getActivity());
	password_edittext.setInputType(InputType.TYPE_CLASS_NUMBER |
		InputType.TYPE_NUMBER_VARIATION_PASSWORD);
	password_edittext.setFilters(new InputFilter[] {new InputFilter.LengthFilter(6)});
	password_edittext.setHintTextColor(getResources().getColor(R.color.silver));
	row.addView(password_edittext);

	addClearButton(row);

	return table;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
	super.onCreateDialog(savedInstanceState);

	AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
	builder.setTitle("会员登录");

	builder.setView(buildLayout());

	builder.setNegativeButton("注册会员", null);
	builder.setNeutralButton("忘记密码", null);
	builder.setPositiveButton("登 陆", null);

	return builder.create();
    }


    @Override
    public void onStart() {
	super.onStart();

	AlertDialog dialog = (AlertDialog) getDialog();

	Button button = dialog.getButton(Dialog.BUTTON_NEGATIVE);
	button.setOnClickListener(new View.OnClickListener() {
	    @Override
	    public void onClick(View view) {
		hideIme(view);
		Administrator admin = Administrator.getAdministrator();
		if (admin.isLogin()) {
		    dismiss();
		    MemberRegisterDialog dialog = new MemberRegisterDialog();
		    dialog.show(getFragmentManager(), "MemberRegisterDialog");
		} else {
		    showToast("需要管理员登录...", R.drawable.tips);
		    admin.setLoginDialogListener(MemberLoginDialog.this);
		    admin.login(getFragmentManager());
		}
	    }
	});

	button = dialog.getButton(Dialog.BUTTON_NEUTRAL);
	button.setOnClickListener(new View.OnClickListener() {
	    @Override
	    public void onClick(View view) {
		hideIme(view);
		showToast("请带上注册会员时使用的身份证件，联系管理员以设置新密码；", R.drawable.tips);
	    }
	});

	button = dialog.getButton(Dialog.BUTTON_POSITIVE);
	button.setOnClickListener(new View.OnClickListener() {
	    @Override
	    public void onClick(View view) {
		if (userid_edittext.length() == 0) {
		    userid_edittext.setHint("证件号码不能为空");
		    userid_edittext.setHintTextColor(getResources().getColor(R.color.red));
		    userid_edittext.requestFocus();
		    return;
		}
		if (password_edittext.length() != 6) {
		    password_edittext.setText("");
		    password_edittext.setHint("密码必须是6位");
		    password_edittext.setHintTextColor(getResources().getColor(R.color.red));
		    password_edittext.requestFocus();
		    return;
		}
		hideIme(view);

		Member member = Member.getMember();
		if (member.login(getActivity(), userid_edittext.getText().toString(),
			password_edittext.getText().toString())) {
		    dismiss();
		    Utils.showToast("登录成功!", R.drawable.smile);
		} else {
		    showToast("证件号码或密码错误，请重试!", R.drawable.cry);
		}
	    }
	});
    }

    @Override
    public void onClick(View view) {
	super.onClick(view);

	if (view.getTag() == null)
	    return;

	if (view.getTag() instanceof Integer) {
	    switch(((Integer) view.getTag()).intValue()) {
	    case TAG_IDNO_TEXTVIEW:
		userid_edittext.setEnabled(true);
		break;
	    case TAG_IDCARD_BUTTON:
		Device idcard = Device.getDevice(Device.DEVICE_IDCARD);
		if (idcard == null) {
		    showToast("不能打开身份证阅读器，请联系管理员检查设备配置", R.drawable.cry);
		    return;
		}
		idcard.setListener(new Device.DeviceListener() {
		    @Override
		    public void onDeviceTaskSuccessed(Device device, Object result) {
			IDCard.IDCardInfo info = (IDCard.IDCardInfo) result;
			userid_edittext.setText(info.idno);
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
		idcard.read(getFragmentManager());
		break;
	    }
	}
    }

    @Override
    public void onAdministratorLoginDialogDisapper(Administrator admin) {
	admin.setLoginDialogListener(null);

	if (admin.isLogin()) {
	    dismiss();
	    MemberRegisterDialog dialog = new MemberRegisterDialog();
	    dialog.show(getFragmentManager(), "MemberRegisterDialog");
	}
    }

}


class MemberRegisterDialog extends Utils.DialogFragment
{
    private View buildLayout() {
	TextView textview = new TextView(getActivity());
	textview.setMaxLines(20);
	textview.setLineSpacing(1, 1.4f);
	textview.setScrollContainer(true);
	textview.setMovementMethod(new ScrollingMovementMethod());
	textview.setPadding(20, 10, 20, 10);
	textview.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
	try {
	    /* TODO: read protocol file from data dir */
	    AssetManager assets = getActivity().getAssets();
	    InputStream stream = assets.open("member_protocol.txt");

	    byte[] buffer = new byte[10240];
	    int count = stream.read(buffer, 0, buffer.length);
	    if (count >= buffer.length) {
		LogActivity.writeLog("会员注册协议文件大于10K，超出的部分不显示。");
	    }
	    stream.close();
	    textview.setText(new String(buffer, 0, count, "UTF-8"));
	} catch (IOException e) {
	    LogActivity.writeLog(e);
	    textview.setText("协议文件未找到，请联系管理员。");
	}
	return textview;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
	super.onCreateDialog(savedInstanceState);

	AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
	builder.setTitle("会员注册协议");

	builder.setView(buildLayout());

	builder.setNegativeButton("取 消", null);
	builder.setPositiveButton("继续注册", new DialogInterface.OnClickListener() {
	    @Override
	    public void onClick(DialogInterface dialog, int which) {
		dismiss();
		MemberProfileDialog dialog2 = new MemberProfileDialog();
		dialog2.show(getFragmentManager(), "MemberProfileDialog");
	    }
	});

	return builder.create();
    }


    @Override
    public void onStart() {
	super.onStart();

	setCancelable(false);
    }

}


class MemberProfileDialog extends Utils.DialogFragment
{
    private EditText userid_edittext;
    private EditText username_edittext;
    private EditText password_edittext;
    private EditText password2_edittext;
    private EditText company_edittext;
    private EditText phone_edittext;
    private Member.MemberProfile profile = null;

    public void setProfile(Member.MemberProfile profile) {
	this.profile = profile;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
	super.onCreateDialog(savedInstanceState);

	AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

	if (profile == null)
	    builder.setTitle("注册会员");
	else
	    builder.setTitle("更新会员信息");

	LayoutInflater inflater = getActivity().getLayoutInflater();
	builder.setView(inflater.inflate(R.layout.dialog_member, null));


	if (profile == null) {
	    builder.setNegativeButton("放 弃", null);
	    builder.setPositiveButton("注 册", null);
	} else {
	    builder.setNegativeButton("关闭窗口", null);
	    builder.setNeutralButton("注销会员", null);
	    builder.setPositiveButton("更新信息", null);
	}

	return builder.create();
    }

    @Override
    public void onStart() {
	super.onStart();

	setCancelable(false);

	final AlertDialog dialog = (AlertDialog) getDialog();

	userid_edittext = (EditText) dialog.findViewById(R.id.userid_edittext);
	username_edittext = (EditText) dialog.findViewById(R.id.username_edittext);
	password_edittext = (EditText) dialog.findViewById(R.id.password_edittext);
	password2_edittext = (EditText) dialog.findViewById(R.id.password2_edittext);
	company_edittext = (EditText) dialog.findViewById(R.id.company_edittext);
	phone_edittext = (EditText) dialog.findViewById(R.id.phone_edittext);

	if (profile != null) {
	    userid_edittext.setText(profile.userid);
	    username_edittext.setText(profile.username);
	    company_edittext.setText(profile.company);
	    phone_edittext.setText(profile.phone);

	    View button = dialog.findViewById(R.id.idcard_read_button);
	    button.setVisibility(View.GONE);
	} else {
	    View button = dialog.findViewById(R.id.idcard_read_button);
	    button.setOnClickListener(this);
	}

	int[] ids = {
		R.id.password_clear,
		R.id.password2_clear,
		R.id.company_clear,
		R.id.phone_clear
	};
	for (int id : ids) {
	    View view = dialog.findViewById(id);
	    view.setTag(Utils.DialogFragment.CLEAR_BUTTON_TAG);
	    view.setOnClickListener(this);
	}

	if (profile == null)
	    password_edittext.requestFocus();

	Button button = dialog.getButton(Dialog.BUTTON_NEGATIVE);
	button.setOnClickListener(new View.OnClickListener() {
	    @Override
	    public void onClick(View view) {
		hideIme(view);
		dismiss();
	    }
	});

	button = dialog.getButton(Dialog.BUTTON_POSITIVE);
	button.setOnClickListener(new View.OnClickListener() {
	    @Override
	    public void onClick(View view) {
		if (userid_edittext.length() == 0) {
		    userid_edittext.setHint("证件号码不能为空");
		    userid_edittext.setHintTextColor(getResources().getColor(R.color.red));
		    return;
		}
		if (username_edittext.length() == 0) {
		    username_edittext.setHint("会员姓名不能为空");
		    username_edittext.setHintTextColor(getResources().getColor(R.color.red));
		    return;
		}

		if (profile == null) {
		    if (password_edittext.length() != 6) {
			password_edittext.setText("");
			password_edittext.setHint("密码长度必须是6位");
			password_edittext.setHintTextColor(getResources().getColor(R.color.red));
			password_edittext.requestFocus();
			return;
		    }
		}
		String userid = userid_edittext.getText().toString();
		String username = username_edittext.getText().toString();
		String password = password_edittext.getText().toString();
		String password2 = password2_edittext.getText().toString();
		String company = company_edittext.getText().toString();
		String phone = phone_edittext.getText().toString();

		if (password_edittext.length() > 0) {
		    if (password_edittext.length() != 6) {
			password_edittext.setText("");
			password_edittext.setHint("密码长度必须是6位");
			password_edittext.setHintTextColor(getResources().getColor(R.color.red));
			password_edittext.requestFocus();
			return;
		    }
		    if (!password.equals(password2)) {
			password2_edittext.setText("");
			password2_edittext.setHint("确认密码不一致");
			password2_edittext.setHintTextColor(getResources().getColor(R.color.red));
			password2_edittext.requestFocus();
			return;
		    }
		}

		hideIme(view);

		Member member = Member.getMember();
		if (profile == null) {
		    long rowid = member.register(getActivity(), userid, username,
			    password, company, phone);
		    if (rowid >= 0) {
			Utils.showToast("注册成功！您现在可以通过证件号码和密码来登录系统使用会员功能。",
				R.drawable.smile);
			dismiss();
		    } else if (rowid == -1) {
			showToast("注册失败！", R.drawable.cry);
		    } else if (rowid == -2) {
			showToast("证件号码 " + userid + " 已经注册过。", R.drawable.tips);
		    } else {
			showToast("未知错误！", R.drawable.cry);
		    }
		} else {
		    if (member.update(getActivity(), profile.rowid, password, company, phone)) {
			showToast("会员信息已成功更新！", R.drawable.smile);
			dismiss();
		    } else {
			showToast("更新会员信息失败！", R.drawable.cry);
		    }
		}
	    }
	});

	if (profile != null) {
	    button = dialog.getButton(Dialog.BUTTON_NEUTRAL);
	    button.setOnClickListener(new View.OnClickListener() {
		@Override
		public void onClick(View view) {
		    hideIme(view);
		    dismiss();
		    MemberDeleteDialog dialog = new MemberDeleteDialog();
		    dialog.show(getFragmentManager(), "MemberDeleteDialog");
		}
	    });
	}
    }

    @Override
    public void onClick(View view) {
	super.onClick(view);

	switch (view.getId()) {
	case R.id.idcard_read_button:
	    Device idcard = Device.getDevice(Device.DEVICE_IDCARD);;
	    if (idcard == null) {
		showToast("不能打开身份证阅读器，请联系管理员检查设备配置", R.drawable.cry);
		break;
	    }

	    idcard.setListener(new Device.DeviceListener() {
		@Override
		public void onDeviceTaskSuccessed(Device device, Object result) {
		    IDCard.IDCardInfo info = (IDCard.IDCardInfo) result;
		    userid_edittext.setText(info.idno);
		    username_edittext.setText(info.name);
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
	    idcard.read(getFragmentManager());
	    break;
	}
    }
}


class MemberDeleteDialog extends Utils.DialogFragment
{
    private LinearLayout buildLayout() {
	LinearLayout linear = new LinearLayout(getActivity());
	linear.setOrientation(LinearLayout.HORIZONTAL);
	linear.setPadding(10, 10, 10, 5);

	ImageView image = new ImageView(getActivity());
	image.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
	image.setImageResource(R.drawable.warning);
	LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
		(int) Utils.convertDpToPixel(60),
		(int) Utils.convertDpToPixel(60));
	params.leftMargin = 20;
	params.rightMargin = 20;
	linear.addView(image, params);

	LinearLayout linear2 = new LinearLayout(getActivity());
	linear2.setOrientation(LinearLayout.VERTICAL);
	linear.addView(linear2);

	TextView textview = new TextView(getActivity());
	textview.setText("您真的要注销当前登录的会员吗？");
	textview.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
	linear2.addView(textview);

	textview = new TextView(getActivity());
	textview.setText("注销会员将删除所有与会员关联的数据！");
	textview.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17);
	params = new LinearLayout.LayoutParams(
		ViewGroup.LayoutParams.MATCH_PARENT,
		ViewGroup.LayoutParams.WRAP_CONTENT);
	params.topMargin = 20;
	linear2.addView(textview, params);

	textview = new TextView(getActivity());
	textview.setText("您必须明白这是不可逆的操作，会员数据一旦删除将无法找回，请务必在注销前做好数据备份！");
	textview.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
	textview.setTextColor(getResources().getColor(R.color.red));
	params = new LinearLayout.LayoutParams(
		ViewGroup.LayoutParams.MATCH_PARENT,
		ViewGroup.LayoutParams.WRAP_CONTENT);
	params.topMargin = 20;
	linear2.addView(textview, params);

	return linear;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
	super.onCreateDialog(savedInstanceState);

	AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

	builder.setTitle("注销会员");
	builder.setView(buildLayout());
	builder.setNegativeButton("放 弃", null);
	builder.setPositiveButton("确认注销", null);

	return builder.create();
    }

    @Override
    public void onStart() {
	super.onStart();

	final AlertDialog dialog = (AlertDialog) getDialog();

	Button button = dialog.getButton(Dialog.BUTTON_POSITIVE);
	button.setOnClickListener(new View.OnClickListener() {
	    @Override
	    public void onClick(View view) {
		showToast("请提供会员的身份证件！");

		Device idcard = Device.getDevice(Device.DEVICE_IDCARD);
		if (idcard == null) {
		    showToast("不能打开身份证阅读器，请联系管理员检查设备配置", R.drawable.cry);
		    return;
		}

		idcard.setListener(new Device.DeviceListener() {
		    @Override
		    public void onDeviceTaskSuccessed(Device device, Object result) {
			Member member = Member.getMember();

			if (!member.isLogin() || member.getProfile() == null) {
			    dismiss();
			    return;
			}

			IDCard.IDCardInfo info = (IDCard.IDCardInfo) result;
			if (member.getProfile().userid.equals(info.idno)) {
			    if (member.delete(getActivity())) {
				Utils.showToast("会员注销成功！");
			    } else {
				Utils.showToast("注销会员失败！", R.drawable.cry);
			    }
			    dismiss();
			} else {
			    showToastLong("必须提供会员注册时使用的的身份证才能注销！", R.drawable.cry);
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
		idcard.read(getFragmentManager());
	    }
	});
    }
}


class MemberVouchersDialog extends Utils.DialogFragment
	implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener
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

	LinearLayout linear = new LinearLayout(getActivity());
	linear.setOrientation(LinearLayout.VERTICAL);

	listview = new ListView(getActivity());
	listview.setOnItemClickListener(this);
	listview.setOnItemLongClickListener(this);
	linear.addView(listview);

	View view = new View(getActivity());
	view.setBackgroundResource(R.color.silver);
	linear.addView(view, new LinearLayout.LayoutParams(
		ViewGroup.LayoutParams.MATCH_PARENT, 1));

	TextView textview = new TextView(getActivity());
	textview.setText("点击打开选择的凭条；" +
		"长按可以删除选择的凭条；" +
		"点击描述文字或后面的图标可以编辑描述信息；");
	textview.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
	textview.setPadding(10, 10, 10, 5);
	textview.setLineSpacing(1, 1.2f);
	textview.setTextColor(getActivity().getResources().getColor(R.color.black));
	linear.addView(textview);

	builder.setView(linear);

	return builder.create();
    }

    @Override
    public void onStart() {
	super.onStart();

	VoucherListAdapter adapter = new VoucherListAdapter();
	adapter.loadFromDB();
	listview.setAdapter(adapter);

	total_textview.setText("共 " + adapter.getVoucherCount() + " 张凭证");

	if(adapter.getCount() > 8) {
	    View item = adapter.getView(0, null, listview);
	    item.measure(0, 0);
	    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
		    ViewGroup.LayoutParams.MATCH_PARENT,
		    (int) (8.5 * item.getMeasuredHeight()));
	    listview.setLayoutParams(params);
	}
    }

    @Override
    public void onClick(View view) {
	super.onClick(view);

	VoucherListAdapter adapter = (VoucherListAdapter) listview.getAdapter();
	if (adapter.getCount() <= 0)
	    return;

	View item = adapter.getView(0, null, listview);
	int distance = 0;
	if (item != null) {
	    item.measure(0, 0);
	    distance = (int) (2.2 * item.getMeasuredHeight());
	}

	int tagval = ((Integer) view.getTag()).intValue();
	switch(tagval) {
	case TAG_BUTTON_SCROLLUP:
	    listview.smoothScrollBy(0 - distance, 200);
	    break;
	case TAG_BUTTON_SCROLLDOWN:
	    listview.smoothScrollBy(distance, 200);
	    break;
	default:
	    break;
	}
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long rowid) {
	Voucher voucher = (Voucher) parent.getItemAtPosition(position);
	if (voucher == null)
	    return;

	voucher.updateAccessTime(getActivity());

	dismiss();

	Activity activity = getActivity();
	if (activity instanceof HomeActivity) {
	    HomeActivity home = (HomeActivity) activity;
	    home.startFormActivity(voucher);
	}
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long rowid) {
	final Voucher voucher = (Voucher) parent.getItemAtPosition(position);
	if (voucher == null)
	    return false;

	AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
	builder.setTitle("删除凭条");
	builder.setMessage("\n确认要删除选择的凭条吗？\n");
	builder.setNegativeButton("取 消", null);
	builder.setPositiveButton("删 除", new DialogInterface.OnClickListener() {
	    @Override
	    public void onClick(DialogInterface dialog, int which) {
		if (voucher.delete(getActivity())) {
		    showToast("凭条已删除!");
		    VoucherListAdapter adapter = (VoucherListAdapter) listview.getAdapter();
		    adapter.loadFromDB();
		    if(adapter.getCount() <= 8) {
			View item = adapter.getView(0, null, listview);
			if (item != null) {
			    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
				    ViewGroup.LayoutParams.MATCH_PARENT,
				    ViewGroup.LayoutParams.WRAP_CONTENT);
			    listview.setLayoutParams(params);
			}
		    }
		    total_textview.setText("共 " + adapter.getVoucherCount() + " 张凭证");
		    adapter.notifyDataSetChanged();
		}
	    }
	});
	Dialog confirm = builder.create();

	confirm.setOnShowListener(new DialogInterface.OnShowListener() {
	    @Override
	    public void onShow(DialogInterface dialog) {
		Button button = ((AlertDialog) dialog).getButton(Dialog.BUTTON_NEGATIVE);
		button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
		button = ((AlertDialog) dialog).getButton(Dialog.BUTTON_POSITIVE);
		button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
	    }
	});
	confirm.show();

	return true;
    }

    class VoucherListAdapter extends BaseAdapter
    {
	private ArrayList<Voucher> vouchers = null;

	public VoucherListAdapter() {
	    vouchers = null;
	}

	public void loadFromDB() {
	    Member member = Member.getMember();
	    Member.MemberProfile profile = member.getProfile();

	    if (member.isLogin() && profile != null) {
		vouchers = EFormSQLite.Voucher.listForUser(getActivity(),
			profile.password, profile.rowid);
	    }
	}

	@Override
	public int getCount() {
	    if (vouchers == null || vouchers.size() == 0)
		return 1;
	    else
		return vouchers.size();
	}

	public int getVoucherCount() {
	    return (vouchers != null) ? vouchers.size() : 0;
	}

	@Override
	public Object getItem(int position) {
	    if (vouchers == null || position < 0 || position >= vouchers.size())
		return null;
	    return vouchers.get(position);
	}

	@Override
	public long getItemId(int position) {
	    if (vouchers == null || position < 0 || position >= vouchers.size())
		return position;
	    return vouchers.get(position).getRowid();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
	    if (vouchers == null || position < 0 || position >= vouchers.size()) {
		TextView textview = new TextView(parent.getContext());
		textview.setText("没有保存的凭条信息！");
		textview.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17);
		textview.setPadding(10, 10, 10, 10);
		textview.setTextColor(parent.getResources().getColor(R.color.red));
		return textview;
	    }

	    Voucher voucher = vouchers.get(position);
	    Context context = parent.getContext();

	    LinearLayout linear = new LinearLayout(context);
	    linear.setPadding(10, 6, 8, 2);
	    linear.setDescendantFocusability(ViewGroup.FOCUS_BEFORE_DESCENDANTS);

	    ImageView image = new ImageView(context);
	    image.setImageResource(voucher.getFormImage());
	    image.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
	    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
		    (int) Utils.convertDpToPixel(40),
		    (int) Utils.convertDpToPixel(40));
	    params.rightMargin = 10;
	    params.gravity = Gravity.CENTER_VERTICAL;
	    linear.addView(image, params);

	    TableLayout table = new TableLayout(context);
	    table.setColumnStretchable(1, true);
	    params = new LinearLayout.LayoutParams(
		    0, ViewGroup.LayoutParams.WRAP_CONTENT);
	    params.weight = 1;
	    linear.addView(table, params);

	    TableRow row = new TableRow(context);
	    table.addView(row);

	    TextView textview = new TextView(context);
	    textview.setText(voucher.getFormLabel().replace("\n", ""));
	    textview.setSingleLine();
	    textview.setTextSize(TypedValue.COMPLEX_UNIT_SP, 21);
	    TableRow.LayoutParams row_params = new TableRow.LayoutParams();
	    row_params.span = 2;
	    row.addView(textview, row_params);

	    row = new TableRow(context);
	    table.addView(row);

	    textview = new TextView(context);
	    textview.setTag(position);
	    textview.setText(voucher.getComment());
	    textview.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17);
	    textview.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.edit, 0);
	    textview.setCompoundDrawablePadding(5);
	    textview.setPadding(0, 2, 0, 0);
	    textview.setOnClickListener(new View.OnClickListener() {
	        @Override
	        public void onClick(View v) {
	            Integer tag = (Integer) v.getTag();
	            final Voucher voucher = vouchers.get(tag.intValue());
	            voucher.setListener(new Voucher.VoucherListener() {
	        	@Override
	        	public void onVoucherChanged(Voucher voucher_new) {
	        	    voucher.setComment(voucher_new.getComment());
	        	    VoucherListAdapter adapter = (VoucherListAdapter) listview.getAdapter();
	        	    adapter.notifyDataSetChanged();
	        	}
	            });
	            voucher.update(getFragmentManager());
	        }
	    });
	    row.addView(textview);

	    textview = new TextView(context);
	    SimpleDateFormat format = new SimpleDateFormat("yyyy年MM月dd日");
	    textview.setText(format.format(voucher.getAtime()));
	    textview.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17);
	    textview.setTextColor(getResources().getColor(R.color.darkgray));
	    textview.setGravity(Gravity.RIGHT);
	    row.addView(textview);

	    return linear;
	}

    }

}


public class Member
{
    static private Member _singleInstance = null;
    static public Member getMember() {
	if (_singleInstance == null) {
	    _singleInstance = new Member();
	}
	return _singleInstance;
    }

    private boolean is_login;
    private MemberProfile profile;
    private MemberListener listener;

    private Member() {
	is_login = false;
	profile = null;
	listener = null;
    }

    public MemberProfile getProfile() {
	return profile;
    }

    public boolean isLogin() {
	return is_login;
    }

    public long register(Context context, String userid, String username,
	    String password, String company, String phone) {
	if (userid == null || userid.length() == 0 ||
		username == null || username.length() == 0 ||
		password == null || password.length() == 0) {
	    LogActivity.writeLog("会员注册的参数无效");
	    return -1;
	}

	Administrator admin = Administrator.getAdministrator();
	if (!admin.isLogin()) {
	    Utils.showToast("不能注册会员，因为管理员未登录或已退出登录。");
	    return -1;
	}

	return EFormSQLite.Member.register(context, userid, username,
		password, company, phone, admin.getPassword());
    }

    public boolean login(Context context, String userid, String password) {
	ContentValues values = EFormSQLite.Member.login(context, userid, password);
	if (values == null)
	    return false;

	is_login = true;

	profile = new MemberProfile();

	profile.rowid = values.getAsLong(EFormSQLite.Member.COLUMN_ID);
	profile.userid = values.getAsString(EFormSQLite.Member.COLUMN_USERID);
	profile.username = values.getAsString(EFormSQLite.Member.COLUMN_USERNAME);
	profile.company = values.getAsString(EFormSQLite.Member.COLUMN_COMPANY);
	profile.phone = values.getAsString(EFormSQLite.Member.COLUMN_PHONE);
	profile.datetime = values.getAsLong(EFormSQLite.Member.COLUMN_DATETIME);
	profile.password = password;

	if (is_login && listener != null) {
	    listener.onMemberLogin();
	}
	return is_login;
    }

    public void login(FragmentManager manager) {
	MemberLoginDialog dialog = new MemberLoginDialog();
	dialog.show(manager, "MemberLoginDialog");
    }

    public void logout() {
	if (is_login) {
	    if (listener != null) {
		listener.onMemberLogout();
	    }
	    profile = null;
	    is_login = false;
	}
    }

    public boolean update(Context context, long rowid, String password,
	    String company, String phone) {
	String admin_passwd = null;

	if (password != null && password.length() > 0) {
	    Administrator admin = Administrator.getAdministrator();
	    if (!admin.isLogin()) {
		Utils.showToast("更新会员密码需要管理员登录");
		return false;
	    } else {
		admin_passwd = admin.getPassword();
	    }
	}
	if (EFormSQLite.Member.update(context, rowid, password,
		company, phone, admin_passwd)) {
	    if (profile != null) {
		profile.company = company;
		profile.password = password;
		profile.phone = phone;
	    }
	    return true;
	}
	return false;
    }

    public void update(FragmentManager manager) {
	if (is_login) {
	    MemberProfileDialog dialog = new MemberProfileDialog();
	    dialog.setProfile(profile);
	    dialog.show(manager, "MemberProfileDialog");
	}
    }

    public boolean delete(Context context) {
	if (is_login) {
	    if (EFormSQLite.Member.delete(context, profile.rowid)) {
		logout();
		return true;
	    }
	}
	return false;
    }

    public void listVouchers(FragmentManager manager) {
	if (is_login) {
	    MemberVouchersDialog dialog = new MemberVouchersDialog();
	    dialog.show(manager, "MemberVouchersDialog");
	}
    }

    public void setListener(MemberListener listener) {
	this.listener = listener;
    }


    public interface MemberListener
    {
	public void onMemberLogin();
	public void onMemberLogout();
    }


    static public class MemberProfile
    {
	public long   rowid;
	public String userid;
	public String username;
	public String password;
	public String company;
	public String phone;
	public long   datetime;
    }
}
