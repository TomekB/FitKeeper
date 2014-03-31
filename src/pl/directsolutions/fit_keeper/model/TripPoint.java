package pl.directsolutions.fit_keeper.model;

public class TripPoint {
	private long time;
	private Double longitude,latitude;
	private float distance=0;

	public TripPoint(double longitude, double latitude, long time){
		this.time=time;
		this.longitude=longitude;
		this.latitude=latitude;
	}
	
	public TripPoint(double longitude, double latitude, long time, float distance){
		this.time=time;
		this.longitude=longitude;
		this.latitude=latitude;
		this.distance=distance;
	}
	
	public void setDistance(float distance){
		this.distance=distance;
	}
	
	public long getTime(){
		return time;
	}
	
	public Double getLongitude(){
		return longitude;
	}
	
	public Double getLatitude(){
		return latitude;
	}
	
	public float getDistance(){
		return distance;
	}
}
