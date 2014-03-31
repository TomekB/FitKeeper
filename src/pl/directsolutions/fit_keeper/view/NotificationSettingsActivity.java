package pl.directsolutions.fit_keeper.view;

import java.util.ArrayList;

import pl.directsolutions.fit_keeper.R;
import pl.directsolutions.fit_keeper.controller.SettingsManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class NotificationSettingsActivity extends Activity
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
		setContentView(R.layout.notification_settings);

		settingsManager = SettingsManager.getInstance(NotificationSettingsActivity.this);

		settingsList = new ArrayList<RowModel>();
		settingsListView = (ListView) this.findViewById(R.id.notificationsList);
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
				String[] table=null;

				if (index == 0)
				{
					table = SettingsManager.getNotificationTypeValuesTable();
					for (int i = 0; i < table.length; i++)
					{
						if (settingsManager.getNotificationType().equals(table[i]))
						{
							choosenPosition = i;
							break;
						}
					}
					showAlertDialog("Choose notification type:", table, index);
				}

				else if (index == 1)
				{
					table = settingsManager.getNotificationIntervalStringTable();

					for (int i = 0; i < table.length; i++)
					{
						if (settingsManager.getNotificationValue().equals(table[i]))
						{
							choosenPosition = i;
							break;
						}
					}
					showAlertDialog("Choose interval:", table, index);
				}
				
				else if (index == 2)
				{
					table = settingsManager.getNotificationIntervalStringTable();

					for (int i = 0; i < table.length; i++)
					{
						if (settingsManager.getNotificationInRaceValue().equals(table[i]))
						{
							choosenPosition = i;
							break;
						}
					}
					showAlertDialog("Choose interval:", table, index);
				}
				
				else if (index == 3)
				{
					settingsManager.changeShowSpeedInsteadOfTempo();
					arrayAdapter.notifyDataSetChanged();
					settingsList.get(index).setDetails(settingsManager.isShowSpeedInsteadOfTempo()+"");
				}
				
				else if (index == 4)
				{
					settingsManager.changeShowBurnedCalories();
					arrayAdapter.notifyDataSetChanged();
					settingsList.get(index).setDetails(settingsManager.isShowBurnedCalories()+"");
				}
				
				else if (index == 5)
				{
					settingsManager.changeVoiceNotification();
					arrayAdapter.notifyDataSetChanged();
					settingsList.get(index).setDetails(settingsManager.isVoiceNotification()+"");
				}

			}
		});

		settingsListView.setAdapter(arrayAdapter);
	}

	private void showAlertDialog(String title, final String[] table, final int index)
	{
		AlertDialog.Builder ab = new AlertDialog.Builder(NotificationSettingsActivity.this);
		ab.setTitle(title);
		temp = choosenPosition;

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

				if (index == 0)
				{
					settingsManager.setNotificationType(table[choosenPosition], NotificationSettingsActivity.this);

					settingsList.get(index).setDetails(settingsManager.getNotificationType());
					settingsList.get(index + 1).setDetails(settingsManager.getNotificationValue());
					settingsList.get(index + 2).setDetails(settingsManager.getNotificationInRaceValue());
				}

				else if (index == 1)
				{				
					settingsManager.setNotificationValue(table[choosenPosition], NotificationSettingsActivity.this);				
					settingsList.get(index).setDetails(settingsManager.getNotificationValue());
				}
				
				else if (index == 2)
				{				
					settingsManager.setNotificationInRaceValue(table[choosenPosition], NotificationSettingsActivity.this);				
					settingsList.get(index).setDetails(settingsManager.getNotificationInRaceValue());
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

	private void addElements(ArrayList<RowModel> list)
	{
		list.add(new RowModel(SettingsManager.getNotificationTypeTag(), settingsManager.getNotificationType()));
		list.add(new RowModel(SettingsManager.getNotificationIntervalTag(), settingsManager.getNotificationValue()));
		list.add(new RowModel(SettingsManager.getNotificationIntrevalInRaceTag(), settingsManager.getNotificationInRaceValue()));
		list.add(new RowModel(SettingsManager.getNotificationShowSpeedInsteadOfTempoTag(), settingsManager.isShowSpeedInsteadOfTempo() + ""));
		list.add(new RowModel(SettingsManager.getNotificationShowBurnedCaloriesTag(), settingsManager.isShowBurnedCalories() + ""));
		list.add(new RowModel(SettingsManager.getVoiceNotificationTag(), settingsManager.isVoiceNotification() + ""));
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

}
