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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import pl.directsolutions.fit_keeper.R;
import pl.directsolutions.fit_keeper.controller.ProductsDatabaseManager;
import pl.directsolutions.fit_keeper.controller.SettingsManager;
import pl.directsolutions.fit_keeper.model.EatenProduct;
import pl.directsolutions.fit_keeper.model.Product;
import pl.directsolutions.fit_keeper.model.TripPoint;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.SQLException;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class DietAnalyzeActivity extends Activity
{
	private static final float gramsPerPound = new Float(453.59237);
	static final private int MENU_CHANGE_DAY = Menu.FIRST;
	static final private int MENU_CONFIGURE_DIET = Menu.FIRST + 1;
	static final private int SUGGEST_MEAL = Menu.FIRST + 2;
	static final int DATE_DIALOG_ID = 0;
	static final int SUGGEST_DIALOG = 1;

	private ListView productsListView;
	ArrayList<Suggested> suggestedProductsList;
	private TextView textView;
	private ArrayAdapter<RowModel> arrayAdapter;
	private ArrayList<RowModel> productsList;
	private SettingsManager settingsManager;
	private float wholeProteins, wholeCarbohydrates, wholeFats, wholeCalories;
	private ArrayList<EatenProduct> eatenProducts;
	private Calendar tempDate;
	private EditText caloriesEditText, fatEditText, carbohydratesEditText, proteinEditText;

	private int mYear;
	private int mMonth;
	private int mDay;

	@Override
	public void onCreate(Bundle icicle)
	{
		super.onCreate(icicle);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.analyze);

		textView = (TextView) findViewById(R.id.AnalyzeTextView);
		productsListView = (ListView) this.findViewById(R.id.AnalyzeList);
		settingsManager = SettingsManager.getInstance(this);
		caloriesEditText = (EditText) findViewById(R.id.caloriesValueEditText);
		proteinEditText = (EditText) findViewById(R.id.proteinValueEditText);
		carbohydratesEditText = (EditText) findViewById(R.id.carbohydratesValueEditText);
		fatEditText = (EditText) findViewById(R.id.fatValueEditText);
	}

	@Override
	public void onResume()
	{
		super.onResume();
		wholeProteins = wholeCarbohydrates = wholeFats = wholeCalories = 0;
		productsList = new ArrayList<RowModel>();

		tempDate = Calendar.getInstance();
		tempDate.setTimeInMillis(System.currentTimeMillis());

		arrayAdapter = new RowAdapter(this, productsList);

		productsListView.setOnItemClickListener(new OnItemClickListener()
		{
			public void onItemClick(AdapterView adapterView, View view, int index, long arg3)
			{
				//show details dialog
			}
		});

		productsListView.setOnItemLongClickListener(new OnItemLongClickListener()
		{

			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2, long arg3)
			{
				showAlertDialog(arg2);
				return true;
			}

		});

		productsListView.setAdapter(arrayAdapter);

		prepareProductsList(productsListView, productsList, tempDate);
		setHeader();
	}

	private void setHeader()
	{
		float factor = 1;

		DecimalFormat df = new DecimalFormat("##.##");
		String postfix;

		if (settingsManager.getUnits().equals(SettingsManager.US_UNITS))
		{
			postfix = "lb";
			factor = 1 / gramsPerPound;
			wholeProteins /= gramsPerPound;
			wholeCarbohydrates /= gramsPerPound;
			wholeFats /= gramsPerPound;

		} else
		{
			postfix = "g";
		}

		String proteinString = df.format(wholeProteins) + postfix + " / " + df.format(settingsManager.getProteinRequirement() * factor);// + "\n";
		String carbohydrateString = df.format(wholeCarbohydrates) + postfix + " / " + df.format(settingsManager.getCarbohydratesRequirement() * factor);// + "\n";
		String fatString = df.format(wholeFats) + postfix + " / " + df.format(settingsManager.getFatRequirement() * factor);// + "\n";
		String calorieString = df.format(wholeCalories) + " / " + df.format(settingsManager.getCaloriesRequirement());

		caloriesEditText.setText(calorieString);
		proteinEditText.setText(proteinString);
		carbohydratesEditText.setText(carbohydrateString);
		fatEditText.setText(fatString);

				textView.setText(getApplicationContext().getString(R.string.date_string) + " " + tempDate.get(Calendar.DAY_OF_MONTH) + "." + (tempDate.get(Calendar.MONTH) + 1) + "."
						+ tempDate.get(Calendar.YEAR));// + "\n" + proteinString + carbohydrateString + fatString + getApplicationContext().getString(R.string.calories_string) + " "
		//				+ df.format(wholeCalories) + " / " + df.format(settingsManager.getCaloriesRequirement()));
	}

	private void prepareProductsList(ListView listView, ArrayList<RowModel> list, Calendar date)
	{
		addElements(list, date);
		arrayAdapter.notifyDataSetChanged();
		setHeader();
	}

	private void addElements(ArrayList<RowModel> list, Calendar date)
	{
		wholeProteins = wholeCarbohydrates = wholeFats = wholeCalories = 0;
		String productPointsFileName = date.get(Calendar.DAY_OF_MONTH) + "_" + (tempDate.get(Calendar.MONTH) + 1) + "_" + date.get(Calendar.YEAR);
		eatenProducts = new ArrayList<EatenProduct>();
		list.clear();

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
			float protein, carbohydrates, fats, calories;
			long time;
			int amount;
			String name;
			boolean isFood;
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

					list.add(new RowModel(product.getName(), product.getCalories() + ""));
					wholeProteins += product.getProtein();
					wholeCarbohydrates += product.getCarbohydrates();
					wholeFats += product.getFat();
					wholeCalories += product.getCalories();
				}
			} catch (IOException e)
			{
				e.printStackTrace();
			}

		} catch (FileNotFoundException e)
		{
			wholeProteins = wholeCarbohydrates = wholeFats = wholeCalories = 0;
			e.printStackTrace();
		}
	}

	public RowModel getModel(int position)
	{
		return (((RowAdapter) productsListView.getAdapter()).getItem(position));
	}

	private class RowAdapter extends ArrayAdapter<RowModel>
	{
		Activity context;

		RowAdapter(Activity context, ArrayList<RowModel> list)
		{
			super(context, R.layout.settings_row, list);
			this.context = context;
		}

		public View getView(int position, View convertView, ViewGroup parent)
		{
			View row = convertView;
			ViewWrapper wrapper;

			if (row == null)
			{
				LayoutInflater inflater = context.getLayoutInflater();
				row = inflater.inflate(R.layout.settings_row, null);
				wrapper = new ViewWrapper(row);
				row.setTag(wrapper);
			} else
			{
				wrapper = (ViewWrapper) row.getTag();
			}

			RowModel model = getModel(position);
			wrapper.getLabel().setText(model.getLabel());
			wrapper.getDetails().setText(model.getDetails());

			return (row);
		}
	}

	private void showAlertDialog(final int index)
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(R.string.delete_question).setCancelable(false).setPositiveButton(R.string.positive_string, new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int id)
			{
				deleteProduct(index);
			}
		}).setNegativeButton(R.string.negative_string, new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int id)
			{
				dialog.cancel();
			}
		});
		AlertDialog alert = builder.create();
		alert.show();
	}

	private synchronized void deleteProduct(final int index)
	{
		Calendar date = Calendar.getInstance();
		date.setTimeInMillis(eatenProducts.get(index).getTime());
		productsList.remove(index);
		eatenProducts.remove(index);
		arrayAdapter.notifyDataSetChanged();

		wholeProteins = wholeCarbohydrates = wholeFats = wholeCalories = 0;
		for (int i = 0; i < eatenProducts.size(); i++)
		{
			EatenProduct product = eatenProducts.get(i);
			wholeProteins += product.getProtein();
			wholeCarbohydrates += product.getCarbohydrates();
			wholeFats += product.getFat();
			wholeCalories += product.getCalories();
		}

		try
		{
			String productPointsFileName = date.get(Calendar.DAY_OF_MONTH) + "_" + (tempDate.get(Calendar.MONTH) + 1) + "_" + date.get(Calendar.YEAR);
			FileOutputStream fos = openFileOutput(productPointsFileName, Context.MODE_PRIVATE);
			PrintWriter out = new PrintWriter(fos);

			EatenProduct product;
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

		setHeader();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		super.onCreateOptionsMenu(menu);

		menu.add(0, MENU_CHANGE_DAY, Menu.NONE, R.string.change_day);
		menu.add(0, MENU_CONFIGURE_DIET, Menu.NONE, R.string.configure_diet);
		menu.add(1, SUGGEST_MEAL, Menu.NONE, R.string.suggest_meal);

		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item)
	{
		super.onOptionsItemSelected(item);

		switch (item.getItemId())
		{
		case (MENU_CHANGE_DAY):
		{
			showDialog(DATE_DIALOG_ID);
			return true;
		}
		case (MENU_CONFIGURE_DIET):
		{
			Intent intent = new Intent(DietAnalyzeActivity.this, ConfigureDietActivity.class);
			startActivity(intent);
			return true;
		}
		case (SUGGEST_MEAL):
		{
			suggestProperMeal();
		}
		}
		return false;
	}

	@Override
	protected Dialog onCreateDialog(int id)
	{
		switch (id)
		{
		case DATE_DIALOG_ID:

			Calendar date = Calendar.getInstance();
			date.setTimeInMillis(System.currentTimeMillis());

			mYear = date.get(Calendar.YEAR);
			mMonth = date.get(Calendar.MONTH);
			mDay = date.get(Calendar.DAY_OF_MONTH);
			return new DatePickerDialog(this, mDateSetListener, mYear, mMonth, mDay);

		case SUGGEST_DIALOG:
			LayoutInflater li = LayoutInflater.from(this);
			View quakeDetailsView = li.inflate(R.layout.news_details, null);

			AlertDialog.Builder quakeDialog = new AlertDialog.Builder(this);
			quakeDialog.setTitle("Suggested meals:");
			quakeDialog.setView(quakeDetailsView);
			return quakeDialog.create();
		}
		return null;
	}

	@Override
	public void onPrepareDialog(int id, Dialog dialog)
	{
		switch (id)
		{
		case (SUGGEST_DIALOG):

			AlertDialog quakeDialog = (AlertDialog) dialog;
			TextView detailsTextView = (TextView) quakeDialog.findViewById(R.id.newsDetailsText);
			detailsTextView.setText("");

			float factor = 1;

			DecimalFormat df = new DecimalFormat("####.###");
			String postfix;

			if (settingsManager.getUnits().equals(SettingsManager.US_UNITS))
			{
				postfix = "lb";
				factor = 1 / gramsPerPound;
			} else
			{
				postfix = "g";
			}

			for (int i = 0; i < suggestedProductsList.size(); i++)
			{
				detailsTextView
						.append(suggestedProductsList.get(i).getProduct().getName() + " " + df.format(suggestedProductsList.get(i).getAmount() * factor) + postfix);
			}

			break;
		}
	}

	private DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener()
	{
		public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth)
		{
			mYear = year;
			mMonth = monthOfYear;
			mDay = dayOfMonth;
			tempDate.set(mYear, mMonth, mDay);
			prepareProductsList(productsListView, productsList, tempDate);
		}
	};

	private void suggestProperMeal()
	{
		suggestedProductsList = new ArrayList<Suggested>();
		ProductsDatabaseManager manager = new ProductsDatabaseManager(this);
		manager.open();

		ArrayList<Product> productsList = new ArrayList<Product>();
		try
		{
			productsList.addAll(manager.getProductsByName(""));
			Log.d("products", productsList.size() + "");

			Product tempProduct;
			float tempAmount, tempMin, factor, temp, productMin;
			float actualMin = (float) 9999999999999999999999999.0;
			float caloriesToFill = settingsManager.getCaloriesRequirement() - wholeCalories;
			float proteinsToFill = settingsManager.getProteinRequirement() - wholeProteins;
			float carbohydratesToFill = settingsManager.getCarbohydratesRequirement() - wholeCarbohydrates;
			float fatsToFill = settingsManager.getFatRequirement() - wholeFats;

			for (int i = 0; i < productsList.size(); i++)
			{
				tempProduct = productsList.get(i);
				tempAmount = tempMin = productMin = 0;

				//count calories
				tempMin = 0;
				factor = caloriesToFill / tempProduct.getCalories();
				temp = proteinsToFill - (factor * tempProduct.getProtein());
				if (temp < 0)
				{
					temp *= -2;
				}
				tempMin += temp;
				temp = carbohydratesToFill - (factor * tempProduct.getCarbohydrates());
				if (temp < 0)
				{
					temp *= -2;
				}
				tempMin += temp;
				temp = fatsToFill - (factor * tempProduct.getFat());
				if (temp < 0)
				{
					temp *= -2;
				}
				tempMin += temp;
				productMin = tempMin;
				tempAmount = factor;

				//count proteins
				tempMin = 0;
				factor = proteinsToFill / tempProduct.getProtein();
				temp = caloriesToFill - (factor * tempProduct.getCalories());
				if (temp < 0)
				{
					temp *= -2;
				}
				tempMin += temp;
				temp = carbohydratesToFill - (factor * tempProduct.getCarbohydrates());
				if (temp < 0)
				{
					temp *= -2;
				}
				tempMin += temp;
				temp = fatsToFill - (factor * tempProduct.getFat());
				if (temp < 0)
				{
					temp *= -2;
				}
				tempMin += temp;
				if (tempMin < productMin)
				{
					productMin = tempMin;
					tempAmount = factor;
				}

				//count carbohydrates
				tempMin = 0;
				factor = carbohydratesToFill / tempProduct.getCarbohydrates();
				temp = caloriesToFill - (factor * tempProduct.getCalories());
				if (temp < 0)
				{
					temp *= -2;
				}
				tempMin += temp;
				temp = proteinsToFill - (factor * tempProduct.getProtein());
				if (temp < 0)
				{
					temp *= -2;
				}
				tempMin += temp;
				temp = fatsToFill - (factor * tempProduct.getFat());
				if (temp < 0)
				{
					temp *= -2;
				}
				tempMin += temp;
				if (tempMin < productMin)
				{
					productMin = tempMin;
					tempAmount = factor;
				}

				//count fats
				tempMin = 0;
				factor = fatsToFill / tempProduct.getFat();
				temp = caloriesToFill - (factor * tempProduct.getCalories());
				if (temp < 0)
				{
					temp *= -2;
				}
				tempMin += temp;
				temp = proteinsToFill - (factor * tempProduct.getProtein());
				if (temp < 0)
				{
					temp *= -2;
				}
				tempMin += temp;
				temp = carbohydratesToFill - (factor * tempProduct.getCarbohydrates());
				if (temp < 0)
				{
					temp *= -2;
				}
				tempMin += temp;
				if (tempMin < productMin)
				{
					productMin = tempMin;
					tempAmount = factor;
				}

				if (productMin < actualMin)
				{
					actualMin = productMin;
					suggestedProductsList = new ArrayList<Suggested>();
					suggestedProductsList.add(new Suggested(tempProduct, tempAmount * 100));
				} else if (productMin == actualMin)
				{
					suggestedProductsList.add(new Suggested(tempProduct, tempAmount * 100));
				}
			}
			manager.close();
			showDialog(SUGGEST_DIALOG);
		} catch (SQLException e)
		{
			Toast toast = Toast.makeText(DietAnalyzeActivity.this, R.string.no_product_found, Toast.LENGTH_SHORT);
			toast.show();
		}
	}

	private class Suggested
	{
		private Product product;
		private float amount;

		private Suggested(Product product, float amount)
		{
			this.product = product;
			this.amount = amount;
		}

		public Product getProduct()
		{
			return product;
		}

		public float getAmount()
		{
			return amount;
		}
	}
}
