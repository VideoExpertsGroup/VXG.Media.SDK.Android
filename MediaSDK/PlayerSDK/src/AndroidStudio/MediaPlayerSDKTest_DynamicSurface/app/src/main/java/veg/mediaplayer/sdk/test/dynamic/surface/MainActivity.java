/*
 *
  * Copyright (c) 2011-2017 VXG Inc.
 *
 */

package veg.mediaplayer.sdk.test.dynamic.surface;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;
import android.view.inputmethod.InputMethodManager;
import android.view.WindowManager;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.view.*;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView.OnEditorActionListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.ArrayList;

import android.preference.PreferenceManager;
import veg.mediaplayer.sdk.MediaPlayer;
import veg.mediaplayer.sdk.MediaPlayer.PlayerNotifyCodes;
import veg.mediaplayer.sdk.MediaPlayerConfig;

class MyWebViewClient extends WebViewClient 
{
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) 
    {
        view.loadUrl(url);
        return true;
    }
}  

public class MainActivity extends Activity implements OnClickListener, MediaPlayer.MediaPlayerCallback, SurfaceHolder.Callback2
{
	public  static AutoCompleteTextView	edtIpAddress;
	public  static ArrayAdapter<String> edtIpAddressAdapter;
	public  static Set<String>			edtIpAddressHistory;
	private Button						btnConnect;
	private Button						btnHistory;

	private Button						btnAddView;
	private Button						btnRemoveView;
	private SurfaceView 				externalView = null;
	private FrameLayout					playerView = null;
	
	private SharedPreferences 			settings;
    private SharedPreferences.Editor 	editor;

    private boolean playing = false;
    private MediaPlayer player = null;
    private MainActivity mthis = null;
	private Surface	surface = null;
 
    private Handler handler = new Handler() 
    {
	        @Override
	        public void handleMessage(Message msg) 
	        {
	        	PlayerNotifyCodes status = (PlayerNotifyCodes) msg.obj;
				if (status == PlayerNotifyCodes.CP_CONNECT_STARTING)
				{
					player.setVisibility(View.VISIBLE);
				}	
				if (status == PlayerNotifyCodes.PLP_CLOSE_STARTING)
				{
					player.setVisibility(View.INVISIBLE);
				}	
	        }
	};
	
	// callback from Native Player 
	public int Status(int arg)
	{
    	Log.e("SDL", "Form Native Player status: " + arg);
		Message msg = new Message();
		msg.obj = PlayerNotifyCodes.forValue(arg);

		if (handler != null)
			handler.sendMessage(msg);
		
    	return 0;
    }

	protected float pxFromDp(float dp)
	{
		return (dp * getResources().getDisplayMetrics().density);
	}


    @Override
    public void onCreate(Bundle savedInstanceState) 
	{
		String  strUrl;
		setTitle(R.string.app_name);
		super.onCreate(savedInstanceState);

		setContentView(R.layout.main);
		mthis = this;
		
		playerView = (FrameLayout)findViewById(R.id.playerView);
		player = new MediaPlayer(this, false);
		//SurfaceView externalView = (SurfaceView)findViewById(R.id.surfaceView);
		//externalView.getHolder().addCallback(this);
			
		Log.e("Test", "RTSPPLayer instance " + player);
		
		settings = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);

		strUrl = settings.getString("connectionUrl", "rtsp://3.84.6.190/vod/mp4:BigBuckBunny_115k.mov");

		HashSet<String> tempHistory = new HashSet<String>();
		tempHistory.add("rtsp://3.84.6.190/vod/mp4:BigBuckBunny_115k.mov");
		tempHistory.add("http://devimages.apple.com/iphone/samples/bipbop/gear1/prog_index.m3u8");

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
						InputMethodManager in = (InputMethodManager) getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
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
				InputMethodManager in = (InputMethodManager) getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
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
        
        RelativeLayout layout = (RelativeLayout) findViewById(R.id.main_view);
        layout.setOnTouchListener(new OnTouchListener() 
		{
			public boolean onTouch(View v, MotionEvent event) 
			{
				InputMethodManager inputManager = (InputMethodManager) getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
				inputManager.hideSoftInputFromWindow(getWindow().getCurrentFocus().getWindowToken(), 0);
				return true;
			}
		});
        
		btnAddView = (Button)findViewById(R.id.button_add_view);
		btnAddView.setOnClickListener(new View.OnClickListener() 
		{
			public void onClick(View v) 
			{
				if (externalView != null)
					return;
				
				externalView = new SurfaceView(mthis);
				externalView.getHolder().addCallback(mthis);
				playerView.addView(externalView);
			}   
		});
        
		btnRemoveView = (Button)findViewById(R.id.button_remove_view);
		btnRemoveView.setOnClickListener(new View.OnClickListener() 
		{
			public void onClick(View v) 
			{
				if (externalView == null)
					return;
				
				playerView.removeView(externalView);
				externalView = null;
			}   
		});
    }

    public void onClick(View v) 
	{
		Log.e("Test", "onClick");
		String ConnectionUrl  = edtIpAddress.getText().toString();
		if (ConnectionUrl.isEmpty())
			return;

		if (!edtIpAddressHistory.contains(ConnectionUrl))
			edtIpAddressHistory.add(ConnectionUrl);
		
		
		Log.e(TAG, "Instance of player is " + player);
		
		if (player != null)
		{
			
			if (playing)
			{
				player.Close();
				btnConnect.setText("Connect");
				playing = false;
			}
			else
			{
    	    	player.setVisibility(View.INVISIBLE);
    	    	
    	    	MediaPlayerConfig conf = new MediaPlayerConfig();
    	    	conf.setDecodingType(1);
    	    	conf.setConnectionUrl(ConnectionUrl);
    	    	
    	    	if (surface != null)
    	    	{
    	    		Log.e(TAG, "setSurface " + surface);
    	    		player.setSurface(surface);
    	    	}

    	    	player.Open(conf, this);
    			
				btnConnect.setText("Disconnect");
				playing = true;
			}
		}
    }
 
	protected void onPause()
	{
		Log.e("SDL", "onPause()");
		super.onPause();

		editor = settings.edit();
		editor.putString("connectionUrl", edtIpAddress.getText().toString());
		editor.putStringSet("connectionHistory", edtIpAddressHistory);
		editor.commit();
		
		if (player != null)
			player.onPause();
	}

	@Override
  	protected void onResume() 
	{
		Log.e("SDL", "onResume()");
		super.onResume();
		if (player != null)
			player.onResume();
  	}

  	@Override
	protected void onStart() 
  	{
      	Log.e("SDL", "onStart()");
		super.onStart();
		if (player != null)
			player.onStart();
	}

  	@Override
	protected void onStop() 
  	{
  		Log.e("SDL", "onStop()");
		super.onStop();
		if (player != null)
			player.onStop();
	}

  	@Override
  	public void onWindowFocusChanged(boolean hasFocus) 
  	{
  		Log.e("SDL", "onWindowFocusChanged(): " + hasFocus);
  		super.onWindowFocusChanged(hasFocus);
		if (player != null)
			player.onWindowFocusChanged(hasFocus);
  	}

  	@Override
  	public void onLowMemory() 
  	{
  		Log.e("SDL", "onLowMemory()");
  		super.onLowMemory();
		if (player != null)
			player.onLowMemory();
  	}

  	@Override
  	protected void onDestroy() 
  	{
  		Log.e("SDL", "onDestroy()");
		if (player != null)
			player.onDestroy();

		super.onDestroy();
   	}	
	
	@Override
	public int OnReceiveData(ByteBuffer buffer, int size, long pts) 
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder)
	{
  		Log.e(TAG, "surfaceCreated");
  		surface = holder.getSurface();
    	if (surface != null && player != null)
    	{
    		Log.e(TAG, "setSurface " + surface);
    		player.setSurface(surface);
    	}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
  		Log.e(TAG, "surfaceChanged");
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) 
	{
  		Log.e(TAG, "surfaceDestroyed");
    	if (player != null)
    	{
    		player.setSurface(null);
    	}
	}

	@Override
	public void surfaceRedrawNeeded(SurfaceHolder holder) 
	{
		// TODO Auto-generated method stub
		
	}
	
	private static final String TAG = "MediaPlayerSDKTest.ExternalSurface";
}
