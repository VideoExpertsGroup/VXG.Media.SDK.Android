package com.vxg.videoplayertest.Views;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Spinner;

import com.vxg.videoplayertest.Listeners.ScreenOrientationNeedChangeListener;
import com.vxg.videoplayertest.R;
import com.vxg.videoplayertest.Models.SharedSettings;
import com.vxg.videoplayertest.Views.Controls.player.VideoPlayer;
import com.vxg.videoplayertest.Utils.SpinnerItem;

import java.util.ArrayList;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;




public class Player1Activity extends Activity {
    private VideoPlayer mPlayer;
    private FrameLayout notPlayerContainer;
    private FrameLayout submitUrl;
	private FrameLayout	ButtonSet;
    private EditText 	urlInput;
	private EditText	bufferSize;
	private EditText	detectTime;
    private Spinner latencySpinner;
    private Spinner rtspSpinner;
	private SharedSettings sett;

    private ArrayList<SpinnerItem> latencySpinnerOptions;
    private ArrayList<SpinnerItem> rtpTransportoptions;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
    
        sett = SharedSettings.getInstance(this);
        
        
        notPlayerContainer 	= findViewById(R.id.notPlayerContainer);
        submitUrl 			= findViewById(R.id.submitUrl);
		ButtonSet			= findViewById(R.id.ButtonSet);
        urlInput 			= findViewById(R.id.urlInput);
		bufferSize			= findViewById(R.id.bufferSize);
		detectTime 			= findViewById(R.id.detectTime);

		
        submitUrl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            	// Set URL in preferred settings	
	            sett.URL = urlInput.getText().toString();
				sett.savePrefSettings();	

				// Start playback
                playVideo(urlInput.getText().toString());
            }
        });

        ButtonSet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

			sett.connectionBufferingTime	= Integer.parseInt(bufferSize.getText().toString());
			sett.connectionDetectionTime	= Integer.parseInt(detectTime.getText().toString());

			SpinnerItem item = (SpinnerItem)latencySpinner.getSelectedItem();
			sett.LatencyControl = item.value;

            item = (SpinnerItem)rtspSpinner.getSelectedItem();
            sett.connectionProtocol = item.value;

			sett.savePrefSettings();	
            }
        });
		

		// Latency control 
        latencySpinner = findViewById(R.id.latencySpinner);
        latencySpinnerOptions = new ArrayList<>();
        latencySpinnerOptions.add(new SpinnerItem("Disabled",0));
        latencySpinnerOptions.add(new SpinnerItem("Extra(No Sync)",1));
        latencySpinnerOptions.add(new SpinnerItem("Min (5 frames)",2));
        latencySpinnerOptions.add(new SpinnerItem("Middle (15 frames)",3));
        latencySpinnerOptions.add(new SpinnerItem("Max  (30 frames)",4));
        ArrayAdapter<SpinnerItem> arrayAdapter = new ArrayAdapter<SpinnerItem>(this, R.layout.layout_spinner_item, latencySpinnerOptions);
        latencySpinner.setAdapter(arrayAdapter);
        latencySpinner.setSelection(latencySpinnerOptions.indexOf(new SpinnerItem("",sett.LatencyControl)), false);
        latencySpinner.setOnItemSelectedListener(initSpinnerListener());

		
		// RTp transport
        rtspSpinner = findViewById(R.id.rtpTransport);
        rtpTransportoptions = new ArrayList<>();
        rtpTransportoptions.add(new SpinnerItem("AUTO(UDP, TCP, HTTP)",-2));
        rtpTransportoptions.add(new SpinnerItem("AUTO(TCP, HTTP)",-1));
        rtpTransportoptions.add(new SpinnerItem("UDP",0));
        rtpTransportoptions.add(new SpinnerItem("TCP",1));
        rtpTransportoptions.add(new SpinnerItem("HTTP",2));
        rtpTransportoptions.add(new SpinnerItem("HTTPS",3));
        ArrayAdapter<SpinnerItem> arrayAdapter1 = new ArrayAdapter<SpinnerItem>(this, R.layout.layout_spinner_item, rtpTransportoptions);
        rtspSpinner.setAdapter(arrayAdapter1);
		rtspSpinner.setSelection(rtpTransportoptions.indexOf(new SpinnerItem("",sett.connectionProtocol)), false);

		// Size of buffer that is accumilated on start 
		bufferSize.setText(String.valueOf(sett.connectionBufferingTime));
		// Detection time. Time to parce video and audio in stream
		detectTime.setText(String.valueOf(sett.connectionDetectionTime));

		mPlayer = (VideoPlayer)findViewById(R.id.player);
        mPlayer.setScreenListener(new ScreenOrientationNeedChangeListener() {
            @Override
            public void onNeedChangeScreenOrientation(boolean need) {
                toggleOrientation();
            }
        });

		urlInput.setText(sett.URL);		
;
    }

    private AdapterView.OnItemSelectedListener initSpinnerListener(){
        return new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        };
    }

    public void playVideo(String source){
        mPlayer.setVideoSource(source);
        mPlayer.open();
    }

    public void setUi(Configuration configuration){
        if(configuration.orientation == Configuration.ORIENTATION_LANDSCAPE){
            notPlayerContainer.setVisibility(GONE);
        } else {
            notPlayerContainer.setVisibility(VISIBLE);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration configuration){
         super.onConfigurationChanged(configuration);
         setUi(configuration);
    }

    @Override
    public void  onPause(){
        super.onPause();
        mPlayer.onPause();
    }

    @Override
    public void onResume(){
        super.onResume();
        mPlayer.onResume();
    }

    private void toggleOrientation() {
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }

}
