package com.example.videouploadapp.services;

import java.util.ArrayList;
import java.util.Collection;

import android.os.Bundle;
import android.os.Message;

import com.example.videouploadapp.model.Video;


public class ResponseMsg {

    private static final String MSG_DB_STATE = "db_state";
    private static final String MSG_TEXT = "text";
    public static final String MSG_ID = "id";
    public static final int MSG_DB_UPDATED = 1;
    

    public static Message makeMsg(String text, int code){
            Message msg = Message.obtain();
            Bundle bundle = new Bundle();
            bundle.putString(MSG_TEXT, text);         
            bundle.putInt(MSG_DB_STATE, code);
            msg.setData(bundle);
            return msg;
    }
    
    public static Message makeMsg(String text, int code, long id){
        Message msg = Message.obtain();
        Bundle bundle = new Bundle();
        bundle.putString(MSG_TEXT, text);         
        bundle.putInt(MSG_DB_STATE, code);
        bundle.putLong(MSG_ID, id);
        msg.setData(bundle);
        return msg;
}

    public static String getText(Message msg){
        return msg.getData().getString(MSG_TEXT);
    }
    
    public static long getId(Message msg){
    	return msg.getData().getLong(MSG_ID);
    }
    
    public static boolean isDataUpdated(Message msg){
        return (msg.getData().getInt(MSG_DB_STATE) == MSG_DB_UPDATED);
    }
}
