package veg.mediacapture.sdk.test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;

import veg.mediacapture.sdk.MediaCapture;
import veg.mediacapture.sdk.MediaCaptureCallback;
import veg.mediacapture.sdk.MediaCaptureConfig;
import veg.mediacapture.sdk.MediaCapture.CaptureNotifyCodes;
import veg.mediacapture.sdk.MediaCapture.CaptureState;
import veg.mediacapture.sdk.MediaCapture.PlayerRecordFlags;
import veg.mediacapture.sdk.MediaCapture.PlayerRecordStat;
import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.os.Handler;
import android.os.Message;

public class MainActivity extends Activity 
                                implements MediaCaptureCallback{
	
	private static final String TAG = "MainActivity";	
	
    Button button;
    int touchCount;
    
    MediaCapture  audioCapture = null;
	private int mOldMsg = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        button = new Button(this);
        button.setText( "Touch to start Audio RTSP server!" );
        button.setOnClickListener(new View.OnClickListener(){

			@Override
			public void onClick(View arg0) {
				
				if(audioCapture.getState() != MediaCapture.CaptureState.Started){
					
					audioCapture.Start();
					
					button.setText( "Touch to stop Audio RTSP server! state="+audioCapture.getState() );
				}else{
					audioCapture.Stop();
					
			        button.setText( "Touch to start Audio RTSP server! audioCapture.getState()="+audioCapture.getState());
				}
		        touchCount++;
			}
        	
        });
        setContentView(button);
        
        audioCapture = new MediaCapture(this, null, false);
        
        MediaCaptureConfig config = audioCapture.getConfig();
		config.setCaptureMode(MediaCaptureConfig.CaptureModes.PP_MODE_AUDIO.val());

		//AAC
        config.setAudioSamplingRate(48000);
        config.setAudioChannels(2);
        config.setAudioFormat(MediaCaptureConfig.TYPE_AUDIO_AAC);

		//MULAW
        //config.setAudioSamplingRate(8000);
        //config.setAudioChannels(1);
        //config.setAudioFormat(MediaCaptureConfig.TYPE_AUDIO_G711_MLAW);

		config.setStreamType(MediaCaptureConfig.StreamerTypes.STREAM_TYPE_RTSP_SERVER.val());
		config.setStreaming(true);

		config.setRecording(false);
		config.setTranscoding(false);
		
        
        audioCapture.Open(null, this);
    }
    
    @Override
    public void onStop()
    {
    	super.onStop();
    	audioCapture.Stop();
    }

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
	        		strText = "Camera surface created";
	        		break;
	        	case CAP_SURFACE_DESTROYED:
	        		strText = "Camera surface destroyed";
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

					if(audioCapture.getState() != MediaCapture.CaptureState.Started){
						
						button.setText( "Touch to stop Audio RTSP server! state="+audioCapture.getState() );
					}else{

						String sT = "Touch to start Audio RTSP server! audioCapture.getState()="+audioCapture.getState();
						if(audioCapture.USE_RTSP_SERVER){
							sT += ". RTSP ON ("+ audioCapture.getRTSPAddr()+")";
						}
						
						button.setText(sT);
					}
	        		
	        		break;

	            default:
	            	break;
	        }
	        if(strText != null){
	        	Log.i(TAG, "=Status handleMessage str="+strText);
                //captureStatusText.setText( strText );
	        }
	    }
	};

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

	@Override
	public int OnCaptureReceiveData(ByteBuffer buffer, int type, int size,
			long pts) {
		
		Log.v(TAG, "=OnCaptureReceiveData buffer.size="+size+" pts="+pts+" buffer.arrayOffset()="+buffer.arrayOffset());
		/*if(fdump != null){
			try {
				fdump.write(buffer.array(), buffer.arrayOffset(), size);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}*/
		
		return 0;
	}
	
	/** Create a File for saving an image or video */
	/*private File getOutputMediaFile(int type){
	    // To be safe, you should check that the SDCard is mounted
	    // using Environment.getExternalStorageState() before doing this.

	    File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
	              Environment.DIRECTORY_DCIM), "AudioTest");
	    // This location works best if you want the created images to be shared
	    // between applications and persist after your app has been uninstalled.

	    // Create the storage directory if it does not exist
	    if (! mediaStorageDir.exists()){
	        if (!(mediaStorageDir.mkdirs() || mediaStorageDir.isDirectory())){
	            Log.e(TAG, "failed to create directory");
	            return null;
	        }
	    }

	    fdump_filename = mediaStorageDir.getPath() + File.separator +
	    		((type==0)?"dump_mlaw.raw":"dump_alaw.raw");
	    File mediaFile = new File( fdump_filename );
	    if(mediaFile.exists()){
	    	mediaFile.delete();
	    }

	    return mediaFile;
	}*/


}
