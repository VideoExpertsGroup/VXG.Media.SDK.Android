/*
 *
  * Copyright (c) 2011-2017 VXG Inc.
 *
 */


package veg.mediaplayer.sdk.test.smoothswitch;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.content.SharedPreferences;
import android.view.*;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ToggleButton;

import java.util.ArrayList;

import android.preference.PreferenceManager;
import veg.mediaplayer.sdk.DebugGuard;
import veg.mediaplayer.sdk.MediaPlayer;
import veg.mediaplayer.sdk.test.smoothswitch.R;
import veg.mediaplayer.sdk.test.smoothswitch.Player.PlayerCallback;

public class MainActivity extends Activity implements PlayerCallback
{
 
	private ArrayList<Player> players = new ArrayList<Player>();
	private ArrayList<String> currentUrls = new ArrayList<String>();;

	private int currentUrlIndex = 0;
	
	private AutoCompleteTextView textviewCurrentUrl;
	private Button buttonPrev;
	private Button buttonPlay;
	private Button buttonNext;
    private ToggleButton buttonVsync;

	private SmoothFrameLayout layoutSurfaceHolder;
	
	protected void loadUrls()
	{


        currentUrls.add("http://devimages.apple.com/iphone/samples/bipbop/gear1/prog_index.m3u8");
        currentUrls.add("rtsp://184.72.239.149/vod/BigBuckBunny_115k.mov");

	}
	
	@Override
    public void onCreate(Bundle savedInstanceState) 
	{
		setTitle(R.string.app_name);
		super.onCreate(savedInstanceState);

        getWindow().requestFeature(Window.FEATURE_PROGRESS);
		getWindow().setFeatureInt(Window.FEATURE_PROGRESS, Window.PROGRESS_VISIBILITY_ON);

		setContentView(R.layout.main);

		buttonPlay = (Button)findViewById(R.id.buttonPlay);
		layoutSurfaceHolder = (SmoothFrameLayout)findViewById(R.id.layoutSurfaceHolder);
		layoutSurfaceHolder.setFocusable(true);

		
		SharedSettings.getInstance(this).loadPrefSettings();
		SharedSettings.getInstance().savePrefSettings();
		
        loadUrls();
		
		players.add(new Player(this, new MediaPlayer(this, false), (SurfaceView)findViewById(R.id.surfaceView1), this, 0, layoutSurfaceHolder));
		players.add(new Player(this, new MediaPlayer(this, false), (SurfaceView)findViewById(R.id.surfaceView2), this, 1, layoutSurfaceHolder));
		
		textviewCurrentUrl = (AutoCompleteTextView)findViewById(R.id.textviewCurrentUrl);
		textviewCurrentUrl.setText(currentUrls.get(currentUrlIndex));
		
		buttonPlay.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
            	if (getMainPlayer() == null && getFreePlayer() != null)
            		getFreePlayer().Open(currentUrls.get(currentUrlIndex));
            }
        });

		buttonPrev = (Button)findViewById(R.id.buttonPrev);
		buttonPrev.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
            	if (getMainPlayer() != null)
            	{
                	if (getFreePlayer() != null)
                		getFreePlayer().OpenAsStandby(getPrevUrl());
            	}
            	
            }
        });

		buttonNext = (Button)findViewById(R.id.buttonNext);
		buttonNext.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
            	if (getMainPlayer() != null)
            	{
                	if (getFreePlayer() != null)
                		getFreePlayer().OpenAsStandby(getNextUrl());
            	}
            }
        });

		buttonVsync = (ToggleButton)findViewById(R.id.buttonVsync);
        buttonVsync.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                if (getMainPlayer() != null)
                {
                    int vsyncEnabled = getMainPlayer().getPlayer().getConfig().getVsyncEnable();
                    Log.e(TAG, "vsyncEnabled " + vsyncEnabled);
                    
                    vsyncEnabled ^= (1 << 0);
                    getMainPlayer().getPlayer().getConfig().setVsyncEnable(vsyncEnabled);

                    Log.e(TAG, "vsyncEnabled " + getMainPlayer().getPlayer().getConfig().getVsyncEnable());
                    buttonVsync.setChecked(getMainPlayer().getPlayer().getConfig().getVsyncEnable() != 0);
                }
            }
        });
		
    }
 
    @Override
    public void onBackPressed() 
    {
  		super.onBackPressed();
    }
  	
  	@Override
  	protected void onDestroy() 
  	{
  		Log.e(TAG, "onDestroy()");
  		closePlayers();
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
			case R.id.main_opt_exit:     
				finish();
				break;

		}
		return true;
	}

	protected String getNextUrl()
	{
		if (currentUrls.size() <= 0)
			return "";
		
		currentUrlIndex++;
		if (currentUrlIndex >= currentUrls.size())
			currentUrlIndex = 0;

		return currentUrls.get(currentUrlIndex);
	}

	protected String getPrevUrl()
	{
		if (currentUrls.size() <= 0)
			return "";
		
		currentUrlIndex--;
		if (currentUrlIndex < 0)
			currentUrlIndex = currentUrls.size() - 1;

		return currentUrls.get(currentUrlIndex);
	}

    public Player getFreePlayer()
    {
        for (int i = 0; i < players.size(); i++)
        {
            if (players.get(i).getType() != Player.PlayerType.Main)
                return players.get(i);
        }

        return null;
    }
	
    public Player getMainPlayer()
    {
        for (int i = 0; i < players.size(); i++)
        {
            if (players.get(i).getType() == Player.PlayerType.Main)
                return players.get(i);
        }

        return null;
    }

    public void closePlayers()
    {
        for (int i = 0; i < players.size(); i++)
        {
        	players.get(i).Close();
        }
    }

    public void bringToFront(final Player player)
    {
        if (player == null)
            return;

        // firstly show
        for (int i = 0; i < players.size(); i++)
        {
            if (player == players.get(i))
            {
                players.get(i).show();
                break;
            }
        }

        // hide others
        for (int i = 0; i < players.size(); i++)
        {
            if (player != players.get(i))
            {
                players.get(i).hide();
            }
        }

    }

	@Override
	public void onPlayerFirstVideoFrameAvailable(final int idPlayer)
	{
  		Log.e(TAG, "Test onPlayerFirstVideoFrameAvailable " + idPlayer);

  		final Player main = getMainPlayer();
  		
        runOnUiThread(new Runnable() 
        {
            @Override
            public void run() 
            {
         		//players.get(idPlayer).show();
            	bringToFront(players.get(idPlayer));
         		textviewCurrentUrl.setText(players.get(idPlayer).getPlayer().getConfig().getConnectionUrl());
                buttonVsync.setChecked(players.get(idPlayer).getPlayer().getConfig().getVsyncEnable() != 0);
        		
            	if (main != null && main.getPlayerId() != idPlayer)
            	{
            		//main.hide();
            		main.Close();
            	}
            	
	    		// temp for test
	    		String info = players.get(idPlayer).getPlayer().getStreamInfo();
        		Log.i(TAG, info);
	    		new AlertDialog.Builder(MainActivity.this)
	    		.setTitle("Info")
	    		.setMessage(info)
	    		.setPositiveButton("Close", new DialogInterface.OnClickListener() 
	    		{
	    			public void onClick(DialogInterface dialog, int which) 
	    			{
	    			}
	    		})
	    		.show();
            	
            }
        });  	
        
	}

	@Override
	public void onPlayerSurfaceCreated(final int idPlayer)
	{
  		Log.e(TAG, "Test onPlayerSurfaceCreated " + idPlayer);
        
	}

	private static final String TAG  = "MediaPlayerTest.SmoothSwitch";
}
