/*
 *
  * Copyright (c) 2011-2017 VXG Inc.
 *
 */


package com.vxg.videoplayertest.Models;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

public class SharedSettings 
{
    private static final String TAG 	 = "MediaPlayerTest.Settings";

	// Video source URL (RTSP,HTTP, HTTPS, RTMP, UDP, RTP and other protocols are supported) 
	public String URL = "";
	
	// Latency control options
	// 0 - Diabled - latency control is disabled
	// 1 - Extra   - Disable audio and video syncronization and video is played with max speed
	// 2 - Minimal - ~ 3-6 frames
	// 3 - Middle  - ~ 10-20 frames
	// 4 - Max     - ~ 25-135 Frames
	public  int LatencyControl = 0; 

	// Transport protocol for RTSP protocol
	public  int connectionProtocol 		= -1;	// -2 - AUTO1(UDP,TCP,HTTP), 
												// -1 - AUTO2 (TCP + HTTP)
												//  0 - UDP
												//  1 - TCP 
												//  2 - HTTP 
												//  3 - HTTPS  
	public  int connectionDetectionTime = 2000;// in milliseconds
	public  int connectionBufferingTime = 1000;	// in milliseconds

	// decoder
	public  int 	decoderType 		= 1;				// 0 - s/w, 1 - h/w

	// renderer

	public  int 	rendererEnableAspectRatio = 1; 	// 0 - resize, 1 - aspect
	
	// synchro
	public  int 	synchroEnable		= 1;				// enable audio video synchro
	//public  int 	synchroNeedDropVideoFrames = 1;	// drop video frames if it older
	
	
	//public  long	OpenAdLastTime = 0;
	
	private  Context m_Context 			= null;
	private  SharedPreferences settings = null;
	private  SharedPreferences.Editor editor = null;
	
	private static volatile SharedSettings _inst = null;
	private SharedSettings()
	{
		m_Context = null;
	}

	private SharedSettings(final Context mContext)
	{
		m_Context = mContext;
	}
	
	public static synchronized SharedSettings getInstance(final Context mContext)
	{
		if (_inst == null)
		{
			_inst = new SharedSettings(mContext);
			_inst.loadPrefSettings();
			_inst.savePrefSettings();
			Log.e(TAG, "SharedSettings: getInstance.");
		}
		return _inst;
	}

	public static synchronized SharedSettings getInstance()
	{
		return _inst;
	}
	
 	public void loadPrefSettings() 
	{
		// load preferences settings to local variables
 		if (settings == null)
 			settings = PreferenceManager.getDefaultSharedPreferences(m_Context);

		URL = settings.getString("URL", "rtsp://3.84.6.190/vod/mp4:BigBuckBunny_115k.mov");
		
		LatencyControl = settings.getInt("LatencyControl", 0);

 		connectionProtocol = settings.getInt("connectionProtocol", -1);
 		connectionDetectionTime = settings.getInt("connectionDetectionTime", 2000);
 		connectionBufferingTime = settings.getInt("connectionBufferingTime", 1000);
 		
 		decoderType = settings.getInt("decoderType", 1);
		rendererEnableAspectRatio = settings.getInt("rendererEnableAspectRatio", 1);
 		synchroEnable = settings.getInt("synchroEnable", 1);
 //		synchroNeedDropVideoFrames = settings.getInt("synchroNeedDropVideoFrames", 1);

// 		OpenAdLastTime = settings.getLong("OpenAdLastTime", 0);
		Log.e(TAG, "SharedSettings: Load settings.");
	}

	public void savePrefSettings() 
	{
 		if (settings == null)
 			settings = PreferenceManager.getDefaultSharedPreferences(m_Context);
 		
		// save preferences settings
 		if (editor == null)
 			editor = settings.edit();

		editor.putString("URL",URL);
		
		editor.putInt("LatencyControl", LatencyControl);
		
 		
 		editor.putInt("connectionProtocol", connectionProtocol);
 		editor.putInt("connectionDetectionTime", connectionDetectionTime);
 		editor.putInt("connectionBufferingTime", connectionBufferingTime);
 		
 		editor.putInt("decoderType", decoderType);
		editor.putInt("rendererEnableAspectRatio", rendererEnableAspectRatio);
 		editor.putInt("synchroEnable", synchroEnable);
// 		editor.putInt("synchroNeedDropVideoFrames", synchroNeedDropVideoFrames);

 //		editor.putLong("OpenAdLastTime", OpenAdLastTime);

		editor.commit();
		Log.e(TAG, "SharedSettings: Save settings.");
	}
	
 	public void loadOpenAdLastTime() {
		// load preferences settings to local variables
 		if (settings == null)
 			settings = PreferenceManager.getDefaultSharedPreferences(m_Context);
 		
// 		OpenAdLastTime = settings.getLong("OpenAdLastTime", 0);
	}

	public void saveOpenAdLastTime(final long value) 
	{
 		if (settings == null)
 			settings = PreferenceManager.getDefaultSharedPreferences(m_Context);
 		
		// save preferences settings
 		if (editor == null)
 			editor = settings.edit();

 //		OpenAdLastTime = value;
// 		editor.putLong("OpenAdLastTime", OpenAdLastTime);
		editor.commit();
	}

	public boolean getBooleanValueForKey(final String key) {
 		if (key.isEmpty())
 			return false;
 		
		// load preferences settings to local variables
 		if (settings == null)
 			settings = PreferenceManager.getDefaultSharedPreferences(m_Context);
 		
		Log.e(TAG, "SharedSettings: getBooleanValueForKey " + key + ":" + settings.getBoolean(key, false));
		return settings.getBoolean(key, false);
	}
	
 	public void setBooleanValueForKey(final String key, final boolean value) {
 		if (key.isEmpty())
 			return;
 		
 		if (settings == null)
 			settings = PreferenceManager.getDefaultSharedPreferences(m_Context);
 		
		// save preferences settings
 		if (editor == null)
 			editor = settings.edit();

 		editor.putBoolean(key, value);
		editor.commit();
	}

 	public long getLongValueForKey(final String key) {
 		if (key.isEmpty())
 			return 0;
 		
		// load preferences settings to local variables
 		if (settings == null)
 			settings = PreferenceManager.getDefaultSharedPreferences(m_Context);
 		
		Log.e(TAG, "SharedSettings: getLongValueForKey " + key + ":" + settings.getLong(key, 0));
		return settings.getLong(key, 0);
	}
	
 	public void setLongValueForKey(final String key, final long value) {
 		if (key.isEmpty())
 			return;
 		
 		if (settings == null)
 			settings = PreferenceManager.getDefaultSharedPreferences(m_Context);
 		
		// save preferences settings
 		if (editor == null)
 			editor = settings.edit();

 		editor.putLong(key, value);
		editor.commit();
	}
 	
 	public int getIntValueForKey(final String key) {
 		if (key.isEmpty())
 			return 0;
 		
		// load preferences settings to local variables
 		if (settings == null)
 			settings = PreferenceManager.getDefaultSharedPreferences(m_Context);
 		
		Log.e(TAG, "SharedSettings: getIntValueForKey " + key + ":" + settings.getInt(key, 0));
		return settings.getInt(key, 0);
	}
	
 	public void setIntValueForKey(final String key, final int value) {
 		if (key.isEmpty())
 			return;
 		
 		if (settings == null)
 			settings = PreferenceManager.getDefaultSharedPreferences(m_Context);
 		
		// save preferences settings
 		if (editor == null)
 			editor = settings.edit();

 		editor.putInt(key, value);
		editor.commit();
	}
	
}
