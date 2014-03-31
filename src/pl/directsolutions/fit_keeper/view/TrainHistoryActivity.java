package pl.directsolutions.fit_keeper.view;

import java.io.FileNotFoundException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import pl.directsolutions.fit_keeper.R;
import pl.directsolutions.fit_keeper.augmented_reality.MixView;
import pl.directsolutions.fit_keeper.controller.Formatter;
import pl.directsolutions.fit_keeper.controller.SettingsManager;
import pl.directsolutions.fit_keeper.controller.WorkoutManager;
import pl.directsolutions.fit_keeper.model.Place;
import pl.directsolutions.fit_keeper.model.Workout;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class TrainHistoryActivity extends Activity
{
	private ListView workoutListView;
	private TextView textView;
	private Button button;
	private ArrayAdapter<RowModel> arrayAdapter;
	private ArrayList<RowModel> workoutsList;
	private ArrayList<Place> placesList;
	private SettingsManager settingsManager;
	private WorkoutManager workoutManager;
	private float wholeDistance;
	private long wholeTime;

	@Override
	public void onCreate(Bundle icicle)
	{
		super.onCreate(icicle);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.train_history);

		textView = (TextView) findViewById(R.id.TrainHistoryTextView);
		workoutListView = (ListView) this.findViewById(R.id.trainHistoryList);
		button = (Button) findViewById(R.id.TrainHistoryButton);

		workoutManager = WorkoutManager.getInstance(TrainHistoryActivity.this);
		settingsManager = SettingsManager.getInstance(TrainHistoryActivity.this);
	}

	@Override
	public void onResume()
	{
		super.onResume();

		wholeDistance = 0;
		wholeTime = 0;
		workoutsList = new ArrayList<RowModel>();
		prepareWorkoutsList(workoutListView, workoutsList);
		setHeader();

		button.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				Intent intent = new Intent(TrainHistoryActivity.this, MixView.class);
				MixView.addPlaces(placesList);
				startActivity(intent);
			}
		});
	}

	private void setHeader()
	{
		String distanceString = "";
		DecimalFormat df = new DecimalFormat("##.##");

		if (settingsManager.getUnits().equals(SettingsManager.US_UNITS))
		{
			if (wholeDistance < 1609)
			{
				distanceString = new Float(wholeDistance * 0.9144).intValue() + " yards";
			} else
			{
				wholeDistance /= 0.9144; //in yards
				wholeDistance /= 1760; //in miles
				distanceString = df.format(wholeDistance) + " mi";
			}
		} else
		{
			if (wholeDistance < 1000)
			{
				distanceString = new Float(wholeDistance).intValue() + " meters";
			} else
			{
				wholeDistance /= 1000;
				distanceString = df.format(wholeDistance) + " km";
			}
		}

		textView.setText("Total workout time: " + Formatter.formatTime(wholeTime) + "\nTotal distance: " + distanceString);
	}

	private void prepareWorkoutsList(ListView listView, ArrayList<RowModel> list)
	{
		addElements(list);

		arrayAdapter = new RowAdapter(this, workoutsList);

		listView.setOnItemClickListener(new OnItemClickListener()
		{
			public void onItemClick(AdapterView<?> adapterView, View view, int index, long arg3)
			{
				workoutManager.setActualWorkoutIndex(index);
				try
				{
					workoutManager.getActualWorkout().readPointsFromFile(openFileInput(workoutManager.getActualWorkout().getWorkoutPointsFilename()));
				} catch (FileNotFoundException e)
				{
				}
				Intent intent = new Intent(TrainHistoryActivity.this, HistoricalWorkoutActivity.class);
				startActivity(intent);
			}
		});

		listView.setOnItemLongClickListener(new OnItemLongClickListener()
		{

			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2, long arg3)
			{
				showAlertDialog(arg2);
				return true;
			}

		});

		workoutListView.setAdapter(arrayAdapter);
	}

	private void addElements(ArrayList<RowModel> list)
	{
		placesList = new ArrayList<Place>();
		ArrayList<Workout> workouts = workoutManager.getAllWorkouts();
		Workout workout;

		for (int i = 0; i < workouts.size(); i++)
		{
			workout = workouts.get(i);
			float distance = workout.getDistance();
			String distanceString = "";
			DecimalFormat df = new DecimalFormat("##.##");
			float speed = workout.getDistance() / (new Float(workout.getWorkoutTime()) / 1000);
			String rapidity;

			if (settingsManager.getUnits().equals(SettingsManager.US_UNITS))
			{
				if (distance < 1609)
				{
					distanceString = new Float(distance * 0.9144).intValue() + " yards";
				} else
				{
					distance /= 0.9144; //in yards
					distance /= 1760; //in miles
					distanceString = df.format(distance) + " mi";
				}
			} else
			{
				if (distance < 1000)
				{
					distanceString = new Float(distance).intValue() + " meters";
				} else
				{
					distance /= 1000;
					distanceString = df.format(distance) + " km";
				}
			}

			//speed or pace in proper units
			if (settingsManager.isShowSpeedInsteadOfTempo())
			{
				if (settingsManager.getUnits().equals(SettingsManager.US_UNITS))
				{
					speed *= 2.23704; // USA units mph
					rapidity = df.format(speed) + " mph";
				} else

				{
					speed *= 3.6; //EU units km/h
					rapidity = df.format(speed) + " km/h";
				}
			} else
			{
				float pace;
				if (settingsManager.getUnits().equals(SettingsManager.US_UNITS))
				{
					speed *= 2.23704;
					pace = 60 / speed; // USA units min/mi
					rapidity = df.format(pace) + " min/mi";
				} else

				{
					speed *= 3.6;
					pace = 60 / speed; //EU units min/km
					rapidity = df.format(pace) + " min/km";
				}
			}

			try
			{
				workout.readStartPointFromFile(openFileInput(workout.getWorkoutPointsFilename()));
				Log.d("READED WORKOUT", workout.getStartPoint().getLatitude() + " " + workout.getStartPoint().getLongitude());
				if (workout.getStartPoint() != null)
				{
					double latitude = workout.getStartPoint().getLatitude();
					double longitude = workout.getStartPoint().getLongitude();
					placesList.add(new Place(distanceString + " " + new SimpleDateFormat("dd.MM.yyyy").format(workout.getStartDate().getTime()) + " Distance: ", latitude, longitude,
							workout.getStartDate(), distanceString));
				}
			} catch (FileNotFoundException e)
			{
				e.printStackTrace();
			}
			list.add(new RowModel(distanceString + " | " + Formatter.formatTime(workout.getWorkoutTime()), new SimpleDateFormat("dd.MM.yyyy  HH:mm:ss").format(workout
					.getStartDate().getTime()) + "   " + rapidity));
			wholeDistance += workout.getDistance();
			wholeTime += workout.getWorkoutTime();
		}
	}

	public RowModel getModel(int position)
	{
		return (((RowAdapter) workoutListView.getAdapter()).getItem(position));
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

	private void showAlertDialog(final int index)
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Are you sure you want to delete?").setCancelable(false).setPositiveButton("Yes", new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int id)
			{
				deleteWorkout(index);
			}
		}).setNegativeButton("No", new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int id)
			{
				dialog.cancel();
			}
		});
		AlertDialog alert = builder.create();
		alert.show();
	}

	private void deleteWorkout(final int index)
	{
		workoutsList.remove(index);
		workoutManager.removeWorkout(index);
		arrayAdapter.notifyDataSetChanged();

		ArrayList<Workout> workouts = workoutManager.getAllWorkouts();
		wholeDistance = 0;
		wholeTime = 0;
		for (int i = 0; i < workouts.size(); i++)
		{
			wholeDistance += workouts.get(i).getDistance();
			wholeTime += workouts.get(i).getWorkoutTime();
		}
		setHeader();
	}
}