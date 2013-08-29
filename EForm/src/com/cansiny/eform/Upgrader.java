/* EForm - Electronic Form System
 *
 * Copyright (C) 2013 Wu Xiaohu. All rights reserved.
 * Copyright (C) 2013 Cansiny Trade Co.,Ltd. All rights reserved.
 */
package com.cansiny.eform;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URI;
import java.security.MessageDigest;
import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;


abstract public class Upgrader extends Utils.DialogFragment
	implements OnCheckedChangeListener
{
    static public final int FILTER_PRINTCONF = 1;
    static public final int FILTER_HOMELOGO  = 2;
    static public final int FILTER_EFORMAPK  = 3;

    static public Upgrader getUpgrader() {
	return new UpgraderUDisk();
    }

    protected ListView listview;
    protected LinearLayout progress_layout;

    public Upgrader() {
    }

    private View buildLayout() {
	LinearLayout linear = new LinearLayout(getActivity());
	linear.setOrientation(LinearLayout.VERTICAL);

	RadioGroup group = new RadioGroup(getActivity());
	group.setOrientation(LinearLayout.HORIZONTAL);
	group.setBackgroundResource(R.color.yellow);
	group.setPadding(10, 4, 10, 10);
	linear.addView(group);

	TextView textview = new TextView(getActivity());
	textview.setText("升级内容：");
	textview.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
	textview.setPadding(10, 10, 10, 0);
	group.addView(textview);

	RadioButton radio = new RadioButton(getActivity());
	radio.setText("打印配置");
	radio.setTag(FILTER_PRINTCONF);
	radio.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17);
	radio.setPadding(22, 0, 10, 0);
	radio.setOnCheckedChangeListener(this);
	group.addView(radio);

	radio = new RadioButton(getActivity());
	radio.setText("主页徽标");
	radio.setTag(FILTER_HOMELOGO);
	radio.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17);
	radio.setPadding(22, 0, 10, 0);
	radio.setOnCheckedChangeListener(this);
	group.addView(radio);

	radio = new RadioButton(getActivity());
	radio.setText("电子填单软件");
	radio.setTag(FILTER_EFORMAPK);
	radio.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17);
	radio.setPadding(22, 0, 0, 0);
	radio.setOnCheckedChangeListener(this);
	group.addView(radio);

	listview = new ListView(getActivity());
	linear.addView(listview);

	progress_layout = new LinearLayout(getActivity());
	progress_layout.setOrientation(LinearLayout.HORIZONTAL);
	progress_layout.setGravity(Gravity.CENTER);
	progress_layout.setPadding(0, 10, 0, 10);
	progress_layout.setVisibility(View.GONE);
	linear.addView(progress_layout);

	ProgressBar progressbar = new ProgressBar(getActivity());
	progressbar.setIndeterminate(true);
	progress_layout.addView(progressbar);

	textview = new TextView(getActivity());
	textview.setText("正在加载 ...");
	textview.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
	textview.setPadding(10, 0, 0, 0);
	progress_layout.addView(textview);

	return linear;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
	super.onCreateDialog(savedInstanceState);

	AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
	builder.setTitle("系统升级");
	builder.setView(buildLayout());

	return builder.create();
    }

    @Override
    public void onStart() {
	super.onStart();

	loadContents();
    }

    @Override
    public void onCheckedChanged(CompoundButton button, boolean isChecked) {
	if (isChecked) {
	    ListAdapter adapter = listview.getAdapter();
	    if (adapter != null) {
		int filter = ((Integer) button.getTag()).intValue();
		((ContentsAdapter) adapter).setFilter(filter);
	    }
	}
    }

    abstract protected void loadContents();

    protected void setListviewAdapter(ContentsAdapter adapter) {
	listview.setAdapter(adapter);

	if(adapter.getCount() > 8) {
	    View item = adapter.getView(0, null, listview);
	    item.measure(0, 0);
	    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
		    ViewGroup.LayoutParams.MATCH_PARENT,
		    (int) (8.5 * item.getMeasuredHeight()));
	    listview.setLayoutParams(params);
	}
    }

    class ContentsAdapter extends BaseAdapter
    {
	class ContentItem
	{
	    public URI srcuri;
	    public byte[] srcmd5;
	    public int matcher;
	    protected File destfile;
	    protected boolean upgradable;
	}

	private ArrayList<ContentItem> items;
	private int filter;

	public ContentsAdapter() {
	    items = new ArrayList<ContentItem>();
	    filter = FILTER_PRINTCONF;
	}

	public void setFilter(int filter) {
	    if (filter == this.filter) {
		return;
	    }
	    this.filter = filter;
	    notifyDataSetChanged();
	}

	public void addItem(URI srcuri, byte[] srcmd5, int matcher) {
	    ContentItem item = new ContentItem();

	    item.srcuri = srcuri;
	    item.srcmd5 = srcmd5;
	    item.matcher = matcher;

	    switch(matcher) {
	    case FILTER_PRINTCONF:
		String path = item.srcuri.getPath();
		int index = path.lastIndexOf("/");
		if (index == -1) {
		    item.destfile = new File("print/" + path);
		} else {
		    item.destfile = new File("print/" + path.substring(index + 1));
		}
		byte[] md5 = getMD5ForFile(item.destfile);
		item.upgradable = (md5 == srcmd5) ? false : true;
		break;
	    case FILTER_HOMELOGO:
		break;
	    case FILTER_EFORMAPK:
		break;
	    default:
		return;
	    }
	    items.add(item);
	}

	private byte[] getMD5ForFile(File file) {
	    try {
		InputStream input = new FileInputStream(file);
		byte[] buffer = new byte[1024];
		MessageDigest digest = MessageDigest.getInstance("MD5");
		int count = 0;
		do {
		    count = input.read(buffer);
		    if (count > 0) {
			digest.update(buffer, 0, count);
		    }
		} while (count != -1);
		
		input.close();
		return digest.digest();
	    } catch (Exception e) {
		LogActivity.writeLog(e);
		return null;
	    }
	}

	private ContentItem getItemAtPosition(int position) {
	    int index = 0;
	    for (ContentItem item : items) {
		if (item.matcher == filter) {
		    if (index++ == position) {
			return item;
		    }
		}
	    }
	    return null;
	}

	@Override
	public int getCount() {
	    int count = 0;
	    for (ContentItem item : items) {
		if (item.matcher == filter) {
		    count++;
		}
	    }
	    return (count > 0) ? count : 1;
	}

	@Override
	public Object getItem(int position) {
	    return getItemAtPosition(position);
	}

	@Override
	public long getItemId(int position) {
	    return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
	    ContentItem item = getItemAtPosition(position);
	    if (item == null) {
		TextView textview = new TextView(getActivity());
		textview.setText("未找到可升级文件");
		textview.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
		textview.setTextColor(getResources().getColor(R.color.red));
		textview.setPadding(20, 10, 0, 10);
		return textview;
	    }
	    TextView textview = new TextView(getActivity());
	    textview.setText(item.srcuri.getPath());
	    return textview;
	}

    }

    class upgradeTask extends AsyncTask<Void, Void, Void>
    {
	@Override
	protected Void doInBackground(Void... params) {
	    // TODO Auto-generated method stub
	    return null;
	}
    }
}


class UpgraderUDisk extends Upgrader
{
    static private final String UDISK_HOME = "/mnt/usbhost1";

    private loadContentsTask task;

    public UpgraderUDisk() {
	task = null;
    }

    @Override
    protected void loadContents() {
	File file = new File(UDISK_HOME);
	if (!file.exists()) {
	    showToast("U盘未找到，请检查U盘是否已插入");
	    listview.setAdapter(new ContentsAdapter());
	    return;
	}
	task = new loadContentsTask();
	task.execute();
    }

    class loadContentsTask extends AsyncTask<Void, Void, ContentsAdapter>
    {
	@Override
	protected void onPreExecute() {
	    progress_layout.setVisibility(View.VISIBLE);
	}

	@Override
	protected ContentsAdapter doInBackground(Void... args) {
	    ContentsAdapter adapter = new ContentsAdapter();
	    File root = new File(UDISK_HOME);
	    File[] filelist = root.listFiles();
	    for (File file : filelist) {
		if (isCancelled()) {
		    return null;
		}
		adapter.addItem(file.toURI(), null, FILTER_PRINTCONF);
		try {
		    Thread.sleep(200);
		} catch (InterruptedException e) {
		    // TODO Auto-generated catch block
		    e.printStackTrace();
		}
	    }
	    return adapter;
	}

	@Override
	protected void onPostExecute(ContentsAdapter result) {
	    progress_layout.setVisibility(View.GONE);
	    if (result == null) {
		showToast("加载升级内容失败！", R.drawable.cry);
	    } else {
		setListviewAdapter(result);
	    }
	}

	@Override
	protected void onCancelled(ContentsAdapter result) {
	    Utils.showToast("操作被取消！", R.drawable.cry);
	}

    }

    @Override
    public void onDismiss(DialogInterface dialog) {
	super.onDismiss(dialog);
	if (task != null) {
	    if (task.getStatus() != AsyncTask.Status.FINISHED) {
		task.cancel(true);
	    }
	}
    }
}


class UpgradeNetwork extends Upgrader
{
    public UpgradeNetwork() {
		
    }

    @Override
    protected void loadContents() {
	// TODO Auto-generated method stub
	
    }

}
