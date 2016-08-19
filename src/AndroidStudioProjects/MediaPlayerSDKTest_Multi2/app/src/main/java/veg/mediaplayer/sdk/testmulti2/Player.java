package veg.mediaplayer.sdk.testmulti2;

import java.nio.ByteBuffer;

import veg.mediaplayer.sdk.MediaPlayer;
import veg.mediaplayer.sdk.MediaPlayer.PlayerNotifyCodes;
import veg.mediaplayer.sdk.MediaPlayer.PlayerState;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class Player extends Fragment implements MediaPlayer.MediaPlayerCallback
{
    private static final String TAG = "Player Instance";
    
	public int 					view_id = 0;

	private Context 			context = null;
    private MediaPlayer			player = null;
    private TextView 			playerStatusText = null;
    private TextView 			playerHwStatus = null;
	
	private StatusProgressTask 	mProgressTask = null;
	
	private int mOldMsg = 0;
	private Object waitOnMe = new Object();
	
	public abstract interface PlayerCallback 
	{
		public void ViewReady();
	}
	
	public PlayerCallback callback = null;

    @Override
    public void onAttach(Activity activity) 
    {
        super.onAttach(activity);
        context = activity;
    }
    
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
	{
		Log.v(TAG, "=>onCreateView");
		
		View view = inflater.inflate(R.layout.player, container, false);
		playerStatusText = (TextView) view.findViewById(R.id.playerStatusText);
		playerHwStatus = (TextView) view.findViewById(R.id.playerHwStatus);
		
		player = (MediaPlayer) view.findViewById(R.id.playerView);
		
		if (callback != null)
			callback.ViewReady();
		
		return view;
	}

	public void Open(final String url) 
	{
		Log.v(TAG, "=>Open player " + url);
		if (player != null)
		{
			player.Close();

			SharedSettings sett = SharedSettings.getInstance();
			boolean bPort = (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT);
	    	int aspect = bPort ? 1 : sett.rendererEnableAspectRatio;
	    	
			Log.v(TAG, " native Open player " + url);
     	    player.Open(url, sett.connectionProtocol, sett.connectionDetectionTime, sett.connectionBufferingTime,
     	    				sett.decoderType, sett.rendererType, sett.synchroEnable, 
	    					sett.synchroNeedDropVideoFrames, sett.rendererEnableColorVideo, aspect, player.getConfig().getDataReceiveTimeout(), 0, this);
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
	        		startProgressTask(strText);
	    			break;
	                
		    	case VRP_NEED_SURFACE:
		    		showVideoView();
			        synchronized (waitOnMe) { waitOnMe.notifyAll(); }
					break;
	
		    	case PLP_PLAY_SUCCESSFUL:
	        		stopProgressTask();
	    			playerStatusText.setText("");
	    			//playerHwStatus.setText("Hardware");
		     		player.setAlpha(1.0f);
			        break;
	                
	        	case PLP_CLOSE_STARTING:
	        		stopProgressTask();
	    			playerStatusText.setText("Disconnected");
	    			showStatusView();
	    			break;
	                
	        	case PLP_CLOSE_SUCCESSFUL:
	        		stopProgressTask();
	    			playerStatusText.setText("Disconnected");
	    			showStatusView();
	    			System.gc();
	                break;
	                
	        	case PLP_CLOSE_FAILED:
	        		stopProgressTask();
	    			playerStatusText.setText("Disconnected");
	    			showStatusView();
	   			break;
	               
	        	case CP_CONNECT_FAILED:
	        		stopProgressTask();
	    			playerStatusText.setText("Disconnected");
	    			showStatusView();
	    			break;
	                
	            case PLP_BUILD_FAILED:
	        		stopProgressTask();
	    			playerStatusText.setText("Disconnected");
	    			showStatusView();
	    			break;
	                
	            case PLP_PLAY_FAILED:
	        		stopProgressTask();
	    			playerStatusText.setText("Disconnected");
	    			showStatusView();
	    			break;
	                
	            case PLP_ERROR:
	        		stopProgressTask();
	    			playerStatusText.setText("Disconnected");
	    			showStatusView();
	    			break;
	                
	            case CP_INTERRUPTED:
	        		stopProgressTask();
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
		        		stopProgressTask();
	            		player.Close();
	        			playerStatusText.setText("Disconnected");
		    			showStatusView();
	            	}
	                break;
	
	            case PLP_EOS:
	            	if (!isPlayerBusy())
	            	{
		        		stopProgressTask();
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

 		SurfaceHolder sfhTrackHolder = player.getSurfaceView().getHolder();
		sfhTrackHolder.setFormat(PixelFormat.TRANSPARENT);
	}
    
	private void startProgressTask(String text)
	{
		stopProgressTask();
	    
	    mProgressTask = new StatusProgressTask(context, text);
	    executeAsyncTask(mProgressTask, text);
	}
	
	private void stopProgressTask()
	{
		playerStatusText.setText("");
		
       	if (mProgressTask != null)
	    {
       		mProgressTask.stopTask();
	    	mProgressTask.cancel(true);
	    }
	}

	private class StatusProgressTask extends AsyncTask<String, Void, Boolean> 
    {
		private String strProgressTextSrc;
       	private String strProgressText;
       	private Rect bounds = new Rect();
        private boolean stop = false;
    	private Context context = null;
    	
       	public StatusProgressTask(Context context, String text)
       	{
        	stop = false;
       		strProgressTextSrc = text;
    		this.context = context;
       	}
       	
       	public void stopTask() { stop = true; }
       	
        @Override
        protected void onPreExecute() 
        {
        	super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(String... params) 
        {
            try 
            {
                if (stop) return true;

                String maxText = "Disconnected.....";//strProgressTextSrc + "....";
                int len = maxText.length();
            	playerStatusText.getPaint().getTextBounds(maxText, 0, len, bounds);

               	strProgressText = strProgressTextSrc + "...";
                
            	Runnable uiRunnable = null;
                uiRunnable = new Runnable()
                {
                    public void run()
                    {
                        if (stop) return;

    	                playerStatusText.setText(strProgressText);
    	            	
//    	            	RelativeLayout.LayoutParams layoutParams = 
//    	            		    (RelativeLayout.LayoutParams)playerStatusText.getLayoutParams();
//    	           		
//    	           		layoutParams.width = bounds.width();
//    	           		playerStatusText.setLayoutParams(layoutParams);        	
//    	            	playerStatusText.setGravity(Gravity.NO_GRAVITY);
    	            	
                        synchronized(this) { this.notify(); }
                    }
                };
                
               	int nCount = 4;
              	do
            	{
                    try
                    {
                    	Thread.sleep(300);
                    }
                    catch ( InterruptedException e ) { stop = true; }
                   
                    if (stop) break;
                    
                	if (nCount <= 3)
                	{
                		strProgressText = strProgressTextSrc;
                		for (int i = 0; i < nCount; i++)
                			strProgressText = strProgressText + ".";
                	}
                    
                    synchronized ( uiRunnable )
                    {
                    	((Activity) context).runOnUiThread(uiRunnable);
                        try
                        {
                            uiRunnable.wait();
                        }
                        catch ( InterruptedException e ) { stop = true; }
                    }
                    
                    if (stop) break;
                    
                    nCount++;
                    if (nCount > 3)
                    {
                    	nCount = 1;
                    	strProgressText = strProgressTextSrc;
                    }
            	}
              	
            	while(!isCancelled());
            } 
            catch (Exception e) 
            {
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) 
        {
            super.onPostExecute(result);
            mProgressTask = null;
        }
        @Override
        protected void onCancelled() 
        {
            super.onCancelled();
        }
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

