/*
 *
 * Copyright (c) 2010-2014 EVE GROUP PTE. LTD.
 *
 */


package veg.rtspplr.sdk.rtsptestplr;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
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

public class MainActivity extends Activity implements OnClickListener, MediaPlayer.MediaPlayerCallback
{
	public  static AutoCompleteTextView	edtIpAddress;
	public  static ArrayAdapter<String> edtIpAddressAdapter;
	public  static Set<String>			edtIpAddressHistory;
	private Button						btnConnect;
	private Button						btnHistory;

	private SharedPreferences 			settings;
    private SharedPreferences.Editor 	editor;

    private boolean playing = false;
    private MediaPlayer player = null;
    private MainActivity mthis = null;
 
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
		
		player = new MediaPlayer(this);
		float w = pxFromDp(125f);
		float h = pxFromDp(125f);
		FrameLayout.LayoutParams params = new FrameLayout.LayoutParams((int)w,(int)h, Gravity.CENTER);
		player.setLayoutParams(params);
	    
		FrameLayout lp = (FrameLayout)findViewById(R.id.playerView);
		lp.addView(player);
			
		Log.e("Test", "RTSPPLayer instance " + player);
		
		settings = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);

		strUrl = settings.getString("connectionUrl", "rtsp://184.72.239.149/vod/mp4:BigBuckBunny_115k.mov");
				
		loadPrefSettings();
		savePrefSettings();

        WebView mWebView = (WebView) findViewById(R.id.webView1);
		
		
        mWebView.setWebViewClient(new MyWebViewClient());
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setBuiltInZoomControls(true);
        mWebView.loadUrl("http://www.google.com"); 

		WebView mWebView2 = (WebView) findViewById(R.id.webView2);
        mWebView2.setWebViewClient(new MyWebViewClient());
        mWebView2.getSettings().setJavaScriptEnabled(true);
        mWebView2.getSettings().setBuiltInZoomControls(true);
        mWebView2.loadUrl("http://www.videoexpertsgroup.com/wp-content/uploads/2014/12/logo-header-12-300x142.png");
		mWebView2.setBackgroundColor(Color.TRANSPARENT);
        
        
		HashSet<String> tempHistory = new HashSet<String>();
		tempHistory.add("rtsp://184.72.239.149/vod/mp4:BigBuckBunny_115k.mov");
		
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
        
        RelativeLayout layout = (RelativeLayout) findViewById(R.id.main_view);
        layout.setOnTouchListener(new OnTouchListener() 
		{
			public boolean onTouch(View v, MotionEvent event) 
			{
				InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				inputManager.hideSoftInputFromWindow(getWindow().getCurrentFocus().getWindowToken(), 0);
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
		
		loadPrefSettings();
		
		Log.e("Test", "Instance of player is " + player);
		
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
    	    	MediaPlayerConfig conf = new MediaPlayerConfig();
    	    	player.setVisibility(View.INVISIBLE);
    	    	conf.setConnectionUrl(ConnectionUrl);
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
	
	private void savePrefSettings()
	{
		//	save preferences settings
		editor = settings.edit();

		editor.commit();
	}

	private void loadPrefSettings()
	{
	}

	@Override
	public int OnReceiveData(ByteBuffer buffer, int size, long pts) {
		// TODO Auto-generated method stub
		return 0;
	}
}
