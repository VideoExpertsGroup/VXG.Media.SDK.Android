/*
 *
  * Copyright (c) 2011-2017 VXG Inc.
 *
 */

package veg.mediaplayer.sdk.test.external.transcoding;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import androidx.core.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;
import android.view.inputmethod.InputMethodManager;
import android.view.WindowManager;
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

import veg.mediacapture.sdk.MediaCapture;
import veg.mediacapture.sdk.MediaCaptureCallback;
import veg.mediacapture.sdk.MediaCaptureConfig;
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

public class MainActivity extends Activity implements OnClickListener, MediaPlayer.MediaPlayerCallback/*, SurfaceHolder.Callback2*/
{
	public  static AutoCompleteTextView	edtIpAddress;
	public  static AutoCompleteTextView	edtIpAddress2;
	public  static ArrayAdapter<String> edtIpAddressAdapter;
	public  static Set<String>			edtIpAddressHistory;
	private Button						btnConnect;
	private Button						btnHistory;

	private SharedPreferences 			settings;
    private SharedPreferences.Editor 	editor;

    private boolean playing = false;
    private MediaPlayer player = null;


	private MediaPlayer player_view = null;
	private boolean playing_view = false;
	private Button	btnConnect_view;



    private MainActivity mthis = null;
	private Surface	surface = null;

	private MediaCapture capture	= null;
	boolean misSurfaceReady			= false;
	private String  streamInfo 		= null;
	final boolean USE_RTSP = true;
	private String mUrlRTMP			= "";
	boolean is_audio_configured 	= false;

 
    private Handler handler = new Handler() 
    {
	        @Override
	        public void handleMessage(Message msg) 
	        {
	        	PlayerNotifyCodes status = (PlayerNotifyCodes) msg.obj;
				Log.e(TAG, "handleMessage Form Native Player status: " + status);
				if (status == PlayerNotifyCodes.CP_CONNECT_STARTING)
				{
					player.setVisibility(View.VISIBLE);
				}	
				if (status == PlayerNotifyCodes.PLP_CLOSE_STARTING)
				{
					player.setVisibility(View.INVISIBLE);
				}
				if (status == PlayerNotifyCodes.PLP_PLAY_SUCCESSFUL)
				{
					streamInfo = player.getStreamInfo();
					if (misSurfaceReady == true) {
						Surface surface = capture.getSurface();
						player.setSurface(surface);
					}

					VXGEncoderCheckStart();
					//VXGDecoderOpen();
				}


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

	protected float pxFromDp(float dp)
	{
		return (dp * getResources().getDisplayMetrics().density);
	}


	// Storage Permissions
	private static final int REQUEST_EXTERNAL_STORAGE = 1;
	private static String[] PERMISSIONS_STORAGE = {
			Manifest.permission.READ_EXTERNAL_STORAGE
	};

	/**
	 * Checks if the app has permission to write to device storage
	 *
	 * If the app does not has permission then the user will be prompted to grant permissions
	 *
	 * @param activity
	 */
	private void verifyStoragePermissions(Activity activity) {
		// Check if we have write permission

		int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

		if (permission != PackageManager.PERMISSION_GRANTED) {
			// We don't have permission so prompt the user

			ActivityCompat.requestPermissions(
					this,
					PERMISSIONS_STORAGE,this.
							REQUEST_EXTERNAL_STORAGE
			);
		}
	}

	//@Override
	public void onRequestPermissionsResult(int requestCode,
										   String permissions[], int[] grantResults) {

		Log.e(TAG, "=onRequestPermissionsResult requestCode="+requestCode+" permissions="+permissions+" grantResults="+grantResults);
		switch (requestCode)
		{
			case REQUEST_EXTERNAL_STORAGE:
			{
				// If request is cancelled, the result arrays are empty.
				if (grantResults.length > 0
						&& grantResults[0] == PackageManager.PERMISSION_GRANTED)
				{

					// permission was granted, yay! Do the
					// contacts-related task you need to do.
					//loadHistory(true);
				}
				else
				{

					// permission denied, boo! Disable the
					// functionality that depends on this permission.
				}
				return;
			}

			// other 'case' lines to check for other
			// permissions this app might request
		}
	}



	@Override
    public void onCreate(Bundle savedInstanceState) 
	{
		MediaCapture.DISABLE_CAMERA = true;
		String  strUrl;

		setTitle(R.string.app_name);
		super.onCreate(savedInstanceState);

		verifyStoragePermissions(this);

		setContentView(R.layout.main);
		mthis = this;
		
		player = new MediaPlayer(this, false);
		player_view = (MediaPlayer)findViewById(R.id.playerView2);
		//SurfaceView externalView = (SurfaceView)findViewById(R.id.surfaceView);
		//externalView.getHolder().addCallback(this);
			
		Log.e("Test", "RTSPPLayer instance " + player);
		
		settings = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);

		strUrl = settings.getString("connectionUrl", "rtsp://rtsp.stream/movie");

		HashSet<String> tempHistory = new HashSet<String>();
		tempHistory.add("rtsp://rtsp.stream/movie");
		tempHistory.add("rtsp://rtsp.stream/pattern");
		tempHistory.add("http://devimages.apple.com/iphone/samples/bipbop/gear1/prog_index.m3u8");

		edtIpAddressHistory = settings.getStringSet("connectionHistory", tempHistory);

		this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN); 
	
		edtIpAddress = (AutoCompleteTextView)findViewById(R.id.edit_ipaddress);
		edtIpAddress.setText(strUrl);

		edtIpAddress2 = (AutoCompleteTextView)findViewById(R.id.edit_ipaddress2);
		edtIpAddress2.setText("");

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

		btnConnect_view = (Button)findViewById(R.id.button_connect_view);
		btnConnect_view.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				if (playing_view == false)
					VXGDecoderOpen();
				else
					VXGDecoderClose();
			}
		});


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

		VXGEncoderOpen();

    }



	private void VXGEncoderOpen()
	{
		capture = (MediaCapture) findViewById(R.id.captureView);
		capture.getConfig().setCaptureSource(MediaCaptureConfig.CaptureSources.PP_MODE_SURFACE.val() | MediaCaptureConfig.CaptureSources.PP_MODE_OFFSCREEN_SURFACE.val());
		capture.getConfig().setStreaming(false);
		capture.getConfig().setRecording(false);
		capture.getConfig().setTranscoding(false);

		if(Build.VERSION.SDK_INT >= MediaCapture.MNEW_VERSION) {
			capture.Open(null, new MediaCaptureCallback() {
				@Override
				public int OnCaptureStatus(int i) {
					MediaCapture.CaptureNotifyCodes status = MediaCapture.CaptureNotifyCodes.forValue(i);
					switch (status) {
						case CAP_SURFACE_CREATED:
							runOnUiThread(new Runnable() {
								@Override
								public void run() {
									misSurfaceReady = true;
									Surface surface = capture.getSurface();
									player.setSurface(surface);
									//capture.getConfig().setUrl("rtmp://54.173.34.172:1937/publish_demo/test");
									//VXGEncoderCheckStart();
								}
							});
							break;
						case CAP_SURFACE_DESTROYED:
							runOnUiThread(new Runnable() {
								@Override
								public void run() {
									misSurfaceReady = false;
									player.setSurface(null);
								}
							});
							break;
						case CAP_STARTED:
							runOnUiThread(new Runnable() {
								@Override
								public void run() {
									edtIpAddress2.setText(capture.getRTSPAddr());
								}
							});
							break;
						case CAP_STOPPED:
							runOnUiThread(new Runnable() {
								@Override
								public void run() {
									edtIpAddress2.setText("");
								}
							});

							break;

					}
					return 0;
				}

				@Override
				public int OnCaptureReceiveData(ByteBuffer byteBuffer, int i, int i1, long l) {
					return 0;
				}
			});
			capture.setVisibility(View.VISIBLE);
			}
		}

	protected void VXGEncoderCheckStart()
	{
		if(Build.VERSION.SDK_INT < MediaCapture.MNEW_VERSION){
			Log.v(TAG, "<=check_streamer_start push mode not supported");
			return;
		}

		if(misSurfaceReady && !capture.getConfig().isStreaming() && streamInfo != null && streamInfo.length()>0){
			//capture.getConfig().setUrl(mUrlRTMP);



			boolean is_audio = streamInfo.contains("<AudioStream id=");
			if(is_audio){

				if(is_audio_configured) {
					MediaCapture.startAudioShare(null);
					capture.StartStreaming();
				}else {
					capture.getConfig().setCaptureMode(MediaCaptureConfig.CaptureModes.PP_MODE_VIDEO.val() | MediaCaptureConfig.CaptureModes.PP_MODE_AUDIO.val());
					capture.getConfig().setAudioFormat(MediaCaptureConfig.TYPE_AUDIO_AAC);

					if (player != null)
						player.setOnDataListener(new MediaPlayer.MediaPlayerCallbackData() {
							@Override
							public int OnVideoSourceFrameAvailable(ByteBuffer byteBuffer, int i, long l, long l1, int i1, int i2) {
								return 0;
							}

							@Override
							public int OnAudioSourceFrameAvailable(ByteBuffer byteBuffer, int i, long l, long l1, int i1, int i2) {
								return 0;
							}

							@Override
							public int OnVideoRendererFrameAvailable(ByteBuffer byteBuffer, int i, String s, int i1, int i2, int i3, long l, int i4) {
								return 0;
							}

							@Override
							public int OnAudioRendererFormat(int sampleRate, int channelConfig, int audioFormat, int bufferSize) {
								is_audio_configured = true;
								capture.getConfig().setAudioSamplingRate(sampleRate);
								capture.getConfig().setAudioChannels(channelConfig);
								MediaCapture.startAudioShare(MediaCapture.SOURCE_AUDIO_PUSH, sampleRate, channelConfig, audioFormat, bufferSize, null);
								capture.StartStreaming();
								return 0;
							}

							@Override
							public int OnAudioRendererFrameAvailable(ByteBuffer byteBuffer, int size, long pts) {
								int ret = MediaCapture.pushAudioSharePacket(byteBuffer, size, pts);
								//Log.v(TAG, "=OnAudioRendererFrameAvailable push ret=" + ret);
								return 0;
							}
						}, MediaPlayer.PlayerCallbackDataMask.forType(MediaPlayer.PlayerCallbackDataMask.PP_CALLBACK_DATA_RENDERER_AUDIO_DATA));
				}
			}else{
				capture.getConfig().setCaptureMode(MediaCaptureConfig.CaptureModes.PP_MODE_VIDEO.val()); //video only
				capture.StartStreaming();
			}
		}
	}

	protected void VXGDecoderOpen()
	{
		MediaPlayerConfig conf = new MediaPlayerConfig();
		conf.setDecodingType(1);
		conf.setConnectionNetworkProtocol(1);
		conf.setConnectionUrl(edtIpAddress2.getText().toString()/*"rtsp://192.168.1.72:5540/ch0"*/);
		conf.setDataReceiveTimeout(30000);

		player_view.Open(conf, new MediaPlayer.MediaPlayerCallback(){

			@Override
			public int Status(int arg) {
				Log.i(TAG, "=player_record Status arg="+arg);
				return 0;
			}

			@Override
			public int OnReceiveData(ByteBuffer buffer, int size,
									 long pts) {
				// TODO Auto-generated method stub
				return 0;
			}

		});
		btnConnect_view.setText("Disconnect");
		playing_view = true;
	}

	protected void VXGDecoderClose()
	{
		btnConnect_view.setText("Connect");
		playing_view = false;
		player_view.Close();
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
				//capture.StopStreaming();
				capture.Stop();
				player.Close();
				btnConnect.setText("START");
				playing = false;
			}
			else
			{
    	    	//player.setVisibility(View.INVISIBLE);
    	    	
    	    	MediaPlayerConfig conf = new MediaPlayerConfig();
    	    	conf.setDecodingType(1);
    	    	conf.setConnectionUrl(ConnectionUrl);
    	    	
    	    	//if (surface != null)
    	    	//{
    	    	//	Log.e(TAG, "setSurface " + surface);
    	    	//	player.setSurface(surface);
    	    	//}
				if(USE_RTSP) {
					capture.getConfig().setStreamType(MediaCaptureConfig.StreamerTypes.STREAM_TYPE_RTSP_SERVER.val());
					String rtsp_url = "rtsp://@:" + "5540";
					capture.getConfig().setUrl(rtsp_url);
					//capture.getConfig().setMulticast(true,"239.0.0.1",2);
				}else{
					capture.getConfig().setUrl("rtmp://54.173.34.172:1937/publish_demo/test2");
				}
				capture.getConfig().setVideoFramerate(30);
				capture.getConfig().setVideoKeyFrameInterval(1);
				capture.getConfig().setVideoBitrate(2024);
				capture.getConfig().setVideoResolution(MediaCaptureConfig.CaptureVideoResolution.VR_1280x720);

				//capture.getConfig().setAudioFormat( MediaCaptureConfig.TYPE_AUDIO_AAC);
				//capture.getConfig().setAudioBitrate(abitrate);
				//capture.getConfig().setAudioSamplingRate(44100); //hardcoded
				//capture.getConfig().setAudioChannels(2);


				VXGEncoderCheckStart();
    	    	player.Open(conf, this);
    			
				btnConnect.setText("STOP");
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
/*
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
*/
	private static final String TAG = "VXGTest";
}
