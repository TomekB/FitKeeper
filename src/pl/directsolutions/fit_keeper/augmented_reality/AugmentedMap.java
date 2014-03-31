package pl.directsolutions.fit_keeper.augmented_reality;

import java.util.ArrayList;
import java.util.List;

import pl.directsolutions.fit_keeper.R;
import pl.directsolutions.fit_keeper.model.Place;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

public class AugmentedMap extends MapActivity
{
	//	public static double latitude, longitude;
	private static ArrayList<Place> placesList;
	private Button backButton;
	private MapController mapController;
	private MapView mapView;
	private LocationManager locationManager;
	private AugmentedMapOverlay mapOverlay;
	private int zoom = 18;
	boolean firstShow = true;

	public static void setPlaces(ArrayList<Place> places)
	{
		placesList = new ArrayList<Place>();
		placesList.addAll(places);
		Log.d("mapview size",placesList.size()+"");
	}

	@Override
	public void onCreate(Bundle bundle)
	{
		super.onCreate(bundle);
		setContentView(R.layout.augmented_map);
		firstShow = true;
		backButton = (Button) findViewById(R.id.augmented_map_button);
		backButton.setText("   BACK   ");
		backButton.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				finish();
			}
		});

		mapView = (MapView) findViewById(R.id.augmented_map_mapView);
		mapController = mapView.getController();

		mapView.setSatellite(false);
		mapView.setStreetView(false);
		mapView.displayZoomControls(true);
		mapController.setZoom(zoom);
		mapView.setFocusable(true);
		mapView.setBuiltInZoomControls(true);

		mapOverlay = new AugmentedMapOverlay(placesList);
		List<Overlay> overlays = mapView.getOverlays();
		overlays.add(mapOverlay);

		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		String provider = LocationManager.GPS_PROVIDER;
		updateWithNewLocation(locationManager.getLastKnownLocation(provider));

		locationManager.requestLocationUpdates(provider, 1000, 1, locationListener);
	}

	private final LocationListener locationListener = new LocationListener()
	{
		public void onLocationChanged(Location location)
		{
			updateWithNewLocation(location);
		}

		public void onProviderDisabled(String provider)
		{
		}

		public void onProviderEnabled(String provider)
		{
		}

		public void onStatusChanged(String provider, int status, Bundle extras)
		{
		}
	};

	private synchronized void updateWithNewLocation(Location location)
	{
		if (location != null)
		{
			Log.d("SET OVERLAY", location+"");
			mapOverlay.setLocation(location);
			Double geoLat = location.getLatitude() * 1E6;
			Double geoLng = location.getLongitude() * 1E6;
			GeoPoint point = new GeoPoint(geoLat.intValue(), geoLng.intValue());
			mapController.animateTo(point);

//			if (firstShow)
//			{
//				int latSpanE6 = (int) (latitude * 1E6 - geoLat);
//				int lonSpanE6 = (int) (longitude * 1E6 - geoLng);
//				if (latSpanE6 < 0)
//					latSpanE6 *= -1;
//				if (lonSpanE6 < 0)
//					lonSpanE6 *= -1;
//
//				//				Log.d("lat,long", latitude * 1E6 + " " + longitude * 1E6 + " " + latSpanE6 + " " + lonSpanE6);
//				mapController.zoomToSpan(latSpanE6, lonSpanE6);
//				firstShow = false;
//			}
		} else
		{
		}
	}

	@Override
	protected boolean isRouteDisplayed()
	{
		return false;
	}
}
