package veg.mediaplayer.sdk.test.smoothswitch;

import java.nio.ByteBuffer;

import veg.mediaplayer.sdk.MediaPlayer;
import veg.mediaplayer.sdk.MediaPlayer.PlayerNotifyCodes;
import veg.mediaplayer.sdk.MediaPlayer.PlayerState;
import veg.mediaplayer.sdk.MediaPlayerConfig;
import android.app.Fragment;
import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout;

public class Player extends Fragment implements MediaPlayer.MediaPlayerCallback, SurfaceHolder.Callback2
{
	private Context		   context = null;
	private PlayerCallback callback = null;
    private MediaPlayer    player = null;
    private SurfaceView    surface = null;
	private SmoothFrameLayout parent = null;
    
    private int    		   id = 0;
	
	private int mOldMsg = 0;
	
    public enum PlayerType
    {
        None,
        Main,
        MainButNeedCloseLater,
        Standby,
    };

    public enum PlayerState
    {
        Closed,

        Opening,
        Working,
        Closing
    };

    private PlayerState state = PlayerState.Closed;
    private PlayerType type = PlayerType.None;
	
	public abstract interface PlayerCallback 
	{
		public void onPlayerFirstVideoFrameAvailable(final int idPlayer);
		public void onPlayerSurfaceCreated(final int idPlayer);
	}
	
	public Player(final Context context, final MediaPlayer player, final SurfaceView surface, final PlayerCallback callback, final int id, final SmoothFrameLayout parent)
	{
		this.context = context;
		this.callback = callback;
		this.player = player;
		this.surface = surface;
		this.parent = parent;
		
		if (this.surface != null)
		{
			this.surface.getHolder().addCallback(this);
		}
		
		this.id = id;
	}
	
    public int getPlayerId() { return id; }
	
    public SurfaceView getSurfaceView() { return surface; }
	public void setSurfaceView(final SurfaceView surface)
	{
		this.surface = surface;
		if (this.surface != null)
			this.surface.getHolder().addCallback(this);
	}
	
	public void Open(final String url) 
	{
		if (player == null || url == null || url.isEmpty())
			return;

		Log.v(TAG, "=>Open player " + url);
//		player.Close();

        state = PlayerState.Opening;
        type = PlayerType.Main;
		
		SharedSettings.getInstance().loadPrefSettings();

		SharedSettings sett = SharedSettings.getInstance();
  	    	
    	MediaPlayerConfig conf = new MediaPlayerConfig();
    	conf.setConnectionUrl(url);
		
    	conf.setConnectionNetworkProtocol(sett.connectionProtocol);
    	conf.setConnectionDetectionTime(1000);
    	conf.setConnectionBufferingTime(200);
    	conf.setDecodingType(1);
    	conf.setRendererType(1);
    	conf.setSynchroEnable(0);
    	conf.setSynchroNeedDropVideoFrames(0);
    	conf.setEnableColorVideo(1);
    	conf.setEnableAspectRatio(sett.rendererEnableAspectRatio);
    	conf.setDataReceiveTimeout(30000);
        conf.setNumberOfCPUCores(0);

        //conf.setVsyncEnable(1);
        
        if (surface != null)
        {
    		Log.i(TAG, "setSurface: " + surface.getHolder() + ", pointer " + this);
    		player.setSurface(surface.getHolder().getSurface());
        }
        
		Log.v(TAG, " native Open player " + url);
     	player.Open(conf, this);
	}

	public void OpenAsStandby(final String url) 
	{
		if (player == null || url == null || url.isEmpty())
			return;

		Log.v(TAG, "=>Open player as standby" + url);
//		player.Close();

        state = PlayerState.Opening;
        type = PlayerType.Standby;
		
		SharedSettings.getInstance().loadPrefSettings();

		SharedSettings sett = SharedSettings.getInstance();
  	    	
    	MediaPlayerConfig conf = new MediaPlayerConfig();
    	conf.setConnectionUrl(url); 
		
    	conf.setConnectionNetworkProtocol(sett.connectionProtocol);
    	conf.setConnectionDetectionTime(1000);
    	conf.setConnectionBufferingTime(200); 
    	conf.setDecodingType(sett.decoderType);
    	conf.setRendererType(1);
    	conf.setSynchroEnable(1);
    	conf.setSynchroNeedDropVideoFrames(0);
    	conf.setEnableColorVideo(1);
    	conf.setEnableAspectRatio(sett.rendererEnableAspectRatio);
    	conf.setDataReceiveTimeout(30000);
        conf.setNumberOfCPUCores(0);
 
        //conf.setVsyncEnable(1);
        
        if (surface != null)
        {
    		Log.i(TAG, "setSurface: " + surface.getHolder() + ", pointer " + this);

    		if (parent.indexOfChild(surface) == -1)
    		{
    			parent.needForAdd = surface;
    			parent.requestLayout();;
    		}
    		
        	player.setSurface(surface.getHolder().getSurface());
        }
        
		Log.v(TAG, " native Open player standby" + url);
     	player.Open(conf, this);
	}
	
	public void Close() 
	{
		if (player == null)
			return;

        state = PlayerState.Closing;
		
        player.Close();

		type = PlayerType.None;
        state = PlayerState.Closed;
		
	}
	
    public void show()
    {
        if (player == null)
            return;

        Log.d(TAG, "show: type " + type + ", pointer " + this);
        //if (player.getVisibility() != View.VISIBLE)
        {
        	type = PlayerType.Main;
        }
    }

    public void hide()
    {
        if (player == null)
            return;

        Log.d(TAG, "hide: type " + type + ", pointer " + this);
        //if (player.getVisibility() != View.INVISIBLE)
        {
        	type = PlayerType.MainButNeedCloseLater;
        	
			parent.needForRemove = surface;
			parent.requestLayout();;
        }
    }
	

	public MediaPlayer getPlayer()
	{
		return player;
	}

    public PlayerState getState() { return state; }
    public PlayerType getType() { return type; }
	
	public MediaPlayer.PlayerState getMediaPlayerState()
	{
		return (player == null) ? MediaPlayer.PlayerState.Closed : player.getState();
	}
	
    public boolean isPlayerBusy()
    {
    	if(player != null && (player.getState() == MediaPlayer.PlayerState.Closing || 
    								player.getState() == MediaPlayer.PlayerState.Opening))
    	{
    		return true;
    	}
    	return false;
    }
    
    public boolean isPlayerStarted()
    {
    	MediaPlayer.PlayerState state = (player == null) ? MediaPlayer.PlayerState.Closed : player.getState();
		if(	state == MediaPlayer.PlayerState.Paused || state == MediaPlayer.PlayerState.Started)
		{
			return true;
		}
		return false;
    }
	
	private Handler handler = new Handler() 
    {
		@Override
	    public void handleMessage(Message msg) 
	    {
	    	PlayerNotifyCodes status = (PlayerNotifyCodes) msg.obj;
	        switch (status) 
	        {
	        	case CP_CONNECT_STARTING:
	    			break;
	                
		    	case PLP_PLAY_SUCCESSFUL:
			        break;
	                
	        	case PLP_CLOSE_STARTING:
	    			break;
	                
	        	case PLP_CLOSE_SUCCESSFUL:
	    			System.gc();
	                break;
	                
	        	case PLP_CLOSE_FAILED:
	        		break;
	               
	        	case CP_CONNECT_FAILED:
	    			break;
	                
	            case PLP_BUILD_FAILED:
	    			break;
	                
	            case PLP_PLAY_FAILED:
	    			break;
	                
	            case PLP_ERROR:
	            {
	            	Close();
	            	break;
	            }
	                
	            case CP_INTERRUPTED:
	    			break;
	                
	            //case CONTENT_PROVIDER_ERROR_DISCONNECTED:
	            case CP_STOPPED:
	            case VDP_STOPPED:
	            case VRP_STOPPED:
	            case ADP_STOPPED:
	            case ARP_STOPPED:
	                break;
	
	            case PLP_EOS:
	                break;
	                
	            case CP_ERROR_DISCONNECTED:
	                break;
	                
	            case VDP_CRASH:
	            {
	            	Close();
	            	break;
	            }
	                
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
	    	case VRP_FIRSTFRAME:
	    		if (callback != null)
	    			callback.onPlayerFirstVideoFrameAvailable(id);
	    		
				break;

			// for asynchronous process
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
	public void surfaceCreated(SurfaceHolder holder) 
	{
		Log.i(TAG, "surfaceCreated: " + holder + ", pointer " + this);
		if (player == null)
			return;

//		surface.setZOrderOnTop(false);
		player.setSurface(holder.getSurface());
		if (callback != null) 
			callback.onPlayerSurfaceCreated(id);
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) 
	{
		Log.i(TAG, "surfaceChanged: " + holder + ", pointer " + this);
		if (player == null)
			return;

		//player.setSurface(holder.getSurface(), width, height);
		player.UpdateView();

	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) 
	{
		Log.i(TAG, "surfaceDestroyed: " + holder + ", pointer " + this);
		if (player == null)
			return;

		player.setSurface(null);
	}

	@Override
	public void surfaceRedrawNeeded(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		
	}
    
    private static final String TAG = "Player";

}

