package com.vxg.videoplayertest.Views.Controls.player;


import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.vxg.videoplayertest.Listeners.CameraManipulatorListener;
import com.vxg.videoplayertest.R;
import com.vxg.videoplayertest.Listeners.ScreenOrientationNeedChangeListener;
import com.vxg.videoplayertest.Models.SharedSettings;
import com.vxg.videoplayertest.Listeners.VideoListener;
import com.vxg.videoplayertest.Utils.OnSwipeTouchListener;

import java.nio.ByteBuffer;

import veg.mediaplayer.sdk.DebugGuard;
import veg.mediaplayer.sdk.MediaPlayer;
import veg.mediaplayer.sdk.MediaPlayerConfig;

public class VideoPlayer extends FrameLayout {
    private final String TAG = VideoPlayer.class.getSimpleName();
    private VideoListener mVideoListener = null;
    private FrameLayout btnPlayPause;
    private FrameLayout btnAspect;
    private ImageView btnAspectImage;
    private ImageView btnUnLock;
    private FrameLayout btnLock;
    private View btnPtz;
    private CameraManipulator cameraManipulator;
    private ViewGroup bottonsPanel;
    private boolean started = true;
    private boolean paused = false;
    private boolean isFullscreen = false;
    private FrameLayout btnFullScreeen;
    private ImageView btnFullScreeenImage;
    private ImageView btnPlayImage;
    private MediaPlayer mediaPlayer;
    int[] ctl2_imgAspects;
    private MediaPlayerConfig conf;
    private ProgressBar playerProgress;
    private Context context;
    private PlayerStates playerState = PlayerStates.NONE;
	private int enabled_change_position = 1;


    private enum PlayerStates {
        Busy,
        ReadyForUse,
        NONE
    };

    private enum PlayerConnectType {
        Normal,
        Reconnecting
    };

    private ScreenOrientationNeedChangeListener screenListener;

    public void setScreenListener(ScreenOrientationNeedChangeListener screenListener) {
        this.screenListener = screenListener;
    }

    public boolean isFullScreen(){
        return isFullscreen;
    }

    public void setFullscreen(boolean fullscreen){
        isFullscreen = fullscreen;
    }

    public VideoPlayer(Context context) {
        super(context);
        this.context = context;
    }

    private void setLockUi(boolean lock){
        btnUnLock.setVisibility(lock?VISIBLE:INVISIBLE);
        bottonsPanel.setVisibility(lock?INVISIBLE:VISIBLE);
    }
     public void hideButtons(){
         bottonsPanel.setVisibility(INVISIBLE);
     }

	public void setContentProvider(int provider)
	{
		// 0 - FFMPEG source (support various video and audio fotmats)
		// 1 - RTSTM source (RTSP protocol with various option)
		// 2 - WEBRTC source (WEBRTC source)
		conf.setContentProviderLibrary(provider);
	}

	public long getPosition()
		{
	    	// Return -1 in case if position is not ready
			if (enabled_change_position == 0)
				{
					Log.e(TAG, "getPosition enabled:" + enabled_change_position);
					return -1;
				}

		
            if (mediaPlayer.getState()  != MediaPlayer.PlayerState.Closed)
            	{
            		long pos = mediaPlayer.getRenderPosition();
            		Log.e(TAG, "getPosition pos:" + pos);
                	return pos; 
            	}

            return -1; // Invalid state
		}

    public long getDuration()
    {
    	// Return -1 in case if duration is not ready
		if (enabled_change_position == 0)
			return -1;
	
        if (mediaPlayer.getState()  != MediaPlayer.PlayerState.Started ||
                mediaPlayer.getState()  != MediaPlayer.PlayerState.Paused)
            return mediaPlayer.getStreamDuration();

        return -1; // Invalid state
    }


	public long setPosition(long pos)
		{
			if (mediaPlayer.getState()	!= MediaPlayer.PlayerState.Closed)
				{
				  enabled_change_position = 0;	
				  return mediaPlayer.setStreamPosition(pos);
				} 

            return -1; // Invalid state
		}


    public VideoPlayer(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
		
        final FrameLayout container = (FrameLayout) LayoutInflater.from(context).inflate(R.layout.layout_payer, null);

		conf = new MediaPlayerConfig();
        mediaPlayer = container.findViewById(R.id.playerSdk);
		
        
        bottonsPanel = container.findViewById(R.id.bottonsPanel);
        btnLock = container.findViewById(R.id.btnLock);
        btnUnLock = container.findViewById(R.id.btnUnLock);

        btnLock.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
               setLockUi(true);
            }
        });

        btnUnLock.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                setLockUi(false);
            }
        });

        btnPlayPause = container.findViewById(R.id.btnPlayPause);
        btnPlayImage = container.findViewById(R.id.btnPlayPauseImage);
        btnPlayPause.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if(playerState==PlayerStates.ReadyForUse){
                    pause();
                } else  if(playerState==PlayerStates.Busy){
                    play();
                } else if(playerState==PlayerStates.NONE){
                    open();
                }
            }
        });

        btnAspect = container.findViewById(R.id.btnAspect);
        btnAspectImage = container.findViewById(R.id.btnAspectImage);

        btnAspect.setOnClickListener(new OnClickListener() {
            int counter = 1;
            @Override
            public void onClick(View view) {
               counter++;
               if(counter>4){
                   counter= 0;
               }

               if(counter==4){
                   container.setOnTouchListener(swipeTouchListener);
               } else {
                   container.setOnTouchListener(null);
               }

               btnAspectImage.setImageResource(ctl2_imgAspects[counter]);
               mediaPlayer.getConfig().setAspectRatioMode(counter);
               int mode = mediaPlayer.getConfig().getAspectRatioMode();
               mediaPlayer.UpdateView();
            }
        });

        btnFullScreeen = container.findViewById(R.id.btnFullScreeen);
        btnFullScreeenImage = container.findViewById(R.id.btnFullScreeenImage);
        btnFullScreeen.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                isFullscreen=!isFullscreen;
                btnFullScreeenImage.setImageResource(isFullscreen?R.drawable.ic_player_screen_compact:R.drawable.ic_player_screen_ful);
                if(screenListener!=null){
                    screenListener.onNeedChangeScreenOrientation(true);
                }
            }
        });

        ctl2_imgAspects = new int[]{
                R.drawable.ic_player_screen_stretch,
                R.drawable.ic_player_screen_aspect,
                R.drawable.ic_player_screen_crop,
                R.drawable.ic_player_screen_realsize,
                R.drawable.ic_player_screen_zoom
        };

        playerProgress = container.findViewById(R.id.playerProgress);
		
        cameraManipulator = new CameraManipulator(container);
        cameraManipulator.setListener(cameraManipulatorListener);
		cameraManipulator.hide();
		
        btnPtz = container.findViewById(R.id.btnPtz);
        btnPtz.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if(cameraManipulator.isVisible()) {
                    cameraManipulator.hide();
                } else {
                    cameraManipulator.show();
                }
            }
        });

		
		
        addView(container);

    }

    private void setPtzPosition(){
        int x =  bottonsPanel.getLeft()+btnPtz.getLeft();
        int y =  bottonsPanel.getTop() + btnPtz.getTop();
        cameraManipulator.setPosition(x,y);
    }

    private void showProgress(){
        playerProgress.setVisibility(VISIBLE);
    }

    private void hideProgress(){
        playerProgress.setVisibility(INVISIBLE);
    }

    public void setVideoListener(VideoListener videoListener) {
        this.mVideoListener = videoListener;
    }

    private void onStartPrepare(){
      showProgress();
    }

    private void onPrepared(){

    }

    private void onPrepareFailed(){

    }

    private void onVideoPlayed(){
	   if (null != this.mVideoListener)
	   		this.mVideoListener.onVideoPlayed();
       hideProgress();
    }

    private void onVideoResumed(){

    }

    private void onVideoPaused(){
		if (null !=  this.mVideoListener)
				  this.mVideoListener.onVideoPaused();
    }

    private void configuremediaPlayer(){
        SharedSettings.getInstance().loadPrefSettings();
        SharedSettings sett = SharedSettings.getInstance();

		// Set tranport protocol if network source is RTSP or ONVIF	
        conf.setConnectionNetworkProtocol(sett.connectionProtocol);
        conf.setConnectionDetectionTime(sett.connectionDetectionTime);
        conf.setConnectionBufferingTime(sett.connectionBufferingTime);
		
		// Set DataReceiver 
		conf.setDataReceiveTimeout(30000000);
		
        conf.setDecodingType(sett.decoderType);
//        conf.setSynchroNeedDropVideoFrames(sett.synchroNeedDropVideoFrames);

		switch (sett.LatencyControl)
			{
				// 0 - Diabled - latency control is disabled
                default:
				case 0:
					conf.setSynchroEnable(1);
					conf.setLatencyPreSet(-2);
				break;
				// 1 - Extra   - Disable audio and video syncronization and video is played with max speed
				case 1:
					conf.setSynchroEnable(0);
					conf.setLatencyPreSet(-2);
				break;	
				// 2 - Minimal - ~ 3-6 frames
				case 2:
					conf.setSynchroEnable(1);
					conf.setLatencyPreSet(0);
				break;
				// 3 - Middle  - ~ 10-20 frames
				case 3:
					conf.setSynchroEnable(1);
					conf.setLatencyPreSet(1);
				break;	
				// 4 - Max	   - ~ 25-135 Frames
				case 4:
					conf.setSynchroEnable(1);
					conf.setLatencyPreSet(2);
				break;	
			}

		// Konst
		// Here is depend from button state 
        conf.setEnableAspectRatio(sett.rendererEnableAspectRatio);
    }

    public void setVideoSource(String source){

		// Set video source URL
        conf.setConnectionUrl(source);

		// Set options 
		configuremediaPlayer();
    }

    public void open(){

        // close player if state was opened
        //if (mediaPlayer.getState() != MediaPlayer.PlayerState.Closed)
	    mediaPlayer.Close();

       btnPlayImage.setImageResource(R.drawable.ic_player_pause);

       enablePtz(false);

       mediaPlayer.Open(conf,playerCallback);
    }

    public void play(){
        if (mediaPlayer.getState()  == MediaPlayer.PlayerState.Closed) {
			open();
       } else {
            mediaPlayer.Play();
        }
    }

    public void play(int speed){
		if (speed != 0)
			mediaPlayer.setRtspPlaybackScale((double)speed);			
		play();
    }


    public void pause(){
        if (mediaPlayer.getState()  == MediaPlayer.PlayerState.Started) {
            mediaPlayer.Pause();
        }
    }

	public boolean isPaused(){
			return (mediaPlayer != null && (mediaPlayer.getState()	== MediaPlayer.PlayerState.Paused));
		}
	
    public void onPause(){
      btnPlayImage.setImageResource(R.drawable.ic_player_play);
      mediaPlayer.Pause();
    }

    public void onResume(){
        //setPtzPosition();
        if(playerState == PlayerStates.NONE) {
            return;
        }
        btnPlayImage.setImageResource(R.drawable.ic_player_pause);
        mediaPlayer.onResume();
    }

    public void close(){
        playerState = PlayerStates.Busy;
        mediaPlayer.Close();
        playerState = PlayerStates.ReadyForUse;
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        close();
    }

    private void enablePtz(final boolean on){
        handler.post(new Runnable() {
            @Override
            public void run() {
            	if (on == true)
	                btnPtz.setVisibility(VISIBLE);
				else 
					{
					btnPtz.setVisibility(INVISIBLE);
					cameraManipulator.hide();
					}	
            }
        });

    }

    private final float PTZ_STEP = 0.5f;
    private CameraManipulatorListener cameraManipulatorListener = new CameraManipulatorListener() {
        @Override
        public void onLeft() {
            mediaPlayer.PTZ_ContinuousMove(-PTZ_STEP,0.0f);
        }

        @Override
        public void onRight() {
            mediaPlayer.PTZ_ContinuousMove(PTZ_STEP,0.0f);
        }

        @Override
        public void onUp() {
            mediaPlayer.PTZ_ContinuousMove(0.0f,PTZ_STEP);
        }

        @Override
        public void onDown() {
            mediaPlayer.PTZ_ContinuousMove(0.0f,-PTZ_STEP);
        }

        @Override
        public void onZoomIn() {
            mediaPlayer.PTZ_Zoom(0.05f);
        }

        @Override
        public void onZoomOut() {
            mediaPlayer.PTZ_Zoom(-0.05f);
        }

		@Override
		public void onStop() {
			mediaPlayer.PTZStop(1,1);
		}


		
    };

    private MediaPlayer.MediaPlayerCallback playerCallback = new MediaPlayer.MediaPlayerCallback() {
        @Override
        public int OnReceiveData(ByteBuffer buffer, int size, long pts)
        {
            Log.e(TAG, "Form Native Player OnReceiveData: size: " + size + ", pts: " + pts);
            return 0;
        }


        // All event are sent to event handlers
        @Override
        public int Status(int arg) {
            MediaPlayer.PlayerNotifyCodes status = MediaPlayer.PlayerNotifyCodes.forValue(arg);
            if (handler == null || status == null)
                return 0;

            Log.e(TAG, "From Native Player status: " + arg);
            switch (MediaPlayer.PlayerNotifyCodes.forValue(arg))
            {
                case ONVIF_PTZ_IS_SUPPORTED:
                    enablePtz(true);
                    break;
                default:
                    Message msg = new Message();
                    msg.obj = status;
                    //handler.removeMessages(mOldMsg);
                    //mOldMsg = msg.what;
                    handler.sendMessage(msg);
            }

            return 0;
        }
    };


    private Handler handler = new Handler() {
        String strText = "Connecting";

        String sText;
        String sCode;

        @Override
        public void handleMessage(Message msg) {
            MediaPlayer.PlayerNotifyCodes status = (MediaPlayer.PlayerNotifyCodes) msg.obj;
            switch (status) {
                case PLP_PLAY_PAUSE:
                    playerState = PlayerStates.ReadyForUse;
					onVideoPaused();
                    break;
					
				case PLP_PLAY_PLAY:
					playerState = PlayerStates.ReadyForUse;
					onVideoPlayed();
					break;

                case CP_CONNECT_STARTING:
                   playerState = PlayerStates.ReadyForUse;
                   onStartPrepare();
                    break;

                case PLP_BUILD_SUCCESSFUL:
                    playerState = PlayerStates.ReadyForUse;
                    sText = mediaPlayer.getPropString(MediaPlayer.PlayerProperties.PP_PROPERTY_PLP_RESPONSE_TEXT);
                    sCode = mediaPlayer.getPropString(MediaPlayer.PlayerProperties.PP_PROPERTY_PLP_RESPONSE_CODE);
                    Log.i(TAG, "=Status PLP_BUILD_SUCCESSFUL: Response sText="+sText+" sCode="+sCode);
                    break;

                case VRP_NEED_SURFACE:
                    playerState = PlayerStates.ReadyForUse;

                    break;

                case PLP_PLAY_SUCCESSFUL:
                    playerState = PlayerStates.Busy;
                    onVideoPlayed();
                    break;
                case PLP_CLOSE_STARTING:
                    playerState = PlayerStates.Busy;

                    break;

                case PLP_CLOSE_SUCCESSFUL:
                    playerState = PlayerStates.Busy;
                    System.gc();
                    break;

                case PLP_CLOSE_FAILED:
                    playerState = PlayerStates.Busy;
                    break;

                case CP_CONNECT_FAILED:
                    playerState = PlayerStates.Busy;
                    break;

                case PLP_BUILD_FAILED:
                    playerState = PlayerStates.Busy;
                    sText = mediaPlayer.getPropString(MediaPlayer.PlayerProperties.PP_PROPERTY_PLP_RESPONSE_TEXT);
                    sCode = mediaPlayer.getPropString(MediaPlayer.PlayerProperties.PP_PROPERTY_PLP_RESPONSE_CODE);
                    Log.i(TAG, "=Status PLP_BUILD_FAILED: Response sText="+sText+" sCode="+sCode);
                    break;
                case PLP_PLAY_FAILED:
                    playerState = PlayerStates.ReadyForUse;
                    break;

                case PLP_ERROR:
                    playerState = PlayerStates.ReadyForUse;
                    break;
                case CP_INTERRUPTED:
                    playerState = PlayerStates.ReadyForUse;
                    break;
                case CP_RECORD_STARTED:
                    Log.v(TAG, "=handleMessage CP_RECORD_STARTED");{
                    String sFile = mediaPlayer.RecordGetFileName(1);

                }
                break;

                case CP_RECORD_STOPPED:
                    Log.v(TAG, "=handleMessage CP_RECORD_STOPPED");
                    String sFile = mediaPlayer.RecordGetFileName(0);
                break;

                //case CONTENT_PROVIDER_ERROR_DISCONNECTED:
                case CP_STOPPED:
                case VDP_STOPPED:
                case VRP_STOPPED:
                case ADP_STOPPED:
                case ARP_STOPPED:
                    if (playerState != PlayerStates.Busy) {
                       close();
                    }
                    break;

                case CP_ERROR_NODATA_TIMEOUT:

                    break;

                case PLP_TRIAL_VERSION:
                case PLP_EOS:
                case CP_ERROR_DISCONNECTED:
                    if (playerState != PlayerStates.Busy) {
                       close();
                    }
                    break;
				case PLP_SEEK_COMPLETED:
					enabled_change_position = 1;
					Log.e(TAG, "PLP_SEEK_COMPLETED:" + enabled_change_position);
					break;
            }
        }
    };


    private  OnSwipeTouchListener swipeTouchListener = new OnSwipeTouchListener(context) {
        public void pinchMove(boolean isGrow) {
            if (DebugGuard.LOG) Log.i(TAG, "=pinchMove grow:" + isGrow);
            mediaPlayer.handleZoom(isGrow);
        }

    public void touchDown(int x, int y, int rawx, int rawy) {
        if (DebugGuard.LOG) Log.i(TAG, "=touchDown x:" + x + " y:" + y);
        mediaPlayer.handleTouchBegin(rawx, rawy);
    }

    public void touchUp(int x, int y, int rawx, int rawy) {
        if (DebugGuard.LOG) Log.i(TAG, "=touchUp x:" + x + " y:" + y);
        mediaPlayer.handleTouchEnd(rawx, rawy);
    }

    public void touchMove(int x, int y, int rawx, int rawy) {
        if (DebugGuard.LOG) Log.i(TAG, "=touchMove x:" + x + " y:" + y);
        mediaPlayer.handleMoveContinue(rawx, rawy);
    }

};


}
