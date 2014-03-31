package pl.directsolutions.fit_keeper.view;

import java.util.ArrayList;

import pl.directsolutions.fit_keeper.controller.WorkoutManager;
import pl.directsolutions.fit_keeper.model.TripPoint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.RectF;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;

public class MapOverlay extends Overlay
{
	private final int mRadius = 5;
	private Paint startPointPaint, stopPointPaint, linePaint;
	private Paint competitiveStartPointPaint, competitiveStopPointPaint, competitiveLinePaint;
	private Point newPoint;
	private int zoomLevel;
	private GeoPoint geoPoint;
	private int increment;

	private Path path;
	private Projection projection;
	private double minLongitude;
	private double minLatitude;
	private double maxLongitude;
	private double maxLatitude;

	private WorkoutManager workoutManager;
	private ArrayList<TripPoint> workoutPointsList;
	private ArrayList<TripPoint> competitiveWorkoutPointsList;

	public void reset()
	{
		workoutPointsList = new ArrayList<TripPoint>();
	}

	public MapOverlay(int displayH, int displayW, Context context)
	{
		super();
		workoutManager = WorkoutManager.getInstance(context);
		workoutPointsList = new ArrayList<TripPoint>();
		
		competitiveWorkoutPointsList = new ArrayList<TripPoint>();
		if (workoutManager.getCompetitiveWorkout() != null)
		{
			competitiveWorkoutPointsList.addAll(workoutManager.getCompetitiveWorkout().getAllWorkoutPoints());
		}

		// Competitive start point color
		competitiveStartPointPaint = new Paint();
		competitiveStartPointPaint.setARGB(250, 0, 150, 0);
		competitiveStartPointPaint.setAntiAlias(true);
		competitiveStartPointPaint.setFakeBoldText(true);

		// Competitive stop point color
		competitiveStopPointPaint = new Paint();
		competitiveStopPointPaint.setARGB(250, 150, 0, 0);
		competitiveStopPointPaint.setAntiAlias(true);
		competitiveStopPointPaint.setFakeBoldText(true);

		// Competitive path color
		competitiveLinePaint = new Paint();
		competitiveLinePaint.setARGB(255, 125, 125, 125);
		competitiveLinePaint.setAntiAlias(true);
		competitiveLinePaint.setStyle(Paint.Style.STROKE);
		competitiveLinePaint.setStrokeWidth(3);

		// Start point color
		startPointPaint = new Paint();
		startPointPaint.setARGB(250, 0, 255, 0);
		startPointPaint.setAntiAlias(true);
		startPointPaint.setFakeBoldText(true);

		// Stop point color
		stopPointPaint = new Paint();
		stopPointPaint.setARGB(250, 255, 0, 0);
		stopPointPaint.setAntiAlias(true);
		stopPointPaint.setFakeBoldText(true);

		// Path color
		linePaint = new Paint();
		linePaint.setARGB(175, 40, 40, 170);
		linePaint.setAntiAlias(true);
		// linePaint.setFakeBoldText(true);
		linePaint.setStyle(Paint.Style.STROKE);
		linePaint.setStrokeWidth(3);
		// linePaint.setPathEffect(null);

		newPoint = new Point();
	}

	@Override
	public boolean onTap(GeoPoint point, MapView mapView)
	{
		return false;
	}

	@Override
	public void draw(Canvas canvas, MapView mapView, boolean shadow)
	{
		projection = mapView.getProjection();
		path = new Path();

		if (shadow == false)
		{
			if (workoutPointsList.size() < workoutManager.getWorkoutTripPointsCount())
			{
				for (int i = workoutPointsList.size(); i < workoutManager.getWorkoutTripPointsCount(); i++)
				{
					workoutPointsList.add(workoutManager.getAllWorkoutPoints().get(i));
				}
			}

			zoomLevel = mapView.getZoomLevel();
			increment = 22 - zoomLevel;
			if (increment == 0)
				increment = 1;

			double vS = mapView.getLatitudeSpan();
			double hS = mapView.getLongitudeSpan();
			double cX = mapView.getMapCenter().getLongitudeE6();
			double cY = mapView.getMapCenter().getLatitudeE6();

			minLongitude = (cX - hS / 2);
			minLatitude = (cY - vS / 2);
			maxLongitude = (cX + hS / 2);
			maxLatitude = (cY + vS / 2);

			// draw startPoint
			if (workoutPointsList.size() > 0)
			{
				geoPoint = new GeoPoint(new Double(1E6 * workoutPointsList.get(0).getLatitude()).intValue(),
						new Double(1E6 * workoutPointsList.get(0).getLongitude()).intValue());

				projection.toPixels(geoPoint, newPoint);
				canvas.drawOval(new RectF(newPoint.x - mRadius, newPoint.y - mRadius, newPoint.x + mRadius, newPoint.y + mRadius), startPointPaint);
			}
			path.moveTo(newPoint.x, newPoint.y);

			for (int i = 1; i < workoutPointsList.size() - increment; i += increment)
			{
				TripPoint point = workoutPointsList.get(i);

				if (point.getLongitude() >= minLongitude / 1E6 && point.getLongitude() <= maxLongitude / 1E6 && point.getLatitude() >= minLatitude / 1E6
						&& point.getLatitude() <= maxLatitude / 1E6)
				{
					geoPoint = new GeoPoint(new Double(1E6 * point.getLatitude()).intValue(), new Double(1E6 * point.getLongitude()).intValue());
					projection.toPixels(geoPoint, newPoint);

					path.lineTo(newPoint.x, newPoint.y);
				}
			}

			// draw stopPoint
			if (workoutPointsList.size() > 1)
			{
				geoPoint = new GeoPoint(new Double(1E6 * workoutPointsList.get(workoutPointsList.size() - 1).getLatitude()).intValue(), new Double(
						1E6 * workoutPointsList.get(workoutPointsList.size() - 1).getLongitude()).intValue());
				projection.toPixels(geoPoint, newPoint);
				canvas.drawOval(new RectF(newPoint.x - mRadius, newPoint.y - mRadius, newPoint.x + mRadius, newPoint.y + mRadius), stopPointPaint);
				path.lineTo(newPoint.x, newPoint.y);
			}

			if (competitiveWorkoutPointsList.size() > 0)
			{
				drawCompetitiveTrack(canvas);
			}
		}
		canvas.drawPath(path, linePaint);
		super.draw(canvas, mapView, shadow);
	}

	private void drawCompetitiveTrack(Canvas canvas)
	{
		Path path = new Path();

		// draw competitive startPoint
		if (competitiveWorkoutPointsList.size() > 0)
		{
			geoPoint = new GeoPoint(new Double(1E6 * competitiveWorkoutPointsList.get(0).getLatitude()).intValue(), new Double(1E6 * competitiveWorkoutPointsList.get(0)
					.getLongitude()).intValue());

			projection.toPixels(geoPoint, newPoint);
			canvas.drawOval(new RectF(newPoint.x - mRadius, newPoint.y - mRadius, newPoint.x + mRadius, newPoint.y + mRadius), competitiveStartPointPaint);
		}
		path.moveTo(newPoint.x, newPoint.y);

		for (int i = 1; i < competitiveWorkoutPointsList.size() - increment; i += increment)
		{
			TripPoint point = competitiveWorkoutPointsList.get(i);

			if (point.getLongitude() >= minLongitude / 1E6 && point.getLongitude() <= maxLongitude / 1E6 && point.getLatitude() >= minLatitude / 1E6
					&& point.getLatitude() <= maxLatitude / 1E6)
			{
				geoPoint = new GeoPoint(new Double(1E6 * point.getLatitude()).intValue(), new Double(1E6 * point.getLongitude()).intValue());
				projection.toPixels(geoPoint, newPoint);

				path.lineTo(newPoint.x, newPoint.y);
			}
		}

		// draw stopPoint
		if (competitiveWorkoutPointsList.size() > 1)
		{
			geoPoint = new GeoPoint(new Double(1E6 * competitiveWorkoutPointsList.get(competitiveWorkoutPointsList.size() - 1).getLatitude()).intValue(), new Double(
					1E6 * competitiveWorkoutPointsList.get(competitiveWorkoutPointsList.size() - 1).getLongitude()).intValue());
			projection.toPixels(geoPoint, newPoint);
			canvas.drawOval(new RectF(newPoint.x - mRadius, newPoint.y - mRadius, newPoint.x + mRadius, newPoint.y + mRadius), competitiveStopPointPaint);
			path.lineTo(newPoint.x, newPoint.y);
		}
		canvas.drawPath(path, competitiveLinePaint);
	}
}
