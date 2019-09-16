/*
 *
 * Copyright (c) 2011-2017 VXG Inc.
 *
 */


package veg.mediaplayer.sdk.test.getdata;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.MulticastLock;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.view.inputmethod.InputMethodManager;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.view.*;
import android.widget.RelativeLayout;
import android.widget.TextView.OnEditorActionListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import java.nio.ByteBuffer;
import java.util.Set;
import java.util.HashSet;
import java.util.ArrayList;

import android.preference.PreferenceManager;
import veg.mediaplayer.sdk.MediaPlayer;
import veg.mediaplayer.sdk.MediaPlayer.PlayerCallbackDataMask;
import veg.mediaplayer.sdk.MediaPlayer.PlayerNotifyCodes;
import veg.mediaplayer.sdk.MediaPlayer.PlayerProperties;
import veg.mediaplayer.sdk.MediaPlayer.PlayerRecordFlags;
import veg.mediaplayer.sdk.MediaPlayer.PlayerState;
import veg.mediaplayer.sdk.MediaPlayer.VideoShot;
import veg.mediaplayer.sdk.MediaPlayerConfig;
import veg.mediaplayer.sdk.test.getdata.VideoSurfaceView.VideoSurfaceViewCallback;

public class MainActivity extends Activity implements MediaPlayer.MediaPlayerCallback, MediaPlayer.MediaPlayerCallbackData, 
                                                        VideoSurfaceViewCallback, SurfaceHolder.Callback2
{
    private static final String TAG 	 = "GLSurfaceView.MediaPlayerTest";
    
	public  static AutoCompleteTextView	edtIpAddress;
	public  static ArrayAdapter<String> edtIpAddressAdapter;
	public  static ArrayList<String>    edtIpAddressHistory;
	private Button						btnConnect;
	private Button						btnHistory;
	private Button						btnShot;

	private SharedPreferences 			settings;
    private SharedPreferences.Editor 	editor;

    private boolean 					playing = false;
    private MainActivity 				mthis = null;

    private RelativeLayout              playerLayout = null;
    private RelativeLayout 				playerStatus = null;
    private TextView 					playerStatusText = null;
    private TextView 					playerHwStatus = null;
    
    private MulticastLock 				multicastLock = null;
    private ProgressDialog 				progress = null;
    
    private MediaPlayer                 player = null;

    private VideoSurfaceView            videoViewForHW = null;
    private SurfaceView                 videoViewForSW = null;
    
	private enum PlayerStates
	{
	  	Busy,
	  	ReadyForUse
	};

    private enum PlayerConnectType
	{
	  	Normal,
	  	Reconnecting
	};
    
	private Object waitOnMe = new Object();
	private PlayerStates player_state = PlayerStates.ReadyForUse; 
	private PlayerConnectType reconnect_type = PlayerConnectType.Normal;
	private int mOldMsg = 0;

	private Toast toastShot = null;
	private String versionName = "";

	// Event handler
	
	private Handler handler = new Handler() 
    {
		String strText = "Connecting";
		
		String sText;
		String sCode;
		
		@Override
	    public void handleMessage(Message msg) 
	    {
	    	PlayerNotifyCodes status = (PlayerNotifyCodes) msg.obj;
	        switch (status) 
	        {
	        	case CP_CONNECT_STARTING:
	        		if (reconnect_type == PlayerConnectType.Reconnecting)
	        			strText = "Reconnecting";
	        		else
	        			strText = "Connecting";
	        			

	        		player_state = PlayerStates.Busy;
	    			showStatusView();
	    			
	    			reconnect_type = PlayerConnectType.Normal;
	    			setHideControls();
	    			break;
	    			
	        	case PLP_BUILD_SUCCESSFUL:
	        		sText = player.getPropString(PlayerProperties.PP_PROPERTY_PLP_RESPONSE_TEXT);
	        		sCode = player.getPropString(PlayerProperties.PP_PROPERTY_PLP_RESPONSE_CODE);
	        		Log.i(TAG, "=Status PLP_BUILD_SUCCESSFUL: Response sText="+sText+" sCode="+sCode);
	        		break;
	                
		    	case VRP_NEED_SURFACE:
		    		player_state = PlayerStates.Busy;
		    		showVideoView();
					break;
	
		    	case PLP_PLAY_SUCCESSFUL:
		    		player_state = PlayerStates.ReadyForUse;
	    			playerStatusText.setText("");
		            setTitle(getResources().getText(R.string.app_name) + versionName);
		    		
		    		// temp for test
		    		String info = player.getStreamInfo();
	        		Log.i(TAG, info);
		    		new AlertDialog.Builder(MainActivity.this)
		    		.setTitle("Info")
		    		.setMessage(info)
		    		.setPositiveButton("Close", new DialogInterface.OnClickListener() 
		    		{
		    			public void onClick(DialogInterface dialog, int which) 
		    			{
		    				edtIpAddressHistory.clear();
		    				loadHistory(false);  
		    			}
		    		})
		    		.show();
		    		
		    		
			        break;
	                
	        	case PLP_CLOSE_STARTING:
	        		player_state = PlayerStates.Busy;
	    			playerStatusText.setText("Disconnected");
	    			showStatusView();
	    			setUIDisconnected();
	    			break;
	                
	        	case PLP_CLOSE_SUCCESSFUL:
	        		player_state = PlayerStates.ReadyForUse;
	    			playerStatusText.setText("Disconnected");
	    			showStatusView();
	    			System.gc();
	    			setShowControls();
	    			setUIDisconnected();
	                break;
	                
	        	case PLP_CLOSE_FAILED:
	        		player_state = PlayerStates.ReadyForUse;
	    			playerStatusText.setText("Disconnected");
	    			showStatusView();
	    			setShowControls();
	    			setUIDisconnected();
	   			break;
	               
	        	case CP_CONNECT_FAILED:
	        		player_state = PlayerStates.ReadyForUse;
	    			playerStatusText.setText("Disconnected");
	    			showStatusView();
	    			setShowControls();
	    			setUIDisconnected();
	    			break;
	                
	            case PLP_BUILD_FAILED:
	        		sText = player.getPropString(PlayerProperties.PP_PROPERTY_PLP_RESPONSE_TEXT);
	        		sCode = player.getPropString(PlayerProperties.PP_PROPERTY_PLP_RESPONSE_CODE);
	        		Log.i(TAG, "=Status PLP_BUILD_FAILED: Response sText="+sText+" sCode="+sCode);

	            	player_state = PlayerStates.ReadyForUse;
	    			playerStatusText.setText("Disconnected");
	    			showStatusView();
	    			setShowControls();
	    			setUIDisconnected();
	    			break;
	                
	            case PLP_PLAY_FAILED:
	            	player_state = PlayerStates.ReadyForUse;
	    			playerStatusText.setText("Disconnected");
	    			showStatusView();
	    			setShowControls();
	    			setUIDisconnected();
	    			break;
	                
	            case PLP_ERROR:
	            	player_state = PlayerStates.ReadyForUse;
	    			playerStatusText.setText("Disconnected");
	    			showStatusView();
	    			setShowControls();
	    			setUIDisconnected();
	    			break;
	                
	            case CP_INTERRUPTED:
	            	player_state = PlayerStates.ReadyForUse;
	    			playerStatusText.setText("Disconnected");
	    			showStatusView();
	    			setShowControls();
	    			setUIDisconnected();
	    			break;
	            case CP_RECORD_STARTED:
	            	Log.v(TAG, "=handleMessage CP_RECORD_STARTED");
	            	{
	            		String sFile = player.RecordGetFileName(1);
	            		Toast.makeText(getApplicationContext(),"Record Started. File "+sFile, Toast.LENGTH_LONG).show();
	            	}
	            	break;

	            case CP_RECORD_STOPPED:
	            	Log.v(TAG, "=handleMessage CP_RECORD_STOPPED");
	            	{
	            		String sFile = player.RecordGetFileName(0);
	            		Toast.makeText(getApplicationContext(),"Record Stopped. File "+sFile, Toast.LENGTH_LONG).show();
	            	}
	            	break;

	            case CP_START_BUFFERING:
//	            	progress = ProgressDialog.show(mthis, null,
//	            		    "buffering...", true);
	            	break;

	            case CP_STOP_BUFFERING:
//	            	if (progress != null)
//	            		progress.dismiss();
	            	break;

//	            case CP_CONNECT_AUTH_SUCCESSFUL:
//	            	Log.v(TAG, "=handleMessage CP_CONNECT_AUTH_SUCCESSFUL");
//	            	{
//	            		Toast.makeText(getApplicationContext(),"Authentification ok!", Toast.LENGTH_SHORT).show();
//	            	}
//	            	break;
//
//	            case CP_CONNECT_AUTH_FAILED:
//	            	Log.v(TAG, "=handleMessage CP_CONNECT_AUTH_FAILED");
//	            	{
//	            		Toast.makeText(getApplicationContext(),"Authentification failed!", Toast.LENGTH_SHORT).show();
//	            	}
//	            	break;
	            	
	            //case CONTENT_PROVIDER_ERROR_DISCONNECTED:
	            case CP_STOPPED:
	            case VDP_STOPPED:
	            case VRP_STOPPED:
	            case ADP_STOPPED:
	            case ARP_STOPPED:
	            	if (player_state != PlayerStates.Busy)
	            	{
	            		player_state = PlayerStates.Busy;
						if (toastShot != null)
							toastShot.cancel();
	            		player.Close();
	        			playerStatusText.setText("Disconnected");
		    			showStatusView();
		    			player_state = PlayerStates.ReadyForUse;
		    			setShowControls();
		    			setUIDisconnected();
	            	}
	                break;
	
	            case CP_ERROR_DISCONNECTED:
//	            	if (player_state != PlayerStates.Busy)
//	            	{
//	            		player_state = PlayerStates.Busy;
//						if (toastShot != null)
//							toastShot.cancel();
//	            		player.Close();
//
//	        			playerStatusText.setText("Disconnected");
//		    			showStatusView();
//		    			player_state = PlayerStates.ReadyForUse;
//		    			setUIDisconnected();
//	            		
//						Toast.makeText(getApplicationContext(), "Demo Version!",
//								   Toast.LENGTH_SHORT).show();
//						
//	            	}
	                break;
	            default:
	            	player_state = PlayerStates.Busy;
	        }
	    }
	};

	// callbacks from Native Player 
	// MediaPlayerCallback
	// All event are sent to event handlers    
	@Override
	public int Status(int arg)
	{
		
		PlayerNotifyCodes status = PlayerNotifyCodes.forValue(arg);
		if (handler == null || status == null)
			return 0;
		
		Log.v(TAG, "Form Native Player status: " + arg);
	    switch (PlayerNotifyCodes.forValue(arg)) 
	    {
    	    case PLP_EOS:
            case CP_ERROR_DISCONNECTED:
//    	        runOnUiThread(new Runnable()
//    	        {
//    	            @Override
//    	            public void run()
//    	            {
//                        restartPlayer();
//    	            }
//    	        });
//    	        break;
	        
	        default:     
				Message msg = new Message();
				msg.obj = status;
				handler.removeMessages(mOldMsg);
				mOldMsg = msg.what;
				handler.sendMessage(msg);
	    }
	    
		return 0;
	}

    @Override
    public int OnReceiveData(ByteBuffer buffer, int size, long pts) 
    {
        Log.v(TAG, "Form Native Player OnReceiveData: size: " + size + ", pts: " + pts);
        return 0;
    }
	
    // callbacks from Native Player: MediaPlayerCallbackData
    @Override
    public int OnVideoSourceFrameAvailable(ByteBuffer buffer, int size, long pts, long dts, int stream_index, int format) {
        Log.v(TAG, "OnVideoSourceFrameAvailable: size:" + size + ", pts: " + pts + ", dts: " + dts + ", stream_index: " + stream_index + ", format: " + format);
        
//        final byte[] frame = new byte[size];
//        buffer.get(frame);
//        buffer.clear();
        
        return 0;
    }

    @Override
    public int OnAudioSourceFrameAvailable(ByteBuffer buffer, int size, long pts, long dts, int stream_index, int format) {
        Log.v(TAG, "OnAudioSourceFrameAvailable: size:" + size + ", pts: " + pts + ", dts: " + dts + ", stream_index: " + stream_index + ", format: " + format);
        
//        final byte[] frame = new byte[size];
//        buffer.get(frame);
//        buffer.clear();
        
        return 0;
    }

    // Will received for SW decoder only, because for HW we use hardware memory and operate only index
    // For receive video frame for HW decoder we will use GLSurfaceView 
    // videoViewForHW.getFrame as sample
    @Override
    public int OnVideoRendererFrameAvailable(ByteBuffer buffer, int size, String format_fourcc, int width, int height,
            int bytes_per_row, long pts, int will_show) {
        Log.v(TAG, "OnVideoRendererFrameAvailable: size:" + size + ", format_fourcc: " + format_fourcc + ", width: " + width
                + ", height: " + height + ", bytes_per_row: " + bytes_per_row + ", pts: " + pts + ", will_show: " + will_show);
        
//        final byte[] frame = new byte[size];
//        buffer.get(frame);
//        buffer.clear();
        
        return 0;
    }

	@Override
	public int OnAudioRendererFormat(int i, int i1, int i2, int i3) {
		return 0;
	}

	@Override
	public int OnAudioRendererFrameAvailable(ByteBuffer byteBuffer, int i, long l) {
		return 0;
	}


	@Override
    public void onCreate(Bundle savedInstanceState) 
	{
		String  strUrl;
		try 
		{
            versionName = " " + getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            versionName = versionName + "(" + getPackageManager().getPackageInfo(getPackageName(), 0).versionCode + ")";
        }
		catch (NameNotFoundException e) 
		{
        }

		setTitle(getResources().getText(R.string.app_name) + versionName);
		
        Log.e(TAG, "Version: " + versionName);
		
		super.onCreate(savedInstanceState);

		WifiManager wifi = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
		multicastLock = wifi.createMulticastLock("multicastLock");
		multicastLock.setReferenceCounted(true);
		multicastLock.acquire();
		
		getWindow().requestFeature(Window.FEATURE_PROGRESS);
		getWindow().setFeatureInt(Window.FEATURE_PROGRESS, Window.PROGRESS_VISIBILITY_ON);
		
		setContentView(R.layout.main);
		mthis = this;
		
		settings = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);

		SharedSettings.getInstance(this).loadPrefSettings();
		SharedSettings.getInstance().savePrefSettings();
		
        playerLayout        = (RelativeLayout)findViewById(R.id.playerViewRelativeLayout);
		playerStatus 		= (RelativeLayout)findViewById(R.id.playerStatus);
		playerStatusText 	= (TextView)findViewById(R.id.playerStatusText);
		playerHwStatus 		= (TextView)findViewById(R.id.playerHwStatus);

		
		player = new MediaPlayer(this, false);
		
		videoViewForHW = new VideoSurfaceView(this, this);
		
        videoViewForSW = new SurfaceView(this);
        videoViewForSW.getHolder().addCallback(this);
        
		strUrl = settings.getString("connectionUrl", "rtsp://3.84.6.190/vod/mp4:BigBuckBunny_115k.mov");
		
		player.setOnTouchListener(new View.OnTouchListener() 
		{
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) 
            {
                switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) 
                {
                    case MotionEvent.ACTION_DOWN:
                    {
                    	if (player.getState() == PlayerState.Paused)
                    		player.Play();
                    	else
                        	if (player.getState() == PlayerState.Started)
                        		player.Pause();
                    }
                }
            		
	        	return true;
            }
        });
			

		this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN); 
	
		edtIpAddress = (AutoCompleteTextView)findViewById(R.id.edit_ipaddress);
		edtIpAddress.setText(strUrl);

		loadHistory(false);

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
		btnHistory.setOnClickListener(new View.OnClickListener() 
		{
			public void onClick(View v) 
			{
				InputMethodManager in = (InputMethodManager) getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
				in.hideSoftInputFromWindow(MainActivity.edtIpAddress.getApplicationWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
				
				if (edtIpAddressHistory.size() <= 0)
					return;

				MainActivity.edtIpAddressAdapter = new ArrayAdapter<String>(MainActivity.this, R.layout.history_item, edtIpAddressHistory);
				MainActivity.edtIpAddress.setAdapter(MainActivity.edtIpAddressAdapter);
				MainActivity.edtIpAddress.showDropDown();
			}   
		});

		btnShot = (Button)findViewById(R.id.button_shot);
		btnShot.setOnClickListener(new View.OnClickListener() 
		{
			public void onClick(View v) 
			{
				if (player != null)
				{
				    Log.v(TAG, "getVideoShot()");
                    SharedSettings sett = SharedSettings.getInstance();

                    if (sett.decoderType == 1)
                    {
                        ByteBuffer frame = videoViewForHW.getFrame(videoViewForHW.getSurfaceWidth(), videoViewForHW.getSurfaceHeight());
                        if (frame == null)
                            return;
                      
                        // get your custom_toast.xml layout
                        LayoutInflater inflater = getLayoutInflater();
       
                        View layout = inflater.inflate(R.layout.videoshot_view,
                                                (ViewGroup) findViewById(R.id.videoshot_toast_layout_id));
       
                        ImageView image = (ImageView) layout.findViewById(R.id.videoshot_image);
                        image.setImageBitmap(getFrameAsBitmap(frame, videoViewForHW.getSurfaceWidth(), videoViewForHW.getSurfaceHeight()));
                      
                        // Toast...
                        if (toastShot != null)
                            toastShot.cancel();

                        toastShot = new Toast(getApplicationContext());
                        toastShot.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
                        toastShot.setDuration(Toast.LENGTH_LONG);
                        toastShot.setView(layout);
                        toastShot.show();
                    }
                    else
                    {
                        VideoShot frame = player.getVideoShot(-1, -1);
                        if (frame == null)
                            return;
                          
                        // get your custom_toast.xml layout
                        LayoutInflater inflater = getLayoutInflater();
         
                        View layout = inflater.inflate(R.layout.videoshot_view,
                          (ViewGroup) findViewById(R.id.videoshot_toast_layout_id));
         
                        ImageView image = (ImageView) layout.findViewById(R.id.videoshot_image);
                        image.setImageBitmap(getFrameAsBitmap(frame.getData(), frame.getWidth(), frame.getHeight()));
                                          
                                          // Toast...
                        if (toastShot != null)
                            toastShot.cancel();
                        
                        toastShot = new Toast(getApplicationContext());
                        toastShot.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
                        toastShot.setDuration(Toast.LENGTH_LONG);
                        toastShot.setView(layout);
                        toastShot.show();
                    }
				}
			}   
		});

		btnConnect = (Button)findViewById(R.id.button_connect);


		btnConnect.setOnClickListener(new View.OnClickListener() 
        {
            public void onClick(View v) 
            {
                SharedSettings.getInstance().loadPrefSettings();
                if (player != null)
                {
                    if (!edtIpAddressHistory.contains(player.getConfig().getConnectionUrl()))
                       edtIpAddressHistory.add(player.getConfig().getConnectionUrl());
                    
                    player.getConfig().setConnectionUrl(edtIpAddress.getText().toString());
                    if (player.getConfig().getConnectionUrl().isEmpty())
                        return;

                    if (toastShot != null)
                        toastShot.cancel();
                    
                    //player_record.Close();
                    
                    player.Close();

                    playerLayout.removeView(videoViewForHW);
                    playerLayout.removeView(videoViewForSW);
                    
                    if (playing)
                    {
                        setUIDisconnected();
                    }
                    else
                    {
                        SharedSettings sett = SharedSettings.getInstance();

                        if (sett.decoderType == 1)
                            playerLayout.addView(videoViewForHW);
                        else
                            playerLayout.addView(videoViewForSW);



                        
                        MediaPlayerConfig conf = new MediaPlayerConfig();
                        
                        conf.setConnectionUrl(player.getConfig().getConnectionUrl());
                        
                        conf.setConnectionNetworkProtocol(-1);
                        conf.setConnectionDetectionTime(sett.connectionDetectionTime);
                        conf.setConnectionBufferingTime(sett.connectionBufferingTime);

                        conf.setDecodingType(sett.decoderType);

                        
                        conf.setSynchroEnable(sett.synchroEnable);
                        conf.setSynchroNeedDropVideoFrames(sett.synchroNeedDropVideoFrames);


                        conf.setEnableAspectRatio(sett.rendererEnableAspectRatio);
                        conf.setDataReceiveTimeout(30000);
                        conf.setNumberOfCPUCores(0);

                        conf.setEnableABR(0);    
                        conf.setAspectRatioMode(1); // Zoom and move mode

                        //player.setSurface(videoView.getSurface());

                        // for receive all data
                        player.setOnDataListener(mthis);
                        

                        player.Open(conf, mthis);
                        btnConnect.setText("Disconnect");
                        
                        playing = true;
                    }
                }
            }   
        });
        
        
        RelativeLayout layout = (RelativeLayout) findViewById(R.id.main_view);
        layout.setOnTouchListener(new OnTouchListener() 
		{
			public boolean onTouch(View v, MotionEvent event) 
			{
				InputMethodManager inputManager = (InputMethodManager) getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
				if (getWindow() != null && getWindow().getCurrentFocus() != null && getWindow().getCurrentFocus().getWindowToken() != null)
					inputManager.hideSoftInputFromWindow(getWindow().getCurrentFocus().getWindowToken(), 0);
				return true;
			}
		});
        
		playerStatusText.setText("DEMO VERSION");
		setShowControls();
    }

    @Override
    public void onSurfaceCreated(Surface surface) {
        if (player != null)
            player.setSurface(surface);
    }
    
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (player != null)
            player.setSurface(holder.getSurface());
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        // TODO Auto-generated method stub
        
    }


    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (player != null)
            player.setSurface(null);
    }

    @Override
    public void surfaceRedrawNeeded(SurfaceHolder holder) {
        // TODO Auto-generated method stub
        
    }
    
    private int[] mColorSwapBuf = null;                        // used by saveFrame()
    public Bitmap getFrameAsBitmap(ByteBuffer frame, int width, int height)
    {
        Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bmp.copyPixelsFromBuffer(frame);
        return bmp;
    }
    
    public Bitmap getFrameAsBitmapWithColorSwap(ByteBuffer buf, int width, int height)
    {
        int pixelCount = width * height;
        mColorSwapBuf = new int[pixelCount];
        buf.asIntBuffer().get(mColorSwapBuf);
        for (int i = 0; i < pixelCount; i++) {
            int c = mColorSwapBuf[i];
            mColorSwapBuf[i] = (c & 0xff00ff00) | ((c & 0x00ff0000) >> 16) | ((c & 0x000000ff) << 16);
        }
        
        Bitmap bmp = Bitmap.createBitmap(mColorSwapBuf, width, height, Bitmap.Config.ARGB_8888);
        return bmp;
    }
    
    
	protected void onPause()
	{
		Log.e(TAG, "onPause()");
		super.onPause();

		editor = settings.edit();
		editor.putString("connectionUrl", edtIpAddress.getText().toString());

		editor.putStringSet("connectionHistory", new HashSet<String>(edtIpAddressHistory));
		editor.commit();
		
		if (player != null)
			player.onPause();
	}

	@Override
  	protected void onResume() 
	{
		Log.e(TAG, "onResume()");
		videoViewForHW.onResume();
        //videoViewForHW.onResume();
		
		super.onResume();
		if (player != null)
			player.onResume();
  	}

  	@Override
	protected void onStart() 
  	{
      	Log.e(TAG, "onStart()");
		super.onStart();
		if (player != null)
			player.onStart();
	}

  	@Override
	protected void onStop() 
  	{
  		Log.e(TAG, "onStop()");
		super.onStop();
		if (player != null)
			player.onStop();
		
		if (toastShot != null)
			toastShot.cancel();

	}

    @Override
    public void onBackPressed() 
    {
//    	if (progress != null)
//    		progress.dismiss();

    	if (toastShot != null)
			toastShot.cancel();
		
		player.Close();
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
		if (player != null)
			player.onWindowFocusChanged(hasFocus);
  	}

  	@Override
  	public void onLowMemory() 
  	{
  		Log.e(TAG, "onLowMemory()");
  		super.onLowMemory();
		if (player != null)
			player.onLowMemory();
  	}

  	@Override
  	protected void onDestroy() 
  	{
  		Log.e(TAG, "onDestroy()");
		if (toastShot != null)
			toastShot.cancel();
		
		if (player != null)
			player.onDestroy();
		
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
						edtIpAddressHistory.clear();
						loadHistory(false);  
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

	protected void loadHistory(boolean withSaved)
	{
		ArrayList<String> tempHistory = new ArrayList<String>();
		
		tempHistory.add("rtsp://3.84.6.190/vod/mp4:BigBuckBunny_115k.mov");
        tempHistory.add("http://devimages.apple.com/iphone/samples/bipbop/gear1/prog_index.m3u8");
		
		edtIpAddressHistory = new ArrayList<String>();
        edtIpAddressHistory.addAll(tempHistory);
        if (withSaved)
        {
            Set<String> savedHistory = settings.getStringSet("connectionHistory", null);
            edtIpAddressHistory.addAll(savedHistory);
        }

        edtIpAddress.setText(tempHistory.get(0));
	}
	
	protected void setUIDisconnected()
	{
        setTitle(getResources().getText(R.string.app_name) + versionName);
		btnConnect.setText("Connect");
		playing = false;
	}

	protected void setHideControls()
	{
		btnShot.setVisibility(View.VISIBLE);
		edtIpAddress.setVisibility(View.GONE);
		btnHistory.setVisibility(View.GONE);
		btnConnect.setVisibility(View.GONE);
	}

	protected void setShowControls()
	{
        setTitle(getResources().getText(R.string.app_name) + versionName);
		
		btnShot.setVisibility(View.GONE);
		edtIpAddress.setVisibility(View.VISIBLE);
		btnHistory.setVisibility(View.VISIBLE);
		btnConnect.setVisibility(View.VISIBLE);
	}

	private void showStatusView() 
	{
		//player.setVisibility(View.INVISIBLE);
		playerHwStatus.setVisibility(View.INVISIBLE);
		//player.setAlpha(0.0f);
		playerStatus.setVisibility(View.VISIBLE);
	}
	
	private void showVideoView() 
	{
        playerStatus.setVisibility(View.INVISIBLE);
 		//player.setVisibility(View.VISIBLE);
		playerHwStatus.setVisibility(View.VISIBLE);


		
		setTitle("");
	}
    

    static public <T> void executeAsyncTask(AsyncTask<T, ?, ?> task, T... params) 
    {
    	{
    		task.execute(params);
    	}
    }

}
