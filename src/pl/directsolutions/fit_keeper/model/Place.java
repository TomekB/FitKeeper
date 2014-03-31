package pl.directsolutions.fit_keeper.model;

import java.util.Calendar;

public class Place
{
	private int id;
	private String name;
	private double latitude;
	private double longitude;
	private Calendar date;
	private String distanceString;

	public Place(String name, double latitude, double longitude, Calendar date, String distanceString)
	{
		this.name = name;
		this.latitude = latitude;
		this.longitude = longitude;
		this.date = date;
		this.distanceString = distanceString;
	}

	public void setId(int id)
	{
		this.id = id;
	}

	public int getId()
	{
		return id;
	}

	public String getName()
	{
		return name;
	}

	public double getLatitude()
	{
		return latitude;
	}

	public double getLongitude()
	{
		return longitude;
	}
	
	public Calendar getDate()
	{
		return date;
	}

	public String getDateString()
	{
		return date.get(Calendar.DAY_OF_MONTH) + "." + (date.get(Calendar.MONTH) + 1) + "." + date.get(Calendar.YEAR);
	}
	
	public String getDistanceString()
	{
		return distanceString;
	}

}
