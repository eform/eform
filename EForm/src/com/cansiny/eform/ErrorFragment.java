/* EForm - Electronic Form System
 *
 * Copyright (C) 2013 Wu Xiaohu. All rights reserved.
 * Copyright (C) 2013 Cansiny Trade Co.,Ltd. All rights reserved.
 * 
 * RecoveryFragment - Recoverable error fragment
 *
 * Authors:
 *   Xiaohu <xiaohu417@gmail.com>, 2013.6.14, hefei
 */
package com.cansiny.eform;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

/**
 * Recoverable error fragment.
 * When recoverable error occur, show this fragment give user tip,
 * and try to recovery from error.
 */
public class ErrorFragment extends Fragment implements OnClickListener
{
	private int error_reason_id;
	private Button recovery_button;
	private Button log_button;
	private Button refresh_button;

	public ErrorFragment() {
		error_reason_id = 0;
		recovery_button = null;
		log_button = null;
		refresh_button = null;
	}

	public void setErrorReason(int reason_resid) {
		this.error_reason_id = reason_resid;
	}

	@Override
	public void onAttach (Activity activity) {
		super.onAttach(activity);
		Log.d("RecoveryFragment", "Fragment attached.");
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState)
	{
		Log.d("RecoveryFragment", "Fragment view created.");
		
		View view = inflater.inflate(R.layout.flagment_error, container, false);
		
		if (error_reason_id != 0) {
			TextView reasonView = (TextView)view.findViewById(R.id.error_reason);
			reasonView.setText(error_reason_id);
		}

		recovery_button = (Button) view.findViewById(R.id.error_recovery_button);
		recovery_button.setOnClickListener(this);
		
		log_button = (Button) view.findViewById(R.id.error_log_button);
		log_button.setOnClickListener(this);

		refresh_button = (Button) view.findViewById(R.id.error_refresh_button);
		refresh_button.setOnClickListener(this);

		return view;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d("RecoveryFragment", "Fragment created.");
	}
	
	@Override
	public void onPause() {
		super.onPause();
		Log.d("RecoveryFragment", "Fragment pasued.");
	}

	@Override
	public void onClick(View view) {
		if (view == recovery_button) {
			onRecoveryButtonClicked(view);
		} else if (view == log_button) {
			onLogButtonClicked(view);
		} else if (view == refresh_button) {
			onRefreshHomeButtonClicked(view);
		}
	}

	public void onLogButtonClicked(View view) {
		Intent intent = new Intent(getActivity(), LogActivity.class);
		startActivity(intent);
	}

	public void onRefreshHomeButtonClicked(View view) {
		Activity activity = getActivity();
		if (activity instanceof HomeActivity)
			((HomeActivity) activity).refreshLayout();
	}

	public void onRecoveryButtonClicked(View button) {
		Log.d("RecoveryFragment", "Recovery button click...");
	}

}
