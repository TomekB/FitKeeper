package pl.directsolutions.fit_keeper.controller;

import pl.directsolutions.fit_keeper.model.Workout;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.util.Log;

public class WorkoutDatabaseManager
{
	private static final String MAIN_DATABASE_NAME = "workouts.db";
	private static final String MAIN_DATABASE_TABLE = "workouts";
	private static final int DATABASE_VERSION = 1;

	public static final String MAIN_KEY_ID = "_id";
	public static final String MAIN_KEY_DISTANCE = "distance";
	public static final String MAIN_KEY_TIME = "time";
	public static final String MAIN_KEY_BURNED_CALORIES = "burned_calories";
	public static final String MAIN_KEY_START_TIME = "start_time";
	public static final String MAIN_KEY_WORKOUT_FILENAME = "workout_filename";

	private SQLiteDatabase database;
	private final Context context;
	private toDoDBOpenHelper databaseHelper;

	/* Constructor */
	public WorkoutDatabaseManager(Context _context)
	{
		this.context = _context;
		databaseHelper = new toDoDBOpenHelper(context, MAIN_DATABASE_NAME, null, DATABASE_VERSION);
	}

	/* Close Database */
	public void close()
	{
		database.close();
	}

	/* Open Database to read and write */
	public void open() throws SQLiteException
	{
		try
		{
			database = databaseHelper.getWritableDatabase();
		} catch (SQLiteException ex)
		{
			database = databaseHelper.getReadableDatabase();
		}
	}

	/* Insert a new point */
	public long insertWorkout(Workout workout)
	{
		ContentValues newWorkoutValues = new ContentValues();

		newWorkoutValues.put(MAIN_KEY_DISTANCE, workout.getDistance());
		newWorkoutValues.put(MAIN_KEY_BURNED_CALORIES, workout.getBurnedCalories());
		newWorkoutValues.put(MAIN_KEY_START_TIME, workout.getStartTimeMillis());
		newWorkoutValues.put(MAIN_KEY_TIME, workout.getWorkoutTime());
		newWorkoutValues.put(MAIN_KEY_WORKOUT_FILENAME, workout.getWorkoutPointsFilename());

		return database.insert(MAIN_DATABASE_TABLE, null, newWorkoutValues);
	}
	
	public void removeWorkout(final long startTime){
		database.delete(MAIN_DATABASE_TABLE, MAIN_KEY_START_TIME + "=" + startTime, null);
	}

	/* Returns all workouts from Database */
	public Cursor getAllWorkoutsCursor()
	{
		Cursor result = database.query(true, MAIN_DATABASE_TABLE, new String[] { MAIN_KEY_ID, MAIN_KEY_DISTANCE, MAIN_KEY_BURNED_CALORIES, MAIN_KEY_START_TIME,
				MAIN_KEY_TIME, MAIN_KEY_WORKOUT_FILENAME }, null, null, null, null, null, null);

		if ((result.getCount() == 0) || !result.moveToFirst())
		{
			throw new SQLException("No Workouts found!");
		}

		return result;
	}

	// ******************************************************************************************************************
	private static class toDoDBOpenHelper extends SQLiteOpenHelper
	{

		public toDoDBOpenHelper(Context context, String name, CursorFactory factory, int version)
		{
			super(context, name, factory, version);
		}

		// SQL Statement to create a new database.
		private static final String DATABASE_CREATE = "create table " + MAIN_DATABASE_TABLE + " (" + MAIN_KEY_ID + " integer primary key autoincrement, "
				+ MAIN_KEY_DISTANCE + " real, " + MAIN_KEY_BURNED_CALORIES + " integer, " + MAIN_KEY_START_TIME + " long, " + MAIN_KEY_TIME + " long, "
				+ MAIN_KEY_WORKOUT_FILENAME + " text);";

		@Override
		public void onCreate(SQLiteDatabase _db)
		{
			_db.execSQL(DATABASE_CREATE);
		}

		@Override
		public void onUpgrade(SQLiteDatabase _db, int _oldVersion, int _newVersion)
		{
			Log.w("TaskDBAdapter", "Upgrading from version " + _oldVersion + " to " + _newVersion + ", which will destroy all old data");

			// Drop the old table.
			_db.execSQL("DROP TABLE IF EXISTS " + MAIN_DATABASE_TABLE);
			// Create a new one.
			onCreate(_db);
		}
	}
}