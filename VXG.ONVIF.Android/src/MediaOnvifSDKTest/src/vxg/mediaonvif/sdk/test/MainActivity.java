/*
 *
 * Copyright (c) 2011-2017 VXG Inc.
 *
 */


package vxg.mediaonvif.sdk.test;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.MulticastLock;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.Button;
import android.widget.TextView;
import android.view.inputmethod.InputMethodManager;
import android.view.WindowManager;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.view.*;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView.OnEditorActionListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.ArrayList;

import android.preference.PreferenceManager;
import vxg.mediaonvif.sdk.MediaOnvif;
import vxg.mediaonvif.sdk.MediaOnvif.MediaOnvifCallback;
import vxg.mediaonvif.sdk.MediaOnvif.OnvifModes;
import vxg.mediaonvif.sdk.MediaOnvif.OnvifNotifyCodes;
import vxg.mediaonvif.sdk.MediaOnvif.OnvifProperties;
import vxg.mediaonvif.sdk.MediaOnvif.OnvifState;
import vxg.mediaonvif.sdk.MediaOnvifConfig;


public class MainActivity extends Activity implements MediaOnvif.MediaOnvifCallback, OnClickListener
{
    private static final String TAG 	 = "VXG.ONVIF.Test";
    
	public  static AutoCompleteTextView	edtIpAddress;
	public  static ArrayAdapter<String> edtIpAddressAdapter;
	public  static Set<String>			edtIpAddressHistory;
	private Button						btnConnect;
	private Button						btnHistory;
	private Button						btnPTZ;
	//private Button						btnShot;
	//private Button						btnRecord;
	//private boolean						is_record = false;

	private StatusProgressTask 			mProgressTask = null;
	
	private SharedPreferences 			settings;
    private SharedPreferences.Editor 	editor;

    private boolean 					playing = false;
    private MediaOnvif 				onvif = null;
    private MainActivity 				mthis = null;

    private RelativeLayout 				onvifStatus = null;
    private TextView 					onvifStatusText = null;
    private TextView 					onvifHwStatus = null;
    
	private boolean test = true;

	
    private MulticastLock multicastLock = null;
    
	private Object waitOnMe = new Object();
	private int mOldMsg = 0;

	private Toast toastShot = null;
	
	// Event handler
	
	private Handler handler = new Handler() 
    {
		String strText = "Connecting";
		
		String sText;
		String sCode;
		
		@Override
	    public void handleMessage(Message msg) 
	    {
	    	OnvifNotifyCodes status = (OnvifNotifyCodes) msg.obj;
	        switch (status) 
	        {
		case PLP_CONNECTED_SUCCESSFUL:
				 //if your index is out of profiles array range then the nearest element is set and MediaOnvif.OnvifNotifyCodes.OUT_OF_RANGE_ERROR comes
				//othewise MediaOnvif.OnvifNotifyCodes.OK comes in response
				Log.e(TAG, "handleMessage PLP_CONNECTED_SUCCESSFUL");
				 
				MediaOnvif.OnvifNotifyCodes state=onvif.setPropInt(MediaOnvif.OnvifProperties.PP_PROPERTY_CUR_PROFILE.val(),0);
				MediaOnvif.OnvifNotifyCodes state1=onvif.setPropInt(MediaOnvif.OnvifProperties.PP_PROPERTY_CUR_VIDEO_ENCODER_PROPERTIES.val(),0);
				MediaOnvif.OnvifNotifyCodes state5=onvif.setPropInt(MediaOnvif.OnvifProperties.PP_PROPERTY_VIDEO_ENCODER_FRAMERATE_LIMIT.val()-2,0);

								
				int n = onvif.getPropInt(MediaOnvif.OnvifProperties.PP_PROPERTY_PROFILES_SIZE);
				Log.e(TAG, "PP_PROPERTY_PROFILES_SIZE = " + n);
				String s;
				int i;

				// Network settings
				s = onvif.getPropString(MediaOnvif.OnvifProperties.PP_PROPERTY_NETWORK_HWADRESS);Log.e(TAG, "PP_PROPERTY_NETWORK_HWADRESS = " + s);
				s = onvif.getPropString(MediaOnvif.OnvifProperties.PP_PROPERTY_NETWORK_IPV4_ADDRESS);Log.e(TAG, "PP_PROPERTY_NETWORK_IPV4_ADDRESS = "+ s);
				s = onvif.getPropString(MediaOnvif.OnvifProperties.PP_PROPERTY_VIDEO_ENCODER_MULTICAST_IPV4_ADDRESS);Log.e(TAG, "PP_PROPERTY_VIDEO_ENCODER_MULTICAST_IPV4_ADDRESS = "+ s);
				s = onvif.getPropString(MediaOnvif.OnvifProperties.PP_PROPERTY_AUDIO_ENCODER_MULTICAST_IPV4_ADDRESS);Log.e(TAG, "PP_PROPERTY_AUDIO_ENCODER_MULTICAST_IPV4_ADDRESS = "+ s);
				s = onvif.getPropString(MediaOnvif.OnvifProperties. PP_PROPERTY_NETWORK_IPV4_ADDRESS);Log.e(TAG, "PP_PROPERTY_NETWORK_IPV4_ADDRESS = "+ s);
				s = onvif.getPropString(MediaOnvif.OnvifProperties.PP_PROPERTY_VIDEO_ENCODER_NAME);Log.e(TAG, "PP_PROPERTY_VIDEO_ENCODER_NAME = "+ s);
				i = onvif.getPropInt(MediaOnvif.OnvifProperties.PP_PROPERTY_HTTP_ENABLED);Log.e(TAG, "PP_PROPERTY_HTTP_ENABLED = "+ i);
				i = onvif.getPropInt(MediaOnvif.OnvifProperties.PP_PROPERTY_HTTPS_ENABLED);Log.e(TAG, "PP_PROPERTY_HTTPS_ENABLED = "+ i);
				i = onvif.getPropInt(MediaOnvif.OnvifProperties.PP_PROPERTY_HTTP_PORT);Log.e(TAG, "PP_PROPERTY_HTTP_PORT = "+ i);
				i = onvif.getPropInt(MediaOnvif.OnvifProperties.PP_PROPERTY_HTTPS_PORT);Log.e(TAG, "PP_PROPERTY_HTTPS_PORT = "+ i);
				i = onvif.getPropInt(MediaOnvif.OnvifProperties.PP_PROPERTY_RTSP_PORT);Log.e(TAG, "PP_PROPERTY_RTSP_PORT = "+ i);
				i = onvif.getPropInt(MediaOnvif.OnvifProperties.PP_PROPERTY_RTSP_ENABLED);Log.e(TAG, "PP_PROPERTY_RTSP_ENABLED = "+ i);
				

				// Common settins
				s = onvif.getPropString(MediaOnvif.OnvifProperties.PP_PROPERTY_FIRMWAREVERSION);Log.e(TAG, "PP_PROPERTY_FIRMWAREVERSION = "+ s);
				s = onvif.getPropString(MediaOnvif.OnvifProperties.PP_PROPERTY_HARD_WARE_ID);Log.e(TAG, "PP_PROPERTY_HARD_WARE_ID = "+ s);
				s = onvif.getPropString(MediaOnvif.OnvifProperties.PP_PROPERTY_MANUFACTURER);Log.e(TAG, "PP_PROPERTY_MANUFACTURER = "+ s);
				s = onvif.getPropString(MediaOnvif.OnvifProperties.PP_PROPERTY_MODEL);Log.e(TAG, "PP_PROPERTY_MODEL = "+ s);
				s = onvif.getPropString(MediaOnvif.OnvifProperties.PP_PROPERTY_SERIAL_NUMBER);Log.e(TAG, "PP_PROPERTY_SERIAL_NUMBER = "+ s);

				// Video Encoder settings
				i = onvif.getPropInt(MediaOnvif.OnvifProperties.PP_PROPERTY_VIDEO_ENCODER_HEIGHT);Log.e(TAG, "PP_PROPERTY_VIDEO_ENCODER_HEIGHT = "+ i);
				i = onvif.getPropInt(MediaOnvif.OnvifProperties.PP_PROPERTY_VIDEO_ENCODER_WIDTH);Log.e(TAG, "PP_PROPERTY_VIDEO_ENCODER_WIDTH = "+ i);
				i = onvif.getPropInt(MediaOnvif.OnvifProperties.PP_PROPERTY_VIDEO_ENCODER_MPEG4FLAG);Log.e(TAG, "PP_PROPERTY_VIDEO_ENCODER_MPEG4FLAG = "+ i);
				i = onvif.getPropInt(MediaOnvif.OnvifProperties.PP_PROPERTY_VIDEO_ENCODER_H264FLAG);Log.e(TAG, "PP_PROPERTY_VIDEO_ENCODER_H264FLAG = "+ i);
				i = onvif.getPropInt(MediaOnvif.OnvifProperties.PP_PROPERTY_VIDEO_ENCODER_RATECONTROLFLAG);Log.e(TAG, "PP_PROPERTY_VIDEO_ENCODER_RATECONTROLFLAG = "+ i);
				i = onvif.getPropInt(MediaOnvif.OnvifProperties.PP_PROPERTY_VIDEO_ENCODER_GOVLENGTH);Log.e(TAG, "PP_PROPERTY_VIDEO_ENCODER_GOVLENGTH = "+ i);
				i = onvif.getPropInt(MediaOnvif.OnvifProperties.PP_PROPERTY_VIDEO_ENCODER_BITRATE_LIMIT);Log.e(TAG, "PP_PROPERTY_VIDEO_ENCODER_BITRATE_LIMIT = "+ i);
				i = onvif.getPropInt(MediaOnvif.OnvifProperties.PP_PROPERTY_VIDEO_ENCODER_FRAMERATE_LIMIT);Log.e(TAG, "PP_PROPERTY_VIDEO_ENCODER_FRAMERATE_LIMIT = "+ i);
				i = onvif.getPropInt(MediaOnvif.OnvifProperties.PP_PROPERTY_VIDEO_ENCODER_MULTICAST_PORT);Log.e(TAG, "PP_PROPERTY_VIDEO_ENCODER_MULTICAST_PORT = "+ i);

				// Audio Encoder settings
				i = onvif.getPropInt(MediaOnvif.OnvifProperties.PP_PROPERTY_AUDIO_ENCODER_SAMPLE_RATE);Log.e(TAG, "PP_PROPERTY_AUDIO_ENCODER_SAMPLE_RATE = "+ i);
				i = onvif.getPropInt(MediaOnvif.OnvifProperties.PP_PROPERTY_AUDIO_ENCODER_BIT_RATE);Log.e(TAG, "PP_PROPERTY_AUDIO_ENCODER_BIT_RATE = "+ i);
				i = onvif.getPropInt(MediaOnvif.OnvifProperties.PP_PROPERTY_AUDIO_ENCODER_MULTICAST_PORT);Log.e(TAG, "PP_PROPERTY_AUDIO_ENCODER_MULTICAST_PORT = "+ i);

				//set test
				int unity_test_SET = 0;
				if (unity_test_SET == 1)
					{
						MediaOnvif.OnvifNotifyCodes stat;
						// VIDEO Encoder
						stat=onvif.setPropInt(MediaOnvif.OnvifProperties.PP_PROPERTY_VIDEO_ENCODER_HEIGHT.val(),15);
						stat=onvif.setPropInt(MediaOnvif.OnvifProperties.PP_PROPERTY_VIDEO_ENCODER_WIDTH.val(),480);
						stat=onvif.setPropInt(MediaOnvif.OnvifProperties.PP_PROPERTY_VIDEO_ENCODER_MPEG4FLAG.val(),1);
						stat=onvif.setPropInt(MediaOnvif.OnvifProperties.PP_PROPERTY_VIDEO_ENCODER_H264FLAG.val(),1);
						stat=onvif.setPropInt(MediaOnvif.OnvifProperties.PP_PROPERTY_VIDEO_ENCODER_RATECONTROLFLAG.val(),0);
						stat=onvif.setPropInt(MediaOnvif.OnvifProperties.PP_PROPERTY_VIDEO_ENCODER_GOVLENGTH.val(),100);
						stat=onvif.setPropInt(MediaOnvif.OnvifProperties.PP_PROPERTY_VIDEO_ENCODER_BITRATE_LIMIT.val(),15);
						stat=onvif.setPropInt(MediaOnvif.OnvifProperties.PP_PROPERTY_VIDEO_ENCODER_MULTICAST_PORT.val(),1024);
						stat=onvif.setPropInt(MediaOnvif.OnvifProperties.PP_PROPERTY_VIDEO_ENCODER_FRAMERATE_LIMIT.val(),15);

						// Audio Encoder
						stat=onvif.setPropInt(MediaOnvif.OnvifProperties.PP_PROPERTY_AUDIO_ENCODER_SAMPLE_RATE.val(),15);
						stat=onvif.setPropInt(MediaOnvif.OnvifProperties.PP_PROPERTY_AUDIO_ENCODER_BIT_RATE.val(),15);
						stat=onvif.setPropInt(MediaOnvif.OnvifProperties.PP_PROPERTY_AUDIO_ENCODER_MULTICAST_PORT.val(),15);
	

						// NetWork settins											
						stat=onvif.setPropInt(MediaOnvif.OnvifProperties.PP_PROPERTY_HTTP_ENABLED.val(),15);
						stat=onvif.setPropString(MediaOnvif.OnvifProperties.PP_PROPERTY_NETWORK_HWADRESS,"225.1.1.1");
						stat=onvif.setPropString(MediaOnvif.OnvifProperties.PP_PROPERTY_NETWORK_IPV4_ADDRESS,"192.168.0.1");
						stat=onvif.setPropString(MediaOnvif.OnvifProperties.PP_PROPERTY_VIDEO_ENCODER_MULTICAST_IPV4_ADDRESS,"225.1.1.1");
						stat=onvif.setPropString(MediaOnvif.OnvifProperties.PP_PROPERTY_VIDEO_ENCODER_NAME,"some");
						stat=onvif.setPropInt(MediaOnvif.OnvifProperties.PP_PROPERTY_HTTP_PORT.val(),888);
					}
			break;
				
			case PLP_CONNECTED_AUTH_FAILED:
				Log.e(TAG, "handleMessage:  PLP_CONNECTED_AUTH_FAILED");
			break;
			
	        default:
				break;
	        }
	    }
	};

	// callback from Native Onvif 
	@Override
	public int OnReceiveData(ByteBuffer buffer, int size, long pts) 
	{
		Log.e(TAG, "Form Native Onvif OnReceiveData: size: " + size + ", pts: " + pts);
		return 0;
	}
    

	// All event are sent to event handlers    
	@Override
	public int Status(int arg)
	{
		
		OnvifNotifyCodes status = OnvifNotifyCodes.forValue(arg);
		if (handler == null || status == null)
			return 0;
		
		Log.e(TAG, "From Native Onvif status: " + arg);
	    switch (OnvifNotifyCodes.forValue(arg)) 
	    {
	        default:     
				Message msg = new Message();
				msg.obj = status;
				//handler.removeMessages(mOldMsg);
				//mOldMsg = msg.what;
				handler.sendMessage(msg);
	    }
	    
		return 0;
	}
	
	
    @Override
    public void onCreate(Bundle savedInstanceState) 
	{
		String  strUrl;

		setTitle(R.string.app_name);
		super.onCreate(savedInstanceState);

		
		getWindow().requestFeature(Window.FEATURE_PROGRESS);
		getWindow().setFeatureInt(Window.FEATURE_PROGRESS, Window.PROGRESS_VISIBILITY_ON);
		
		setContentView(R.layout.main);
		mthis = this;
		
		settings = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);

		SharedSettings.getInstance(this).loadPrefSettings();
		SharedSettings.getInstance().savePrefSettings();
		
		onvifStatus 		= (RelativeLayout)findViewById(R.id.playerStatus);
		onvifStatusText 	= (TextView)findViewById(R.id.playerStatusText);
		onvifHwStatus 		= (TextView)findViewById(R.id.playerHwStatus);
		
		//onvif = (MediaOnvif)findViewById(R.id.onvifView);
		onvif = new MediaOnvif(this);
		

		strUrl = settings.getString("connectionUrl", "http://10.20.16.80/onvif_device");
		
		HashSet<String> tempHistory = new HashSet<String>();
		
		tempHistory.add("http://10.20.16.80/onvif_device");
		
		edtIpAddressHistory = settings.getStringSet("connectionHistory", tempHistory);

		this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN); 
	
		edtIpAddress = (AutoCompleteTextView)findViewById(R.id.edit_ipaddress);
		edtIpAddress.setText(strUrl);

		edtIpAddress.setOnEditorActionListener(new OnEditorActionListener() 
		{
			@Override
			public boolean onEditorAction(TextView v, int actionId,	KeyEvent event) 
			{
				if (event != null&& (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) 
				{
					InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
					in.hideSoftInputFromWindow(edtIpAddress.getApplicationWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
					return true;
	
				}
				return false;
			}
		});

		btnHistory = (Button)findViewById(R.id.button_history);

		// Array of choices
		btnHistory.setOnClickListener(new View.OnClickListener() 
		{
			public void onClick(View v) 
			{
				InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				in.hideSoftInputFromWindow(MainActivity.edtIpAddress.getApplicationWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
				
				if (edtIpAddressHistory.size() <= 0)
					return;

				String urlHistory[] = {	};

				MainActivity.edtIpAddressAdapter = new ArrayAdapter<String>(MainActivity.this, R.layout.history_item, new ArrayList<String>(edtIpAddressHistory));
				MainActivity.edtIpAddress.setAdapter(MainActivity.edtIpAddressAdapter);
				MainActivity.edtIpAddress.showDropDown();
			}   
		});

		btnConnect = (Button)findViewById(R.id.button_connect);
        btnConnect.setOnClickListener(this);
        
		btnPTZ = (Button)findViewById(R.id.button_ptz);
        btnPTZ.setOnClickListener(new View.OnClickListener()
			{
			public void onClick(View v) 
			{
				// Indicates whether the field PanTilt is valid
				int PanTiltFlag = 1;
				// optional, An optional Timeout parameter, unit is second
				// 0 - Timeout Flag is disabled
				int Timeout	= 0; 
				// optional, 
				//Pan and tilt speed. 
				//The x component corresponds to pan and the y component to tilt. 
				//If omitted in a request, the current (if any) 
				//PanTilt movement should not be affected
				float PanTilt_x	= 0.0f;
				float PanTilt_y	= 0.5f;

				if (test == true)
					PanTilt_y	= 0.5f;
				else 
					PanTilt_y	= -0.5f;
	
				// optional, A zoom speed. 
				//If omitted in a request, 
				//the current (if any) Zoom movement should not be affected
				float Zoom		= 0.0f;


				onvif.PTZControl(PanTiltFlag,Timeout,PanTilt_x,PanTilt_y,Zoom);

				test = !test;
			}   
		});

        
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
        
		onvifStatusText.setText("DEMO VERSION");
		setShowControls();
        
    }

    private int[] mColorSwapBuf = null;                        // used by saveFrame()
    public Bitmap getFrameAsBitmap(ByteBuffer frame, int width, int height)
    {
        Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bmp.copyPixelsFromBuffer(frame);
        return bmp;
    }

    
    public void onClick(View v) 
	{
		SharedSettings.getInstance().loadPrefSettings();
		if (onvif != null)
		{
			if (!edtIpAddressHistory.contains(onvif.getConfig().getConnectionUrl()))
				edtIpAddressHistory.add(onvif.getConfig().getConnectionUrl());
			
			onvif.getConfig().setConnectionUrl(edtIpAddress.getText().toString());
			if (onvif.getConfig().getConnectionUrl().isEmpty())
				return;

			if (toastShot != null)
				toastShot.cancel();
			
			//onvif_record.Close();
			
			onvif.Close();
			if (playing)
			{
    			setUIDisconnected();
			}
			else
			{
    	    	SharedSettings sett = SharedSettings.getInstance();
    			boolean bPort = (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT);
    	    	int aspect = bPort ? 1 : sett.rendererEnableAspectRatio;
    	    	
    	    	MediaOnvifConfig conf = new MediaOnvifConfig();
    	    	
    	    	conf.setConnectionUrl(onvif.getConfig().getConnectionUrl());
				conf.setconnectionIP_ADDRESS("10.20.16.80");
				conf.setconnectionTCP_PORT(80);
				conf.setconnectionPath("/onvif_device");
				conf.setconnectionUser("admin");
				conf.setconnectionPassword("1234");
    	    	
				// Open Onvif	
        	    onvif.Open(conf, mthis);

				btnConnect.setText("Disconnect");
				
				
				playing = true;
			}
		}
    }
 
	protected void onPause()
	{
		Log.e(TAG, "onPause()");
		super.onPause();

		editor = settings.edit();
		editor.putString("connectionUrl", edtIpAddress.getText().toString());

		editor.putStringSet("connectionHistory", edtIpAddressHistory);
		editor.commit();
		
		if (onvif != null)
			onvif.onPause();
	}

	@Override
  	protected void onResume() 
	{
		Log.e(TAG, "onResume()");
		super.onResume();
		if (onvif != null)
			onvif.onResume();
  	}

  	@Override
	protected void onStart() 
  	{
      	Log.e(TAG, "onStart()");
		super.onStart();
		if (onvif != null)
			onvif.onStart();
	}

  	@Override
	protected void onStop() 
  	{
  		Log.e(TAG, "onStop()");
		super.onStop();
		if (onvif != null)
			onvif.onStop();
		
		if (toastShot != null)
			toastShot.cancel();

	}

    @Override
    public void onBackPressed() 
    {
		if (toastShot != null)
			toastShot.cancel();
		
		onvif.Close();
		if (!playing)
		{
	  		super.onBackPressed();
	  		return;			
		}

		setUIDisconnected();
    }
  	
  	@Override
  	public void onWindowFocusChanged(boolean hasFocus) 
  	{
  		Log.e(TAG, "onWindowFocusChanged(): " + hasFocus);
  		super.onWindowFocusChanged(hasFocus);
		if (onvif != null)
			onvif.onWindowFocusChanged(hasFocus);
  	}

  	@Override
  	public void onLowMemory() 
  	{
  		Log.e(TAG, "onLowMemory()");
  		super.onLowMemory();
		if (onvif != null)
			onvif.onLowMemory();
  	}

  	@Override
  	protected void onDestroy() 
  	{
  		Log.e(TAG, "onDestroy()");
		if (toastShot != null)
			toastShot.cancel();
		
		if (onvif != null)
			onvif.onDestroy();
		
		stopProgressTask();
		System.gc();
		
		if (multicastLock != null) {
		    multicastLock.release();
		    multicastLock = null;
		}		
		super.onDestroy();
   	}	
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) 
    {
		super.onCreateOptionsMenu(menu);

		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return true;
    }

	@Override
	public boolean onOptionsItemSelected(MenuItem item)  
	{
		switch (item.getItemId())  
		{
			case R.id.main_opt_settings:   
		
				SharedSettings.getInstance().loadPrefSettings();

				Intent intentSettings = new Intent(MainActivity.this, PreferencesActivity.class);     
				startActivity(intentSettings);

				break;
			case R.id.main_opt_clearhistory:     
			
				new AlertDialog.Builder(this)
				.setTitle("Clear History")
				.setMessage("Do you really want to delete the history?")
				.setPositiveButton("Yes", new DialogInterface.OnClickListener() 
				{
					public void onClick(DialogInterface dialog, int which) 
					{
						HashSet<String> tempHistory = new HashSet<String>();
						edtIpAddressHistory.clear();
						edtIpAddressHistory = tempHistory;  
					}
				})
				.setNegativeButton("No", new DialogInterface.OnClickListener() 
				{
					public void onClick(DialogInterface dialog, int which) 
					{
						// do nothing
					}
				}).show();
				break;
			case R.id.main_opt_exit:     
				finish();
				break;

		}
		return true;
	}

	protected void setUIDisconnected()
	{
		setTitle(R.string.app_name);
		btnConnect.setText("Connect");
		playing = false;
	}

	protected void setHideControls()
	{
		//btnShot.setVisibility(View.VISIBLE);
		edtIpAddress.setVisibility(View.GONE);
		btnHistory.setVisibility(View.GONE);
		btnConnect.setVisibility(View.GONE);
	}

	protected void setShowControls()
	{
		setTitle(R.string.app_name);
		
		//btnShot.setVisibility(View.GONE);
		edtIpAddress.setVisibility(View.VISIBLE);
		btnHistory.setVisibility(View.VISIBLE);
		btnConnect.setVisibility(View.VISIBLE);
	}

	private void showStatusView() 
	{
		//onvif.setVisibility(View.INVISIBLE);
		onvifHwStatus.setVisibility(View.INVISIBLE);
		//onvif.setAlpha(0.0f);
		onvifStatus.setVisibility(View.VISIBLE);
		
	}
	
	private void showVideoView() 
	{
        onvifStatus.setVisibility(View.INVISIBLE);
 		//onvif.setVisibility(View.VISIBLE);
		onvifHwStatus.setVisibility(View.VISIBLE);

 		//SurfaceHolder sfhTrackHolder = onvif.getSurfaceView().getHolder();
		//sfhTrackHolder.setFormat(PixelFormat.TRANSPARENT);
		
		setTitle("");
	}
    
	private void startProgressTask(String text)
	{
		stopProgressTask();
	    
	    mProgressTask = new StatusProgressTask(text);
	    //mProgressTask.execute(text);
	    executeAsyncTask(mProgressTask, text);
	}
	
	private void stopProgressTask()
	{
		onvifStatusText.setText("");
		setTitle(R.string.app_name);
		
       	if (mProgressTask != null)
	    {
       		mProgressTask.stopTask();
	    	mProgressTask.cancel(true);
	    }
	}

	private class StatusProgressTask extends AsyncTask<String, Void, Boolean> 
    {
       	String strProgressTextSrc;
       	String strProgressText;
        Rect bounds = new Rect();
    	boolean stop = false;
      	
       	public StatusProgressTask(String text)
       	{
        	stop = false;
       		strProgressTextSrc = text;
       	}
       	
       	public void stopTask() { stop = true; }
       	
        @Override
        protected void onPreExecute() 
        {
        	super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(String... params) 
        {
            try 
            {
                if (stop) return true;

                String maxText = "Disconnected.....";//strProgressTextSrc + "....";
                int len = maxText.length();
            	onvifStatusText.getPaint().getTextBounds(maxText, 0, len, bounds);

               	strProgressText = strProgressTextSrc + "...";
                
            	Runnable uiRunnable = null;
                uiRunnable = new Runnable()
                {
                    public void run()
                    {
                        if (stop) return;

    	                onvifStatusText.setText(strProgressText);
    	            	
    	            	RelativeLayout.LayoutParams layoutParams = 
    	            		    (RelativeLayout.LayoutParams)onvifStatusText.getLayoutParams();
    	           		
    	           		layoutParams.width = bounds.width();
    	           		onvifStatusText.setLayoutParams(layoutParams);        	
    	            	onvifStatusText.setGravity(Gravity.NO_GRAVITY);
    	            	
                        synchronized(this) { this.notify(); }
                    }
                };
                
               	int nCount = 4;
              	do
            	{
                    try
                    {
                    	Thread.sleep(300);
                    }
                    catch ( InterruptedException e ) { stop = true; }
                   
                    if (stop) break;
                    
                	if (nCount <= 3)
                	{
                		strProgressText = strProgressTextSrc;
                		for (int i = 0; i < nCount; i++)
                			strProgressText = strProgressText + ".";
                	}
                    
                    synchronized ( uiRunnable )
                    {
                    	runOnUiThread(uiRunnable);
                        try
                        {
                            uiRunnable.wait();
                        }
                        catch ( InterruptedException e ) { stop = true; }
                    }
                    
                    if (stop) break;
                    
                    nCount++;
                    if (nCount > 3)
                    {
                    	nCount = 1;
                    	strProgressText = strProgressTextSrc;
                    }
            	}
              	
            	while(!isCancelled());
            } 
            catch (Exception e) 
            {
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) 
        {
            super.onPostExecute(result);
            mProgressTask = null;
        }
        @Override
        protected void onCancelled() 
        {
            super.onCancelled();
        }
    }
	
    static public <T> void executeAsyncTask(AsyncTask<T, ?, ?> task, T... params) 
    {
    	{
    		task.execute(params);
    	}
    }  
	
	
}
