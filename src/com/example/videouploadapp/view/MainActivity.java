package com.example.videouploadapp.view;

import java.io.File;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.example.videouploadapp.R;
import com.example.videouploadapp.common.CustomCursorAdapter;
import com.example.videouploadapp.common.Utils;
import com.example.videouploadapp.provider.VideoContract;
import com.example.videouploadapp.services.DataLoader;

public class MainActivity extends Activity implements DataLoader.DataLoaderCallbacks{


	private final String TAG = getClass().getSimpleName();
	private final int REQUEST_GET_VIDEO = 1;
	private ListView mVideoList;
	private SimpleCursorAdapter mAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
				
		mVideoList = (ListView)findViewById(R.id.video_list);		
		mVideoList.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Cursor cursor = ((SimpleCursorAdapter)parent.getAdapter()).getCursor();
			    cursor.moveToPosition(position);
			    startPlayerActivity(cursor);
			}
		});
		
		mAdapter = new CustomCursorAdapter(this, null);
		mVideoList.setAdapter(mAdapter);	
				
        FragmentManager fm = getFragmentManager();
        DataLoader dataLoader = (DataLoader) fm.findFragmentByTag(TAG);
        if (dataLoader == null) {
            dataLoader = new DataLoader();
            fm.beginTransaction().add(dataLoader, TAG).commit();
        }
        else{
        	mAdapter.changeCursor(dataLoader.getCursor());
        	mAdapter.notifyDataSetChanged();
        }
	}
	
	
	@Override
	protected void onStart() {
		super.onStart();
        FragmentManager fm = getFragmentManager();
        DataLoader dataLoader = (DataLoader) fm.findFragmentByTag(TAG);
        if (dataLoader != null) {
		   dataLoader.getVideos();
		}		
	}
	
	/************************ menu function ***************************/

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_upload) {
			openGallery();
			return true;
		}		
		return super.onOptionsItemSelected(item);
	}

	/********************* in order to get a video from the gallery *******************************/
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		Uri videoUri = null;

		// Check if the Result is Ok and upload the Video to the Video Service.
		if (resultCode == Activity.RESULT_OK) {
			// Video picked from the Gallery.
			if (requestCode == REQUEST_GET_VIDEO){
				videoUri = data.getData();
			}

			if (videoUri != null) {
				
					Utils.showToast(this, "Uploading video");
					FragmentManager fm = getFragmentManager();
			        DataLoader dataLoader = (DataLoader) fm.findFragmentByTag(TAG);
			        if (dataLoader != null) {
			        	dataLoader.uploadVideo(videoUri);
			        }
			}
		}

		// Pop a toast if we couldn't get a video to upload.
		if (videoUri == null){
			Utils.showToast(this, "Could not get video to upload");
		}
	}
	
	/************************ in order to open an external gallery app ************************/

	private void openGallery() {
		final Intent videoGalleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
		videoGalleryIntent.setType("video/*");
		videoGalleryIntent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);

		// Verify the intent will resolve to an Activity.
		if (videoGalleryIntent.resolveActivity(getPackageManager()) != null)
			startActivityForResult(videoGalleryIntent, REQUEST_GET_VIDEO);
	}
	
	/*******************************************************************************************/
	
	private void startPlayerActivity(Cursor cursor){
		String location = cursor.getString(cursor.getColumnIndex(VideoContract.COL_LOCATION));
		Uri uri = Uri.parse(location);
		long videoId = cursor.getLong(cursor.getColumnIndex(VideoContract.COL_VIDEO_ID));
		float rating = cursor.getFloat(cursor.getColumnIndex(VideoContract.COL_RATING));
		startActivity(PlayerActivity.makeIntent(MainActivity.this,  uri, videoId, rating));		
	}
	
	/************************* DataLoader callback functions ********************************/

	@Override
	public void onPostExecute(Cursor cursor) {
		mAdapter.changeCursor(cursor);
		mAdapter.notifyDataSetChanged();	
	}

}
