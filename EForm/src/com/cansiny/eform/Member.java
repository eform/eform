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
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
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


class MemberDatabaseHelper extends SQLiteOpenHelper
{

    public MemberDatabaseHelper(Context context, String name,
	    CursorFactory factory, int version) {
	super(context, name, factory, version);
	// TODO Auto-generated constructor stub
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
	// TODO Auto-generated method stub
	
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	// TODO Auto-generated method stub
	
    }

}


class MemberLoginDialog extends DialogFragment implements OnClickListener
{
    private static final int EDITTEXT_ID_USERNAME = 0x00010001;
    private static final int EDITTEXT_ID_PASSWORD = 0x00010002;

    private static final int BUTTON_TAG_USERNAME_CLEAR = 1;
    private static final int BUTTON_TAG_PASSWORD_CLEAR = 2;

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
	edittext.setId(EDITTEXT_ID_USERNAME);
	edittext.setInputType(InputType.TYPE_CLASS_PHONE);
	edittext.setFilters(new InputFilter[] {new InputFilter.LengthFilter(18)});
	edittext.setMinEms(10);
	params = new TableRow.LayoutParams();
	row.addView(edittext, params);

	Button button = new Button(getActivity());
	button.setTag(BUTTON_TAG_USERNAME_CLEAR);
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
	edittext.setId(EDITTEXT_ID_PASSWORD);
	edittext.setInputType(InputType.TYPE_CLASS_NUMBER |
		InputType.TYPE_NUMBER_VARIATION_PASSWORD);
	edittext.setFilters(new InputFilter[] {new InputFilter.LengthFilter(6)});
	params = new TableRow.LayoutParams();
	row.addView(edittext, params);

	button = new Button(getActivity());
	button.setTag(BUTTON_TAG_PASSWORD_CLEAR);
	button.setOnClickListener(this);
	button.setBackgroundResource(R.drawable.clear);
	params = new TableRow.LayoutParams(
		(int) HomeActivity.convertDpToPixel(26),
		(int) HomeActivity.convertDpToPixel(26));
	row.addView(button, params);

	return table;
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


    private void hideIme(View view) {
	InputMethodManager imm = 
		(InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
	imm.hideSoftInputFromWindow(view.getApplicationWindowToken(), 0);
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
		dismiss();

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

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle("找回密码");
		builder.setMessage("您可以通过下面步骤找回密码：\n\n" +
				"    1、带上注册会员时使用的身份证件；\n" +
				"    2、联系大堂经理设置新密码；\n");
		builder.create().show();
	    }
	});

	button = dialog.getButton(Dialog.BUTTON_POSITIVE);
	button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
	button.setOnClickListener(new View.OnClickListener() {
	    @Override
	    public void onClick(View view) {
	        EditText username = (EditText) getDialog().findViewById(EDITTEXT_ID_USERNAME);
		EditText password = (EditText) getDialog().findViewById(EDITTEXT_ID_PASSWORD);
		if (username.length() == 0) {
		    username.requestFocus();
		    return;
		}
		if (password.length() == 0) {
		    password.requestFocus();
		    return;
		}

		hideIme(view);

		Member member = Member.getMember();
		if (member.login(username.getText().toString(), password.getText().toString())) {
		    dismiss();
		} else {
		    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		    builder.setTitle("登陆失败");
		    builder.setMessage("用户名或密码错误，请重新输入!\n");
		    builder.create().show();
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
	case BUTTON_TAG_USERNAME_CLEAR:
	    ((TextView) getDialog().findViewById(EDITTEXT_ID_USERNAME)).setText("");
	    break;
	case BUTTON_TAG_PASSWORD_CLEAR:
	    ((TextView) getDialog().findViewById(EDITTEXT_ID_PASSWORD)).setText("");
	    break;
	}
    }
}


class MemberProfileDialog extends DialogFragment implements OnClickListener
{
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
	super.onCreateDialog(savedInstanceState);

	AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
	builder.setTitle("会员注册");

	LayoutInflater inflater = getActivity().getLayoutInflater();
	builder.setView(inflater.inflate(R.layout.dialog_member, null));

	builder.setNegativeButton("取 消", null);
	builder.setPositiveButton("注 册", null);

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

	AlertDialog dialog = (AlertDialog) getDialog();

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
		hideIme(view);
	    }
	});

	getDialog().getWindow().setSoftInputMode(
		WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    @Override
    public void onDismiss (DialogInterface dialog) {
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
    private MemberDatabaseHelper db_helper;
    private MemberListener listener;

    private Member() {
	is_login = false;
//	db_helper = new MemberDatabaseHelper();
    }


    public boolean is_login() {
	return is_login;
    }

    public boolean login(String uname, String passwd) {
	is_login = true;
	if (listener != null) {
	    listener.on_member_login();
	}
	return is_login;
    }

    public void login(FragmentManager manager) {
	MemberLoginDialog dialog = new MemberLoginDialog();
	dialog.show(manager, "MemberLoginDialog");
    }

    public void logout() {
	is_login = false;
	if (listener != null) {
	    listener.on_member_logout();
	}
    }
    
    public boolean register(String uname, String passwd) {
	return true;
    }

    public void modify_profile(FragmentManager manager) {
	MemberProfileDialog dialog = new MemberProfileDialog();
	dialog.show(manager, "MemberInfoDialog");
    }


    public void setListener(MemberListener listener) {
	this.listener = listener;
    }


    public interface MemberListener
    {
	public void on_member_login();
	public void on_member_logout();
    }
}
