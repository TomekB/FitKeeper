package pl.directsolutions.fit_keeper.view;

import pl.directsolutions.fit_keeper.R;
import pl.directsolutions.fit_keeper.scanner.CaptureActivity;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;

public class MainActivity extends Activity
{

	private Button button1, button2, button3, button4;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.main);

		button1 = (Button) findViewById(R.id.MainButton01);
		button1.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				//scanning barcode
				Intent intent = new Intent(MainActivity.this, CaptureActivity.class);
				startActivity(intent);
			}
		});

		button2 = (Button) findViewById(R.id.MainButton02);
		button2.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				//searching for food 
				Intent intent = new Intent(MainActivity.this, ManualProductSearchActivity.class);
				startActivity(intent);
			}
		});

		button3 = (Button) findViewById(R.id.MainButton03);
		button3.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				//Analize
				Intent intent = new Intent(MainActivity.this, DietAnalyzeActivity.class);
				startActivity(intent);
			}
		});

		button4 = (Button) findViewById(R.id.MainButton04);
		button4.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				//Training
				Intent intent = new Intent(MainActivity.this, TrainActivity.class);
				startActivity(intent);
			}
		});
	}
}