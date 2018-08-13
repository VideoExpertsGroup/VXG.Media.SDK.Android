/*
 *
 * Copyright (c) 2010-2017 VXG Inc.
 *
 */

package vxg.mediaplayer.sliding.test;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;
import android.view.inputmethod.InputMethodManager;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.view.*;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
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
import vxg.mediaplayer.sliding.test.SurfacePageFragment.SurfacePageFragmentCallback;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;

import veg.mediaplayer.sdk.MediaPlayer;
import veg.mediaplayer.sdk.MediaPlayer.PlayerNotifyCodes;
import veg.mediaplayer.sdk.MediaPlayer.PlayerState;
import veg.mediaplayer.sdk.MediaPlayerConfig;


public class MainActivity extends FragmentActivity implements SurfacePageFragmentCallback, MediaPlayer.MediaPlayerCallback
{
	public  static AutoCompleteTextView	edtIpAddress;
	public  static ArrayAdapter<String> edtIpAddressAdapter;
//	public  static Set<String>			edtIpAddressHistory;
	private Button						btnConnect;
	private Button						btnHistory;

	private SharedPreferences 			settings;
    private SharedPreferences.Editor 	editor;

    //private boolean playing = false;
    private MainActivity mthis = null;
	//private Surface	surface = null;
 
	//private boolean	isFullScreen = false;

    //private static final int NUM_PAGES = 5;
    private MediaPlayer[] players = new MediaPlayer[4];

	private ViewPager mPager = null;
    private PagerAdapter mPagerAdapter = null;
	private TextView	 textPlayerTitle = null;

	private int 		cur_position = 0;


    private Switch switchDecoderType = null;
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
				
		    	SurfacePageFragment page = (SurfacePageFragment)mPager.getAdapter().instantiateItem(mPager, mPager.getCurrentItem());
		        if (page == null) 
		        	return;
	
		        int position = page.getPageNumber();
				if (players[position].getState() == PlayerState.Closed)
					btnConnect.setText("Connect");
	        }
	};
	
	// callback from Native Player 
	@Override
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
		return 0;
	}
	
	@Override
	public void onSurfaceCreated(int position, Surface surface) 
	{
    	Log.d(TAG, "onSurfaceCreated: pos:" + position + ", surface: " + surface);
    	SurfacePageFragment page = (SurfacePageFragment)mPager.getAdapter().instantiateItem(mPager, mPager.getCurrentItem());
        if (page == null)
        	return;
        
    	players[position].setSurface(surface);
	}

	@Override
	public void onSurfaceChanged(int position, Surface surface, int newWidth, int newHeight) 
	{
    	Log.d(TAG, "onSurfaceChanged1: pos:" + position + ", surface: " + surface + ", newWidth: " + newWidth + ", newHeight: " + newHeight);
    	final SurfacePageFragment page = (SurfacePageFragment)mPager.getAdapter().instantiateItem(mPager, mPager.getCurrentItem());
        if (page == null)
        	return;

		/*		
    	final FrameLayout.LayoutParams params = (FrameLayout.LayoutParams)page.getView().getLayoutParams(); 
		int x = 0 , y = 0, w = 1080, h = 1920;
		
		params.leftMargin	= x;
		params.topMargin	=  newHeight - y - h;
		params.rightMargin	=  (w > newWidth) ? (- w - params.leftMargin + newWidth) : (newWidth - w - params.leftMargin );
		params.bottomMargin =  (h > newHeight) ? (- h - params.topMargin + newHeight) : (newHeight - h - params.topMargin);

		
		page.getView().post(new Runnable()
		  {
			  @Override
			  public void run() 
			  {
				  page.getView().requestLayout();
			  }
		  });
		 */ 
		  

    	players[position].setSurface(surface, newWidth, newHeight);

		players[position].UpdateView();
	}

	@Override
	public void onSurfaceDestroyed(int position, Surface surface) 
	{
    	Log.d(TAG, "onSurfaceDestroyed: pos:" + position + ", surface: " + surface);
    	SurfacePageFragment page = (SurfacePageFragment)mPager.getAdapter().instantiateItem(mPager, mPager.getCurrentItem());
        if (page == null)
        	return;
        
    	players[position].setSurface(null);
	}

	@Override
	public void onSurfaceTouched(int position) 
	{
    	Log.d(TAG, "onSurfaceTouched: pos:" + position);
    	SurfacePageFragment page = (SurfacePageFragment)mPager.getAdapter().instantiateItem(mPager, mPager.getCurrentItem());
        if (page == null)
        	return;
        /*
        Log.d(TAG, "onSurfaceTouched state = " + players[position].getState());
		if (players[position].getState() == PlayerState.Started)
		{
			players[position].Pause();
		}
		else
			if (players[position].getState() == PlayerState.Paused)
			{
				players[position].Play();
			}
		*/	
	}

	protected float pxFromDp(float dp)
	{
		return (dp * getResources().getDisplayMetrics().density);
	}



	protected int set_url()
	{
		String url_field =  "connectionUrl" + cur_position;
		editor.putString(url_field, edtIpAddress.getText().toString());
		editor.commit();
		Log.e(TAG, "set_url  url_field:" + url_field   +   "  url:" + edtIpAddress.getText().toString());
		return 0;
	}


	protected int get_url()
	{
		String url_field =  "connectionUrl" + cur_position;
		Log.e(TAG, "get_url  url_field:" + url_field   +   "  url:" + settings.getString(url_field,"rtsp://184.72.239.149/vod/mp4:BigBuckBunny_115k.mov"));
		edtIpAddress.setText(settings.getString(url_field,"rtsp://184.72.239.149/vod/mp4:BigBuckBunny_115k.mov"));
		return 0;
	}


	protected int set_accel_dec()
	{
		String hw_field =  "hwDecoder" + cur_position;
		editor.putInt(hw_field, switchDecoderType.isChecked() ? 1 : 0);
		editor.commit();
		Log.e(TAG, "set_accel_dec  :" + hw_field   +   "  H/W ON:" + ((switchDecoderType.isChecked() == true) ? 1 : 0));
		return 0;
	}

	protected int get_accel_dec()
	{
		String hw_field =  "hwDecoder" + cur_position;
		Log.e(TAG, "get_accel_dec  hw_field:" + hw_field   +   "  H/W ON:" + settings.getInt(hw_field, 1));
		switchDecoderType.setChecked((settings.getInt(hw_field, 1)==0)?false:true);
		return 0;
	}

	


    @SuppressLint("ClickableViewAccessibility")
	@Override
    public void onCreate(Bundle savedInstanceState) 
	{
		String  strUrl;

		//setTitle(R.string.app_name); 
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		cur_position = 0;
		super.onCreate(savedInstanceState);

		setContentView(R.layout.main);
		mthis = this;
		
		for (int i = 0; i < players.length; i++) {
			players[i] = new MediaPlayer(this, false);
		}

		Log.e(TAG, "MediaPlayer instances: " + players.length);
		
		settings = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);

 		if (editor == null)
 			editor = settings.edit();

		strUrl = settings.getString("connectionUrl", "rtsp://184.72.239.149/vod/mp4:BigBuckBunny_115k.mov");

		HashSet<String> tempHistory = new HashSet<String>();
		tempHistory.add("rtsp://184.72.239.149/vod/mp4:BigBuckBunny_115k.mov");
		tempHistory.add("rtmp://184.72.239.149/vod/mp4:bigbuckbunny_450.mp4");
		

		this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN); 
	
		edtIpAddress = (AutoCompleteTextView)findViewById(R.id.edit_ipaddress);
		//edtIpAddress.setText(strUrl);

		get_url();

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

		/*
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
		*/

        btnConnect 		= (Button)findViewById(R.id.button_connect);
		textPlayerTitle = (TextView) findViewById(R.id.channel_name);
		
        btnConnect.setOnClickListener(new View.OnClickListener() 
		{
			public void onClick(View v) 
			{
				String ConnectionUrl  = edtIpAddress.getText().toString();
				if (ConnectionUrl.isEmpty())
					return;
	
		    	SurfacePageFragment page = (SurfacePageFragment)mPager.getAdapter().instantiateItem(mPager, mPager.getCurrentItem());
		        if (page == null) 
		        	return;
	
		        int position = page.getPageNumber();
//		        if (!edtIpAddressHistory.contains(ConnectionUrl))
//					edtIpAddressHistory.add(ConnectionUrl);

	            Log.d(TAG, "btnConnect state = " + players[position].getState());
				if (players[position].getState() != PlayerState.Closed)
				{
			        players[position].Close();
//			    	if (page.getSurface() != null)
//			    	{
//			    		Log.e(TAG, "setSurface " + page.getSurface());
//			    		players[position].setSurface(page.getSurface());
//			    	}
					btnConnect.setText("Connect");
				}
				else
				{
			        MediaPlayerConfig conf = new MediaPlayerConfig();
			    	conf.setDecodingType(switchDecoderType.isChecked() ? 1 : 0);
			    	conf.setConnectionUrl(ConnectionUrl);
					conf.setConnectionBufferingTime(500);
					conf.setConnectionDetectionTime(3000);
					
					conf.setSynchroNeedDropVideoFrames(0);
					conf.setDataReceiveTimeout(60000);
					
					conf.setAspectRatioMode(1); //1 - fittoscreen with aspect ratio
			    	if (ConnectionUrl.contains("udp://"))
			    	{
			    		conf.setConnectionNetworkProtocol(0);
			    	}
			    	conf.setConnectionUrl(ConnectionUrl);
			    	
			    	if (page.getSurface() != null)
			    	{
			    		Log.e(TAG, "setSurface " + page.getSurface());
			    		players[position].setSurface(page.getSurface());
			    	}
			    	
			    	players[position].Open(conf, mthis);
					set_url();
					set_accel_dec();
			    	btnConnect.setText("Disconnect");
				}
			}   
		});

        switchDecoderType = (Switch) findViewById(R.id.switch_decoder_type);
		switchDecoderType.setOnCheckedChangeListener(new OnCheckedChangeListener() {
		   @Override
		   public void onCheckedChanged(CompoundButton buttonView,
			 boolean isChecked) {
			set_accel_dec();		
		   }
		  });


		
		get_accel_dec();
		
        // Instantiate a ViewPager and a PagerAdapter.
        mPager = (ViewPager) findViewById(R.id.surfacePager);
        mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(mPagerAdapter);

        ViewTreeObserver observerEmpty = mPager.getViewTreeObserver();
        observerEmpty.addOnGlobalLayoutListener(layoutEmptyListener); 

		
        mPager.setOnPageChangeListener(new OnPageChangeListener() 
        {
            @Override
            public void onPageSelected(int position) 
            {
                Log.d(TAG, "onPageSelected, position = " + position);

				cur_position = position;
				get_url();
				get_accel_dec();
				

				switch(position) 
					{
						case 0:
							textPlayerTitle.setText("   1 (4) ->");
							break;
						case 1:
							textPlayerTitle.setText("<- 2 (4) ->");
							break;
						case 2:
							textPlayerTitle.setText("<- 3 (4) ->");
							break;
						case 3:
							textPlayerTitle.setText("<- 4 (4)   ");
							break;
						default:
							break;
				}

				
				if (players[position].getState() == PlayerState.Closed)
				{
					btnConnect.setText("Connect");
				}
				else
				{
			    	btnConnect.setText("Disconnect");
				}
				
				for (int i = 0; i < players.length; i++) 
				{
					players[i].toggleMute(!(position == i));
				}
				
				//switchDecoderType.setChecked(players[position].getConfig().getDecodingType() == 1);
				
//			   	SurfacePageFragment page = (SurfacePageFragment)mPager.getAdapter().instantiateItem(mPager, mPager.getCurrentItem());
//		        if (page == null)
//		        	return;
//		        
//	    		players[0].setSurface(page.getSurface());
           }

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) 
            {
            }

            @Override
            public void onPageScrollStateChanged(int state) 
            {
            }
        });
        
	}

    @Override
    public void onConfigurationChanged(Configuration newConfigure)
    {
    	super.onConfigurationChanged(newConfigure);
    	
    	SurfacePageFragment page = (SurfacePageFragment)mPager.getAdapter().instantiateItem(mPager, mPager.getCurrentItem());
        if (page == null) 
        	return;

        int position = page.getPageNumber();
		players[position].UpdateView();
    }
    
    @Override
    public void onBackPressed() 
    {
        if (mPager.getCurrentItem() == 0) 
        {
            super.onBackPressed();
        } 
        else 
        {
            mPager.setCurrentItem(mPager.getCurrentItem() - 1);
        }
    }

    protected void onPause()
	{
		Log.e(TAG, "onPause()");
		super.onPause();
		set_url();
		set_accel_dec();

		//editor.putString("connectionUrl", edtIpAddress.getText().toString());
		//editor.putStringSet("connectionHistory", edtIpAddressHistory);
		//editor.commit();
	}

	@Override
  	protected void onResume() 
	{
		Log.e(TAG, "onResume()");
		super.onResume();
  	}

  	@Override
	protected void onStart() 
  	{
      	Log.e(TAG, "onStart()");
		super.onStart();
	}

  	@Override
	protected void onStop() 
  	{
  		Log.e(TAG, "onStop()");
		super.onStop();
	}

  	@Override
  	public void onWindowFocusChanged(boolean hasFocus) 
  	{
  		Log.e(TAG, "onWindowFocusChanged(): " + hasFocus);
  		super.onWindowFocusChanged(hasFocus);
  	}

  	@Override
  	public void onLowMemory() 
  	{
  		Log.e(TAG, "onLowMemory()");
  		super.onLowMemory();
  	}

  	@Override
  	protected void onDestroy() 
  	{
  		Log.e(TAG, "onDestroy()");
		for (int i = 0; i < players.length; i++) {
			players[i].Close();
		}

		super.onDestroy();
   	}	
	
    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter 
    {
        public ScreenSlidePagerAdapter(FragmentManager fm) 
        {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) 
        {
            return SurfacePageFragment.create(position, mthis);
        }

        @Override
        public int getCount() 
        {
            return players.length;
        }
    }


	
	private static final String TAG = "MediaPlayerSDKTest.SlidingViews";


	
    ViewTreeObserver.OnGlobalLayoutListener layoutEmptyListener = new ViewTreeObserver.OnGlobalLayoutListener() 
    {
        @Override
        public void onGlobalLayout() 
        {
	        Log.e(TAG, "onGlobalLayout 2");

			final SurfacePageFragment page = (SurfacePageFragment)mPager.getAdapter().instantiateItem(mPager, mPager.getCurrentItem());
			if (page == null)
				return;

			//page.ChangeSize();

			/*
	    	final FrameLayout.LayoutParams params = (FrameLayout.LayoutParams)page.getView().getLayoutParams(); 
			int x = 0 , y = 0, w = 1080, h = 1920;
			int newHeight = 1088;
			int newWidth = 1600;
		
			params.leftMargin	= x;
			params.topMargin	=  newHeight - y - h;
			params.rightMargin	=  (w > newWidth) ? (- w - params.leftMargin + newWidth) : (newWidth - w - params.leftMargin );
			params.bottomMargin =  (h > newHeight) ? (- h - params.topMargin + newHeight) : (newHeight - h - params.topMargin);

		
		  page.getView().post(new Runnable()
		  {
			  @Override
			  public void run() 
			  {
				  page.getView().requestLayout();
			  }
		  });
		  */


			
        }
    };

}
