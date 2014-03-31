package pl.directsolutions.fit_keeper.view;

import java.util.ArrayList;

import pl.directsolutions.fit_keeper.R;
import pl.directsolutions.fit_keeper.controller.SettingsManager;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;

public class TrainSettingsActivity extends Activity
{

	private ListView settingsListView;
	private ArrayAdapter<RowModel> arrayAdapter;
	private ArrayList<RowModel> settingsList;
	private SettingsManager settingsManager;

	private int choosenPosition;
	private int temp;

	@Override
	public void onCreate(Bundle icicle)
	{
		super.onCreate(icicle);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.train_settings);

		settingsManager = SettingsManager.getInstance(TrainSettingsActivity.this);
	}

	@Override
	public void onResume()
	{
		super.onResume();

		settingsList = new ArrayList<RowModel>();
		settingsListView = (ListView) this.findViewById(R.id.settingsList);
		prepareSettingsList(settingsListView, settingsList);
	}

	private void prepareSettingsList(ListView listView, ArrayList<RowModel> list)
	{
		addElements(list);

		arrayAdapter = new RowAdapter(this, settingsList);

		listView.setOnItemClickListener(new OnItemClickListener()
		{
			public void onItemClick(AdapterView adapterView, View view, int index, long arg3)
			{

				String[] table;

				if (index == 0)
				{
					Intent intent = new Intent(TrainSettingsActivity.this, UserSettingsActivity.class);
					startActivity(intent);
				}

				else if (index == 1)
				{
					table = SettingsManager.getActivityValuesTable();
					for (int i = 0; i < table.length; i++)
					{
						if (settingsManager.getActivity().equals(table[i]))
						{
							choosenPosition = i;
							break;
						}
					}
					showAlertDialog(SettingsManager.ACTIVITY_PROMPT, table, index);
				}

				else if (index == 2)
				{
					table = SettingsManager.getUnitsValuesTable();
					for (int i = 0; i < table.length; i++)
					{
						if (settingsManager.getUnits().equals(table[i]))
						{
							choosenPosition = i;
							break;
						}
					}
					showAlertDialog(SettingsManager.UNITS_PROMPT, table, index);
				}

				else if (index == 3)
				{
					table = SettingsManager.getAutoPauseStringsTable();
					for (int i = 0; i < table.length; i++)
					{
						if (settingsManager.getAutoPause().equals(table[i]))
						{
							choosenPosition = i;
							break;
						}
					}
					showAlertDialog(SettingsManager.AUTOPAUSE_PROMPT, table, index);
				}

				else if (index == 4)
				{
					table = SettingsManager.getGpsRefreshRateStringsTable();
					for (int i = 0; i < table.length; i++)
					{
						if (settingsManager.getGpsRefreshRate().equals(table[i]))
						{
							choosenPosition = i;
							break;
						}
					}
					showAlertDialog(SettingsManager.GPS_REFRESH_RATE_PROMPT, table, index);
				}

				else if (index == 5)
				{
					Intent intent = new Intent(TrainSettingsActivity.this, NotificationSettingsActivity.class);
					startActivity(intent);
				}
			}
		});

		settingsListView.setAdapter(arrayAdapter);
	}

	private void addElements(ArrayList<RowModel> list)
	{
		for (int i = 0; i < settingsManager.getMainTagsListSize(); i++)
		{
			if (i == 0)
			{
				list.add(new RowModel(settingsManager.getMainTag(i), settingsManager.getUserDetails()));
			} else if (i == 1)
			{
				list.add(new RowModel(settingsManager.getMainTag(i), settingsManager.getActivity()));
			} else if (i == 2)
			{
				list.add(new RowModel(settingsManager.getMainTag(i), settingsManager.getUnits()));
			} else if (i == 3)
			{
				list.add(new RowModel(settingsManager.getMainTag(i), settingsManager.getAutoPause()));
			} else if (i == 4)
			{
				list.add(new RowModel(settingsManager.getMainTag(i), settingsManager.getGpsRefreshRate()));
			} else
			{
				list.add(new RowModel(settingsManager.getMainTag(i), settingsManager.getNotificationDetails())); 
			}
		}
	}

	public RowModel getModel(int position)
	{
		return (((RowAdapter) settingsListView.getAdapter()).getItem(position));
	}

	private class RowAdapter extends ArrayAdapter<RowModel>
	{
		Activity context;

		RowAdapter(Activity context, ArrayList<RowModel> list)
		{
			super(context, R.layout.settings_row, list);
			this.context = context;
		}

		public View getView(int position, View convertView, ViewGroup parent)
		{
			View row = convertView;
			ViewWrapper wrapper;

			if (row == null)
			{
				LayoutInflater inflater = context.getLayoutInflater();
				row = inflater.inflate(R.layout.settings_row, null);
				wrapper = new ViewWrapper(row);
				row.setTag(wrapper);
			} else
			{
				wrapper = (ViewWrapper) row.getTag();
			}

			RowModel model = getModel(position);
			wrapper.getLabel().setText(model.getLabel());
			wrapper.getDetails().setText(model.getDetails());

			return (row);
		}
	}

	private void showAlertDialog(String title, final String[] table, final int index)
	{
		AlertDialog.Builder ab = new AlertDialog.Builder(TrainSettingsActivity.this);
		ab.setTitle(title);
		temp=choosenPosition;

		ab.setSingleChoiceItems(table, choosenPosition, new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int whichButton)
			{
				temp = whichButton;
			}
		}).setPositiveButton(SettingsManager.AGREEMENT_TAG, new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int whichButton)
			{
				choosenPosition = temp;

				if (index == 1)
				{
					settingsManager.setActivity(table[choosenPosition], TrainSettingsActivity.this);
					settingsList.get(index).setDetails(settingsManager.getActivity());
				}

				else if (index == 2)
				{
					settingsManager.setUnits(table[choosenPosition], TrainSettingsActivity.this);
					settingsList.get(index).setDetails(settingsManager.getUnits());
					settingsList.get(0).setDetails(settingsManager.getUserDetails());
				}

				else if (index == 3)
				{
					settingsManager.setAutoPause(table[choosenPosition], TrainSettingsActivity.this);
					settingsList.get(index).setDetails(settingsManager.getAutoPause());
				}

				else if (index == 4)
				{
					settingsManager.setGPSRefreshRate(table[choosenPosition], TrainSettingsActivity.this);
					settingsList.get(index).setDetails(settingsManager.getGpsRefreshRate());
				}

				arrayAdapter.notifyDataSetChanged();
			}
		}).setNegativeButton(SettingsManager.CANCEL_TAG, new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int whichButton)
			{
			}
		});
		ab.show();
	}
}