package com.cansiny.eform;

import android.app.Activity;
import android.os.Bundle;

public class IDLItemActivity extends Activity
{
	static public final String INTENT_MESSAGE_NAME = "com.cansiny.eform.NAME";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_item);

	}
}
