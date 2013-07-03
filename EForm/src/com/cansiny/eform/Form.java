package com.cansiny.eform;

import java.util.ArrayList;

import com.cansiny.eform.FormScrollView.ScrollViewListener;

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


/**
 * Swipe magcard dialog
 */
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


/**
 * Read ID Card dialog
 */
class ReadIdcardDialogFragment extends DialogFragment
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
		builder.setTitle(R.string.read_idcard);
		LayoutInflater inflater = getActivity().getLayoutInflater();
		builder.setView(inflater.inflate(R.layout.dialog_idcard, null));
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


/**
 * Form scroll view
 */
class FormScrollView extends ScrollView
{
	private ScrollViewListener listener;
	
	public FormScrollView(Context context) {
		super(context);
		listener = null;
		setSmoothScrollingEnabled(true);
	}

	/* check if scroll view at top/bottom */
	public boolean isAtTop() {
		return getScrollY() == 0;
	}
	public boolean isAtBottom() {
		if (getChildCount() == 0)
			return true;
		
		if (getBottom() == 0)
			return false;

		View child = getChildAt(getChildCount() - 1);
		return child.getBottom() == getScrollY() + getBottom();
	}
	
	/* event listener */
	public void setListener(ScrollViewListener listener) {
		this.listener = listener;
	}
	@Override
	protected void onScrollChanged(int l, int t, int oldl, int oldt) {
		super.onScrollChanged(l, t, oldl, oldt);

		if (listener != null)
			listener.onScollChanged(this);
	}
	public interface ScrollViewListener
	{
		public void onScollChanged(ScrollView scroll_view);
	}
}


public abstract class Form implements OnClickListener, OnFocusChangeListener, ScrollViewListener
{
	public ArrayList<FormPage> pages;
	protected Activity activity;
	protected ArrayList<Integer> cardno_views; 
	private FormListener listener;
	SwipeMagcardDialogFragment magcard_dialog;
	ReadIdcardDialogFragment idcard_dialog;

	public Form(Activity activity) {
		this.activity = activity;
		pages = new ArrayList<FormPage>();
		cardno_views = new ArrayList<Integer>();
		listener = null;
	}

	/* set event listener */
	public void setListener(FormListener listener) {
		this.listener = listener;
	}


	public boolean verify() {
		return true;
	}
	
	
	public boolean print() {
		return true;
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

	/* check off sibling in same layout */
	protected void checkOffSibling(CheckBox checkbox) {
		if (checkbox.isChecked()) {
			ViewGroup parent = (ViewGroup) checkbox.getParent();
			for (int i = 0; i < parent.getChildCount(); i++) {
				Object child = parent.getChildAt(i);
				if (child instanceof CheckBox && child != checkbox)
					((CheckBox) child).setChecked(false);
			}
		}
	}


	/* popup a dialog and read magcard */
	protected CharSequence swipeMagcard() {
		magcard_dialog = new SwipeMagcardDialogFragment();
		magcard_dialog.show(activity.getFragmentManager(), "SwipeMagcardDialog");
		MagcardReader reader = new MagcardReader();
		return reader.getCardno();
	}


	/* popup a dialog and read id card information */
	protected void readIdCard() {
		idcard_dialog = new ReadIdcardDialogFragment();
		idcard_dialog.show(activity.getFragmentManager(), "ReadIdcardDialog");
	}


	/* call by page when its state be saved */
	public void onPageSaved(FormPage page) {
		/* save card no tag */
		if (page.scroll_view != null) {
			for (Integer viewid : cardno_views) {
				View view = page.scroll_view.findViewById(viewid.intValue());
				if (view == null)
					continue;
				Object tag = view.getTag();
				if (tag != null && ((CharSequence) tag).length() > 0) {
					String key = viewid.toString();
					if (page.bundle.containsKey(key)) {
						page.bundle.putCharSequence(key + "-tag", (CharSequence) tag);
					}
				}
			}
		}
	}

	
	/* call by page when its state be restored */
	public void onPageRestored(FormPage page) {
		((FormScrollView) page.scroll_view).setListener(this);

		/* listen all views event */
		addViewsListener(page.scroll_view);
		
		/* restore card no tag */
		for (Integer viewid : cardno_views) {
			View view = page.scroll_view.findViewById(viewid.intValue());
			if (view == null)
				continue;
			String key = viewid.toString() + "-tag";
			if (page.bundle.containsKey(key)) {
				view.setTag(page.bundle.getCharSequence(key));
			}
		}
	}


	/* call by activity when page be insert to layout */
	public void onPageStart(int page_no) {
		if (page_no < 0 || page_no >= pages.size())
			return;
		
		FormPage page = pages.get(page_no);
		
		/* restore view focus */
		int focus_id = page.bundle.getInt("focus-view");
		View view = page.scroll_view.findViewById(focus_id);
		if (view != null)
			view.requestFocus();
	}

	
	/* traversing the layout tree to set view's listener. */
	private void addViewsListener(Object object) {
		View view = (View) object;
		view.setClickable(true);
		view.setOnClickListener(this);

		if (object instanceof ViewGroup) {
			ViewGroup group = (ViewGroup) object;
			for (int i = 0; i < group.getChildCount(); i++) {
				addViewsListener(group.getChildAt(i));
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

	
	@Override
	public void onScollChanged(ScrollView scroll_view) {
		/* notify activity the from has scrolled. */
		if (listener != null)
			listener.onScrollViewScrolled(this, scroll_view);
	}


	@Override
	public void onClick(View view) {
		/* format card no if its view lost focus */
		for (Integer viewid : cardno_views) {
			if (view.getId() == viewid.intValue())
				continue;
			
			TextView textview = findTextView(viewid.intValue());
			if (textview != null) {
				CharSequence cardno = (CharSequence) textview.getTag();
				if (cardno != null)
					textview.setText(MagcardReader.formatCardno(cardno , 4, 3));
			}
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
		for (Integer viewid : cardno_views) {
			if (view.getId() == viewid.intValue()) {
				TextView textview = findTextView(viewid.intValue());
				CharSequence cardno = (CharSequence) textview.getTag();
				if (cardno != null) {
					if (hasfocus)
						textview.setText(cardno);
					else
						textview.setText(MagcardReader.formatCardno(cardno , 4, 3));
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
		for (Integer viewid : cardno_views) {
			if (textview.getId() == viewid.intValue()) {
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
	 * a class to hold a form page
	 */
	public class FormPage
	{
		public String title;
		public int layout;
		public ScrollView scroll_view;
		public Bundle bundle;

		private void initialize(String title, int layout) {
			this.title = title;
			this.layout = layout;
			scroll_view = null;
			bundle = new Bundle();
		}

		public FormPage(String title, int layout) {
			initialize(title, layout);
		}
		public FormPage(int title_id, int layout) {
			String title = activity.getResources().getString(title_id);
			initialize(title, layout);
		}

		public ScrollView restoreState() {
			FormScrollView scroll = new FormScrollView(activity.getApplicationContext());
			LayoutInflater inflater = activity.getLayoutInflater();
			scroll_view = (ScrollView) inflater.inflate(layout, scroll);
			storeViewState(scroll_view);
			onPageRestored(this);
			return scroll_view;
		}

		private void storeViewState(Object object) {
			if (object instanceof ViewGroup) {
				ViewGroup group = (ViewGroup) object;
				for (int i = 0; i < group.getChildCount(); i++) {
					storeViewState(group.getChildAt(i));
				}
				return;
			}

			String key = Integer.valueOf(((View) object).getId()).toString();
			if (bundle.containsKey(key)) {
				if (object instanceof EditText) {
					((EditText) object).setText(bundle.getCharSequence(key));
				} else if (object instanceof CheckBox) {
					((CheckBox) object).setChecked(true);
				}
			}
		}


		public void saveState() {
			if (scroll_view != null) {
				saveViewState(scroll_view);

				View view = scroll_view.findFocus();
				if (view != null)
					bundle.putInt("focus-view", view.getId());
				else
					bundle.remove("focus-view");

				onPageSaved(this);
				scroll_view = null;
			}
		}

		private void saveViewState(Object object) {
			if (object instanceof ViewGroup) {
				ViewGroup group = (ViewGroup) object;
				for (int i = 0; i < group.getChildCount(); i++) {
					saveViewState(group.getChildAt(i));
				}
				return;
			}
			
			if (object instanceof EditText) {
				EditText edit_text = (EditText) object;
				Editable text = edit_text.getText();
				if (text.length() > 0) {
					bundle.putCharSequence(Integer.valueOf(edit_text.getId()).toString(), text);
				}
			} else if (object instanceof CheckBox) {
				CheckBox checkbox = (CheckBox) object;
				if (checkbox.isChecked())
					bundle.putBoolean(Integer.valueOf(checkbox.getId()).toString(), true);
			}
		}


		/* handler scroll view */
		public boolean canScrollUp() {
			if (scroll_view == null || !((Object) scroll_view instanceof ScrollView))
				return false;
			return ((FormScrollView) scroll_view).isAtTop() ? false : true;
		}
		public boolean canScrollDown() {
			if (scroll_view == null || !((Object) scroll_view instanceof ScrollView))
				return false;
			return ((FormScrollView) scroll_view).isAtBottom() ? false : true;
		}
		public void scrollUp() {
			if (scroll_view == null || !((Object) scroll_view instanceof ScrollView))
				return;
			scroll_view.smoothScrollBy(0, -100);
		}
		public void scrollDown() {
			if (scroll_view == null || !((Object) scroll_view instanceof ScrollView))
				return;
			scroll_view.smoothScrollBy(0, 100);
		}
	}
	

	/**
	 * Form listener interface
	 */
	public interface FormListener
	{
		/* fire when scroll view scrolled. */
		public void onScrollViewScrolled(Form form, ScrollView scroll_view);
	}
}
