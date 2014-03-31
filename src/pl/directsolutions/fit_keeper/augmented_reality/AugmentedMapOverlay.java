package pl.directsolutions.fit_keeper.augmented_reality;

import java.util.ArrayList;

import pl.directsolutions.fit_keeper.model.Place;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.location.Location;
import android.util.Log;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;

public class AugmentedMapOverlay extends Overlay
{
	//	private double destLatitude, destLongitude;
	private ArrayList<Place> placesList;

	public AugmentedMapOverlay(ArrayList<Place> places)
	{
		placesList = new ArrayList<Place>();
		placesList.addAll(places);
		Log.d("overlay size",placesList.size()+"");
	}

	public boolean isVisible(double latitude, double longitude, MapView mapView)
	{

		double minLongitude;
		double minLatitude;
		double maxLongitude;
		double maxLatitude;
		double vS = mapView.getLatitudeSpan();
		double hS = mapView.getLongitudeSpan();
		double cX = mapView.getMapCenter().getLongitudeE6();
		double cY = mapView.getMapCenter().getLatitudeE6();
		minLongitude = (cX - hS / 2) / 1E6;
		minLatitude = (cY - vS / 2) / 1E6;
		maxLongitude = (cX + hS / 2) / 1E6;
		maxLatitude = (cY + vS / 2) / 1E6;
		Log.d("centerlat,centerlong,latspan,longspan", cY + " " + cX + " " + vS + " " + hS);
		Log.d("lat,long", latitude + " " + longitude);
		Log.d("minlat, minlong,maxlat,maxlong", minLatitude + " " + minLongitude + " " + maxLatitude + " " + maxLongitude);

		if (longitude >= minLongitude && longitude <= maxLongitude && latitude >= minLatitude && latitude <= maxLatitude)
		{
			return true;
		}
		return false;
	}

	private final int mRadius = 5;
	Location location = null;

	public Location getLocation()
	{
		return location;
	}

	public void setLocation(Location location)
	{
		this.location = location;
	}

	@Override
	public boolean onTap(GeoPoint point, MapView mapView)
	{
		return false;
	}

	@Override
	public void draw(Canvas canvas, MapView mapView, boolean shadow)
	{
		Projection projection = mapView.getProjection();

		if (shadow == false && location != null)
		{
			// Get the current location    
			Double latitude = location.getLatitude() * 1E6;
			Double longitude = location.getLongitude() * 1E6;
			GeoPoint geoPoint;
			geoPoint = new GeoPoint(latitude.intValue(), longitude.intValue());

			// Convert the location to screen pixels     
			Point point = new Point();
			projection.toPixels(geoPoint, point);

			RectF oval = new RectF(point.x - mRadius, point.y - mRadius, point.x + mRadius, point.y + mRadius);

			// Setup the paint
			Paint paint = new Paint();
			paint.setARGB(250, 255, 255, 255);
			paint.setAntiAlias(true);
			paint.setFakeBoldText(true);

			Paint backPaint = new Paint();
			backPaint.setARGB(175, 50, 50, 50);
			backPaint.setAntiAlias(true);

			RectF backRect = new RectF(point.x + 2 + mRadius, point.y - 3 * mRadius, point.x + 65, point.y + mRadius);

			// Draw the marker    
			canvas.drawOval(oval, paint);
			canvas.drawRoundRect(backRect, 5, 5, backPaint);
			canvas.drawText("Here I Am", point.x + 2 * mRadius, point.y, paint);

			//**************************************************************************************************

			for (int i = 0; i < placesList.size(); i++)
			{
				// Get the current location    
				latitude = placesList.get(i).getLatitude() * 1E6;
				longitude = placesList.get(i).getLongitude() * 1E6;
				geoPoint = new GeoPoint(latitude.intValue(), longitude.intValue());

				// Convert the location to screen pixels     
				point = new Point();
				projection.toPixels(geoPoint, point);

				oval = new RectF(point.x - mRadius, point.y - mRadius, point.x + mRadius, point.y + mRadius);

				// Setup the paint
				paint = new Paint();
				paint.setARGB(250, 255, 255, 255);
				paint.setAntiAlias(true);
				paint.setFakeBoldText(true);

				backPaint = new Paint();
				backPaint.setARGB(175, 50, 50, 50);
				backPaint.setAntiAlias(true);

				backRect = new RectF(point.x + 2 + mRadius, point.y - 3 * mRadius, point.x + placesList.get(i).getDistanceString().length()*8, point.y + mRadius);

				// Draw the marker    
				canvas.drawOval(oval, paint);
				canvas.drawRoundRect(backRect, 5, 5, backPaint);
				canvas.drawText(placesList.get(i).getDistanceString(), point.x + 2 * mRadius, point.y, paint);
				Log.d("DRAW", "draw point");
			}
		}
		super.draw(canvas, mapView, shadow);
	}
}