package pl.directsolutions.fit_keeper.augmented_reality;

import java.util.ArrayList;

import org.json.JSONObject;

import pl.directsolutions.fit_keeper.model.Place;

import android.util.Log;

public class DataHandler
{
	public ArrayList<Marker> markers = new ArrayList<Marker>();
	public static final int MAX_OBJECTS = 50;

	public DataHandler(ArrayList<Place> placesList)//String title, double latitude, double longitude, double elevation)
	{
		for (int i = 0; i < placesList.size(); i++)
		{
			Place place = placesList.get(i);
			Log.d("CREATE MARKER", i + " " + place.getLatitude() + " " + place.getLongitude());
			createMarker(place.getName(), place.getLatitude(), place.getLongitude(), 0, "");
		}
	}

	private Marker createMarker(String title, double latitude, double longitude, double elevation, String link)
	{
		PhysicalPlace refpt = new PhysicalPlace();
		Marker ma = new Marker();

		if (link != null && link.length() > 0)
		{
			ma.mOnPress = "webpage:" + java.net.URLDecoder.decode(link);
		}

		ma.mText = title;
		refpt.setLatitude(latitude);
		refpt.setLongitude(longitude);
		refpt.setAltitude(elevation);
		ma.mGeoLoc.setTo(refpt);
		if (markers.size() < MAX_OBJECTS)
			markers.add(ma);
		return ma;
	}

	public void setMarkers(ArrayList<Marker> markers)
	{
		this.markers = markers;
	}
}
