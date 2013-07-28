/* EForm - Electronic Form System
 *
 * Copyright (C) 2013 Wu Xiaohu. All rights reserved.
 * Copyright (C) 2013 Cansiny Trade Co.,Ltd. All rights reserved.
 */
package com.cansiny.eform;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


public class ErrorFragment extends Fragment implements OnClickListener
{
    private final static int BUTTON_TAG_DETAIL = 1;
    private final static int BUTTON_TAG_HOME = 2;
    private final static int BUTTON_TAG_RECOVERY = 3;
    private final static int BUTTON_TAG_CONTACT = 4;

    private int error_reason_id;

    public ErrorFragment() {
	error_reason_id = 0;
    }

    public void setErrorReason(int reason_resid) {
	this.error_reason_id = reason_resid;
    }

    @Override
    public void onAttach (Activity activity) {
	super.onAttach(activity);
	Log.d("ErrorFragment", "Fragment attached.");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	Log.d("ErrorFragment", "Fragment created.");
    }

    private LinearLayout buildLayout() {
	LinearLayout linear = new LinearLayout(getActivity());
	linear.setOrientation(LinearLayout.HORIZONTAL);
	linear.setBackgroundResource(R.color.darkred);
	linear.setGravity(Gravity.CENTER);
	linear.setPadding(0, 20, 0, 20);

	ImageView image = new ImageView(getActivity());
	image.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
	image.setImageResource(R.drawable.cry);
	linear.addView(image, new LinearLayout.LayoutParams(
		(int) HomeActivity.convertDpToPixel(200),
		(int) HomeActivity.convertDpToPixel(200)));
	
	LinearLayout linear2 = new LinearLayout(getActivity());
	linear2.setOrientation(LinearLayout.VERTICAL);
	LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
		ViewGroup.LayoutParams.WRAP_CONTENT,
		ViewGroup.LayoutParams.WRAP_CONTENT);
	params.leftMargin = (int) HomeActivity.convertDpToPixel(60);
	linear.addView(linear2, params);

	TextView textview = new TextView(getActivity());
	textview.setText("系统出现错误");
	textview.setTextSize(TypedValue.COMPLEX_UNIT_SP, 32);
	textview.setTextColor(getResources().getColor(R.color.white));
	linear2.addView(textview);

	textview = new TextView(getActivity());
	textview.setText("之所以出现这个界面是因为系统在运行过程中碰到一些异常情况。\n" +
			"您不用太担心，因为这些异常通常都是容易修复的！");
	textview.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
	textview.setTextColor(getResources().getColor(R.color.silver));
	params = new LinearLayout.LayoutParams(
		ViewGroup.LayoutParams.WRAP_CONTENT,
		ViewGroup.LayoutParams.WRAP_CONTENT);
	params.topMargin = (int) HomeActivity.convertDpToPixel(10);
	params.leftMargin = (int) HomeActivity.convertDpToPixel(20);
	linear2.addView(textview, params);

	textview = new TextView(getActivity());
	textview.setText("错误原因：");
	textview.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
	textview.setTextColor(getResources().getColor(R.color.white));
	params = new LinearLayout.LayoutParams(
		ViewGroup.LayoutParams.WRAP_CONTENT,
		ViewGroup.LayoutParams.WRAP_CONTENT);
	params.topMargin = (int) HomeActivity.convertDpToPixel(30);
	linear2.addView(textview, params);

	textview = new TextView(getActivity());
	if (error_reason_id != 0) {
	    textview.setText(error_reason_id);
	} else {
	    textview.setText("未知错误!");
	}
	textview.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);
	textview.setTextColor(getResources().getColor(R.color.yellow));
	params = new LinearLayout.LayoutParams(
		ViewGroup.LayoutParams.WRAP_CONTENT,
		ViewGroup.LayoutParams.WRAP_CONTENT);
	params.topMargin = (int) HomeActivity.convertDpToPixel(10);
	params.leftMargin = (int) HomeActivity.convertDpToPixel(20);
	linear2.addView(textview, params);

	Button button = new Button(getActivity());
	button.setText("详细信息 ...");
	button.setTag(BUTTON_TAG_DETAIL);
	button.setTextColor(getResources().getColor(R.color.white));
	button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);
	button.setOnClickListener(this);
	params = new LinearLayout.LayoutParams(
		ViewGroup.LayoutParams.WRAP_CONTENT,
		ViewGroup.LayoutParams.WRAP_CONTENT);
	params.topMargin = (int) HomeActivity.convertDpToPixel(10);
	params.leftMargin = (int) HomeActivity.convertDpToPixel(20);
	linear2.addView(button, params);

	textview = new TextView(getActivity());
	textview.setText("解决方法：");
	textview.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
	textview.setTextColor(getResources().getColor(R.color.white));
	params = new LinearLayout.LayoutParams(
		ViewGroup.LayoutParams.WRAP_CONTENT,
		ViewGroup.LayoutParams.WRAP_CONTENT);
	params.topMargin = (int) HomeActivity.convertDpToPixel(50);
	params.bottomMargin = (int) HomeActivity.convertDpToPixel(10);
	linear2.addView(textview, params);

	button = new Button(getActivity());
	button.setText("1、点击此处返回到主页面继续其他操作");
	button.setTag(BUTTON_TAG_HOME);
	button.setTextColor(getResources().getColor(R.color.white));
	button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);
	button.setBackgroundResource(R.drawable.transparent_button);
	button.setOnClickListener(this);
	params = new LinearLayout.LayoutParams(
		ViewGroup.LayoutParams.WRAP_CONTENT,
		ViewGroup.LayoutParams.WRAP_CONTENT);
	params.leftMargin = (int) HomeActivity.convertDpToPixel(20);
	linear2.addView(button, params);

	button = new Button(getActivity());
	button.setText("2、点击此处恢复到系统最近的正确版本");
	button.setTag(BUTTON_TAG_RECOVERY);
	button.setTextColor(getResources().getColor(R.color.white));
	button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);
	button.setBackgroundResource(R.drawable.transparent_button);
	button.setOnClickListener(this);
	params = new LinearLayout.LayoutParams(
		ViewGroup.LayoutParams.WRAP_CONTENT,
		ViewGroup.LayoutParams.WRAP_CONTENT);
	params.leftMargin = (int) HomeActivity.convertDpToPixel(20);
	linear2.addView(button, params);

	button = new Button(getActivity());
	button.setText("3、联系技术支持人员协助解决此问题");
	button.setTag(BUTTON_TAG_CONTACT);
	button.setTextColor(getResources().getColor(R.color.white));
	button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);
	button.setBackgroundResource(R.drawable.transparent_button);
	button.setOnClickListener(this);
	params = new LinearLayout.LayoutParams(
		ViewGroup.LayoutParams.WRAP_CONTENT,
		ViewGroup.LayoutParams.WRAP_CONTENT);
	params.leftMargin = (int) HomeActivity.convertDpToPixel(20);
	linear2.addView(button, params);

	return linear;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
			     Bundle savedInstanceState)
    {
	Log.d("ErrorFragment", "Fragment view created.");
	return buildLayout();
    }

    @Override
    public void onPause() {
	super.onPause();
	Log.d("ErrorFragment", "Fragment pasued.");
    }

    @Override
    public void onClick(View view) {
	switch(((Integer) view.getTag()).intValue()) {
	case BUTTON_TAG_DETAIL:
	    Intent intent = new Intent(getActivity(), LogActivity.class);
	    startActivity(intent);
	    break;
	case BUTTON_TAG_HOME:
	    Activity activity = getActivity();
	    if (activity instanceof HomeActivity)
		((HomeActivity) activity).refreshLayout();
	    break;
	case BUTTON_TAG_RECOVERY:
	    Log.d("ErrorFragment", "Recovery button click...");
	    break;
	case BUTTON_TAG_CONTACT:
	    ContactDialog dialog = new ContactDialog();
	    dialog.show(getFragmentManager(), "ContactDialog");
	    break;
	}
    }

}
