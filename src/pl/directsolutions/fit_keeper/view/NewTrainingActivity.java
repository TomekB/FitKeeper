package pl.directsolutions.fit_keeper.view;

import pl.directsolutions.fit_keeper.R;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;

public class NewTrainingActivity extends Activity
{
	private Button newWorkoutButton;
	private Button raceAgainstYourselfButton;

	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.train_new);

		newWorkoutButton = (Button) findViewById(R.id.TrainNewButton01);
		newWorkoutButton.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				Intent intent = new Intent(NewTrainingActivity.this, NewWorkoutActivity.class);
				startActivity(intent);
			}
		});

		raceAgainstYourselfButton = (Button) findViewById(R.id.TrainNewButton02);
		raceAgainstYourselfButton.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				Intent intent = new Intent(NewTrainingActivity.this, RaceAgainstYourselfListActivity.class);
				startActivity(intent);
			}
		});
	}
}