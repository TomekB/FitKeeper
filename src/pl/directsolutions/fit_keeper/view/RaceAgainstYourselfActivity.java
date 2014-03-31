package pl.directsolutions.fit_keeper.view;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import pl.directsolutions.fit_keeper.R;
import pl.directsolutions.fit_keeper.controller.Formatter;
import pl.directsolutions.fit_keeper.controller.SettingsManager;
import pl.directsolutions.fit_keeper.controller.WorkoutManager;
import pl.directsolutions.fit_keeper.model.TripPoint;
import pl.directsolutions.fit_keeper.model.Workout;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class RaceAgainstYourselfActivity extends MapActivity
{
	public static final int CHANGED_STATUS_MESSAGE = 1;

	private MapController mapController;
	private MapView mapView;
	private LocationManager locMan;
	private Location newLocation, oldLocation;
	private LocationListener locationListener;
	private MapOverlay positionOverlay;

	private Button startButton, stopButton, saveButton;
	private TextView distanceTextView, timeTextView, speedTextView, caloriesTextView, differenceTextView, toFinishTextView, statusTextView;

	private WorkoutManager workoutManager;
	private SettingsManager settingsManager;

	private boolean fixStatus;
	private TripPoint tripPoint;
	private int displayHeight, displayWidth;

	private Workout competitiveWorkout;
	private ArrayList<TripPoint> workoutPointsList;
	private ArrayList<TripPoint> competitiveWorkoutPointsList;

	private int previousIndex = 0;
	private float distanceDifference;
	private boolean distanceAchieved, won;
	private String distanceString;
	private String differenceString;
	private Double burnedCalories;
	private float lastWorkoutDistance;
	private long lastWorkoutTime;
	private int weight;
	private boolean isNotificationInTime;
	private double notificationInterval;
	private int notificationsCounter;
	private MediaPlayer mp;

	private boolean firstTimeGPSAnimation = true;
	private boolean workoutActive = false;

	Handler handler = new Handler()
	{
		@Override
		public void handleMessage(Message msg)
		{
			if (msg.what == CHANGED_STATUS_MESSAGE)
			{
				float speed = newLocation.getSpeed(); //speed in m/s
				String rapidity, status;
				float distance = workoutManager.getWorkoutDistance();

				distanceString = Formatter.formatDistance(distance, settingsManager.getUnits());

				countRace();
				if (distanceDifference > 0)
				{
					differenceString = "advantage ";
				} else if (distanceDifference <= 0)
				{
					differenceString = "loss ";
					distanceDifference *= -1;
				}
				differenceString += Formatter.formatDistance(distanceDifference, settingsManager.getUnits());
				rapidity = Formatter.formatRapidity(speed, settingsManager.isShowSpeedInsteadOfTempo(), settingsManager.getUnits());

				countBurnedCalories();
				showNotification();

				status = "";
				if (distanceAchieved)
				{
					status = "loose";
					if (won)
					{
						status = "WIN";
					}
				}

				distanceTextView.setText(distanceString);
				speedTextView.setText(rapidity);
				timeTextView.setText(Formatter.formatTime(workoutManager.getWorkoutTimeMillis()));
				if (settingsManager.isShowBurnedCalories())
				{
					caloriesTextView.setText("Calories: " + (new Double(burnedCalories)).intValue());// + workoutManager.getActualWorkout().getBurnedCalories());
				}
				differenceTextView.setText(differenceString); //change in proper units
				if (!distanceAchieved)
				{
					toFinishTextView.setText("To finish: "
							+ Formatter.formatDistance(workoutManager.getCompetitiveWorkout().getDistance() - workoutManager.getWorkoutDistance(),
									settingsManager.getUnits()));
				}
				if (status.equals(""))
				{
					statusTextView.setVisibility(View.GONE);
				} else
				{
					statusTextView.setVisibility(View.VISIBLE);
					statusTextView.setText(status);
				}

			}
		}
	};

	@Override
	public boolean isRouteDisplayed()
	{
		return false;
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.train_competitive_workout);

		workoutManager = WorkoutManager.getInstance(this);
		settingsManager = SettingsManager.getInstance(this);

		distanceTextView = (TextView) findViewById(R.id.NewDistanceTextView);
		timeTextView = (TextView) findViewById(R.id.NewTimeTextView);
		speedTextView = (TextView) findViewById(R.id.NewRapidityTextView);
		caloriesTextView = (TextView) findViewById(R.id.NewCaloriesTextView);
		differenceTextView = (TextView) findViewById(R.id.NewDifferenceTextView);
		toFinishTextView = (TextView) findViewById(R.id.NewToFinishTextView);
		statusTextView = (TextView) findViewById(R.id.NewStatusTextView);
		distanceTextView.setTextColor(Color.rgb(255, 255, 255));
		distanceTextView.setTextSize(20);
		timeTextView.setTextColor(Color.rgb(255, 255, 255));
		timeTextView.setTextSize(20);
		speedTextView.setTextColor(Color.rgb(255, 255, 255));
		speedTextView.setTextSize(20);
		caloriesTextView.setTextColor(Color.rgb(255, 255, 255));
		caloriesTextView.setTextSize(20);
		differenceTextView.setTextColor(Color.rgb(255, 255, 255));
		differenceTextView.setTextSize(20);
		toFinishTextView.setTextColor(Color.rgb(255, 255, 255));
		toFinishTextView.setTextSize(20);
		statusTextView.setTextColor(Color.rgb(255, 255, 255));
		statusTextView.setTextSize(20);

		weight = settingsManager.getWeightInKG();
		setNotificationInterval();
		mp = MediaPlayer.create(this, R.raw.jingle);

		competitiveWorkoutPointsList = new ArrayList<TripPoint>();
		competitiveWorkoutPointsList.addAll(workoutManager.getCompetitiveWorkout().getAllWorkoutPoints());

		startButton = (Button) findViewById(R.id.TrainNewNewWorkoutButton1);
		startButton.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				if (fixStatus && !workoutActive)
				{
					startJog();
				}
			}
		});

		stopButton = (Button) findViewById(R.id.TrainNewNewWorkoutButton2);
		stopButton.setEnabled(false);
		stopButton.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				if (workoutActive)
				{
					startButton.setEnabled(true);
					stopJog();
				}
			}
		});

		saveButton = (Button) findViewById(R.id.TrainNewNewWorkoutButton3);
		saveButton.setEnabled(false);
		saveButton.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				if (!workoutActive)
				{
					int filenumber = -1;
					String workoutPointsFileName = "";

					while (true)
					{
						filenumber++;
						workoutPointsFileName = "joglog_" + filenumber + ".txt";
						try
						{
							openFileInput(workoutPointsFileName);
						} catch (FileNotFoundException e)
						{
							break;
						}
					}

					try
					{
						FileOutputStream fos = openFileOutput(workoutPointsFileName, Context.MODE_PRIVATE);
						workoutManager.setBurnedCalories(burnedCalories.intValue());
						workoutManager.saveWorkout(fos, workoutPointsFileName);
						saveButton.setEnabled(false);
					} catch (FileNotFoundException e)
					{
						e.printStackTrace();
					}
				}
			}
		});

		workoutActive = false;

		mapView = (MapView) findViewById(R.id.myMapView);
		mapController = mapView.getController();
		mapController.setZoom(19); // scale from 1 to 21 (21 is max)
		mapView.setSatellite(false);
		mapView.setStreetView(false);
		mapView.displayZoomControls(true);
		mapView.setFocusable(true);
		mapView.setBuiltInZoomControls(true);

		displayHeight = getWindowManager().getDefaultDisplay().getHeight();
		displayWidth = getWindowManager().getDefaultDisplay().getWidth();
		positionOverlay = new MapOverlay(displayHeight, displayWidth, this);

		List<Overlay> overlays = mapView.getOverlays();
		overlays.add(positionOverlay);

		locMan = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		String provider = LocationManager.GPS_PROVIDER;
		oldLocation = locMan.getLastKnownLocation(provider);
		newLocation = locMan.getLastKnownLocation(provider);
		updateMap(newLocation);

		locationListener = new LocationListener()
		{
			public void onLocationChanged(Location location)
			{
				if (fixStatus)
				{
					newLocation = location;

					if (firstTimeGPSAnimation || workoutActive)
					{
						updateMap(newLocation);
						firstTimeGPSAnimation = false;
					}

					if (workoutActive)
					{
						updateData();
					}
				}
			}

			public void onProviderDisabled(String provider)
			{
			}

			public void onProviderEnabled(String provider)
			{
			}

			public void onStatusChanged(String provider, int status, Bundle extras)
			{
				switch (status)
				{
				case LocationProvider.OUT_OF_SERVICE:
					if (oldLocation == null || oldLocation.getProvider().equals(provider))
					{
						fixStatus = false;
						oldLocation = null;
					}
					break;
				case LocationProvider.TEMPORARILY_UNAVAILABLE:
					if (oldLocation == null || oldLocation.getProvider().equals(provider))
					{
						fixStatus = false;
					}
					break;
				case LocationProvider.AVAILABLE:
					fixStatus = true;
					break;
				}
			}
		};

		GpsStatus.Listener listener = new GpsStatus.Listener()
		{
			public void onGpsStatusChanged(int event)
			{
				if (event == GpsStatus.GPS_EVENT_FIRST_FIX)
				{
					Log.d("FIX", "FIX");
					fixStatus = true;
				}
			}
		};

		long gpsRefreshRate = 0;
		for (int i = 0; i < SettingsManager.gpsRefreshRateStringsTable.length; i++)
		{
			if (SettingsManager.gpsRefreshRateStringsTable[i].equals(settingsManager.getGpsRefreshRate()))
			{
				gpsRefreshRate = SettingsManager.gpsRefreshRateInMillisTable[i];
				break;
			}
		}
		locMan.requestLocationUpdates(provider, gpsRefreshRate, 0, locationListener);
		locMan.addGpsStatusListener(listener);

		if (!locMan.isProviderEnabled(provider))
		{
			showGPSDialog();
		}
	}

	@Override
	public void onStart()
	{
		super.onStart();
		workoutManager.reset();

		Thread myThread = new Thread(new Runnable()
		{
			public void run()
			{
				while (true)
				{
					try
					{
						Thread.sleep(200);
						workoutManager.updateTime(System.currentTimeMillis());

						if (workoutActive)
						{
							handler.sendMessage(handler.obtainMessage(CHANGED_STATUS_MESSAGE));
						}
					} catch (Throwable t)
					{
					}
				}
			}
		});
		myThread.start();
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
		workoutManager.clear();
	}

	private void updateMap(Location location)
	{
		if (location != null)
		{
			Double geoLat = location.getLatitude() * 1E6;
			Double geoLng = location.getLongitude() * 1E6;
			GeoPoint point = new GeoPoint(geoLat.intValue(), geoLng.intValue());
			mapController.animateTo(point);
		}
	}

	private void startJog()
	{
		workoutManager.startWorkout(System.currentTimeMillis());
		workoutActive = true;
		previousIndex = 0;
		positionOverlay.reset();
		burnedCalories = 0.0;
		lastWorkoutDistance = 0;
		lastWorkoutTime = 0;
		notificationsCounter = 0;
		distanceAchieved = won = false;

		stopButton.setEnabled(true);
		startButton.setEnabled(false);
		stopButton.setText(R.string.pauseJog);
		workoutPointsList = new ArrayList<TripPoint>();
	}

	private void stopJog()
	{
		workoutManager.stopWorkout(System.currentTimeMillis());
		workoutActive = false;

		stopButton.setEnabled(false);
		startButton.setText(R.string.startJog);
		saveButton.setEnabled(true);
	}

	private synchronized void updateData()
	{
		if (workoutActive == true)
		{
			tripPoint = new TripPoint(newLocation.getLongitude(), newLocation.getLatitude(), (System.currentTimeMillis() - workoutManager.getActualWorkout()
					.getStartTimeMillis()));
			workoutManager.addTripPoint(tripPoint);
			workoutPointsList.add(tripPoint);
		}
		oldLocation = newLocation;
	}

	private void countBurnedCalories()
	{
		//running
		if (settingsManager.getActivity().equals(SettingsManager.activityValuesTable[0]))
		{
			Float actualDistance = workoutManager.getWorkoutDistance() - lastWorkoutDistance;

			if (actualDistance > 0)
			{
				Double time = new Long(workoutManager.getWorkoutTimeMillis() - lastWorkoutTime).doubleValue();
				time /= 1000; //in seconds

				Double actualSpeed = actualDistance / time;

				int index = 0;
				double temp;

				int i = 0;
				while (i < WorkoutManager.runningMetSpeeds.length)
				{
					temp = WorkoutManager.runningMetSpeeds[i];

					if (actualSpeed <= temp && i == 0)
					{
						index = i;
						break;
					}

					else if (actualSpeed >= temp && i == WorkoutManager.runningMetSpeeds.length - 1)
					{
						index = i;
						break;
					}

					else if (actualSpeed >= temp && actualSpeed <= WorkoutManager.runningMetSpeeds[i + 1])
					{
						if ((actualSpeed - temp) > WorkoutManager.runningMetSpeeds[i + 1] - actualSpeed)
						{
							index = i + 1;
						} else
						{
							index = i;
						}
						break;
					}

					i++;
				}
				Double MET = WorkoutManager.runningMetValues[index];
				burnedCalories += MET * settingsManager.getBMR() * (time / (60 * 60 * 24));
				Log.d("MET BMR TIME", MET + " " + settingsManager.getBMR() + " " + time);

				lastWorkoutDistance = workoutManager.getWorkoutDistance();
				lastWorkoutTime = workoutManager.getWorkoutTimeMillis();
			}
		}
	}

	private void countRace()
	{
		long actualWorkoutTime = workoutManager.getWorkoutTimeMillis();
		float actualDistance = workoutManager.getWorkoutDistance();
		TripPoint earlierPoint = null, laterPoint = null, temp = null;
		int earlierIndex = -1, laterIndex = -1;
		float competitiveDistance = 0;
		boolean count = false;

		for (int i = previousIndex; i < competitiveWorkoutPointsList.size(); i++)
		{
			temp = competitiveWorkoutPointsList.get(i);

			if (temp.getTime() < actualWorkoutTime)
			{
				earlierPoint = temp;
				earlierIndex = i;
				previousIndex = i;
			} else
			{
				laterPoint = temp;
				laterIndex = i;
				break;
			}
			count = true;
		}

		if (count)
		{
			/*
			 * 3 situations: 
			 * a) only later (later index = 0) 
			 * b) earlier and later were found 
			 * c) only earlier (earlier index is last element)
			 */

			if (laterIndex == 0)
			{
				competitiveDistance = (workoutManager.getWorkoutTimeMillis() / laterPoint.getTime()) * laterPoint.getDistance();
				distanceDifference = actualDistance - competitiveDistance;
			}

			else if (laterIndex > 0)
			{
				long timeToEarlier, timeToLater;
				timeToEarlier = workoutManager.getWorkoutTimeMillis() - earlierPoint.getTime();
				timeToLater = laterPoint.getTime() - workoutManager.getWorkoutTimeMillis();

				long differenceInTime = laterPoint.getTime() - earlierPoint.getTime();
				float differenceInDistance = laterPoint.getDistance() - earlierPoint.getDistance();

				if (timeToEarlier > timeToLater)
				{
					competitiveDistance = laterPoint.getDistance()
							- (((laterPoint.getTime() - workoutManager.getWorkoutTimeMillis()) / differenceInTime) * differenceInDistance);
				}

				else
				{
					competitiveDistance = earlierPoint.getDistance()
							+ (((workoutManager.getWorkoutTimeMillis() - earlierPoint.getTime()) / differenceInTime) * differenceInDistance);
					//				distanceAchieved = true;
				}
				distanceDifference = actualDistance - competitiveDistance;
			}

			else if (earlierIndex == competitiveWorkoutPointsList.size() - 1)
			{
				competitiveDistance = earlierPoint.getDistance();
				distanceDifference = actualDistance - competitiveDistance;
			}

		}

		if (actualDistance > workoutManager.getCompetitiveWorkout().getDistance() || actualWorkoutTime > workoutManager.getCompetitiveWorkout().getWorkoutTime())
		{
			Log.d("DISTANCE", "DISTANCE");
			distanceAchieved = true;

			if (actualWorkoutTime < workoutManager.getCompetitiveWorkout().getWorkoutTime())
			{
				won = true;
			}
		}
	}

	private void showGPSDialog()
	{

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Application needs GPS, do You want to enable GPS now?").setCancelable(false).setPositiveButton("Yes", new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int id)
			{
				startActivityForResult(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS), 0);
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

	private void showNotification()
	{
		int duration = Toast.LENGTH_SHORT;

		if (isNotificationInTime)
		{
			if (notificationInterval < (new Double(workoutManager.getWorkoutTimeMillis()) / 1000 - notificationsCounter * notificationInterval))
			{
				Toast toast = Toast.makeText(RaceAgainstYourselfActivity.this, Formatter.formatTime(workoutManager.getWorkoutTimeMillis()), duration);
				toast.show();
				notificationsCounter++;
				if (settingsManager.isVoiceNotification())
				{
					mp.start();
				}
			}
		} else
		{
			if (settingsManager.getUnits().equals(SettingsManager.US_UNITS))
			{
				if (notificationInterval < (new Double(workoutManager.getWorkoutDistance()) / 0.914 - notificationsCounter * notificationInterval))
				{
					Toast toast = Toast.makeText(RaceAgainstYourselfActivity.this, distanceString, duration);
					toast.show();
					notificationsCounter++;
				}
			} else
			{
				if (notificationInterval < workoutManager.getWorkoutDistance() - notificationsCounter * notificationInterval)
				{
					Toast toast = Toast.makeText(RaceAgainstYourselfActivity.this, distanceString, duration);
					toast.show();
					notificationsCounter++;
				}
			}
		}
	}

	private void setNotificationInterval()
	{
		if (settingsManager.getNotificationType().equals(SettingsManager.notificationTypeValuesTable[0]))
		{
			isNotificationInTime = true;

			for (int i = 0; i < SettingsManager.notificationTimesStringTable.length; i++)
			{
				if (SettingsManager.notificationTimesStringTable[i].equals(settingsManager.getNotificationInRaceValue()))
				{
					notificationInterval = SettingsManager.notificationTimesValueTable[i];
					break;
				}
			}
		} else
		{
			isNotificationInTime = false;

			if (settingsManager.getUnits().equals(SettingsManager.US_UNITS))
			{
				for (int i = 0; i < SettingsManager.notificationDistancesInMilesStringTable.length; i++)
				{
					if (SettingsManager.notificationDistancesInMilesStringTable[i].equals(settingsManager.getNotificationInRaceValue()))
					{
						notificationInterval = SettingsManager.notificationDistancesInYardsValueTable[i];
						break;
					}
				}
			} else
			{
				for (int i = 0; i < SettingsManager.notificationDistancesInKilometersStringTable.length; i++)
				{
					if (SettingsManager.notificationDistancesInKilometersStringTable[i].equals(settingsManager.getNotificationInRaceValue()))
					{
						notificationInterval = SettingsManager.notificationDistancesInMetersValueTable[i];
						break;
					}
				}
			}
		}
	}
}
