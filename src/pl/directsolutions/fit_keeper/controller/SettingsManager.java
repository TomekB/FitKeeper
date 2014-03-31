package pl.directsolutions.fit_keeper.controller;

import java.util.ArrayList;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

public class SettingsManager
{
	private static SettingsManager instance = null;
	private static ArrayList<String> mainTagsList;
	private static final double POUNDS_TO_KILOGRAM = 2.20462262;
	private static final float GRAMS_PER_POUND = new Float(453.59237);

	public static final String SCANNED_BARCODE_KEY = "scanned_barcode_key";

	public static final String PREFERENCES_CALORIES_REQUIREMENT = "calories_requirement";
	public static final String PREFERENCES_PROTEIN_REQUIREMENT = "protein_requirement";
	public static final String PREFERENCES_CARBOHYDRATES_REQUIREMENT = "carbohydrates_requirement";
	public static final String PREFERENCES_FAT_REQUIREMENT = "fat_requirement";

	public static final String PREFERENCES_USER_AGE_KEY = "user_age_key";
	public static final String PREFERENCES_USER_EU_WEIGHT_KEY = "user_eu_weight_key";
	public static final String PREFERENCES_USER_US_WEIGHT_KEY = "user_us_weight_key";
	public static final String PREFERENCES_USER_US_HEIGHT_KEY = "user_us_height_key";
	public static final String PREFERENCES_USER_EU_HEIGHT_KEY = "user_eu_height_key";
	public static final String PREFERENCES_USER_SEX_KEY = "user_sex_key";
	public static final String PREFERENCES_ACTIVITY_TYPE_KEY = "activity_type_key";
	public static final String PREFERENCES_UNITS_TYPE_KEY = "units_type_key";
	public static final String PREFERENCES_AUTOPAUSE_KEY = "autopause_key";
	public static final String PREFERENCES_GPS_REFRESH_RATE_KEY = "gps_refresh_rate_key";
	public static final String PREFERENCES_NOTIFICATION_TYPE_KEY = "notification_type_key";
	public static final String PREFERENCES_NOTIFICATION_EU_DISTANCE_KEY = "notification_distance_in_meters_key";
	public static final String PREFERENCES_NOTIFICATION_EU_DISTANCE_IN_RACE_KEY = "notification_distance_in_meters_in_race_key";
	public static final String PREFERENCES_NOTIFICATION_US_DISTANCE_KEY = "notification_distance_in_miles_key";
	public static final String PREFERENCES_NOTIFICATION_US_DISTANCE_IN_RACE_KEY = "notification_distance_in_miles_in_race_key";
	public static final String PREFERENCES_NOTIFICATION_PERIOD_KEY = "notification_period_key";
	public static final String PREFERENCES_NOTIFICATION_PERIOD_IN_RACE_KEY = "notification_period_in_race_key";
	public static final String PREFERENCES_NOTIFICATION_SHOW_SPEED_INSTEAD_OF_TEMPO_KEY = "notification_speed_or_tempo_key";
	public static final String PREFERENCES_NOTIFICATION_SHOW_BURNED_CALORIES_KEY = "notification_burned_calories_key";
	public static final String PREFERENCES_VOICE_NOTIFICATION_KEY = "notification_voice_notification_key";

	private static final String USER_AGE_TAG = "Age";
	private static final String USER_WEIGHT_TAG = "Weight";
	private static final String USER_HEIGHT_TAG = "Height";
	private static final String USER_SEX_TAG = "Sex";

	public static final String AGREEMENT_TAG = "OK";
	public static final String CANCEL_TAG = "CANCEL";

	public static final String AGE_PROMPT = "Choose your age:";
	public static final String WEIGHT_PROMPT = "Choose your weight:";
	public static final String HEIGHT_PROMPT = "Choose your height:";
	public static final String SEX_PROMPT = "Choose your sex:";
	public static final String ACTIVITY_PROMPT = "Choose activity type:";
	public static final String UNITS_PROMPT = "Choose units type:";
	public static final String AUTOPAUSE_PROMPT = "Choose autopause interval:";
	public static final String GPS_REFRESH_RATE_PROMPT = "Choose GPS refresh rate:";

	private static final String NOTIFICATION_TYPE_TAG = "Notification type";
	private static final String NOTIFICATION_INTERVAL_TAG = "Notification interval";
	private static final String NOTIFICATION_INTREVAL_IN_RACE_TAG = "In race interval";
	private static final String NOTIFICATION_SHOW_SPEED_INSTEAD_OF_TEMPO_TAG = "Show speed instead of tempo";
	private static final String NOTIFICATION_SHOW_BURNED_CALORIES_TAG = "Notify burned calories";
	private static final String NOTIFICATION_VOICE_NOTIFICATION_TAG = "Voice notifications enabled";

	public static final String EU_UNITS = "km / kg";
	public static final String US_UNITS = "mile / pound";

	private static final String EU_WEIGHT_UNIT = "kg";
	private static final String US_WEIGHT_UNIT = "pounds";
	private static final String EU_LENGTH_UNIT = "km";
	private static final String US_LENGTH_UNIT = "mile";

	private static String ageValuesTable[];
	private static String EUWeightValuesTable[];
	private static String USWeightValuesTable[];
	private static String USHeightStringTable[];
	private static String EUHeightStringTable[];
	private static int EUHeightValuesTable[];
	private static int USHeightValuesTable[];
	private static String sexValuesTable[] = { "male", "female" };
	public static String activityValuesTable[] = { "running", "biking", "kayaking" };
	private static String unitsValuesTable[] = { EU_UNITS, US_UNITS };
	public static String autoPauseStringsTable[] = { "5 seconds", "10 seconds", "15 seconds", "30 seconds", "1 minute", "never" };
	public static long autoPauseInMillisTable[] = { 5000, 10000, 15000, 30000, 60000, 0 };
	public static String gpsRefreshRateStringsTable[] = { "1 second", "5 seconds", "10 seconds", "15 seconds", "30 seconds", "1 minute", "2 minutes", "5 minutes" };
	public static long gpsRefreshRateInMillisTable[] = { 1000, 5000, 10000, 15000, 30000, 60000, 120000, 300000 };
	public static String notificationTypeValuesTable[] = { "time", "distance" };
	public static String notificationDistancesInKilometersStringTable[] = { "100m", "200m", "300m", "400m", "500m", "1km", "2km", "5km" };
	public static int notificationDistancesInMetersValueTable[] = { 100, 200, 300, 400, 500, 1000, 2000, 5000 };
	public static String notificationDistancesInMilesStringTable[] = { "0,1mi", "0,2mi", "0,3mi", "0,4mi", "0,5mi", "1mi", "2mi", "5mi" };
	public static int notificationDistancesInYardsValueTable[] = { 176, 352, 528, 704, 880, 1760, 3520, 8800 };
	public static String notificationTimesStringTable[] = { "15 seconds", "30 seconds", "1 minute", "2 minutes", "5 minutes", "10 minutes" };
	public static int notificationTimesValueTable[] = { 15, 30, 60, 120, 300, 600 };

	private SharedPreferences sharedPreferences;

	static
	{
		mainTagsList = new ArrayList<String>();
		mainTagsList.add("User");
		mainTagsList.add("Activity");
		mainTagsList.add("Units");
		mainTagsList.add("Autopause");
		mainTagsList.add("GPS");
		mainTagsList.add("Notifications");

		ageValuesTable = new String[100];
		for (int i = 0; i < 100; i++)
		{
			ageValuesTable[i] = Integer.toString(i + 5);
		}

		EUWeightValuesTable = new String[151];
		for (int i = 0; i < 151; i++)
		{
			EUWeightValuesTable[i] = Integer.toString(i + 30);
		}

		USWeightValuesTable = new String[331];
		for (int i = 0; i < 331; i++)
		{
			USWeightValuesTable[i] = Integer.toString(i + 66);
		}

		USHeightStringTable = new String[144];
		for (int i = 2; i < 8; i++)
		{
			USHeightStringTable[(i - 2) * 24] = Integer.toString(i) + "ft";
			USHeightStringTable[(i - 2) * 24 + 1] = Integer.toString(i) + "ft " + Integer.toString(0) + ",5" + "in";

			for (int j = 1; j < 12; j++)
			{
				USHeightStringTable[(i - 2) * 24 + (j * 2) + 1] = Integer.toString(i) + "ft " + Integer.toString(j) + ",5" + "in";
				USHeightStringTable[(i - 2) * 24 + (j * 2)] = Integer.toString(i) + "ft " + Integer.toString(j) + "in";
			}
		}

		EUHeightStringTable = new String[180];
		for (int i = 0; i < 180; i++)
		{
			EUHeightStringTable[i] = Integer.toString(i + 50) + "cm";
		}

		EUHeightValuesTable = new int[180];
		for (int i = 0; i < 180; i++)
		{
			EUHeightValuesTable[i] = i + 50;
		}

		USHeightValuesTable = new int[144];
		for (int i = 0; i < 144; i++)
		{
			Double value = new Double(2 * 0.3048);
			value += i * (0.3048 / 24);
			USHeightValuesTable[i] = value.intValue();
		}
	}

	public static SettingsManager getInstance(Context context)
	{
		if (instance == null)
		{
			instance = new SettingsManager(context);
		}
		return instance;
	}

	private SettingsManager(Context context)
	{
		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
	}

	public int getMainTagsListSize()
	{
		return mainTagsList.size();
	}

	public String getMainTag(int index)
	{
		if (index < mainTagsList.size())
		{
			return (mainTagsList.get(index));
		} else
		{
			return null;
		}
	}

	public int getAge()
	{
		int age = sharedPreferences.getInt(SettingsManager.PREFERENCES_USER_AGE_KEY, 18);
		return age;
	}

	public int getWeight()
	{
		int weight;
		if (getUnits().equals(US_UNITS))
		{
			weight = sharedPreferences.getInt(SettingsManager.PREFERENCES_USER_US_WEIGHT_KEY, 132);
		} else
		{
			weight = sharedPreferences.getInt(SettingsManager.PREFERENCES_USER_EU_WEIGHT_KEY, 60);
		}
		return weight;
	}

	public int getHeightValueInCentimeters()
	{
		int height;
		int position;
		if (getUnits().equals(US_UNITS))
		{
			position = sharedPreferences.getInt(SettingsManager.PREFERENCES_USER_US_HEIGHT_KEY, 100);
			height = USHeightValuesTable[position];
		} else
		{
			position = sharedPreferences.getInt(SettingsManager.PREFERENCES_USER_EU_HEIGHT_KEY, 120);
			height = EUHeightValuesTable[position];
		}
		return height;
	}

	public String getHeightString()
	{
		int position;
		if (getUnits().equals(US_UNITS))
		{
			position = sharedPreferences.getInt(SettingsManager.PREFERENCES_USER_US_HEIGHT_KEY, 100);
			return USHeightStringTable[position];
		} else
		{
			position = sharedPreferences.getInt(SettingsManager.PREFERENCES_USER_EU_HEIGHT_KEY, 120);
			return EUHeightStringTable[position];
		}
	}

	public int getWeightInKG()
	{
		int weight;

		if (getUnits().equals(US_UNITS))
		{
			weight = sharedPreferences.getInt(SettingsManager.PREFERENCES_USER_US_WEIGHT_KEY, 132);
			Double kilograms = weight / POUNDS_TO_KILOGRAM;
			weight = kilograms.intValue();
		} else
		{
			weight = sharedPreferences.getInt(SettingsManager.PREFERENCES_USER_EU_WEIGHT_KEY, 60);
		}
		return weight;
	}

	public String getSex()
	{
		String sex = sharedPreferences.getString(SettingsManager.PREFERENCES_USER_SEX_KEY, "male");
		return sex;
	}

	public String getActivity()
	{
		String activity = sharedPreferences.getString(SettingsManager.PREFERENCES_ACTIVITY_TYPE_KEY, "running");
		return activity;
	}

	public String getUnits()
	{
		String units = sharedPreferences.getString(SettingsManager.PREFERENCES_UNITS_TYPE_KEY, "km / kg");
		return units;
	}

	public String getAutoPause()
	{
		String autoPause = sharedPreferences.getString(SettingsManager.PREFERENCES_AUTOPAUSE_KEY, "never");
		return autoPause;
	}

	public String getGpsRefreshRate()
	{
		String gpsRefreshRate = sharedPreferences.getString(SettingsManager.PREFERENCES_GPS_REFRESH_RATE_KEY, "1 second");
		return gpsRefreshRate;
	}

	public static String[] getActivityValuesTable()
	{
		return activityValuesTable;
	}

	public String[] getAgeValuesTable()
	{
		return ageValuesTable;
	}

	public String[] getWeightValuesTable()
	{
		if (getUnits().equals(US_UNITS))
		{
			return USWeightValuesTable;
		}
		return EUWeightValuesTable;
	}

	public String[] getHeightStringTable()
	{
		if (getUnits().equals(US_UNITS))
		{
			return USHeightStringTable;
		}
		return EUHeightStringTable;
	}

	public String[] getSexValuesTable()
	{
		return sexValuesTable;
	}

	public static String[] getUnitsValuesTable()
	{
		return unitsValuesTable;
	}

	public static String[] getAutoPauseStringsTable()
	{
		return autoPauseStringsTable;
	}

	public static String[] getGpsRefreshRateStringsTable()
	{
		return gpsRefreshRateStringsTable;
	}

	public static String[] getNotificationTypeValuesTable()
	{
		return notificationTypeValuesTable;
	}

	public static String[] getNotificationDistancesInKilometersStringTable()
	{
		return notificationDistancesInKilometersStringTable;
	}

	public static String[] getNotificationDistancesInMilesStringTable()
	{
		return notificationDistancesInMilesStringTable;
	}

	public static String[] getNotificationTimesStringTable()
	{
		return notificationTimesStringTable;
	}

	public static String getUserAgeTag()
	{
		return USER_AGE_TAG;
	}

	public static String getUserWeightTag()
	{
		return USER_WEIGHT_TAG;
	}

	public static String getUserHeightTag()
	{
		return USER_HEIGHT_TAG;
	}

	public static String getUserSexTag()
	{
		return USER_SEX_TAG;
	}

	public static String getNotificationTypeTag()
	{
		return NOTIFICATION_TYPE_TAG;
	}

	public static String getNotificationIntervalTag()
	{
		return NOTIFICATION_INTERVAL_TAG;
	}

	public static String getNotificationIntrevalInRaceTag()
	{
		return NOTIFICATION_INTREVAL_IN_RACE_TAG;
	}

	public static String getNotificationShowSpeedInsteadOfTempoTag()
	{
		return NOTIFICATION_SHOW_SPEED_INSTEAD_OF_TEMPO_TAG;
	}

	public static String getNotificationShowBurnedCaloriesTag()
	{
		return NOTIFICATION_SHOW_BURNED_CALORIES_TAG;
	}

	public static String getVoiceNotificationTag()
	{
		return NOTIFICATION_VOICE_NOTIFICATION_TAG;
	}

	public void setActivity(String activity, Context context)
	{
		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		Editor editor = sharedPreferences.edit();
		editor.putString(SettingsManager.PREFERENCES_ACTIVITY_TYPE_KEY, activity);
		editor.commit();
	}

	public void setUnits(String units, Context context)
	{
		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		Editor editor = sharedPreferences.edit();
		editor.putString(SettingsManager.PREFERENCES_UNITS_TYPE_KEY, units);
		editor.commit();
	}

	public void setAutoPause(String autoPause, Context context)
	{
		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		Editor editor = sharedPreferences.edit();
		editor.putString(SettingsManager.PREFERENCES_AUTOPAUSE_KEY, autoPause);
		editor.commit();
	}

	public void setGPSRefreshRate(String gpsRefreshRate, Context context)
	{
		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		Editor editor = sharedPreferences.edit();
		editor.putString(SettingsManager.PREFERENCES_GPS_REFRESH_RATE_KEY, gpsRefreshRate);
		editor.commit();
	}

	public void setAge(Integer age, Context context)
	{
		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		Editor editor = sharedPreferences.edit();
		editor.putInt(SettingsManager.PREFERENCES_USER_AGE_KEY, age);
		editor.commit();
	}

	public void setWeight(Integer weight, Context context)
	{
		if (weight != getWeight())
		{
			sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
			Editor editor = sharedPreferences.edit();

			if (getUnits().equals(EU_UNITS))
			{
				editor.putInt(SettingsManager.PREFERENCES_USER_EU_WEIGHT_KEY, weight);
				editor.putInt(SettingsManager.PREFERENCES_USER_US_WEIGHT_KEY, (int) (POUNDS_TO_KILOGRAM * weight));
			}

			else
			{
				editor.putInt(SettingsManager.PREFERENCES_USER_EU_WEIGHT_KEY, (int) (weight / POUNDS_TO_KILOGRAM));
				editor.putInt(SettingsManager.PREFERENCES_USER_US_WEIGHT_KEY, weight);
			}

			editor.commit();
		}
	}

	public void setHeight(Integer position, Context context)
	{
		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		Editor editor = sharedPreferences.edit();

		if (getUnits().equals(EU_UNITS))
		{
			editor.putInt(SettingsManager.PREFERENCES_USER_EU_HEIGHT_KEY, position);
		}

		else
		{
			editor.putInt(SettingsManager.PREFERENCES_USER_US_HEIGHT_KEY, position);
		}

		editor.commit();
	}

	public void setSex(String sex, Context context)
	{
		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		Editor editor = sharedPreferences.edit();
		editor.putString(SettingsManager.PREFERENCES_USER_SEX_KEY, sex);
		editor.commit();
	}

	public String getWeightUnit()
	{
		if (getUnits().equals(EU_UNITS))
		{
			return EU_WEIGHT_UNIT;
		} else
		{
			return US_WEIGHT_UNIT;
		}
	}

	public String getLenghtUnit()
	{
		if (getUnits().equals(EU_UNITS))
		{
			return EU_LENGTH_UNIT;
		} else
		{
			return US_LENGTH_UNIT;
		}
	}

	public String getUserDetails()
	{
		String userDetails = getAge() + " years / " + getWeight() + " " + getWeightUnit() + " / " + getHeightString() + " / " + getSex();
		return userDetails;
	}

	public String getNotificationType()
	{
		return sharedPreferences.getString(PREFERENCES_NOTIFICATION_TYPE_KEY, "time");
	}

	public String getNotificationValue()
	{
		String notificationValue;

		if (getNotificationType().equals("distance"))
		{
			if (getUnits().equals(EU_UNITS))
			{
				notificationValue = sharedPreferences.getString(SettingsManager.PREFERENCES_NOTIFICATION_EU_DISTANCE_KEY, "100m");
			} else
			{
				notificationValue = sharedPreferences.getString(SettingsManager.PREFERENCES_NOTIFICATION_US_DISTANCE_KEY, "0,1mi");
			}
		} else
		{
			notificationValue = sharedPreferences.getString(SettingsManager.PREFERENCES_NOTIFICATION_PERIOD_KEY, "30 seconds");
		}

		return notificationValue;
	}

	public String getNotificationInRaceValue()
	{
		String notificationInRaceValue;

		if (getNotificationType().equals("distance"))
		{
			if (getUnits().equals(EU_UNITS))
			{
				notificationInRaceValue = sharedPreferences.getString(SettingsManager.PREFERENCES_NOTIFICATION_EU_DISTANCE_IN_RACE_KEY, "100m");
			} else
			{
				notificationInRaceValue = sharedPreferences.getString(SettingsManager.PREFERENCES_NOTIFICATION_US_DISTANCE_IN_RACE_KEY, "0,1mi");
			}
		} else
		{
			notificationInRaceValue = sharedPreferences.getString(SettingsManager.PREFERENCES_NOTIFICATION_PERIOD_IN_RACE_KEY, "30 seconds");
		}

		return notificationInRaceValue;
	}

	public boolean isShowSpeedInsteadOfTempo()
	{
		return sharedPreferences.getBoolean(PREFERENCES_NOTIFICATION_SHOW_SPEED_INSTEAD_OF_TEMPO_KEY, true);
	}

	public boolean isShowBurnedCalories()
	{
		return sharedPreferences.getBoolean(PREFERENCES_NOTIFICATION_SHOW_BURNED_CALORIES_KEY, true);
	}

	public boolean isVoiceNotification()
	{
		return sharedPreferences.getBoolean(PREFERENCES_VOICE_NOTIFICATION_KEY, true);
	}

	public String[] getNotificationIntervalStringTable()
	{
		String[] table;
		if (getNotificationType().equals("distance"))
		{
			if (getUnits().equals(EU_UNITS))
			{
				table = getNotificationDistancesInKilometersStringTable();
			} else
			{
				table = getNotificationDistancesInMilesStringTable();
			}
		} else
		{
			table = getNotificationTimesStringTable();
		}
		return table;
	}

	public void changeShowSpeedInsteadOfTempo()
	{
		boolean show = isShowSpeedInsteadOfTempo();
		Editor editor = sharedPreferences.edit();
		editor.putBoolean(SettingsManager.PREFERENCES_NOTIFICATION_SHOW_SPEED_INSTEAD_OF_TEMPO_KEY, !show);
		editor.commit();
	}

	public void changeShowBurnedCalories()
	{
		boolean show = isShowBurnedCalories();
		Editor editor = sharedPreferences.edit();
		editor.putBoolean(SettingsManager.PREFERENCES_NOTIFICATION_SHOW_BURNED_CALORIES_KEY, !show);
		editor.commit();
	}

	public void changeVoiceNotification()
	{
		boolean notification = isVoiceNotification();
		Editor editor = sharedPreferences.edit();
		editor.putBoolean(SettingsManager.PREFERENCES_VOICE_NOTIFICATION_KEY, !notification);
		editor.commit();
	}

	public void setNotificationType(String notificationType, Context context)
	{
		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		Editor editor = sharedPreferences.edit();
		editor.putString(SettingsManager.PREFERENCES_NOTIFICATION_TYPE_KEY, notificationType);
		editor.commit();
	}

	public void setNotificationValue(String notificationValue, Context context)
	{
		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		Editor editor = sharedPreferences.edit();

		if (getNotificationType().equals("time"))
		{
			editor.putString(SettingsManager.PREFERENCES_NOTIFICATION_PERIOD_KEY, notificationValue);
		} else if (getNotificationType().equals("distance"))
		{
			if (getUnits().equals(EU_UNITS))
			{
				editor.putString(SettingsManager.PREFERENCES_NOTIFICATION_EU_DISTANCE_KEY, notificationValue);
			} else
			{
				editor.putString(SettingsManager.PREFERENCES_NOTIFICATION_US_DISTANCE_KEY, notificationValue);
			}
			
		}
		editor.commit();
	}

	public void setNotificationInRaceValue(String notificationInRaceValue, Context context)
	{
		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		Editor editor = sharedPreferences.edit();

		if (getNotificationType().equals("time"))
		{
			editor.putString(SettingsManager.PREFERENCES_NOTIFICATION_PERIOD_IN_RACE_KEY, notificationInRaceValue);
		} else if (getNotificationType().equals("distance"))
		{
			if (getUnits().equals(EU_UNITS))
			{
				editor.putString(SettingsManager.PREFERENCES_NOTIFICATION_EU_DISTANCE_IN_RACE_KEY, notificationInRaceValue);
			} else
			{
				editor.putString(SettingsManager.PREFERENCES_NOTIFICATION_US_DISTANCE_IN_RACE_KEY, notificationInRaceValue);
			}
		}
		editor.commit();
	}

	public String getNotificationDetails()
	{
		String notificationDetails = "Interval: " + getNotificationValue() + " Show: ";
		if (isShowSpeedInsteadOfTempo())
		{
			notificationDetails += " speed";
		} else
		{
			notificationDetails += " tempo";
		}

		if (isShowBurnedCalories())
		{
			notificationDetails += ", calories";
		}

		return notificationDetails;
	}

	public void setCaloriesRequirement(float calories, Context context)
	{
		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		Editor editor = sharedPreferences.edit();
		editor.putFloat(SettingsManager.PREFERENCES_CALORIES_REQUIREMENT, calories);
		editor.commit();
	}

	public void setFatRequirement(float fat, Context context)
	{
		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		Editor editor = sharedPreferences.edit();
		editor.putFloat(SettingsManager.PREFERENCES_FAT_REQUIREMENT, fat);
		editor.commit();
	}

	public void setProteinRequirement(float protein, Context context)
	{
		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		Editor editor = sharedPreferences.edit();
		editor.putFloat(SettingsManager.PREFERENCES_PROTEIN_REQUIREMENT, protein);
		editor.commit();
	}

	public void setCarbohydratesRequirement(float carbohydrates, Context context)
	{
		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		Editor editor = sharedPreferences.edit();
		editor.putFloat(SettingsManager.PREFERENCES_CARBOHYDRATES_REQUIREMENT, carbohydrates);
		editor.commit();
	}
	
	public float getCaloriesRequirement()
	{
		return sharedPreferences.getFloat(SettingsManager.PREFERENCES_CALORIES_REQUIREMENT, 1600);
	}
	
	public float getFatRequirement()
	{
		return sharedPreferences.getFloat(SettingsManager.PREFERENCES_FAT_REQUIREMENT, 120);
	}
	
	public float getProteinRequirement()
	{
		return sharedPreferences.getFloat(SettingsManager.PREFERENCES_PROTEIN_REQUIREMENT, 120);
	}
	
	public float getCarbohydratesRequirement()
	{
		return sharedPreferences.getFloat(SettingsManager.PREFERENCES_CARBOHYDRATES_REQUIREMENT, 360);
	}
	
	public float getBMR(){
		float factor = 1;
		
		if (getUnits().equals(US_UNITS))
		{
			factor = 1 / GRAMS_PER_POUND;
		} 
		
		float height = getHeightValueInCentimeters();
		float weight = getWeightInKG();
		int age = getAge();
		boolean isMan = true;
		if (getSex().equals(getSexValuesTable()[1]))
		{
			isMan = false;
		}

		float BMR;

		if (isMan)
		{
			BMR = (float) ((9.99 * weight) + (6.25 * height) - (4.92 * age) + 5);
		} else
		{
			BMR = (float) (655 + (9.6 * weight) + (1.8 * height) - (4.7 * age));
		}
		
		return BMR;
	}
}
