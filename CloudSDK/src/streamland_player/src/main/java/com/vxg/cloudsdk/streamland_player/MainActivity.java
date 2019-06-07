/*
 *
  * Copyright (c) 2011-2017 VXG Inc.
 *
 */


package com.vxg.cloudsdk.streamland_player;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.MulticastLock;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.vxg.cloudsdk.CloudPlayerSDK;
import com.vxg.cloudsdk.CloudSDK;
import com.vxg.cloudsdk.Enums.CloudPlayerEvent;
import com.vxg.cloudsdk.Interfaces.ICloudObject;
import com.vxg.cloudsdk.Interfaces.ICloudPlayerCallback;
import com.vxg.cloudsdk.Objects.CloudPlayerConfig;
import com.vxg.ui.CloudPlayerView;
import com.vxg.ui.TimeLineSet;

import java.util.List;

public class MainActivity extends Activity implements OnClickListener, CloudPlayerView.OnCloudPlayerViewChange
{
	public final String TAG = MainActivity.class.getSimpleName();

	public  static		AutoCompleteTextView	edtId;
	private Button		btnConnect;
	private Button 		btnShowTimeline;
	private View 		viewLayout_info;

	private CloudPlayerView player;
	private CloudPlayerSDK playerSDK;

	private MulticastLock multicastLock = null;

	//SET Channel
	String msAccessToken = "";


    @Override
    public void onCreate(Bundle savedInstanceState)
	{
		setTitle(R.string.app_name);
		super.onCreate(savedInstanceState);

		WifiManager wifi = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
		multicastLock = wifi.createMulticastLock("multicastLock");
		multicastLock.setReferenceCounted(true);
		multicastLock.acquire();

		requestWindowFeature(Window.FEATURE_NO_TITLE);

		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
		getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

		getWindow().requestFeature(Window.FEATURE_PROGRESS);
		getWindow().setFeatureInt(Window.FEATURE_PROGRESS, Window.PROGRESS_VISIBILITY_ON);

		CloudSDK.setContext(this);
		CloudSDK.setLogLevel(2);
		CloudSDK.setLogEnable(true);

		setContentView(R.layout.view_camera_buy_id);

		Toast.makeText(getApplicationContext(), "CloudSDK ver="+CloudSDK.getLibVersion(), Toast.LENGTH_LONG).show();

		player = (CloudPlayerView) findViewById(R.id.player_view);
		player.setOnCloudPlayerViewChange(this);
		playerSDK = player.getCloudPlayerSDK();

		player.setTimeLineEnabled(true);
		//player.getTimeLine().setType(TimeLineSet.TimeLine.TYPE_RANGE);
		player.hideTimeLine();

		player.getTimeLine().setLoaderListener(new TimeLineSet.LoaderListener() {
			//Shows data wich has been loaded on timline directly from server. For debug purpose
			@Override
			public void onDataLoaded(List<Pair<Long, Long>> data) {
			if(data!=null){
				Log.d(TAG,"onDataLoaded to TimeLine size ="+data.size());
				int i=0;
				for(Pair p:data){
					Log.d(TAG,"onDataLoaded item"+(i++)+"start="+TimeLineSet.getDate((long)p.first)+" end="+TimeLineSet.getDate((long)p.second));
				}
			} else
				Log.d(TAG,"onDataLoaded data = null");
			}
		});

		btnShowTimeline =(Button) findViewById(R.id.but_show_timeline);
		btnShowTimeline.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (player.getTimeLineVisibility() == View.INVISIBLE) {
					player.showTimeLine();
					btnShowTimeline.setText("hide timeline");
				} else {
					player.hideTimeLine();
					btnShowTimeline.setText("show timeline");
				}
			}
		});

		this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

		edtId = (AutoCompleteTextView)findViewById(R.id.edit_id);
		edtId.setText(msAccessToken ==null?"": msAccessToken);

		edtId.setOnEditorActionListener(new OnEditorActionListener()
		{
			@Override
			public boolean onEditorAction(TextView v, int actionId,	KeyEvent event)
			{
				if (event != null&& (event.getKeyCode() == KeyEvent.KEYCODE_ENTER))
				{
					InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
					in.hideSoftInputFromWindow(edtId.getApplicationWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
					return true;

				}
				return false;
			}
		});

		btnConnect = (Button)findViewById(R.id.button_connect);
        btnConnect.setOnClickListener(this);

        RelativeLayout layout = (RelativeLayout) findViewById(R.id.main_view);
        layout.setOnTouchListener(new OnTouchListener() 
		{
			public boolean onTouch(View v, MotionEvent event) 
			{
				InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				if (getWindow() != null && getWindow().getCurrentFocus() != null && getWindow().getCurrentFocus().getWindowToken() != null)
					inputManager.hideSoftInputFromWindow(getWindow().getCurrentFocus().getWindowToken(), 0);
				return true;
			}
		});

		viewLayout_info = findViewById(R.id.layout_info);

    }

	public boolean check_access_token(){
    	String ch = edtId.getText().toString();
    	if(ch.length() > 0){
			msAccessToken = ch;
		}
		if(msAccessToken == null || msAccessToken.length()<1){

					new AlertDialog.Builder(this)
									.setTitle("Access token")
									.setMessage("Please set \'Access token\' STRING into msAccessToken variable ")
									.setNeutralButton("OK", null)
									.show();

					return false;
			}
		return true;
	}

	public void onClick(View v)
	{
		try{
			if(!check_access_token())
				return ;
			playerSDK.setSource(msAccessToken);
			player.play();

		} catch (Exception e){
			Log.e(TAG,e.toString());
			return;
		}
    }
 
  	@Override
  	protected void onDestroy() 
  	{
  		Log.e(TAG, "onDestroy()");

		System.gc();
		
		if (multicastLock != null) {
		    multicastLock.release();
		    multicastLock = null;
		}		
		super.onDestroy();
   	}	

	//=> CloudPlayerView.OnCloudPlayerViewChange
	@Override
	public void onFullscreen(boolean isFullscreen, int nLockOrientation) {
		if(isFullscreen){
			viewLayout_info.setVisibility(View.GONE);
		}else{
			viewLayout_info.setVisibility(View.VISIBLE);
		}

		if(nLockOrientation != -1){ // user clicked on player
			setRequestedOrientation(nLockOrientation);
		}else{
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
		}

	}

	@Override
	public void onShowControls(boolean isShow) {

	}

	@Override
	public void onPlayed() {

	}

	@Override
	public void onPaused() {

	}

	@Override
	public void onEOS() {

	}

	@Override
	public void onOutOfRange() {

	}

	@Override
	public void onError(int error) {

	}
	//<= CloudPlayerView.OnCloudPlayerViewChange

}
