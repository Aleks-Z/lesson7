package com.example.RSS;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.preference.PreferenceManager;

/**
 * Created by Alex on 12.02.14.
 */
public class RSSSqlOpenHelper extends SQLiteOpenHelper {

	public static final String NAME_RSS = "name_rss";
	public static final String LINK = "link";
	public static final int VERSION = 1;
	public static final String DATABASE_NAME = "database.db";
	public static final String TABLE_NAME = "name";
	public static final String NAME_RSS_TABLE = "name_rss_table";
	public static Context mContext = null;
	public static final String TITLE =        "title";
	public static final String DESCRIPTION =  "description";
	public static final String DATE =         "date";

	public RSSSqlOpenHelper(Context context) {
		super(context, DATABASE_NAME, null, VERSION);
		mContext =context;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
	    db.execSQL("CREATE TABLE " + TABLE_NAME
				+ " (_id INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ NAME_RSS + " TEXT, " + NAME_RSS_TABLE + " TEXT, " + LINK + " TEXT);");
		db.execSQL("INSERT INTO "+TABLE_NAME + "("+NAME_RSS+", "+NAME_RSS_TABLE+", "+ LINK+") VALUES (\"Bash\",\"table1\",\"http://bash.im/rss/\");");
		db.execSQL("CREATE TABLE " + "table1"
				+ " (_id INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ TITLE + " TEXT, " + DESCRIPTION + " TEXT, " + DATE + " TEXT);");
		Intent intentRSS = new Intent(mContext, RSSIntentService.class);
		mContext.startService(intentRSS.putExtra("link", "http://bash.im/rss/").putExtra("task", "refresh").putExtra("name", "table1"));
		SharedPreferences sPref = mContext.getSharedPreferences("mMane",Context.MODE_PRIVATE);
		Editor ed = sPref.edit();
		ed.clear();
		ed.putInt("number", 1);
		ed.commit();
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

	}
}
