/* EForm - Electronic Form System
 *
 * Copyright (C) 2013 Wu Xiaohu. All rights reserved.
 * Copyright (C) 2013 Cansiny Trade Co.,Ltd. All rights reserved.
 */
package com.cansiny.eform;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;


class AdministratorLoginDialog extends Utils.DialogFragment
{
    private EditText password_edittext;
    private boolean set_password = false;

    private View buildLayout() {
	LinearLayout linear = new LinearLayout(getActivity());
	linear.setOrientation(LinearLayout.VERTICAL);
	linear.setPadding(20, 10, 20, 20);

	TextView textview = new TextView(getActivity());
	textview.setText("管理员密码非常重要，请务必牢记，一旦忘记将无法读取某些加密数据；\n" +
			"请不要将管理员密码告诉他人。");
	textview.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
	textview.setTextColor(getResources().getColor(R.color.red));
	LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
		ViewGroup.LayoutParams.MATCH_PARENT,
		ViewGroup.LayoutParams.WRAP_CONTENT);
	params.bottomMargin = (int) Utils.convertDpToPixel(40);
	linear.addView(textview, params);

	LinearLayout linear2 = new LinearLayout(getActivity());
	linear2.setOrientation(LinearLayout.HORIZONTAL);
	linear2.setGravity(Gravity.CENTER_VERTICAL);
	linear.addView(linear2);

	textview = new TextView(getActivity());
	textview.setText("管理员密码：");
	textview.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
	linear2.addView(textview);

	password_edittext = new EditText(getActivity());
	password_edittext.setInputType(InputType.TYPE_CLASS_NUMBER |
		InputType.TYPE_NUMBER_VARIATION_PASSWORD);
	password_edittext.setFilters(new InputFilter[] {new InputFilter.LengthFilter(6)});
	password_edittext.setMinEms(6);
	password_edittext.setHintTextColor(getResources().getColor(R.color.silver));
	params = new LinearLayout.LayoutParams(
		0, ViewGroup.LayoutParams.WRAP_CONTENT);
	params.weight = 1;
	linear2.addView(password_edittext, params);

	addClearButton(linear2);

	Administrator admin = Administrator.getAdministrator();
	if (!admin.hasPassword(getActivity())) {
	    textview = new TextView(getActivity());
	    textview.setText("首次登录密码为 123456，登录后请立即设置管理员密码");
	    textview.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
	    textview.setTextColor(getResources().getColor(R.color.purple));
	    params = new LinearLayout.LayoutParams(
		    ViewGroup.LayoutParams.MATCH_PARENT,
		    ViewGroup.LayoutParams.WRAP_CONTENT);
	    params.topMargin = (int) Utils.convertDpToPixel(30);
	    linear.addView(textview, params);
	}
	return linear;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
	super.onCreateDialog(savedInstanceState);

	AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

	builder.setTitle("管理员登录");
	builder.setView(buildLayout());
	builder.setNegativeButton("忘记密码", null);
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
		showToastLong("管理员密码是经过加密存储的，建议你多尝试各种可能的密码。" +
			"如确实忘记请联系售后服务人员协助！", R.drawable.cry);
	    }
	});

	button = dialog.getButton(Dialog.BUTTON_POSITIVE);
	button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
	button.setOnClickListener(new View.OnClickListener() {
	    @Override
	    public void onClick(View view) {
		if (password_edittext.length() != 6) {
		    password_edittext.setText("");
		    password_edittext.setHint("密码必须是6位");
		    password_edittext.requestFocus();
		    return;
		}
		hideIme(view);

		Administrator admin = Administrator.getAdministrator();
		int retval = admin.login(getActivity(), password_edittext.getText().toString());
		switch(retval) {
		case 0:
		    dismiss();
		    Utils.showToast("登录成功!", R.drawable.smile);
		    break;
		case 1:
		    dismiss();
		    Utils.showToast("使用初始密码登录成功，请立即设置密码!", R.drawable.tips);
		    set_password = true;
		    break;
		case -1:
		    Utils.showToast("密码错误，请重试!", R.drawable.cry);
		    break;
		}
	    }
	});
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
	super.onDismiss(dialog);

	Administrator admin = Administrator.getAdministrator();
	admin.fireLoginDialogDisapper();

	if (set_password)
	    admin.setPassword(getFragmentManager());
    }

}


class AdministratorPasswordDialog extends Utils.DialogFragment
{
    private EditText password_edittext;
    private EditText password2_edittext;

    private View buildLayout() {
	LinearLayout linear = new LinearLayout(getActivity());
	linear.setOrientation(LinearLayout.VERTICAL);
	linear.setPadding(20, 10, 20, 20);

	TextView textview = new TextView(getActivity());
	textview.setText("管理员密码非常重要，请务必牢记，一旦忘记将无法读取某些加密数据；\n" +
			"请不要将管理员密码告诉他人。");
	textview.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
	textview.setTextColor(getResources().getColor(R.color.red));
	LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
		ViewGroup.LayoutParams.MATCH_PARENT,
		ViewGroup.LayoutParams.WRAP_CONTENT);
	params.bottomMargin = (int) Utils.convertDpToPixel(40);
	linear.addView(textview, params);

	TableLayout table = new TableLayout(getActivity());
	table.setColumnStretchable(1, true);
	linear.addView(table);

	TableRow row = new TableRow(getActivity());
	row.setGravity(Gravity.CENTER_VERTICAL);
	table.addView(row);

	textview = new TextView(getActivity());
	textview.setText("设置管理员密码：");
	textview.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
	row.addView(textview);

	password_edittext = new EditText(getActivity());
	password_edittext.setInputType(InputType.TYPE_CLASS_NUMBER |
		InputType.TYPE_NUMBER_VARIATION_PASSWORD);
	password_edittext.setFilters(new InputFilter[] {new InputFilter.LengthFilter(6)});
	password_edittext.setMinEms(6);
	password_edittext.setHintTextColor(getResources().getColor(R.color.silver));
	row.addView(password_edittext);

	addClearButton(row);

	row = new TableRow(getActivity());
	row.setGravity(Gravity.CENTER_VERTICAL);
	TableLayout.LayoutParams table_params = new TableLayout.LayoutParams();
	table_params.topMargin = (int) Utils.convertDpToPixel(20);
	table.addView(row, table_params);

	textview = new TextView(getActivity());
	textview.setText("确认管理员密码：");
	textview.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
	row.addView(textview);

	password2_edittext = new EditText(getActivity());
	password2_edittext.setInputType(InputType.TYPE_CLASS_NUMBER |
		InputType.TYPE_NUMBER_VARIATION_PASSWORD);
	password2_edittext.setFilters(new InputFilter[] {new InputFilter.LengthFilter(6)});
	password2_edittext.setMinEms(6);
	password2_edittext.setHintTextColor(getResources().getColor(R.color.silver));
	row.addView(password2_edittext);

	addClearButton(row);

	Administrator admin = Administrator.getAdministrator();
	if (!admin.hasPassword(getActivity())) {
	    textview = new TextView(getActivity());
	    textview.setText("请立即设置管理员密码，以免被不相关人员抢先设置而造成不必要的麻烦。");
	    textview.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
	    textview.setTextColor(getResources().getColor(R.color.purple));
	    params = new LinearLayout.LayoutParams(
		    ViewGroup.LayoutParams.MATCH_PARENT,
		    ViewGroup.LayoutParams.WRAP_CONTENT);
	    params.topMargin = (int) Utils.convertDpToPixel(50);
	    linear.addView(textview, params);
	}
	return linear;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
	super.onCreateDialog(savedInstanceState);

	AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

	builder.setTitle("设置管理员密码");
	builder.setView(buildLayout());
	builder.setNegativeButton("放 弃", new DialogInterface.OnClickListener() {
	    @Override
	    public void onClick(DialogInterface dialog, int which) {
//		Utils.showToastLong("您可以在“设置”对话框的“安全“选项卡中设置管理员密码。",
//			R.drawable.tips);
	    }
	});
	builder.setPositiveButton("设 置", null);

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
	button.setOnClickListener(new View.OnClickListener() {
	    @Override
	    public void onClick(View view) {
		if (password_edittext.length() != 6) {
		    password_edittext.setText("");
		    password_edittext.setHint("密码必须是6位");
		    password_edittext.requestFocus();
		    return;
		}
		if (password2_edittext.length() != 6) {
		    password2_edittext.setText("");
		    password2_edittext.setHint("确认密码必须是6位");
		    password2_edittext.requestFocus();
		    return;
		}
		String password = password_edittext.getText().toString();
		String password2 = password2_edittext.getText().toString();

		if (password.equals("123456")) {
		    showToast("密码不能设置为 123456，请选择其他的密码。");
		    return;
		}
		if (!password.equals(password2)) {
		    password2_edittext.setText("");
		    password2_edittext.setHint("确认密码不一致");
		    password2_edittext.requestFocus();
		    return;
		}
		hideIme(view);

		Administrator admin = Administrator.getAdministrator();
		if (admin.setPassword(getActivity(), password)) {
		    dismiss();
		    Utils.showToast("密码设置成功!", R.drawable.smile);
		} else {
		    showToast("密码设置错误，请重试!", R.drawable.cry);
		}
	    }
	});
    }

}


public class Administrator
{
    static private Administrator _singleInstance;
    static Administrator getAdministrator() {
	if (_singleInstance == null)
	    _singleInstance = new Administrator();
	return _singleInstance;
    }


    private boolean is_login;
    private String password;
    private ArrayList<AdministratorListener> listeners;
    private AdministratorLoginDialogListener login_dialog_listener;

    private Administrator() {
	password = null;
	is_login = false;
	listeners = new ArrayList<AdministratorListener>();
	login_dialog_listener = null;
    }

    public boolean isLogin() {
	return is_login;
    }

    public String getPassword() {
	return password;
    }

    public boolean hasPassword(Context context) {
	return EFormSQLite.Account.hasPassword(context);
    }

    public int login(Context context, String password) {
	if (!is_login) {
	    int retval = EFormSQLite.Account.login(context, password);
	    if (retval >= 0) {
		this.password = password;
		is_login = true;
		for (AdministratorListener listener : listeners) {
		    listener.onAdministratorLogin(this);
		}
	    }
	    return retval;
	}
	return 0;
    }

    public void login(FragmentManager manager) {
	if (!is_login) {
	    AdministratorLoginDialog dialog = new AdministratorLoginDialog();
	    dialog.show(manager, "AdministratorLoginDialog");
	}
    }

    public boolean setPassword(Context context, String password) {
	if (password == null)
	    throw new IllegalArgumentException("管理员密码不能为NULL");

	if (is_login) {
	    if (EFormSQLite.Account.setPassword(context, password)) {
		this.password = password;
		return true;
	    }
	}
	return false;
    }

    public void setPassword(FragmentManager manager) {
	if (is_login) {
	    AdministratorPasswordDialog dialog = new AdministratorPasswordDialog();
	    dialog.show(manager, "AdministratorPasswordDialog");
	}
    }

    public void logout() {
	if (is_login) {
	    for (AdministratorListener listener : listeners) {
		listener.onAdministratorLogout(this);
	    }
	    password = null;
	    is_login = false;
	}
    }

    public void addListener(AdministratorListener listener) {
	listeners.add(listener);
    }

    public void removeListener(AdministratorListener listener) {
	listeners.remove(listener);
    }

    public void setLoginDialogListener(AdministratorLoginDialogListener listener) {
	login_dialog_listener = listener;
    }

    public void fireLoginDialogDisapper() {
	if (login_dialog_listener != null)
	    login_dialog_listener.onAdministratorLoginDialogDisapper(this);
    }

    public interface AdministratorListener
    {
	public void onAdministratorLogin(Administrator admin);
	public void onAdministratorLogout(Administrator admin);
    }

    public interface AdministratorLoginDialogListener
    {
	public void onAdministratorLoginDialogDisapper(Administrator admin);
    }

}
