package pl.directsolutions.fit_keeper.view;

import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import pl.directsolutions.fit_keeper.R;
import pl.directsolutions.fit_keeper.controller.Formatter;
import pl.directsolutions.fit_keeper.controller.ProductsDatabaseManager;
import pl.directsolutions.fit_keeper.controller.SettingsManager;
import pl.directsolutions.fit_keeper.model.Product;
import pl.directsolutions.fit_keeper.model.Workout;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.SQLException;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Toast;

public class ManualProductSearchActivity extends Activity
{
	static final private int MENU_ADD = Menu.FIRST;
	static final private int MENU_SHOW_ALL = Menu.FIRST + 1;
	public static final String PRODUCT_ID = "product_id";

	private Button searchButton;
	private EditText searchEditText;

	private ArrayAdapter<RowModel> arrayAdapter;
	private ArrayList<RowModel> productsList;
	private ListView productsListView;
	private ProductsDatabaseManager manager;
	private ArrayList<Product> productsArrayList;

	public void onCreate(Bundle bundle)
	{
		super.onCreate(bundle);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.manual_search);

		searchButton = (Button) findViewById(R.id.searchButton);
		searchButton.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				search(searchEditText.getText().toString().trim());
			}
		});

		searchEditText = (EditText) findViewById(R.id.searchEditBox);
		searchEditText.setHint(R.string.type_in_product_name_hint);

		productsListView = (ListView) findViewById(R.id.foundProductsListView);
		productsList = new ArrayList<RowModel>();
		arrayAdapter = new RowAdapter(this, productsList);

		productsListView.setOnItemClickListener(new OnItemClickListener()
		{
			public void onItemClick(AdapterView adapterView, View view, int index, long arg3)
			{
				Intent intent = new Intent(ManualProductSearchActivity.this, ChooseAmountOfNotScannedActivity.class);
				intent.putExtra(PRODUCT_ID, productsArrayList.get(index).getId());
				startActivity(intent);
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
		manager = new ProductsDatabaseManager(this);
		manager.open();
	}

	private void search(String typedInString)
	{
		productsList.clear();
		productsArrayList = new ArrayList<Product>();
		String[] stringToSearchArray;

		try
		{
			stringToSearchArray = typedInString.split(" ");
			productsArrayList.addAll(searchProductByName(typedInString));

			if (stringToSearchArray.length > 1)
			{
				for (int i = 0; i < stringToSearchArray.length; i++)
				{
					try
					{
						productsArrayList.addAll(searchProductByName(stringToSearchArray[i]));
					} catch (SQLException e)
					{
						e.printStackTrace();
					}
				}
			}
		} catch (SQLException e)
		{
			e.printStackTrace();

			int duration = Toast.LENGTH_SHORT;
			Toast toast = Toast.makeText(ManualProductSearchActivity.this, R.string.no_product_found, duration);
			toast.show();
			arrayAdapter.notifyDataSetChanged();
		}

		for (int i = 0; i < productsArrayList.size(); i++)
		{
			Product product = productsArrayList.get(i);
			productsList.add(new RowModel(product.getName(), product.getCalories() + "")); //per ile gram/ml
			arrayAdapter.notifyDataSetChanged();
		}
	}

	private ArrayList<Product> searchProductByName(String productName) throws SQLException
	{
		ArrayList<Product> productsList = new ArrayList<Product>();
		productsList.addAll(manager.getProductsByName(productName));
		return productsList;
	}

	public void onDestroy()
	{
		super.onDestroy();
		manager.close();
	}

	private RowModel getModel(int position)
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

	private void deleteProduct(final int index)
	{
		productsList.remove(index);
		manager.removeWorkoutById(productsArrayList.get(index).getId());
		arrayAdapter.notifyDataSetChanged();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		super.onCreateOptionsMenu(menu);

		menu.add(0, MENU_ADD, Menu.NONE, R.string.add_product);
		menu.add(0, MENU_SHOW_ALL, Menu.NONE, R.string.show_all_products);

		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item)
	{
		super.onOptionsItemSelected(item);

		switch (item.getItemId())
		{
		case (MENU_ADD):
		{
			Intent intent = new Intent(ManualProductSearchActivity.this, ChooseAmountOfNotScannedActivity.class);
			intent.putExtra(PRODUCT_ID, -1);
			startActivity(intent);
			return true;
		}
		case (MENU_SHOW_ALL):
		{
			search("");
			return true;
		}
		}
		return false;
	}

}
