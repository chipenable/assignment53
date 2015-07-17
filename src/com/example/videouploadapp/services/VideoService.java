package com.example.videouploadapp.services;

import java.io.File;
import java.io.InputStream;
import java.util.Collection;

import retrofit.RestAdapter;
import retrofit.client.Response;
import retrofit.mime.TypedFile;
import android.app.IntentService;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import com.example.videouploadapp.model.Video;
import com.example.videouploadapp.model.VideoStatus;
import com.example.videouploadapp.model.VideoStatus.VideoState;
import com.example.videouploadapp.provider.VideoContract;
import com.example.videouploadapp.utils.Constants;
import com.example.videouploadapp.utils.VideoMediaStoreUtils;
import com.example.videouploadapp.utils.VideoStorageUtils;

public class VideoService extends IntentService {

	private static final String TAG = "VideoService";

	public static final int UPLOAD_VIDEO = 1;
	public static final int DOWNLOAD_VIDEO = 2;
	public static final int GET_LIST_VIDEO = 3;
	public static final int SET_RATING = 4;
	public static final int GET_RATING = 5; 

	
	private static final String COMMAND = "com";
	private static final String URI = "uri";
	private static final String MESSENGER = "messenger";
	private static final String RATING = "rating";
	
		
    public static final String STATUS_UPLOAD_SUCCESSFUL = "Upload succeeded";
    public static final String STATUS_UPLOAD_ERROR_FILE_TOO_LARGE = "Upload failed: File too big";
    public static final String STATUS_UPLOAD_ERROR = "Upload failed";
    public static final String STATUS_DOWNLOAD_SUCCESSFUL = "Dounload succeeded";
	

	public static Intent makeUploadIntent(Context context, Uri uri, Handler handler) {
		return makeIntent(context, UPLOAD_VIDEO, uri, handler);
	}

	public static Intent makeDownloadIntent(Context context, Uri videoUri, Handler handler) {
		return makeIntent(context, DOWNLOAD_VIDEO, videoUri, handler);
	}

	public static Intent makeGetListVideoIntent(Context context, Handler handler) {
		return makeIntent(context, GET_LIST_VIDEO, null, handler);
	}
	
	public static Intent makeSetRatingIntent(Context context, Uri videoUri, float rating, Handler handler){
		Intent intent = makeIntent(context, SET_RATING, videoUri, handler);
		intent.putExtra(RATING,  rating);
		return intent;
	}
	
	public static Intent makeGetRatingIntent(Context context, Uri videoUri, Handler handler){
		return makeIntent(context, GET_RATING, videoUri, handler);
	}

	private static Intent makeIntent(Context context, int com, Uri uri, Handler handler) {
		Intent intent = new Intent(context, VideoService.class);
		intent.putExtra(COMMAND, com);
		intent.setData(uri);
		if (handler != null) {
			intent.putExtra(MESSENGER, new Messenger(handler));
		}

		Log.d(TAG, "intent was made");
		return intent;
	}

	public VideoService() {
		super(TAG);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Log.d(TAG, "start service");
						
		int com = intent.getIntExtra(COMMAND, 0);
		Uri uri = intent.getData();
		
		Log.d(TAG, "uri: " + ((uri == null)? "-" : uri.toString()));
		
		Messenger messenger = intent.getParcelableExtra(MESSENGER);
		int code = 0; 
		String result = null;
		long id = -1; 
		
		VideoServiceProxy videoServiceProxy = new RestAdapter
                .Builder()
                .setEndpoint(Constants.SERVER_URL)
                .build()
		        .create(VideoServiceProxy.class);
        
		switch(com){
		case UPLOAD_VIDEO:
			result = STATUS_UPLOAD_ERROR;
		    try{
		        result = uploadVideo(getApplicationContext(), videoServiceProxy, uri);
		        
			}
			catch(Exception e){
				//Log.d(TAG, e.getMessage());
			}
            Log.d(TAG, (result == null)? "nothing":result);
            code = ResponseMsg.MSG_DB_UPDATED;
			break; 
			
		case DOWNLOAD_VIDEO:
		    try{
		    	Log.d(TAG, "download video: " + uri.toString());
		    	ContentResolver resolver = getApplicationContext().getContentResolver();
		    	Cursor cursor = resolver.query(uri, null, null, null, null);
		    	cursor.moveToFirst();
		    	long videoId = cursor.getLong(cursor.getColumnIndex(VideoContract.COL_VIDEO_ID));
		    	String videoTitle = cursor.getString(cursor.getColumnIndex(VideoContract.COL_TITLE));
		    	
		        Response response = videoServiceProxy.getData(videoId);
		   	    File file = VideoStorageUtils.storeVideoInExternalDirectory(getApplicationContext(), response, videoTitle);		        
			    if (file != null){
			    	Log.d(TAG, "video file: " + file.getAbsolutePath());
			    	Video video = VideoMediaStoreUtils.getVideo(getApplicationContext(), file.getAbsoluteFile().toString());
			    	video.setId(videoId);
			    	videoServiceProxy.addVideo(video);
			    	ContentValues values = new ContentValues();
			    	values.put(VideoContract.COL_LOCATION, file.getAbsolutePath());		    	
					resolver.update(uri, values, null, null);
					code = ResponseMsg.MSG_DB_UPDATED;
					id = videoId;
					result = STATUS_DOWNLOAD_SUCCESSFUL;
			    }
		    }
			catch(Exception e){
				Log.d(TAG, e.getMessage());
			}
			break; 	
			
			
		case SET_RATING:
			float rating = intent.getFloatExtra(RATING,  0);
			Log.d(TAG, "set rating: " + Float.toString(rating));
			
		    try{
		    	ContentResolver resolver = getApplicationContext().getContentResolver();
		    	Cursor cursor = resolver.query(uri,  null,  null,  null,  null);
		    	
		    	cursor.moveToFirst();
		    	long videoId = cursor.getLong(cursor.getColumnIndex(VideoContract.COL_VIDEO_ID));
		    	
		    	/*Log.d(TAG, "current rating: " + Float.toString(curRating));
		    	rating = (curRating + rating);
		    	rating = (rating == 0)? 0:(rating/2);
		    	Log.d(TAG, "new rating: " + Float.toString(rating));*/
		    	
		    	/*ContentValues values = new ContentValues();
		    	values.put(VideoContract.COL_RATING, rating);		    	
				resolver.update(uri, values, null, null);*/
				
				float averageRating = videoServiceProxy.setVideoRating(videoId, rating);
				ContentValues values = new ContentValues();
		    	values.put(VideoContract.COL_RATING, averageRating);		    	
				resolver.update(uri, values, null, null);
				code = ResponseMsg.MSG_DB_UPDATED;
				id = videoId;
		        //result = uploadVideo(getApplicationContext(), videoServiceProxy, uri);        
			}
			catch(Exception e){
				Log.d(TAG, e.getMessage());
			}
			break; 
			
			
		case GET_RATING:
			break; 
			
		case GET_LIST_VIDEO:
			Collection<Video> videoList = null;
			
			try{
			   videoList = videoServiceProxy.getVideoList();
			   ContentResolver resolver = getApplicationContext().getContentResolver();
			   resolver.delete(VideoContract.BASE_VIDEO_URI,  null,  null);
			   
				if (videoList != null && videoList.size() > 0) {
					
					
					
					Cursor cursor;

					for (Video v : videoList) {				
						Uri videoUri = VideoContract.buildVideoUri(v.getId());
						resolver.insert(VideoContract.BASE_VIDEO_URI, v.toContentValues());
						
						/*Log.d(TAG, "check video: " + videoUri.toString());
						cursor = resolver.query(videoUri, null, null, null, null);
						if (cursor.getCount() == 0) {
							Log.d(TAG, "insert");
							resolver.insert(VideoContract.BASE_VIDEO_URI, v.toContentValues());
						} else {
							Log.d(TAG, "update");
							resolver.update(videoUri, v.toContentValues(), null, null);
						}*/
					}
				}

			}
			catch(Exception e){
			   Log.d(TAG, e.getMessage());	
			}
			
			
			code = ResponseMsg.MSG_DB_UPDATED;
			int size = (videoList == null)? -1 : videoList.size();
			Log.d(TAG, "N videos: " + Integer.toString(size));
			break; 
		}
		
		sendResponse(messenger, result, code, id);

	}

	private String uploadVideo(Context context, VideoServiceProxy videoServiceProxy, Uri videoUri) {

		String filePath = VideoMediaStoreUtils.getPath(context, videoUri);
		Video androidVideo = VideoMediaStoreUtils.getVideo(context, filePath);

		if (androidVideo != null) {

			File videoFile = new File(filePath);

			if (videoFile.length() < Constants.MAX_SIZE_MEGA_BYTE) {
				Video receivedVideo = videoServiceProxy.addVideo(androidVideo);
                
				if (receivedVideo != null) {
					
					Log.d(TAG, "video id: " + receivedVideo.getId());
					// Finally, upload the Video data to the server
					// and get the status of the uploaded video data.
					VideoStatus status = videoServiceProxy.setVideoData(receivedVideo.getId(), new TypedFile(
							receivedVideo.getContentType(), videoFile));

					if (status.getState() == VideoState.READY) {
						getApplicationContext().getContentResolver().insert(VideoContract.BASE_VIDEO_URI, receivedVideo.toContentValues());
						return STATUS_UPLOAD_SUCCESSFUL;
					}
				}
			} else
				return STATUS_UPLOAD_ERROR_FILE_TOO_LARGE;
		}

		return STATUS_UPLOAD_ERROR;
	}
	
	
    private void sendResponse(Messenger messenger, String text, int code, long id){
        Log.d(TAG, "send response");
        if (messenger == null){
        	return;
        }
        
        Message msg = ResponseMsg.makeMsg(text, code, id);

        try {
            messenger.send(msg);
        } catch (RemoteException e) {
        	Log.d(TAG, e.getMessage());
            e.printStackTrace();
        }
    }

}
