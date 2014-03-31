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

public class UserSettingsActivity extends Activity
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
		setContentView(R.layout.user_settings);

		settingsManager = SettingsManager.getInstance(UserSettingsActivity.this);

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
					table = settingsManager.getAgeValuesTable();
					for (int i = 0; i < table.length; i++)
					{
						if (settingsManager.getAge() == Integer.parseInt(table[i]))
						{
							choosenPosition = i;
							break;
						}
					}
					showAlertDialog(SettingsManager.AGE_PROMPT, settingsManager.getAgeValuesTable(), index);
				}

				else if (index == 1)
				{
					table = settingsManager.getWeightValuesTable();
					for (int i = 0; i < table.length; i++)
					{
						if (settingsManager.getWeight() == Integer.parseInt(table[i]))
						{
							choosenPosition = i;
							break;
						}
					}
					showAlertDialog(SettingsManager.WEIGHT_PROMPT, settingsManager.getWeightValuesTable(), index);
				}

				else if (index == 3)
				{
					table = settingsManager.getSexValuesTable();
					for (int i = 0; i < table.length; i++)
					{
						if (settingsManager.getSex().equals(table[i]))
						{
							choosenPosition = i;
							break;
						}
					}
					showAlertDialog(SettingsManager.SEX_PROMPT, settingsManager.getSexValuesTable(), index);
				}
				
				else if (index == 2)
				{
					table = settingsManager.getHeightStringTable();
					for (int i = 0; i < table.length; i++)
					{
						if (settingsManager.getHeightString().equals(table[i]))
						{
							choosenPosition = i;
							break;
						}
					}
					showAlertDialog(SettingsManager.HEIGHT_PROMPT, settingsManager.getHeightStringTable(), index);
				}

			}
		});

		settingsListView.setAdapter(arrayAdapter);
	}

	private void showAlertDialog(String title, final String[] table, final int index)
	{
		AlertDialog.Builder ab = new AlertDialog.Builder(UserSettingsActivity.this);
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
					settingsManager.setAge(Integer.parseInt(table[choosenPosition]), UserSettingsActivity.this);
					settingsList.get(index).setDetails(Integer.toString(settingsManager.getAge()));
				}

				else if (index == 1)
				{
					settingsManager.setWeight(Integer.parseInt(table[choosenPosition]), UserSettingsActivity.this);
					settingsList.get(index).setDetails(settingsManager.getWeight() + " " + settingsManager.getWeightUnit());
				}

				else if (index == 3)
				{
					settingsManager.setSex(table[choosenPosition], UserSettingsActivity.this);
					settingsList.get(index).setDetails(settingsManager.getSex());
				}
				
				else if (index == 2)
				{
					settingsManager.setHeight(choosenPosition, UserSettingsActivity.this);
					settingsList.get(index).setDetails(settingsManager.getHeightString());
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
		list.add(new RowModel(SettingsManager.getUserAgeTag(), Integer.toString(settingsManager.getAge()) + " years"));
		list.add(new RowModel(SettingsManager.getUserWeightTag(), Integer.toString(settingsManager.getWeight()) + " " + settingsManager.getWeightUnit()));
		list.add(new RowModel(SettingsManager.getUserHeightTag(), settingsManager.getHeightString()));
		list.add(new RowModel(SettingsManager.getUserSexTag(), settingsManager.getSex()));
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
