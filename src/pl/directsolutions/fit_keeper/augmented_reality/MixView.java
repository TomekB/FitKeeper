package pl.directsolutions.fit_keeper.augmented_reality;

import static android.hardware.SensorManager.SENSOR_DELAY_GAME;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import pl.directsolutions.fit_keeper.model.Place;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.hardware.Camera;
import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

public class MixView extends Activity implements SensorEventListener, LocationListener
{
	Button backButton, mapButton;

	CameraSurface camScreen;
	AugmentedView augScreen;

//	public static String name;
//	public static double latitude, longitude, altitude;

	static boolean isInited = false;
	static MixContext ctx;
	static PaintScreen dWindow;
	static DataView view;
	Thread downloadThread;

	float RTmp[] = new float[9];
	float R[] = new float[9];
	float I[] = new float[9];
	float grav[] = new float[3];
	float mag[] = new float[3];

	private SensorManager sensorMgr;
	private List<Sensor> sensors;
	private Sensor sensorGrav, sensorMag;
	public static LocationManager locationMgr;
	boolean isGpsEnabled = false;

	int rHistIdx = 0;
	Matrix tempR = new Matrix();
	Matrix finalR = new Matrix();
	Matrix smoothR = new Matrix();
	Matrix histR[] = new Matrix[60];
	Matrix m1 = new Matrix();
	Matrix m2 = new Matrix();
	Matrix m3 = new Matrix();
	Matrix m4 = new Matrix();

	private WakeLock mWakeLock;

	private boolean fError = false;

	static boolean isZoombarVisible = false;
	static String zoomLevel;
	static int zoomProgress;
	static boolean zoomChanging = false;
	public static final String TAG = "Mixare";

	public static double GPS_LONGITUDE = 0;
	public static double GPS_LATITUDE = 0;
	public static float GPS_ACURRACY = 0;
	public static String GPS_LAST_FIX = "";
	public static double GPS_ALTITUDE = 0;
	public static float GPS_SPEED = 0;
	public static String GPS_ALL = "";

	/*Vectors to store the titles and URLs got from Json for the alternative list view */
	public Vector<String> listDataVector;
	public Vector<String> listURL;
	private static ArrayList<Place> placesList;

	/*string to name & access the preference file in the internal storage*/
	public static final String PREFS_NAME = "MyPrefsFileForMenuItems";

	public void killOnError() throws Exception
	{
		if (fError)
			throw new Exception();
	}

	public void repaint()
	{
//		altitude=0;
		view = new DataView(ctx, placesList);//name, latitude, longitude, 0);//altitude);
		dWindow = new PaintScreen();
	}

	public static void addPlaces(ArrayList<Place> places){
		placesList = new ArrayList<Place>();
		placesList.addAll(places);
		Log.d("MIX VIEW", placesList.size()+"");
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		try
		{
			final PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
			this.mWakeLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "My Tag");
			locationMgr = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
			locationMgr.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 10, this);

			killOnError();
			requestWindowFeature(Window.FEATURE_NO_TITLE);

			FrameLayout FL = new FrameLayout(this);
			FL.setMinimumWidth(3000);
			FL.setPadding(10, 10, 10, 10);

			camScreen = new CameraSurface(this);
			augScreen = new AugmentedView(this);
			setContentView(camScreen);

			addContentView(augScreen, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

			View mainscreen = getLayoutInflater().inflate(pl.directsolutions.fit_keeper.R.layout.buttons, null, false);
			ViewGroup.LayoutParams generalLayoutParam = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
			addContentView(mainscreen, generalLayoutParam);

			addContentView(FL, new FrameLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT, Gravity.BOTTOM));

			backButton = (Button) findViewById(pl.directsolutions.fit_keeper.R.id.buttons_back_button);
			backButton.setText("   BACK   ");
			backButton.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					finish();
				}
			});

			mapButton = (Button) findViewById(pl.directsolutions.fit_keeper.R.id.buttons_map_button);
			mapButton.setText("   MAP   ");
			mapButton.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					AugmentedMap.setPlaces(placesList);
					Intent intent = new Intent(MixView.this, AugmentedMap.class);
					startActivity(intent);
				}
			});

			if (!isInited)
			{
				ctx = new MixContext(this);
				dWindow = new PaintScreen();
				view = new DataView(ctx, placesList);//name, latitude, longitude,  0);//altitude);
				isInited = true;
			}

			if (ctx.isActualLocation() == false)
			{
				Toast.makeText(this, getString(view.CONNECITON_GPS_DIALOG_TEXT), Toast.LENGTH_LONG).show();
			}

		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

	@Override
	protected void onPause()
	{
		super.onPause();

		try
		{
			this.mWakeLock.release();

			try
			{
				sensorMgr.unregisterListener(this, sensorGrav);
			} catch (Exception ignore)
			{
			}
			try
			{
				sensorMgr.unregisterListener(this, sensorMag);
			} catch (Exception ignore)
			{
			}
			sensorMgr = null;

			try
			{
				locationMgr.removeUpdates(this);
			} catch (Exception ignore)
			{
			}
			try
			{
				locationMgr = null;
			} catch (Exception ignore)
			{
			}

			if (fError)
			{
				finish();
			}
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

	@Override
	protected void onResume()
	{
		super.onResume();

		try
		{
			this.mWakeLock.acquire();

			killOnError();
			ctx.mixView = this;
			view = new DataView(ctx, placesList);// name, latitude, longitude,  0);//altitude);
			view.doStart();
			view.clearEvents();

			double angleX, angleY;

			angleX = Math.toRadians(-90);
			m1.set(1f, 0f, 0f, 0f, (float) Math.cos(angleX), (float) -Math.sin(angleX), 0f, (float) Math.sin(angleX), (float) Math.cos(angleX));

			angleX = Math.toRadians(-90);
			angleY = Math.toRadians(-90);
			m2.set(1f, 0f, 0f, 0f, (float) Math.cos(angleX), (float) -Math.sin(angleX), 0f, (float) Math.sin(angleX), (float) Math.cos(angleX));
			m3.set((float) Math.cos(angleY), 0f, (float) Math.sin(angleY), 0f, 1f, 0f, (float) -Math.sin(angleY), 0f, (float) Math.cos(angleY));

			m4.toIdentity();

			for (int i = 0; i < histR.length; i++)
			{
				histR[i] = new Matrix();
			}

			sensorMgr = (SensorManager) getSystemService(SENSOR_SERVICE);

			sensors = sensorMgr.getSensorList(Sensor.TYPE_ACCELEROMETER);
			if (sensors.size() > 0)
			{
				sensorGrav = sensors.get(0);
			}

			sensors = sensorMgr.getSensorList(Sensor.TYPE_MAGNETIC_FIELD);
			if (sensors.size() > 0)
			{
				sensorMag = sensors.get(0);
			}

			sensorMgr.registerListener(this, sensorGrav, SENSOR_DELAY_GAME);
			sensorMgr.registerListener(this, sensorMag, SENSOR_DELAY_GAME);

			try
			{
				Criteria c = new Criteria();

				c.setAccuracy(Criteria.ACCURACY_FINE);
				//c.setBearingRequired(true);

				locationMgr = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
				locationMgr.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 10, this);

				String bestP = locationMgr.getBestProvider(c, true);
				isGpsEnabled = locationMgr.isProviderEnabled(bestP);

				/*defaulting to our place*/
				Location hardFix = new Location("reverseGeocoded");
				hardFix.setLatitude(46.47122383117541);
				hardFix.setLongitude(11.260278224944742);
				hardFix.setAltitude(300);

				try
				{
					ctx.curLoc = new Location(locationMgr.getLastKnownLocation(bestP));
				} catch (Exception ex2)
				{
					ctx.curLoc = new Location(hardFix);
				}

				GeomagneticField gmf = new GeomagneticField((float) ctx.curLoc.getLatitude(), (float) ctx.curLoc.getLongitude(), (float) ctx.curLoc.getAltitude(),
						System.currentTimeMillis());

				angleY = Math.toRadians(-gmf.getDeclination());
				m4.set((float) Math.cos(angleY), 0f, (float) Math.sin(angleY), 0f, 1f, 0f, (float) -Math.sin(angleY), 0f, (float) Math.cos(angleY));
				ctx.declination = gmf.getDeclination();

			} catch (Exception ex)
			{
				Log.d("mixare", "GPS Initialize Error", ex);
			}

		} catch (Exception ex)
		{
			ex.printStackTrace();

			try
			{
				if (sensorMgr != null)
				{
					sensorMgr.unregisterListener(this, sensorGrav);
					sensorMgr.unregisterListener(this, sensorMag);
					sensorMgr = null;
				}

				if (locationMgr != null)
				{
					locationMgr.removeUpdates(this);
					locationMgr = null;
				}
			} catch (Exception ignore)
			{
			}
		}
	}

	//*************************************************************************************************
	public void onSensorChanged(SensorEvent evt)
	{
		try
		{
			killOnError();

			if (evt.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
			{
				grav[0] = evt.values[0];
				grav[1] = evt.values[1];
				grav[2] = evt.values[2];

				augScreen.postInvalidate();
			} else if (evt.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
			{
				mag[0] = evt.values[0];
				mag[1] = evt.values[1];
				mag[2] = evt.values[2];

				augScreen.postInvalidate();
			}

			SensorManager.getRotationMatrix(RTmp, I, grav, mag);
			SensorManager.remapCoordinateSystem(RTmp, SensorManager.AXIS_X, SensorManager.AXIS_MINUS_Z, R);

			tempR.set(R[0], R[1], R[2], R[3], R[4], R[5], R[6], R[7], R[8]);

			finalR.toIdentity();
			finalR.prod(m4);
			finalR.prod(m1);
			finalR.prod(tempR);
			finalR.prod(m3);
			finalR.prod(m2);
			finalR.invert();

			histR[rHistIdx].set(finalR);
			rHistIdx++;
			if (rHistIdx >= histR.length)
				rHistIdx = 0;

			smoothR.set(0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f);
			for (int i = 0; i < histR.length; i++)
			{
				smoothR.add(histR[i]);
			}
			smoothR.mult(1 / (float) histR.length);

			synchronized (ctx.rotationM)
			{
				ctx.rotationM.set(smoothR);
			}
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

	public void onProviderDisabled(String provider)
	{
		isGpsEnabled = locationMgr.isProviderEnabled(LocationManager.GPS_PROVIDER);
	}

	public void onProviderEnabled(String provider)
	{
		isGpsEnabled = locationMgr.isProviderEnabled(LocationManager.GPS_PROVIDER);
	}

	public void onStatusChanged(String provider, int status, Bundle extras)
	{

	}

	public void onLocationChanged(Location location)
	{
		try
		{
			killOnError();
			if (LocationManager.GPS_PROVIDER.equals(location.getProvider()))
			{
				synchronized (ctx.curLoc)
				{
					ctx.curLoc = location;
				}
				isGpsEnabled = true;
			}
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

	public void onAccuracyChanged(Sensor sensor, int accuracy)
	{
	}
}

//*******************************************************************************************************
class CameraSurface extends SurfaceView implements SurfaceHolder.Callback
{
	MixView app;
	SurfaceHolder holder;
	Camera camera;

	CameraSurface(Context context)
	{
		super(context);

		try
		{
			app = (MixView) context;
			holder = getHolder();
			holder.addCallback(this);
			holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		} catch (Exception ex)
		{

		}
	}

	public void surfaceCreated(SurfaceHolder holder)
	{
		try
		{
			if (camera != null)
			{
				try
				{
					camera.stopPreview();
				} catch (Exception ignore)
				{
				}
				try
				{
					camera.release();
				} catch (Exception ignore)
				{
				}
				camera = null;
			}

			camera = Camera.open();
			camera.setPreviewDisplay(holder);
		} catch (Exception ex)
		{
			try
			{
				if (camera != null)
				{
					try
					{
						camera.stopPreview();
					} catch (Exception ignore)
					{
					}
					try
					{
						camera.release();
					} catch (Exception ignore)
					{
					}
					camera = null;
				}
			} catch (Exception ignore)
			{

			}
		}
	}

	public void surfaceDestroyed(SurfaceHolder holder)
	{
		try
		{
			if (camera != null)
			{
				try
				{
					camera.stopPreview();
				} catch (Exception ignore)
				{
				}
				try
				{
					camera.release();
				} catch (Exception ignore)
				{
				}
				camera = null;
			}
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

	public void surfaceChanged(SurfaceHolder holder, int format, int w, int h)
	{
		try
		{
			Camera.Parameters parameters = camera.getParameters();
			try
			{
				List<Camera.Size> supportedSizes = null;
				supportedSizes = Compatibility.getSupportedPreviewSizes(parameters);

				Iterator<Camera.Size> itr = supportedSizes.iterator();
				while (itr.hasNext())
				{
					Camera.Size element = itr.next();
					element.width -= w;
					element.height -= h;
				}

				int preferredSizeIndex = 0;
				int checkWidth = 0;
				int bestDistance = Integer.MAX_VALUE;
				for (int i = 0; i < supportedSizes.size() - 1; i++)
				{
					if (supportedSizes.get(i).width == 0)
					{
						preferredSizeIndex = i;
					} else
					{
						if (supportedSizes.get(i).width < 0)
							checkWidth = (supportedSizes.get(i).width) * (-1);
						else
							checkWidth = supportedSizes.get(i).width;

						if (checkWidth < bestDistance)
						{
							bestDistance = checkWidth;
							preferredSizeIndex = i;
						}
					}
				}

				parameters.setPreviewSize(w + supportedSizes.get(preferredSizeIndex).width, h + supportedSizes.get(preferredSizeIndex).height);
			} catch (Exception ex)
			{
				parameters.setPreviewSize(480, 320);
			}

			camera.setParameters(parameters);
			camera.startPreview();
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}
}

class AugmentedView extends View
{
	MixView app;

	public AugmentedView(Context context)
	{
		super(context);

		try
		{
			app = (MixView) context;
			app.killOnError();

		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

	@Override
	protected void onDraw(Canvas canvas)
	{
		try
		{
			app.killOnError();

			MixView.dWindow.setWidth(canvas.getWidth());
			MixView.dWindow.setHeight(canvas.getHeight());
			MixView.dWindow.setCanvas(canvas);

			if (!MixView.view.isInited())
			{
				MixView.view.init(MixView.dWindow.getWidth(), MixView.dWindow.getHeight());
			}

			MixView.view.draw(MixView.dWindow);
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}
}
