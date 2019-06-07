package com.vxg.videoplayertest.Views;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.vxg.videoplayertest.Listeners.VideoListener;
import com.vxg.videoplayertest.Models.SharedSettings;
import com.vxg.videoplayertest.R;
import com.vxg.videoplayertest.Views.Controls.player.VideoPlayer;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;


public class PlayBackActivity extends Activity {
	private final String TAG = PlayBackActivity.class.getSimpleName();
    private VideoPlayer mPlayer;
    
    private SeekBar timeLine;
    private TextView currentSpeed;
	private FrameLayout submitUrl;
    private FrameLayout btnSpeedDown;
    private FrameLayout btnSpeedUp;
    private FrameLayout btnPlay;
	
    private ArrayList<Integer> lowSpeeds;
    private ArrayList<Integer> highSpeeds;
	
    private SharedSettings sett;
    private EditText urlInput;
    private ImageView btnPlayIm;
    private TextView startTime;
    private TextView endTime;

	private Timer myTimer;
	boolean paused = true;

	int progress;
	

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_back);
        sett = SharedSettings.getInstance(this);
        initViews();
        setListeners();
        highSpeeds = new ArrayList<Integer>();
        lowSpeeds = new ArrayList<Integer>();
		highSpeeds.add(1);
		highSpeeds.add(2);
		highSpeeds.add(4);
		lowSpeeds.add(-1);
    }

    private void initViews() {
        btnSpeedDown 	= findViewById(R.id.btnSpeedDown);
        btnSpeedUp 		= findViewById(R.id.btnSpeedUp);
        mPlayer 		= (VideoPlayer) findViewById(R.id.player);
        timeLine 		= (SeekBar) findViewById(R.id.timeLine);
        currentSpeed 	= (TextView) findViewById(R.id.currentSpeed);
        btnPlay 		= findViewById(R.id.btnPlay);
        urlInput 		= findViewById(R.id.urlInput);
        
        btnPlayIm 		= findViewById(R.id.btnPlayIm);
        startTime 		= findViewById(R.id.startTime);
        endTime 		= findViewById(R.id.endTime);
		submitUrl 		= findViewById(R.id.submitUrl);
		progress		= 0;
    }

    private void setListeners(){
        submitUrl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            	// Set URL in preferred settings	
                Open(urlInput.getText().toString());
            }
        });
		
        timeLine.setOnSeekBarChangeListener(timeLineListener);
        btnSpeedDown.setOnClickListener(new View.OnClickListener() {
            int curSpeed = 0;
            @Override
            public void onClick(View view) {
                curSpeed++;
                if(curSpeed==lowSpeeds.size()){
                    curSpeed = 0;
                }
                int speed = lowSpeeds.get(curSpeed);

				currentSpeed.setText(Integer.toString(speed) + "x");
				mPlayer.pause();
                mPlayer.play(speed);
				
				
            }
        });

        btnSpeedUp.setOnClickListener(new View.OnClickListener() {
            int curSpeed = 0;
            @Override
            public void onClick(View view) {
                curSpeed++;
                if(curSpeed==highSpeeds.size()){
                    curSpeed = 0;
                }
                int speed = highSpeeds.get(curSpeed);

				currentSpeed.setText(Integer.toString(speed) + "x");
                mPlayer.pause();
                mPlayer.play(speed);
            }
        });

        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
              paused = mPlayer.isPaused();
              if(paused){
  			  	  btnPlayIm.setImageResource(R.drawable.black_play);
                  mPlayer.play();
              } else {
	              btnPlayIm.setImageResource(R.drawable.black_pause);
              	  mPlayer.pause();
				  
              }
            }
        });

		submitUrl.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				// Set URL in preferred settings	
				sett.URL = urlInput.getText().toString();
				sett.savePrefSettings();	
		
				// Start playback
				Open(urlInput.getText().toString());

				
			}
		});

		mPlayer.setVideoListener (new VideoListener() {
			
			public void onVideoReady()
				{
					return;
				}


            public void onVideoPlayed()
				{
					Log.v(TAG, "onVideoPlayed : black_pause");
					btnPlayIm.setImageResource(R.drawable.black_pause);
					paused = false;
					//return;
				}
            public void onVideoPaused()
				{
					Log.v(TAG, "onVideoPaused : black_play");
					btnPlayIm.setImageResource(R.drawable.black_play);
					paused = true;
					//return;
				}
            public void onVideoClosed()
				{
					//return;
				}

			});
		
    }


	private void TimerMethod()
	{
		this.runOnUiThread(Timer_Tick);
	}

	private String convertTimeToString(long sec)
		{

			long hours = sec/3600;
            long mins  = (hours != 0 )? sec%(hours*3600) : sec/60;
            long secs  = sec%60;

			String s = "";
			String s1 = "";
            s1 = s.format("%02d:%02d:%02d", hours, mins, secs);


			Log.v(TAG, "sec:" + sec + " s1:" + s1 + " hours:" + hours + " mins:" + mins + " secs:" + secs );
			
            return s1;

		}
	
	private Runnable Timer_Tick = new Runnable() {
		public void run() {

				 	
		 long pos = mPlayer.getPosition();
		 long dur = mPlayer.getDuration();
         Log.v(TAG, "Position:" + pos + " Duration:" + dur);
	
		 if (mPlayer.getPosition() >=  0)
		 	{
				 timeLine.setMax((int)dur);
				 endTime.setText(convertTimeToString(dur/1000));
			
				 timeLine.setProgress((int)pos);
				 startTime.setText(convertTimeToString(pos/1000));
		 	}

		 
						
		 
		}
	};


    private SeekBar.OnSeekBarChangeListener timeLineListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        	if (b)
	          progress = i;
		  Log.v(TAG, "onProgressChanged i:" + i + " progress:" + progress + " b:" + b);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
        
	        Log.v(TAG, "onStopTrackingTouch current:" + progress );
			mPlayer.pause();
			mPlayer.setPosition(progress);
			mPlayer.play();
        }
    };

    public void Open(String source){

		
	mPlayer.setContentProvider(1);
        mPlayer.setVideoSource(source);
        mPlayer.open();

		
		myTimer = new Timer();
		myTimer.schedule(new TimerTask() {
		@Override
		public void run() {
			TimerMethod();
		}
		
		}, 0, 1000);
    }
}
