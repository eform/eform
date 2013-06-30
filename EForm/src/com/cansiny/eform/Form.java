package com.cansiny.eform;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;


class SwipeMagcardDialogFragment extends DialogFragment
{
	private int total_seconds = 30;
	private long starttime;

	Handler handler = new Handler();
	Runnable runable = new Runnable() {
		@Override
		public void run() {
			long currtime = System.currentTimeMillis();
			long millis = currtime - starttime;
			starttime = currtime;
			int seconds = (int) (millis / 1000);

			total_seconds -= seconds;
			if (total_seconds <= 0) {
				dismiss();
				return;
			}
			
			TextView textview = (TextView) getDialog().findViewById(R.id.second_textview);
			textview.setText("" + total_seconds);
			
			handler.postDelayed(this, 1000);
		}
	};

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		super.onCreateDialog(savedInstanceState);

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(R.string.swipe_card);
		LayoutInflater inflater = getActivity().getLayoutInflater();
		builder.setView(inflater.inflate(R.layout.dialog_magcard, null));
		builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
			}
		});
		return builder.create();
	}
	
	@Override
	public void onStart() {
		super.onStart();

		starttime = System.currentTimeMillis();
		TextView textview = (TextView) getDialog().findViewById(R.id.second_textview);
		textview.setText("" + total_seconds);
		handler.postDelayed(runable, 0);
	}
	
	@Override
	public void onDismiss (DialogInterface dialog) {
		super.onDismiss(dialog);
		handler.removeCallbacks(runable);
	}
}


public abstract class Form implements OnClickListener, OnFocusChangeListener
{
	public ArrayList<FormPage> pages;
	protected Activity activity;
	protected ArrayList<TextView> cardno_textviews; 
	SwipeMagcardDialogFragment dialogFragment;

	abstract public boolean validate();

	public Form(Activity activity) {
		this.activity = activity;
		pages = new ArrayList<FormPage>();
		cardno_textviews = new ArrayList<TextView>();
	}

	
	/* call by activity when page be insert to layout */
	public void pageVisibled(FormPage page) {
		
	}

	
	/* convenient method to find view and cast to corresponding type */
	protected View findView(int id) {
		return activity.findViewById(id);
	}
	protected EditText findEditText(int id) {
		return (EditText) activity.findViewById(id);
	}
	protected TextView findTextView(int id) {
		return (TextView) activity.findViewById(id);
	}
	protected CheckBox findCheckBox(int id) {
		return (CheckBox) activity.findViewById(id);
	}
	protected LinearLayout findLinearLayout(int id) {
		return (LinearLayout) activity.findViewById(id);
	}


	/* popup a dialog and read magcard */
	protected CharSequence swipeMagcard() {
		dialogFragment = new SwipeMagcardDialogFragment();
		dialogFragment.show(activity.getFragmentManager(), "SwipeMagcardDialog");
		MagcardReader reader = new MagcardReader();
		return reader.getCardno();
	}


	/* popup a dialog and read id card information */
	protected void readIdCard() {
		
	}

	
	/* traversing the layout tree to set view's listener. */
	private void traversingLayout(Object object) {
		View view = (View) object;
		view.setClickable(true);
		view.setOnClickListener(this);

		if (object instanceof ViewGroup) {
			ViewGroup group = (ViewGroup) object;
			for (int i = 0; i < group.getChildCount(); i++) {
				traversingLayout(group.getChildAt(i));
			}
			return;
		}
		
		if (object instanceof View) {
			view.setOnFocusChangeListener(this);
		}
		
		if (object instanceof TextView) {
			TextView text_view = (TextView) object;
			GenericTextWatcher wathcer = new GenericTextWatcher(text_view);
			text_view.addTextChangedListener(wathcer);
		}
	}

	protected void addFormPage(String title, int xml_resid) {
		FormPage page = new FormPage();

		page.title = title;

		ScrollView scroll = new ScrollView(activity.getApplicationContext());
		LayoutInflater inflater = activity.getLayoutInflater();
		page.layout = inflater.inflate(xml_resid, scroll);;
//		page.layout = scroll;

		/* listen all of child events*/
		pages.add(page);

		traversingLayout(page.layout);
	}

	protected void addFormPage(int title_resid, int xml_resid) {
		String title = activity.getResources().getString(title_resid);
		addFormPage(title, xml_resid);
	}

	@Override
	public void onClick(View view) {
		/* format card no if its view lost focus */
		for (TextView textview : cardno_textviews) {
			if (textview == view)
				continue;
			
			CharSequence cardno = (CharSequence) textview.getTag();
			if (cardno != null)
				textview.setText(MagcardReader.formatCardno(cardno , 4, 4));
		}

		/* clear edit text if clear button clicked*/
		if (view.getClass() == Button.class) {
			Object tag = view.getTag();
			if (tag != null && tag instanceof String) {
				if (((String) tag).equals("edittext_clear_button")) {
					ViewGroup parent = (ViewGroup) view.getParent();
					int index = parent.indexOfChild(view);
					if (index > 0) {
						EditText edit_text = (EditText) parent.getChildAt(index - 1);
						if (edit_text instanceof EditText) {
							edit_text.setText("");
						}
					}
				}
			}
		}
		
		/* hide the soft keyboard when click non-edit text widget */
		if (!(view instanceof EditText)) {
            InputMethodManager imm = 
            		(InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getApplicationWindowToken(), 0);
		}
	}

	
	@Override
	public void onFocusChange(View view, boolean hasfocus) {
		if (hasfocus) {
		}

		/* switch card no display format */
		for (TextView textview : cardno_textviews) {
			if (view == textview) {
				CharSequence cardno = (CharSequence) textview.getTag();
				if (cardno != null) {
					if (hasfocus)
						textview.setText(cardno);
					else
						textview.setText(MagcardReader.formatCardno(cardno , 4, 4));
				}
				break;
			}
		}
	}

	/* callback for edit text listener */
	protected void afterTextChanged(TextView textview, Editable editable) {
	}

	/* callback for edit text listener */
	protected void beforeTextChanged(TextView textview, CharSequence sequence,
				int start, int count, int after) {
	}
	
	/* callback for edit text listener */
	protected void onTextChanged(TextView textview, CharSequence sequence,
			int start, int before, int count) {
		/* record real card no to tag, and display card no with formatter */
		for (TextView view : cardno_textviews) {
			if (textview == view) {
				if (sequence.toString().matches("[0-9]*"))
					textview.setTag(sequence);
			}
		}
	}

	
	/**
	 * a class to attach view parameter to text watcher callback
	 */
	private class GenericTextWatcher implements TextWatcher
	{
		private TextView textview;
		private GenericTextWatcher(TextView view) {
			textview = view;
		}
		@Override
		public void afterTextChanged(Editable editable) {
			Form.this.afterTextChanged(textview, editable);
		}
		@Override
		public void beforeTextChanged(CharSequence sequence,
				int start, int count, int after) {
			Form.this.beforeTextChanged(textview, sequence, start, count, after);
		}
		@Override
		public void onTextChanged(CharSequence sequence,
				int start, int before, int count) {
			Form.this.onTextChanged(textview, sequence, start, before, count);
		}
	}


	/**
	 * a class to hold a page information
	 */
	public class FormPage
	{
		String title;
		View layout;
	}
}
