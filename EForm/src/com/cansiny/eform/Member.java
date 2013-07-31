/* EForm - Electronic Form System
 *
 * Copyright (C) 2013 Wu Xiaohu. All rights reserved.
 * Copyright (C) 2013 Cansiny Trade Co.,Ltd. All rights reserved.
 */
package com.cansiny.eform;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;


class MemberLoginDialog extends DialogFragment implements OnClickListener
{
    private static final int ID_USERID_EDITTEXT = 0x00010001;
    private static final int ID_PASSWORD_EDITTEXT = 0x00010002;

    private static final int TAG_USERID_CLEAR_BUTTON = 1;
    private static final int TAG_PASSWORD_CLEAR_BUTTON = 2;

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
	TableRow.LayoutParams params = new TableRow.LayoutParams();
	row.addView(textview, params);

	EditText edittext = new EditText(getActivity());
	edittext.setId(ID_USERID_EDITTEXT);
	edittext.setInputType(InputType.TYPE_CLASS_PHONE);
	edittext.setFilters(new InputFilter[] {new InputFilter.LengthFilter(18)});
	edittext.setMinEms(10);
	edittext.setHintTextColor(getResources().getColor(R.color.silver));
	edittext.setEnabled(false);
	params = new TableRow.LayoutParams();
	row.addView(edittext, params);

	Button button = new Button(getActivity());
	button.setTag(TAG_USERID_CLEAR_BUTTON);
	button.setBackgroundResource(R.drawable.clear);
	button.setOnClickListener(this);
	params = new TableRow.LayoutParams(
		(int) HomeActivity.convertDpToPixel(26),
		(int) HomeActivity.convertDpToPixel(26));
	row.addView(button, params);

	button = new Button(getActivity());
	button.setText("读身份证");
	button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
	button.setTextColor(getResources().getColor(R.color.purple));
	button.setBackgroundResource(R.drawable.button);
	params = new TableRow.LayoutParams(
		ViewGroup.LayoutParams.WRAP_CONTENT,
		ViewGroup.LayoutParams.WRAP_CONTENT);
	params.leftMargin = (int) HomeActivity.convertDpToPixel(20);
	row.addView(button, params);


	row = new TableRow(getActivity());
	row.setGravity(Gravity.CENTER_VERTICAL);
	TableLayout.LayoutParams table_params = new TableLayout.LayoutParams();
	table_params.topMargin = (int) HomeActivity.convertDpToPixel(20);
	table.addView(row, table_params);

	textview = new TextView(getActivity());
	textview.setText("登陆密码：");
	textview.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
	row.addView(textview);

	edittext = new EditText(getActivity());
	edittext.setId(ID_PASSWORD_EDITTEXT);
	edittext.setInputType(InputType.TYPE_CLASS_NUMBER |
		InputType.TYPE_NUMBER_VARIATION_PASSWORD);
	edittext.setFilters(new InputFilter[] {new InputFilter.LengthFilter(6)});
	edittext.setHintTextColor(getResources().getColor(R.color.silver));
	params = new TableRow.LayoutParams();
	row.addView(edittext, params);

	button = new Button(getActivity());
	button.setTag(TAG_PASSWORD_CLEAR_BUTTON);
	button.setOnClickListener(this);
	button.setBackgroundResource(R.drawable.clear);
	params = new TableRow.LayoutParams(
		(int) HomeActivity.convertDpToPixel(26),
		(int) HomeActivity.convertDpToPixel(26));
	row.addView(button, params);

	return table;
    }

    private void hideIme(View view) {
	InputMethodManager imm =
		(InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
	imm.hideSoftInputFromWindow(view.getApplicationWindowToken(), 0);
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
	super.onCreateDialog(savedInstanceState);

	AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
	builder.setTitle("会员登陆");

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
		MemberProfileDialog dialog = new MemberProfileDialog();
		dialog.show(getFragmentManager(), "MemberInfoDialog");
	    }
	});

	button = dialog.getButton(Dialog.BUTTON_NEUTRAL);
	button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
	button.setOnClickListener(new View.OnClickListener() {
	    @Override
	    public void onClick(View view) {
		hideIme(view);
		CommonDialog.showBox(getFragmentManager(),
			"找回密码",
			"您可以通过下面步骤找回密码：\n\n" +
				"    1、带上注册会员时使用的身份证件；\n" +
				"    2、联系大堂经理设置新密码；\n");
	    }
	});

	button = dialog.getButton(Dialog.BUTTON_POSITIVE);
	button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
	button.setOnClickListener(new View.OnClickListener() {
	    @Override
	    public void onClick(View view) {
	        EditText username = (EditText) getDialog().findViewById(ID_USERID_EDITTEXT);
		EditText password = (EditText) getDialog().findViewById(ID_PASSWORD_EDITTEXT);
		if (username.length() == 0) {
		    username.setHint("证件号码不能为空");
		    username.requestFocus();
		    return;
		}
		if (password.length() == 0) {
		    password.setHint("登陆密码不能为空");
		    password.requestFocus();
		    return;
		}
		hideIme(view);

		Member member = Member.getMember();
		if (member.login(username.getText().toString(),
			password.getText().toString())) {
		    dismiss();
		} else {
		    CommonDialog.showBox(getFragmentManager(),
			    "登陆失败", "用户名或密码错误，请重新输入!\n");
		}
	    }
	});

	getDialog().getWindow().setSoftInputMode(
		WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }


    @Override
    public void onDismiss(DialogInterface dialog) {
	super.onDismiss(dialog);
    }


    @Override
    public void onClick(View view) {
	switch(((Integer) view.getTag()).intValue()) {
	case TAG_USERID_CLEAR_BUTTON:
	    ((TextView) getDialog().findViewById(ID_USERID_EDITTEXT)).setText("");
	    getDialog().findViewById(ID_USERID_EDITTEXT).setEnabled(true);
	    break;
	case TAG_PASSWORD_CLEAR_BUTTON:
	    ((TextView) getDialog().findViewById(ID_PASSWORD_EDITTEXT)).setText("");
	    break;
	}
    }
}


class MemberProfileDialog extends DialogFragment implements OnClickListener
{
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
	    builder.setTitle("修改会员信息");

	LayoutInflater inflater = getActivity().getLayoutInflater();
	builder.setView(inflater.inflate(R.layout.dialog_member, null));

	builder.setNegativeButton("取 消", null);

	if (profile == null)
	    builder.setPositiveButton("注 册", null);
	else
	    builder.setPositiveButton("修 改", null);

	return builder.create();
    }

    private void hideIme(View view) {
	InputMethodManager imm = 
		(InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
	imm.hideSoftInputFromWindow(view.getApplicationWindowToken(), 0);
    }

    @Override
    public void onStart() {
	super.onStart();

	setCancelable(false);

	final AlertDialog dialog = (AlertDialog) getDialog();

	if (profile != null) {
	    TextView textview = (TextView) dialog.findViewById(R.id.userid_edittext);
	    textview.setText(profile.userid);
	    textview = (TextView) dialog.findViewById(R.id.username_edittext);
	    textview.setText(profile.username);
	    textview = (TextView) dialog.findViewById(R.id.company_edittext);
	    textview.setText(profile.company);
	    textview = (TextView) dialog.findViewById(R.id.phone_edittext);
	    textview.setText(profile.phone);

	    dialog.findViewById(R.id.idcard_read_button).setVisibility(View.GONE);
	}

	View view = dialog.findViewById(R.id.idcard_read_button);
	view.setOnClickListener(this);
	view = dialog.findViewById(R.id.password_clear);
	view.setOnClickListener(this);
	view = dialog.findViewById(R.id.password2_clear);
	view.setOnClickListener(this);
	view = dialog.findViewById(R.id.company_clear);
	view.setOnClickListener(this);
	view = dialog.findViewById(R.id.phone_clear);
	view.setOnClickListener(this);

	dialog.findViewById(R.id.password_edittext).requestFocus();

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
		EditText userid = (EditText) dialog.findViewById(R.id.userid_edittext);
		EditText username = (EditText) dialog.findViewById(R.id.username_edittext);
		EditText password = (EditText) dialog.findViewById(R.id.password_edittext);
		EditText password2 = (EditText) dialog.findViewById(R.id.password2_edittext);
		EditText company = (EditText) dialog.findViewById(R.id.company_edittext);
		EditText phone = (EditText) dialog.findViewById(R.id.phone_edittext);

		if (userid.length() == 0) {
		    userid.setHint("证件号码不能为空");
		    return;
		}
		if (username.length() == 0) {
		    username.setHint("会员姓名不能为空");
		    return;
		}

		if (profile == null) {
		    if (password.length() == 0) {
			password.setText("密码不能为空");
			password.requestFocus();
			return;
		    }
		}
		if (password.length() > 0) {
		    if (password.length() != 6) {
			password.setText("");
			password.setHint("密码长度必须是6位");
			password.requestFocus();
			return;
		    }
		    if (!password.getText().toString().equals(password2.getText().toString())) {
			password2.setText("");
			password2.setHint("确认密码不一致");
			password2.requestFocus();
			return;
		    }
		}

		hideIme(view);

		String userid_str = userid.getText().toString();
		String username_str = username.getText().toString();
		String password_str = password.getText().toString();
		String company_str = company.getText().toString();
		String phone_str = phone.getText().toString();

		Member member = Member.getMember();
		if (profile == null) {
		    if (member.register(userid_str, username_str,
			    password_str, company_str, phone_str)) {
			CommonDialog.showBox(getFragmentManager(), "注册成功",
				"注册成功！您现在可以通过证件号码和密码来登陆系统使用会员功能。");
		    } else {
			CommonDialog.showBox(getFragmentManager(), "注册失败",
				"注册失败！");
		    }
		} else {
		    if (member.update(userid_str, username_str,
			    password_str, company_str, phone_str)) {
			CommonDialog.showBox(getFragmentManager(), "修改成功",
				"您输入的信息已成功保存到系统中！");
			dismiss();
		    }
		}
	    }
	});

	getDialog().getWindow().setSoftInputMode(
		WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
	super.onDismiss(dialog);
    }


    @Override
    public void onClick(View view) {
	switch (view.getId()) {
	case R.id.password_clear:
	    ((TextView) getDialog().findViewById(R.id.password_edittext)).setText("");
	    break;
	case R.id.password2_clear:
	    ((TextView) getDialog().findViewById(R.id.password2_edittext)).setText("");
	    break;
	case R.id.company_clear:
	    ((TextView) getDialog().findViewById(R.id.company_edittext)).setText("");
	    break;
	case R.id.phone_clear:
	    ((TextView) getDialog().findViewById(R.id.phone_edittext)).setText("");
	    break;
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
    }


    public boolean isLogin() {
	return is_login;
    }

    public boolean login(String userid, String password) {
	profile = new MemberProfile();
	profile.userid = userid;
	profile.username = "吴小虎";
	profile.company = "Cansiny";
	profile.phone = "18655953721";

	is_login = true;

	if (listener != null) {
	    listener.onMemberLogin();
	}
	return is_login;
    }

    public void login(FragmentManager manager) {
	MemberLoginDialog dialog = new MemberLoginDialog();
	dialog.show(manager, "MemberLoginDialog");
    }

    public void logout() {
	profile = null;
	is_login = false;

	if (listener != null) {
	    listener.onMemberLogout();
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
	    dialog.show(manager, "MemberInfoDialog");
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


    public class MemberProfile
    {
	public String userid;
	public String username;
	public String company;
	public String phone;
    }
}
