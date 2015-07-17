package com.example.videouploadapp.services;

import java.lang.ref.WeakReference;

import com.example.videouploadapp.provider.VideoContract;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

public class DataLoader extends Fragment {
	
	public static final String TAG = "DataLoader";

    public static final String COM_LOAD = "load";
    public static final String COM_FILTER = "filter";

    private DataLoaderCallbacks mCallbacks = null;
    private Handler mHandler; 
    private Cursor mCursor;

    public interface DataLoaderCallbacks {
        void onPostExecute(Cursor cursor);
    }

    /***************************************************************/

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mCallbacks = (DataLoaderCallbacks) activity;
        Log.d(TAG, "onAttach");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
        Log.d(TAG, "onDetach");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
                
        /*it handles messages from VideoService and starts LoaderTask*/
		mHandler = new Handler(new Handler.Callback() {	
			@Override
			public boolean handleMessage(Message msg) {
				if(ResponseMsg.isDataUpdated(msg)){
					
					long id = ResponseMsg.getId(msg);
					Uri uri = (id == -1)? VideoContract.BASE_VIDEO_URI: VideoContract.buildVideoUri(id);
					Log.d(TAG, uri.toString());
					new LoaderTask(getActivity()).execute(uri);

					String result = ResponseMsg.getText(msg);
					if (result != null){
					   Context context = getActivity();
					   if (context != null){
					      Toast.makeText(context, result, Toast.LENGTH_SHORT).show();
					   }
					}
					return true; 
				}
				return false;
			}
			
		});
        
    }
    
    public void setRating(Uri uri, float rating){
    	getActivity().startService(VideoService.makeSetRatingIntent(getActivity(), uri, rating, mHandler));
    }
    
    
    public void getVideos(){
    	getActivity().startService(VideoService.makeGetListVideoIntent(getActivity(),  mHandler));
    }
    
    public void uploadVideo(Uri uri){
    	getActivity().startService(VideoService.makeUploadIntent(getActivity(),  uri,  mHandler));
    }
    
    public void downloadVideo(Uri uri){
    	getActivity().startService(VideoService.makeDownloadIntent(getActivity(),  uri,  mHandler));
    }
    
    public Cursor getCursor(){
    	return mCursor;
    }


    /**
     * **************************************************************
     */

    private class LoaderTask extends AsyncTask<Uri, Void, Cursor> {

        private WeakReference<Context> mContext = null;

        LoaderTask(Context context) {
            mContext = new WeakReference<Context>(context);
        }

        @Override
        protected Cursor doInBackground(Uri... params) {
            
        	Context context = mContext.get();
        	Cursor cursor = null; 
        	Uri uri = params[0];
        	
        	if (context != null){
			    cursor = context.getContentResolver().query(uri, null, null, null, null);
			    mCursor = cursor;
        	}
			
            return cursor;
        }

        @Override
        protected void onPostExecute(Cursor cursor) {
            if (mCallbacks != null) {
                mCallbacks.onPostExecute(cursor);
            }
            Log.d(TAG, "onPostExecute");
        }
    }
}
