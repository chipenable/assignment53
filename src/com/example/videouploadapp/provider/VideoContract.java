package com.example.videouploadapp.provider;

import android.net.Uri;

public final class VideoContract {
	

	public static final String TABLE_NAME = "video_table";
	
	public static final String COL_ID           = "_id";
	public static final String COL_VIDEO_ID     = "video_id";
	public static final String COL_TITLE        = "title";
	public static final String COL_DURATION     = "duration";
	public static final String COL_CONTENT_TYPE = "content_type";
	public static final String COL_DATA_URL     = "data_url";
	public static final String COL_LOCATION     = "location";
    public static final String COL_RATING       = "rating";
	
	
	public static final String AUTHORITY   = "com.example.videouploadapp.provider.VideoProvider";
	public static final String VIDEOS_PATH = "video";
	public static final String VIDEO_PATH  = "video/#";
	public static final Uri BASE_VIDEO_URI = Uri.parse("content://" + AUTHORITY + "/" + VIDEOS_PATH);
	
	
	public static final String SELECTION_VIDEO_ID = VideoContract.COL_VIDEO_ID + "=? ";
	
	public static Uri buildVideoUri(long id) {
		return Uri.withAppendedPath(BASE_VIDEO_URI, Long.toString(id));
	}
}
