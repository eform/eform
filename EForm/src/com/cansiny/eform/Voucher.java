/* EForm - Electronic Form System
 *
 * Copyright (C) 2013 Wu Xiaohu. All rights reserved.
 * Copyright (C) 2013 Cansiny Trade Co.,Ltd. All rights reserved.
 */
package com.cansiny.eform;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.FragmentManager;
import android.content.Context;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;


class VoucherDialog extends Utils.DialogFragment
{
    protected Voucher voucher;
    protected EditText comment_edittext;

    public void setVoucher(Voucher voucher) {
	this.voucher = voucher;
    }

    protected View buildCommentLayout() {
	LinearLayout layout = new LinearLayout(getActivity());
	layout.setOrientation(LinearLayout.VERTICAL);
	layout.setPadding(10, 10, 10, 10);

	TextView textview = new TextView(getActivity());
	textview.setText("请简单描述一下要保存的凭证信息");
	textview.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17);
	layout.addView(textview);

	LinearLayout linear = new LinearLayout(getActivity());
	linear.setOrientation(LinearLayout.HORIZONTAL);
	linear.setGravity(Gravity.CENTER_VERTICAL);
	LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
		ViewGroup.LayoutParams.MATCH_PARENT,
		ViewGroup.LayoutParams.WRAP_CONTENT);
	params.topMargin = 20;
	layout.addView(linear, params);

	comment_edittext = new EditText(getActivity());
	comment_edittext.setSingleLine();
	comment_edittext.addTextChangedListener(new TextWatcher() {
	    @Override
	    public void afterTextChanged(Editable editable) {
	    }
	    @Override
	    public void beforeTextChanged(CharSequence s, int start,
		    int before, int count) {
		if (voucher != null)
		    voucher.fireOnCommentTextChanged();
	    }
	    @Override
	    public void onTextChanged(CharSequence s, int start,
		    int before, int count) {
	    }
	});
	if (voucher != null && voucher.getComment() != null) {
	    comment_edittext.setText(voucher.getComment());
	}
	params = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT);
	params.weight = 1;
	linear.addView(comment_edittext, params);

	addClearButton(linear);

	return layout;
    }

    @Override
    public void onStart() {
	super.onStart();
    }
}


class VoucherInsertDialog extends VoucherDialog
{
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
	super.onCreateDialog(savedInstanceState);

	AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

	builder.setTitle("保存凭条");
	builder.setView(buildCommentLayout());
	builder.setNegativeButton("取 消", null);
	builder.setPositiveButton("保 存", null);

	return builder.create();
    }

    @Override
    public void onStart() {
	super.onStart();

	AlertDialog dialog = (AlertDialog) getDialog();

	Button button = dialog.getButton(Dialog.BUTTON_POSITIVE);
	button.setOnClickListener(new View.OnClickListener() {
	    @Override
	    public void onClick(View view) {
		String comment = comment_edittext.getText().toString();
		if (comment.length() == 0) {
		    comment_edittext.setHint("描述信息不能为空");
		    comment_edittext.setHintTextColor(getResources().getColor(R.color.red));
		    return;
		}
		if (!comment.equals(voucher.getComment())) {
		    voucher.setComment(comment);
		}
		hideIme(view);

		if (voucher.insert(getActivity())) {
		    Utils.showToast("凭条信息保存成功！", R.drawable.smile);
		} else {
		    Utils.showToast("凭条信息保存失败！", R.drawable.cry);
		}
		dismiss();
	    }
	});
    }

}

class VoucherUpdateDialog extends VoucherDialog
{
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
	super.onCreateDialog(savedInstanceState);

	AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

	builder.setTitle("更新凭条");
	builder.setView(buildCommentLayout());
	builder.setNegativeButton("放 弃", null);
	builder.setPositiveButton("更 新", null);

	return builder.create();
    }

    @Override
    public void onStart() {
	super.onStart();

	AlertDialog dialog = (AlertDialog) getDialog();

	Button button = dialog.getButton(Dialog.BUTTON_POSITIVE);
	button.setOnClickListener(new View.OnClickListener() {
	    @Override
	    public void onClick(View view) {
		if (voucher == null || voucher.getRowid() < 0) {
		    LogActivity.writeLog("不能更新凭条，voucher成员没有正确设置(程序错误)");
		    return;
		}

		String comment = comment_edittext.getText().toString();
		if (comment.length() == 0) {
		    comment_edittext.setHint("描述信息不能为空");
		    comment_edittext.setHintTextColor(getResources().getColor(R.color.red));
		    return;
		}
		voucher.setComment(comment);

		hideIme(view);

		if (voucher.update(getActivity())) {
		    Utils.showToast("凭条信息更新成功！", R.drawable.smile);
		} else {
		    Utils.showToast("凭条信息更新失败！", R.drawable.cry);
		}
		dismiss();
	    }
	});
    }

}


class VoucherReplaceDialog extends VoucherDialog
{
    private RadioButton update_radio;
    private RadioButton new_radio;

    private View buildLayout() {
	RadioGroup group = new RadioGroup(getActivity());
	group.setPadding(10, 10, 10, 10);

	update_radio = new RadioButton(getActivity());
	update_radio.setText("更新已保存凭条");
	update_radio.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
	group.addView(update_radio);

	TextView textview = new TextView(getActivity());
	textview.setText("您之前已经保存过此凭条，选择更新将用当前信息覆盖之前保存的信息");
	textview.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
	RadioGroup.LayoutParams params = new RadioGroup.LayoutParams(
		ViewGroup.LayoutParams.WRAP_CONTENT,
		ViewGroup.LayoutParams.WRAP_CONTENT);
	params.leftMargin = 20;
	params.bottomMargin = 30;
	group.addView(textview, params);

	new_radio = new RadioButton(getActivity());
	new_radio.setText("保存为新凭条");
	new_radio.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
	group.addView(new_radio);

	textview = new TextView(getActivity());
	textview.setText("保存为新的凭条将在您的凭条本中新建一项，您之前保存的信息不会改变");
	textview.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
	params = new RadioGroup.LayoutParams(
		ViewGroup.LayoutParams.WRAP_CONTENT,
		ViewGroup.LayoutParams.WRAP_CONTENT);
	params.leftMargin = 20;
	group.addView(textview, params);

	return group;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
	super.onCreateDialog(savedInstanceState);

	AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

	builder.setTitle("请选择");
	builder.setView(buildLayout());
	builder.setNegativeButton("取 消", null);
	builder.setPositiveButton("确 定", null);

	return builder.create();
    }

    @Override
    public void onStart() {
	super.onStart();

	update_radio.toggle();

	AlertDialog dialog = (AlertDialog) getDialog();

	Button button = dialog.getButton(Dialog.BUTTON_POSITIVE);
	button.setOnClickListener(new View.OnClickListener() {
	    @Override
	    public void onClick(View view) {
		if (voucher == null || voucher.getRowid() < 0) {
		    LogActivity.writeLog("不能更新凭条，voucher成员没有正确设置(程序错误)");
		    return;
		}

		dismiss();

		if (update_radio.isChecked()) {
		    voucher.update(getFragmentManager());
		} else {
		    voucher.insert(getFragmentManager());
		}
	    }
	});
    }
}


public class Voucher implements Parcelable
{
    private long   rowid;
    private long   userid;
    private String formclass;
    private String formlabel;
    private int    formimage;
    private String contents;
    private String comment;
    private long   ctime;
    private long   mtime;
    private long   atime;

    private boolean contents_changed;
    private boolean comment_changed;

    private VoucherListener listener;
    private VoucherDialogListener dialog_listener;

    public Voucher() {
	rowid = -1;
	userid = -1;
	formclass = null;
	formlabel = null;
	formimage = 0;
	contents = null;
	comment = null;
	ctime = 0;
	mtime = 0;
	atime = 0;

	contents_changed = false;
	comment_changed = false;
    }

    public Voucher(long rowid, long userid, String formclass,
	    String formlabel, int formimage, String contents,
	    String comment, long ctime, long mtime, long atime) {
	this.rowid = rowid;
	this.userid = userid;
	this.formclass = formclass;
	this.formlabel = formlabel;
	this.formimage = formimage;
	this.contents = contents;
	this.comment = comment;
	this.ctime = ctime;
	this.mtime = mtime;
	this.atime = atime;

	contents_changed = false;
	comment_changed = false;
    }

    private Voucher(Parcel parcel) {
	rowid = parcel.readLong();
	userid = parcel.readLong();
	formclass = parcel.readString();
	formlabel = parcel.readString();
	formimage = parcel.readInt();
	contents = parcel.readString();
	comment = parcel.readString();
	ctime = parcel.readLong();
	mtime = parcel.readLong();
	atime = parcel.readLong();
    }

    @Override
    public int describeContents() {
	return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
	dest.writeLong(rowid);
	dest.writeLong(userid);
	dest.writeString(formclass);
	dest.writeString(formlabel);
	dest.writeInt(formimage);
	dest.writeString(contents);
	dest.writeString(comment);
	dest.writeLong(ctime);
	dest.writeLong(mtime);
	dest.writeLong(atime);
    }

    public static final Parcelable.Creator<Voucher> CREATOR =
	    new Parcelable.Creator<Voucher>() {
	public Voucher createFromParcel(Parcel parcel) {
	    return new Voucher(parcel);
	}
	public Voucher[] newArray(int size) {
	    return new Voucher[size];
	}
    };

    public long getRowid() {
	return rowid;
    }

    public long getUserid() {
	return userid;
    }

    public String getFormClass() {
	return formclass;
    }

    public void setFormClass(String klass) {
	formclass = klass;
    }

    public String getFormLabel() {
	return formlabel;
    }

    public void setFormLabel(String label) {
	formlabel = label;
    }

    public int getFormImage() {
	return formimage;
    }

    public void setFormImage(int image) {
	formimage = image;
    }

    public String getContents() {
	return contents;
    }

    public void setContents(String contents) {
	if (this.contents == null || !this.contents.equals(contents)) {
	    this.contents = contents;
	    contents_changed = true;
	}
    }

    public String getComment() {
	return comment;
    }

    public void setComment(String comment) {
	if (this.comment == null || !this.comment.equals(comment)) {
	    this.comment = comment;
	    comment_changed = true;
	}
    }

    public long getAtime() {
	return atime;
    }

    public boolean insert(Context context) {
	Member member = Member.getMember();
	if (!member.isLogin() || member.getProfile() == null) {
	    LogActivity.writeLog("插入凭条失败，会员未登录");
	    return false;
	}
	userid = member.getProfile().rowid;

	long id = EFormSQLite.Voucher.insert(context,
		member.getProfile().password, userid,
		formclass, formlabel, formimage, contents, comment);
	if (id >= 0) {
	    rowid = id;
	    return true;
	}
	return false;
    }

    public void insert(FragmentManager manager) {
	VoucherInsertDialog dialog = new VoucherInsertDialog();
	dialog.setVoucher(this);
	dialog.show(manager, "VoucherInsertDialog");
    }

    public boolean update(Context context) {
	Member member = Member.getMember();
	if (!member.isLogin() || member.getProfile() == null) {
	    LogActivity.writeLog("更新凭条失败，会员未登录");
	    return false;
	}
	Member.MemberProfile profile = member.getProfile();
	if (profile.rowid != userid) {
	    LogActivity.writeLog("尝试更新不属于当前会员的凭条，不允许");
	    return false;
	}

	String new_contents = null;
	String new_comment = null;

	if (contents_changed) {
	    new_contents = contents;
	}
	if (comment_changed) {
	    new_comment = comment;
	}
	if (EFormSQLite.Voucher.update(context, profile.password,
		rowid, userid, new_contents, new_comment)) {
	    contents_changed = false;
	    comment_changed = false;

	    if (listener != null)
		listener.onVoucherChanged(this);

	    return true;
	}
	return false;
    }

    public void update(FragmentManager manager) {
	VoucherUpdateDialog dialog = new VoucherUpdateDialog();
	dialog.setVoucher(this);
	dialog.show(manager, "VoucherUpdateDialog");
    }

    public void replace(FragmentManager manager) {
	VoucherReplaceDialog dialog = new VoucherReplaceDialog();
	dialog.setVoucher(this);
	dialog.show(manager, "VoucherReplaceDialog");
    }

    public boolean delete(Context context) {
	Member member = Member.getMember();
	if (!member.isLogin() || member.getProfile() == null) {
	    LogActivity.writeLog("删除凭条失败，会员未登录");
	    return false;
	}
	Member.MemberProfile profile = member.getProfile();
	if (profile.rowid != userid) {
	    LogActivity.writeLog("尝试删除不属于当前会员的凭条，不允许");
	    return false;
	}
	return EFormSQLite.Voucher.delete(context, rowid);
    }

    public void updateAccessTime(Context context) {
	Member member = Member.getMember();
	if (!member.isLogin() || member.getProfile() == null) {
	    LogActivity.writeLog("更新凭条失败，会员未登录");
	    return;
	}
	Member.MemberProfile profile = member.getProfile();
	if (profile.rowid != userid) {
	    LogActivity.writeLog("尝试更新不属于当前会员的凭条，不允许");
	    return;
	}
	EFormSQLite.Voucher.updateAccesstime(context, rowid);
    }

    public void setListener(VoucherListener listener) {
	this.listener = listener;
    }

    public void setDialogListener(VoucherDialogListener listener) {
	dialog_listener = listener;
    }

    public void fireOnCommentTextChanged() {
	if (dialog_listener != null)
	    dialog_listener.onCommentTextChanged();
    }

    public interface VoucherListener
    {
	public void onVoucherChanged(Voucher voucher);
    }

    public interface VoucherDialogListener
    {
	public void onCommentTextChanged();
    }

}
