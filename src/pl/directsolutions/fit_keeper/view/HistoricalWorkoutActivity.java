package pl.directsolutions.fit_keeper.view;

import java.util.List;

import pl.directsolutions.fit_keeper.R;
import pl.directsolutions.fit_keeper.controller.Formatter;
import pl.directsolutions.fit_keeper.controller.SettingsManager;
import pl.directsolutions.fit_keeper.controller.WorkoutManager;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Window;
import android.widget.TextView;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

public class HistoricalWorkoutActivity extends MapActivity
{
	private MapController mapController;
	private MapView mapView;
	private MapOverlay positionOverlay;

	private TextView distanceTextView, timeTextView, speedTextView, caloriesTextView;
	private WorkoutManager workoutManager;
	private SettingsManager settingsManager;
	private int displayHeight, displayWidth;
	private String distanceString;

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
		setContentView(R.layout.train_historical_workout);

		workoutManager = WorkoutManager.getInstance(HistoricalWorkoutActivity.this);
		settingsManager = SettingsManager.getInstance(HistoricalWorkoutActivity.this);
		distanceTextView = (TextView) findViewById(R.id.HistoricalDistanceTextView);
		timeTextView = (TextView) findViewById(R.id.HistoricalTimeTextView);
		speedTextView = (TextView) findViewById(R.id.HistoricalRapidityTextView);
		caloriesTextView = (TextView) findViewById(R.id.HistoricalCaloriesTextView);

		float speed = workoutManager.getWorkoutDistance() * 1000 / workoutManager.getWorkoutTimeMillis(); //speed in m/s
		String rapidity;
		float distance = workoutManager.getWorkoutDistance();

		distanceString = Formatter.formatDistance(distance, settingsManager.getUnits());
		rapidity = Formatter.formatRapidity(speed, settingsManager.isShowSpeedInsteadOfTempo(), settingsManager.getUnits());

		distanceTextView.setTextColor(Color.rgb(255, 255, 255));
		distanceTextView.setTextSize(20);
		timeTextView.setTextColor(Color.rgb(255, 255, 255));
		timeTextView.setTextSize(20);
		speedTextView.setTextColor(Color.rgb(255, 255, 255));
		speedTextView.setTextSize(20);
		caloriesTextView.setTextColor(Color.rgb(255, 255, 255));
		caloriesTextView.setTextSize(20);

		distanceTextView.setText(distanceString);
		speedTextView.setText(rapidity);
		timeTextView.setText(Formatter.formatTime(workoutManager.getWorkoutTimeMillis()));
		if (settingsManager.isShowBurnedCalories())
		{
			caloriesTextView.setText("Calories: " + workoutManager.getActualWorkout().getBurnedCalories());
		}

		mapView = (MapView) findViewById(R.id.myMapView);
		mapController = mapView.getController();
		mapController.setZoom(21);
		mapView.setSatellite(false);
		mapView.setStreetView(false);
		mapView.displayZoomControls(true);
		mapView.setFocusable(true);
		mapView.setBuiltInZoomControls(true);

		displayHeight = getWindowManager().getDefaultDisplay().getHeight();
		displayWidth = getWindowManager().getDefaultDisplay().getWidth();
		positionOverlay = new MapOverlay(displayHeight, displayWidth, HistoricalWorkoutActivity.this);

		List<Overlay> overlays = mapView.getOverlays();
		overlays.add(positionOverlay);

		Double geoLat = workoutManager.getAllWorkoutPoints().get(0).getLatitude() * 1E6;
		Double geoLng = workoutManager.getAllWorkoutPoints().get(0).getLongitude() * 1E6;
		GeoPoint point = new GeoPoint(geoLat.intValue(), geoLng.intValue());
		mapController.animateTo(point);
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
		workoutManager.clear();
	}
}
