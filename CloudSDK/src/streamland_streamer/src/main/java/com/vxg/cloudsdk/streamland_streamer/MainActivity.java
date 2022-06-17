/*
 *
  * Copyright (c) 2011-2017 VXG Inc.
 *
 */


package com.vxg.cloudsdk.streamland_streamer;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.MulticastLock;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
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
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.security.ProviderInstaller;
import com.vxg.cloud.core.CloudHelpers;
import com.vxg.cloudsdk.CloudSDK;
import com.vxg.cloudsdk.CloudStreamerSDK;
import com.vxg.cloudsdk.Interfaces.ICloudStreamerCallback;
import com.vxg.cloudsdk.Interfaces.ICompletionCallback;
import com.vxg.cloudsdk.Objects.CloudStreamerConfig;
import com.vxg.cloudsdk.Objects.CloudTimelineSegment;
import com.vxg.cloudsdk.Objects.CloudTimelineThumbnail;

import java.nio.ByteBuffer;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import javax.net.ssl.SSLContext;

import veg.mediacapture.sdk.MediaCapture;
import veg.mediacapture.sdk.MediaCaptureCallback;
import veg.mediacapture.sdk.MediaCaptureConfig;

public class MainActivity extends Activity implements OnClickListener, MediaCaptureCallback
{

	private static final String TAG 	 = "streamland_streamer";

	public  static AutoCompleteTextView	edtId;
	private Button						btnConnect;

	CloudStreamerSDK mCloudStreamer;
	boolean     mStreamerStarted = false;
	
	MediaCapture capturer;
	boolean misSurfaceCreated = false;
	private ImageView 			led;
	private TextView 			captureStatusText = null;
	private TextView 			captureStatusText2 = null;
	private TextView			captureStatusStat = null;
	String rtmp_url = "";
	private static final boolean USE_PORTRAIT_MODE = false;
	boolean is_demo_limit = false;
	String  demo_string = "Streaming stopped. DEMO VERSION limitation";

	private MulticastLock multicastLock = null;
	private PowerManager.WakeLock mWakeLock;

	final boolean TEST_SEGMENT_UPLOAD_ASYNC = false;
	final boolean TEST_SEGMENT_UPLOAD_SYNC = false;

	final boolean TEST_THUMBNAIL_UPLOAD_ASYNC = false;
	final boolean TEST_THUMBNAIL_UPLOAD_SYNC = false;

	//SET Channel
	private long expireTimout = 2 * 60 * 1000;
	private String msPreviousAccessToken = "";
	private String msAccessToken = "";

	private SharedPreferences sharedPref = null;

	public boolean isRec(){
		return ( capturer != null && capturer.getState() == MediaCapture.CaptureState.Started );
	}

	// Event handler
	private Handler handler = new Handler()
	{
		@Override
		public void handleMessage(Message msg)
		{
			MediaCapture.CaptureNotifyCodes status = (MediaCapture.CaptureNotifyCodes) msg.obj;
			//Log.i(TAG, "=Status handleMessage status="+status);

			String strText = null;
			int rtmp_status = capturer.getRTMPStatus();
			if (rtmp_status == (-999)) {
				is_demo_limit = true;
			}

			switch (status)
			{
				case CAP_OPENED:
					strText = "Opened";
					break;
				case CAP_SURFACE_CREATED:
					strText = "Camera surface created surfaceView="+capturer.getSurfaceView();
					misSurfaceCreated = true;
					break;
				case CAP_SURFACE_DESTROYED:
					strText = "Camera surface destroyed";
					misSurfaceCreated = false;
					break;
				case CAP_STARTED:
					strText = "Started";
					break;
				case CAP_STOPPED:
					strText = "Stopped";
					break;
				case CAP_CLOSED:
					strText = "Closed";
					break;
				case CAP_ERROR:
					strText = "Error";
					//break;
				case CAP_TIME:
					if(isRec()) {
						int dur = (int) (long) capturer.getDuration() / 1000;
						int v_cnt = capturer.getVideoPackets();
						int a_cnt = capturer.getAudioPackets();
						long v_pts = capturer.getLastVideoPTS();
						long a_pts = capturer.getLastAudioPTS();
						int nreconnects = capturer.getStatReconnectCount();

						String sss = "";
						String sss2 = "";
						int min = dur / 60;
						int sec = dur - (min * 60);
						sss = String.format("%02d:%02d", min, sec);
						if (rtmp_status == (-999)) {
							led.setImageResource(R.drawable.led_green);
							//mbuttonRec.setImageResource(R.drawable.ic_fiber_manual_record_red);
							sss = demo_string;
							stopStreamer();
						} else if (rtmp_status != (-1)) {

							if (capturer.USE_RTSP_SERVER) {
								sss += ". RTSP ON (" + capturer.getRTSPAddr() + ")";
								sss2 += "v:" + v_cnt + " a:" + a_cnt + " rcc:" + nreconnects;
							} else {
								sss += ". RTMP " + ((rtmp_status == 0) ? "ON ( " + rtmp_url + " )" : "Err:" + rtmp_status);
								//sss += ". RTMP "+ ((rtmp_status == 0)?"ON ":"Err:"+rtmp_status);
								if (rtmp_status == (-5)) {
									sss += " Server not connected ( " + rtmp_url + " )";
								} else if (rtmp_status == (-12)) {
									sss += " Out of memory";
								}
								sss2 += "v:" + v_cnt + " a:" + a_cnt + " rcc:" + nreconnects;
								sss2 += "\nv_pts: " + v_pts + " a_pts: " + a_pts + " delta: " + (v_pts - a_pts);
							}

						}

						captureStatusText.setText(sss);
						captureStatusStat.setText(sss2);
					}
					break;

				default:
					break;
			}
			if(strText != null){
				Log.i(TAG, "=Status handleMessage str="+strText);
			}
		}
	};

	// All event are sent to event handlers
	@Override
	public int OnCaptureStatus(int arg) {
		MediaCapture.CaptureNotifyCodes status = MediaCapture.CaptureNotifyCodes.forValue(arg);
		if (handler == null || status == null)
			return 0;

		//Log.v(TAG, "=OnCaptureStatus status=" + arg);
		switch (MediaCapture.CaptureNotifyCodes.forValue(arg))
		{
			default:
				Message msg = new Message();
				msg.obj = status;
				handler.sendMessage(msg);
		}

		return 0;
	}

	// callback from Native Capturer
	@Override
	public int OnCaptureReceiveData(ByteBuffer buffer, int type, int size,
									long pts) {

		return 0;
	}

	@Override
    public void onCreate(Bundle savedInstanceState)
	{
		setTitle(R.string.app_name);
		super.onCreate(savedInstanceState);

		sharedPref = getPreferences(Context.MODE_PRIVATE);

		PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
		mWakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, "com.vxg.cloudsdk.streamland_streamer");

		WifiManager wifi = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
		multicastLock = wifi.createMulticastLock("multicastLock");
		multicastLock.setReferenceCounted(true);
		multicastLock.acquire();

		getWindow().requestFeature(Window.FEATURE_PROGRESS);
		getWindow().setFeatureInt(Window.FEATURE_PROGRESS, Window.PROGRESS_VISIBILITY_ON);

		setContentView(R.layout.activity_streamer);

		if(USE_PORTRAIT_MODE){
			//set portrait mode
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		}else{
			//set landscape mode
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		}


		initializeSSLContext(this);
		CloudSDK.setContext(this);
		CloudSDK.setLogEnable(true);
		CloudSDK.setLogLevel(2);
		CloudSDK.requestPermissions(new String[]{
				Manifest.permission.CAMERA,
				Manifest.permission.RECORD_AUDIO});
		//Toast.makeText(getApplicationContext(), "CloudSDK ver="+CloudSDK.getLibVersion(), Toast.LENGTH_LONG).show();

		led = (ImageView)findViewById(R.id.led);
		led.setImageResource(R.drawable.led_green);

		captureStatusText = (TextView)findViewById(R.id.statusRec);
		captureStatusStat = (TextView)findViewById(R.id.statusStat);
		captureStatusStat.setText("");

		captureStatusText2 = (TextView)findViewById(R.id.statusRec2);
		captureStatusText2.setText("");

		capturer = (MediaCapture)findViewById(R.id.captureView);

		if(USE_PORTRAIT_MODE){
			capturer.getConfig().setVideoOrientation(90); //portrait
		}else{
			capturer.getConfig().setVideoOrientation(0); //landscape
		}

		//audio
		capturer.getConfig().setAudioFormat(MediaCaptureConfig.TYPE_AUDIO_AAC);
		capturer.getConfig().setAudioBitrate(128);
		capturer.getConfig().setAudioSamplingRate(44100);
		capturer.getConfig().setAudioChannels(2);

		//video
		capturer.getConfig().setSecVideoFramerate(30);
		capturer.getConfig().setVideoBitrate(800);
		capturer.getConfig().setVideoResolution(MediaCaptureConfig.CaptureVideoResolution.VR_640x480);

		capturer.Open(null, this);

		mCloudStreamer = new CloudStreamerSDK(new ICloudStreamerCallback() {
			@Override
			public void onConfigUpdated() {
				Log.v(TAG, "=>onConfigUpdated");
				String config = mCloudStreamer.getConfig();
				Log.v(TAG, "TestConfig: save config: " + config);
				SharedPreferences.Editor editor = sharedPref.edit();
				editor.putString("token", msAccessToken);
				editor.putString("config", config);
				editor.putLong("time", System.currentTimeMillis());
				editor.apply();
			}

			@Override
			public void onStarted(String surl) {
				Log.v(TAG, "=>onStarted surl="+surl);
				rtmp_url = surl;

				capturer.getConfig().setUrl(surl);
				capturer.StartStreaming();
			}

			@Override
			public void onStopped() {
				Log.v(TAG, "<=onStopeed");
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						if(is_demo_limit){
							stopStreamer();
						}else {
							captureStatusText.setText(mStreamerStarted ? "No Clients" : "");
							captureStatusStat.setText("");
						}
					}
				});
			}

			@Override
			public void onError(final int result) {
				Log.v(TAG, "=onError");

				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						Toast.makeText(getApplicationContext(), "Error="+result, Toast.LENGTH_LONG).show();

						capturer.StopStreaming();

						//channel will be auto restored on network on
						//if(mCloudStreamer.isNetworkOn())
						//	stopStreamer();
					}
				});
			}

			@Override
			public void onCameraConnected() {

				Log.v(TAG, "=>onCameraConnected");

				if(TEST_SEGMENT_UPLOAD_SYNC || TEST_SEGMENT_UPLOAD_ASYNC) {
					//test segment upload
					final long minute = 60 * 1000;
					final long time_cur = CloudHelpers.currentTimestampUTC();
					final String filename1 = "/storage/emulated/0/DCIM/RecordsMediaStreamer/_20180411_113943_0.mp4";
					final String filename2 = "/storage/emulated/0/DCIM/RecordsMediaStreamer/_20180411_114014_1.mp4";

					//async test
					if(TEST_SEGMENT_UPLOAD_ASYNC) {
						mCloudStreamer.putTimelineSegment(time_cur - 5 * minute, time_cur - 4 * minute - minute / 2, filename1,
								new ICompletionCallback() {
									@Override
									public int onComplete(Object o_result, final int result) {
										runOnUiThread(new Runnable() {
										@Override
										public void run() {
											Toast.makeText(getApplicationContext(), "Upload1 ret="+result, Toast.LENGTH_LONG).show();
										}
										});
                           				return 0;
									}
								});
						mCloudStreamer.putTimelineSegment(time_cur - 4 * minute - minute / 2, time_cur - 4 * minute, filename2,
								new ICompletionCallback() {
									@Override
									public int onComplete(Object o_result, final int result) {
										runOnUiThread(new Runnable() {
										@Override
										public void run() {
											Toast.makeText(getApplicationContext(), "Upload2 ret="+result, Toast.LENGTH_LONG).show();
										}
										});

										ArrayList<CloudTimelineSegment> segments = mCloudStreamer.getTimelineSegmentsSync(time_cur - 5 * minute, time_cur);
					    				return 0;
									}
								});
					}

					//sync test
					if(TEST_SEGMENT_UPLOAD_SYNC) {
						Thread t = new Thread(new Runnable() {
							@Override
							public void run() {
								int ret1 = mCloudStreamer.putTimelineSegmentSync(time_cur - 5 * minute, time_cur - 4 * minute - minute / 2, filename1);
								int ret2 = mCloudStreamer.putTimelineSegmentSync(time_cur - 4 * minute - minute / 2, time_cur - 4 * minute, filename2);
							}
						});
						t.start();
					}
				}

				if(TEST_THUMBNAIL_UPLOAD_SYNC || TEST_THUMBNAIL_UPLOAD_ASYNC) {
					//test thumbnail upload
					final long time_cur = CloudHelpers.currentTimestampUTC();
					final String filename1 = "/storage/emulated/0/DCIM/Camera/IMG_20180411_152023.jpg";

					//async test
					if(TEST_THUMBNAIL_UPLOAD_ASYNC) {
						mCloudStreamer.putTimelineThumbnail(time_cur, filename1,
								new ICompletionCallback() {
									@Override
									public int onComplete(Object o_result, final int result) {
										runOnUiThread(new Runnable() {
											@Override
											public void run() {
												Toast.makeText(getApplicationContext(), "Upload Thumbnail ret="+result, Toast.LENGTH_LONG).show();
											}
										});

										ArrayList<CloudTimelineThumbnail> thumbnails = mCloudStreamer.getTimelineThumbnailsSync(time_cur, time_cur+1000);

										return 0;
									}
								});
					}

					//sync test
					if(TEST_THUMBNAIL_UPLOAD_SYNC) {
						Thread t = new Thread(new Runnable() {
							@Override
							public void run() {
								int ret1 = mCloudStreamer.putTimelineThumbnailSync(time_cur, filename1);
							}
						});
						t.start();
					}
				}

			}
		});
		mCloudStreamer.getStreamerConfig().useProtocolDefaults(CloudStreamerConfig.ProtocolDefaults.SECURE);
		long time = sharedPref.getLong("time", 0);
		msPreviousAccessToken = sharedPref.getString("token", "");
		String config = sharedPref.getString("config", "");
		long delta = (System.currentTimeMillis() - time);
		if (!config.isEmpty() && !msAccessToken.isEmpty() &&
					msAccessToken.equals(msPreviousAccessToken) && (delta < expireTimout)) {
			Log.v(TAG, "TestConfig: restore config: " + config + ", delta: " + delta + ", token: " + msPreviousAccessToken);
			mCloudStreamer.setConfig(config);
		}
		mCloudStreamer.setSource(msAccessToken);

		this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		edtId = (AutoCompleteTextView)findViewById(R.id.edit_id);
		edtId.setText(msAccessToken ==null?"": msAccessToken);
		//edtId.setVisibility(View.INVISIBLE);

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
		btnConnect.setText("Start");

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
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
	    for(int i=0; i< permissions.length; i++){
	        String p = permissions[i];
	        int g = grantResults[i];
	        if(p.equals(Manifest.permission.CAMERA)){
	        	if(g != PackageManager.PERMISSION_GRANTED){
	        		Log.e(TAG, "=onRequestPermissionsResult Manifest.permission.CAMERA not GRANTED");
				}else{
	        		if(capturer != null) {
						capturer.Close();
						capturer.Open(null, this);
					}
				}
			}
        }
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
		if(mStreamerStarted){
			stopStreamer();
			return;
		}

		if(!check_access_token())
			return ;

		startStreamer();

    }

	void startStreamer(){
		is_demo_limit = false;

		if(msAccessToken == null || msAccessToken.length()<1)
			return;

		if(!misSurfaceCreated){
			Toast.makeText(getApplicationContext(), "Camera not ready yet", Toast.LENGTH_LONG).show();
			return;
		}

		mCloudStreamer.setSource(msAccessToken);
		mCloudStreamer.Start();

		btnConnect.setText("Stop");
		captureStatusText.setText("No clients");
		captureStatusStat.setText("");
		mStreamerStarted = true;
	}

	void stopStreamer(){
		if(!mStreamerStarted) {
			return;
		}

		capturer.StopStreaming();
		mCloudStreamer.Stop();
		btnConnect.setText("Start");
		mStreamerStarted = false;

		captureStatusText.setText(is_demo_limit?demo_string:"");
		captureStatusStat.setText("");
	}


	protected void onPause()
	{
		Log.e(TAG, "onPause()");
		super.onPause();
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

		mWakeLock.acquire();
	}

  	@Override
	protected void onStop() 
  	{
  		Log.e(TAG, "onStop()");
		super.onStop();

		stopStreamer();

		if (mWakeLock.isHeld())
			mWakeLock.release();
	}

  	@Override
  	protected void onDestroy() 
  	{
  		Log.e(TAG, "onDestroy()");

		mCloudStreamer.Stop();
		capturer.StopStreaming();
		capturer.Close();
		
		System.gc();
		
		if (multicastLock != null) {
		    multicastLock.release();
		    multicastLock = null;
		}		
		super.onDestroy();
   	}	

	protected void setUIDisconnected()
	{
		setTitle(R.string.app_name);
		btnConnect.setText("Connect");
	}

    static boolean is_inited = false;
    public static void initializeSSLContext(final Context context){

        if(is_inited)
            return;

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
        	is_inited = true;
		}

        Log.v(TAG, "=>initializeSSLContext context="+context);
        if(context == null) {
            Log.e(TAG, "<=initializeSSLContext err context==null!");
            return;
        }

        try {
            SSLContext.getInstance("TLSv1.2");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        if(!is_inited) {
            ((Activity) context).runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    try {
                        ProviderInstaller.installIfNeeded(context.getApplicationContext());
                    } catch (GooglePlayServicesRepairableException e) {
                        e.printStackTrace();
                    } catch (GooglePlayServicesNotAvailableException e) {
                        e.printStackTrace();
                    }

                    is_inited = true;
                }
            });
        }

        while (!is_inited){
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        Log.v(TAG, "<=initializeSSLContext");

    }

}
