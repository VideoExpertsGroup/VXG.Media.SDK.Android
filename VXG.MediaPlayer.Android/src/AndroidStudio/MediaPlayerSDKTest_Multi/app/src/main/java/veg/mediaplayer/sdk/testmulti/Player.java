package veg.mediaplayer.sdk.testmulti;

import java.nio.ByteBuffer;

import veg.mediaplayer.sdk.MediaPlayer;
import veg.mediaplayer.sdk.MediaPlayer.PlayerNotifyCodes;
import veg.mediaplayer.sdk.MediaPlayer.PlayerState;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

class Player extends FrameLayout implements MediaPlayer.MediaPlayerCallback
{
    private static final String TAG = "Player Instance";
    
	//public String  				url = "";
	public int 					view_id = 0;

	public ScaleGestureDetector detectors 		= null;	

	private Context 			context = null;
    private MediaPlayer			player = null;
    private TextView 			playerStatusText = null;
    private TextView 			playerHwStatus = null;
	

	private int mOldMsg = 0;
	private Object waitOnMe = new Object();
	
	public Player(Context context)
	{
        super(context);
		this.context = context;

		player = new MediaPlayer(context);
		playerStatusText = new TextView(context);
		playerHwStatus = new TextView(context);
		
		addView(player, new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, 300));
		addView(playerHwStatus, new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		addView(playerStatusText, new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
	}
	
	public Player(Context context, AttributeSet attr)
	{
        super(context, attr);
 		this.context = context;

		player = new MediaPlayer(context);
		playerStatusText = new TextView(context);
		playerHwStatus = new TextView(context);
		
		addView(player, new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, 300));
		addView(playerHwStatus, new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		addView(playerStatusText, new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
	}
	
	public void Open(final String url) 
	{

		//SurfaceHolder sfhTrackHolder = player.getSurfaceView().getHolder();
		//sfhTrackHolder.setFormat(PixelFormat.TRANSPARENT);
		
		if (player != null)
		{
			player.Close();

			SharedSettings sett = SharedSettings.getInstance();

	    	
     	    player.Open(url, sett.connectionProtocol, sett.connectionDetectionTime, sett.connectionBufferingTime,
     	    				sett.decoderType, 1, sett.synchroEnable,
	    					sett.synchroNeedDropVideoFrames, 1, sett.rendererEnableAspectRatio, player.getConfig().getDataReceiveTimeout(), 0, this);
		}
		

	}

	public void Close() 
	{
		if (player != null)
		{
			player.Close();
		}
	}

	public MediaPlayer getPlayer()
	{
		return player;
	}

	public PlayerState getPlayerState()
	{
		return (player == null) ? PlayerState.Closed : player.getState();
	}
	
    public boolean isPlayerBusy()
    {
    	if(player != null && (player.getState() == PlayerState.Closing || 
    								player.getState() == PlayerState.Opening))
    	{
    		return true;
    	}
    	return false;
    }
    
    public boolean isPlayerStarted()
    {
		PlayerState state = (player == null) ? PlayerState.Closed : player.getState();
		if(	state == PlayerState.Paused || state == PlayerState.Started)
		{
			return true;
		}
		return false;
    }
	
	private Handler handler = new Handler() 
    {
		String strText = "Connecting";
		
		@Override
	    public void handleMessage(Message msg) 
	    {
	    	PlayerNotifyCodes status = (PlayerNotifyCodes) msg.obj;
	        switch (status) 
	        {
	        	case CP_CONNECT_STARTING:
	    			showStatusView();
	    			break;
	                
		    	case VRP_NEED_SURFACE:
		    		showVideoView();
			        synchronized (waitOnMe) { waitOnMe.notifyAll(); }
					break;
	
		    	case PLP_PLAY_SUCCESSFUL:playerStatusText.setText("");
	    			//playerHwStatus.setText("Hardware");
		     		player.setAlpha(1.0f);
			        break;
	                
	        	case PLP_CLOSE_STARTING:
	    			playerStatusText.setText("Disconnected");
	    			showStatusView();
	    			break;
	                
	        	case PLP_CLOSE_SUCCESSFUL:
	    			playerStatusText.setText("Disconnected");
	    			showStatusView();
	    			System.gc();
	                break;
	                
	        	case PLP_CLOSE_FAILED:
	    			playerStatusText.setText("Disconnected");
	    			showStatusView();
	   			break;
	               
	        	case CP_CONNECT_FAILED:
	    			playerStatusText.setText("Disconnected");
	    			showStatusView();
	    			break;
	                
	            case PLP_BUILD_FAILED:
	    			playerStatusText.setText("Disconnected");
	    			showStatusView();
	    			break;
	                
	            case PLP_PLAY_FAILED:
	    			playerStatusText.setText("Disconnected");
	    			showStatusView();
	    			break;
	                
	            case PLP_ERROR:
	    			playerStatusText.setText("Disconnected");
	    			showStatusView();
	    			break;
	                
	            case CP_INTERRUPTED:
	    			playerStatusText.setText("Disconnected");
	    			showStatusView();
	    			break;
	                
	            //case CONTENT_PROVIDER_ERROR_DISCONNECTED:
	            case CP_STOPPED:
	            case VDP_STOPPED:
	            case VRP_STOPPED:
	            case ADP_STOPPED:
	            case ARP_STOPPED:
	            	if (!isPlayerBusy())
	            	{
	            		player.Close();
	        			playerStatusText.setText("Disconnected");
		    			showStatusView();
	            	}
	                break;
	
	            case PLP_EOS:
	            	if (!isPlayerBusy())
	            	{
	            		player.Close();
	        			playerStatusText.setText("Disconnected");
		    			showStatusView();
	            	}
	                break;
	                
	            case CP_ERROR_DISCONNECTED:
	            	if (!isPlayerBusy())
	            	{
	            		player.Close();
	        			playerStatusText.setText("Disconnected");
		    			showStatusView();
	            	}
	                break;
	                
	            default:
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

	@Override
	public int Status(int arg)
	{
		
		PlayerNotifyCodes status = PlayerNotifyCodes.forValue(arg);
		if (handler == null)
			return 0;
		
		Log.e(TAG, "Form Native Player status: " + arg);
	    switch (PlayerNotifyCodes.forValue(arg)) 
	    {
	    	// for synchronus process
			//case PLAY_SUCCESSFUL:
	    	case VRP_NEED_SURFACE:
	    		synchronized (waitOnMe) 
	    		{
					Message msg = new Message();
					msg.obj = status;
					handler.sendMessage(msg);
	    		    try 
	    		    {
	    		        waitOnMe.wait();
	    		    }
	    		    catch (InterruptedException e) {}
	    		}			
				break;
	            
			// for asynchronus process
	        default:     
				Message msg = new Message();
				msg.obj = status;
				handler.removeMessages(mOldMsg);
				mOldMsg = msg.what;
				handler.sendMessage(msg);
	    }
	    
		return 0;
	}
	
	private void showStatusView() 
	{
		player.setVisibility(View.INVISIBLE);
		playerHwStatus.setVisibility(View.INVISIBLE);
		player.setAlpha(0.0f);
		playerStatusText.setVisibility(View.VISIBLE);
		
	}
	
	private void showVideoView() 
	{
		playerStatusText.setVisibility(View.INVISIBLE);
 		player.setVisibility(View.VISIBLE);
		playerHwStatus.setVisibility(View.VISIBLE);

 		//SurfaceHolder sfhTrackHolder = player.getSurfaceView().getHolder();
		//sfhTrackHolder.setFormat(PixelFormat.TRANSPARENT);
	}
    



    static public <T> void executeAsyncTask(AsyncTask<T, ?, ?> task, T... params) 
    {
    	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) 
    	{
    		task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params);
    	}
    	else 
    	{
    		task.execute(params);
    	}
    }  
	

}

