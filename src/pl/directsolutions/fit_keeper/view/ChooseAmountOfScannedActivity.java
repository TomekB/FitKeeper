package pl.directsolutions.fit_keeper.view;

import java.io.BufferedReader;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;

import pl.directsolutions.fit_keeper.R;
import pl.directsolutions.fit_keeper.controller.ProductsDatabaseManager;
import pl.directsolutions.fit_keeper.controller.SettingsManager;
import pl.directsolutions.fit_keeper.model.EatenProduct;
import pl.directsolutions.fit_keeper.model.Product;
import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

public class ChooseAmountOfScannedActivity extends Activity
{
	private static final float gramsPerPound = new Float(453.59237);
	private static final float mililitersPerOunce = new Float(28.413);
	private static DecimalFormat df = new DecimalFormat("###.#");

	private TextView barcodeTextView, productNameTextView, proteinTextView;
	private TextView carbohydratesTextView, fatTextView, caloriesTextView, header;
	private EditText amountEditText, caloriesEditText, fatEditText;
	private EditText carbohydratesEditText, nameEditText, proteinEditText;
	private EditText amountForInputEditText;
	private RadioButton liquidButton, foodButton;

	private Button addButton, cancelButton;
	private View foundView, notFoundView;

	private Product product, tempProduct;
	private String barcode;
	private boolean barcodeFound;

	private SettingsManager settingsManager;
	private boolean USunits, isFood;
	private String unitsString;

	@Override
	public void onCreate(Bundle icicle)
	{
		super.onCreate(icicle);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.choose_amount);

		isFood = true;
		USunits = false;
		unitsString = "gram";

		settingsManager = SettingsManager.getInstance(this);
		if (settingsManager.getUnits().equals(SettingsManager.EU_UNITS))
		{
			USunits = false;
		} else
		{
			USunits = true;
		}

		barcodeTextView = (TextView) findViewById(R.id.barcodeValueTextView);
		header = (TextView) findViewById(R.id.chooseAmountHeaderTextView);

		productNameTextView = (TextView) findViewById(R.id.productNameValueTextView);
		proteinTextView = (TextView) findViewById(R.id.proteinValueTextView);
		carbohydratesTextView = (TextView) findViewById(R.id.carbohydratesValueTextView);
		fatTextView = (TextView) findViewById(R.id.fatValueTextView);
		caloriesTextView = (TextView) findViewById(R.id.caloriesValueTextView);
		amountEditText = (EditText) findViewById(R.id.amountValueEditText);

		caloriesEditText = (EditText) findViewById(R.id.caloriesValueEditText);
		proteinEditText = (EditText) findViewById(R.id.proteinValueEditText);
		carbohydratesEditText = (EditText) findViewById(R.id.carbohydratesValueEditText);
		fatEditText = (EditText) findViewById(R.id.fatValueEditText);
		nameEditText = (EditText) findViewById(R.id.productNameEditText);
		amountForInputEditText = (EditText) findViewById(R.id.amountValuesForInputEditBox);
		amountForInputEditText.addTextChangedListener(new TextWatcher()
		{
			@Override
			public void afterTextChanged(Editable s)
			{
				setHints();
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after)
			{
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count)
			{
			}
		});
		amountForInputEditText.setText("100");

		foundView = (View) findViewById(R.id.barcode_found_view);
		notFoundView = (View) findViewById(R.id.barcode_not_found_view);

		addButton = (Button) findViewById(R.id.addButton);
		cancelButton = (Button) findViewById(R.id.cancelButton);

		liquidButton = (RadioButton) findViewById(R.id.radioButtonRight);
		liquidButton.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				isFood = false;
				if (USunits)
				{
					unitsString = "oz";
				} else
				{
					unitsString = "ml";
				}
				setHints();
			}
		});

		foodButton = (RadioButton) findViewById(R.id.radioButtonLeft);
		foodButton.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				isFood = true;
				if (USunits)
				{
					unitsString = "lb";
				} else
				{
					unitsString = "gram";
				}
				setHints();
			}
		});
		foodButton.setChecked(true);

		if (USunits)
		{
			foodButton.setText("lb");
			liquidButton.setText("oz");
		} else
		{
			foodButton.setText("gram");
			liquidButton.setText("ml");
		}

		Bundle extras = getIntent().getExtras();
		if (extras != null)
		{
			barcode = extras.getString(SettingsManager.SCANNED_BARCODE_KEY);
			barcodeTextView.setText(barcode);
		}

		ProductsDatabaseManager databaseManager = new ProductsDatabaseManager(this);
		//		Cursor cursor;
		databaseManager.open();
		try
		{
			//id found

			product = databaseManager.getProductByBarcode(barcode);
			barcodeFound = true;
			foundView.setVisibility(View.VISIBLE);
			prepareData();
		} catch (SQLException exception)
		{
			//id not found
			exception.printStackTrace();
			barcodeFound = false;
			notFoundView.setVisibility(View.VISIBLE);
			addButton.setText(getApplicationContext().getString(R.string.add_to_database_button_string));
			header.setText(getApplicationContext().getString(R.string.barcode_not_found_header));
		}
		databaseManager.close();

		addButton.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				Context context = getApplicationContext();
				int duration = Toast.LENGTH_SHORT;

				if (barcodeFound)
				{
					addToDiet();
				} else
				{
					try
					{
						addToDatabase();
						notFoundView.setVisibility(View.GONE);
						foundView.setVisibility(View.VISIBLE);
						product = tempProduct;
						barcodeFound = true;
						prepareData();

						CharSequence text = "Succesfully added to database";
						Toast toast = Toast.makeText(context, text, duration);
						toast.show();

					} catch (Exception e)
					{
						e.printStackTrace();

						CharSequence text = "Check typed in values";
						Toast toast = Toast.makeText(context, text, duration);
						toast.show();
					}
				}
			}
		});

		cancelButton.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				finish();
			}
		});
	}

	private void prepareData()
	{
		float factor = 1;
		String postfix = " per";
		if (USunits)
		{
			if (isFood)
			{
				factor = gramsPerPound / 100;
				postfix += " 1 lb";
			} else
			{
				factor = mililitersPerOunce / 100;
				postfix += " 1 oz";
			}
		} else
		{
			if (isFood)
			{
				postfix += " 100 g";
			} else
			{
				postfix += " 100 ml";
			}
		}

		productNameTextView.setText(product.getName());
		proteinTextView.setText(df.format(product.getProtein() * factor) + " " + unitsString + postfix);
		carbohydratesTextView.setText(df.format(product.getCarbohydrates() * factor) + " " + unitsString + postfix);
		fatTextView.setText(df.format(product.getFat() * factor) + " " + unitsString + postfix);
		caloriesTextView.setText(df.format(product.getCalories() * factor) + postfix);
		addButton.setText(getApplicationContext().getString(R.string.add_to_diet_button_string));
		header.setText(getApplicationContext().getString(R.string.barcode_found_header));
		amountEditText.setHint(getApplicationContext().getString(R.string.value_per_hint1) + " " + unitsString);
	}

	private void addToDiet()
	{
		Calendar date = Calendar.getInstance();
		date.setTimeInMillis(System.currentTimeMillis());
		String productPointsFileName = date.get(Calendar.DAY_OF_MONTH) + "_" + (date.get(Calendar.MONTH) + 1) + "_" + date.get(Calendar.YEAR);
		ArrayList<EatenProduct> eatenProducts = new ArrayList<EatenProduct>();

		float protein, carbohydrates, fats, calories;
		long time;
		int amount;
		String name;
		boolean isFood;

		try
		{
			FileInputStream inputStream = openFileInput(productPointsFileName);
			FileDescriptor descriptor = null;
			try
			{
				descriptor = inputStream.getFD();
			} catch (IOException e1)
			{
				e1.printStackTrace();
			}
			FileReader fileReader = new FileReader(descriptor);
			BufferedReader reader = new BufferedReader(fileReader);
			String line;
			String array[];

			try
			{
				while ((line = reader.readLine()) != null)
				{
					array = line.split(" ");
					amount = Integer.parseInt(array[0].trim());
					protein = Float.parseFloat(array[1].trim());
					carbohydrates = Float.parseFloat(array[2].trim());
					fats = Float.parseFloat(array[3].trim());
					calories = Float.parseFloat(array[4].trim());
					time = Long.parseLong(array[5].trim());
					isFood = Boolean.parseBoolean(array[6].trim());
					name = array[7];
					for (int i = 8; i < array.length; i++)
					{
						name += " " + array[i];
					}

					EatenProduct product = new EatenProduct(amount, protein, carbohydrates, fats, calories, time, isFood, name);
					eatenProducts.add(product);
				}
			} catch (IOException e)
			{
				e.printStackTrace();
			}

		} catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}

		//add my product

		float amountToCalculate = Float.parseFloat(amountEditText.getText().toString());
		if (product.isFood())
		{
			if (USunits)
			{
				amountToCalculate *= gramsPerPound;
			}
		} else
		{
			if (USunits)
			{
				amountToCalculate *= mililitersPerOunce;
			}
		}
		amount = new Double(amountToCalculate).intValue();
		time = System.currentTimeMillis();
		protein = new Float(amount) * product.getProtein() / 100;
		carbohydrates = new Float(amount) * product.getCarbohydrates() / 100;
		fats = new Float(amount) * product.getFat() / 100;
		calories = new Float(amount) * product.getCalories() / 100;
		name = product.getName();
		isFood = product.isFood();
		EatenProduct product = new EatenProduct(amount, protein, carbohydrates, fats, calories, time, isFood, name);
		eatenProducts.add(product);

		try
		{
			FileOutputStream fos = openFileOutput(productPointsFileName, Context.MODE_PRIVATE);
			PrintWriter out = new PrintWriter(fos);

			for (int i = 0; i < eatenProducts.size(); i++)
			{
				product = eatenProducts.get(i);
				out.print(product.getAmount() + " ");
				out.print(product.getProtein() + " ");
				out.print(product.getCarbohydrates() + " ");
				out.print(product.getFat() + " ");
				out.print(product.getCalories() + " ");
				out.print(product.getTime() + " ");
				out.print(product.isFood() + " ");
				out.println(product.getName());
			}

			out.flush();
			out.close();
		} catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		
		CharSequence text = "Succesfully added to diet";
		Toast toast = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT);
		toast.show();
	}

	private void addToDatabase() throws NumberFormatException
	{
		String name = nameEditText.getText().toString().trim();
		float amountToCalculate = Float.parseFloat(amountForInputEditText.getText().toString()); //check
		float calories = Float.parseFloat(caloriesEditText.getText().toString()); //catch exception
		float fat = Float.parseFloat(fatEditText.getText().toString());
		float protein = Float.parseFloat(proteinEditText.getText().toString());
		float carbohydrates = Float.parseFloat(carbohydratesEditText.getText().toString());

		//		Log.d("sum", carbohydrates + protein + fat + "");
		//		Log.d("amount", amountToCalculate * 1.01 + "");
		if (carbohydrates + protein + fat > amountToCalculate * 1.01)
		{
			throw new NumberFormatException();
		}

		if (isFood)
		{
			if (USunits)
			{
				amountToCalculate *= gramsPerPound;
			}
		} else
		{
			if (USunits)
			{
				amountToCalculate *= mililitersPerOunce;
			}
		}

		calories = calories * 100 / amountToCalculate;
		fat = fat * 100 / amountToCalculate;
		protein = protein * 100 / amountToCalculate;
		carbohydrates = carbohydrates * 100 / amountToCalculate;
		Log.d("save product", "->" + name + "<=");

		tempProduct = new Product(name, isFood, calories, barcode, fat, protein, carbohydrates);

		ProductsDatabaseManager databaseManager = new ProductsDatabaseManager(this);
		databaseManager.open();
		databaseManager.insertWorkout(tempProduct);
		databaseManager.close();
	}

	private void setHints()
	{
		String hint = getApplicationContext().getString(R.string.value_per_hint1) + " " + unitsString + " " + getApplicationContext().getString(R.string.value_per_hint2)
				+ " " + amountForInputEditText.getText().toString() + " " + unitsString;
		caloriesEditText.setHint(getApplicationContext().getString(R.string.value_per_hint2) + " " + amountForInputEditText.getText().toString() + " " + unitsString);
		proteinEditText.setHint(hint);
		carbohydratesEditText.setHint(hint);
		fatEditText.setHint(hint);
	}
}
