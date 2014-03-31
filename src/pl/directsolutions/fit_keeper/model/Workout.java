package pl.directsolutions.fit_keeper.model;

import java.io.BufferedReader;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;

import android.util.Log;

/*
 * TO DO:
 * return distance in proper units
 */

public class Workout
{
	private int burnedCalories;
	private float distance; // in meters
	private Calendar startDate;
	private long workoutTime;
	private long pauseStartTimeInMillis, currentPauseTime, absolutePauseTime;
	ArrayList<TripPoint> workoutPoints = null;
	private TripPoint startPoint;
	double speed, pace;
	private String workoutPointsFilename;

	boolean workoutActive;
	boolean workoutPaused;

	private TripPoint previousPoint;
	private TripPoint actualPoint;

	public Workout(int burnedCalories, float distance, Calendar startDate, long workoutTime, String workoutPointsFilename)
	{
		this.burnedCalories = burnedCalories;
		this.distance = distance;
		this.startDate = startDate;
		this.workoutTime = workoutTime;
		this.workoutPointsFilename = workoutPointsFilename;
		workoutPoints = new ArrayList<TripPoint>();
		startPoint=null;
	}

	public void readPointsFromFile(FileInputStream fileInputStream)
	{

		FileDescriptor descriptor = null;
		try
		{
			descriptor = fileInputStream.getFD();
		} catch (IOException e1)
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		FileReader fileReader = new FileReader(descriptor);
		BufferedReader reader = new BufferedReader(fileReader);
		String line;
		Double longitude, latitude;
		long time;
		float distance;
		String array[];

		try
		{
			while ((line = reader.readLine()) != null)
			{
				array = line.split(" ");
				longitude = Double.parseDouble(array[0].trim());
				latitude = Double.parseDouble(array[1].trim());
				time = Long.parseLong(array[2].trim());
				distance = Float.parseFloat(array[3].trim());
				workoutPoints.add(new TripPoint(longitude, latitude, time, distance));
			}
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public Workout(Calendar date)
	{
		this.startDate = date;
		workoutPoints = new ArrayList<TripPoint>();
		distance = workoutTime = burnedCalories = 0;
		workoutActive = true;
		workoutPaused = false;
		speed = 0;
		pace = 0;
		absolutePauseTime = currentPauseTime = 0;
	}

	public synchronized void setBurnedCalories(int burnedCalories)
	{
		this.burnedCalories = burnedCalories;
	}

	public synchronized void addTripPoint(TripPoint tripPoint)
	{
		workoutPoints.add(tripPoint);
		actualPoint = workoutPoints.get(workoutPoints.size() - 1);
		actualPoint.setDistance(0);

		if (workoutPoints.size() > 1)
		{
			previousPoint = workoutPoints.get(workoutPoints.size() - 2);
			updateDistance();
		}
	}

	/*
	 * Distance counted with Haversine formula in kilometers
	 */
	private synchronized void updateDistance()
	{
		double previousLatitude = previousPoint.getLatitude();
		double previousLongitude = previousPoint.getLongitude();
		double actualLatitude = actualPoint.getLatitude();
		double actualLongitude = actualPoint.getLongitude();

		double dist = 0.0;
		double deltaLat = Math.toRadians(actualLatitude - previousLatitude);
		double deltaLon = Math.toRadians(actualLongitude - previousLongitude);
		previousLatitude = Math.toRadians(previousLatitude);
		actualLatitude = Math.toRadians(actualLatitude);

		double earthRadius = 6371;
		double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2) + Math.cos(previousLatitude) * Math.cos(actualLatitude) * Math.sin(deltaLon / 2)
				* Math.sin(deltaLon / 2);
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
		dist = earthRadius * c; // in kilometers
		dist *= 1000; // in meters

		distance += dist;
		actualPoint.setDistance(distance);
	}

	public synchronized void updateTime(long currentTimeInMillis)
	{
		if (workoutPaused)
		{
			currentPauseTime = currentTimeInMillis - pauseStartTimeInMillis;
		}
		if (workoutActive)
		{
			workoutTime = currentTimeInMillis - startDate.getTimeInMillis() - absolutePauseTime;
		}
	}

	public synchronized int getBurnedCalories()
	{
		return burnedCalories;
	}

	public synchronized float getDistance()
	{
		return distance;
	}

	public synchronized Calendar getStartDate()
	{
		return startDate;
	}

	public synchronized long getWorkoutTime()
	{
		return workoutTime;
	}

	public synchronized ArrayList<TripPoint> getAllWorkoutPoints()
	{
		return workoutPoints;
	}

	public void pauseWorkout(long currentTimeInMillis)
	{
		workoutActive = false;
		workoutPaused = true;
		pauseStartTimeInMillis = currentTimeInMillis;
	}

	public void stopWorkout(long currentTimeInMillis)
	{
		workoutActive = false;
	}

	public void resumeWorkout(long currentTimeInMillis)
	{
		workoutActive = true;
		workoutPaused = false;
		absolutePauseTime += currentPauseTime;
		currentPauseTime = 0;
	}

	public synchronized ArrayList<TripPoint> getVisiblePoints(double minLongitude, double minLatitude, double maxLongitude, double maxLatitude)
	{
		ArrayList<TripPoint> visiblePoints = new ArrayList<TripPoint>();
		boolean lastNotShowed = false;

		for (int i = 0; i < workoutPoints.size(); i++)
		{
			TripPoint point = workoutPoints.get(i);

			if (i == 0 || i == workoutPoints.size() - 1 || point.getLongitude() >= minLongitude / 1E6 && point.getLongitude() <= maxLongitude / 1E6
					&& point.getLatitude() >= minLatitude / 1E6 && point.getLatitude() <= maxLatitude / 1E6)
			{
				if (lastNotShowed)
				{
					visiblePoints.add(workoutPoints.get(i - 1));
				}
				visiblePoints.add(point);
				lastNotShowed = false;
			}

			else
			{
				if (!lastNotShowed)
				{
					visiblePoints.add(workoutPoints.get(i));
				}
				lastNotShowed = true;
			}
		}

		return visiblePoints;
	}

	public boolean isWorkoutActive()
	{
		return workoutActive;
	}

	public boolean isWorkoutPaused()
	{
		return workoutPaused;
	}

	public double getSpeed()
	{
		return speed;
	}

	public double getPace()
	{
		return pace;
	}

	public String getWorkoutPointsFilename()
	{
		return workoutPointsFilename;
	}

	public void setWorkoutPointsFilename(String workoutPointsFilename)
	{
		this.workoutPointsFilename = workoutPointsFilename;
	}

	public long getStartTimeMillis()
	{
		return startDate.getTimeInMillis();
	}

	public TripPoint readStartPointFromFile(FileInputStream fileInputStream)
	{
		FileDescriptor descriptor = null;
		try
		{
			descriptor = fileInputStream.getFD();
		} catch (IOException e1)
		{
			Log.d("NO file", "No file descriptor");
			e1.printStackTrace();
		}
		FileReader fileReader = new FileReader(descriptor);
		BufferedReader reader = new BufferedReader(fileReader);
		String line;
		Double longitude, latitude;
		long time;
		float distance;
		String array[];

		try
		{
			if ((line = reader.readLine()) != null)
			{
				Log.d("LINE", line);
				array = line.split(" ");
				longitude = Double.parseDouble(array[0].trim());
				latitude = Double.parseDouble(array[1].trim());
				time = Long.parseLong(array[2].trim());
				distance = Float.parseFloat(array[3].trim());
				startPoint = new TripPoint(longitude, latitude, time, distance);
			}
		} catch (IOException e)
		{
			e.printStackTrace();
		}
		return startPoint;
	}

	public TripPoint getStartPoint()
	{
		return startPoint;
	}
}
