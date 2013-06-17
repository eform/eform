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
public class RecoveryFragment extends Fragment implements OnClickListener
{
	private int errorReasonID;
	private Button recoveryButton;

	public RecoveryFragment() {
		errorReasonID = 0;
		recoveryButton = null;
	}

	public void setErrorReason(int reasonResID) {
		this.errorReasonID = reasonResID;
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
		
		View view = inflater.inflate(R.layout.fragment_recovery, container, false);
		
		if (errorReasonID != 0) {
			TextView reasonView = (TextView)view.findViewById(R.id.error_reason);
			reasonView.setText(errorReasonID);
		}

		recoveryButton = (Button)view.findViewById(R.id.error_recovery_button);
		recoveryButton.setOnClickListener(this);
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
		if (view == recoveryButton)
			onRecoveryButtonClicked(view);
	}

	public void onRecoveryButtonClicked(View button) {
		Log.d("RecoveryFragment", "Recovery button click...");
	}

}
