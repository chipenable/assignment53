package com.example.videouploadapp.model;

import java.io.Serializable;
import java.util.Objects;

import android.content.ContentValues;
import android.database.Cursor;

import com.example.videouploadapp.provider.VideoContract;

public class Video implements Serializable{

	private long id;
	private String title;
	private long duration;
	private String location;
	private String subject;
	private String contentType;
	private String dataUrl;
	private float rating;
	
	public Video(){

	}

	public Video(String title, long duration, String contentType, String location) {
		this.title = title;
		this.duration = duration;
		this.contentType = contentType;
		this.location = location;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public long getDuration() {
		return duration;
	}

	public void setDuration(long duration) {
		this.duration = duration;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getDataUrl() {
		return dataUrl;
	}

	public void setDataUrl(String dataUrl) {
		this.dataUrl = dataUrl;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	@Override
	public int hashCode() {
		return Objects.hash(getTitle(), getDuration());
	}

	@Override
	public boolean equals(Object obj) {
		return (obj instanceof Video) && Objects.equals(getTitle(), ((Video) obj).getTitle())
				&& getDuration() == ((Video) obj).getDuration();
	}
	
	public ContentValues toContentValues(){
		 ContentValues cv = new ContentValues();
		 cv.put(VideoContract.COL_VIDEO_ID, id);
		 cv.put(VideoContract.COL_TITLE, title);
		 cv.put(VideoContract.COL_DURATION, duration);
		 cv.put(VideoContract.COL_CONTENT_TYPE,  contentType);
		 cv.put(VideoContract.COL_LOCATION,  location);
		 cv.put(VideoContract.COL_DATA_URL, dataUrl);
		 cv.put(VideoContract.COL_RATING, rating);
		 return cv;
	}
	
	public static Video makeFromCursor(Cursor c){
		Video video = new Video();
		
		return video;
	}

}
