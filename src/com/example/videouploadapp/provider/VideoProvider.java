package com.example.videouploadapp.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

public class VideoProvider extends ContentProvider {

	private static final String TAG = "VideoProvider";
	private static final UriMatcher sUriMatcher = buildUriMatcher();
	private static final int VIDEO = 1;
	private static final int VIDEOS = 2;

	private VideoDataBaseHelper mOpenHelper;

	@Override
	public boolean onCreate() {
		Log.d(TAG, "create data base");
		mOpenHelper = new VideoDataBaseHelper(getContext());
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		Cursor cursor;

		switch (sUriMatcher.match(uri)) {
		case VIDEOS:
			Log.d(TAG, "get videos");
			cursor = mOpenHelper.getReadableDatabase().query(VideoContract.TABLE_NAME, null, null, null, null, null,
					sortOrder);
			break;

		case VIDEO:
			Log.d(TAG, "get one video");

			String[] selArgs = new String[] { uri.getLastPathSegment() };
			cursor = mOpenHelper.getReadableDatabase().query(VideoContract.TABLE_NAME, null, VideoContract.SELECTION_VIDEO_ID, selArgs, null, null,
					sortOrder);
			break;

		default:
			throw new UnsupportedOperationException("Unknown uri: " + uri);
		}

		if (cursor != null) {
			cursor.setNotificationUri(getContext().getContentResolver(), uri);

			/*
			 * cursor.moveToFirst(); int count = cursor.getCount(); while(count
			 * != 0){ Log.d(TAG,
			 * cursor.getString(cursor.getColumnIndex(VideoContract.COL_ID)));
			 * cursor.moveToNext(); count--; }
			 */
		}
		return cursor;
	}

	@Override
	public String getType(Uri uri) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {

		final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		Uri returnUri;

		switch (sUriMatcher.match(uri)) {
		case VIDEOS:
			Log.d(TAG, "insert video");
			
			long videoId = values.getAsLong(VideoContract.COL_VIDEO_ID);
			long id = db.insert(VideoContract.TABLE_NAME, null, values);

			if (id > 0) {
				
				returnUri = VideoContract.buildVideoUri(videoId);
				Log.d(TAG, returnUri.toString());
			} else {
				throw new android.database.SQLException("Failed to insert row into " + uri);
			}
			break;

		default:
			throw new UnsupportedOperationException("Unknown uri: " + uri);
		}

		getContext().getContentResolver().notifyChange(uri, null);

		return returnUri;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		int rowsDeleted = 0;

		switch (sUriMatcher.match(uri)) {
		case VIDEO:
			Log.d(TAG, "delete one video");

			String[] selArgs = new String[] { uri.getLastPathSegment() };
			rowsDeleted = db.delete(VideoContract.TABLE_NAME, VideoContract.SELECTION_VIDEO_ID, selArgs);
			break;

		case VIDEOS:
			Log.d(TAG, "delete all videos");
			rowsDeleted = db.delete(VideoContract.TABLE_NAME, null, null);
			break;

		default:
			throw new UnsupportedOperationException("Unknown uri: " + uri);
		}

		Log.d(TAG, "rowsDeleted = " + Integer.toString(rowsDeleted));
		return rowsDeleted;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

		final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		int rowsUpdated;

		switch (sUriMatcher.match(uri)) {
		case VIDEO:
			Log.d(TAG, "update data base");
			String[] selArgs = new String[] { uri.getLastPathSegment() };
			rowsUpdated = db.update(VideoContract.TABLE_NAME, values, VideoContract.SELECTION_VIDEO_ID, selArgs);
			break;
			
		default:
			throw new UnsupportedOperationException("Unknown uri: " + uri);
		}

		// Notifies registered observers that rows were updated.
		if (rowsUpdated != 0){
			getContext().getContentResolver().notifyChange(uri, null);
		}
		
		Log.d(TAG, "rowsUpdated = " + Integer.toString(rowsUpdated));
		return rowsUpdated;
	}

	private static UriMatcher buildUriMatcher() {
		final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
		matcher.addURI(VideoContract.AUTHORITY, VideoContract.VIDEO_PATH, VIDEO);
		matcher.addURI(VideoContract.AUTHORITY, VideoContract.VIDEOS_PATH, VIDEOS);
		return matcher;
	}

}
