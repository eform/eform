package com.cansiny.eform;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.atomic.AtomicInteger;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ViewFlipper;

public class FormActivity extends Activity implements OnClickListener
{
	/* constants uses for communication with previous activity. */
	static public final String INTENT_MESSAGE_CLASS    = "com.cansiny.eform.CLASS";
	static public final String INTENT_MESSAGE_LABEL    = "com.cansiny.eform.LABEL";
	static public final String INTENT_RESULT_ERRREASON = "com.cansiny.eform.ERRREASON";

	/* constants uses for identified the difference of event source */
	static private final String VIEW_TAG_STEP_BUTTON = "com.cansiny.eform.STEP_BUTTON";

	private AtomicInteger atomic_int;
	private Form form;
	private int current_step;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		atomic_int = new AtomicInteger(HomeActivity.ITEM_VIEW_ID_BASE);
		current_step = -1;

		setContentView(R.layout.activity_form);

		/* set form label */
		TextView text_view = (TextView) findViewById(R.id.label_textview);
		String label = getResources().getString(getIntent().getIntExtra(INTENT_MESSAGE_LABEL, 0));
		text_view.setText(label.replaceAll("\n", ""));

		try {
			/* get form class from home activity */
			String klass = getIntent().getStringExtra(INTENT_MESSAGE_CLASS);
			if (klass == null) {
				Log.e("FormActivity", "Intent missing 'klass' attribute");
				finish();
				return;
			}
			/* build a instance from class name */
			form = (Form) Class.forName(klass).getConstructor(Activity.class).newInstance(this);

			ViewGroup step_layout = (ViewGroup) findViewById(R.id.step_layout);
			ViewFlipper flipper = (ViewFlipper) findViewById(R.id.flipper_view);
			for (Form.FormPage page : form.pages) {
				step_layout.addView(createStepButton(page.title));

				FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
						ViewGroup.LayoutParams.MATCH_PARENT,
						ViewGroup.LayoutParams.MATCH_PARENT);
				params.leftMargin = 120;
				params.topMargin = 100;
				params.rightMargin = 120;

				flipper.addView(page.layout, params);
				form.pageVisibled(page);
			}
			/* default active the first step */
			gotoStep(0);

			/* hide the soft keyboard until user touch the edit text */
			getWindow().setSoftInputMode(
				      WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
			} catch (InstantiationException e) {
			LogActivity.writeLog(e);
			getIntent().putExtra(INTENT_RESULT_ERRREASON, R.string.error_instantiation_exception);
			setResult(RESULT_CANCELED, getIntent());
			finish();
		} catch (IllegalAccessException e) {
			LogActivity.writeLog(e);
			getIntent().putExtra(INTENT_RESULT_ERRREASON, R.string.error_instantiation_exception);
			setResult(RESULT_CANCELED, getIntent());
			finish();
		} catch (ClassNotFoundException e) {
			LogActivity.writeLog(e);
			getIntent().putExtra(INTENT_RESULT_ERRREASON, R.string.error_class_not_found);
			setResult(RESULT_CANCELED, getIntent());
			finish();
		} catch (IllegalArgumentException e) {
			LogActivity.writeLog(e);
			getIntent().putExtra(INTENT_RESULT_ERRREASON, R.string.error_instantiation_exception);
			setResult(RESULT_CANCELED, getIntent());
			finish();
		} catch (InvocationTargetException e) {
			LogActivity.writeLog(e);
			getIntent().putExtra(INTENT_RESULT_ERRREASON, R.string.error_instantiation_exception);
			setResult(RESULT_CANCELED, getIntent());
			finish();
		} catch (NoSuchMethodException e) {
			LogActivity.writeLog(e);
			getIntent().putExtra(INTENT_RESULT_ERRREASON, R.string.error_instantiation_exception);
			setResult(RESULT_CANCELED, getIntent());
			finish();
		}		
	}
	
	private View createStepButton(String title) {
		LinearLayout button_layout = new LinearLayout(getApplicationContext());
		button_layout.setId(atomic_int.incrementAndGet());
		button_layout.setOrientation(LinearLayout.VERTICAL);
		
		Button button = new Button(getApplicationContext());
		button.setId(atomic_int.incrementAndGet());
		button.setTag(VIEW_TAG_STEP_BUTTON);
		button.setText(title);
		button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);
		button.setPadding(4, 2, 4, 2);
		button.setClickable(true);
		button.setOnClickListener(this);

		LinearLayout.LayoutParams button_params = new LinearLayout.LayoutParams(
				ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		button_params.gravity = Gravity.CENTER;
		button_layout.addView(button, button_params);

		ImageView line = new ImageView(getApplicationContext());
		line.setId(atomic_int.incrementAndGet());

		LinearLayout.LayoutParams line_params = new LinearLayout.LayoutParams(
				ViewGroup.LayoutParams.MATCH_PARENT,
				(int) HomeActivity.convertDpToPixel(6));
		button_layout.addView(line, line_params);

		return button_layout;
	}

	private void resetStepLayout() {
		LinearLayout step_layout = (LinearLayout) findViewById(R.id.step_layout);
		for (int i = 0; i < step_layout.getChildCount(); i++) {
			LinearLayout button_layout = (LinearLayout) step_layout.getChildAt(i);

			Button button = (Button) button_layout.getChildAt(0);
			button.setBackgroundResource(R.color.translucence);
			button.setTextColor(getResources().getColor(R.color.white));
			
			ImageView line = (ImageView) button_layout.getChildAt(1);
			line.setBackgroundResource(R.color.blue);
		}
	}

	private void gotoStep(int index) {
		if (index == current_step)
			return;

		LinearLayout step_layout = (LinearLayout) findViewById(R.id.step_layout);

		if (index >= step_layout.getChildCount()) {
			Log.e("ItemActivity", String.format("Try to goto unexists step %d", index));
			return;
		}
		resetStepLayout();

		LinearLayout button_layout = (LinearLayout) step_layout.getChildAt(index);

		Button button = (Button) button_layout.getChildAt(0);
		button.setBackgroundResource(R.color.white);
		button.setTextColor(getResources().getColor(R.color.blue));
		
		ImageView image = (ImageView) button_layout.getChildAt(1);
		image.setBackgroundResource(R.color.red);

		ViewFlipper flipper = (ViewFlipper) findViewById(R.id.flipper_view);
		if (current_step != index)
			flipper.setDisplayedChild(index);

		current_step = index;

		button = (Button) findViewById(R.id.prev_button);
		if (current_step == 0) {
			button.setEnabled(false);
		} else {
			button.setEnabled(true);
		}

		button = (Button) findViewById(R.id.next_button);
		if (current_step + 1 == form.pages.size()) {
			button.setEnabled(false);
		} else {
			button.setEnabled(true);
		}
	}

	@Override
	public void onClick(View view) {
		Object object = view.getTag();
		if (object == null) {
			Log.e("ItemActivity", "Clickable object missing tag");
			return;
		}

		if (object.getClass() == String.class) {
			String string = (String) object;
			if (string.equals(VIEW_TAG_STEP_BUTTON)) {
				LinearLayout step_layout = (LinearLayout) findViewById(R.id.step_layout);
				int index = step_layout.indexOfChild((View) view.getParent());
				Log.d("", String.format("click %d", index));
				gotoStep(index);
			}
		}
	}
}
