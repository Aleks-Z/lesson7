package com.example.RSS;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.xml.sax.InputSource;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;


public class RSSIntentService extends IntentService {
	private ArrayList<Node> nodes;
	private static String link,name;
	public RSSIntentService() {
		super("RSS#6");
	}
	public void onCreate() {
		super.onCreate();
	}

	@Override
	protected void onHandleIntent(Intent intent) {

		link = intent.getStringExtra("link");
		name = intent.getStringExtra("name");

		String task = intent.getStringExtra("task");

		if (link == null) {
			return;
		}
		Log.d("mega", name);
		InputStream inputStream = null;

		URL url;
		HttpURLConnection connect;

		try {

			HttpClient client = new DefaultHttpClient();
			HttpGet request = new HttpGet(link);
			HttpResponse response = client.execute(request);
			HttpEntity entity = response.getEntity();
			Reader reader;
			if (EntityUtils.getContentCharSet(entity) != null) {
				reader = new InputStreamReader(entity.getContent(), EntityUtils.getContentCharSet(entity));
			}
			else {
				reader = new InputStreamReader(entity.getContent(), "windows-1251");
			}
			InputSource is;
			is = new InputSource(reader);

			SAXParserFactory factory = SAXParserFactory.newInstance();
			factory.setNamespaceAware(false);
			SAXParser parser;
			parser = factory.newSAXParser();
			RSSDefaultHandler rssParser = new RSSDefaultHandler();
			parser.parse(is, rssParser);
			nodes = rssParser.getResult();

		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			try {
				if (inputStream != null) {
					inputStream.close();
				}
			} catch (IOException e) {}
		}

		setArticle(name, nodes);

		Intent intentResponse = new Intent("com.example.RSSReader.RESPONSE");
		intentResponse.addCategory(Intent.CATEGORY_DEFAULT);
		if ("load".equals(task)) {
			sendBroadcast(intentResponse);
		}
	}

	public void setArticle(String name, ArrayList<Node> nodes) {
		RSSSqlOpenHelper helper = null;
		SQLiteDatabase database = null;
		try {
			helper = new RSSSqlOpenHelper(this);
			database = helper.getWritableDatabase();

			database.delete(name, null, null);

			for (int i = 0; i < nodes.size(); i++) {
				ContentValues contentValues = new ContentValues();
				contentValues.put(RSSSqlOpenHelper.TITLE, nodes.get(i).getTitle());
				contentValues.put(RSSSqlOpenHelper.DESCRIPTION, nodes.get(i).getDescription());
				contentValues.put(RSSSqlOpenHelper.DATE, nodes.get(i).getDate());
				database.insert(name, null, contentValues);
			}
		} finally {
			database.close();
			helper.close();
		}

	}
}
