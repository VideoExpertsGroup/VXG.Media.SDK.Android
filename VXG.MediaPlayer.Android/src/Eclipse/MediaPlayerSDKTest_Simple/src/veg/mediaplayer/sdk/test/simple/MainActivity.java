/*
 *
  * Copyright (c) 2011-2017 VXG Inc.
 *
 */

package veg.mediaplayer.sdk.test.simple;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.view.inputmethod.InputMethodManager;
import android.content.SharedPreferences;
import android.view.*;
import android.widget.RelativeLayout;
import android.widget.TextView.OnEditorActionListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import java.nio.ByteBuffer;
import java.util.Set;
import java.util.HashSet;
import java.util.ArrayList;

import android.preference.PreferenceManager;
import veg.mediaplayer.sdk.MediaPlayer;
import veg.mediaplayer.sdk.MediaPlayer.PlayerNotifyCodes;
import veg.mediaplayer.sdk.MediaPlayer.PlayerState;
import veg.mediaplayer.sdk.MediaPlayerConfig;

public class MainActivity extends Activity implements MediaPlayer.MediaPlayerCallback
{
	private MediaPlayer player = null;
	
	@Override
    public void onCreate(Bundle savedInstanceState) 
	{
		setTitle(R.string.app_name);
		super.onCreate(savedInstanceState);

		setContentView(R.layout.main);
		
		player = (MediaPlayer)findViewById(R.id.playerView);
		
		final ImageView pauseIndicator = (ImageView)findViewById(R.id.pauseIndicator);

    	MediaPlayerConfig conf = new MediaPlayerConfig();
    	conf.setDecodingType(0);
    	conf.setConnectionUrl("rtsp://184.72.239.149/vod/mp4:BigBuckBunny_115k.mov");
    	//conf.setConnectionUrl("rtsp://10.20.16.36/1.ts");
       	conf.setStartOffest(60000);

    	player.Open(conf, this);
    	
        player.setOnTouchListener(new OnTouchListener() 
		{
			public boolean onTouch(View v, MotionEvent event) 
			{
				switch(event.getAction())
				{
					case MotionEvent.ACTION_DOWN:
					{
						if (player != null && PlayerState.forType(player.getState()) > PlayerState.forType(PlayerState.Opened) && 
								PlayerState.forType(player.getState()) < PlayerState.forType(PlayerState.Closing))
						{
							
							if (player.getState() == PlayerState.Started)
							{
								player.Pause();
								pauseIndicator.setVisibility(View.VISIBLE);
							}
							else
							{
				    	    	player.Play();
								pauseIndicator.setVisibility(View.GONE);
							}
						}
					}
				}
				
				return true;
			}
		});
    	
	}

  	@Override
  	protected void onDestroy() 
  	{
  		Log.e(TAG, "onDestroy()");
  		player.Close();

		super.onDestroy();
   	}	
	
	private Handler handler = new Handler() 
    {
	        @Override
	        public void handleMessage(Message msg) 
	        {
	        	PlayerNotifyCodes status = (PlayerNotifyCodes) msg.obj;
	        }
	};
	
	// callback from Native Player 
	public int Status(int arg)
	{
    	Log.e(TAG, "Form Native Player status: " + arg);
		Message msg = new Message();
		msg.obj = PlayerNotifyCodes.forValue(arg);

		if (handler != null)
			handler.sendMessage(msg);
		
    	return 0;
    }

	@Override
	public int OnReceiveData(ByteBuffer buffer, int size, long pts) 
	{
		// TODO Auto-generated method stub
		return 0;
	}

	private static final String TAG = "MediaPlayerSDKTest.Simple";
}
