package pl.directsolutions.fit_keeper.view;

import java.text.DecimalFormat;

import pl.directsolutions.fit_keeper.R;
import pl.directsolutions.fit_keeper.controller.SettingsManager;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;

public class ConfigureDietActivity extends Activity
{
	private static final float gramsPerPound = new Float(453.59237);
	private EditText caloriesEditText, fatEditText;
	private EditText carbohydratesEditText, proteinEditText;
	private static DecimalFormat df = new DecimalFormat("###.00");
	private Button calculateButton, saveButton;

	private SettingsManager settingsManager;
	private boolean USunits;

	@Override
	public void onCreate(Bundle icicle)
	{
		super.onCreate(icicle);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.configure_diet);
		settingsManager = SettingsManager.getInstance(ConfigureDietActivity.this);
		USunits = false;

		settingsManager = SettingsManager.getInstance(this);
		if (settingsManager.getUnits().equals(SettingsManager.EU_UNITS))
		{
			USunits = false;
		} else
		{
			USunits = true;
		}

		caloriesEditText = (EditText) findViewById(R.id.caloriesValueEditText);
		proteinEditText = (EditText) findViewById(R.id.proteinValueEditText);
		carbohydratesEditText = (EditText) findViewById(R.id.carbohydratesValueEditText);
		fatEditText = (EditText) findViewById(R.id.fatValueEditText);

		calculateButton = (Button) findViewById(R.id.calculateButton);
		saveButton = (Button) findViewById(R.id.saveButton);

		calculateButton.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				calculateValues();
			}
		});

		saveButton.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				save();
				finish();
			}
		});

		prepareData();
	}

	private void prepareData()
	{
		String hint;
		float factor = 1;

		if (USunits)
		{
			hint = " lb";
			factor = 1 / gramsPerPound;
		} else
		{
			hint = " grams";
		}

		caloriesEditText.setHint(new String(df.format(settingsManager.getCaloriesRequirement())).replace(",", "."));
		proteinEditText.setHint(new String(df.format(settingsManager.getProteinRequirement() * factor) + hint).replace(",", "."));
		carbohydratesEditText.setHint(new String(df.format(settingsManager.getCarbohydratesRequirement() * factor) + hint).replace(",", "."));
		fatEditText.setHint(new String(df.format(settingsManager.getFatRequirement() * factor) + hint).replace(",", "."));
	}

	private void save()
	{
		float factor = 1;
		if (USunits)
		{
			factor = gramsPerPound;
		}

		try
		{
			float calories = Float.parseFloat(caloriesEditText.getText().toString());
			settingsManager.setCaloriesRequirement(calories, ConfigureDietActivity.this);
		} catch (Exception e)
		{
			e.printStackTrace();
		}

		try
		{
			float fat = Float.parseFloat(fatEditText.getText().toString());
			fat = fat * factor;
			settingsManager.setFatRequirement(fat, ConfigureDietActivity.this);
		} catch (Exception e)
		{
			e.printStackTrace();
		}

		try
		{
			float protein = Float.parseFloat(proteinEditText.getText().toString());
			protein = protein * factor;
			settingsManager.setProteinRequirement(protein, ConfigureDietActivity.this);
		} catch (Exception e)
		{
			e.printStackTrace();
		}

		try
		{
			float carbohydrates = Float.parseFloat(carbohydratesEditText.getText().toString());
			carbohydrates = carbohydrates * factor;
			settingsManager.setCarbohydratesRequirement(carbohydrates, ConfigureDietActivity.this);
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private void calculateValues()
	{
		float factor = 1;
		if (USunits)
		{
			factor = 1 / gramsPerPound;
		} 
		
		float height = settingsManager.getHeightValueInCentimeters();
		float weight = settingsManager.getWeightInKG();
		int age = settingsManager.getAge();
		boolean isMan = true;
		if (settingsManager.getSex().equals(settingsManager.getSexValuesTable()[1]))
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

		float calories = BMR;
		float proteins = (float) ((0.3 * calories) / 4);
		float carbohydrates = (float) ((0.55 * calories) / 4);
		float fats = (float) ((0.15 * calories) / 9);

		caloriesEditText.setText(new String(df.format(calories)).replace(",", "."));
		proteinEditText.setText(new String(df.format(proteins*factor)).replace(",", "."));
		carbohydratesEditText.setText(new String(df.format(carbohydrates*factor)).replace(",", "."));
		fatEditText.setText(new String(df.format(fats*factor)).replace(",", "."));
	}
}