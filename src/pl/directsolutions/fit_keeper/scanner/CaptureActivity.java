/*
 * Copyright (C) 2008 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package pl.directsolutions.fit_keeper.scanner;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import java.io.IOException;
import java.util.Vector;
import java.util.regex.Pattern;

import pl.directsolutions.fit_keeper.R;
import pl.directsolutions.fit_keeper.controller.SettingsManager;
import pl.directsolutions.fit_keeper.view.ChooseAmountOfScannedActivity;

/**
 * The barcode reader activity itself. This is loosely based on the CameraPreview
 * example included in the Android SDK.
 *
 * @author dswitkin@google.com (Daniel Switkin)
 */
public final class CaptureActivity extends Activity implements SurfaceHolder.Callback
{
	private static final String TAG = "CaptureActivity";
	private static final Pattern COMMA_PATTERN = Pattern.compile(",");

	private static final float BEEP_VOLUME = 0.10f;
	private static final long VIBRATE_DURATION = 200L;

	private static final String PRODUCT_SEARCH_URL_PREFIX = "http://www.google";
	private static final String PRODUCT_SEARCH_URL_SUFFIX = "/m/products/scan";
	private static final String ZXING_URL = "http://zxing.appspot.com/scan";

	static final Vector<BarcodeFormat> PRODUCT_FORMATS;
	static final Vector<BarcodeFormat> ONE_D_FORMATS;
	static final Vector<BarcodeFormat> QR_CODE_FORMATS;
	static final Vector<BarcodeFormat> ALL_FORMATS;

	static
	{
		PRODUCT_FORMATS = new Vector<BarcodeFormat>(5);
		PRODUCT_FORMATS.add(BarcodeFormat.UPC_A);
		PRODUCT_FORMATS.add(BarcodeFormat.UPC_E);
		PRODUCT_FORMATS.add(BarcodeFormat.EAN_13);
		PRODUCT_FORMATS.add(BarcodeFormat.EAN_8);
		ONE_D_FORMATS = new Vector<BarcodeFormat>(PRODUCT_FORMATS.size() + 3);
		ONE_D_FORMATS.addAll(PRODUCT_FORMATS);
		ONE_D_FORMATS.add(BarcodeFormat.CODE_39);
		ONE_D_FORMATS.add(BarcodeFormat.CODE_128);
		ONE_D_FORMATS.add(BarcodeFormat.ITF);
		QR_CODE_FORMATS = new Vector<BarcodeFormat>(1);
		QR_CODE_FORMATS.add(BarcodeFormat.QR_CODE);
		ALL_FORMATS = new Vector<BarcodeFormat>(ONE_D_FORMATS.size() + QR_CODE_FORMATS.size());
		ALL_FORMATS.addAll(ONE_D_FORMATS);
		ALL_FORMATS.addAll(QR_CODE_FORMATS);
	}

	private enum Source
	{
		NATIVE_APP_INTENT, PRODUCT_SEARCH_LINK, ZXING_LINK, NONE
	}

	private CaptureActivityHandler handler;

	private ViewfinderView viewfinderView;
	private View statusView;
	private MediaPlayer mediaPlayer;
	private boolean hasSurface;
	private boolean playBeep;
	private boolean vibrate;
	private Source source;
	private Vector<BarcodeFormat> decodeFormats;
	private String characterSet;

	private final OnCompletionListener beepListener = new BeepListener();

	private final DialogInterface.OnClickListener aboutListener = new DialogInterface.OnClickListener()
	{
		public void onClick(DialogInterface dialogInterface, int i)
		{
			Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.zxing_url)));
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
			startActivity(intent);
		}
	};

	ViewfinderView getViewfinderView()
	{
		return viewfinderView;
	}

	public Handler getHandler()
	{
		return handler;
	}

	@Override
	public void onCreate(Bundle icicle)
	{
		super.onCreate(icicle);

		Window window = getWindow();
		window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.capture);

		CameraManager.init(getApplication());
		viewfinderView = (ViewfinderView) findViewById(R.id.viewfinder_view);
		statusView = findViewById(R.id.status_view);
		handler = null;
		hasSurface = false;
	}

	@Override
	protected void onResume()
	{
		super.onResume();

		SurfaceView surfaceView = (SurfaceView) findViewById(R.id.preview_view);
		SurfaceHolder surfaceHolder = surfaceView.getHolder();
		if (hasSurface)
		{
			// The activity was paused but not stopped, so the surface still exists. Therefore
			// surfaceCreated() won't be called, so init the camera here.
			initCamera(surfaceHolder);
		} else
		{
			// Install the callback and wait for surfaceCreated() to init the camera.
			surfaceHolder.addCallback(this);
			surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		}

		Intent intent = getIntent();
		String action = intent == null ? null : intent.getAction();
		String dataString = intent == null ? null : intent.getDataString();
		if (intent != null && action != null)
		{
			if (action.equals(Intents.Scan.ACTION))
			{
				// Scan the formats the intent requested, and return the result to the calling activity.
				source = Source.NATIVE_APP_INTENT;
				decodeFormats = parseDecodeFormats(intent);
				resetStatusView();
			} else if (dataString != null && dataString.contains(PRODUCT_SEARCH_URL_PREFIX) && dataString.contains(PRODUCT_SEARCH_URL_SUFFIX))
			{
				// Scan only products and send the result to mobile Product Search.
				source = Source.PRODUCT_SEARCH_LINK;
				decodeFormats = PRODUCT_FORMATS;
				resetStatusView();
			} else if (dataString != null && dataString.equals(ZXING_URL))
			{
				// Scan all formats and handle the results ourselves.
				// TODO: In the future we could allow the hyperlink to include a URL to send the results to.
				source = Source.ZXING_LINK;
				decodeFormats = null;
				resetStatusView();
			} else
			{
				// Scan all formats and handle the results ourselves (launched from Home).
				source = Source.NONE;
				decodeFormats = null;
				resetStatusView();
			}
			characterSet = intent.getStringExtra(Intents.Scan.CHARACTER_SET);
		} else
		{
			source = Source.NONE;
			decodeFormats = null;
			characterSet = null;
		}

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		playBeep = prefs.getBoolean(PreferencesActivity.KEY_PLAY_BEEP, true);
		vibrate = prefs.getBoolean(PreferencesActivity.KEY_VIBRATE, false);
		initBeepSound();
	}

	private static Vector<BarcodeFormat> parseDecodeFormats(Intent intent)
	{
		String scanFormats = intent.getStringExtra(Intents.Scan.SCAN_FORMATS);
		if (scanFormats != null)
		{
			Vector<BarcodeFormat> formats = new Vector<BarcodeFormat>();
			try
			{
				for (String format : COMMA_PATTERN.split(scanFormats))
				{
					formats.add(BarcodeFormat.valueOf(format));
				}
			} catch (IllegalArgumentException iae)
			{
				// ignore it then
			}
		}
		String decodeMode = intent.getStringExtra(Intents.Scan.MODE);
		if (decodeMode != null)
		{
			if (Intents.Scan.PRODUCT_MODE.equals(decodeMode))
			{
				return PRODUCT_FORMATS;
			}
			if (Intents.Scan.QR_CODE_MODE.equals(decodeMode))
			{
				return QR_CODE_FORMATS;
			}
			if (Intents.Scan.ONE_D_MODE.equals(decodeMode))
			{
				return ONE_D_FORMATS;
			}
		}
		return null;
	}

	@Override
	protected void onPause()
	{
		super.onPause();
		if (handler != null)
		{
			handler.quitSynchronously();
			handler = null;
		}
		CameraManager.get().closeDriver();
	}
	
	@Override
	public void onConfigurationChanged(Configuration config)
	{
		// Do nothing, this is to prevent the activity from being restarted when the keyboard opens.
		super.onConfigurationChanged(config);
	}

	public void surfaceCreated(SurfaceHolder holder)
	{
		if (!hasSurface)
		{
			hasSurface = true;
			initCamera(holder);
		}
	}

	public void surfaceDestroyed(SurfaceHolder holder)
	{
		hasSurface = false;
	}

	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
	{

	}

	/**
	 * A valid barcode has been found, so give an indication of success and show the results.
	 *
	 * @param rawResult The contents of the barcode.
	 * @param barcode   A greyscale bitmap of the camera data which was decoded.
	 */
	public void handleDecode(Result rawResult, Bitmap barcode)
	{
		playBeepSoundAndVibrate();
		Log.d("barcode found", "----- " + rawResult.getText());
		Intent intent = new Intent(CaptureActivity.this, ChooseAmountOfScannedActivity.class);
		intent.putExtra(SettingsManager.SCANNED_BARCODE_KEY, rawResult.getText());
		startActivity(intent);
		this.finish();
	}

	/**
	 * Creates the beep MediaPlayer in advance so that the sound can be triggered with the least
	 * latency possible.
	 */
	private void initBeepSound()
	{
		if (playBeep && mediaPlayer == null)
		{
			// The volume on STREAM_SYSTEM is not adjustable, and users found it too loud,
			// so we now play on the music stream.
			setVolumeControlStream(AudioManager.STREAM_MUSIC);
			mediaPlayer = new MediaPlayer();
			mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
			mediaPlayer.setOnCompletionListener(beepListener);

			AssetFileDescriptor file = getResources().openRawResourceFd(R.raw.beep);
			try
			{
				mediaPlayer.setDataSource(file.getFileDescriptor(), file.getStartOffset(), file.getLength());
				file.close();
				mediaPlayer.setVolume(BEEP_VOLUME, BEEP_VOLUME);
				mediaPlayer.prepare();
			} catch (IOException e)
			{
				mediaPlayer = null;
			}
		}
	}

	private void playBeepSoundAndVibrate()
	{
		if (playBeep && mediaPlayer != null)
		{
			mediaPlayer.start();
		}
		if (vibrate)
		{
			Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
			vibrator.vibrate(VIBRATE_DURATION);
		}
	}

	private void initCamera(SurfaceHolder surfaceHolder)
	{
		try
		{
			CameraManager.get().openDriver(surfaceHolder);
		} catch (Exception ioe)
		{
			ioe.printStackTrace();
		}
		if (handler == null)
		{
			boolean beginScanning = true; //lastResult == null;
			handler = new CaptureActivityHandler(this, decodeFormats, characterSet, beginScanning);
		}
	}

	private void resetStatusView()
	{
		statusView.setVisibility(View.VISIBLE);
		statusView.setBackgroundColor(getResources().getColor(R.color.status_view));
		viewfinderView.setVisibility(View.VISIBLE);

		TextView textView = (TextView) findViewById(R.id.status_text_view);
		textView.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
		textView.setTextSize(14.0f);
		textView.setText(R.string.msg_default_status);
	}

	public void drawViewfinder()
	{
		viewfinderView.drawViewfinder();
	}

	/**
	 * When the beep has finished playing, rewind to queue up another one.
	 */
	private static class BeepListener implements OnCompletionListener
	{
		public void onCompletion(MediaPlayer mediaPlayer)
		{
			mediaPlayer.seekTo(0);
		}
	}
}
