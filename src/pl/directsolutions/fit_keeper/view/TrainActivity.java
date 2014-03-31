package pl.directsolutions.fit_keeper.view;

import pl.directsolutions.fit_keeper.R;
import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.widget.TabHost;

public class TrainActivity extends TabActivity {
/** Called when the activity is first created. */

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.train_activity);
		
		TabHost tabHost = getTabHost();
		TabHost.TabSpec spec;
		Intent intent;
		
		intent = new Intent().setClass(this, NewTrainingActivity.class);
		spec = tabHost.newTabSpec("New").setIndicator("New").setContent(intent);
		tabHost.addTab(spec);
		
		intent = new Intent().setClass(this, TrainHistoryActivity.class);
		spec = tabHost.newTabSpec("History").setIndicator("History").setContent(intent);
		tabHost.addTab(spec);
		
		intent = new Intent().setClass(this, TrainSettingsActivity.class);
		spec = tabHost.newTabSpec("Settings").setIndicator("Settings").setContent(intent);
		tabHost.addTab(spec);
		
		intent = new Intent().setClass(this, TrainHelpActivity.class);
		spec = tabHost.newTabSpec("Help").setIndicator("Help").setContent(intent);
		tabHost.addTab(spec);
		
		tabHost.setCurrentTab(0);
	}
}