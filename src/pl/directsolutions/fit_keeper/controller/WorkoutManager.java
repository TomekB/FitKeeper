package pl.directsolutions.fit_keeper.controller;

import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Calendar;

import pl.directsolutions.fit_keeper.model.TripPoint;
import pl.directsolutions.fit_keeper.model.Workout;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;

/*
 * TO DO:
 * - clear on destroyActivity
 */
public class WorkoutManager
{
	private static WorkoutManager instance = null;
	private Workout workout = null;
	private Workout competitiveWorkout = null;
	Context context;
	private ArrayList<Workout> workoutsList = null;

	public static Double runningMetValues[] = { 8.0, 9.0, 10.0, 11.0, 11.5, 12.5, 13.5, 14.0, 15.0, 16.0, 18.0 };
	public static Double runningMetSpeeds[] = { 2.235, 2.324, 2.682, 2.995, 3.129, 3.352, 3.576, 3.844, 4.023, 4.470, 4.872 }; // in m/s

	public static WorkoutManager getInstance(Context context)
	{
		if (instance == null)
		{
			instance = new WorkoutManager(context);
		}
		return instance;
	}

	private WorkoutManager(Context context)
	{
		this.context = context;
	}

	public void startWorkout(long currentTimeInMillis)
	{
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(currentTimeInMillis);
		workout = new Workout(calendar);
	}

	public void setActualWorkoutIndex(int index)
	{
		workout = workoutsList.get(index);
	}
	
	public void setBurnedCalories(int burnedCalories){
		workout.setBurnedCalories(burnedCalories);
	}

	public Workout getActualWorkout()
	{
		return workout;
	}

	public void setCompetitiveWorkoutIndex(int index)
	{
		competitiveWorkout = workoutsList.get(index);
	}

	public Workout getCompetitiveWorkout()
	{
		return competitiveWorkout;
	}

	public void pauseWorkout(long currentTimeInMillis)
	{
		workout.pauseWorkout(currentTimeInMillis);
	}

	public void resumeWorkout(long currentTimeInMillis)
	{
		workout.resumeWorkout(currentTimeInMillis);
	}

	public void stopWorkout(long currentTimeInMillis)
	{
		workout.stopWorkout(currentTimeInMillis);
	}

	public void saveWorkout(FileOutputStream fos, String workoutPointsFileName)
	{
		PrintWriter out = new PrintWriter(fos);

		ArrayList<TripPoint> workoutPoints = workout.getAllWorkoutPoints();
		TripPoint tripPoint;

		for (int i = 0; i < workoutPoints.size(); i++)
		{
			tripPoint = workoutPoints.get(i);
			out.print(tripPoint.getLongitude() + " ");
			out.print(tripPoint.getLatitude() + " ");
			out.print(tripPoint.getTime() + " ");
			out.println(tripPoint.getDistance());
		}

		out.flush();
		out.close();

		workout.setWorkoutPointsFilename(workoutPointsFileName);

		WorkoutDatabaseManager databaseManager = new WorkoutDatabaseManager(context);
		databaseManager.open();
		databaseManager.insertWorkout(workout);
		databaseManager.close();
	}

	public void addTripPoint(TripPoint tripPoint)
	{
		workout.addTripPoint(tripPoint);
	}

	public void clear()
	{
		workout = null;
		competitiveWorkout = null;
	}

	public long getWorkoutTimeMillis()
	{
		if (workout != null)
			return workout.getWorkoutTime();

		return 0;
	}

	/*
	 * Return distance in meters
	 */
	public float getWorkoutDistance()
	{
		if (workout != null)
			return workout.getDistance();

		return 0;
	}

	public int getWorkoutTripPointsCount()
	{
		if (workout != null)
			return workout.getAllWorkoutPoints().size();

		return 0;
	}

	public ArrayList<TripPoint> getAllWorkoutPoints()
	{
		if (workout != null)
			return workout.getAllWorkoutPoints();

		return null;
	}

	public ArrayList<TripPoint> getVisiblePoints(double vS, double hS, double cX, double cY)
	{
		if (workout != null)
		{
			return workout.getVisiblePoints((double) (cX - hS / 2), (double) (cY - vS / 2), (double) (cX + hS / 2), (double) (cY + vS / 2));
		}

		return new ArrayList<TripPoint>();
	}

	public void updateTime(long currentTimeInMillis)
	{
		if (workout != null)
			workout.updateTime(currentTimeInMillis);
	}

	public ArrayList<Workout> getAllWorkouts()
	{
		workoutsList = new ArrayList<Workout>();
		WorkoutDatabaseManager databaseManager = new WorkoutDatabaseManager(context);
		Cursor cursor;
		databaseManager.open();

		int burnedCalories;
		float distance;
		Calendar startDate;
		long workoutTime;
		long startDateInMillis;
		String workoutPointsFilename;

		try
		{
			cursor = databaseManager.getAllWorkoutsCursor();

			if (cursor.moveToFirst())
			{
				do
				{
					burnedCalories = cursor.getInt(cursor.getColumnIndex(WorkoutDatabaseManager.MAIN_KEY_BURNED_CALORIES));
					distance = cursor.getFloat(cursor.getColumnIndex(WorkoutDatabaseManager.MAIN_KEY_DISTANCE));
					startDateInMillis = cursor.getLong(cursor.getColumnIndex(WorkoutDatabaseManager.MAIN_KEY_START_TIME));
					workoutTime = cursor.getLong(cursor.getColumnIndex(WorkoutDatabaseManager.MAIN_KEY_TIME));
					workoutPointsFilename = cursor.getString(cursor.getColumnIndex(WorkoutDatabaseManager.MAIN_KEY_WORKOUT_FILENAME));

					startDate = Calendar.getInstance();
					startDate.setTimeInMillis(startDateInMillis);

					workoutsList.add(new Workout(burnedCalories, distance, startDate, workoutTime, workoutPointsFilename));

				} while (cursor.moveToNext());
			}
		} catch (SQLException e)
		{
		}
		databaseManager.close();

		return workoutsList;
	}

	public void removeWorkout(final int index)
	{
		WorkoutDatabaseManager databaseManager = new WorkoutDatabaseManager(context);
		databaseManager.open();
		databaseManager.removeWorkout(workoutsList.get(index).getStartTimeMillis());
		databaseManager.close();
		workoutsList.remove(index);
	}

	public void reset()
	{
		workout = null;
	}
}
