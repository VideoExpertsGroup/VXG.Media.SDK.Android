/*
 *
 * Copyright (c) 2011-2017 VXG Inc.
 *
 */


package vxg.mediaplayer.onvif.sdk.test;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.MulticastLock;
import android.opengl.GLES20;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import veg.mediaplayer.sdk.MediaPlayer;
import veg.mediaplayer.sdk.MediaPlayer.MediaPlayerCallback;
import veg.mediaplayer.sdk.MediaPlayer.PlayerModes;
import veg.mediaplayer.sdk.MediaPlayer.PlayerNotifyCodes;
import veg.mediaplayer.sdk.MediaPlayer.PlayerProperties;
import veg.mediaplayer.sdk.MediaPlayer.PlayerRecordFlags;
import veg.mediaplayer.sdk.MediaPlayer.PlayerState;
import veg.mediaplayer.sdk.MediaPlayer.VideoShot;
import veg.mediaplayer.sdk.MediaPlayerConfig;
import vxg.mediaonvif.sdk.MediaOnvif;
import vxg.mediaonvif.sdk.MediaOnvifConfig;

public class MainActivity extends Activity implements OnClickListener, MediaPlayer.MediaPlayerCallback
{
    private static final String TAG 	 = "MediaPlayerTest";
	private MediaOnvif onvif;
	public  static AutoCompleteTextView edtIpAddress;
	public  static AutoCompleteTextView edtIpAddress1;
	public String curUrl;
	public  static ArrayAdapter<String> edtIpAddressAdapter;
	public  static Set<String> edtIpAddressHistory;
	private Button btnConnect;
	private Button btnHistory;
	private Button btnShot;
	private Button btnRecord;
	private Button b_up;
	private Button b_down;
	private Button b_right;
	private Button b_left;
	private Button b_zoom_inc;
	private Button b_zoom_dec;
	private Button b_stop;
	private Button b_abs_pos;
	private Button b_pre_remove;
	private Button b_pre_set;
	private Button b_pre_goto;
	private Button b_pre_set_home;
	private Button b_pre_goto_home;
	
	private ArrayList<String> profileUrls=new ArrayList<>();
	private String user_name="admin";
	private String password="admin";
	private boolean						is_record = false;

	//private StatusProgressTask 			mProgressTask = null;

	private SharedPreferences settings;
    private SharedPreferences.Editor 	editor;

    private boolean 					playing = false;
    private MediaPlayer 				player = null;
    //private MediaPlayer 				player_record = null;
    private MainActivity 				mthis = null;

    private RelativeLayout playerStatus = null;
    private TextView playerStatusText = null;
    private TextView playerHwStatus = null;

	public ScaleGestureDetector detectors = null;

    private MulticastLock multicastLock = null;

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

					player_state = PlayerStates.ReadyForUse;
	    			showStatusView();

	    			reconnect_type = PlayerConnectType.Normal;
	    			setHideControls();
	    			break;

	        	case PLP_BUILD_SUCCESSFUL:
					player_state = PlayerStates.ReadyForUse;
	        		sText = player.getPropString(PlayerProperties.PP_PROPERTY_PLP_RESPONSE_TEXT);
	        		sCode = player.getPropString(PlayerProperties.PP_PROPERTY_PLP_RESPONSE_CODE);
	        		Log.i(TAG, "=Status PLP_BUILD_SUCCESSFUL: Response sText="+sText+" sCode="+sCode);
	        		break;

		    	case VRP_NEED_SURFACE:
					player_state = PlayerStates.ReadyForUse;
		    		showVideoView();
					break;

		    	case PLP_PLAY_SUCCESSFUL:
					player.setVisibility(View.VISIBLE);
		    		player_state = PlayerStates.ReadyForUse;
	    			playerStatusText.setText("");
		    		setTitle(R.string.app_name);
			        break;

	        	case PLP_CLOSE_STARTING:
	        		player_state = PlayerStates.Busy;
	    			playerStatusText.setText("Disconnected");
	    			showStatusView();
	    			setUIDisconnected();
	    			break;

	        	case PLP_CLOSE_SUCCESSFUL:
	        		player_state = PlayerStates.Busy;
	    			playerStatusText.setText("Disconnected");
	    			showStatusView();
	    			System.gc();
	    			setShowControls();
	    			setUIDisconnected();
	                break;

	        	case PLP_CLOSE_FAILED:
	        		player_state = PlayerStates.Busy;
	    			playerStatusText.setText("Disconnected");
	    			showStatusView();
	    			setShowControls();
	    			setUIDisconnected();
	   			break;

	        	case CP_CONNECT_FAILED:
	        		player_state = PlayerStates.Busy;
	    			playerStatusText.setText("Disconnected");
	    			showStatusView();
	    			setShowControls();
	    			setUIDisconnected();
	    			break;

	            case PLP_BUILD_FAILED:
	        		sText = player.getPropString(PlayerProperties.PP_PROPERTY_PLP_RESPONSE_TEXT);
	        		sCode = player.getPropString(PlayerProperties.PP_PROPERTY_PLP_RESPONSE_CODE);
	        		Log.i(TAG, "=Status PLP_BUILD_FAILED: Response sText="+sText+" sCode="+sCode);

	        		player_state = PlayerStates.Busy;
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

				case CP_ERROR_NODATA_TIMEOUT:
					Toast.makeText(getApplicationContext(), "CP_ERROR_NODATA_TIMEOUT "+player_state,
							   Toast.LENGTH_SHORT).show();
					break;

				case PLP_TRIAL_VERSION:
				case PLP_EOS:
	            case CP_ERROR_DISCONNECTED:
					Toast.makeText(getApplicationContext(), ((status == PlayerNotifyCodes.PLP_EOS) ? "PLP_EOS ":"CP_ERROR_DISCONNECTED ")+player_state,
							   Toast.LENGTH_SHORT).show();
	            	if (player_state != PlayerStates.Busy)
	            	{
	            		player_state = PlayerStates.Busy;
						if (toastShot != null)
							toastShot.cancel();
	            		player.Close();

	        			playerStatusText.setText("Disconnected");
		    			showStatusView();
		    			player_state = PlayerStates.ReadyForUse;
		    			setUIDisconnected();

						if(status == PlayerNotifyCodes.PLP_TRIAL_VERSION){
							Toast.makeText(getApplicationContext(), "Demo Version!",
								   Toast.LENGTH_SHORT).show();
						}
	            	}
	                break;
	        }
	    }
	};

	// callback from Native Player
	@Override
	public int OnReceiveData(ByteBuffer buffer, int size, long pts)
	{
		Log.e(TAG, "Form Native Player OnReceiveData: size: " + size + ", pts: " + pts);
		return 0;
	}


	// All event are sent to event handlers
	@Override
	public int Status(int arg)
	{

		PlayerNotifyCodes status = PlayerNotifyCodes.forValue(arg);
		if (handler == null || status == null)
			return 0;

		Log.e(TAG, "From Native Player status: " + arg);
	    switch (PlayerNotifyCodes.forValue(arg))
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

    public String getRecordPath()
    {
    	File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
    		      Environment.DIRECTORY_DCIM), "RecordsMediaPlayer");

	    if (! mediaStorageDir.exists()){
	        if (!(mediaStorageDir.mkdirs() || mediaStorageDir.isDirectory())){
	            Log.e(TAG, "<=getRecordPath() failed to create directory path="+mediaStorageDir.getPath());
	            return "";
	        }
	    }
	    return mediaStorageDir.getPath();
    }
	public String getCamUrl(String profiles,int index)
	{

		String pr =null;
		int ind1=0;
		int ind2=0;
		for(int i=0;i<index+1;i++)
		{

			ind1=profiles.indexOf("<uri>",ind1+5);
			ind2=profiles.indexOf("</uri>",ind2+6);
			if(ind1==-1)
				break;
		}

		try {
			pr = profiles.substring(ind1+5, ind2);

		}
		catch (java.lang.StringIndexOutOfBoundsException e)
		{

		}

		return pr;
	}
	private Handler handler1 = new Handler()
	{
		String strText = "Connecting";

		String sText;
		String sCode;

		@Override
		public void handleMessage(Message msg)
		{

			MediaOnvif.OnvifNotifyCodes status = (MediaOnvif.OnvifNotifyCodes) msg.obj;
			switch (status)
			{
				case PLP_CONNECTED_SUCCESSFUL:
                     //if your index is out of profiles array range then the nearest element is set and MediaOnvif.OnvifNotifyCodes.OUT_OF_RANGE_ERROR comes
					//othewise MediaOnvif.OnvifNotifyCodes.OK comes in response
					MediaOnvif.OnvifNotifyCodes state=onvif.setPropInt(MediaOnvif.OnvifProperties.PP_PROPERTY_CUR_PROFILE.ordinal(),0);
					String profiles = onvif.getPropString(MediaOnvif.OnvifProperties.PP_PROPERTY_PROFILES);
					Log.e(TAG, "handleMessage PLP_CONNECTED_SUCCESSFUL :" + profiles);
					String url=null;
					String urlT=getCamUrl(profiles,0);
					urlT=urlT.replace("192.168.1.200","admin:admin@96.69.46.121");
					//rtsp://admin:admin@96.69.46.121:554/live/90c92494-2f6c-443e-95a0-1dafb3c6ff09
					int ind=0;
					profileUrls.clear();
					do
					{
						url=getCamUrl(profiles,ind++);
						if(url!=null)
						profileUrls.add(url.replace("192.168.1.200","admin:admin@96.69.46.121"));

					}while (url!=null);

					if(profileUrls.size()>0)
					{




						edtIpAddress1.setText(profileUrls.get(1));
						curUrl=url;
						playerStatusText.setText("SUCCESS!");
						//startPlayer(url);
					}
					else
						playerStatusText.setText("CONNECTION_FAILED");
					//startPlayer("rtsp://admin:admin@96.69.46.121:554/live/90c92494-2f6c-443e-95a0-1dafb3c6ff09");
					break;
				case PNC_CONNECTED_FAILED:
					Log.e(TAG, "handleMessage:  PNC_CONNECTED_FAILED");
					playerStatusText.setText("PNC_CONNECTED_FAILED");
					break;
				case PLP_CONNECTED_AUTH_FAILED:
					Log.e(TAG, "handleMessage:  PLP_CONNECTED_AUTH_FAILED");
					playerStatusText.setText("PLP_CONNECTED_AUTH_FAILED");
					break;

				default:
					break;
			}
		}
	};

	MediaOnvif.MediaOnvifCallback call=new MediaOnvif.MediaOnvifCallback()
	{
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

			MediaOnvif.OnvifNotifyCodes status = MediaOnvif.OnvifNotifyCodes.forValue(arg);
			if (handler1 == null || status == null)
				return 0;

			Log.e(TAG, "From Native Onvif status: " + arg);
			switch (MediaOnvif.OnvifNotifyCodes.forValue(arg))
			{
				default:
					Message msg = new Message();
					msg.obj = status;
					//handler.removeMessages(mOldMsg);
					//mOldMsg = msg.what;
					handler1.sendMessage(msg);
			}

			return 0;
		}
	};

	// 0 - PTZ_ContinuousMove
	// 1 - PTZ_RelativeMove
	int PTZ_MODE 	= 0;	
	int PRESET_ID 	= 1; // from 1 to MAX (MAX depends camera capabilities)
	 
	//PanTilt movement should not be affected
	// PanTilt_x,PanTilt_y - Pan and tilt speed. The x component corresponds to pan and the y component to tilt.
	void PTZ_ContinuousMove(float PanTilt_x,float PanTilt_y)
		{
			// Indicates whether the field PanTilt is valid
			int PanTiltFlag = 1;
			// An optional Timeout parameter, unit is second
			// 0 - Timeout Flag is disabled
			int Timeout = 10;
			//Pan and tilt speed.
			// A zoom speed.
			//If omitted in a request,
			//the current (if any) Zoom movement should not be affected
			float Zoom		= 0.0f;
			onvif.PTZControl(PanTiltFlag,Timeout,PanTilt_x,PanTilt_y,Zoom);
		}	

	// A positional Translation relative to the current position
	// shift_x,shift_y : Pan and tilt position. The x component corresponds to pan and the y component to tilt
	// speed_x,speed_y : Pan and tilt speed. The x component corresponds to pan and the y component to tilt. If omitted in a request, the current (if any) PanTilt movement should not be affected
	void PTZ_RelativeMove(float shift_x,float shift_y,float speed_x,float speed_y)
		{
			// Indicates whether the field PanTilt is valid
			int shift_flag 	= 1;
			// Indicates whether the field Zoom is valid
			int speed_flag 	= 1;
			
			float Zoom		= 0.0f;
			onvif.PTZRelativeMove( shift_flag,
										shift_x,
										shift_y,
										speed_flag,
										speed_x,
										speed_y,
										Zoom);
		}	

	// A Position vector specifying the absolute target position
	// shift_x,shift_y : Pan and tilt position. The x component corresponds to pan and the y component to tilt
	// speed_x,speed_y : Pan and tilt speed. The x component corresponds to pan and the y component to tilt. If omitted in a request, the current (if any) PanTilt movement should not be affected
	void PTZ_AbsoluteMove(float pos_x,float pos_y,float speed_x,float speed_y)
		{
			// Indicates whether the field PanTilt is valid
			int shift_flag 	= 1;
			// Indicates whether the field Zoom is valid
			int speed_flag 	= 1;
			
			float Zoom		= 0.0f;
			onvif.PTZAbsoluteMove(	shift_flag,
												pos_x,
												pos_y,
												speed_flag,
												speed_x,
												speed_y,
												Zoom);
		}	



	//Zoom - zoom speed.
	void PTZ_Zoom(float Zoom)
		{
			// Indicates whether the field PanTilt is valid
			int PanTiltFlag = 1;
			// optional, An optional Timeout parameter, unit is second
			// 0 - Timeout Flag is disabled
			int Timeout = 10;
			// optional,
			//Pan and tilt speed.
			//The x component corresponds to pan and the y component to tilt.
			//If omitted in a request, the current (if any)
			//PanTilt movement should not be affected
			float PanTilt_x = 0.0f;
			float PanTilt_y = 0.0f;
			onvif.PTZControl(PanTiltFlag,Timeout,PanTilt_x,PanTilt_y,Zoom);
		}			
	
    @Override
    public void onCreate(Bundle savedInstanceState)
	{
		String strUrl;
		onvif = new MediaOnvif(this);


		setTitle(R.string.app_name);
		super.onCreate(savedInstanceState);

		WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
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

		playerStatus 		= (RelativeLayout)findViewById(R.id.playerStatus);
		playerStatusText 	= (TextView)findViewById(R.id.playerStatusText);
		playerHwStatus 		= (TextView)findViewById(R.id.playerHwStatus);

		player = (MediaPlayer)findViewById(R.id.playerView);

		//record only
		//player_record = new MediaPlayer(this, false);

		strUrl = settings.getString("connectionUrl", "http://admin:1234@10.20.16.80:80/onvif/device_service/");

		player.getSurfaceView().setZOrderOnTop(true);    // necessary
		SurfaceHolder sfhTrackHolder = player.getSurfaceView().getHolder();
		sfhTrackHolder.setFormat(PixelFormat.TRANSPARENT);

		HashSet<String> tempHistory = new HashSet<String>();

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
                        	if (player.getState() == PlayerState.Started){
                        		player.Pause();
                        		//player.PauseFlush();
                        	}
                    }
                }

	        	return true;
            }
        });


		edtIpAddressHistory = settings.getStringSet("connectionHistory", tempHistory);
		edtIpAddressHistory.add("http://admin:1234@10.20.16.80:80/onvif/device_service/");
		this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

		edtIpAddress = (AutoCompleteTextView)findViewById(R.id.edit_ipaddress);
		edtIpAddress.setText("http://admin:1234@10.20.16.80:80/onvif/device_service/");
		edtIpAddress1 = (AutoCompleteTextView)findViewById(R.id.edit_ipaddress1);
		//edtIpAddress1.setText(strUrl);

		edtIpAddress.setOnEditorActionListener(new OnEditorActionListener()
		{
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
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
		b_up 			= (Button)findViewById(R.id.upBut);
		b_down 			= (Button)findViewById(R.id.downBut);
		b_left 			= (Button)findViewById(R.id.leftBut);
		b_right 		= (Button)findViewById(R.id.rightBut);
		b_zoom_inc 		= (Button)findViewById(R.id.zoomIncBut);
		b_zoom_dec 		= (Button)findViewById(R.id.zoomDecBut);
		b_stop 			= (Button)findViewById(R.id.ptzStop);
		b_abs_pos 		= (Button)findViewById(R.id.AbsPos);

		b_pre_remove 	= (Button)findViewById(R.id.presetRem);
		b_pre_set 		= (Button)findViewById(R.id.presetSet);
		b_pre_goto		= (Button)findViewById(R.id.presetGogo);
		b_pre_set_home 	= (Button)findViewById(R.id.presetSetHome);
		b_pre_goto_home = (Button)findViewById(R.id.presetGotoHome);

		

		

	b_up.setOnClickListener(new View.OnClickListener()
	{
		public void onClick(View v)
		{
			switch(PTZ_MODE)
			{
				case 0 :	
					PTZ_ContinuousMove(/*PanTilt_x=*/0.0f,/*PanTilt_y=*/0.05f);
					break;
				case 1 :	
					PTZ_RelativeMove(/*shift_x=*/0.00f,/*shift_y=*/0.05f,/*speed_x*/ 0.1f,/*speed_y*/ 0.1f);
					break;
			}	
		}
	});
	b_down.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				switch(PTZ_MODE)
				{
					case 0 : 	
						PTZ_ContinuousMove(/*PanTilt_x=*/0.0f,/*PanTilt_y=*/-0.05f);
						break;
					case 1 :	
						PTZ_RelativeMove(/*shift_x=*/0.0f,/*shift_y=*/-0.05f,/*speed_x*/ 0.1f,/*speed_y*/ 0.1f);
						break;
				}	
					

					
			}
		});
	 b_right.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				switch(PTZ_MODE)
				{
					case 0 : 	
						PTZ_ContinuousMove(/*PanTilt_x=*/0.05f,/*PanTilt_y=*/0.0f);
						break;
					case 1 :	
						PTZ_RelativeMove(/*shift_x=*/0.05f,/*shift_y=*/0.00f,/*speed_x*/ 0.1f,/*speed_y*/ 0.1f);
						break;
				}	
			}
		});
	 b_left.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				switch(PTZ_MODE)
				{
					case 0 : 	
						PTZ_ContinuousMove(/*PanTilt_x=*/-0.05f,/*PanTilt_y=*/0.0f);
						break;
					case 1 :	
						PTZ_RelativeMove(/*shift_x=*/-0.05f,/*shift_y=*/0.00f,/*speed_x*/ 0.1f,/*speed_y*/ 0.1f);
						break;
				}	
			}
		});
	 
	 b_zoom_inc.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				PTZ_Zoom(0.01f);	
			}
		});

		b_zoom_dec.setOnClickListener(new View.OnClickListener()
		   {
			   public void onClick(View v)
			   {
				 PTZ_Zoom(-0.01f);	
			   }
		   });

		b_stop.setOnClickListener(new View.OnClickListener()
		   {
			   public void onClick(View v)
			   {
				   // Set true when we want to stop ongoing pan and tilt movements.If PanTilt arguments are not present, this command stops these movements
				   int PanTiltStop = 1;
				   // Set true when we want to stop ongoing zoom movement.If Zoom arguments are not present, this command stops ongoing zoom movement
				   int ZoomStop	   = 1;
				   onvif.PTZStop(PanTiltStop,ZoomStop);
			   }
		   });
	
		b_abs_pos.setOnClickListener(new View.OnClickListener()
		   {
			   public void onClick(View v)
			   	{
					PTZ_AbsoluteMove(/*pos_x=*/0.05f,/*pos_y=*/0.00f,/*speed_x*/ 0.1f,/*speed_y*/ 0.1f);
			    }
		   });

		// ------------------------------------------------- 
		// PRESET options
		// -------------------------------------------------
		b_pre_remove.setOnClickListener(new View.OnClickListener()
		   {
			   public void onClick(View v)
				{

					onvif.PTZPreset(MediaOnvif.OnvifPTZPresetCommands.PRESET_COMMAND_REMOVE,PRESET_ID);
					onvif.PTZPreset(MediaOnvif.OnvifPTZPresetCommands.PRESET_COMMAND_GET_NUM,0);
				}
		   });
		
		b_pre_set.setOnClickListener(new View.OnClickListener()
		   {
			   public void onClick(View v)
			   	{
					onvif.PTZPreset(MediaOnvif.OnvifPTZPresetCommands.PRESET_COMMAND_SET,PRESET_ID);
					onvif.PTZPreset(MediaOnvif.OnvifPTZPresetCommands.PRESET_COMMAND_GET_NUM,0);
			    }
		   });		

		b_pre_goto.setOnClickListener(new View.OnClickListener()
		   {
			   public void onClick(View v) 
			   	{
					onvif.PTZPreset(MediaOnvif.OnvifPTZPresetCommands.PRESET_COMMAND_GOTO,PRESET_ID);
			    }
		   });		

		b_pre_set_home.setOnClickListener(new View.OnClickListener()
		   {
			   public void onClick(View v)
			   	{
					onvif.PTZPreset(MediaOnvif.OnvifPTZPresetCommands.PRESET_COMMAND_SET_HOME,PRESET_ID);
			    }
		   });		

		b_pre_goto_home.setOnClickListener(new View.OnClickListener()
		   {
			   public void onClick(View v)
			   	{
					onvif.PTZPreset(MediaOnvif.OnvifPTZPresetCommands.PRESET_COMMAND_GOTO_HOME,PRESET_ID);
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
//				"rtsp://russiatoday.fms.visionip.tv/rt/Russia_al_yaum_1000k_1/1000k_1",
//						"rtsp://www.tvarm.ru:1935/live/myStream1.sdp",
//						"rtsp://184.72.239.149/vod/mp4:BigBuckBunny_115k.mov",
//						"rtsp://live240.impek.com/brtvhd",
//						"rtmp://121.121.31.106:443/video2/VXG.stream_high"
				String urlHistory[] = {
										"http://admin:1234@10.20.16.80:80/onvif/device_service/"
										};
				//edtIpAddressHistory.clear();
				MainActivity.edtIpAddressAdapter = new ArrayAdapter<String>(MainActivity.this, R.layout.history_item, new ArrayList<String>(edtIpAddressHistory));
				MainActivity.edtIpAddress.setAdapter(MainActivity.edtIpAddressAdapter);
				MainActivity.edtIpAddress.showDropDown();
			}
		});

		btnShot = (Button)findViewById(R.id.button_shot);

		// Array of choices
		btnShot.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				if (player != null&&!btnShot.getText().equals("Get"))
				{
					Log.e(TAG, "getVideoShot()");

	    	    	SharedSettings sett = SharedSettings.getInstance();
					
					//VideoShot frame = player.getVideoShot(200, 200);
					VideoShot frame = player.getVideoShot(-1, -1);
					if (frame == null)
						return;
					
					// get your custom_toast.xml ayout
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
					toastShot.setDuration(Toast.LENGTH_SHORT);
					toastShot.setView(layout);
					toastShot.show();
					
				}
				else
				{
					MediaOnvifConfig conf = new MediaOnvifConfig();
					user_name="";
					password="";
					String url=		edtIpAddress.getText().toString();
					Uri uri = Uri.parse(edtIpAddress.getText().toString()+File.separator);
					String adress=uri.getHost();
					int port=uri.getPort();
					String path=uri.getPath();

					String[] parts=url.split("@");
					if(parts.length>=2) {
						String[] parts1 = parts[0].split("//");
						if(parts1.length>=2) {
							parts1 = parts1[1].split(":");
							if(parts1.length>=2)
							{
								user_name=parts1[0];
								password=parts1[1];
							}
						}

					}


					if(url!=null&&adress!=null&&path!=null&&port>=0) {
						conf.setConnectionUrl(url);
						conf.setconnectionIP_ADDRESS(adress);
						conf.setconnectionTCP_PORT(port);
						conf.setconnectionPath(path);
						conf.setconnectionUser(user_name);
						conf.setconnectionPassword(password);
						onvif.Close();
						// Open Onvif
						if (!edtIpAddressHistory.contains(url))
							edtIpAddressHistory.add(url);
						edtIpAddress1.setText("");
						profileUrls.clear();
						onvif.Open(conf, call);
						playerStatusText.setText("Connecting...");
					}
					else {
						playerStatusText.setText("Invalid url!");
					}
				}
			}   
		});

		btnConnect = (Button)findViewById(R.id.button_connect);
        btnConnect.setOnClickListener(this);
        
        btnRecord = (Button) findViewById(R.id.button_record);
        //btnRecord.setVisibility(View.GONE);
        btnRecord.setOnClickListener( new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				if (!btnShot.getText().equals("Get")) {
					is_record = !is_record;

				/*if(player_record != null){
					if(is_record){
						//start recording
						int record_flags = PlayerRecordFlags.forType(PlayerRecordFlags.PP_RECORD_AUTO_START) | PlayerRecordFlags.forType(PlayerRecordFlags.PP_RECORD_SPLIT_BY_TIME); //1 - auto start
						int rec_split_time = 30;	
						player_record.RecordSetup(getRecordPath(), record_flags, rec_split_time, 0, "");
						player_record.RecordStart();
						player_record.Play();
					}else{
						//stop recording
						player_record.RecordStop();
					}
				}*/

					if (is_record) {
						//start recording
						if (player != null) {
							int record_flags = get_record_flags();
							int rec_split_time = ((record_flags & PlayerRecordFlags.forType(PlayerRecordFlags.PP_RECORD_SPLIT_BY_TIME)) != 0) ? 15 : 0; //15 sec
							player.RecordSetup(getRecordPath(), record_flags, rec_split_time, 0, "");
							player.RecordStart();
						}
					} else {
						//stop recording
						if (player != null) {
							player.RecordStop();
						}
					}

					ImageView ivLed = (ImageView) findViewById(R.id.led);
					if (ivLed != null)
						ivLed.setImageResource((is_record ? R.drawable.led_red : R.drawable.led_green));
					btnRecord.setText(is_record ? "Stop Record" : "Start Record");
				}
				else
				{
					MainActivity.edtIpAddressAdapter = new ArrayAdapter<String>(MainActivity.this, R.layout.history_item, new ArrayList<String>(profileUrls));
					MainActivity.edtIpAddress1.setAdapter(MainActivity.edtIpAddressAdapter);
					MainActivity.edtIpAddress1.showDropDown();
				}
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
        
		playerStatusText.setText("DEMO VERSION");
		setShowControls();
        
    }

    private int[] mColorSwapBuf = null;                        // used by saveFrame()
    public Bitmap getFrameAsBitmap(ByteBuffer frame, int width, int height)
    {
        Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bmp.copyPixelsFromBuffer(frame);
        return bmp;
    }

	int get_record_flags()
	{
		int flags = PlayerRecordFlags.forType(PlayerRecordFlags.PP_RECORD_AUTO_START) | PlayerRecordFlags.forType(PlayerRecordFlags.PP_RECORD_SPLIT_BY_TIME);	// auto start and split by time
		//+ audio only
		//flags |= PlayerRecordFlags.forType(PlayerRecordFlags.PP_RECORD_DISABLE_VIDEO);	
		
		//+ PTS correction
		flags |= PlayerRecordFlags.forType(PlayerRecordFlags.PP_RECORD_PTS_CORRECTION);	
		return flags;	
	}
		
    public void startPlayer(String url)
	{
		SharedSettings.getInstance().loadPrefSettings();
		if (player != null)
		{


			player.getConfig().setConnectionUrl(edtIpAddress.getText().toString());
			if (player.getConfig().getConnectionUrl().isEmpty())
				return;

			if (toastShot != null)
				toastShot.cancel();



			player.Close();
			if (playing)
			{
				setUIDisconnected();
			}
			else
			{
				SharedSettings sett = SharedSettings.getInstance();
				boolean bPort = (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT);
				int aspect = bPort ? 1 : sett.rendererEnableAspectRatio;

				MediaPlayerConfig conf = new MediaPlayerConfig();

				player.setVisibility(View.INVISIBLE);

				conf.setConnectionUrl(url);

				conf.setConnectionNetworkProtocol(sett.connectionProtocol);
				conf.setConnectionDetectionTime(sett.connectionDetectionTime);
				conf.setConnectionBufferingTime(sett.connectionBufferingTime);
				conf.setDecodingType(sett.decoderType);
				conf.setSynchroEnable(sett.synchroEnable); 
				conf.setSynchroNeedDropVideoFrames(sett.synchroNeedDropVideoFrames);
				conf.setEnableAspectRatio(1);
				conf.setDataReceiveTimeout(30000);

				//record config
				if(is_record){
					int record_flags = get_record_flags();
					int rec_split_time = ( (record_flags & PlayerRecordFlags.forType(PlayerRecordFlags.PP_RECORD_SPLIT_BY_TIME)) != 0)? 15:0; //15 sec
					conf.setRecordPath(getRecordPath());
					conf.setRecordFlags(record_flags);
					conf.setRecordSplitTime(rec_split_time);
					conf.setRecordSplitSize(0);
				}else{
					conf.setRecordPath("");
					conf.setRecordFlags(0);
					conf.setRecordSplitTime(0);
					conf.setRecordSplitSize(0);
				}

				Log.v(TAG, "conf record="+is_record+" getRecordFlags()="+conf.getRecordFlags());
				Log.e(TAG, "startPlayer url="+conf.getConnectionUrl());
				// Open Player
				player.Open(conf, mthis);

				btnConnect.setText("Disconnect");
				conf.setMode(PlayerModes.PP_MODE_RECORD);

				playing = true;
			}
		}
	}
    public void onClick(View v)
	{
         if(!edtIpAddress1.getText().toString().equals(""))
		startPlayer(edtIpAddress1.getText().toString());
		else
			 playerStatusText.setText("URL is NULL!");

    }
 
	protected void onPause()
	{
		Log.e(TAG, "onPause()");
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
		Log.e(TAG, "onResume()");
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
		if (toastShot != null)
			toastShot.cancel();
		//onvif.Close();
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
						HashSet<String> tempHistory = new HashSet<String>();
//						tempHistory.add("rtsp://184.72.239.149/vod/mp4:BigBuckBunny_115k.mov");
//						tempHistory.add("http://hls.cn.ru/streaming/2x2tv/tvrec/playlist.m3u8");
//						tempHistory.add("rtsp://rtmp.infomaniak.ch/livecast/latele");
//						tempHistory.add("rtmp://121.121.31.106:443/video2/VXG.stream_high");
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
		btnRecord.setText("Start Record");
		btnShot.setText("Shot");
		btnShot.setVisibility(View.VISIBLE);
		edtIpAddress.setVisibility(View.GONE);
		btnHistory.setVisibility(View.GONE);
		btnConnect.setVisibility(View.GONE);
		b_up.setVisibility(View.VISIBLE);
		b_down.setVisibility(View.VISIBLE);
	}

	protected void setShowControls()
	{
		setTitle(R.string.app_name);

		//btnShot.setVisibility(View.GONE);
		btnRecord.setText("Channels");
		btnShot.setText("Get");
		edtIpAddress.setVisibility(View.VISIBLE);
		btnHistory.setVisibility(View.VISIBLE);
		btnConnect.setVisibility(View.VISIBLE);
	}

	private void showStatusView() 
	{
		player.setVisibility(View.INVISIBLE);
		playerHwStatus.setVisibility(View.INVISIBLE);
		//player.setAlpha(0.0f);
		playerStatus.setVisibility(View.VISIBLE);
		
	}
	
	private void showVideoView() 
	{
        playerStatus.setVisibility(View.INVISIBLE);
 		player.setVisibility(View.VISIBLE);
		playerHwStatus.setVisibility(View.VISIBLE);
		
		setTitle("");
	}
	
}
