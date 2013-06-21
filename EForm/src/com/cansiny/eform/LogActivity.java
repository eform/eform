package com.cansiny.eform;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class LogActivity extends Activity
{
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_log);

		String buffer = RecoveryFragment.readLog();
		if (buffer != null) {
			EditText edit = (EditText) findViewById(R.id.log_text);
//			buffer.substring(0);
			edit.setText(buffer);
		}
	}

	public void onCloseButtonClicked(View view) {
		finish();
	}
}
