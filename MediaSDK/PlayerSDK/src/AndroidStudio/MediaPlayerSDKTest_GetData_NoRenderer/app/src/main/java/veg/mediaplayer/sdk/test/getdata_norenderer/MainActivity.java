/*
 *
  * Copyright (c) 2011-2017 VXG Inc.
 *
 */

package veg.mediaplayer.sdk.test.getdata_norenderer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.SparseArray;
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
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.view.*;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView.OnEditorActionListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.Arrays;

import android.preference.PreferenceManager;
import android.renderscript.*;
import veg.mediaplayer.sdk.MediaPlayer;
import veg.mediaplayer.sdk.MediaPlayer.PlayerCallbackDataConfig;
import veg.mediaplayer.sdk.MediaPlayer.PlayerCallbackDataMask;
import veg.mediaplayer.sdk.MediaPlayer.PlayerNotifyCodes;
import veg.mediaplayer.sdk.MediaPlayerConfig;

public class MainActivity extends Activity implements OnClickListener, MediaPlayer.MediaPlayerCallback, 
                                                                       MediaPlayer.MediaPlayerCallbackData, SurfaceHolder.Callback2
{

    public static AutoCompleteTextView  edtIpAddress;
    public static ArrayAdapter<String>  edtIpAddressAdapter;
    public static ArrayList<String>     edtIpAddressHistory = new ArrayList<String>();
	
	private Button						btnConnect;
	private Button						btnHistory;

	private Button						btnAddView;
	private Button						btnRemoveView;
    private ToggleButton                btnShowVideoAsBitmap;
    private ToggleButton                btnFaceDetection;
    private ToggleButton                btnVideoCrop;
    
	private SurfaceView 				externalView = null;
	private FrameLayout					playerView = null;
	
	private SharedPreferences 			settings;
    private SharedPreferences.Editor 	editor;

    private boolean                     playing = false;
    private MediaPlayer                 player = null;
    private MainActivity                mthis = null;
	private Surface                     surface = null;
	
    private FaceDetector                faceDetector = null;
 
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

    protected void loadHistory(boolean withSaved) {
        ArrayList<String> tempHistory = new ArrayList<String>();



		tempHistory.add("rtsp://3.84.6.190/vod/mp4:BigBuckBunny_115k.mov");
		tempHistory.add("http://devimages.apple.com/iphone/samples/bipbop/gear1/prog_index.m3u8");


		edtIpAddressHistory.addAll(tempHistory);
        edtIpAddress.setText(tempHistory.get(0));
    }
    
    private RenderScript rs;
    private ScriptIntrinsicYuvToRGB yuvToRgbIntrinsic;
    private Allocation in;
    private Allocation out;
    private Bitmap bmout;
    private ScriptC_yuv mScript;
    private ScriptGroup mGroup;

    private ImageView iv;
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

		Log.e("Test", "RTSPPLayer instance " + player);
		
		settings = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);

        strUrl = "rtsp://3.84.6.190/vod/mp4:BigBuckBunny_115k.mov";


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

		// Array of choices
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
		btnAddView.setVisibility(View.GONE);
		btnAddView.setOnClickListener(new View.OnClickListener() 
		{
			public void onClick(View v) 
			{
				if (externalView != null)
					return;
				
				externalView = new SurfaceView(mthis);
				externalView.getHolder().addCallback(mthis);
				
				externalView.setZOrderOnTop(true);    // necessary

				playerView.addView(externalView);
                externalView.setZOrderOnTop(true);    // necessary
			}   
		});
        
		btnRemoveView = (Button)findViewById(R.id.button_remove_view);
		btnRemoveView.setVisibility(View.GONE);
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
		
				
        btnFaceDetection = (ToggleButton)findViewById(R.id.button_face_detection);
        btnFaceDetection.setOnClickListener(new View.OnClickListener() 
        {
            public void onClick(View v) 
            {
            }   
        });

        btnShowVideoAsBitmap = (ToggleButton)findViewById(R.id.button_show_video_as_bitmap);
		btnShowVideoAsBitmap.setOnClickListener(new View.OnClickListener() 
        {
            public void onClick(View v) 
            {
            }   
        });
		
		btnVideoCrop = (ToggleButton)findViewById(R.id.button_video_crop);
		btnVideoCrop.setOnClickListener(new View.OnClickListener() 
        {
            public void onClick(View v) 
            {
            }   
        });
		
				
		
		iv = (ImageView) findViewById(R.id.image);
		
		rs = RenderScript.create(this);
		mScript = new ScriptC_yuv(rs);
		yuvToRgbIntrinsic = ScriptIntrinsicYuvToRGB.create(rs, Element.RGBA_8888(rs));
		
        faceDetector = new FaceDetector.Builder(getApplicationContext())
                .setTrackingEnabled(false)
                .setLandmarkType(FaceDetector.ALL_LANDMARKS)
                .build();
		
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
				
				btnVideoCrop.setEnabled(true);
				
	            in = null; 
	            bmout = null;
	            out = null;
				
			}
			else
			{
    	    	player.setVisibility(View.INVISIBLE);
    	    	
    	    	MediaPlayerConfig conf = new MediaPlayerConfig();
    	    	conf.setDecodingType(2);
    	    	conf.setRendererType(1);
    	    	conf.setSynchroEnable(0);
                //conf.setEnableLogging(-1);
    	    	conf.setConnectionUrl(ConnectionUrl);
    	    	
    	    	if (surface != null)
    	    	{
    	    		Log.e(TAG, "setSurface " + surface);
    	    		player.setSurface(surface);
    	    	}

                int callbackDataMask = PlayerCallbackDataMask.forType(PlayerCallbackDataMask.PP_CALLBACK_DATA_RENDERER_VIDEO_DATA);
                PlayerCallbackDataConfig dataConfig = player.new PlayerCallbackDataConfig();

                // Enable crop here
                if (btnVideoCrop.isChecked())
                {
                    int video_width = 1920;
                    int video_height = 1080;
                    
                    int crop_width = 640;
                    int crop_height = 480;
                    
                    int crop_left = (video_width / 2) - (crop_width / 2);
                    int crop_top = (video_height / 2) - (crop_height / 2);
                    int crop_right = crop_left + crop_width;
                    int crop_bottom = crop_top + crop_height;
                    
                    dataConfig.setVideoRendererFrameCropRect(new Rect(crop_left, crop_top, crop_right, crop_bottom));
                }
                
                player.setOnDataListener(mthis, callbackDataMask, dataConfig);

                btnVideoCrop.setEnabled(false);
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
	
    private void getListFiles2(File parentDir, ArrayList<String> fullPath) {
        Queue<File> files = new LinkedList<File>();
        files.addAll(Arrays.asList(parentDir.listFiles()));
        while (!files.isEmpty()) {
            File file = files.remove();
            if (file.isDirectory()) {
                files.addAll(Arrays.asList(file.listFiles()));
            } else {
                if (file.getName().endsWith(".ts") || file.getName().endsWith(".mp4") || file.getName().endsWith(".avi")
                        || file.getName().endsWith(".mpeg2") || file.getName().endsWith(".mov")
                        || file.getName().endsWith(".flv") || file.getName().endsWith(".mpeg4")
                        || file.getName().endsWith(".mkv")) {
                    fullPath.add(file.getAbsolutePath());
                }
            }
        }
    }
	
    protected float pxFromDp(float dp)
    {
        return (dp * getResources().getDisplayMetrics().density);
    }

    public Bitmap getFrameAsBitmap(ByteBuffer frame, int width, int height) {
        Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bmp.copyPixelsFromBuffer(frame);
        return bmp;
    }
    
    @Override
    public int OnVideoSourceFrameAvailable(ByteBuffer buffer, int size, long pts, long dts, int stream_index,
            int format) {
        return 0;
    }

    @Override
    public int OnAudioSourceFrameAvailable(ByteBuffer buffer, int size, long pts, long dts, int stream_index,
            int format) {
        return 0;
    }

    @Override
    public int OnVideoRendererFrameAvailable(ByteBuffer buffer, int size, String format_fourcc, int width, int height,
            int bytes_per_row, long pts, int will_show) {
        Log.e(TAG, "OnVideoRendererFrameAvailable: FOURCC: " + format_fourcc + ", size: " + size + ", width: " + width + 
                    ", height: " + height + ", bytes_per_row: " + bytes_per_row + ", pts: " + pts + ", will_show: " + will_show);

        SparseArray<Face> faces = null;
        if (btnFaceDetection.isChecked())
        {
            int format = ImageFormat.NV21;
            if (format_fourcc == "NV12")
                format = ImageFormat.NV21;

            Frame frame1 = new Frame.Builder().setImageData(buffer, width, height, format).build();
            faces = faceDetector.detect(frame1);
            for(int i=0; i<faces.size(); i++) 
            {
              Face thisFace = faces.valueAt(i);
              float x1 = thisFace.getPosition().x;
              float y1 = thisFace.getPosition().y;
              float x2 = x1 + thisFace.getWidth();
              float y2 = y1 + thisFace.getHeight();
              Log.v(TAG, "FACE: " + x1 + ":" + y1 + "      " +  x2 + ":" + y2);
            }
        }
        
        if (btnShowVideoAsBitmap.isChecked())
        {
            byte[] bbuffer = new byte[buffer.remaining()];
            buffer.get(bbuffer);

            if (in == null)
            {
                bmout = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                out = Allocation.createFromBitmap(rs, bmout);
                

                Type.Builder tb = new Type.Builder(rs, Element.createPixel(rs, Element.DataType.UNSIGNED_8, Element.DataKind.PIXEL_YUV));
                tb.setX(width);
                tb.setY(height);
                tb.setYuvFormat(android.graphics.ImageFormat.NV21);
                in = Allocation.createTyped(rs, tb.create(), Allocation.USAGE_SCRIPT);
                yuvToRgbIntrinsic.setInput(in);
                

            }
            
            in.copyFrom(bbuffer); 
            yuvToRgbIntrinsic.forEach(out); 
            out.copyTo(bmout);
            
            if (faces != null && faces.size() > 0)
            {
                Canvas c = new Canvas(bmout);
                Paint p = new Paint();
                p.setStyle(Style.STROKE);
                p.setStrokeWidth(2);
                p.setColor(Color.RED);


                //Log.v(TAG, "FACE :" + faces.size());
                for(int i=0; i<faces.size(); i++) 
                {
                  Face thisFace = faces.valueAt(i);
                  float x1 = thisFace.getPosition().x;
                  float y1 = thisFace.getPosition().y;
                  float x2 = x1 + thisFace.getWidth();
                  float y2 = y1 + thisFace.getHeight();
                  c.drawRect(x1, y1, x2, y2, p);                
                }
            }
            
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    iv.setImageBitmap(bmout);
                }
            });
        }
       
        
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

	private static final String TAG = "MediaPlayerSDKTest.ExternalSurface";

}
