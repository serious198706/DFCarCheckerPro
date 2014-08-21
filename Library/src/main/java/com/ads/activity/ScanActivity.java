package com.ads.activity;

import java.util.ArrayList;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.df.library.R;
import com.ads.model.CategoryList;
import com.ads.model.MenuItem;
import com.ads.view.ImageTextView;
import com.df.library.util.MyApplication;

public class ScanActivity extends BaseActivity implements OnItemClickListener
{
	private static final String tag = ScanActivity.class.getSimpleName();

	private static final int ACTIVITY_RESULT_CODE_FOR_DLL_EXIT_VALUE = 1;
	private CategoryList mCategoryList = null;
	private MenuItem mCurrentMenu;
	private ListView mAdapterView = null;
	private ArrayList<MenuItem> mModuleListForTrial = null;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		final ProgressDialog progress = ProgressDialog.show(this, "ADS",
				getString(R.string.loading));
		final Handler handler = new Handler();

		new Thread()
		{
			@Override
			public void run()
			{
				try
				{
					String language = getResources().getConfiguration().locale.getLanguage();
					mCategoryList = CategoryList.create(language);
				} catch (Exception e)
				{
					e.printStackTrace();
				}
				progress.dismiss();
				handler.post(new Runnable()
				{
					@Override
					public void run()
					{
						mCurrentMenu = mCategoryList;
						if (mAdapterView != null)
						{
							mAdapterView.invalidateViews();
						}
					}
				});
			}
		}.start();

		mAdapterView = new ListView(this);
		mAdapterView.setAdapter(new MyAdapter());
		mAdapterView.setOnItemClickListener(this);

		setContentView(mAdapterView);
	}

	class MyAdapter extends BaseAdapter
	{

		@Override
		public int getCount()
		{
			if (mCurrentMenu != null)
			{
				return mCurrentMenu.size();
			}
			return 0;
		}

		@Override
		public Object getItem(int position)
		{
			return null;
		}

		@Override
		public long getItemId(int position)
		{
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
			ImageTextView view;
			if (convertView != null)
			{
				view = (ImageTextView) convertView;
			} else
			{
				view = new ImageTextView(ScanActivity.this);
			}

			MenuItem item = mCurrentMenu.getItem(position);
			int drawable = MyApplication.getDrawableResource(item.pic);

			view.setHorizontal();

			if (item.level <= 0)
			{
				view.setImageAndText(drawable, item.title);
				view.setTextGravity(Gravity.CENTER);
			} else
			{
				view.setImageAndText(null,
						String.format("%02d. %s", position + 1, item.title));
				view.setTextGravity(Gravity.CENTER_HORIZONTAL | Gravity.LEFT);
			}
			if (mCurrentMenu.level < -1)
			{
				view.setLargeSize();
			} else
			{
				view.setSmallSize();
			}

			return view;
		}

	}

	@Override
	public void onItemClick(AdapterView<?> listView, View v, int index, long id)
	{
		MenuItem item = mCurrentMenu.getItem(index);
		if (item.size() > 0)
		{
			if (item.getItem(0).title.equals("<SCAN>"))
			{ // Special handling of <SCAN> tags
				Log.i(tag, "Get <SCAN></SCAN> tag, create trial modules list.");
				mModuleListForTrial = new ArrayList<MenuItem>();

				for (int i = 1/* ignore the first <Scan> */; i <= item.size() - 2/*
																				 * ignore
																				 * the
																				 * last
																				 * <
																				 * /
																				 * SCAN
																				 * >
																				 */; i++)
				{
					Log.i(tag, "Add module: " + item.getItem(i).title);
					mModuleListForTrial.add(item.getItem(i));
				}
				startTrialModuleList();
			} else
			{
				int pos = mAdapterView.getLastVisiblePosition() - 1;
				mCurrentMenu.lastVisitPostion = pos;
				mCurrentMenu = item;

				mAdapterView.invalidateViews();
				mAdapterView.smoothScrollToPosition(0);
			}
		} else
		{
			Log.d(tag, "ConnectorType=" + mConnectorType);
			if (mConnectorType == CONNECTOR_TYPE_BLUETOOTH)
			{
//				Log.i(tag, "Try to find the last connected bluetooth device.");
//				findAndMarkLastConnectedBluetoothConnecter();
			}
			startWorkingActivity(item);
		}

	}

	private void startTrialModuleList()
	{
		if (mModuleListForTrial != null && mModuleListForTrial.size() > 0)
		{
			MenuItem item = mModuleListForTrial.remove(0);
			mAllowShowDialog = false; // Disable dialog during auto trials
			Log.i(tag, "start Trial Module List, module = " + item.title);
			startWorkingActivity(item);
		} else
		{
			mAllowShowDialog = true;
		}
	}

	private void startWorkingActivity(MenuItem item)
	{
//		showProgressDialog("", "加载诊断模块");
		Log.d(tag, "BoxID:"+getBoxId());
		
		Log.d(tag, String.format("link=%s, param=%s", item.link, item.param));
		Intent i = new Intent(this, WorkingActivity.class);
		i.putExtra("link", item.link);
		i.putExtra("param", item.param);
		startActivityForResult(i, ACTIVITY_RESULT_CODE_FOR_DLL_EXIT_VALUE);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		Log.i(tag, "onActivityResult, requestCode code = " + requestCode);
		if (requestCode == ACTIVITY_RESULT_CODE_FOR_DLL_EXIT_VALUE)
		{
			if (resultCode == RESCODE_BLUETOOTH_PAIR_FAIL)
			{
				hideProgressDialog();
				Toast.makeText(this, getString(R.string.bt_pair_fail), Toast.LENGTH_LONG).show();
			}else if (resultCode == RESCODE_BLUETOOTH_CONNECT_FAIL)
			{
				hideProgressDialog();
				Toast.makeText(this, getString(R.string.bt_connect_fail), Toast.LENGTH_LONG).show();
			}else
				
			
			Log.i(tag, "DLL thread exit value = " + resultCode);
			if (resultCode != 0 && (mModuleListForTrial != null))
			{ // Special case for <SCAN></SCAN> list
				Log.w(tag, "Try next one in <SCAN> list.");
				startTrialModuleList();
			}
		}
	}

	@Override
	public void onBackPressed()
	{
		mModuleListForTrial = null; // Clear this when back to parent item or
									// former activity
		if (mCurrentMenu.parent != null)
		{
			mCurrentMenu = mCurrentMenu.parent;
			mAdapterView.invalidateViews();
			mAdapterView.postDelayed(new Runnable()
			{
				@Override
				public void run()
				{
					mAdapterView
							.smoothScrollToPosition(mCurrentMenu.lastVisitPostion);
				}
			}, 10);
		} else
		{
			super.onBackPressed();
		}
	}
}