package com.example.RSS;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

public class RSSChanelActivity extends Activity {
	String table_name,link;
	ListView listview;
	RSSBroadcastReceiver rssBroadcastReceiver;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		listview = (ListView) findViewById(R.id.listView);

		link = getIntent().getStringExtra("link");
		table_name = getIntent().getStringExtra("name_table");


		ArrayList<HashMap<String, String>> feeds = getFeeds(table_name);
		SimpleAdapter adapter = new SimpleAdapter(RSSChanelActivity.this,feeds , R.layout.my_simple_item,
				new String[]{"title", "date"},
				new int[] {R.id.headerTextView, R.id.timeTextView});
		listview.setAdapter(adapter);
		IntentFilter intentFilter = new IntentFilter("com.example.RSSReader.RESPONSE");
		intentFilter.addCategory(Intent.CATEGORY_DEFAULT);
		rssBroadcastReceiver = new RSSBroadcastReceiver();
		registerReceiver(rssBroadcastReceiver, intentFilter);

		listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> av, View view, int index, long arg3) {
				RSSSqlOpenHelper helper = new RSSSqlOpenHelper(RSSChanelActivity.this);
				SQLiteDatabase database = helper.getWritableDatabase();
				Cursor cursor = database.query(table_name,null,null,null,null,null,null);
				cursor.moveToPosition(index);
				String text = cursor.getString(cursor.getColumnIndex(RSSSqlOpenHelper.DESCRIPTION));

				Intent intent = new Intent(RSSChanelActivity.this, WebActivity.class);
				intent.putExtra("text", text);
				startActivity(intent);
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.rss_chanel, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.refresh:
				Intent intentRSS = new Intent(RSSChanelActivity.this, RSSIntentService.class);
				startService(intentRSS.putExtra("link", link).putExtra("task", "load").putExtra("name", table_name));

				return true;
			default:
				return super.onOptionsItemSelected(item);
		}

	}

	public class RSSBroadcastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {

			Toast toast = Toast.makeText(getApplicationContext(), "Обновление завершено", Toast.LENGTH_SHORT);
			toast.show();

			SimpleAdapter adapter = new SimpleAdapter(RSSChanelActivity.this, getFeeds(table_name), R.layout.my_simple_item,
					new String[]{"title", "date"},
					new int[] {R.id.headerTextView, R.id.timeTextView});

			listview.setAdapter(adapter);
		}
	}

	public ArrayList<HashMap<String, String>> getFeeds(String name) {
		ArrayList<HashMap<String, String>> items = new ArrayList<HashMap<String, String>>();
		HashMap<String, String> map;
		RSSSqlOpenHelper helper = null;
		SQLiteDatabase database = null;
		Cursor cursor = null;
		try {
			helper = new RSSSqlOpenHelper(this);
			database = helper.getWritableDatabase();
			assert database != null;
			cursor = database.query(name, null,
					null,
					null,
					null,
					null,
					null
			);

			while (cursor.moveToNext()) {

				String title = cursor.getString(cursor.getColumnIndex(RSSSqlOpenHelper.TITLE));
				String date = cursor.getString(cursor.getColumnIndex(RSSSqlOpenHelper.DATE));
				String description = cursor.getString(cursor.getColumnIndex(RSSSqlOpenHelper.DESCRIPTION));

				map = new HashMap<String, String>();
				map.put("title", title);
				map.put("date", date);
				map.put("description", description);
				items.add(map);
			}
		} finally {
			cursor.close();
			helper.close();
			database.close();
		}


		return items;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(rssBroadcastReceiver);
	}

}




