package veg.mediacapture.sdk.test;

import android.Manifest;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.MulticastLock;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;

import veg.mediacapture.sdk.MediaCapture;
import veg.mediacapture.sdk.MediaCapture.CaptureNotifyCodes;
import veg.mediacapture.sdk.MediaCapture.CaptureState;
import veg.mediacapture.sdk.MediaCapture.PlayerRecordFlags;
import veg.mediacapture.sdk.MediaCapture.PlayerRecordStat;
import veg.mediacapture.sdk.MediaCaptureCallback;
import veg.mediacapture.sdk.MediaCaptureConfig;
import veg.mediacapture.sdk.MediaCaptureConfig.CaptureModes;
import veg.mediacapture.sdk.MediaCaptureConfig.CaptureVideoResolution;
import veg.mediacapture.sdk.test.demo.R;

//import android.support.v4.app.ActivityCompat;
//import android.support.v4.content.ContextCompat;


public class MainActivity extends Activity implements MediaCaptureCallback
{
    private static final String TAG 	 = "MediaCaptureTest";
    
    private static final boolean TEST_SEPARATED_CONTROL = false;
	private static final boolean USE_PORTRAIT_MODE = false;

	private static final boolean GET_JPEG_ON_START = false;
    
    private SharedPreferences settings=null;
    
    private MediaCapture 				capturer = null;
    private boolean 					misAudioEnabled=true;
    private boolean						misSurfaceCreated = false;
	private boolean						USE_RTSP_G711=false;
    
    private boolean 					misRecfileEnabled=true;
    private boolean 					misTranscodingEnabled=true;

	private ImageView led;
    private TextView 					captureStatusText = null;
    private TextView 					captureStatusText2 = null;
    private TextView					captureStatusStat = null;
    private ImageButton					mbuttonRec = null;
    private ImageButton 				mbuttonSettings = null;
    
    String rtmp_url = "";
    
    private MulticastLock multicastLock = null;
    private PowerManager.WakeLock mWakeLock;
    
	private CaptureState capture_state = CaptureState.Closed; 
	private int mOldMsg = 0;

	private Toast toastShot = null;
	
	public static MainActivity sMainActivity;
	public MediaCaptureConfig mConfig;

	boolean mJPEG_ready = false;
	String  mJPEG_file = "";
	
	// Event handler
	
	private Handler handler = new Handler() 
    {
		@Override
	    public void handleMessage(Message msg) 
	    {
	    	CaptureNotifyCodes status = (CaptureNotifyCodes) msg.obj;
    		//Log.i(TAG, "=Status handleMessage status="+status);

    		String strText = null;

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
	        		
					 if(isRec()){
			        	int rtmp_status = capturer.getStreamStatus();
			        	int dur = (int)(long)capturer.getDuration()/1000;
			        	int v_cnt = capturer.getVideoPackets();
			        	int a_cnt = capturer.getAudioPackets();
			        	long v_pts = capturer.getLastVideoPTS();
			        	long a_pts = capturer.getLastAudioPTS();
			        	int nreconnects = capturer.getStatReconnectCount();
						long actual_bitrate = capturer.getPropLong(PlayerRecordStat.forType(PlayerRecordStat.PP_RECORD_STAT_ACTUAL_BITRATE));
						long actual_framerate = capturer.getPropLong(PlayerRecordStat.forType(PlayerRecordStat.PP_RECORD_STAT_ACTUAL_FRAMERATE));

						 String sss = "";
						 String sss2 = "";
						 int min = dur/60;
						 int sec = dur- (min*60);
						 sss = String.format("%02d:%02d", min, sec);
						 if(!misAudioEnabled)
							 sss += ". Audio OFF";
						 if(rtmp_status == (-999)){
							 sss = "Streaming stopped. DEMO VERSION limitation";
							 capturer.Stop();
							 led.setImageResource(R.drawable.led_green);
        	        		 mbuttonRec.setImageResource(R.drawable.ic_fiber_manual_record_red);
						 }else
						 if(rtmp_status != (-1)){

							 if(capturer.USE_RTSP_SERVER){
							 	sss += ". RTSP ON ("+ capturer.getRTSPAddr()+")";
								sss2 += "v:"+v_cnt+" a:"+a_cnt+" rcc:"+nreconnects;
							 }else{
							 sss += ". RTMP "+ ((rtmp_status == 0)?"ON ( "+rtmp_url+" )":"Err:"+rtmp_status);
							 //sss += ". RTMP "+ ((rtmp_status == 0)?"ON ":"Err:"+rtmp_status);
							 if(rtmp_status == (-5)){
								 sss += " Server not connected ( "+rtmp_url+" )";
							 }else if(rtmp_status == (-12)){
								 sss += " Out of memory";
							 }
							 sss2 += "v:"+v_cnt+" a:"+a_cnt+" rcc:"+nreconnects+"  "+actual_bitrate+"kbps : "+actual_framerate+"fps";
							 sss2 += "\nv_pts: "+v_pts+" a_pts: "+a_pts+" delta: "+(v_pts-a_pts);
							 }
							 
						 }else{
							// rtmp_status == (-1)
							 sss += ". Connecting ...";
						 }

						 String sss3 = "";
						 int rec_status = capturer.getRECStatus();
						 if(rec_status != -1){
						 	if(rec_status == (-999)){
								sss = "Streaming stopped. DEMO VERSION limitation";
								capturer.Stop();
								led.setImageResource(R.drawable.led_green);
								mbuttonRec.setImageResource(R.drawable.ic_fiber_manual_record_red);
						 	}else
						 	if(rec_status != 0 && rec_status != (-999)){
								sss3 += "REC Err:"+rec_status;
						 	}else
						 		sss3 += "REC ON. "+capturer.getPropString(PlayerRecordStat.forType(PlayerRecordStat.PP_RECORD_STAT_FILE_NAME));
						 }
						 
						 captureStatusText.setText(sss);
						 captureStatusStat.setText(sss2);
						 captureStatusText2.setText(sss3);
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
		CaptureNotifyCodes status = CaptureNotifyCodes.forValue(arg);
		if (handler == null || status == null)
			return 0;
		
		//Log.v(TAG, "=OnCaptureStatus status=" + arg);
	    switch (CaptureNotifyCodes.forValue(arg)) 
	    {
	        default:     
				Message msg = new Message();
				msg.obj = status;
				handler.removeMessages(mOldMsg);
				mOldMsg = msg.what;
				handler.sendMessage(msg);
	    }
	    
		return 0;
	}

	private void StartJPEG(){
		mJPEG_ready = false;
		runOnUiThread(new Runnable()
        {
           @Override
           public void run(){
			   capturer.StartTranscoding();
           }
        });
	}

	private void StopJPEG(){
		mJPEG_ready = true;
		runOnUiThread(new Runnable()
        {
           @Override
           public void run(){
			   File filePreview = new File(mJPEG_file);
			   if(filePreview.exists()){
				   Toast.makeText(MainActivity.sMainActivity, "JPEG file ready="+filePreview.getAbsolutePath(), Toast.LENGTH_LONG).show();
			   }
			   capturer.StopTranscoding();
           }
        });
	}

	// callback from Native Capturer 
	@Override
	public int OnCaptureReceiveData(ByteBuffer buffer, int type, int size,
			long pts) {
		
		Log.v(TAG, "=OnCaptureReceiveData buffer="+buffer+" type="+type+" size="+size+" pts="+pts);

		
        if (mJPEG_ready) {
            return 0;
        }

        if (buffer == null){
            Log.e(TAG, "=OnCaptureReceiveData, buffer is null");
            return 0;
        }

        if(type != 0){ // not video frame
            Log.e(TAG, "=OnCaptureReceiveData, it's not video frame");
            return 0;
        }

		String spath = getRecordPath();
        if(spath == null){
            Log.e(TAG, "=OnCaptureReceiveData spath is null");
            StopJPEG();
            return 0;
        }

		mJPEG_file = getRecordPath()+"/preview_"+pts+".jpg";
        File filePreview = new File(getRecordPath(), "preview_"+pts+".jpg");
        Log.e(TAG, "=OnCaptureReceiveData, filePreview " + filePreview.getAbsolutePath());
        if(filePreview.exists()){
            //Log.e(TAG, "=OnCaptureReceiveData, preview already exists");
            //StopTranscoding();
            //return 0;
            filePreview.delete();
        }

		int width = capturer.getConfig().getTransWidth(); //320;
		int height = capturer.getConfig().getTransHeight(); //240;
        Log.v(TAG, "=OnCaptureReceiveData, buffer="+buffer+" type="+type+" size="+size+" pts="+pts);
        Log.i(TAG, "=OnCaptureReceiveData, Send image buffer.capacity() " + buffer.capacity() );
        Log.i(TAG, "=OnCaptureReceiveData, Send image buffer.capacity() expected " + (width* height*4) );
        Log.i(TAG, "=OnCaptureReceiveData, Image width " + width);
        Log.i(TAG, "=OnCaptureReceiveData, Image height " + height);

        // Prepare image
        /*try {
            Bitmap bm = Bitmap.createBitmap(
                    width,
                    height,
                    Bitmap.Config.ARGB_8888
            );
            buffer.rewind();
            bm.copyPixelsFromBuffer(buffer);

            ByteArrayOutputStream fOut = new ByteArrayOutputStream();
            bm.compress(Bitmap.CompressFormat.JPEG, 100, fOut);

            FileOutputStream filePreviewOutputStream = new FileOutputStream(filePreview);
            filePreviewOutputStream.write(fOut.toByteArray());
            filePreviewOutputStream.flush();
            filePreviewOutputStream.close();
            fOut.flush();
            fOut.close();
            File filePreviewCrop = new File(spath, "preview.jpg");
			//mJPEG_ready = true;

        } catch (IOException e) {
            Log.e(TAG, "=OnCaptureReceiveData ", e);
        }*/
		BufferedOutputStream bos = null;
		try {
			bos = new BufferedOutputStream(new FileOutputStream(filePreview));
			Bitmap bmp0 = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
			bmp0.copyPixelsFromBuffer(buffer);

			Matrix matrix = new Matrix();
			matrix.preScale(-1, 1);
			matrix.preRotate(180);
			//matrix.postScale(width, height);
			//matrix.postRotate(180);
			Bitmap bmp = Bitmap.createBitmap(bmp0, 0, 0,width, height, matrix, true);
			
			bmp.compress(Bitmap.CompressFormat.JPEG, 100, bos);
			bmp0.recycle();
			bmp.recycle();
			if (bos != null) bos.close();
		} catch (IOException e) {
			Log.e(TAG, "=OnCaptureReceiveData ", e);
		}
			

        Log.e(TAG, "=OnCaptureReceiveData end ");

		StopJPEG();
		return 0;
	}

	private boolean opened = false;
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != MediaCapture.PERMISSION_CODE) {
            //Toast.makeText(this, "Unknown request code: " + requestCode, Toast.LENGTH_SHORT).show();
            return;
        }
        if (resultCode != RESULT_OK) {
            Toast.makeText(this, "Screen Cast Permission Denied", Toast.LENGTH_SHORT).show();
            return;
        }
        Log.i(TAG, "Get media projection with the new permission");

		if (!opened) {
			capturer.SetPermissionRequestResults(resultCode, data);
			capturer.Open(null, this);
			opened = true;
		}
    }

/*	private void requestPermissions(Context context, String[] permissions)
	{
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			if(context == null){
				Log.e(TAG, "=requestPermissions, context == null");
				return;
			}
			boolean needRequest = false;
			for(String p : permissions){
				if(ContextCompat.checkSelfPermission(context, p) != PackageManager.PERMISSION_GRANTED){
					needRequest = true;
					break;
				}
			}
			if(needRequest){
				ActivityCompat.requestPermissions((Activity)context, permissions, 1);
			}
		}
		return;
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
        }*/


    @Override
	public void onCreate(Bundle savedInstanceState)
	{
		//MediaCapture.DISABLE_CAMERA = true;
		String  strUrl;

		setTitle(R.string.app_name);

		super.onCreate(savedInstanceState);

		sMainActivity = this;

		//get library version
		Log.v(TAG, "=>onCreate MediaCapture::getLibVersion()="+MediaCapture.getLibVersion());

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
				checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED ||
				checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
				checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
				) {
				ActivityCompat.requestPermissions(this, new String[]{
					Manifest.permission.CAMERA, 
					Manifest.permission.RECORD_AUDIO, 
					Manifest.permission.READ_EXTERNAL_STORAGE, 
					Manifest.permission.WRITE_EXTERNAL_STORAGE
					}, 1);
            }
		}


		// Prevents the phone to go to sleep mode
		PowerManager pm = (PowerManager) getApplicationContext().getSystemService(Context.POWER_SERVICE);
		mWakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, "veg.mediacapture.sdk.test.mediastream");


		WifiManager wifi = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
		multicastLock = wifi.createMulticastLock("multicastLock");
		multicastLock.setReferenceCounted(true);
		multicastLock.acquire();

		getWindow().requestFeature(Window.FEATURE_PROGRESS);
		getWindow().setFeatureInt(Window.FEATURE_PROGRESS, Window.PROGRESS_VISIBILITY_ON);

		settings = PreferenceManager.getDefaultSharedPreferences(this);
		int intro_needed = settings.getInt("intro", 1);
		if(intro_needed == 1){
			//String androidID = android.provider.Settings.Secure.getString(getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
			Editor ed = settings.edit();
			String surlr = settings.getString("urlrtmp", "rtmp://54.173.34.172:1937/publish_demo/");
			//surlr += androidID;
			ed.putString("urlrtmp", surlr);
			ed.putString("urlch", "0");
			ed.putString("login", "demo");
			//ed.putString("urlpasscode", "0000");
			ed.putInt("intro", 0);
			ed.apply();
		}
		intro_needed = 0;

		setContentView(R.layout.main);

		if(USE_PORTRAIT_MODE){
			//set portrait mode
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		}else{
			//set landscape mode
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		}

		ActionBar bar = getActionBar();
		if(bar != null)
			bar.hide();

		led = (ImageView)findViewById(R.id.led);
		led.setImageResource(R.drawable.led_green);

		captureStatusText = (TextView)findViewById(R.id.statusRec);
		captureStatusStat = (TextView)findViewById(R.id.statusStat);
		captureStatusStat.setText("");

		captureStatusText2 = (TextView)findViewById(R.id.statusRec2);
		captureStatusText2.setText("");

		/*requestPermissions(this, new String[]{
				Manifest.permission.CAMERA,
				Manifest.permission.RECORD_AUDIO});*/

		capturer = (MediaCapture)findViewById(R.id.captureView);

		if(false){
			//dynamic creation sample code
			capturer = new MediaCapture(this, null);
			Log.i(TAG, "=onCreate capturer="+capturer);

			FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(250,250, Gravity.CENTER);
			capturer.setLayoutParams(params);

			//
			// Add Capture Instance to layout
			FrameLayout lp = (FrameLayout)findViewById(R.id.captureView);
			lp.addView(capturer);
		}

		load_config();

		capturer.RequestPermission(this, mConfig.getCaptureSource());

		if (mConfig.getCaptureSource() != MediaCaptureConfig.CaptureSources.PP_MODE_VIRTUAL_DISPLAY.val())
			capturer.Open(null, this);

        mbuttonSettings = (ImageButton) findViewById(R.id.imageButtonMenu);
		mbuttonSettings.setSoundEffectsEnabled(false);
		mbuttonSettings.setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						Intent intent;
						if(isRec()){
							Toast.makeText(MainActivity.sMainActivity,"Press 'Stop Streaming' button  first", Toast.LENGTH_LONG).show();
						}else{
							// Starts QualityListActivity where user can change the streaming quality
							intent = new Intent(MainActivity.sMainActivity.getBaseContext(),SettingsActivity.class);
							startActivity(intent);
							finish();
						}
					}
				}
			);

		mbuttonRec = (ImageButton) findViewById(R.id.button_capture);
		mbuttonRec.setSoundEffectsEnabled(false);
		mbuttonRec.setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						if(capturer == null)
							return;
						if( !isRec() ){

							String sRecStatus = misAudioEnabled?"00:00":"00:00. Audio OFF";
							captureStatusText.setText(sRecStatus);
							captureStatusStat.setText("");
							led.setImageResource(R.drawable.led_red);

							if(!TEST_SEPARATED_CONTROL){
								//all start
								capturer.Start();

								//test
								//capturer.StopStreaming();
								mbuttonRec.setImageResource(R.drawable.ic_stop);

							}else{
								//induvidual start test
								capturer.StartStreaming();
								mbuttonRec.setImageResource(R.drawable.ic_stop);

								//start postponed rec
								new Handler().postDelayed(new Runnable()
								{
										@Override
									public void run()
									  {
											capturer.StartRecording();
									  }
								}, 10000);

								//start postponed rec
								new Handler().postDelayed(new Runnable()
								{
										@Override
									public void run()
									  {
											capturer.StartTranscoding();
									  }
								}, 20000);

							}

							
							if(GET_JPEG_ON_START){
								StartJPEG();
							}

							//mbuttonRec.setText("Stop Streaming");
						}else{
							capturer.Stop();
							led.setImageResource(R.drawable.led_green);
							captureStatusText.setText("");
							captureStatusStat.setText("");
							captureStatusText2.setText("");
							mbuttonRec.setImageResource(R.drawable.ic_fiber_manual_record_red);
							//mbuttonRec.setText("Start Streaming");
						}

					}
				}
			);
	}
    
	public boolean isRec(){
		return ( capturer != null && capturer.getState() == CaptureState.Started );
	}

	
	public String getRecordPath()
	{
		File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
				  Environment.DIRECTORY_DCIM), "RecordsMediaStreamer");

		if (! mediaStorageDir.exists()){
			if (!(mediaStorageDir.mkdirs() || mediaStorageDir.isDirectory())){
				Log.e(TAG, "<=getRecordPath() failed to create directory path="+mediaStorageDir.getPath());
				return "";
			}
		}
		return mediaStorageDir.getPath();
	}
	
	int get_record_flags()
	{
		int flags = PlayerRecordFlags.forType(PlayerRecordFlags.PP_RECORD_AUTO_START) | PlayerRecordFlags.forType(PlayerRecordFlags.PP_RECORD_SPLIT_BY_TIME);	// auto start and split by time
		//int flags = PlayerRecordFlags.forType(PlayerRecordFlags.PP_RECORD_AUTO_START) | PlayerRecordFlags.forType(PlayerRecordFlags.PP_RECORD_SPLIT_BY_SIZE);	// auto start and split by size
		return flags;
	}

	void load_config(){
		if(capturer == null)
			return;
		
		mConfig = capturer.getConfig();

		int settings_changed = settings.getInt("streaming_changed", 1);
		rtmp_url = ""+settings.getString("urlrtmp", "rtmp://54.173.34.172:1937/publish_demo/")+settings.getString("login", "demo")+settings.getString("urlch","0");
		if(settings_changed == 1){
			//rtmp_url = "rtmp://"+settings.getString("urlipport","")+"/live/ch"+settings.getString("urlch","0")+"?pass_code="+settings.getString("urlpasscode","0000");
			
			Editor ed = settings.edit();
			//ed.putString("urlrtmp", rtmp_url);
			ed.putInt("streaming_changed", 0);
			ed.apply();
		}else{
			//rtmp_url = settings.getString("urlrtmp", "rtmp://54.173.34.172:1937/publish_demo/abc");
		}
    	
		String sres = settings.getString("videoRes", "1280");
		int resX = 720;//1280;
		try{
			resX = Integer.parseInt(sres);
		}catch(NumberFormatException e){
			e.printStackTrace();
		}
		
		String svbitrate = settings.getString("HRVbitrate", "700");
		int vbitrate = 700;//1000;
		try{
			vbitrate = Integer.parseInt(svbitrate);
		}catch(NumberFormatException e){
			e.printStackTrace();
		}
		
		String sabitrate = settings.getString("audio_bitrate", "64");
		int abitrate = 64;//128;
		try{
			abitrate = Integer.parseInt(sabitrate);
		}catch(NumberFormatException e){
			e.printStackTrace();
		}


		String s_serverType = settings.getString("serverType", "1");
		int server_type = 1;// publish RTMP
		try{
			server_type = Integer.parseInt(s_serverType);
		}catch(NumberFormatException e){
			e.printStackTrace();
		}

		// RTSP server audio only mode
		boolean is_rtsp = false;
		if(server_type == MediaCaptureConfig.StreamerTypes.STREAM_TYPE_RTSP_SERVER.val()){
			is_rtsp = true;
			//Editor ed = settings.edit();
			//ed.putString("urlrtmp", rtmp_url);
			//ed.putBoolean("audio_enable", false);
			//ed.putBoolean("record_enable", false);
			//ed.apply();
		}
		
		misAudioEnabled = settings.getBoolean("audio_enable", true);
		
		boolean is_secvideo= settings.getBoolean("secvideo_enable", true);
		
		MediaCaptureConfig config = capturer.getConfig();

		//config.setCameraFacing(MediaCaptureConfig.CAMERA_FACING_FRONT);
		//config.setCameraFilter(MediaCaptureConfig.CAMERA_FILTER_FLIP_X);
		
		int ncm = config.getCaptureMode();
		if(misAudioEnabled){
			 ncm |= CaptureModes.PP_MODE_AUDIO.val();
		}else{
			ncm &= ~(CaptureModes.PP_MODE_AUDIO.val());
		}
		//set audio only
		//ncm = CaptureModes.PP_MODE_AUDIO.val();
		
		//config.setUseAVSync(false); //av sync off
		//config.setStreaming(false);
		config.setStreaming(true);
		config.setCaptureMode(ncm);
		config.setStreamType(server_type);
		if(USE_RTSP_G711){
			config.setAudioFormat( MediaCaptureConfig.TYPE_AUDIO_G711_MLAW);
			//config.setAudioFormat( MediaCaptureConfig.TYPE_AUDIO_G711_ALAW);
			config.setAudioBitrate(abitrate);
			config.setAudioSamplingRate(8000); 
			config.setAudioChannels(1);
		}else{
			config.setAudioFormat( MediaCaptureConfig.TYPE_AUDIO_AAC);
			config.setAudioBitrate(abitrate);
			config.setAudioSamplingRate(44100); //hardcoded
			config.setAudioChannels(2);
		}

		//change audio input
                /*AudioManager audio_manager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
                audio_manager.setMode(AudioManager.MODE_IN_COMMUNICATION);
                audio_manager.setSpeakerphoneOn(true);

		config.setAudioSource(MediaRecorder.AudioSource.MIC);
*/

		
		if(is_rtsp){
			String rtsp_url = "rtsp://@:"+settings.getString("urlport", "5540");
			config.setUrl(rtsp_url);
			config.setUrlSec(is_secvideo?rtsp_url:"");
		}else{
			config.setUrl(rtmp_url);
			config.setUrlSec(is_secvideo?rtmp_url+"2":"");
		}
		/*
		//START RTSP + RTMP together
		String rtsp_url = "rtsp://@:"+settings.getString("urlport", "5540");
		config.setUrl(MediaCaptureConfig.StreamerTypes.STREAM_TYPE_RTSP_SERVER.val(), rtsp_url);
		config.setUrlSec(MediaCaptureConfig.StreamerTypes.STREAM_TYPE_RTSP_SERVER.val(), is_secvideo?rtsp_url:"");
		config.setUrl(MediaCaptureConfig.StreamerTypes.STREAM_TYPE_RTMP_PUBLISH.val(), rtmp_url);
		config.setUrlSec(MediaCaptureConfig.StreamerTypes.STREAM_TYPE_RTMP_PUBLISH.val(), is_secvideo?rtmp_url+"2":"");
		*/
		
		if(USE_PORTRAIT_MODE){
			config.setVideoOrientation(90); //portrait
		}else{
			config.setVideoOrientation(0); //landscape
		}
		config.setVideoFramerate(30);
		config.setVideoKeyFrameInterval(1);
		config.setVideoBitrate(vbitrate);

		int bitrateMode = MediaCaptureConfig.BITRATE_MODE_ADAPTIVE;
		try{
			bitrateMode = Integer.parseInt(settings.getString("bitrateMode", ""+MediaCaptureConfig.BITRATE_MODE_ADAPTIVE));
		}catch(NumberFormatException e){
			e.printStackTrace();
		}
		config.setVideoBitrateMode(bitrateMode);
		config.setVideoSecBitrateMode(bitrateMode);
		if(bitrateMode == MediaCaptureConfig.BITRATE_MODE_ADAPTIVE){
			config.setVideoMaxLatency(500); //0.5sec
		}else{
			config.setVideoMaxLatency(0); //no latency control
		}
		
		List<CaptureVideoResolution> listRes = config.getVideoSupportedRes();
		if(listRes != null && listRes.size()>0){
			for(CaptureVideoResolution vr:listRes){
				int w = (config.getVideoOrientation() == 0)? config.getVideoWidth(vr):config.getVideoHeight(vr);
				if(w < resX){
					Log.i(TAG, "load_config resolution="+resX+" not supported. Force set resolution="+w);
					resX = w;
				}
				break;
			}
		}

		//test UHD		
		//resX=3840;
		switch( resX ){
		case 3840:
			config.setVideoResolution(CaptureVideoResolution.VR_3840x2160);
			break;
		case 1920:
			config.setVideoResolution(CaptureVideoResolution.VR_1920x1080);
			break;
		case 1280:
			config.setVideoResolution(CaptureVideoResolution.VR_1280x720);
			break;
		case 721:
			config.setVideoResolution(CaptureVideoResolution.VR_720x576);
			break;
		case 720:
			config.setVideoResolution(CaptureVideoResolution.VR_720x480);
			break;
		case 640:
			config.setVideoResolution(CaptureVideoResolution.VR_640x480);
			break;
		case 352:
			config.setVideoResolution(CaptureVideoResolution.VR_352x288);
			break;
		case 320:
			config.setVideoResolution(CaptureVideoResolution.VR_320x240);
			break;
		case 176:
			config.setVideoResolution(CaptureVideoResolution.VR_176x144);
			break;
		}
		
		if(config.getVideoWidth() == 320 || config.getVideoWidth() == 240){
			config.setSecVideoResolution(CaptureVideoResolution.VR_720x480);
		}else{
			config.setSecVideoResolution(CaptureVideoResolution.VR_320x240);
		}
		config.setSecVideoFramerate(5);
		config.setSecVideoKeyFrameInterval(2);
		//config.setUseSec(is_rtsp && is_secvideo);

		misRecfileEnabled = settings.getBoolean("record_enable", false);
		//if(is_rtsp)
		//	misRecfileEnabled = false;
		//misRecfileEnabled = true;
		config.setUseSec(true);
		config.setUseSecRecord(true);
		config.setRecordPrefixSec("secondary");
		config.setRecording(misRecfileEnabled);
		int record_flags = get_record_flags();
		int rec_split_time = ( (record_flags & PlayerRecordFlags.forType(PlayerRecordFlags.PP_RECORD_SPLIT_BY_TIME)) != 0)? 30:0; //30 sec	
		config.setRecordPath(getRecordPath());
		config.setRecordFlags(record_flags);
		config.setRecordSplitTime(rec_split_time);//in sec
		config.setRecordSplitSize(10); //in MB
		
		//transcoding
		misTranscodingEnabled = false;//settings.getBoolean("transcode_enable", true);
		config.setTranscoding(misTranscodingEnabled);
		config.setTransWidth(320);
		config.setTransHeight(240);
		config.setTransFps(1);
		config.setTransFormat(MediaCaptureConfig.TYPE_VIDEO_RAW);
		String key = "-----BEGIN RSA PRIVATE KEY-----\n" +
                "MIICXQIBAAKBgQC6pKw/FG9ujxEJb5kRTRAoFNBNO0B7DP9LJ/ME6KI2JAC8utDH\n" +
                "uhaEOU/UIFl+0uW80+Nl/BBPf6s1vrPjcDfbrUGcW1DOjsvGwPEjtrmH4jalnj2N\n" +
                "kyZ7VSin1vVUl0EBTiFGJf4aBfPl2RhLQ7WoG24jvPjgorfjTLzpnIRkIwIDAQAB\n" +
                "AoGAa+j3mYT0JETdQcpfAmy+0Z2vDWgbkMlj9Q0E6aqi1pCcSIHuYfuKNyp3qBqI\n" +
                "A9Zlc3ZCfF1vBLe4wlse4HmqIP9M9ee+PfEuhJhJrj+ETMzz3KPSHrlCHrbYIsan\n" +
                "sBL7Buq6J+TIfQdll9rZrPfvdR2P8WX8KxX7IjUSFIwlGkkCQQD0JBe1o4Z4HmMJ\n" +
                "DPUAbBmXuLRjqHdALmdz48EysIl6ffbtHbttbrBCGMhnzcK8ls1KX95inojm/7FA\n" +
                "FsBROBMHAkEAw7WXselRy16NfJHhnZPnH9JrCDrY8PbgD1q2bIbZzzMj9a1gJoBq\n" +
                "ZOgSXbi3Ck9ZvuGQtKAuaUWYXvp7RcmTBQJBANinBttLyFLkNGKduvWq+HMpl/sw\n" +
                "TtMH2wp+vL3s57NqASyey+rq5UNilsV3VS7ibD9qIAFISpkjovoKtpPcvRUCQQC4\n" +
                "jwgl29ypx1nwDnZQLsk3xivvT+eDnZyAflAoGidO8XBI354b0OAElqGzRl0+2MPO\n" +
                "cVMQMzpxRhNCBj63+jatAkB9jvvcMlxLJYheEAQq0fcBHKNTPFIMyEt7aJh2sUTb\n" +
                "jzV40Dt3ecGSigFYT8lmzNKN5m5kSU5AWumWkkQ+Fs98\n" +
                "-----END RSA PRIVATE KEY-----";
	        String cert ="-----BEGIN RSA PRIVATE KEY-----\n" +
                "MIICXQIBAAKBgQC6pKw/FG9ujxEJb5kRTRAoFNBNO0B7DP9LJ/ME6KI2JAC8utDH\n" +
                "uhaEOU/UIFl+0uW80+Nl/BBPf6s1vrPjcDfbrUGcW1DOjsvGwPEjtrmH4jalnj2N\n" +
                "kyZ7VSin1vVUl0EBTiFGJf4aBfPl2RhLQ7WoG24jvPjgorfjTLzpnIRkIwIDAQAB\n" +
                "AoGAa+j3mYT0JETdQcpfAmy+0Z2vDWgbkMlj9Q0E6aqi1pCcSIHuYfuKNyp3qBqI\n" +
                "A9Zlc3ZCfF1vBLe4wlse4HmqIP9M9ee+PfEuhJhJrj+ETMzz3KPSHrlCHrbYIsan\n" +
                "sBL7Buq6J+TIfQdll9rZrPfvdR2P8WX8KxX7IjUSFIwlGkkCQQD0JBe1o4Z4HmMJ\n" +
                "DPUAbBmXuLRjqHdALmdz48EysIl6ffbtHbttbrBCGMhnzcK8ls1KX95inojm/7FA\n" +
                "FsBROBMHAkEAw7WXselRy16NfJHhnZPnH9JrCDrY8PbgD1q2bIbZzzMj9a1gJoBq\n" +
                "ZOgSXbi3Ck9ZvuGQtKAuaUWYXvp7RcmTBQJBANinBttLyFLkNGKduvWq+HMpl/sw\n" +
                "TtMH2wp+vL3s57NqASyey+rq5UNilsV3VS7ibD9qIAFISpkjovoKtpPcvRUCQQC4\n" +
                "jwgl29ypx1nwDnZQLsk3xivvT+eDnZyAflAoGidO8XBI354b0OAElqGzRl0+2MPO\n" +
                "cVMQMzpxRhNCBj63+jatAkB9jvvcMlxLJYheEAQq0fcBHKNTPFIMyEt7aJh2sUTb\n" +
                "jzV40Dt3ecGSigFYT8lmzNKN5m5kSU5AWumWkkQ+Fs98\n" +
                "-----END RSA PRIVATE KEY-----\n" +
                "-----BEGIN CERTIFICATE-----\n" +
                "MIICATCCAWoCCQCkiVNSr0w0DDANBgkqhkiG9w0BAQsFADBFMQswCQYDVQQGEwJB\n" +
                "VTETMBEGA1UECAwKU29tZS1TdGF0ZTEhMB8GA1UECgwYSW50ZXJuZXQgV2lkZ2l0\n" +
                "cyBQdHkgTHRkMB4XDTE1MDcwODE2MjgzNVoXDTE2MDcwNzE2MjgzNVowRTELMAkG\n" +
                "A1UEBhMCQVUxEzARBgNVBAgMClNvbWUtU3RhdGUxITAfBgNVBAoMGEludGVybmV0\n" +
                "IFdpZGdpdHMgUHR5IEx0ZDCBnzANBgkqhkiG9w0BAQEFAAOBjQAwgYkCgYEAuqSs\n" +
                "PxRvbo8RCW+ZEU0QKBTQTTtAewz/SyfzBOiiNiQAvLrQx7oWhDlP1CBZftLlvNPj\n" +
                "ZfwQT3+rNb6z43A3261BnFtQzo7LxsDxI7a5h+I2pZ49jZMme1Uop9b1VJdBAU4h\n" +
                "RiX+GgXz5dkYS0O1qBtuI7z44KK340y86ZyEZCMCAwEAATANBgkqhkiG9w0BAQsF\n" +
                "AAOBgQAGOdsEThgYAd3LAV9xt8aYAEONMDivrlWxC849PX+PSh25mQXTPAsfEEP2\n" +
                "4dWCxtkKaIMIRiYfcSCGqErtUVufB0jkwS+oE9/RIpmGFRh3zMH/NBsI4eNcjJwV\n" +
                "R6G0eVEvNUdCPixHTYs/9VPzJ2MJgI+AsQPxC6/kg78SJAbcwA==\n" +
                "-----END CERTIFICATE-----";
		config.setSecureStreaming(true, cert, key);

		if (settings.getBoolean("capture_screen", false)) {
			config.setCaptureSource(MediaCaptureConfig.CaptureSources.PP_MODE_VIRTUAL_DISPLAY.val());
		}
		else {
			if(MediaCapture.DISABLE_CAMERA){
				config.setCaptureSource(MediaCaptureConfig.CaptureSources.PP_MODE_SURFACE.val());
			}else{
				config.setCaptureSource(MediaCaptureConfig.CaptureSources.PP_MODE_CAMERA.val());
			}
		}

		//config.setRecordTimeshift(10);

	}

	protected void onPause()
	{
		Log.e(TAG, "onPause()");
		super.onPause();

		if (capturer != null)
			capturer.onPause();
	}

	@Override
  	protected void onResume() 
	{
		Log.e(TAG, "onResume()");
		super.onResume();
		if (capturer != null)
			capturer.onResume();
  	}

  	@Override
	protected void onStart() 
  	{
      	Log.e(TAG, "onStart()");
		super.onStart();
		sMainActivity = this;
		
		// Lock screen
		mWakeLock.acquire();
		
		if (capturer != null)
			capturer.onStart();
	}

  	@Override
	protected void onStop() 
  	{
  		Log.e(TAG, "onStop()");
		super.onStop();
		if (capturer != null)
			capturer.onStop();
		
		// A WakeLock should only be released when isHeld() is true !
		if (mWakeLock.isHeld()) mWakeLock.release();
		
		if (toastShot != null)
			toastShot.cancel();
		
		if(misSurfaceCreated){
			finish();
		}
	}

    @Override
    public void onBackPressed() 
    {
		if (toastShot != null)
			toastShot.cancel();
		
		if(capturer != null)
			capturer.Close();
		
		super.onBackPressed();
    }
  	
  	@Override
  	public void onWindowFocusChanged(boolean hasFocus) 
  	{
  		Log.e(TAG, "onWindowFocusChanged(): " + hasFocus);
  		super.onWindowFocusChanged(hasFocus);
		if (capturer != null)
			capturer.onWindowFocusChanged(hasFocus);
  	}

  	@Override
  	public void onLowMemory() 
  	{
  		Log.e(TAG, "onLowMemory()");
  		super.onLowMemory();
		//if (capturer != null)
		//	capturer.onLowMemory();
  	}

  	@Override
  	protected void onDestroy() 
  	{
  		Log.e(TAG, "onDestroy()");
		if (toastShot != null)
			toastShot.cancel();
		
		if (capturer != null)
			capturer.onDestroy();
		
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
		Intent intent;

		switch (item.getItemId()) {
		case R.id.menu_settings:
			if(isRec()){
				Toast.makeText(this,"Press 'Stop Streaming' button  first", Toast.LENGTH_LONG).show();
			}else{
				intent = new Intent(this.getBaseContext(),SettingsActivity.class);
				startActivity(intent);
				finish();
			}
			return true;
			
		case R.id.menu_exit:
			if(isRec()){
				Toast.makeText(this,"Press 'Stop Streaming' button  first", Toast.LENGTH_LONG).show();
			}else{
				finish();
			}
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
}
