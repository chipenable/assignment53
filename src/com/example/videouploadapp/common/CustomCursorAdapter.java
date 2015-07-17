package com.example.videouploadapp.common;

import com.example.videouploadapp.R;
import com.example.videouploadapp.provider.VideoContract;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.SimpleCursorAdapter;

public class CustomCursorAdapter extends SimpleCursorAdapter {
	
	private static final String[] columns = new String[] { 
			VideoContract.COL_TITLE,
			VideoContract.COL_RATING
		    };
	
	private static final int[] to = new int[] { 
			R.id.video_name, 
			R.id.video_rating
			};	
	
    public CustomCursorAdapter(Context context, Cursor cursor){
    	super(context, R.layout.video_item, cursor, columns, to, 0);
    }
}
