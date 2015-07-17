package com.example.videouploadapp.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.videouploadapp.provider.VideoContract;

public class VideoDataBaseHelper extends SQLiteOpenHelper {
	
	private static final String DB_NAME    = "videos.db";
	private static final int    DB_VERSION = 4;

	
	public VideoDataBaseHelper(Context context){
		super(context, DB_NAME, null, DB_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("create table " + VideoContract.TABLE_NAME + " (" 
	            + VideoContract.COL_ID + " integer primary key autoincrement, "
				+ VideoContract.COL_VIDEO_ID + " long, " 
	            + VideoContract.COL_TITLE    + " text, " 
				+ VideoContract.COL_DURATION + " long, "
				+ VideoContract.COL_CONTENT_TYPE + " text, " 
				+ VideoContract.COL_DATA_URL + " text, " 
				+ VideoContract.COL_LOCATION + " text, "
	            + VideoContract.COL_RATING   + " real);");

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("drop table if exists " + VideoContract.TABLE_NAME);
		onCreate(db);
	}

}
