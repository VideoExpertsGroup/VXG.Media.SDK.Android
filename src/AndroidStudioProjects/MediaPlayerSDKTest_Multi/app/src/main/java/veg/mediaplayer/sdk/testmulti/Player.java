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

class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener 
{
	public static final float MIN_ZOOM = 1f;
	public static final float MAX_ZOOM = 5f;
	public float scaleFactor = 1.f;
	public boolean zoom = false;
	
	@Override
	public boolean onScale(ScaleGestureDetector detector) 
	{
		scaleFactor *= detector.getScaleFactor();
		scaleFactor = Math.max(MIN_ZOOM, Math.min(scaleFactor, MAX_ZOOM));
		//Log.e("Player", "onScale " + scaleFactor);
		return true;
	}
	@Override
	 public boolean onScaleBegin(ScaleGestureDetector detector) 
	{
		//Log.e("Player", "onScaleBegin");
		zoom = true;
		return true;
	 }

	 @Override
	 public void onScaleEnd(ScaleGestureDetector detector) 
	 {
		//Log.e("Player", "onScaleEnd");
		zoom = false;
	 }
}

class ViewSizes
{
	public float dx = 0;
	public float dy = 0;
	
	public int orig_width = 0;
	public int orig_height = 0;
	
	public ScaleListener listnrr = null;
	
}

class Player extends FrameLayout implements MediaPlayer.MediaPlayerCallback,  View.OnTouchListener
{
    private static final String TAG = "Player Instance";
    
	//public String  				url = "";
	public int 					view_id = 0;

	public ScaleGestureDetector detectors 		= null;	
	public ViewSizes 			mSurfaceSizes 	= null;
	
	private Context 			context = null;
    private MediaPlayer			player = null;
    private TextView 			playerStatusText = null;
    private TextView 			playerHwStatus = null;
	
	private StatusProgressTask 	mProgressTask = null;
	
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
		player.getSurfaceView().setZOrderOnTop(true);    // necessary
		SurfaceHolder sfhTrackHolder = player.getSurfaceView().getHolder();
		sfhTrackHolder.setFormat(PixelFormat.TRANSPARENT);
		
		if (player != null)
		{
			player.Close();

			SharedSettings sett = SharedSettings.getInstance();
			boolean bPort = (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT);
	    	int aspect = bPort ? 1 : sett.rendererEnableAspectRatio;
	    	
     	    player.Open(url, sett.connectionProtocol, sett.connectionDetectionTime, sett.connectionBufferingTime,
     	    				sett.decoderType, sett.rendererType, sett.synchroEnable, 
	    					sett.synchroNeedDropVideoFrames, sett.rendererEnableColorVideo, aspect, player.getConfig().getDataReceiveTimeout(), 0, this);
		}
		
//        ViewSizes sz = new ViewSizes();
//        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) view.getLayoutParams();
//        sz.orig_width = lp.width;
//        sz.orig_height = lp.height;
//        //sz.listnrr = new ScaleListener();
//        mSurfaceSizes = sz;
//        
//        //detectors = new ScaleGestureDetector(view.getContext(), sz.listnrr);
//        view.setOnTouchListener(this);  
    	
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
	
	@Override
	public boolean onTouch(View view, MotionEvent event) 
	{
		if (detectors != null)
			detectors.onTouchEvent(event);
		
	    switch (event.getAction()) 
	    {
	        case MotionEvent.ACTION_DOWN:
	        	mSurfaceSizes.dx =  event.getX();
	        	mSurfaceSizes.dy =  event.getY();
	            break;
	
	        case MotionEvent.ACTION_MOVE:
	            float x =  event.getX();
	            float y =  event.getY();
	            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) view.getLayoutParams();
	            float left = lp.leftMargin + (x - mSurfaceSizes.dx); 
	            float top = lp.topMargin + (y - mSurfaceSizes.dy);
	            if (mSurfaceSizes.listnrr != null && mSurfaceSizes.listnrr.zoom)
	            {
	            	int srcw = lp.width;
	            	int srch = lp.height;
	            	
		    		//Log.e("Player", "ACTION_MOVE1 " + lp.width + "," + mSurfaceSizes.orig_width + "," + mSurfaceSizes.listnrr.scaleFactor);
		    		
		    		int left_offset = (int) (mSurfaceSizes.orig_width - (mSurfaceSizes.orig_width * mSurfaceSizes.listnrr.scaleFactor));
		    		int top_offset = (int) (mSurfaceSizes.orig_height - (mSurfaceSizes.orig_height * mSurfaceSizes.listnrr.scaleFactor));
		    		Log.e("Player", "ACTION_MOVE2 " + left_offset + "," + top_offset);
		    		//lp.setMargins(left_offset, top_offset, left_offset, top_offset);
		    		
	                lp.leftMargin = left_offset;
	                lp.topMargin  = top_offset;
	                lp.rightMargin = left_offset;
	                lp.bottomMargin  = top_offset;
		    		
		    		
	                //lp.width = (int) (mSurfaceSizes.orig_width * mSurfaceSizes.listnrr.scaleFactor);
		            //lp.height  = (int) (mSurfaceSizes.orig_height  * mSurfaceSizes.listnrr.scaleFactor);
	
	                //lp.leftMargin -= (lp.width - srcw) / 2;
	                //lp.topMargin  -= (lp.height - srch) / 2;
	            }
//	            else
//	            {
//	                lp.leftMargin = (int)left;
//	                lp.topMargin = (int)top;
//	            }
	            view.setLayoutParams(lp);
	            //view.requestLayout();
	            break;
	    }	    
	    //view.invalidate();
	    return true;
	}
}

