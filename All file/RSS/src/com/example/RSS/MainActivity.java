package com.example.RSS;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.*;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;

import java.util.ArrayList;

public class MainActivity extends Activity {
	ListView listView;
	public static final String TITLE =        "title";
	public static final String DESCRIPTION =  "description";
	public static final String DATE =         "date";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		listView = (ListView)findViewById(R.id.listView);
		registerForContextMenu(listView);
		Intent intent = new Intent(this, Refresher.class);
		AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
		alarmManager.cancel(pendingIntent);
		alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 30 * 60 * 1000, 30 * 60 * 1000, pendingIntent);
	}



	@Override
	protected void onResume() {
		super.onResume();
		updateListView(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.add:
				addRSS(this);
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	void addRSS(final Context context) {
		AlertDialog.Builder dialog = new AlertDialog.Builder(context);
		dialog.setTitle("Добавление RSS Ленты");  // заголовок
		LinearLayout layout = new LinearLayout(this);
		layout.setOrientation(LinearLayout.VERTICAL);
		final EditText nameBox = new EditText(context);
		nameBox.setHint("Название RSS Ленты");
		layout.addView(nameBox);
		final EditText linkBox = new EditText(context);
		linkBox.setHint("Ссылка на ленту");
		layout.addView(linkBox);
		dialog.setView(layout);

		dialog.setPositiveButton("Добавить",new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				String name = nameBox.getText().toString();
				String link = linkBox.getText().toString();
				if (!checkLink(link)) {
					SharedPreferences sPref = context.getSharedPreferences("mMane", MODE_PRIVATE);
					int number = sPref.getInt("number",1) + 1;
					String table_name = "table"+ number;
					SharedPreferences.Editor ed = sPref.edit();
					ed.clear();
					ed.putInt("number", number);
					ed.commit();
					RSSSqlOpenHelper openHelper = null;
					SQLiteDatabase database = null;
					try {
						openHelper = new RSSSqlOpenHelper(context);
						database = openHelper.getWritableDatabase();
						assert database != null;
						database.execSQL("CREATE TABLE " + table_name
								+ " (_id INTEGER PRIMARY KEY AUTOINCREMENT, "
								+ TITLE + " TEXT, " + DESCRIPTION + " TEXT, " + DATE + " TEXT);");

						ContentValues contentValues = new ContentValues();
						contentValues.put(RSSSqlOpenHelper.NAME_RSS, name);
						contentValues.put(RSSSqlOpenHelper.LINK, link);
						contentValues.put(RSSSqlOpenHelper.NAME_RSS_TABLE,table_name);
						database.insert(RSSSqlOpenHelper.TABLE_NAME, null, contentValues);
						Intent intentRSS = new Intent(MainActivity.this, RSSIntentService.class);
						context.startService(intentRSS.putExtra("link", link).putExtra("task", "refresh").putExtra("name", table_name));
					} finally {
						database.close();
						openHelper.close();
					}
					Toast.makeText(context,"Добавлено" + number,Toast.LENGTH_SHORT).show();
					updateListView(context);
				} else {
					Toast.makeText(context,"Уже есть",Toast.LENGTH_SHORT).show();
				}
			}
		});
		dialog.show();
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
		if (v.getId() == R.id.listView) {
			menu.add("Удалить").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
				@Override
				public boolean onMenuItemClick(MenuItem item) {
					AdapterView.AdapterContextMenuInfo acmi = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
					RSSSqlOpenHelper openHelper = null;
					SQLiteDatabase database = null;
					Cursor cursor = null;
					try {
						openHelper = new RSSSqlOpenHelper(MainActivity.this);
						database = openHelper.getWritableDatabase();
						assert database != null;
						cursor = database.query(RSSSqlOpenHelper.TABLE_NAME,null,null,null,null,null,null);
						cursor.moveToPosition(acmi.position);
						String name_table = cursor.getString(cursor.getColumnIndex(RSSSqlOpenHelper.NAME_RSS_TABLE));
						String link =  cursor.getString(cursor.getColumnIndex(RSSSqlOpenHelper.LINK));
						database.delete(
								RSSSqlOpenHelper.TABLE_NAME,
								RSSSqlOpenHelper.LINK + "='" + link + "'",
								null
						);
						database.execSQL("DROP TABLE IF EXISTS "+ name_table);
						updateListView(MainActivity.this);
					} finally {
						database.close();
						cursor.close();
						openHelper.close();
					}

					return true;
				}
			});
			menu.add("Редакторивать").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
				@Override
				public boolean onMenuItemClick(MenuItem item) {
					final AdapterView.AdapterContextMenuInfo acmi = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
					final int position = acmi.position;
					RSSSqlOpenHelper helper = new RSSSqlOpenHelper(MainActivity.this);
					SQLiteDatabase database = helper.getWritableDatabase();
					assert database != null;
					Cursor cursor = database.query(RSSSqlOpenHelper.TABLE_NAME,null, null, null, null, null, null);
					cursor.moveToPosition(position);
					String name = cursor.getString(cursor.getColumnIndex(RSSSqlOpenHelper.NAME_RSS));
					String link  = cursor.getString(cursor.getColumnIndex(RSSSqlOpenHelper.LINK));
					cursor.close();
					helper.close();

					final Context context = MainActivity.this;
					AlertDialog.Builder dialog = new AlertDialog.Builder(context);
					dialog.setTitle("Редактирование");
					LinearLayout layout = new LinearLayout(context);
					layout.setOrientation(LinearLayout.VERTICAL);
					final EditText nameBox = new EditText(context);
					nameBox.setText(name);
					layout.addView(nameBox);
					final EditText linkBox = new EditText(context);
					linkBox.setText(link);
					layout.addView(linkBox);
					dialog.setView(layout);
					dialog.setPositiveButton("Редактировать",new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							RSSSqlOpenHelper openHelper = new RSSSqlOpenHelper(context);
							SQLiteDatabase database = openHelper.getWritableDatabase();
							ContentValues contentValues = new ContentValues();
							contentValues.put(RSSSqlOpenHelper.NAME_RSS, nameBox.getText().toString());
							contentValues.put(RSSSqlOpenHelper.LINK, linkBox.getText().toString());
							assert database != null;
							database.update(
									RSSSqlOpenHelper.TABLE_NAME,
									contentValues,
									RSSSqlOpenHelper.NAME_RSS + "='" + listView.getItemAtPosition(position).toString() + "'",
									null);
							openHelper.close();
							database.close();
							Toast.makeText(context,"Изменено",Toast.LENGTH_SHORT).show();
							updateListView(context);
						}
					});
					dialog.show();
					return true;
				}
			});
		}
	}


	private boolean checkLink(String link) {
		RSSSqlOpenHelper openHelper = new RSSSqlOpenHelper(this);
		SQLiteDatabase database = openHelper.getWritableDatabase();
		boolean result = false;
		assert database != null;
		Cursor cursor = database.query(RSSSqlOpenHelper.TABLE_NAME, null,
				null,
				null,
				null,
				null,
				null
		);
		while (cursor.moveToNext()) {
			if (link.equals(cursor.getString(cursor.getColumnIndex(RSSSqlOpenHelper.LINK)))) {
				result = true;
				break;
			}
		}
		cursor.close();
		database.close();
		openHelper.close();
		return result;
	}


	public void updateListView(final Context context) {

		ArrayAdapter<String> adapter;
		Cursor cursor;
		ArrayList<String> names_manga = new ArrayList<String>();
		RSSSqlOpenHelper helper = new RSSSqlOpenHelper(context);

		SQLiteDatabase database = helper.getWritableDatabase();
		assert database != null;
		cursor = database.query(RSSSqlOpenHelper.TABLE_NAME, null, null, null, null, null, null);
		while (cursor.moveToNext()) {
			names_manga.add(cursor.getString(cursor.getColumnIndex(RSSSqlOpenHelper.NAME_RSS)));
		}
		cursor.close();
		helper.close();
		database.close();
		adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, names_manga);

		listView.setAdapter(adapter);
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				RSSSqlOpenHelper helper = new RSSSqlOpenHelper(MainActivity.this);
				SQLiteDatabase database  = helper.getWritableDatabase();
				Cursor cursor = database.query(RSSSqlOpenHelper.TABLE_NAME,null,null,null,null,null,null);
				cursor.moveToPosition(position);
				String link = cursor.getString(cursor.getColumnIndex(RSSSqlOpenHelper.LINK));
				String table_name = cursor.getString(cursor.getColumnIndex(RSSSqlOpenHelper.NAME_RSS_TABLE));
				startActivity(new Intent(MainActivity.this,RSSChanelActivity.class)
						.putExtra("link", link)
						.putExtra("name_table", table_name)
				);
				cursor.close();
				database.close();
				helper.close();
			}
		});
	}


}
