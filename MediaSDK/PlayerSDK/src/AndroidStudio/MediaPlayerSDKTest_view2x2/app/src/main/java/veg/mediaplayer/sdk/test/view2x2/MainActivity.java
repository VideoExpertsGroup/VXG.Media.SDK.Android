/*
 *
  * Copyright (c) 2011-2017 VXG Inc.
 *
 */

package veg.mediaplayer.sdk.test.view2x2;

import android.annotation.SuppressLint;
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
import android.widget.LinearLayout;
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

	private SharedPreferences 			settings;
    private SharedPreferences.Editor 	editor;

    private boolean playing = false;
    private MediaPlayer player1 = null;
    private MediaPlayer player2 = null;
    private MediaPlayer player3 = null;
    private MediaPlayer player4 = null;
    private MainActivity mthis = null;
	private Surface	surface = null;
 
	private boolean	isFullScreen = false;
	private LinearLayout playerRow0 = null;
	private LinearLayout playerRow1 = null;

	private Handler handler = new Handler() 
    {
	        @Override
	        public void handleMessage(Message msg) 
	        {
	        	PlayerNotifyCodes status = (PlayerNotifyCodes) msg.obj;
				if (status == PlayerNotifyCodes.CP_CONNECT_STARTING)
				{
					//player1.setVisibility(View.VISIBLE);
				}	
				if (status == PlayerNotifyCodes.PLP_CLOSE_STARTING)
				{
					//player1.setVisibility(View.INVISIBLE);
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


    @SuppressLint("ClickableViewAccessibility")
	@Override
    public void onCreate(Bundle savedInstanceState) 
	{
		String  strUrl;

		setTitle(R.string.app_name);
		super.onCreate(savedInstanceState);

		setContentView(R.layout.main);
		mthis = this;
		
//		player = new MediaPlayer(this, false);
//		SurfaceView externalView = (SurfaceView)findViewById(R.id.surfaceView);
//		externalView.getHolder().addCallback(this);

		playerRow0 = (LinearLayout)findViewById(R.id.playerRow0);
		playerRow1 = (LinearLayout)findViewById(R.id.playerRow1);
		
		player1 = (MediaPlayer)findViewById(R.id.playerView1);
		player2 = (MediaPlayer)findViewById(R.id.playerView2);
		player3 = (MediaPlayer)findViewById(R.id.playerView3);
		player4 = (MediaPlayer)findViewById(R.id.playerView4);


		player1.getSurfaceView().setZOrderOnTop(false);
		player2.getSurfaceView().setZOrderOnTop(false);
		player3.getSurfaceView().setZOrderOnTop(false);
		player4.getSurfaceView().setZOrderOnTop(false);



		Log.e("Test", "RTSPPLayer instance " + player1);
		
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
        
        player1.setOnTouchListener(new OnTouchListener() 
		{
			public boolean onTouch(View v, MotionEvent event) 
			{
				switch(event.getAction())
				{
					case MotionEvent.ACTION_DOWN:
					{
						if (isFullScreen)
						{
							LinearLayout.LayoutParams parent = (LinearLayout.LayoutParams) playerRow0.getLayoutParams();
							parent.height = 0;
							parent.weight = (float) 0.5;
							parent.width = LinearLayout.LayoutParams.MATCH_PARENT;
							playerRow0.setLayoutParams(parent);
							
							LinearLayout.LayoutParams lpc = (LinearLayout.LayoutParams) player1.getLayoutParams();
							lpc.width = 0;
							lpc.weight = (float) 0.5;
							lpc.height = LinearLayout.LayoutParams.MATCH_PARENT;
							player1.setLayoutParams(lpc);
							
							player1.getSurfaceView().setZOrderOnTop(false);
							player2.getSurfaceView().setZOrderOnTop(false);
							player3.getSurfaceView().setZOrderOnTop(false);
							player4.getSurfaceView().setZOrderOnTop(false);
							
							isFullScreen = false;
						}
						else
						{
							LinearLayout.LayoutParams parent = (LinearLayout.LayoutParams) playerRow0.getLayoutParams();
							parent.height = LinearLayout.LayoutParams.MATCH_PARENT;
							parent.width = LinearLayout.LayoutParams.MATCH_PARENT;
							playerRow0.setLayoutParams(parent);
							
							LinearLayout.LayoutParams lpc = (LinearLayout.LayoutParams) player1.getLayoutParams();
							
							lpc.width = LinearLayout.LayoutParams.MATCH_PARENT;
							lpc.height = LinearLayout.LayoutParams.MATCH_PARENT;
							player1.setLayoutParams(lpc);
							
							player1.getSurfaceView().setZOrderOnTop(false);
							player2.getSurfaceView().setZOrderOnTop(true);
							player3.getSurfaceView().setZOrderOnTop(true);
							player4.getSurfaceView().setZOrderOnTop(true);
							
							isFullScreen = true;
						}
					}
				}
				
				return true;
			}
		});
        
        player2.setOnTouchListener(new OnTouchListener() 
		{
			public boolean onTouch(View v, MotionEvent event) 
			{
				switch(event.getAction())
				{
					case MotionEvent.ACTION_DOWN:
					{
						if (isFullScreen)
						{
							LinearLayout.LayoutParams parent = (LinearLayout.LayoutParams) playerRow0.getLayoutParams();
							parent.height = 0;
							parent.weight = (float) 0.5;
							parent.width = LinearLayout.LayoutParams.MATCH_PARENT;
							playerRow0.setLayoutParams(parent);
							
							LinearLayout.LayoutParams lpc = (LinearLayout.LayoutParams) player2.getLayoutParams();
							
							lpc.width = 0;
							lpc.weight = (float) 0.5;
							lpc.height = LinearLayout.LayoutParams.MATCH_PARENT;
							player2.setLayoutParams(lpc);
							
							player1.getSurfaceView().setZOrderOnTop(false);
							player2.getSurfaceView().setZOrderOnTop(false);
							player3.getSurfaceView().setZOrderOnTop(false);
							player4.getSurfaceView().setZOrderOnTop(false);
							isFullScreen = false;
						}
						else
						{
							LinearLayout.LayoutParams parent = (LinearLayout.LayoutParams) playerRow0.getLayoutParams();
							parent.height = LinearLayout.LayoutParams.MATCH_PARENT;
							parent.width = LinearLayout.LayoutParams.MATCH_PARENT;
							playerRow0.setLayoutParams(parent);
							
							LinearLayout.LayoutParams lpc = (LinearLayout.LayoutParams) player2.getLayoutParams();
							
							lpc.width = LinearLayout.LayoutParams.MATCH_PARENT;
							lpc.height = LinearLayout.LayoutParams.MATCH_PARENT;
							player2.setLayoutParams(lpc);
							
							player1.getSurfaceView().setZOrderOnTop(true);
							player2.getSurfaceView().setZOrderOnTop(false);
							player3.getSurfaceView().setZOrderOnTop(true);
							player4.getSurfaceView().setZOrderOnTop(true);
							isFullScreen = true;
						}
					}
				}
				
				return true;
			}
		});

        player3.setOnTouchListener(new OnTouchListener() 
		{
			public boolean onTouch(View v, MotionEvent event) 
			{
				switch(event.getAction())
				{
					case MotionEvent.ACTION_DOWN:
					{
						if (isFullScreen)
						{
							LinearLayout.LayoutParams parent = (LinearLayout.LayoutParams) playerRow0.getLayoutParams();
							parent.height = 0;
							parent.weight = (float) 0.5;
							parent.width = LinearLayout.LayoutParams.MATCH_PARENT;
							playerRow0.setLayoutParams(parent);
							
							parent = (LinearLayout.LayoutParams) playerRow1.getLayoutParams();
							parent.height = 0;
							parent.weight = (float) 0.5;
							parent.width = LinearLayout.LayoutParams.MATCH_PARENT;
							playerRow1.setLayoutParams(parent);
							
							LinearLayout.LayoutParams lpc = (LinearLayout.LayoutParams) player3.getLayoutParams();
							
							lpc.width = 0;
							lpc.weight = (float) 0.5;
							lpc.height = LinearLayout.LayoutParams.MATCH_PARENT;
							player3.setLayoutParams(lpc);
							
							player1.getSurfaceView().setZOrderOnTop(false);
							player2.getSurfaceView().setZOrderOnTop(false);
							player3.getSurfaceView().setZOrderOnTop(false);
							player4.getSurfaceView().setZOrderOnTop(false);
							
							isFullScreen = false;
						}
						else
						{
							LinearLayout.LayoutParams parent = (LinearLayout.LayoutParams) playerRow0.getLayoutParams();
							parent.height = 0;
							parent.width = 0;
							parent.weight = (float) 0.0;
							playerRow0.setLayoutParams(parent);
							
							parent = (LinearLayout.LayoutParams) playerRow1.getLayoutParams();
							parent.height = LinearLayout.LayoutParams.MATCH_PARENT;
							parent.width = LinearLayout.LayoutParams.MATCH_PARENT;
							parent.weight = (float) 1.0;
							playerRow1.setLayoutParams(parent);
							
							LinearLayout.LayoutParams lpc = (LinearLayout.LayoutParams) player3.getLayoutParams();
							
							lpc.width = LinearLayout.LayoutParams.MATCH_PARENT;
							lpc.height = LinearLayout.LayoutParams.MATCH_PARENT;
							player3.setLayoutParams(lpc);
							
							player1.getSurfaceView().setZOrderOnTop(true);
							player2.getSurfaceView().setZOrderOnTop(true);
							player3.getSurfaceView().setZOrderOnTop(false);
							player4.getSurfaceView().setZOrderOnTop(true);
							
							isFullScreen = true;
						}
					}
				}
				
				return true;
			}
		});

        player4.setOnTouchListener(new OnTouchListener() 
		{
			public boolean onTouch(View v, MotionEvent event) 
			{
				switch(event.getAction())
				{
					case MotionEvent.ACTION_DOWN:
					{
						if (isFullScreen)
						{
							LinearLayout.LayoutParams parent = (LinearLayout.LayoutParams) playerRow0.getLayoutParams();
							parent.height = 0;
							parent.weight = (float) 0.5;
							parent.width = LinearLayout.LayoutParams.MATCH_PARENT;
							playerRow0.setLayoutParams(parent);
							
							parent = (LinearLayout.LayoutParams) playerRow1.getLayoutParams();
							parent.height = 0;
							parent.weight = (float) 0.5;
							parent.width = LinearLayout.LayoutParams.MATCH_PARENT;
							playerRow1.setLayoutParams(parent);
							
							LinearLayout.LayoutParams lpc = (LinearLayout.LayoutParams) player4.getLayoutParams();
							
							lpc.width = 0;
							lpc.weight = (float) 0.5;
							lpc.height = LinearLayout.LayoutParams.MATCH_PARENT;
							player4.setLayoutParams(lpc);
							
							player1.getSurfaceView().setZOrderOnTop(false);
							player2.getSurfaceView().setZOrderOnTop(false);
							player3.getSurfaceView().setZOrderOnTop(false);
							player4.getSurfaceView().setZOrderOnTop(false);
							
							isFullScreen = false;
						}
						else
						{
							LinearLayout.LayoutParams parent = (LinearLayout.LayoutParams) playerRow0.getLayoutParams();
							parent.height = 0;
							parent.width = 0;
							parent.weight = (float) 0.0;
							playerRow0.setLayoutParams(parent);
							
							parent = (LinearLayout.LayoutParams) playerRow1.getLayoutParams();
							parent.height = LinearLayout.LayoutParams.MATCH_PARENT;
							parent.width = LinearLayout.LayoutParams.MATCH_PARENT;
							playerRow1.setLayoutParams(parent);
							
							LinearLayout.LayoutParams lpc = (LinearLayout.LayoutParams) player4.getLayoutParams();
							lpc.width = LinearLayout.LayoutParams.MATCH_PARENT;
							lpc.height = LinearLayout.LayoutParams.MATCH_PARENT;
							player4.setLayoutParams(lpc);
							
							player1.getSurfaceView().setZOrderOnTop(true);
							player2.getSurfaceView().setZOrderOnTop(true);
							player3.getSurfaceView().setZOrderOnTop(true);
							player4.getSurfaceView().setZOrderOnTop(false);
							
							isFullScreen = true;
						}
					}
				}
				
				return true;
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
		
		
		Log.e(TAG, "Instance of player is " + player1);
		
		if (player1 != null)
		{
			
			if (playing)
			{
				player1.Close();
				player2.Close();
				player3.Close();
				player4.Close();
				btnConnect.setText("Connect");
				playing = false;
			}
			else
			{

    	    	
    	    	MediaPlayerConfig conf = new MediaPlayerConfig();
    	    	conf.setDecodingType(1);
    	    	conf.setConnectionUrl(ConnectionUrl);
    	    	

    	    	player1.backgroundColor(Color.RED);
    	    	player1.Open(conf, this);
    	    	player2.backgroundColor(Color.GREEN);
    	    	player2.Open(conf, this);
    	    	player3.backgroundColor(Color.BLUE);
    	    	player3.Open(conf, this);
    	    	player4.backgroundColor(Color.YELLOW);
    	    	player4.Open(conf, this);
    			
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
		
		if (player1 != null)
			player1.onPause();
		if (player2 != null)
			player2.onPause();
		if (player3 != null)
			player3.onPause();
		if (player4 != null)
			player4.onPause();
	}

	@Override
  	protected void onResume() 
	{
		Log.e("SDL", "onResume()");
		super.onResume();
		if (player1 != null)
			player1.onResume();
		if (player2 != null)
			player2.onResume();
		if (player3 != null)
			player3.onResume();
		if (player4 != null)
			player4.onResume();
  	}

  	@Override
	protected void onStart() 
  	{
      	Log.e("SDL", "onStart()");
		super.onStart();
		if (player1 != null)
			player1.onStart();
		if (player2 != null)
			player2.onStart();
		if (player3 != null)
			player3.onStart();
		if (player4 != null)
			player4.onStart();
	}

  	@Override
	protected void onStop() 
  	{
  		Log.e("SDL", "onStop()");
		super.onStop();
		if (player1 != null)
			player1.onStop();
		if (player2 != null)
			player2.onStop();
		if (player3 != null)
			player3.onStop();
		if (player4 != null)
			player4.onStop();
	}

  	@Override
  	public void onWindowFocusChanged(boolean hasFocus) 
  	{
  		Log.e("SDL", "onWindowFocusChanged(): " + hasFocus);
  		super.onWindowFocusChanged(hasFocus);
		if (player1 != null)
			player1.onWindowFocusChanged(hasFocus);
		if (player2 != null)
			player2.onWindowFocusChanged(hasFocus);
		if (player3 != null)
			player3.onWindowFocusChanged(hasFocus);
		if (player4 != null)
			player4.onWindowFocusChanged(hasFocus);
  	}

  	@Override
  	public void onLowMemory() 
  	{
  		Log.e("SDL", "onLowMemory()");
  		super.onLowMemory();
		if (player1 != null)
			player1.onLowMemory();
		if (player2 != null)
			player2.onLowMemory();
		if (player3 != null)
			player3.onLowMemory();
		if (player4 != null)
			player4.onLowMemory();
  	}

  	@Override
  	protected void onDestroy() 
  	{
  		Log.e("SDL", "onDestroy()");
		if (player1 != null)
			player1.onDestroy();
		if (player2 != null)
			player2.onDestroy();
		if (player3 != null)
			player3.onDestroy();
		if (player4 != null)
			player4.onDestroy();

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
	}

	@Override
	public void surfaceRedrawNeeded(SurfaceHolder holder) 
	{
		// TODO Auto-generated method stub
		
	}
	
	
	private static final String TAG = "MediaPlayerSDKTest.2x2";
}
