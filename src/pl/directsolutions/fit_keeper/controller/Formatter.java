package pl.directsolutions.fit_keeper.controller;

import java.text.DecimalFormat;

import android.content.Context;

public class Formatter
{
	private static DecimalFormat df = new DecimalFormat("##.##");

	public static String formatTime(long timeInMillis)
	{
		Integer hours = 0, minutes = 0, seconds = 0;
		String wholeString = "";

		seconds = new Double(timeInMillis / 1000).intValue();
		minutes = new Double(seconds / 60).intValue();
		hours = new Double(minutes / 60).intValue();
		minutes = minutes - hours * 60;
		seconds = seconds - minutes * 60;

		DecimalFormat df = new DecimalFormat("##.##");

		if (hours > 0)
		{
			wholeString += (hours + "h ");
		}

		if (minutes > 0)
		{
			wholeString += (minutes + "min ");
		}

		if (seconds > 0)
		{
			wholeString += (seconds + "s");
		}

		return wholeString;
	}

	public static String formatDistance(float distance, String units)
	{
		String distanceString;
		if (units.equals(SettingsManager.US_UNITS))
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

		return distanceString;
	}

	public static String formatRapidity(float speed, boolean isShowSpeedInsteadOfTempo, String units)
	{
		String rapidity;
		if (isShowSpeedInsteadOfTempo)
		{
			if (units.equals(SettingsManager.US_UNITS))
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
			if (units.equals(SettingsManager.US_UNITS))
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

		return rapidity;
	}
}
