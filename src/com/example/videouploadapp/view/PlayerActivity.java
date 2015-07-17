package com.example.videouploadapp.view;

import java.io.File;
import java.io.IOException;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.RatingBar.OnRatingBarChangeListener;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.example.videouploadapp.R;
import com.example.videouploadapp.common.Utils;
import com.example.videouploadapp.provider.VideoContract;
import com.example.videouploadapp.services.DataLoader;


public class PlayerActivity extends Activity implements SurfaceHolder.Callback, DataLoader.DataLoaderCallbacks{
	
	private static final String TAG = "PlayerActivity";
	private static final String VIDEO_ID = "video_id";
	private static final String VIDEO_RATING = "video_rating";
	private static final String FILE_PATH = "file_path";
	private static final String CUR_POSITION = "cur_pos";
	private static final String PLAYER_STATE = "player_state";
	
	private RatingBar mRatingBar;
	private Button mContrButton;
	private Button mDownloadButton;
	private MediaPlayer mPlayer;
	private long mVideoId;
	private String mPath;
	private Float mRating; 
	//private Uri mUri;
	private boolean mPlayerState;
	private SurfaceHolder mHolder;
	private int mCurPos;
	private boolean mHolderState;
 

	
	public static Intent makeIntent(Context context, Uri data, long id, float rating){
		Intent intent = new Intent(context, PlayerActivity.class);
		intent.putExtra(VIDEO_ID,  id);	
		intent.putExtra(VIDEO_RATING,  rating);
		intent.setData(data);		
		return intent; 
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_player);	
		
		if (savedInstanceState == null){
		   Intent intent = getIntent();
		   mPath = intent.getData().toString();
		   mVideoId = intent.getLongExtra(VIDEO_ID, 0);
		   mRating = intent.getFloatExtra(VIDEO_RATING, 0);
		   mCurPos = 0; 
		   mPlayerState = false; 
		}
		else{
			mPath = savedInstanceState.getString(FILE_PATH);
			mRating = savedInstanceState.getFloat(VIDEO_RATING);
			mVideoId = savedInstanceState.getLong(VIDEO_ID);
			mCurPos = savedInstanceState.getInt(CUR_POSITION);
			mPlayerState = savedInstanceState.getBoolean(PLAYER_STATE);
		}
		
		mRatingBar = (RatingBar)findViewById(R.id.rating_bar);
		mContrButton = (Button)findViewById(R.id.play_button);
		mDownloadButton = (Button)findViewById(R.id.download_button);
		SurfaceView surfaceView = (SurfaceView)findViewById(R.id.surface_view);
	
		mPlayer = new MediaPlayer();
		mPlayer.setOnCompletionListener(new OnCompletionListener() {		
			@Override
			public void onCompletion(MediaPlayer mp) {
				mContrButton.setText(R.string.play);
			}
		});
		
		mContrButton.setEnabled(false);
		mDownloadButton.setEnabled(true);
		
		if (mPath != null){
			File file = new File(mPath);
			boolean fileExist = (file != null && file.exists());
			mContrButton.setEnabled(fileExist);
			mDownloadButton.setEnabled(!fileExist);
			mPath = (fileExist == false)? null:mPath;
		}
		
		mHolderState = false; 
		mHolder = surfaceView.getHolder();
		mHolder.addCallback(this);
		
		mRatingBar.setRating(mRating);
		mRatingBar.setOnRatingBarChangeListener(new OnRatingBarChangeListener() {	
			@Override
			public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
				
		        DataLoader dataLoader = getDataLoader();
		        if (dataLoader != null) {
		        	Log.d(TAG, "set rating");
				   dataLoader.setRating(VideoContract.buildVideoUri(mVideoId), rating); 
		        }

			}
		});
	
		
        FragmentManager fm = getFragmentManager();
        DataLoader dataLoader = (DataLoader) fm.findFragmentByTag(TAG);
        if (dataLoader == null) {
            dataLoader = new DataLoader();
            fm.beginTransaction().add(dataLoader, TAG).commit();
        }

	}
	
	 @Override
	    public void onSaveInstanceState(Bundle savedInstanceState) {
	        //save important variables
	        savedInstanceState.putString(FILE_PATH, mPath);
	        savedInstanceState.putLong(VIDEO_ID, mVideoId);
	        savedInstanceState.putFloat(VIDEO_RATING, mRating);
	        savedInstanceState.putInt(CUR_POSITION, mPlayer.getCurrentPosition());
	        savedInstanceState.putBoolean(PLAYER_STATE, mPlayer.isPlaying());
	        super.onSaveInstanceState(savedInstanceState);
	    }
	
	
	@Override
	protected void onDestroy() {
		if (mPlayer != null){
			mPlayer.release();
		}
		super.onDestroy();
	}

	/*  holder callback */
	
	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		try{
			   mPlayer.setDisplay(holder);
			   if (mPath != null){
                  mPlayer.setDataSource(mPath);
                 
			      mPlayer.prepare();
			      mPlayer.seekTo(mCurPos);
			      if (mPlayerState){
			    	  mPlayer.start();
			      }
			   }
			   mHolderState = true; 	   
			}
			catch(Exception e){
			   Log.d(TAG, e.getMessage());
			}	
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		mPlayer.release();		
	}
	
	
	/* Button handlers */
	
	public void downloadVideo(View v){

        DataLoader dataLoader = getDataLoader();
        if (dataLoader != null) {
           Utils.showToast(this, "Downloading video");
		   dataLoader.downloadVideo(VideoContract.buildVideoUri(mVideoId)); 
        }
	}
	
	public void playVideo(View v){
		if (mHolderState){
			if (!mPlayer.isPlaying()){
			    mPlayer.start();
			    mContrButton.setText(R.string.pause);
			}	
			else{
			    mPlayer.pause();
			    mContrButton.setText(R.string.play);
			}
		}	
	}

	@Override
	public void onPostExecute(Cursor cursor) {

		if (cursor != null && cursor.getCount() == 1){
			cursor.moveToFirst();
			float rating = cursor.getFloat(cursor.getColumnIndex(VideoContract.COL_RATING));
		    mRatingBar.setRating(rating);
		    
		    if (mPath == null){
			    String path = cursor.getString(cursor.getColumnIndex(VideoContract.COL_LOCATION));
			    File file = new File(path);
				boolean fileExist = (file != null && file.exists());
				if (fileExist){
					try {
						mPath = path;
						mPlayer.setDataSource(mPath); 
						mPlayer.prepare();	
						mPlayer.seekTo(mCurPos);
					    mContrButton.setEnabled(true);
						mDownloadButton.setEnabled(false);
						
					} catch (Exception e) {
						Log.d(TAG, e.getMessage());
					} 
				}
				
		    }
		}
	}
	
	private DataLoader getDataLoader(){
		FragmentManager fm = getFragmentManager();
        DataLoader dataLoader = (DataLoader) fm.findFragmentByTag(TAG);
		return dataLoader;
	}
	
	

}
