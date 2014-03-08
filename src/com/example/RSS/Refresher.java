package com.example.RSS;

import android.app.IntentService;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.widget.ProgressBar;

public class Refresher extends IntentService {
	RSSSqlOpenHelper openHelper;
	SQLiteDatabase database;
	Cursor cursor;
    public Refresher() {
        super("Refresher");
    }

	@Override
	public void onDestroy() {
		super.onDestroy();
		openHelper.close();
		database.close();
		cursor.close();
	}

	public void onCreate() {
        super.onCreate();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
		openHelper = new RSSSqlOpenHelper(this);
		database = openHelper.getWritableDatabase();
		assert database != null;
		cursor = database.query(RSSSqlOpenHelper.TABLE_NAME, null,
				null,
				null,
				null,
				null,
				null
		);
		String link,table_name;
		while (cursor.moveToNext()) {
			Intent intentRSS = new Intent(this, RSSIntentService.class);
			link = cursor.getString(cursor.getColumnIndex(RSSSqlOpenHelper.LINK));
			table_name = cursor.getString(cursor.getColumnIndex(RSSSqlOpenHelper.NAME_RSS_TABLE));
			startService(intentRSS.putExtra("link", link).putExtra("task", "refresh").putExtra("name", table_name));
		}
		openHelper.close();
		database.close();
		cursor.close();
    }
}
