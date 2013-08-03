/* EForm - Electronic Form System
 *
 * Copyright (C) 2013 Wu Xiaohu. All rights reserved.
 * Copyright (C) 2013 Cansiny Trade Co.,Ltd. All rights reserved.
 */
package com.cansiny.eform;

import java.io.IOException;
import java.io.InputStream;

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
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;


class MemberLoginDialog extends Utils.DialogFragment
	implements Administrator.AdministratorLoginDialogListener
{
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
	textview.setText("证件号码：");
	textview.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
	textview.setOnClickListener(this);
	TableRow.LayoutParams params = new TableRow.LayoutParams();
	row.addView(textview, params);

	userid_edittext = new EditText(getActivity());
	userid_edittext.setInputType(InputType.TYPE_CLASS_PHONE);
	userid_edittext.setFilters(new InputFilter[] {new InputFilter.LengthFilter(18)});
	userid_edittext.setMinEms(10);
	userid_edittext.setHintTextColor(getResources().getColor(R.color.silver));
	userid_edittext.setEnabled(false);
	row.addView(userid_edittext);

	addClearButton(row);

	Button button = new Button(getActivity());
	button.setText("读身份证");
	button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
	button.setTextColor(getResources().getColor(R.color.purple));
	button.setBackgroundResource(R.drawable.button);
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
	button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
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
	button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
	button.setOnClickListener(new View.OnClickListener() {
	    @Override
	    public void onClick(View view) {
		hideIme(view);
		showToastLong("您可以通过下面步骤找回密码：\n\n" +
			"    1、带上注册会员时使用的身份证件；\n" +
			"    2、联系管理员设置新密码；", R.drawable.tips);
	    }
	});

	button = dialog.getButton(Dialog.BUTTON_POSITIVE);
	button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
	button.setOnClickListener(new View.OnClickListener() {
	    @Override
	    public void onClick(View view) {
		if (userid_edittext.length() == 0) {
		    userid_edittext.setHint("证件号码不能为空");
		    userid_edittext.requestFocus();
		    return;
		}
		if (password_edittext.length() == 0) {
		    password_edittext.setHint("登录密码不能为空");
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
		    showToastLong("证件号码或密码错，请重试! \n\n" +
			    "如忘记密码，请选择“忘记密码”，根据提示找回密码。\n" +
			    "如不是会员，请选择“注册会员”，根据提示注册会员。\n", R.drawable.cry);
		}
	    }
	});
    }

    @Override
    public void onClick(View view) {
	super.onClick(view);
	if (view.getClass().equals(TextView.class)) {
	    userid_edittext.setEnabled(true);
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

	AlertDialog dialog = (AlertDialog) getDialog();

	Button button = dialog.getButton(Dialog.BUTTON_NEGATIVE);
	button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);

	button = dialog.getButton(Dialog.BUTTON_POSITIVE);
	button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
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

	builder.setNegativeButton("取 消", null);

	if (profile == null)
	    builder.setPositiveButton("注 册", null);
	else
	    builder.setPositiveButton("更 新", null);

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

	    dialog.findViewById(R.id.idcard_read_button).setVisibility(View.GONE);
	}

	View view = dialog.findViewById(R.id.idcard_read_button);
	view.setOnClickListener(this);

	view = dialog.findViewById(R.id.password_clear);
	view.setTag(Utils.DialogFragment.CLEAR_BUTTON_TAG);
	view.setOnClickListener(this);
	view = dialog.findViewById(R.id.password2_clear);
	view.setTag(Utils.DialogFragment.CLEAR_BUTTON_TAG);
	view.setOnClickListener(this);
	view = dialog.findViewById(R.id.company_clear);
	view.setTag(Utils.DialogFragment.CLEAR_BUTTON_TAG);
	view.setOnClickListener(this);
	view = dialog.findViewById(R.id.phone_clear);
	view.setTag(Utils.DialogFragment.CLEAR_BUTTON_TAG);
	view.setOnClickListener(this);

	password_edittext.requestFocus();

	Button button = dialog.getButton(Dialog.BUTTON_NEGATIVE);
	button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
	button.setOnClickListener(new View.OnClickListener() {
	    @Override
	    public void onClick(View view) {
		hideIme(view);
		dismiss();
	    }
	});

	button = dialog.getButton(Dialog.BUTTON_POSITIVE);
	button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
	button.setOnClickListener(new View.OnClickListener() {
	    @Override
	    public void onClick(View view) {
		if (userid_edittext.length() == 0) {
		    userid_edittext.setHint("证件号码不能为空");
		    return;
		}
		if (username_edittext.length() == 0) {
		    username_edittext.setHint("会员姓名不能为空");
		    return;
		}

		if (profile == null) {
		    if (password_edittext.length() == 0) {
			password_edittext.setText("密码不能为空");
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
			password_edittext.requestFocus();
			return;
		    }
		    if (!password.equals(password2)) {
			password2_edittext.setText("");
			password2_edittext.setHint("确认密码不一致");
			password2_edittext.requestFocus();
			return;
		    }
		}

		hideIme(view);

		Member member = Member.getMember();
		if (profile == null) {
		    if (member.register(userid, username, password, company, phone)) {
			showToast("注册成功！您现在可以通过证件号码和密码来登录系统使用会员功能。",
				R.drawable.smile);
			dismiss();
		    } else {
			showToast("注册失败！", R.drawable.cry);
		    }
		} else {
		    if (member.update(userid, username, password, company, phone)) {
			showToast("会员信息已成功更新！", R.drawable.smile);
			dismiss();
		    } else {
			showToast("不能更新会员信息！", R.drawable.cry);
		    }
		}
	    }
	});
    }


    @Override
    public void onClick(View view) {
	super.onClick(view);

	switch (view.getId()) {
	case R.id.idcard_read_button:
	    break;
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


    public boolean isLogin() {
	return is_login;
    }

    public boolean login(Context context, String userid, String password) {
	ContentValues values = EFormSQLiteHelper.Member.login(context, userid, password);
	if (values == null)
	    return false;

	is_login = true;

	profile = new MemberProfile();
	profile.rowid = values.getAsLong(EFormSQLiteHelper.Member.COLUMN_ID);
	profile.userid = values.getAsString(EFormSQLiteHelper.Member.COLUMN_USERID);
	profile.username = values.getAsString(EFormSQLiteHelper.Member.COLUMN_USERNAME);
	profile.company = values.getAsString(EFormSQLiteHelper.Member.COLUMN_COMPANY);
	profile.phone = values.getAsString(EFormSQLiteHelper.Member.COLUMN_PHONE);

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

    public boolean register(String userid, String username, String password,
	    String company, String phone) {
	if (password == null || password.length() == 0)
	    return false;

	return true;
    }

    public boolean update(String userid, String username, String password,
	    String company, String phone) {
	return true;
    }

    public void update(FragmentManager manager) {
	if (is_login) {
	    MemberProfileDialog dialog = new MemberProfileDialog();
	    dialog.setProfile(profile);
	    dialog.show(manager, "MemberProfileDialog");
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
	public String company;
	public String phone;
    }
}
